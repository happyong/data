/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Element;
import org.dom4j.Node;

import com.arma.web.util.InVarAM;
import com.neulion.iptv.web.service.BaseDaoBean;
import com.neulion.iptv.web.util.ParseUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class TSymbol extends BaseDaoBean
{
	private int id;
	private String code;
	private String name;						// 名称
	private String nameS;						// 名称缩写
	private String market;					// 交易所, sh/sz
	private int type;							// 类型, 1- 股票, 2 - 债券, 3 - 基金 (310/M/311/A/312/B), 4 - 期货, 5 - 期权, 6 - 外汇
	private double equity;					// 总股本/总份额
	private String manager;					// 基金经理
	private String startDate;					// 上市日, yyyy-MM-dd
	private String endDate;					// 退市日, yyyy-MM-dd
	
	// "nameM"; 
	// "codeA" - 分级A代码; "codeB" - 分级B代码; "codeM" - 分级母代码
	private Map<String, String> _funds = new HashMap<String, String>();

	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
		dbmap.put("code", WebUtil.unull(code));
		dbmap.put("name", WebUtil.unull(name));
		dbmap.put("nameS", WebUtil.unull(nameS));
		dbmap.put("market", WebUtil.unull(market));
		dbmap.put("type", type);
		dbmap.put("equity", equity);
		dbmap.put("manager", WebUtil.unull(manager));
		dbmap.put("startDate", WebUtil.unull(startDate));
		dbmap.put("endDate", WebUtil.unull(endDate));
		dbmap.put("options", WebUtil.unull(getOptions()));
		return dbmap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
	{
		if (dbmap == null || dbmap.size() == 0) return (T)this;
		id = WebUtil.obj2int(dbmap.get("id"));
		code = (String)dbmap.get("code");
		name = (String)dbmap.get("name");
		nameS = (String)dbmap.get("nameS");
		market = (String)dbmap.get("market");
		type = WebUtil.obj2int(dbmap.get("type"));
		equity = WebUtil.s2d_0000(dbmap.get("equity"));
		manager = (String)dbmap.get("manager");
		startDate = (String)dbmap.get("startDate");
		endDate = (String)dbmap.get("endDate");
		_funds.clear();
		Node _node = ParseUtil.parseRoot((String)dbmap.get("options"));
		List<Element> list = (_node == null ? null : ParseUtil.elements(_node.selectSingleNode("fund")));
		if (list != null)
			for (Element e : list) 
				addFundInfo(e.getName(), e.getTextTrim());	
		return (T)this;
	}

	public TSymbol fromRequest(int t, Node node, HttpServletRequest request)
	{
		String i = (t >= InVarAM.s_names.length || t < 0 ? "" : "" + t);
		code = WebUtil.scan_str("code" + i, node, request);
		name = WebUtil.scan_str("name" + i, node, request);
		nameS = WebUtil.scan_str("name_s" + i, node, request);
		market = WebUtil.scan_str("market" + i, node, request);
		type = WebUtil.obj2int(WebUtil.scan_str("type" + i, node, request));
		equity = WebUtil.s2d_0000(WebUtil.scan_str("equity" + i, node, request));
		manager = WebUtil.scan_str("manager" + i, node, request);
		startDate = WebUtil.scan_str("start_date" + i, node, request);
		endDate = WebUtil.scan_str("end_date" + i, node, request);
		flush();
		// options = WebUtil.scan_str("options" + i, node, request);
		return this;
	}

	public TSymbol copyit()
	{
		TSymbol bean = new TSymbol();
		bean.setId(getId());
		bean.setCode(getCode());
		bean.setName(getName());
		bean.setNameS(getNameS());
		bean.setMarket(getMarket());
		bean.setType(getType());
		bean.setEquity(getEquity());
		bean.setManager(getManager());
		bean.setStartDate(getStartDate());
		bean.setEndDate(getEndDate());
		bean._funds.clear();
		bean._funds.putAll(_funds);
		return bean;
	}

	// change or not after merge
	public boolean merge(TSymbol bean)
	{
		boolean ret = false, diff;
		if (WebUtil.empty(getCode()) || (!getCode().equals(bean.getCode()))) return ret;
		diff = (!WebUtil.empty(bean.getName()) && !bean.getName().equals(getName()));
		if (diff) setName(bean.getName());
		ret = ret || diff;
		diff = (!WebUtil.empty(bean.getNameS()) && !bean.getNameS().equals(getNameS()));
		if (diff) setNameS(bean.getNameS());
		ret = ret || diff;
		diff = (!WebUtil.empty(bean.getMarket()) && !bean.getMarket().equals(getMarket()));
		if (diff) setMarket(bean.getMarket());
		ret = ret || diff;
		diff = (bean.getType() > 0 && getType() != bean.getType());
		if (diff) setType(bean.getType());
		ret = ret || diff;
		diff = (bean.getEquity() > 0d && getEquity() != bean.getEquity());
		if (diff) setEquity(bean.getEquity());
		ret = ret || diff;
		diff = (!WebUtil.empty(bean.getManager()) && !bean.getManager().equals(getManager()));
		if (diff) setManager(bean.getManager());
		ret = ret || diff;
		diff = (!WebUtil.empty(bean.getStartDate()) && !bean.getStartDate().equals(getStartDate()));
		if (diff) setStartDate(bean.getStartDate());
		ret = ret || diff;
		diff = (!WebUtil.empty(bean.getEndDate()) && !bean.getEndDate().equals(getEndDate()));
		if (diff) setEndDate(bean.getEndDate());
		ret = ret || diff;
		for (String str : _funds.keySet())
		{
			diff = (!WebUtil.empty(bean._funds.get(str)) && !bean._funds.get(str).equals(_funds.get(str)));
			if (diff) addFundInfo(str, bean._funds.get(str));
			ret = ret || diff;
		}
		return ret;
	}
	
	public String code(int pos)
	{
		if (pos < 0 || pos >= InVarAM.s_names.length) return "";
		return WebUtil.unull(_funds.get(InVarAM.s_names[pos]));
	}
	
	public int abm(TSymbol bean)
	{
		String nameM = (bean == null ? null : bean.getStrFundInfo("nameM"));
		if (WebUtil.empty(nameM)) return -1;
		if (nameM.equals(getStrFundInfo("nameM"))) return type;
		return -1;
	}
	
	private void flush() 
	{
		if (WebUtil.empty(name) || type < InVarAM.i_fund_type || type > InVarAM.i_fund_type + 2) return;
		boolean a = name.endsWith("A"), b = name.endsWith("B");
		type = InVarAM.i_fund_type + (a ? 0 : (b ? 1 : 2));
		addFundInfo("nameM", (a||b ? name.substring(0, name.length() - 1) : name));
	}
	
	public String hqlist(boolean quote)
	{
		return (quote ? market : "f_") + code;
	}
	
	public boolean hit(String str)
	{
		return (WebUtil.empty(str) ? false : str.equals(code) || str.equals(name) || str.equals(nameS));
	}
	
	public boolean filter(String str)
	{
		return (WebUtil.empty(str) ? true : (code != null && code.indexOf(str) != -1) || (name != null && name.indexOf(str) != -1) || (nameS != null && nameS.indexOf(str) != -1));
	}
	
	public boolean filter(List<String> includes, List<String> excludes)
	{
		if (excludes != null) for (String str : excludes) if (filter(str)) return false;
		if (includes == null || includes.size() == 0) return true;
		for (String str : includes) if (filter(str)) return true;
		return false;
	}
	
	public boolean recent(String base)
	{
		return (WebUtil.empty(startDate) ? false : startDate.compareTo(base) >= 0);
	}
	
	public void addFundInfo(String name, String value)
	{
		if (WebUtil.empty(name)) return;
		if (value != null)
			_funds.put(name, value);
		else
			_funds.remove(name);
	}
	
	public String getStrFundInfo(String name)
	{
		if (WebUtil.empty(name)) return "";
		return WebUtil.unull(_funds.get(name));
	}
	
	public int getIntFundInfo(String name)
	{
		if (WebUtil.empty(name)) return 0;
		return WebUtil.obj2int(_funds.get(name));
	}
	
	public double getDoubleFundInfo(String name)
	{
		if (WebUtil.empty(name)) return 0;
		return WebUtil.obj2double(_funds.get(name));
	}

	public String toText()
	{
		return (toTextS() + "|" + type);
	}

	public String toTextS()
	{
		return (WebUtil.unull(code) + "|" + WebUtil.unull(nameS) + "|" + toTextAbm() + "|" + WebUtil.unull(startDate) + "|" + WebUtil.unull(name) + "|" + equity + "|" + WebUtil.unull(manager));
	}
	
	public String toTextAbm()
	{
		String ret = "";
		for (int i = 0; i < InVarAM.s_names.length; i++) ret += "," + code(i);
		return ret.substring(1);
	}

	public boolean empty()
	{
		return WebUtil.empty(code);
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
		if (WebUtil.empty(market)) market = (code.startsWith("6") ? "sh" : "sz");
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		flush();	
	}
	public String getNameS() {
		return nameS;
	}
	public void setNameS(String nameS) {
		this.nameS = nameS;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		if (WebUtil.empty(market)) return;
		this.market = market;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
		flush();
	}
	public double getEquity() {
		return equity;
	}
	public void setEquity(double equity) {
		this.equity = equity;
	}
	public String getManager() {
		return manager;
	}
	public void setManager(String manager) {
		this.manager = manager;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getOptions() {
		boolean flag = false;
		XmlOutput4j xop = new XmlOutput4j().appendHeader();
		xop.openTag("result", null, null);		
		if (_funds.size() > 0)
		{
			flag = true;
			xop.openTag("fund", null, null);
			for (String str : _funds.keySet()) append(WebUtil.unull(_funds.get(str)), WebUtil.unull(str), xop);
			xop.closeTag();
		}
		return (flag ? xop.output() : "");
	}
	public boolean append(String val, String name, XmlOutput4j xop)
	{
		if (WebUtil.empty(val)) return false;
		xop.appendTag(false, name, val, null, null);
		return true;
	}
}

