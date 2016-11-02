/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.arma.web.config.ConfigHelper;
import com.arma.web.service.ConfigDaoService;
import com.arma.web.service.bean.UserLogin;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.util.KeyUtil;
import com.neulion.iptv.web.util.WebUtil;

public class UserHelper
{	
	private static List<UserLogin> _users;
	private static List<String> _actives = new ArrayList<String>();
	private static final Logger _logger = Logger.getLogger(UserHelper.class);
	
	private static void load()
	{
		if (_users == null) 
		{
			_users = GlobalCache.getInstance().getBean(ConfigDaoService.class).getUsers();
			_actives.clear();
			for (UserLogin user : _users) 
				if (user.isActive()) 
					_actives.add(user.getName());
			Collections.sort(_actives);
		}
	}
	
	private static void user(boolean sort, boolean active, String name)
	{
		if (WebUtil.empty(name)) return;
		boolean changed = false;
		if (active)
		{
			changed = !_actives.contains(name);
			if (changed) _actives.add(name);
		}
		else
		{
			changed = _actives.contains(name);
			if (changed) _actives.remove(name);
		}
		if (sort && changed) Collections.sort(_actives);
	}
	
	private static UserLogin scan(String name)
	{
		if (WebUtil.empty(name)) return new UserLogin();
		load();
		for (UserLogin user : _users)
			if (name.equals(user.getName()))
				return user;
		return new UserLogin();
	}
	
	public static List<String> users()
	{
		load();
		return _actives;
	}
	
	public static String user(HttpServletRequest request)
	{
        UserLogin user = user2(request);
		return (user == null || user.empty() ? ConfigHelper.USER_VAPI : user.getName());
	}
	
	public static UserLogin user2(HttpServletRequest request)
	{
        UserLogin user = (UserLogin)request.getSession().getAttribute(ConfigHelper.USER_LOGIN);
		return (user == null || user.empty() || !user.isActive() ? null : user);
	}
	
	public static UserLogin userLogin(String name)
	{
		if (WebUtil.empty(name)) return new UserLogin();
		UserLogin user = scan(name);
		if (!user.empty() && user.isActive()) return user;
		return new UserLogin();
	}
	
	public static UserLogin userLogin(String name, String password)
	{
		if (WebUtil.empty(name) || WebUtil.empty(password)) return new UserLogin();
		UserLogin user = scan(name);
		if (!user.empty() && user.isActive() && KeyUtil.encrypt(password).equals(user.getSecure())) return user;
		return new UserLogin();
	}
	
	public static void userUpdate(boolean active, String user, String ip, String name, String password, String roles, Map<String, Object> map)
	{
		if (WebUtil.empty(name)) return;
		String type2 = null;
		UserLogin user_login = scan(name);
		ConfigDaoService dao = GlobalCache.getInstance().getBean(ConfigDaoService.class);
		if (WebUtil.empty(password))
		{
			// set active or not
			user_login.setName(name);
			user_login.setActive(active);
			dao.updateUser2(user_login);
			if (!active)
			{
				type2 = "delete";
			}
		}
		else
		{
			String secure = KeyUtil.encrypt(password);
			user_login.setSecure(secure);
			user_login.setRoles(roles);
			user_login.setActive(active);
			if (user_login.empty())
			{
				user_login.setName(name);
				dao.insertUser(user_login);
				_users.add(user_login);
				type2 = "add";
			}
			else 
			{
				dao.updateUser(user_login);
				type2 = "update";
			}
		}
		user(true, active, name);
		map.put("result", true);
		if (!WebUtil.empty(type2))
		{
			_logger.info("Sky user " + type2 + ", user|" + user + "|ip|" + ip + "|username|" + name);
		}
	}
}
