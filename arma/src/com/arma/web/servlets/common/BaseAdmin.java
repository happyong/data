/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arma.web.config.ConfigHelper;
import com.neulion.iptv.web.servlets.AbstractBaseComponent;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.KeyUtil;
import com.neulion.iptv.web.util.WebUtil;

public abstract class BaseAdmin extends AbstractBaseComponent
{
    private static final long serialVersionUID = -5815921147426312041L;
    protected boolean check_admin;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!ConfigHelper.rebooted)
            return;
        String admin = request.getParameter("admin");
        if (check_admin && !ConfigHelper.common.getKey4PortalAccount().equals(KeyUtil.encrypt(admin)))
        {
            request.setAttribute(WebUtil.RETURN_CODE, WebUtil.CODE_FAIL);
            PortalUtil.postDataAttribute("error", "check admin invalid", request);
            request.getRequestDispatcher(WebUtil.JSP_RETURN).forward(request, response);
            return;
        }
        super.service(request, response);
    }
}
