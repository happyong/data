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

public class TQuoteDaily extends BaseDaoBean
{
	private int id;
	private String code;
	private String date;								// 当前日期, yyyy-MM-dd
	private String time;								// 当前时间, HH:mm:ss
	private double price;								// 市价或今收
	private double closePrev;						// 昨收
	private double open;								// 今开
	private double high;								// 最高价
	private double low;								// 最低价
	private double volume;							// 成交量, 单位手
	private double amount;							// 成交金额, 单位万元
	private double high60;							// 三月最高
	private double low60;							// 三月最低
	private double volume60;						// 三月成交均量, 单位手
	
	private String nameS;								// used for scan symbol only

	@Override
	public Map<String, Object> toDbMap()
	{
		Map<String, Object> dbmap = new HashMap<String, Object>();
		dbmap.put("code", WebUtil.unull(code));
		dbmap.put("date", WebUtil.unull(date));
		dbmap.put("time", WebUtil.unull(time));
		dbmap.put("price", price);
		dbmap.put("closePrev", closePrev);
		dbmap.put("open", open);
		dbmap.put("high", high);
		dbmap.put("low", low);
		dbmap.put("volume", volume);
		dbmap.put("amount", amount);
		dbmap.put("high60", high60);
		dbmap.put("low60", low60);
		dbmap.put("volume60", volume60);
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
		time = (String)dbmap.get("time");
		price = WebUtil.s2d_0000(dbmap.get("price"));
		closePrev = WebUtil.s2d_0000(dbmap.get("closePrev"));
		open = WebUtil.s2d_0000(dbmap.get("open"));
		high = WebUtil.s2d_0000(dbmap.get("high"));
		low = WebUtil.s2d_0000(dbmap.get("low"));
		volume = WebUtil.s2d_0000(dbmap.get("volume"));
		amount = WebUtil.s2d_0000(dbmap.get("amount"));
		high60 = WebUtil.s2d_0000(dbmap.get("high60"));
		low60 = WebUtil.s2d_0000(dbmap.get("low60"));
		volume60 = WebUtil.s2d_0000(dbmap.get("volume60"));
		return (T)this;
	}

	public TQuoteDaily fromRequest(Node node, HttpServletRequest request)
	{
		code = WebUtil.scan_str("code", node, request);
		date = WebUtil.scan_str("date", node, request);
		time = WebUtil.scan_str("time", node, request);
		price = WebUtil.s2d_0000(WebUtil.scan_str("price", node, request));
		closePrev = WebUtil.s2d_0000(WebUtil.scan_str("closePrev", node, request));
		open = WebUtil.s2d_0000(WebUtil.scan_str("open", node, request));
		high = WebUtil.s2d_0000(WebUtil.scan_str("high", node, request));
		low = WebUtil.s2d_0000(WebUtil.scan_str("low", node, request));
		volume = WebUtil.s2d_0000(WebUtil.scan_str("volume", node, request));
		amount = WebUtil.s2d_0000(WebUtil.scan_str("amount", node, request));
		// high60 = WebUtil.s2d_0000(WebUtil.scan_str("high_60", node, request));
		// low60 = WebUtil.s2d_0000(WebUtil.scan_str("low_60", node, request));
		// volume60 = WebUtil.s2d_0000(WebUtil.scan_str("volume_60", node, request));
		return this;
	}

	public TQuoteDaily copyit()
	{
		TQuoteDaily bean = new TQuoteDaily();
		bean.setId(getId());
		bean.setCode(getCode());
		bean.setDate(getDate());
		bean.setTime(getTime());
		bean.setPrice(getPrice());
		bean.setClosePrev(getClosePrev());
		bean.setOpen(getOpen());
		bean.setHigh(getHigh());
		bean.setLow(getLow());
		bean.setVolume(getVolume());
		bean.setAmount(getAmount());
		bean.setHigh60(getHigh60());
		bean.setLow60(getLow60());
		bean.setVolume60(getVolume60());
		return bean;
	}

	// change or not after merge
	public boolean merge(TQuoteDaily bean)
	{
		boolean ret = false, diff;
		if (WebUtil.empty(getCode()) || (!getCode().equals(bean.getCode()))) return ret;
		diff = (!WebUtil.empty(bean.getDate()) && !bean.getDate().equals(getDate()));
		if (diff) setDate(bean.getDate());
		ret = ret || diff;
		diff = (!WebUtil.empty(bean.getTime()) && !bean.getTime().equals(getTime()));
		if (diff) setTime(bean.getTime());
		ret = ret || diff;
		diff = (bean.getPrice() > 0d && getPrice() != bean.getPrice());
		if (diff) setPrice(bean.getPrice());
		ret = ret || diff;
		diff = (bean.getClosePrev() > 0d && getClosePrev() != bean.getClosePrev());
		if (diff) setClosePrev(bean.getClosePrev());
		ret = ret || diff;
		diff = (bean.getOpen() > 0d && getOpen() != bean.getOpen());
		if (diff) setOpen(bean.getOpen());
		ret = ret || diff;
		diff = (bean.getHigh() > 0d && getHigh() != bean.getHigh());
		if (diff) setHigh(bean.getHigh());
		ret = ret || diff;
		diff = (bean.getLow() > 0d && getLow() != bean.getLow());
		if (diff) setLow(bean.getLow());
		ret = ret || diff;
		diff = (bean.getVolume() > 0d && getVolume() != bean.getVolume());
		if (diff) setVolume(bean.getVolume());
		ret = ret || diff;
		diff = (bean.getAmount() > 0d && getAmount() != bean.getAmount());
		if (diff) setAmount(bean.getAmount());
		ret = ret || diff;
		diff = (bean.getHigh60() > 0d && getHigh60() != bean.getHigh60());
		if (diff) setHigh60(bean.getHigh60());
		ret = ret || diff;
		diff = (bean.getLow60() > 0d && getLow60() != bean.getLow60());
		if (diff) setLow60(bean.getLow60());
		ret = ret || diff;
		diff = (bean.getVolume60() > 0d && getVolume60() != bean.getVolume60());
		if (diff) setVolume60(bean.getVolume60());
		return (ret || diff);
	}
	
	public boolean normalize()
	{
		return (high60 > 0d && low60 > 0d && volume60 > 0d);
	}
	
	// apply to all fields or not after normalize
	public boolean normalize(double h60, double l60, double v60)
	{
		boolean ret = true, diff;
		diff = (h60 > 0d);
		if (diff) setHigh60(h60);
		ret = ret && diff;
		diff = (l60 > 0d);
		if (diff) setLow60(l60);
		ret = ret && diff;
		diff = (v60 > 0d);
		if (diff) setVolume60(v60);
		return (ret && diff);
	}
	
	public String key(String date)
	{
		return (WebUtil.unull(code) + "_" + (WebUtil.empty(date) ? WebUtil.unull(this.date) : date));
	}

	public String toText()
	{
		return (WebUtil.unull(code) + "|" + WebUtil.unull(date) + "|" + WebUtil.unull(time) + "|" + price + "|" + closePrev + "|" + open + "|" + high + "|" + low + "|" +
				volume + "|" + amount + "|" + high60 + "|" + low60 + "|" + volume60);
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
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getClosePrev() {
		return closePrev;
	}
	public void setClosePrev(double closePrev) {
		this.closePrev = closePrev;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getHigh60() {
		return high60;
	}
	public void setHigh60(double high60) {
		this.high60 = high60;
	}
	public double getLow60() {
		return low60;
	}
	public void setLow60(double low60) {
		this.low60 = low60;
	}
	public double getVolume60() {
		return volume60;
	}
	public void setVolume60(double volume60) {
		this.volume60 = volume60;
	}

	public String getNameS() {
		return nameS;
	}
	public void setNameS(String nameS) {
		this.nameS = nameS;
	}
}

