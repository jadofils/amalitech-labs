package main.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Names the date/time patterns used across the main.app, previously copy-pasted
 * as raw strings into independent {@code SimpleDateFormat} instances in
 * {@code Logger}, {@code Grade}, {@code ReportGenerator}, and
 * {@code BulkImportService}. Centralizing them here means a pattern only
 * needs to be read (and changed) in one place.
 */
public final class DateFormats {

    public static final String LOG_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
    public static final String FILE_SAFE_TIMESTAMP = "yyyyMMdd_HHmmss";
    public static final String DISPLAY_DATE_TIME = "dd-MM-yyyy HH:mm:ss";
    public static final String DISPLAY_DATE_SHORT_TIME = "dd-MM-yyyy HH:mm";
    public static final String DISPLAY_DATE = "dd-MM-yyyy";

    private DateFormats() {
    }

    /** A new SimpleDateFormat per call - SimpleDateFormat is not thread-safe to share. */
    public static String now(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }
}
