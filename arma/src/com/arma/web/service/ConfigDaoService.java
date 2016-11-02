/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.arma.web.service.bean.UserLogin;
import com.neulion.iptv.web.service.BaseDaoService;
import com.neulion.iptv.web.util.WebUtil;

public class ConfigDaoService extends BaseDaoService
{	
	// ------------------------ Insert Table Data ------------------------ // 
	
	public void insertUser(UserLogin user)
	{
		if (user == null || user.empty()) return;
		batchUpdate(insert_user, WebUtil.params(user.toDbMap()));
	}	
	private static final String insert_user = "insert into user_login (login_id, login_password, roles, status) values " +
		"(:name, :secure, :roles, :status) ";
	
	// ------------------------ Update Table Data ------------------------ //

	public void updateUser(UserLogin user)
	{
		if (user == null || user.empty()) return;
		batchUpdate(update_user, WebUtil.params(user.toDbMap()));
	}
	public void updateUser2(UserLogin user)
	{
		if (user == null || user.empty()) return;
		batchUpdate(update_user2, WebUtil.params(user.toDbMap()));
	}
	private static final String update_user = "update user_login set login_password=:secure, roles=:roles, status=:status where login_id=:name ";
	private static final String update_user2 = "update user_login set status=:status where login_id=:name "; 

	// ------------------------ Delete Table Data ------------------------ //
	
	// ------------------------ Select Table Data ------------------------ //

	public List<UserLogin> getUsers()
	{
		List<UserLogin> list = new ArrayList<UserLogin>();
		String sql = WebUtil.substituteParam("cond", "1=1", select_user);
		List<Map<String, Object>> results = query(sql);
		for (Map<String, Object> result : results) list.add(new UserLogin().fromDbMap(UserLogin.class, result));
		return list;
	}
	private static final String select_user = "select login_id as name, login_password as secure, roles as roles, status as status " +
		"from user_login where ${cond} ";
}
