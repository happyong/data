package com.arma.web.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtil
{
    // 2011-07-25T11:00:00-0400
    public static final String df_zonz = "yyyy-MM-dd'T'HH:mm:ssZ";
    // 2011-07-25T11:00:00.000+0800
    public static final String df_zonz_lonz = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    // 2011-07-25T11:00:00.000
    public static final String df_long = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    // 2011-07-25 11:00:00.000 +0800
    public static final String df_zone_long = "yyyy-MM-dd HH:mm:ss.SSS Z";
    // 2011-07-25 11:00:00.000+0800
    public static final String df_zone_lonz = "yyyy-MM-dd HH:mm:ss.SSSZ";
    // 2011-07-25 11:00:00.000
    public static final String df_date_long = "yyyy-MM-dd HH:mm:ss.SSS";
    // 2011-07-25 11:00:00
    public static final String df_date_time = "yyyy-MM-dd HH:mm:ss";
    // 2011-07-25
    public static final String df_date = "yyyy-MM-dd";
    // 2011-07-25T11:00:00Z
    public static final String df_iso_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    // Mon, 25 Jul 2011 11:00:00 GMT
    public static final String df_rfc_1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    // ET (EST/EDT), Eastern Time
    public static final String zone_et = "America/New_York";
    // PT, Pacific Time
    public static final String zone_pt = "America/Los_Angeles";
    // CT, Central Time
    public static final String zone_ct = "America/Chicago";
    // MT, Mountain Time
    public static final String zone_mt = "America/Denver";
    // GMT+0800
    public static final String zone_cn = "GMT+0800";
    // UTC, Greenwich Time
    public static final String zone_utc = "GMT";

    public static final String date_min = "1970-01-01";
    public static final String date_max = "9999-12-31";

    public static String str(Date date)
    {
        return date24Str(date, df_date_time);
    }

    public static String str2(Date date)
    {
        return date24Str(date, df_date);
    }

    public static String str3(Date date)
    {
        return date24Str(date, df_date_long);
    }

    public static String strlz(long time)
    {
        return strlz(new Date(time));
    }

    public static String strlz(Date date)
    {
        return strlz(date, null);
    }

    public static String strlzu(long time)
    {
        return strlzu(new Date(time));
    }

    public static String strlzu(Date date)
    {
        return strlz(date, zone_utc);
    }

    public static String strlz(Date date, String zone)
    {
        return date24Str(date, df_zone_lonz, zone);
    }

    public static Date date(String str)
    {
        return str2Date(str, getDateFormat(df_date_time, null));
    }

    public static Date date2(String str)
    {
        return str2Date(str, getDateFormat(df_date, null));
    }

    public static Date date3(String str)
    {
        return str2Date(str, getDateFormat(df_date_long, null));
    }

    // HH:mm:ss
    public static String time(int duration)
    {
        if (duration < 1)
            return "00:00:00";
        int hour = duration / 3600, minute = (duration % 3600) / 60, second = duration % 60;
        return WebUtil.i2s(2, hour) + ":" + WebUtil.i2s(2, minute) + ":" + WebUtil.i2s(2, second);
    }

    // HH:mm:ss
    public static long time2ms(String str)
    {
        if (WebUtil.empty(str))
            return 0L;
        String[] arr = str.split(":");
        if (arr == null || arr.length != 3)
            return 0L;
        int pos = arr[2].indexOf("."), ms = 0;
        if (pos != -1)
        {
            ms = WebUtil.str2int(arr[2].substring(pos + 1));
            arr[2] = arr[2].substring(0, pos);
        }
        return ms + WebUtil.str2int(arr[2]) * 1000L + WebUtil.str2int(arr[1]) * 60000L + WebUtil.str2int(arr[0]) * 3600000L;
    }

    public static String date24Str(Date date, String format)
    {
        if (date == null)
            return "";
        return date2Str(date, getDateFormat(format, null));
    }

    public static String date24Str(Date date, String format, String zone)
    {
        if (date == null)
            return "";
        return date2Str(date, getDateFormat(format, zone));
    }

    public static String date2Str(Date date, SimpleDateFormat df)
    {
        if (date == null)
            return "";
        if (df == null)
            df = getDateFormat(null, null);
        return df.format(date);
    }

    public static Date str24Date(String str)
    {
        if (WebUtil.empty(str))
            return null;
        Date date = str2Date(str, getDateFormat(df_date_time, null));
        if (date == null)
            date = str2Date(str, getDateFormat(df_date, null));
        return date;
    }

    public static Date str24Date(String str, String format)
    {
        if (WebUtil.empty(str))
            return null;
        return str2Date(str, getDateFormat(format, null));
    }

    public static Date str24Date(String str, String format, String zone)
    {
        if (WebUtil.empty(str))
            return null;
        return str2Date(str, getDateFormat(format, zone));
    }

    public static Date str2Date(String str, SimpleDateFormat df)
    {
        if (WebUtil.empty(str))
            return null;

        Date date = null;
        if (df == null)
            df = getDateFormat(null, null);
        try
        {
            date = df.parse(str);
        }
        catch (Exception e)
        {
        }
        return date;
    }

    public static long str2min(String base)
    {
        Date date = str24Date(base, "HH:mm", zone_cn);
        return (date == null ? 0L : date.getTime());
    }

    public static String shiftMinute(int minute, long base)
    {
        return date24Str(new Date(base + minute * 60000L), "HH:mm", zone_cn);
    }

    public static SimpleDateFormat getDateFormat(String format, String zone)
    {
        if (WebUtil.empty(format))
            format = df_long;
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.US);
        if (!WebUtil.empty(zone))
        {
            TimeZone time_zone = TimeZone.getTimeZone(zone);
            df.setTimeZone(time_zone);
        }
        return df;
    }

    // yyyy-MM-dd
    public static String shiftDays(String str, int days)
    {
        if (days == 0 || WebUtil.empty(str))
            return str;
        Date date = date2(str);
        if (date == null)
            return str;
        date.setTime(date.getTime() + (86400000L * days));
        str = str2(date);
        return str;
    }

    // yyyy-MM-dd HH:mm:ss
    public static String shiftStrDateTime(String str, int delta, TimeUnit unit)
    {
        if (delta == 0 || WebUtil.empty(str))
            return str;
        Date date = str24Date(str, df_date_time);
        if (date == null)
            return str;
        shiftDateTime(date, delta, unit);
        str = date24Str(date, df_date_time);
        return str;
    }

    // yyyy-MM-dd HH:mm:ss
    public static void shiftDateTime(Date date, int deta, TimeUnit unit)
    {
        if (deta == 0 || date == null)
            return;

        long time = 1000L * deta;
        if (unit == TimeUnit.MINUTES)
            time *= 60L;
        else if (unit == TimeUnit.HOURS)
            time *= 3600L;
        else if (unit == TimeUnit.DAYS)
            time *= 86400L;
        date.setTime(date.getTime() + time);
    }

    public static long shiftZero(Date date)
    {
        if (date == null)
            return 0L;
        Calendar cal = new GregorianCalendar();
        Calendar base = new GregorianCalendar();
        base.clear();
        cal.setTime(date);
        base.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
        return base.getTimeInMillis();
    }

    // yyyy-MM-dd
    public static int compareDate(String date, String begin, String end)
    {
        if (WebUtil.empty(date))
            return 0;
        if (WebUtil.empty(begin))
            begin = date_min;
        if (WebUtil.empty(end))
            end = date_max;
        if (begin.compareTo(date) > 0)
            return -1;
        else if (end.compareTo(date) < 0)
            return 1;
        return 0;
    }

    public static int days(Date date1, Date date2)
    {
        if (date1 == null || date2 == null)
            return 0;

        long time1, time2;
        Calendar cal = new GregorianCalendar();
        Calendar base = new GregorianCalendar();

        base.clear();
        cal.setTime(date1);
        base.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
        time1 = base.getTimeInMillis();
        base.clear();
        cal.setTime(date2);
        base.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
        time2 = base.getTimeInMillis();
        return (int) ((time2 - time1) / 86400000L);
    }
}
