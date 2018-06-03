package top.ccxh.common.utils;

import java.time.*;
import java.util.Date;

/**
 * 提供新旧时间类转换
 * @author honey
 */
public class DateUtil {
    /**
     * java.util.Date --> java.time.LocalDateTime
     * @param date
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
         return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * java.util.Date --> java.time.LocalDate
     * @param date
     * @return
     */
    public static  LocalDate dateToLocalDate(Date date) {
        return dateToLocalDateTime(date).toLocalDate();
    }

    /**
     * java.util.Date --> java.time.LocalTime
     * @param date
     * @return
     */
    public static  LocalTime dateToLocalTime(Date date) {
        return dateToLocalDateTime(date).toLocalTime();
    }

    /**
     * java.time.LocalDateTime --> java.util.Date
     * @param localDateTime
     * @return
     */
    public static  Date localDateTimeToUdate(LocalDateTime localDateTime ) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }


    /**
     *  java.time.LocalDate --> java.util.Date
     * @param localDate
     * @return
     */
    public static  Date localDateToUdate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
       return Date.from(instant);
    }


    /**
     * java.time.LocalTime --> java.util.Date
     * @param localTime
     * @param localDate
     * @return
     */
    public static  Date localTimeToUdate(LocalTime localTime, LocalDate localDate) {
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }
}
