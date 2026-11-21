package com.helloparent.fees;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end through HTTP -> service -> real JPA queries on a real PostgreSQL (Testcontainers).
 * Also proves the custom JPQL (pending list, summary, row lock) works on PostgreSQL.
 * Needs Docker; if Docker isn't running the test is skipped (unit tests still run).
 */
@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class FeeFlowIntegrationTest {

    /** One Postgres container for the class; @ServiceConnection wires its URL/user/password into Spring. */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mvc;

    private ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long id(ResultActions r) throws Exception {
        return ((Number) JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void fullFeeLifecycle() throws Exception {
        long schoolId = id(postJson("/api/v1/schools", """
                {"name":"IT School","code":"it-1"}""").andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value("IT-1")));

        long studentId = id(postJson("/api/v1/students", """
                {"schoolId":%d,"name":"Riya","admissionNumber":"a-1","grade":"6-b"}""".formatted(schoolId))
                .andExpect(status().isCreated()));

        String due = LocalDate.now().minusDays(1).toString(); // already overdue
        long feeId = id(postJson("/api/v1/fees", """
                {"studentId":%d,"academicYear":"2025-26","feeType":"tuition","totalAmount":1000,"dueDate":"%s"}"""
                .formatted(studentId, due))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.overdue").value(true)));

        // duplicate fee -> 409
        postJson("/api/v1/fees", """
                {"studentId":%d,"academicYear":"2025-26","feeType":"TUITION","totalAmount":5,"dueDate":"%s"}"""
                .formatted(studentId, due)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_FEE_RECORD"));

        // partial payment
        postJson("/api/v1/fees/" + feeId + "/payments", """
                {"amount":400,"mode":"UPI","transactionReference":"T-1"}""")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fee.status").value("PARTIAL"))
                .andExpect(jsonPath("$.fee.outstandingAmount").value(600.0));

        // same reference again -> 409
        postJson("/api/v1/fees/" + feeId + "/payments", """
                {"amount":100,"mode":"UPI","transactionReference":"T-1"}""")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_PAYMENT"));

        // overpayment -> 422
        postJson("/api/v1/fees/" + feeId + "/payments", """
                {"amount":600.01,"mode":"CASH"}""")
                .andExpect(status().isUnprocessableEntity());

        // pending list + summary (validates JPQL on a real DB)
        mvc.perform(get("/api/v1/fees/pending-students").param("schoolId", String.valueOf(schoolId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].outstandingAmount").value(600.0))
                .andExpect(jsonPath("$.content[0].overdue").value(true));

        mvc.perform(get("/api/v1/schools/" + schoolId + "/fee-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCollected").value(400.0))
                .andExpect(jsonPath("$.totalOutstanding").value(600.0))
                .andExpect(jsonPath("$.overdueAmount").value(600.0))
                .andExpect(jsonPath("$.collectionRatePercent").value(40.0))
                .andExpect(jsonPath("$.partialCount").value(1));

        // pay the rest -> PAID, then any further payment -> 409
        postJson("/api/v1/fees/" + feeId + "/payments", """
                {"amount":600,"mode":"CASH"}""").andExpect(jsonPath("$.fee.status").value("PAID"));
        postJson("/api/v1/fees/" + feeId + "/payments", """
                {"amount":1,"mode":"CASH"}""").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FEE_ALREADY_PAID"));

        mvc.perform(get("/api/v1/fees/" + feeId + "/payments").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mvc.perform(get("/api/v1/students/" + studentId + "/fees"))
                .andExpect(jsonPath("$.totalOutstanding").value(0.0));

        mvc.perform(get("/api/v1/fees/pending-students").param("schoolId", String.valueOf(schoolId)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void validationAndNotFoundErrors() throws Exception {
        postJson("/api/v1/fees", """
                {"studentId":1,"academicYear":"2025","feeType":"X","totalAmount":-5}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.academicYear").exists())
                .andExpect(jsonPath("$.fieldErrors.totalAmount").exists())
                .andExpect(jsonPath("$.fieldErrors.dueDate").exists());

        postJson("/api/v1/fees/1/payments", """
                {"amount":10,"mode":"BITCOIN"}""").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

        mvc.perform(get("/api/v1/fees/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/fees/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/fees/pending-students").param("size", "500")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/fees/pending-students").param("schoolId", "999999")).andExpect(status().isNotFound());
    }
}
