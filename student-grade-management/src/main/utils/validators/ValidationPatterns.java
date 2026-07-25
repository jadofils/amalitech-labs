package main.utils.validators;

import java.util.regex.Pattern;

/**
 * Single source of truth for every regex used to validate structured input (US-3/PBI-3) -
 * {@link StudentValidator}, {@link SubjectValidator}, and {@link GradeValidator} all reference
 * these instead of declaring their own inline {@code Pattern}s, so a format only needs to be read
 * (and changed) in one place.
 */
public final class ValidationPatterns {

    /**
     * {@code STU} followed by 3+ digits. {@code {3,}}, not an exact {@code {3}}, because
     * {@link main.model.student.Student#getStudentId} generates IDs via {@code String.format("STU%03d", ...)} -
     * zero-padded to 3 digits but unbounded above 999, so a strict 3-digit pattern would start
     * rejecting real, auto-generated IDs the moment a 1000th student is added.
     */
    public static final Pattern STUDENT_ID = Pattern.compile("^STU\\d{3,}$");

    /** Same reasoning as {@link #STUDENT_ID}: {@code GRD} + 3+ digits, matching {@code Grade}'s own {@code GRD%03d} generator. */
    public static final Pattern GRADE_ID = Pattern.compile("^GRD\\d{3,}$");

    public static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Local format: exactly 10 digits, no separators (e.g. {@code 1234567890}). */
    public static final Pattern PHONE_LOCAL = Pattern.compile("^\\d{10}$");

    /** International format: a leading {@code +}, 1-3 digit country code, then dash-separated 3-4 digit groups (e.g. {@code +1-555-0101}). */
    public static final Pattern PHONE_INTERNATIONAL = Pattern.compile("^\\+\\d{1,3}-\\d{3}-\\d{4}$");

    /** ISO-8601 calendar date, {@code YYYY-MM-DD}. Not currently used to validate {@code Grade.getDate()} -
     *  see {@link GradeValidator}'s class Javadoc for why. Provided for any future structured input
     *  that carries an explicit ISO date. */
    public static final Pattern DATE_ISO = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /** Course/subject code: 2+ uppercase letters then 2+ digits (e.g. {@code ENG101}, {@code MATH01}). */
    public static final Pattern COURSE_CODE = Pattern.compile("^[A-Z]{2,}\\d{2,}$");

    private ValidationPatterns() {
    }
}
