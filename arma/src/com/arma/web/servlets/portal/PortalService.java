/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.portal;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arma.web.config.ConfigHelper;
import com.arma.web.service.bean.UserLogin;
import com.arma.web.servlets.kms.KmsCacher;
import com.arma.web.util.ArmaUtil;
import com.arma.web.util.InVarAM;
import com.arma.web.util.SkyHelper;
import com.neulion.iptv.web.servlets.AbstractBaseComponent;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;

public class PortalService extends AbstractBaseComponent
{
	private static final long serialVersionUID = 1375736121664752971L;

	@Override
	// /servlets/portal/funds
    // /servlets/portal/kms
	// /servlets/portal/charts
	// /servlets/portal/system
	// /servlets/portal/locale
	// /servlets/portal/login
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		String servlet = request.getRequestURI();
		servlet = servlet.substring(servlet.lastIndexOf("/") + 1);	
		if (WebUtil.empty(servlet)) servlet = "console";
		Map<String, Object> result = new HashMap<String, Object>();

		if ("funds".equals(servlet)) 
			funds(result, request);
		else if ("kms".equals(servlet)) 
            kms(result, request);
        else if ("charts".equals(servlet)) 
			charts(result, request);
		else if ("system".equals(servlet)) 
			system(result, request);
		else if ("locale".equals(servlet)) 
			return locale(result, request, response);

		result.put("menus", PortalCacher.getPortalMenus());
		result.put("portals", ConfigHelper.common.getPortals());
		if (!WebUtil.empty(request.getParameter("detid"))) request.setAttribute("detid", request.getParameter("detid"));
		request.setAttribute("result", result);
		UserLogin user = UserHelper.user2(request);
		return PortalCacher.getResponsePage(servlet, user);
	}

	private void funds(Map<String, Object> result, HttpServletRequest request)
	{
		result.put("maxRecords", "200");
		result.put("account", WebUtil.KEY_ADMIN);
		String now = DateUtil.date24Str(new Date(), DateUtil.df_date_time, DateUtil.zone_cn), now_date = now.substring(0, 10), now_time = now.substring(11, 16);
		result.put("last_trade", (ArmaUtil.trade(now_date) && InVarAM.s_trade_times[6].compareTo(now_time) < 0 ? now_date : ArmaUtil.shiftTrade(-1, now_date)));
		result.put("jsfunds", PortalCacher.getJsWords("jsfunds", PortalUtil.getCurrentLocale(request)));
	}

    private void kms(Map<String, Object> result, HttpServletRequest request)
    {
        result.put("maxRecords", "2000");
        result.put("account", WebUtil.KEY_ADMIN);
        result.put("sepv", WebUtil.sep_kval);
        result.put("sepke", WebUtil.sep_kenum);
        result.put("kmsmeta", KmsCacher.outputMeta());
        result.put("jskms", PortalCacher.getJsWords("jskms", PortalUtil.getCurrentLocale(request)));
    }

	private void charts(Map<String, Object> result, HttpServletRequest request)
	{	
		result.put("canTimer", !"stop".equals(request.getParameter("timer")));
		result.put("jscharts", PortalCacher.getJsWords("jscharts", PortalUtil.getCurrentLocale(request)));
	}

	private void system(Map<String, Object> result, HttpServletRequest request)
	{	
		result.put("account", WebUtil.KEY_ADMIN);
		SkyHelper.system(WebUtil.FORMAT_JAVA, result);
		UserLogin user = UserHelper.user2(request);
		if (user != null && user.isAdmin()) result.put("users", UserHelper.users());
		result.put("jssystem", PortalCacher.getJsWords("jssystem", PortalUtil.getCurrentLocale(request)));
	}

	@SuppressWarnings("unchecked")
	private String locale(Map<String, Object> result, HttpServletRequest request, HttpServletResponse response)
	{	
		String locale = request.getParameter("locale");
		String format = (WebUtil.FORMAT_JSON.equals(request.getParameter("format")) ? WebUtil.FORMAT_JSON : WebUtil.FORMAT_XML);
		result.put("result", false);
		do
		{
			if (WebUtil.empty(locale)) break;
			Map<String, Object> supportedLocales = (Map<String, Object>)getServletContext().getAttribute("locales");
			if (supportedLocales == null || supportedLocales.size() == 0) break;
			if (supportedLocales.size() == 1)
			{
				Locale newlocale = PortalUtil.parseLocale(locale);
				if (newlocale == null) break;
				request.getSession().setAttribute("locale", newlocale);
				PortalUtil.setCookie("locale", locale, 7 * 86400, null, response);
				result.put("result", true);
			}
			else if (supportedLocales.containsKey(locale))
			{
				PortalUtil.setCurrentLocale(request, response, locale, null); 
				result.put("result", true);
			}
		}
		while (false);
		return response("", format, result, request);
	}
}
