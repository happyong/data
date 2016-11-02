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

public class TKeyword extends BaseDaoBean
{
	private int keyId;
	private String content;
	private String nameCn;
	private String nameEn;
	private int typeId;
	private boolean asEnum;
	private Date updateDate;

	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
        dbmap.put("keyId", keyId);
		dbmap.put("content", WebUtil.unull(content));
		dbmap.put("nameCn", WebUtil.unull(nameCn));
		dbmap.put("nameEn", WebUtil.unull(nameEn));
		dbmap.put("typeId", typeId);
		dbmap.put("asEnum", asEnum ? 1 : 0);
		dbmap.put("updateDate", updateDate);
		return dbmap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
	{
		if (dbmap == null || dbmap.size() == 0) return (T)this;
		keyId = WebUtil.obj2int(dbmap.get("keyId"));
		content = (String)dbmap.get("content");
		nameCn = (String)dbmap.get("nameCn");
		nameEn = (String)dbmap.get("nameEn");
		typeId = WebUtil.obj2int(dbmap.get("typeId"));
		asEnum = (WebUtil.obj2int(dbmap.get("asEnum")) == 1);
		updateDate = (Date)dbmap.get("updateDate");
		return (T)this;
	}

	public TKeyword fromRequest(Node node, HttpServletRequest request)
	{
        keyId = WebUtil.obj2int(WebUtil.scan_str("key_id", node, request));
		content = WebUtil.scan_str("content", node, request);
		nameCn = WebUtil.scan_str("name_cn", node, request);
		nameEn = WebUtil.scan_str("name_en", node, request);
		typeId = WebUtil.obj2int(WebUtil.scan_str("type_id", node, request));
		asEnum = (WebUtil.obj2int(WebUtil.scan_str("as_enum", node, request)) == 1);
		updateDate = DateUtil.date(WebUtil.scan_str("update_date", node, request));
		return this;
	}

	public TKeyword copyit()
	{
		TKeyword bean = new TKeyword();
		bean.setKeyId(getKeyId());
		bean.setContent(getContent());
		bean.setNameCn(getNameCn());
		bean.setNameEn(getNameEn());
		bean.setTypeId(getTypeId());
		bean.setAsEnum(isAsEnum());
		bean.setUpdateDate(getUpdateDate() == null ? null : new Date(getUpdateDate().getTime()));
		return bean;
	}

	@Override
	public String toText()
	{
		return (keyId + "|" + WebUtil.unull(content) + "|" + WebUtil.unull(nameCn) + "|" + WebUtil.unull(nameEn) + "|" + typeId + "|" + asEnum + "|" + DateUtil.date24Str(updateDate, DateUtil.df_long));
	}

	public boolean empty()
	{
		return (WebUtil.empty(nameCn) || typeId < 1);
	}

	public void append(boolean close, XmlOutput4j xop)
	{
		xop.openTag("keyword", InVarKM.attrs_keyword, new String[]{"" + getKeyId(), getContent(), getNameCn(), getNameEn(), "" + getTypeId(), "" + isAsEnum(), DateUtil.str(getUpdateDate())});
		if (close) xop.closeTag();
	}

	public int getKeyId() {
		return keyId;
	}
	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getNameCn() {
		return nameCn;
	}
	public void setNameCn(String nameCn) {
		this.nameCn = nameCn;
	}
	public String getNameEn() {
		return nameEn;
	}
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public boolean isAsEnum() {
		return asEnum;
	}
	public void setAsEnum(boolean asEnum) {
		this.asEnum = asEnum;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}

