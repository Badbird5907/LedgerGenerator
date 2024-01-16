package dev.badbird.ledger.util;

import java.time.Month;

public class DateUtil {
    public static Month parseMonth(String in) { // LMFAO
        return switch (in.toLowerCase()) {
            case "jan", "january" -> Month.JANUARY;
            case "feb", "february" -> Month.FEBRUARY;
            case "mar", "march" -> Month.MARCH;
            case "apr", "april" -> Month.APRIL;
            case "may" -> Month.MAY;
            case "jun", "june" -> Month.JUNE;
            case "jul", "july" -> Month.JULY;
            case "aug", "august" -> Month.AUGUST;
            case "sep", "september" -> Month.SEPTEMBER;
            case "oct", "october" -> Month.OCTOBER;
            case "nov", "november" -> Month.NOVEMBER;
            case "dec", "december" -> Month.DECEMBER;
            default -> null;
        };
    }
}
