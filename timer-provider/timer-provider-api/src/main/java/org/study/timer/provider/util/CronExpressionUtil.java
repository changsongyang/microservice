package org.study.timer.provider.util;

/**
 * @author chenyf on 2016/10/6.
 *
 * cron表达式的格式及意义（与unix的cron不一样），如
     Seconds (秒)         ：可以用数字0－59 表示
     Minutes(分)          ：可以用数字0－59 表示
     Hours(时)            ：可以用数字0-23表示
     Day-of-Month(天)     ：可以用数字1-31 中的任一一个值，但要注意一些特别的月份
     Month(月)            ：可以用0-11 或用字符串  “JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV and DEC” 表示
     Day-of-Week(每周)    ：可以用数字1-7表示（1 ＝ 星期日）或用字符串“SUN, MON, TUE, WED, THU, FRI and SAT”表示
     Year(年)

     “/”：为特别单位，表示为“每”如“0/15”表示每隔15分钟执行一次,“0”表示为从“0”分开始, “3/20”表示表示每隔20分钟执行一次，“3”表示从第3分钟开始执行
     “?”：表示每月的某一天，或第周的某一天
     “L”：用于每月，或每周，表示为每月的最后一天，或每个月的最后星期几如“6L”表示“每月的最后一个星期五”
     “W”：表示为最近工作日，如“15W”放在每月（day-of-month）字段上表示为“到本月15日最近的工作日”
     “#”：是用来指定“的”每月第n个工作日,例 在每周（day-of-week）这个字段中内容为"6#3" or "FRI#3" 则表示“每月第三个星期五”
     “*” 代表整个时间段.
 *  可到 http://cron.qqe2.com/ 使用工具生成
 *
 */
public class CronExpressionUtil {
    /**
     * 每隔多少秒执行一次，如： 每2秒执行一次，则输入： 2
     * @param seconds
     * @return
     */
    public static String getSecondlyExpression(int seconds){
        return String.format("*/%d * * * * ?", seconds);
    }

    /**
     * 每隔多少分钟执行一次，如： 每3分钟执行一次，则输入： 3
     * @param minutes
     * @return
     */
    public static String getMinutelyExpression(int minutes){
        return String.format("* */%d * * * ?", minutes);
    }

    /**
     * 每隔多少小时执行一次，如： 每5小时执行一次，则输入： 5
     * @param hours
     * @return
     */
    public static String getHourlyExpression(int hours){
        return String.format("0 * */%d * * ?", hours);
    }

    /**
     * 每天的某时、某分 执行，如： 每周三的12点25分执行，则输入： 12, 25
     * @param hour
     * @param minute
     * @return
     */
    public static String getDailyExpression(int hour, int minute){
        return String.format("0 %d %d * * ?", minute, hour);
    }

    /**
     * 每天的某时、某分 执行，如： 每周三的12点25分执行，则输入： 12, 25
     * @param hourStart 开始时间
     * @param hourEnd   结束时间
     * @param intervalMinute 间隔分钟
     * @return
     */
    public static String getDailyPeriodExpression(int hourStart, int hourEnd, int intervalMinute){
        return String.format("0 */%d %d-%d * * ?", intervalMinute, hourStart, hourEnd);
    }

    /**
     * 每周某几天的某时、某分 执行，如： 每周一、周三、周五的12点25分执行，则输入： 12, 25, 1,3,5
     * @param hour
     * @param minute
     * @param daysOfWeek
     * @return
     */
    public static String getWeeklyExpression(int hour, int minute, Integer... daysOfWeek){
        String cronExpression = String.format("0 %d %d ? * %s", minute, hour, getChineseWeekDay(daysOfWeek[0]));

        for (int i = 1; i < daysOfWeek.length; i++){
            cronExpression = cronExpression + "," + getChineseWeekDay(daysOfWeek[i]);
        }
        return cronExpression;
    }

    /**
     * 每月的某天或某几天、某时、某分 执行，如： 每月2号的12点25分执行，则输入： 2, 12, 25
     * @param hour
     * @param minute
     * @param dates
     * @return
     */
    public static String getMonthlyExpression(int hour, int minute, Integer... dates){
        String cronExpression = String.format("0 %d %d %d", minute, hour, dates[0]);
        for (int i = 1; i < dates.length; i++){
            cronExpression += "," + dates[i];
        }
        cronExpression += " * ?";
        return cronExpression;
    }

    private static String getChineseWeekDay(int weekday){
        weekday = weekday - 1;
        String[] weekdays = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        if(weekday < weekdays.length){
            return weekdays[weekday];
        }
        return "";
    }

    public static void main(String[] args){
        System.out.println(CronExpressionUtil.getSecondlyExpression(8));
    }
}
