/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.arma.web.service.bean.TSymbol;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;

public class ArmaUtil
{	
	public static boolean hitf(TSymbol bean)
	{
		if (bean == null || bean.empty()) return false;
		return (hitfc(bean.getCode()) || WebUtil.hit(bean.getName(), InVarAM.s_fund_focus) != -1 || WebUtil.hit(bean.getNameS(), InVarAM.s_fund_focus) != -1);
	}
	
	public static boolean hitfc(String code)
	{
		return (WebUtil.hit(code, InVarAM.s_fund_focus) != -1);
	}
	
	public static boolean hitfi(String code)
	{
		for (String[] arr : InVarAM.s_fund_include) 
			if ((arr.length == 1 && code.equals(arr[0])) || (arr.length > 1 && code.compareTo(arr[0]) >= 0 && code.compareTo(arr[1]) <= 0)) return true;
		return false;
	}
	
	public static boolean hitfe(String code)
	{
		for (String[] arr : InVarAM.s_fund_exclude) 
			if ((arr.length == 1 && code.equals(arr[0])) || (arr.length > 1 && code.compareTo(arr[0]) >= 0 && code.compareTo(arr[1]) <= 0)) return true;
		return false;
	}

	// yyyy-MM-dd
	public static String shiftTrade(int days, String base)
	{
		if (days == 0) return base;
		Date date = DateUtil.date2(base);
		if (date == null) return null;
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		
		int max = Math.abs(days);
		for (int i = 0; i < max;)
		{
			cal.add(Calendar.DATE, max == days ? 1 : -1);
			if (hittc(cal)) continue;
			i++;
		}
		return DateUtil.str2(cal.getTime());
	}
	
	public static int daysTrade(String base, String to)
	{
		if (WebUtil.empty(base) || WebUtil.empty(to) || base.compareTo(to) >= 0) return 0;
		Date d_base = DateUtil.date2(base), d_to = DateUtil.date2(to);
		if (d_base == null || d_to == null) return 0;

		int days = 0;
		long t_to = d_to.getTime();
		Calendar c_base = new GregorianCalendar();
		c_base.setTime(d_base);
		for (int i = 0; i < 1000; i++)
		{
			c_base.add(Calendar.DATE, 1);
			if (hittc(c_base)) continue;
			days++;
			if (c_base.getTimeInMillis() >= t_to) break;
		}
		return days;
	}
	
	public static boolean trade(String date)
	{
		Date d = DateUtil.date2(date);
		if (d == null) return false;
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		return !hittc(cal);
	}
	
	private static boolean hittc(Calendar cal)
	{
		int weekday = cal.get(Calendar.DAY_OF_WEEK);
		if (weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY) return true;
		String ret = DateUtil.str2(cal.getTime());
		for (String[] arr : InVarAM.s_trading_close)  if ((arr.length == 1 && ret.equals(arr[0])) || (arr.length > 1 && ret.compareTo(arr[0]) >= 0 && ret.compareTo(arr[1]) <= 0)) return true;
		return false;
	}
}
