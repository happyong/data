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
import com.arma.web.service.bean.UserLogin;
import com.neulion.iptv.web.servlets.AbstractBaseComponent;
import com.neulion.iptv.web.util.WebUtil;

public class Login extends AbstractBaseComponent
{
    private static final long serialVersionUID = 981945224268029476L;

    @Override
    // /servlets/login?username=admin&password=123456
            protected
            String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Map<String, Object> map = WebUtil.param("result", false);
        UserLogin user = UserHelper.userLogin(username, password);
        if (user != null && !user.empty())
        {
            request.getSession().setAttribute(ConfigHelper.USER_LOGIN, user);
            map.put("result", true);
        }
        return response("", WebUtil.FORMAT_JSON, map, request);
    }
}
