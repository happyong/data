/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.portal;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arma.web.config.ConfigHelper;
import com.neulion.iptv.web.servlets.AbstractBaseComponent;
import com.neulion.iptv.web.util.KeyUtil;
import com.neulion.iptv.web.util.WebUtil;

public class Logout extends AbstractBaseComponent
{
	private static final long serialVersionUID = -7854430574886965350L;

	@Override
	@SuppressWarnings("unchecked")
	// /servlets/logout?admin=YsH10gE&account=false&page=false
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		String format = (WebUtil.FORMAT_JSON.equals(request.getParameter("format")) ? WebUtil.FORMAT_JSON : WebUtil.FORMAT_XML);
		request.getSession().invalidate();
		Map<String, Object> map = WebUtil.param("result", true);

		String admin = request.getParameter("admin");
		Map<String, Object> params = request.getParameterMap();
		if (ConfigHelper.common.getKey4PortalAccount().equals(KeyUtil.encrypt(admin)))
		{
			if (params.containsKey("service"))	ConfigHelper.common.setShowPortalService("true".equals(request.getParameter("service")));
			PortalCacher.updatePortalCacher();
		}
		return response("", format, map, request);
	}
}
