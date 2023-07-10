package ru.practicum.explorewithme.stats.server.util;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.stats.server.exception.ValidationDateException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateFormatter {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    public static LocalDateTime formatDate(String date) {
        LocalDateTime newDate = null;
        if (date == null) {
            throw new ValidationDateException("Date should be set");
        }
        try {
            newDate = LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new ValidationDateException("Wrong formate date");
        }
        return newDate;
    }
}
