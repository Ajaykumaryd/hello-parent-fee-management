package com.helloparent.fees.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /** Injected wherever "today" is needed -> tests can freeze time. School-local timezone. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Kolkata"));
    }

    /** Swagger header info. UI at /swagger-ui.html, spec at /v3/api-docs. */
    @Bean
    public OpenAPI feeManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Student Fee Management API")
                .version("1.0.0")
                .description("Create fee records, record payments, view history, pending fees and school summaries. "
                        + "All amounts are in INR with 2 decimals. Errors share one JSON shape (ErrorResponse)."));
    }
}
