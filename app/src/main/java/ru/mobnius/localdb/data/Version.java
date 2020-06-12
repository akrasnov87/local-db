package ru.mobnius.localdb.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Класс для рабоыт с версией приложения
 * Применение:
 * Version version = new Version();
 * // Проверяем валидность версии
 * version.isValid(versionNumber);
 * // Проверяем не пустая ли версия
 * version.isEmpty(versionNumber)
 */
public class Version {
    /**
     * Дата рождения приложения
     */
    public static final Date BIRTH_DAY = new GregorianCalendar(2020, 5, 12).getTime();
    /**
     * альфа версия
     */
    public static final int ALPHA = 0;
    /**
     * бета версия
     */
    public static final int BETA = 1;
    /**
     * релиз кандидан
     */
    public static final int RELEASE_CANDIDATE = 2;
    /**
     * публичный выпуск
     */
    public static final int PRODUCTION = 3;

    /**
     * Версия является пустой
     *
     * @param version версия приложения
     * @return результат
     */
    public boolean isEmpty(String version) {
        return version.equals("0.0.0.0");
    }

    /**
     * проверка на валидность номера версии
     *
     * @param version версия приложения
     * @return результат проверки
     */
    public boolean isValid(String version) {
        int[] parts = getVersionParts(version);
        if (parts != null) {
            if (parts[0] >= 0 && parts[1] >= 0 && parts[2] >= 0 && parts[3] >= 0) {
                return parts[2] <= 3 && parts[3] <= 24 * 60;
            }
        }
        return false;
    }

    /**
     * Получение даты публикации
     *
     * @param birthDay дата создания
     * @param version  номер версии
     * @return дата сборки приложения
     */
    public Date getBuildDate(Date birthDay, String version) {
        int[] parts = getVersionParts(version);
        if (parts != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(birthDay);
            cal.add(Calendar.DAY_OF_MONTH, parts[1]);
            cal.add(Calendar.MINUTE, parts[3]);
            return cal.getTime();
        }
        return null;
    }

    /**
     * Получение статуса версии
     *
     * @param version номер версии
     * @return возращается одно из значений: ALPHA, BETA, RELEASE_CANDIDATE, PRODUCTION, либо Null
     */
    public Integer getVersionState(String version) {
        int[] parts = getVersionParts(version);
        if (parts != null) {
            return parts[2];
        }
        return null;
    }

    /**
     * Получение частей версии
     *
     * @param version номер версии
     * @return массив чисел
     */
    public int[] getVersionParts(String version) {
        String[] data = version.split("\\.");
        if (data.length == 4) {
            try {
                int one = Integer.parseInt(data[0]);
                int two = Integer.parseInt(data[1]);
                int three = Integer.parseInt(data[2]);
                int four = Integer.parseInt(data[3]);

                int[] parts = new int[4];
                parts[0] = one;
                parts[1] = two;
                parts[2] = three;
                parts[3] = four;
                return parts;
            } catch (NumberFormatException ignored) {

            }
        }

        return null;
    }
}
