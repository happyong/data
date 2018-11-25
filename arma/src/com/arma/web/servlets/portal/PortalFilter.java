/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.portal;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.arma.web.config.ConfigHelper;
import com.arma.web.service.bean.UserLogin;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.WebUtil;

public class PortalFilter implements Filter
{
    protected FilterConfig _config;

    @Override
    public void init(FilterConfig config) throws ServletException
    {
        _config = config;
        ServletContext ctx = config.getServletContext();
        String str = ctx.getInitParameter("supportedLocales");
        if (!WebUtil.empty(str))
        {
            Map<String, Object> supportedLocales = new LinkedHashMap<String, Object>();
            String[] locales = str.split(",");
            for (int i = 0; i < locales.length; i++)
            {
                String[] locale = locales[i].split(WebUtil.KEY_DELIMITER);
                if (locale.length == 2)
                    supportedLocales.put(locale[0], new Object[] { PortalUtil.parseLocale(locale[0]), locale[1] });
            }
            if (supportedLocales.size() > 0)
                ctx.setAttribute("locales", supportedLocales);
            else
                ctx.setAttribute("single_locale", ((Object[]) supportedLocales.values().iterator().next())[0]);
        }
        else
            ctx.setAttribute("single_locale", Locale.US);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession();

        Locale locale1 = PortalUtil.getCurrentLocale(request);
        Locale locale2 = (Locale) session.getAttribute("locale");
        if (locale1 == null || !locale1.equals(locale2))
            PortalUtil.setCurrentLocale(request, (HttpServletResponse) resp, (locale1 == null ? null : PortalUtil.formatLocale(locale1)), null);

        String servlet = request.getRequestURI();
        servlet = servlet.substring(servlet.lastIndexOf("/") + 1);
        String login_mode = ConfigHelper.common.getLoginMode();
        UserLogin user = UserHelper.user2(request);
        boolean no_login = (user == null);
        boolean login_nl = "user@neulion.com.cn".equals(login_mode);
        if (no_login && login_nl)
        {
            no_login = false;
            String user_name = request.getHeader("Remote-User");
            if (WebUtil.empty(user_name))
                user_name = "guest";
            user = UserHelper.userLogin(user_name);
            if (user == null)
                user = new UserLogin();
            if (user.empty())
                user.setName(user_name);
            session.setAttribute(ConfigHelper.USER_LOGIN, user);
        }
        if (no_login && !servlet.startsWith("login"))
        {
            // ((HttpServletResponse)resp).sendRedirect(request.getContextPath() + "/servlets/portal/login");
            req.getRequestDispatcher(PortalCacher.getResponsePage("login", user)).forward(req, resp);
            return;
        }
        request.setAttribute(ConfigHelper.USER_LOGIN, user);
        request.setAttribute(ConfigHelper.CAN_LOGOUT, !login_nl);
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy()
    {
    }
}
