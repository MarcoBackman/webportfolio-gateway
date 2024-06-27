package com.marco.gateway.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil implements IDateTime {
    public static String getCurrentDateTimeFormatted() {
        LocalDateTime now = IDateTime.getCurrentDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
