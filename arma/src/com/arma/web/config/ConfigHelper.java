/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.config;

import com.neulion.iptv.web.util.WebUtil;

public class ConfigHelper
{
    public static enum PortalMenu
    {
        funds, kms, charts
    };

    public static long reboot;
    public static boolean rebooted;
    public static CommonConfig common;
    public static String web;
    public static String sep = WebUtil.KEY_SEP_WIN; // os sep
    public static String sep2 = WebUtil.KEY_SEP_WIN; // media param sep
    public static boolean unix2 = false; // media param sep

    public static final String NAME_SKY_ADMIN = "sky";
    public static final String HTTP = "http://";
    public static final String ADAPTIVE = "adaptive://";
    public static final String USER_LOGIN = "login_user";
    public static final String USER_VAPI = "vapi";
    public static final String CAN_LOGOUT = "can_logout";
    public static final String PAGE_404 = "/WEB-INF/portal/error404.jsp";

    public static String web(String dir)
    {
        return web + (WebUtil.empty(dir) ? "" : dir + sep);
    }
}
