package net.ysq.webchat.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * @author passerbyYSQ
 * @create 2021-04-21 15:29
 */
public class DateTimeUtils {

    // Date => LocalDateTime
    public static LocalDateTime toLocalDateTime(Date date) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.systemDefault());
        return zonedDateTime.toLocalDateTime();
    }

    // LocalDateTime => Date
    public static Date toDate(LocalDateTime dateTime) {
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    // 相差多少个时间单位
    public static long dif(Temporal early, Temporal late, ChronoUnit unit) {
        return unit.between(early, late);
    }
}
