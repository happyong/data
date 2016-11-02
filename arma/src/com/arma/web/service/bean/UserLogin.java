/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.neulion.iptv.web.service.BaseDaoBean;
import com.neulion.iptv.web.util.WebUtil;

public class UserLogin extends BaseDaoBean
{
	private String name;
	private String secure;
	private Date expired;
	private boolean active;
	private boolean admin;
	private boolean schedule;
	private boolean task;
	private boolean readonly;
	private boolean guest = true;
	
	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
		dbmap.put("name", WebUtil.unull(name));
		dbmap.put("secure", WebUtil.unull(secure));
		dbmap.put("expired", expired);
		dbmap.put("status", active ? 1 : 0);
		dbmap.put("roles", getRoles());
		
		return dbmap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
	{
		if (dbmap == null || dbmap.size() == 0) return (T)this;
		
		name = WebUtil.string("name", dbmap);
		secure = WebUtil.string("secure", dbmap);
		expired = (Date)dbmap.get("expired");
		active = (WebUtil.obj2int(dbmap.get("status")) == 1);
		setRoles(WebUtil.string("roles", dbmap));
		
		return (T)this;
	}
	
	@Override
	public String toText()
	{
		return name;
	}
	
	public boolean empty()
	{
		return WebUtil.empty(name);
	}
	
	public String getRoles() {
		String roles = "";
		if (readonly) 
			roles += ",readonly";
		else
		{
			if (admin) roles += ",admin";
			if (schedule) roles += ",schedule";
			if (task) roles += ",task";
		}
		return roles.length() == 0 ? "guest" : WebUtil.str(roles);
	}
	
	public void setRoles(String roles) {
		admin = false;
		schedule = false;
		task = false;
		readonly = false;
		String[] arr = WebUtil.split(",", roles);
		for (String str : arr)
		{
			if ("admin".equals(str))
				admin = true;
			else if ("schedule".equals(str))
				schedule = true;
			else if ("task".equals(str))
				task = true;
			else if ("readonly".equals(str))
			{
				admin = true;
				readonly = true;
			}
		}
		guest = (!admin && !schedule && !task && !readonly);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSecure() {
		return secure;
	}
	public void setSecure(String secure) {
		this.secure = secure;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isAdmin() {
		return admin;
	}
	public boolean isSchedule() {
		return schedule;
	}
	public boolean isTask() {
		return task;
	}
	public boolean isReadonly() {
		return readonly;
	}
	public boolean isGuest() {
		return guest;
	}
	public Date getExpired() {
		return expired;
	}
	public void setExpired(Date expired) {
		this.expired = expired;
	}
}
