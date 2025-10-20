package com.example.ProjectTeam121.utils;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


public class DateUtils {

    public static final String DEFAULT_PATTERN = "dd-MM-yyyy HH:mm:ss";
    public static final String DATE_ONLY_PATTERN = "dd-MM-yyyy";

    /** Lấy thời gian hiện tại dạng LocalDateTime */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /** Lấy thời gian hiện tại dạng chuỗi (dd-MM-yyyy HH:mm:ss) */
    public static String nowString() {
        return format(LocalDateTime.now(), DEFAULT_PATTERN);
    }

    /** Định dạng LocalDateTime thành chuỗi */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_PATTERN);
    }

    /** Định dạng LocalDateTime theo pattern tùy chọn */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /** Chuyển chuỗi thành LocalDateTime */
    public static LocalDateTime parse(String dateStr) {
        return parse(dateStr, DEFAULT_PATTERN);
    }

    /** Chuyển chuỗi thành LocalDateTime theo pattern tùy chọn */
    public static LocalDateTime parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateStr, formatter);
    }

    /** Cộng thêm ngày */
    public static LocalDateTime plusDays(LocalDateTime dateTime, int days) {
        return dateTime.plusDays(days);
    }

    /** Trừ bớt ngày */
    public static LocalDateTime minusDays(LocalDateTime dateTime, int days) {
        return dateTime.minusDays(days);
    }

    /** Tính số ngày giữa 2 thời điểm */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /** Chuyển từ LocalDateTime sang chuỗi ngày (dd-MM-yyyy) */
    public static String toDateString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(DATE_ONLY_PATTERN));
    }
}
