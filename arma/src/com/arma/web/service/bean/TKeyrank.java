/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Node;

import com.arma.web.servlets.kms.InVarKM;

import com.neulion.iptv.web.service.BaseDaoBean;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class TKeyrank extends BaseDaoBean
{
	private int keyId;
	private int rank;
	private Date updateDate;

	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
		dbmap.put("keyId", keyId);
		dbmap.put("rank", rank);
		dbmap.put("updateDate", updateDate);
		return dbmap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
	{
		if (dbmap == null || dbmap.size() == 0) return (T)this;
		keyId = WebUtil.obj2int(dbmap.get("keyId"));
		rank = WebUtil.obj2int(dbmap.get("rank"));
		updateDate = (Date)dbmap.get("updateDate");
		return (T)this;
	}

	public TKeyrank fromRequest(Node node, HttpServletRequest request)
	{
		keyId = WebUtil.obj2int(WebUtil.scan_str("key_id", node, request));
		rank = WebUtil.obj2int(WebUtil.scan_str("rank", node, request));
		updateDate = DateUtil.date(WebUtil.scan_str("update_date", node, request));
		return this;
	}

	public TKeyrank copyit()
	{
		TKeyrank bean = new TKeyrank();
		bean.setKeyId(getKeyId());
		bean.setRank(getRank());
		bean.setUpdateDate(getUpdateDate() == null ? null : new Date(getUpdateDate().getTime()));
		return bean;
	}

	@Override
	public String toText()
	{
		return (keyId + "|" + rank + "|" + DateUtil.date24Str(updateDate, DateUtil.df_long));
	}

	public boolean empty()
	{
		return (keyId < 1);
	}

	public void append(boolean close, XmlOutput4j xop)
	{
		xop.openTag("keyrank", InVarKM.attrs_keyrank, new String[]{"" + getKeyId(), "" + getRank(), DateUtil.str(getUpdateDate())});
		if (close) xop.closeTag();
	}

	public int getKeyId() {
		return keyId;
	}
	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}

