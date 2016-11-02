/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.fund;

import com.arma.web.service.bean.TNetDaily;
import com.arma.web.service.bean.TQuoteDaily;
import com.arma.web.util.InVarAM;
import com.neulion.iptv.web.util.WebUtil;

public class FundRtStats extends TNetDaily
{
	private String dateQuote;						// 市价日期, yyyy-MM-dd
	private String minute;								// 扫描时间, HH:mm
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

	private String minute0;							// 市价时间, HH:mm
	
	public FundRtStats(String code)
	{
		setCode(code);
	}

	public FundRtStats copyit()
	{
		FundRtStats bean = new FundRtStats(getCode());
		bean.merge(super.copyit());
		bean.setDateQuote(getDateQuote());
		bean.setMinute(getMinute());
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
	public boolean merge2(TQuoteDaily bean)
	{
		boolean ret = false, diff;
		if (WebUtil.empty(getCode()) || (!getCode().equals(bean.getCode()))) return ret;
		// diff = (!WebUtil.empty(bean.getDate()) && !bean.getDate().equals(getDate()));
		// if (diff) setDate(bean.getDate());
		// ret = ret || diff;
		diff = (!WebUtil.empty(bean.getTime()) && (WebUtil.empty(getMinute()) || !bean.getTime().startsWith(getMinute())));
		if (diff) setMinute(bean.getTime());
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
		return (super.normalize() && high60 > 0d && low60 > 0d && volume60 > 0d);
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
	
	public String key()
	{
		return (getCode() + "_" + WebUtil.unull(minute0));
	}

	public String toText()
	{
		return (super.toText()+ "|" + WebUtil.unull(dateQuote) + "|" + WebUtil.unull(minute) + "|" + price + "|" + closePrev + "|" + open + "|" + high + "|" + low + "|" + 
				volume + "|" + amount + "|" + high60 + "|" + low60 + "|" + volume60);
	}

	public boolean empty()
	{
		return (super.empty() || WebUtil.empty(minute));
	}

	public String getDateQuote() {
		return dateQuote;
	}
	public void setDateQuote(String dateQuote) {
		this.dateQuote = dateQuote;
	}
	public String getMinute() {
		return minute;
	}
	public String getMinute0() {
		return minute0;
	}
	public void setMinute(String minute) {
		if (minute == null || minute.length() < 5) return;
		this.minute = minute.substring(0, 5);
		if (this.minute.compareTo(InVarAM.s_trade_times[1]) < 0) minute0 = InVarAM.s_trade_times[1];
		else if (this.minute.compareTo(InVarAM.s_trade_times[4]) > 0) minute0 = InVarAM.s_trade_times[4];
		else if (this.minute.compareTo(InVarAM.s_trade_times[2]) > 0 && this.minute.compareTo(InVarAM.s_trade_times[3]) < 0) minute0 = InVarAM.s_trade_times[2];
		else minute0 = this.minute;
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
}

