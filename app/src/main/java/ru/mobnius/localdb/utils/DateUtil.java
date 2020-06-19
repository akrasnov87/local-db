package ru.mobnius.localdb.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static final String USER_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final String SYSTEM_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * Преобразовать дату в пользовательскую строку
     *
     * @param date дата
     * @return возврщается строка
     */
    public static String convertDateToUserString(Date date) {
        return convertDateToUserString(date, USER_FORMAT);
    }

    /**
     * Преобразование строки в дату
     * @param time время в милисекундах
     * @return результат преобразования
     */
    public static Date convertTimeToDate(String time) {
        return new Date(Long.parseLong(time));
    }

    /**
     * Преобразование строки в дату
     * @param date дата
     * @return результат преобразования
     * @throws ParseException исключение при неверном формате
     */
    public static Date convertStringToDate(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SYSTEM_FORMAT, Locale.getDefault());
        return dateFormat.parse(date);
    }

    /**
     * Дата преобразуется в строку с определнным форматом
     * @param date дата
     * @return возврщается строка
     */
    public static String convertDateToString(Date date) {
        return convertDateToString(date, SYSTEM_FORMAT);
    }

    /**
     * Дата преобразуется в строку с определнным форматом
     * @param date дата
     * @return возврщается строка
     */
    public static String convertDateToString(Date date, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    /**
     * Дата преобразуется в строку с определнным форматом
     * @param date дата
     * @param format формат даты
     * @return возврщается строка
     */
    public static String convertDateToUserString(Date date, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }
}