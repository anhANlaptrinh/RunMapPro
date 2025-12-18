package com.example.runmapproapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FormatUtils {

    /**
     * Convert distanceMeters to km with 2 decimal places
     * @param distanceMeters distance in meters
     * @return formatted string like "5.23 km"
     */
    public static String formatDistance(double distanceMeters) {
        double km = distanceMeters / 1000.0;
        return String.format(Locale.US, "%.2f km", km);
    }

    /**
     * Convert durationMs to hh:mm:ss format
     * @param durationMs duration in milliseconds
     * @return formatted string like "01:23:45"
     */
    public static String formatDuration(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Convert paceSecPerKm to m'ss"/km format
     * @param paceSecPerKm pace in seconds per km
     * @return formatted string like "5'30\"/km"
     */
    public static String formatPace(double paceSecPerKm) {
        if (paceSecPerKm <= 0 || Double.isInfinite(paceSecPerKm) || Double.isNaN(paceSecPerKm)) {
            return "--";
        }
        
        int totalSeconds = (int) Math.round(paceSecPerKm);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        
        return String.format(Locale.US, "%d'%02d\"/km", minutes, seconds);
    }

    /**
     * Format ISO-8601 datetime to local readable format
     * @param isoDateTime ISO-8601 string like "2025-12-16T10:30:00Z"
     * @return formatted string like "Dec 16, 2025 10:30"
     */
    public static String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "--";
        }
        
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            Date date = isoFormat.parse(isoDateTime);
            if (date == null) {
                // Try without milliseconds
                isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = isoFormat.parse(isoDateTime);
            }
            
            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return isoDateTime; // fallback
    }

    /**
     * Format ISO-8601 datetime to date only
     * @param isoDateTime ISO-8601 string
     * @return formatted string like "Dec 16, 2025"
     */
    public static String formatDate(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "--";
        }
        
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            Date date = isoFormat.parse(isoDateTime);
            if (date == null) {
                isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = isoFormat.parse(isoDateTime);
            }
            
            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return isoDateTime;
    }

    /**
     * Format number with comma separators
     * @param number any number
     * @return formatted string like "1,234"
     */
    public static String formatNumber(int number) {
        return String.format(Locale.US, "%,d", number);
    }

    /**
     * Format calories with no decimals
     * @param calories calorie value
     * @return formatted string like "350 kcal"
     */
    public static String formatCalories(double calories) {
        return String.format(Locale.US, "%.0f kcal", calories);
    }
}
