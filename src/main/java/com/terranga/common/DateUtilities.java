package com.terranga.common;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtilities {

    public static String formaDateFromApiFootball(String date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return OffsetDateTime.parse(date).format(formatter);
    }

    // Saison API-Football : année de début de saison (bascule en juillet).
    public static int currentSeason() {
        LocalDate today = LocalDate.now();
        return today.getMonthValue() >= 7 ? today.getYear() : today.getYear() - 1;
    }
}
