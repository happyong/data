/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.common;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arma.web.config.ConfigHelper;
import com.arma.web.servlets.portal.UserHelper;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.SkyUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.http.HttpClient4;

public class SkyAdmin extends BaseAdmin
{
	private static final long serialVersionUID = 4138778844818587318L;

	@Override
	// /servlets/admin/sky?admin=YsH10gE&type=sql&p1=
	// /servlets/admin/sky?admin=YsH10gE&type=file&p1=	
	// /servlets/admin/sky?admin=YsH10gE&type=xml&p1=
	// /servlets/admin/encrypt?admin=YsH10gE&code=neulion
	// /servlets/admin/time?admin=YsH10gE&time=
	// /servlets/admin/user?admin=YsH10gE&username=&password=
	// /servlets/admin/userdel?admin=YsH10gE&username=
	// /servlets/admin/shutdown?admin=YsH10gE
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{		
		String servlet = request.getRequestURI();
		servlet = servlet.substring(servlet.lastIndexOf("/") + 1);	
		String user = UserHelper.user(request), ip = SkyHelper.getAddress(request);
		Map<String, Object> map = WebUtil.param("result", false);
		
		if (SkyHelper.sky(servlet, request, response)) 
			return null;
		else if ("call".equals(servlet))
		{
			String url = request.getParameter("url");
			String resp = HttpClient4.createInstance().http(url, null);
			request.setAttribute("body", WebUtil.unull(resp));
			return WebUtil.FOLDER_JSP + WebUtil.FOLDER_COMPONENT + "/response.jsp";
		}
		else if ("encrypt".equals(servlet))
		{
			String code = request.getParameter("code");
			SkyHelper.encrypt(code, map);
		}
		else if ("time".equals(servlet))
		{
			String format = "yyyy-MM-dd HH:mm:ss.SSS";
			String time = request.getParameter("time");
			long ltime = WebUtil.str2long(time);
			Date date = (ltime > 0 ? new Date(ltime) : DateUtil.str24Date(time, format));
			if (date != null)
			{
				map.put("time", date.getTime());
				map.put("date", DateUtil.date24Str(date, format));
				map.put("result", true);
			}
		}
		else if ("system".equals(servlet))
		{
			String format = (WebUtil.FORMAT_JSON.equals(request.getParameter("format")) ? WebUtil.FORMAT_JSON : WebUtil.FORMAT_XML);
			SkyHelper.system(format, map);
		}
		else if ("user".equals(servlet))
		{
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String roles = request.getParameter("roles");
			UserHelper.userUpdate(true, user, ip, username, password, roles, map);
		}
		else if ("userdel".equals(servlet))
		{
			String username = request.getParameter("delname");
			if (user.equals(username))
				map.put("error", "not delete current user");
			else
				UserHelper.userUpdate(false, user, ip, username, null, null, map);
		}
		else if ("shutdown".equals(servlet))
			SkyHelper.shutdown(user, ip, map);

		return response("", WebUtil.FORMAT_XML, map, request);
	}

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		check_admin = true;
		SkyUtil.NAME_SKY_ADMIN = ConfigHelper.NAME_SKY_ADMIN;
	}
}
