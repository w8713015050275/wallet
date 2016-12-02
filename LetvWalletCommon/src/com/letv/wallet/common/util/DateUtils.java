package com.letv.wallet.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by liuliang on 16-1-25.
 */
public class DateUtils {

    public static Date parseDate(String date, String pattern) {
        Date result = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            result = dateFormat.parse(date);
        } catch (Exception e) {
        }
        return result;
    }

    public static String formatDate(Date date, String pattern) {
        String result = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            result = dateFormat.format(date);
        } catch (Exception e) {
        }
        return result;
    }

    public static String convertPatternForDate(String date, String origPattern, String resultPattern) {
        Date dateObj = parseDate(date, origPattern);
        if (dateObj == null) {
            return null;
        }
        return formatDate(dateObj, resultPattern);
    }

    public static String convertPatternForDate(long date, String resultPattern) {
        Date dateObj = new Date(date);
        if (dateObj == null) {
            return null;
        }
        return formatDate(dateObj, resultPattern);
    }

    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }
        return isSameDay(date, new Date(System.currentTimeMillis()));
    }

    public static boolean isThisMonth(Date date) {
        if (date == null) {
            return false;
        }
        return isSameMonth(date, new Date(System.currentTimeMillis()));
    }

    public static boolean isThisYear(Date date) {
        if (date == null) {
            return false;
        }
        return isSameYear(date, new Date(System.currentTimeMillis()));
    }


    public static boolean isTomorrow(Date date) {
        if (date == null) {
            return false;
        }
        return isSameDay(date, new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));
    }

    public static boolean isSameDay(Date date, Date then) {
        if (date == null || then == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar thenCalendar = Calendar.getInstance();
        thenCalendar.setTime(then);
        return calendar.get(Calendar.YEAR) == thenCalendar.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == thenCalendar.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameMonth(Date date, Date then) {
        if (date == null || then == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar thenCalendar = Calendar.getInstance();
        thenCalendar.setTime(then);
        return calendar.get(Calendar.YEAR) == thenCalendar.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == thenCalendar.get(Calendar.MONTH);
    }

    public static boolean isSameYear(Date date, Date then) {
        if (date == null || then == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar thenCalendar = Calendar.getInstance();
        thenCalendar.setTime(then);
        return calendar.get(Calendar.YEAR) == thenCalendar.get(Calendar.YEAR);
    }

    public static String getTimeStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date(time));
        return date;
    }

    public static String getDayStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date(time));
        return date;
    }

    public static String getDayMMDDStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        String date = sdf.format(new Date(time));
        return date;
    }

    public static SimpleDateFormat getSimpleDateFormatByLocale(){
        String localeStr = Locale.getDefault().toString();
        if ("zh_HK".equals(localeStr) || "zh_CN".equals(localeStr) || "zh_TW".equals(localeStr) ) {
            return new SimpleDateFormat("yyyy-MM-dd");
        } else if ("in_ID".equals(localeStr)){
            return new SimpleDateFormat("dd-MM-yyyy ");
        }else if ("ru_RU".equals(localeStr)) {
            return new SimpleDateFormat("dd.MM.yyyy");
        } else {
            return new SimpleDateFormat("MM-dd-yyyy");
        }
    }
}
