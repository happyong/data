/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.config;

import java.util.Map;

import com.arma.web.config.ConfigHelper.PortalMenu;
import com.neulion.iptv.web.BaseConfigBean;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.util.KeyUtil;
import com.neulion.iptv.web.util.WebUtil;

public class CommonConfig extends BaseConfigBean
{
	private boolean showPortalService;
	private String portals = "";
	private String loginMode;
	private String key4PortalAccount = "";          // key word for admin sky

	@SuppressWarnings("unchecked")
	public CommonConfig()
	{
		Map<String, String> cc = (Map<String, String>)GlobalCache.getInstance().getBean("commonConfig");
		if (cc == null) return;

		String str = cc.get("media.tool.unix");
		ConfigHelper.unix2 = (!WebUtil.empty(str) && "true".equals(str));
		ConfigHelper.sep2 = WebUtil.sep(ConfigHelper.unix2);
		str = cc.get("show.portal.service");
		if (str != null) showPortalService = "true".equals(str);
		str = cc.get("menu.portal");
		if (str != null) portals = str;
		str = cc.get("mode.login");
		if (str != null) loginMode = str;
		key4PortalAccount = KeyUtil.encrypt(WebUtil.KEY_ADMIN);
	}
	
	@Override
	public boolean validate() 
	{		
		return true;
	}
	
	protected int getIntValue(int min, String key, Map<String, String> cc)
	{
		String value = cc.get(key);
		return (WebUtil.empty(value) ? -1 : Math.max(min, WebUtil.str2int(value)));
	}
	
	protected long getLongValue(long min, String key, Map<String, String> cc)
	{
		String value = cc.get(key);
		return (WebUtil.empty(value) ? -1 : Math.max(min, WebUtil.str2long(value)));
	}
	
	public boolean show(PortalMenu menu)
	{
		if (!showPortalService || menu == null) return false;
		int pos = menu.ordinal();
		return (portals.length() <= pos || "1".equals(portals.substring(pos, pos + 1)));
	}

	public boolean isShowPortalService() {
		return showPortalService;
	}
	public void setShowPortalService(boolean showPortalService) {
		this.showPortalService = showPortalService;
	}
	public String getPortals() {
		return portals;
	}
	public String getLoginMode() {
		return loginMode;
	}
	public String getKey4PortalAccount() {
		return key4PortalAccount;
	}
}
