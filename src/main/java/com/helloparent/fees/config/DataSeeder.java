package com.helloparent.fees.config;

import com.helloparent.fees.entity.*;
import com.helloparent.fees.repository.FeeRecordRepository;
import com.helloparent.fees.repository.PaymentRepository;
import com.helloparent.fees.repository.SchoolRepository;
import com.helloparent.fees.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Demo data so every API returns something right after startup.
 * Runs only when app.seed.enabled=true AND the DB has no schools (safe on restarts / real DBs).
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SchoolRepository schools;
    private final StudentRepository students;
    private final FeeRecordRepository fees;
    private final PaymentRepository payments;
    private final Clock clock;

    public DataSeeder(SchoolRepository schools, StudentRepository students, FeeRecordRepository fees,
                      PaymentRepository payments, Clock clock) {
        this.schools = schools;
        this.students = students;
        this.fees = fees;
        this.payments = payments;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (schools.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now(clock);

        School dps = schools.save(new School("Delhi Public School, Gurgaon", "DPS-GGN"));
        School kv = schools.save(new School("Kendriya Vidyalaya, Noida", "KV-NOIDA"));

        Student aarav = students.save(new Student("Aarav Sharma", "ADM-001", "5-A", dps));
        Student diya = students.save(new Student("Diya Verma", "ADM-002", "5-A", dps));
        Student kabir = students.save(new Student("Kabir Singh", "ADM-003", "7-B", dps));
        Student meera = students.save(new Student("Meera Iyer", "KV-101", "3-C", kv));

        // Aarav: tuition partly paid & overdue, transport pending
        FeeRecord f1 = fees.save(new FeeRecord(aarav, "2025-26", "TUITION", "Annual tuition", amt("50000"), today.minusDays(10)));
        pay(f1, "20000", today.minusDays(20), PaymentMode.UPI, "SEED-UPI-001");
        fees.save(new FeeRecord(aarav, "2025-26", "TRANSPORT", "Bus fee", amt("12000"), today.plusDays(30)));

        // Diya: fully paid
        FeeRecord f3 = fees.save(new FeeRecord(diya, "2025-26", "TUITION", "Annual tuition", amt("50000"), today.plusDays(15)));
        pay(f3, "50000", today.minusDays(5), PaymentMode.NET_BANKING, "SEED-NB-002");

        // Kabir: pending, overdue
        fees.save(new FeeRecord(kabir, "2025-26", "TUITION", "Annual tuition", amt("55000"), today.minusDays(3)));

        // Meera (other school): partly paid
        FeeRecord f5 = fees.save(new FeeRecord(meera, "2025-26", "TUITION", "Annual tuition", amt("30000"), today.plusDays(20)));
        pay(f5, "10000", today.minusDays(1), PaymentMode.CASH, null);

        log.info("Seeded demo data: 2 schools, 4 students, 5 fee records");
    }

    private void pay(FeeRecord fee, String amount, LocalDate date, PaymentMode mode, String ref) {
        fee.applyPayment(amt(amount));
        payments.save(new Payment(fee, amt(amount), date, mode, ref, "Seed payment"));
    }

    private static BigDecimal amt(String v) {
        return new BigDecimal(v).setScale(2);
    }
}
