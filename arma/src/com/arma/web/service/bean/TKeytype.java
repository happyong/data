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

public class TKeytype extends BaseDaoBean
{
	private int typeId;
	private String nameCn;
	private String nameEn;
	private String demoCn;
	private String demoEn;
	private Date updateDate;

	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
        dbmap.put("typeId", typeId);
		dbmap.put("nameCn", WebUtil.unull(nameCn));
		dbmap.put("nameEn", WebUtil.unull(nameEn));
		dbmap.put("demoCn", WebUtil.unull(demoCn));
		dbmap.put("demoEn", WebUtil.unull(demoEn));
		dbmap.put("updateDate", updateDate);
		return dbmap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
	{
		if (dbmap == null || dbmap.size() == 0) return (T)this;
		typeId = WebUtil.obj2int(dbmap.get("typeId"));
		nameCn = (String)dbmap.get("nameCn");
		nameEn = (String)dbmap.get("nameEn");
		demoCn = (String)dbmap.get("demoCn");
		demoEn = (String)dbmap.get("demoEn");
		updateDate = (Date)dbmap.get("updateDate");
		return (T)this;
	}

	public TKeytype fromRequest(Node node, HttpServletRequest request)
	{
	    typeId = WebUtil.obj2int(WebUtil.scan_str("type_id", node, request));
		nameCn = WebUtil.scan_str("name_cn", node, request);
		nameEn = WebUtil.scan_str("name_en", node, request);
		demoCn = WebUtil.scan_str("demo_cn", node, request);
		demoEn = WebUtil.scan_str("demo_en", node, request);
		updateDate = DateUtil.date(WebUtil.scan_str("update_date", node, request));
		return this;
	}

	public TKeytype copyit()
	{
		TKeytype bean = new TKeytype();
		bean.setTypeId(getTypeId());
		bean.setNameCn(getNameCn());
		bean.setNameEn(getNameEn());
		bean.setDemoCn(getDemoCn());
		bean.setDemoEn(getDemoEn());
		bean.setUpdateDate(getUpdateDate() == null ? null : new Date(getUpdateDate().getTime()));
		return bean;
	}

	@Override
	public String toText()
	{
		return (typeId + "|" + WebUtil.unull(nameCn) + "|" + WebUtil.unull(nameEn) + "|" + WebUtil.unull(demoCn) + "|" + WebUtil.unull(demoEn) + "|" + DateUtil.date24Str(updateDate, DateUtil.df_long));
	}

	public boolean empty()
	{
		return (typeId < 1 || WebUtil.empty(nameCn));
	}

	public void append(boolean close, XmlOutput4j xop)
	{
		xop.openTag("keytype", InVarKM.attrs_keytype, new String[]{"" + getTypeId(), getNameCn(), getNameEn(), getDemoCn(), getDemoEn(), DateUtil.str(getUpdateDate())});
		if (close) xop.closeTag();
	}

	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
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
	public String getDemoCn() {
		return demoCn;
	}
	public void setDemoCn(String demoCn) {
		this.demoCn = demoCn;
	}
	public String getDemoEn() {
		return demoEn;
	}
	public void setDemoEn(String demoEn) {
		this.demoEn = demoEn;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}

