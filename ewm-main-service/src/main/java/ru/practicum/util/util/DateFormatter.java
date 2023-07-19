package ru.practicum.util.util;

import lombok.experimental.UtilityClass;
import ru.practicum.exception.ValidationDateException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateFormatter {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final LocalDateTime date = LocalDateTime.of(LocalDate.of(1900, 1, 1), LocalTime.of(0, 0, 1));

    public static final LocalDateTime now = LocalDateTime.now();

    public static LocalDateTime formatDate(String date) {
        LocalDateTime newDate = null;
        if (date == null) {
            throw new ValidationDateException("Дата должна быть задана");
        }
        try {
            newDate = LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new ValidationDateException("Неверный формат даты");
        }
        return newDate;
    }
}
