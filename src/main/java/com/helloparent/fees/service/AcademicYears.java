package com.helloparent.fees.service;

import com.helloparent.fees.exception.BadRequestException;

import java.util.regex.Pattern;

/** Validation helper for academic year strings like "2025-26". */
public final class AcademicYears {

    private static final Pattern FORMAT = Pattern.compile("^\\d{4}-\\d{2}$");

    private AcademicYears() { }

    /**
     * Returns the trimmed year, or null if input is null/blank (= "no filter").
     * Rejects bad format and non-consecutive years ("2025-27").
     */
    public static String validateOptional(String year) {
        if (year == null || year.isBlank()) {
            return null;
        }
        String y = year.trim();
        if (!FORMAT.matcher(y).matches()) {
            throw new BadRequestException("INVALID_ACADEMIC_YEAR", "academicYear must be in format YYYY-YY, e.g. 2025-26");
        }
        int start = Integer.parseInt(y.substring(0, 4));
        int end = Integer.parseInt(y.substring(5));
        if ((start + 1) % 100 != end) {
            throw new BadRequestException("INVALID_ACADEMIC_YEAR",
                    "academicYear must span consecutive years, e.g. " + start + "-" + String.format("%02d", (start + 1) % 100));
        }
        return y;
    }
}
