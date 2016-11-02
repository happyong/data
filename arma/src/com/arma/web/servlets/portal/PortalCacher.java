/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.portal;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.arma.web.config.ConfigHelper;
import com.arma.web.config.ConfigHelper.PortalMenu;
import com.arma.web.service.bean.UserLogin;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.FileUtil;
import com.neulion.iptv.web.util.WebUtil;

public class PortalCacher
{	
	private static Map<String, Object> _cache = new ConcurrentHashMap<String, Object>();
	private static Map<String, Object> _map = new ConcurrentHashMap<String, Object>();
	
	public static void updatePortalCacher()
	{	
		_cache.clear();
	}
	
	public static String getResponsePage(String servlet, UserLogin user)
	{
		String result = ConfigHelper.PAGE_404;
		if (WebUtil.empty(servlet) || !ConfigHelper.common.isShowPortalService() || !show(servlet, user)) return result;
		String key = "page_" + servlet.toLowerCase();
		result = (String)_cache.get(key);
		if (result != null) return result;

		String page = "/WEB-INF/portal/" + servlet.toLowerCase() + ".jsp", path = GlobalCache._config_root + "/../.." + page;
		result = (FileUtil.exist(1, path) != null ? page : ConfigHelper.PAGE_404);
		_cache.put(key, result);
		return result;
	}
	
	public static boolean show(String servlet, UserLogin user)
	{
		boolean admin = (user != null && user.isAdmin());
		boolean schedule = (user != null && user.isSchedule());
		boolean task = (user != null && user.isTask());
	
		if ("charts".equals(servlet)) 
			return ((admin || schedule || task) && ConfigHelper.common.show(PortalMenu.charts));
		return true;
	}
	
	public static String getPortalMenus()
	{
		String key = "portal_menus";
		String result = (String)_cache.get(key);
		if (result != null) return result;

		result = "";
		for (PortalMenu menu : PortalMenu.values()) result += "," + menu.name();
		result = WebUtil.str(result);
		_cache.put(key, result);
		return result;
	}
	
	public static String getJsWords(String name, Locale locale)
	{
		if (locale == null) locale = Locale.US;
		String key = "jswords_" + name + "_" + PortalUtil.formatLocale(locale);
		String result = (String)_map.get(key);
		if (result != null) return result;

		StringBuffer sb = new StringBuffer();
		do
		{
			ResourceBundle bundle = PortalUtil.getBundle("i18n." + name, locale);
			if (bundle == null) break;
			for (String str : bundle.keySet())
			{
				// <div id="msg_login_account_alert"><fmt:message key="login_account_alert" /></div>
				sb.append("<div id=\"msg_").append(str).append("\">");
				sb.append(bundle.getString(str)).append("</div>");
			}
		}
		while(false);
		result = sb.toString();
		_map.put(key, result);
		return result;
	}
}
