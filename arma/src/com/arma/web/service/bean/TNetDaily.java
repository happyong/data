/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service.bean;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Node;

import com.neulion.iptv.web.service.BaseDaoBean;
import com.neulion.iptv.web.util.WebUtil;

public class TNetDaily extends BaseDaoBean
{
	private int id;
	private String code;
	private String date;										// 当前日期, yyyy-MM-dd
	private double net;										// 净值
	private double netTotal;								// 累计净值
	private double growth;									// 净值增长率, growth = (net - netPrev) * 100d / netPrev, netPrev = net / (1 + growth /100d)
	private double premium = -200d;					// 溢价率, (close - net) * 100d / net, (codeA.close + codeB.close - net * 2d) * 50d / net
	private double premium2 = -200d;					// 溢价率二, (close - netPrev) * 100d / netPrev, (codeA.close + codeB.close - netPrev * 2d) * 50d / netPrev
	private double premiumHigh5 = -200d;			// 五日最高溢价率
	private double premiumLow5 = -200d;			// 五日最低溢价率
	
	public TNetDaily()
	{
	}

	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
		dbmap.put("code", WebUtil.unull(code));
		dbmap.put("date", WebUtil.unull(date));
		dbmap.put("net", net);
		dbmap.put("netTotal", netTotal);
		dbmap.put("growth", growth);
		dbmap.put("premium", premium);
		dbmap.put("premium2", premium2);
		dbmap.put("premiumHigh5", premiumHigh5);
		dbmap.put("premiumLow5", premiumLow5);
		return dbmap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
	{
		if (dbmap == null || dbmap.size() == 0) return (T)this;
		id = WebUtil.obj2int(dbmap.get("id"));
		code = (String)dbmap.get("code");
		date = (String)dbmap.get("date");
		net = WebUtil.obj2double(dbmap.get("net"));
		netTotal = WebUtil.obj2double(dbmap.get("netTotal"));
		growth = WebUtil.obj2double(dbmap.get("growth"));
		premium = WebUtil.obj2double(dbmap.get("premium"));
		premium2 = WebUtil.obj2double(dbmap.get("premium2"));
		premiumHigh5 = WebUtil.obj2double(dbmap.get("premiumHigh5"));
		premiumLow5 = WebUtil.obj2double(dbmap.get("premiumLow5"));
		return (T)this;
	}

	public TNetDaily fromRequest(Node node, HttpServletRequest request)
	{
		code = WebUtil.scan_str("code", node, request);
		date = WebUtil.scan_str("date", node, request);
		net = WebUtil.obj2double(WebUtil.scan_str("net", node, request));
		netTotal = WebUtil.obj2double(WebUtil.scan_str("net_total", node, request));
		growth = WebUtil.obj2double(WebUtil.scan_str("growth", node, request));
		// premium = WebUtil.obj2double(WebUtil.scan_str("premium", node, request));
		// premium2 = WebUtil.obj2double(WebUtil.scan_str("premium2", node, request));
		// premiumHigh5 = WebUtil.obj2double(WebUtil.scan_str("premium_high_5", node, request));
		// premiumLow5 = WebUtil.obj2double(WebUtil.scan_str("premium_low_5", node, request));
		return this;
	}

	public TNetDaily copyit()
	{
		TNetDaily bean = new TNetDaily();
		bean.setId(getId());
		bean.setCode(getCode());
		bean.setDate(getDate());
		bean.setNet(getNet());
		bean.setNetTotal(getNetTotal());
		bean.setGrowth(getGrowth());
		bean.setPremium(getPremium());
		bean.setPremium2(getPremium2());
		bean.setPremiumHigh5(getPremiumHigh5());
		bean.setPremiumLow5(getPremiumLow5());
		return bean;
	}

	// change or not after merge
	public boolean merge(TNetDaily bean)
	{
		boolean ret = false, diff;
		if (WebUtil.empty(getCode()) || (!getCode().equals(bean.getCode()))) return ret;
		diff = (!WebUtil.empty(bean.getDate()) && !bean.getDate().equals(getDate()));
		if (diff) setDate(bean.getDate());
		ret = ret || diff;
		diff = (bean.getNet() > 0d && getNet() != bean.getNet());
		if (diff) setNet(bean.getNet());
		ret = ret || diff;
		diff = (bean.getNetTotal() > 0d && getNetTotal() != bean.getNetTotal());
		if (diff) setNetTotal(bean.getNetTotal());
		ret = ret || diff;
		diff = (bean.getGrowth() > -101d && getGrowth() != bean.getGrowth());
		if (diff) setGrowth(bean.getGrowth());
		ret = ret || diff;
		diff = (bean.getPremium() > -101d && getPremium() != bean.getPremium());
		if (diff) setPremium(bean.getPremium());
		ret = ret || diff;
		diff = (bean.getPremium2() > -101d && getPremium2() != bean.getPremium2());
		if (diff) setPremium2(bean.getPremium2());
		ret = ret || diff;
		diff = (bean.getPremiumHigh5() > -101d && getPremiumHigh5() != bean.getPremiumHigh5());
		if (diff) setPremiumHigh5(bean.getPremiumHigh5());
		ret = ret || diff;
		diff = (bean.getPremiumLow5() > -101d && getPremiumLow5() != bean.getPremiumLow5());
		if (diff) setPremiumLow5(bean.getPremiumLow5());
		return (ret || diff);
	}
	
	public boolean normalize()
	{
		return (premium > -101d && premium2 > -101d && premiumHigh5 > -101d && premiumLow5 > -101d);
	}

	// apply to all fields or not after normalize
	public boolean normalize(double p, double p2)
	{
		boolean ret = true, diff;
		diff = (p > -101d);
		if (diff) setPremium(p);
		ret = ret && diff;
		diff = (p2 > -101d);
		if (diff) setPremium2(p2);
		return (ret && diff);
	}

	// apply to all fields or not after normalize
	public boolean normalize5(double ph5, double pl5)
	{
		boolean ret = true, diff;
		diff = (ph5 > -101d);
		if (diff) setPremiumHigh5(ph5);
		ret = ret && diff;
		diff = (pl5 > -101d);
		if (diff) setPremiumLow5(pl5);
		return (ret && diff);
	}
	
	public String key(String date)
	{
		return (WebUtil.unull(code) + "_" + (WebUtil.empty(date) ? WebUtil.unull(this.date) : date));
	}

	public String toText()
	{
		return (WebUtil.unull(code) + "|" + toTextS() + "|" + WebUtil.d2s(4, netTotal) + "|" + WebUtil.d2s(4, premium) + "|" + 
		        WebUtil.d2s(4, premium2) + "|" + WebUtil.d2s(4, premiumHigh5) + "|" + WebUtil.d2s(4, premiumLow5));
	}

	public String toTextS()
	{
		return (WebUtil.unull(date) + "|" + WebUtil.d2s(4, net) + "|" + WebUtil.d2s(4, growth));
	}

	public boolean empty()
	{
		return (WebUtil.empty(code) || WebUtil.empty(date));
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
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getNet() {
		return net;
	}
	public void setNet(double net) {
		this.net = net;
	}
	public double getNetTotal() {
		return netTotal;
	}
	public void setNetTotal(double netTotal) {
		this.netTotal = netTotal;
	}
	public double getGrowth() {
		return growth;
	}
	public void setGrowth(double growth) {
		this.growth = growth;
	}
	public double getPremium() {
		return premium;
	}
	public void setPremium(double premium) {
		this.premium = premium;
	}
	public double getPremium2() {
		return premium2;
	}
	public void setPremium2(double premium2) {
		this.premium2 = premium2;
	}
	public double getPremiumHigh5() {
		return premiumHigh5;
	}
	public void setPremiumHigh5(double premiumHigh5) {
		this.premiumHigh5 = premiumHigh5;
	}
	public double getPremiumLow5() {
		return premiumLow5;
	}
	public void setPremiumLow5(double premiumLow5) {
		this.premiumLow5 = premiumLow5;
	}
}

