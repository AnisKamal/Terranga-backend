package com.terranga.common;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtilities {

    public static String formaDateFromApiFootball(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return OffsetDateTime.parse(date).format(formatter);
    }
}
