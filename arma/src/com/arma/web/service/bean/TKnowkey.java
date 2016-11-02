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

public class TKnowkey extends BaseDaoBean
{
	private int kmId;
	private int keyId;
	private String keyVal;
	private Date updateDate;

	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
		dbmap.put("kmId", kmId);
		dbmap.put("keyId", keyId);
		dbmap.put("keyVal", WebUtil.unull(keyVal));
		dbmap.put("updateDate", updateDate);
		return dbmap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
	{
		if (dbmap == null || dbmap.size() == 0) return (T)this;
		kmId = WebUtil.obj2int(dbmap.get("kmId"));
		keyId = WebUtil.obj2int(dbmap.get("keyId"));
		keyVal = (String)dbmap.get("keyVal");
		updateDate = (Date)dbmap.get("updateDate");
		return (T)this;
	}

	public TKnowkey fromRequest(Node node, HttpServletRequest request)
	{
		kmId = WebUtil.obj2int(WebUtil.scan_str("km_id", node, request));
		keyId = WebUtil.obj2int(WebUtil.scan_str("key_id", node, request));
		keyVal = WebUtil.scan_str("key_val", node, request);
		updateDate = DateUtil.date(WebUtil.scan_str("update_date", node, request));
		return this;
	}

	public TKnowkey copyit()
	{
		TKnowkey bean = new TKnowkey();
		bean.setKmId(getKmId());
		bean.setKeyId(getKeyId());
		bean.setKeyVal(getKeyVal());
		bean.setUpdateDate(getUpdateDate() == null ? null : new Date(getUpdateDate().getTime()));
		return bean;
	}

	@Override
	public String toText()
	{
		return (kmId + "|" + keyId + "|" + WebUtil.unull(keyVal) + "|" + DateUtil.date24Str(updateDate, DateUtil.df_long));
	}

	public boolean empty()
	{
		return (kmId < 1 || keyId < 1 || WebUtil.empty(keyVal));
	}

	public void append(boolean close, XmlOutput4j xop)
	{
		xop.openTag("knowkey", InVarKM.attrs_knowkey, new String[]{"" + getKmId(), "" + getKeyId(), getKeyVal(), DateUtil.str(getUpdateDate())});
		if (close) xop.closeTag();
	}

	public int getKmId() {
		return kmId;
	}
	public void setKmId(int kmId) {
		this.kmId = kmId;
	}
	public int getKeyId() {
		return keyId;
	}
	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}
	public String getKeyVal() {
		return keyVal;
	}
	public void setKeyVal(String keyVal) {
		this.keyVal = keyVal;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}

