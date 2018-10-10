package org.study.common.util.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @author: chenyf
 * @Date: 2018/1/5
 */
public class DateUtil {
    public static final String DATE_ONLY_REGEX = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$";
    public static final String DATE_TIME_REGEX = "^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])(\\s+(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d)$";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormat.forPattern("yyMMdd");

    /**
     * 比较 source 和 target 大小，如果 source > target 则返回1，如果 source = target 则返回0，如果 source < target 则返回-1
     * @param source
     * @param target
     * @return
     */
    public static int compare(Date source, Date target, int withUnit) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(source);

        Calendar otherDateCal = Calendar.getInstance();
        otherDateCal.setTime(target);
        switch (withUnit) {
            case Calendar.YEAR:
                dateCal.clear(Calendar.MONTH);
                otherDateCal.clear(Calendar.MONTH);
            case Calendar.MONTH:
                dateCal.set(Calendar.DATE, 1);
                otherDateCal.set(Calendar.DATE, 1);
            case Calendar.DATE:
                dateCal.set(Calendar.HOUR_OF_DAY, 0);
                otherDateCal.set(Calendar.HOUR_OF_DAY, 0);
            case Calendar.HOUR:
                dateCal.clear(Calendar.MINUTE);
                otherDateCal.clear(Calendar.MINUTE);
            case Calendar.MINUTE:
                dateCal.clear(Calendar.SECOND);
                otherDateCal.clear(Calendar.SECOND);
            case Calendar.SECOND:
                dateCal.clear(Calendar.MILLISECOND);
                otherDateCal.clear(Calendar.MILLISECOND);
            case Calendar.MILLISECOND:
                break;
            default:
                throw new IllegalArgumentException("withUnit 单位字段 " + withUnit + " 不合法！！");
        }
        return dateCal.compareTo(otherDateCal);
    }

    public static Date addDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    public static Date addMinute(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public static Date addSecond(Date date, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    /**
     * 判断是否日期格式，如：2017-05-31
     * @param str
     * @return
     */
    public static boolean isDateOnly(String str){
        Matcher mat = Pattern.compile(DATE_ONLY_REGEX).matcher(str);
        return mat.matches();
    }

    /**
     * 判断是否日期或者日期和时间格式，如：2017-05-31 或者 2017-05-31 15:24:31
     * @return
     */
    public static boolean isTimeFormat(Object obj){
        if(obj == null){
            return false;
        }
        if(obj instanceof Date){
            return true;
        }
        Matcher mat = Pattern.compile(DATE_TIME_REGEX).matcher(obj.toString());
        return mat.matches();
    }

    public static String formatDateTime(Date date){
        DateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(DATE_TIME_FORMATTER);
    }

    public static String formatDate(Date date){
        DateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(DATE_FORMATTER);
    }

    public static String formatShortDate(Date date){
        DateTime dateTime = parseJodaDateTime(date);
        return dateTime.toString(SHORT_DATE_FORMATTER);
    }

    public static DateTime parseJodaDateTime(Date date){
        return new DateTime(date);
    }
}
