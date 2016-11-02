/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.fund;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Node;

import com.arma.web.service.bean.TNetDaily;
import com.arma.web.service.bean.TQuoteDaily;
import com.arma.web.service.bean.TSymbol;
import com.arma.web.util.ArmaUtil;
import com.arma.web.util.InVarAM;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.HttpUtil;
import com.neulion.iptv.web.util.ParseUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class FundSpider
{
	private static Logger _logger = Logger.getLogger(FundSpider.class);

	// scan fund symbol list via scanSinaNetJs, scanSinaNetOpen, scanSinaNetCx, scanSinaQuoteJs
	public List<String> scanFundSymbol(Map<String, TSymbol> map)
	{
		List<String> list = new ArrayList<String>();
		if (map == null) return list;
		map.clear();
		
		// http://finance.sina.com.cn/iframe/286/20091218/26.js
		// http://hq.sinajs.cn/rn=fgu87&list=f_150265,f_150266,f_168201
		Map<String, TSymbol> map_base = new HashMap<String, TSymbol>();
		scanSinaNetJs(map_base);

		// http://vip.stock.finance.sina.com.cn/fund_center/data/xml.php/NetValue_Service.getNetValueOpen
		Map<String, TSymbol> map_sinanet = new HashMap<String, TSymbol>();
		int ret = scanSinaNetOpen(1000, map_sinanet);
		if (ret > 0) scanSinaNetOpen(ret, map_sinanet);
		// http://vip.stock.finance.sina.com.cn/fund_center/data/xml.php/NetValue_Service.getNetValueCX
		Map<String, TSymbol> map_sinanetcx = new HashMap<String, TSymbol>();
		ret = scanSinaNetCx(1000, map_sinanetcx);
		if (ret > 0) scanSinaNetCx(ret, map_sinanetcx);
		// merge funds from map_sinanetcx to map_sinanet
		merge(true, "scanSinaNetOpenCx", map_sinanet, map_sinanetcx);
		// merge funds from map_sinanet to map_base
		merge(true, "scanFundNetJsOpenCx", map_base, map_sinanet);
		
		TSymbol base;
		Collection<TSymbol> col = map_base.values();
		String date = ArmaUtil.shiftTrade(-7, DateUtil.str(new Date()));
		String[] arr = map_base.keySet().toArray(new String[map_base.size()]);
		Arrays.sort(arr);
		for (String code : arr)
		{
			base = map_base.get(code);
			if (base.recent(date)) _logger.info("recent fund - " + base.toText());
			if (map.containsKey(code) || !ArmaUtil.hitfi(code) || ArmaUtil.hitfe(code)) continue;
			abm(base, list, col, map);
		}
		_logger.info("scan abm funds done, abm|" + map.size() + "|origin|" + map_base.size());

		// scan the short name via scanSinaQuoteJs
		// http://hq.sinajs.cn/rn=fgu87&list=sz150265,sz150266,sz168201
		int count = 0;
		StringBuffer sb = new StringBuffer();
		Map<String, TQuoteDaily> map_sinaquotejs = new HashMap<String, TQuoteDaily>();
		for (String code : list)
		{
			count++;
			sb.append(",").append(map.get(code).hqlist(true));
			if (count > 99)
			{
				scanSinaQuoteJs(sb.substring(1), map_sinaquotejs);
				count = 0;
				sb = new StringBuffer();
			}
		}
		if (sb.length() > 0) scanSinaQuoteJs(sb.substring(1), map_sinaquotejs);
		for (TQuoteDaily bean : map_sinaquotejs.values()) 
		{
			base = map.get(bean.getCode());
			if (base != null) base.setNameS(bean.getNameS());
		}
		for (TSymbol bean : map.values()) 
		{
			if (!ArmaUtil.hitf(bean)) continue;
			for (int i = InVarAM.s_names.length - 1; i >= 0; i--)
			{
				String strc = bean.code(i);
				list.remove(strc);
				list.add(0, strc);
			}
			break;
		}
		if (InVarAM.b_show_info && _logger.isDebugEnabled())
		{
			for (String code : list) 
			{
				if (!map.containsKey(code))  continue;
				_logger.debug("abm fund - " + map.get(code).toText());
			}
			_logger.debug(" **************** show all abm fund symbols done, abm|" + map.size() + " **************** ");
		}
		return list;
	}

	// http://finance.sina.com.cn/iframe/286/20091218/26.js
	// http://hq.sinajs.cn/rn=fgu87&list=f_150265,f_150266,f_168201
	private void scanSinaNetJs(Map<String, TSymbol> map_sinanetjs)
	{
		String url = WebUtil.substituteName("{rn}", "" + System.currentTimeMillis(), InVarAM.s_fund_iframe_js);
		List<String> lines = HttpUtil.lines(url, null);
		if (lines == null || lines.size() < 1) return;
		String line = lines.get(0);
		
		StringBuffer sb = new StringBuffer();
		int pos1 = line.indexOf("\""), pos2 = line.lastIndexOf("\""), count = 0;
		String line2= (pos1 != -1 && pos2 != -1 ? line.substring(pos1 + 1, pos2) : line);
		String[] arr = line2.split(",", -1), arr2;
		_logger.info("scanSinaIframeJs done, count|" + arr.length);
		Map<String, String> map_market = new HashMap<String, String>();
		for (String str : arr)
		{
			// 000091|of|0|, 150062|cf|1|sz
			arr2 = str.split("\\|", -1);
			if (arr2.length < 4) continue;
			if (!WebUtil.empty(arr2[3])) map_market.put(arr2[0], arr2[3].toLowerCase());
			if (sb.indexOf("f_" + arr2[0]) != -1) continue;
			// f_150062
			count++;
			sb.append(",f_").append(arr2[0]);
			if (count > 99)
			{
				scanSinaNetJs(sb.substring(1), map_market, map_sinanetjs);
				sb = new StringBuffer();
				count = 0;
				map_market.clear();
			}
		}
		if (sb.length() < 1) return;
		scanSinaNetJs(sb.substring(1), map_market, map_sinanetjs);
	}
	
	// http://hq.sinajs.cn/rn=fgu87&list=f_150265,f_150266,f_168201
	private void scanSinaNetJs(String list, Map<String, String> map_market, Map<String, TSymbol> map_sinanetjs)
	{
		String url = WebUtil.substituteName("{rn}", "" + System.currentTimeMillis(), InVarAM.s_hq_js);
		url = WebUtil.substituteName("{list}", list, url);
		List<String> lines = HttpUtil.lines(url, WebUtil.CHARSET_GB2312);
		if (lines == null || lines.size() < 1) return;
		_logger.info("scanSinaNetJs, count|" + lines.size());
		
		TSymbol bean;
		for (String line : lines)
		{
			bean = fromSinaNetJs(line, map_market);
			if (bean.empty()) continue;
			map_sinanetjs.put(bean.getCode(), bean);
			if (InVarAM.b_show_info && ArmaUtil.hitf(bean)) _logger.info("origin info via scanSinaNetJs - " + bean.toText() + "|" + line);
		}
	}

	// http://vip.stock.finance.sina.com.cn/fund_center/data/xml.php/NetValue_Service.getNetValueOpen
	private int scanSinaNetOpen(int num, Map<String, TSymbol> map_sinanetopen)
	{
		String url = WebUtil.substituteName("{rn}", "" + System.currentTimeMillis(), InVarAM.s_fund_net_open_xml);
		url = WebUtil.substituteName("{num}", "" + num, url);
		Node root = ParseUtil.parseRoot(HttpUtil.postXml(url, "", WebUtil.CHARSET_GB2312), WebUtil.CHARSET_GB2312);
		List<Node> data = ParseUtil.nodes("data/item", root);
		if (data == null || data.size() == 0) return -1;
		int total = WebUtil.str2int(ParseUtil.xpathNode("total_num", root));
		if (total > data.size()) return total;
		_logger.info("scanSinaNetOpen done, count|" + total);
		
		TSymbol bean;
		for (Node node : data)
		{
			bean = fromSinaNetOpen(node);
			if (bean.empty()) continue;
			map_sinanetopen.put(bean.getCode(), bean);
			if (InVarAM.b_show_info && ArmaUtil.hitf(bean)) 
			{
				XmlOutput4j xop = new XmlOutput4j();
				xop.appendNode(node);
				_logger.info("origin info via scanSinaNetOpen - " + bean.getCode() + "|" + xop.output());
			}
		}
		return 0;
	}

	// http://vip.stock.finance.sina.com.cn/fund_center/data/xml.php/NetValue_Service.getNetValueCX
	private int scanSinaNetCx(int num, Map<String, TSymbol> map_sinanetcx)
	{
		String url = WebUtil.substituteName("{rn}", "" + System.currentTimeMillis(), InVarAM.s_fund_net_cx_xml);
		url = WebUtil.substituteName("{num}", "" + num, url);
		Node root = ParseUtil.parseRoot(HttpUtil.postXml(url, "", WebUtil.CHARSET_GB2312), WebUtil.CHARSET_GB2312);
		List<Node> data = ParseUtil.nodes("data/item", root);
		if (data == null || data.size() == 0) return -1;
		int total = WebUtil.str2int(ParseUtil.xpathNode("total_num", root));
		if (total > data.size()) return total;
		_logger.info("scanSinaNetCx done, count|" + total);
		
		TSymbol bean;
		for (Node node : data)
		{
			bean = fromSinaNetCx(node);
			if (bean.empty()) continue;
			map_sinanetcx.put(bean.getCode(), bean);
			if (InVarAM.b_show_info && ArmaUtil.hitf(bean)) 
			{
				XmlOutput4j xop = new XmlOutput4j();
				xop.appendNode(node);
				_logger.info("origin info via scanSinaNetCx - " + bean.getCode() + "|" + xop.output());
			}
		}
		return 0;
	}
	
	// var hq_str_f_150265="中融一带一路分级A,1.005,1.005,1.005,2015-06-12,9.87814";
	// 基金名称, 今日净值, 累计净值, 昨日净值, 日期, 溢价
	private TSymbol fromSinaNetJs(String line, Map<String, String> map_market)
	{
		TSymbol bean = new TSymbol();
		if (WebUtil.empty(line)) return bean;
		int pos1 = line.indexOf("="), pos2 = line.lastIndexOf("_");
		if (pos1 == -1 || pos2 == -1 || pos1 < pos2) return bean;
		String code = line.substring(pos2 + 1, pos1);

		pos1 = line.indexOf("\"");
		pos2 = line.lastIndexOf("\"");
		String line2= (pos1 != -1 && pos2 != -1 ? line.substring(pos1 + 1, pos2) : line);
		String[] arr = line2.split(",", -1);
		if (arr.length < 6) return bean;
		
		bean.setCode(code);
		bean.setMarket(map_market.get(code));
		bean.setName(arr[0]);
		bean.setType(InVarAM.i_fund_type);
		// not used - arr[1], arr[2], arr[3], arr[4], arr[5]
		return bean;
	}

	/*		
		<item>
			<symbol>168201</symbol>
			<sname>中融一带一路分级</sname>
			<per_nav>1.0740</per_nav>
			<total_nav>1.0740</total_nav>
			<yesterday_nav>1.07</yesterday_nav>
			<nav_rate>0.3738</nav_rate>
			<nav_a>0.004</nav_a>
			<sg_states>开放</sg_states>
			<nav_date>2015-06-12</nav_date>
			<fund_manager>赵菲</fund_manager>
			<jjlx>偏股型基金</jjlx>
			<jjzfe>92447500</jjzfe>
		</item>
	*/
	// 基金名称, 基金经理, 今日净值, 累计净值, 昨日净值, 净值增长率, 日期
	private TSymbol fromSinaNetOpen(Node node)
	{
		TSymbol bean = new TSymbol();
		if (node == null) return bean;
		bean.setCode(ParseUtil.xpathNode("symbol", node));
		bean.setName(ParseUtil.xpathNode("sname", node));
		bean.setType(InVarAM.i_fund_type);
		bean.setManager(ParseUtil.xpathNode("fund_manager", node));
		// not used - nav_date, per_nav, total_nav, nav_rate, yesterday_nav, nav_a, sg_states, jjlx, jjzfe
		return bean;
	}

	/*
		<item>
			<symbol>150266</symbol>
			<sname>中融一带一路分级B</sname>
			<per_nav>1.1440</per_nav>
			<total_nav>1.1440</total_nav>
			<nav_rate>0.793</nav_rate>
			<discount_rate>7.86713</discount_rate>
			<start_date>2015-05-14</start_date>
			<end_date/>
			<nav_date>2015-06-12</nav_date>
			<fund_manager>赵菲</fund_manager>
			<jjlx>偏股型基金</jjlx>
			<zjzfe>206808</zjzfe>
		</item>
	 */
	// 基金名称, 基金经理, 上市日, 退市日, 今日净值, 累计净值, 净值增长率, 日期, 溢价
	private TSymbol fromSinaNetCx(Node node)
	{
		TSymbol bean = new TSymbol();
		if (node == null) return bean;
		bean.setCode(ParseUtil.xpathNode("symbol", node));
		bean.setName(ParseUtil.xpathNode("sname", node));
		bean.setType(InVarAM.i_fund_type);
		bean.setManager(ParseUtil.xpathNode("fund_manager", node));
		bean.setStartDate(ParseUtil.xpathNode("start_date", node));
		bean.setEndDate(ParseUtil.xpathNode("end_date", node));
		// not used - nav_date, per_nav, total_nav, nav_rate, discount_rate, jjlx, jjzfe
		return bean;
	}
	
	// merge funds from map to map_base
	private void merge(boolean right, String key, Map<String, TSymbol> map_base, Map<String, TSymbol> map)
	{
		TSymbol bean, base;
		for (String code : map.keySet())
		{
			bean = map.get(code);
			if (!map_base.containsKey(code))
			{
				map_base.put(code, bean);
				continue;
			}
			base = map_base.get(code);
			base.merge(bean);
		}
	}
	
	// scan the related funds - A, B, M
	private void abm(TSymbol base, List<String> list, Collection<TSymbol> col, Map<String, TSymbol> map)
	{
		if (base == null || base.empty()) return;
		
		int ret;
		TSymbol[] beans = new TSymbol[3];
		for (TSymbol bean : col)
		{
			ret = bean.abm(base) - InVarAM.i_fund_type;
			if (ret >= 0 && ret <= 2) beans[ret] = bean;
			if (beans[0] != null && beans[1] != null && beans[2] != null) break;
		}
		if (beans[0] == null || ArmaUtil.hitfe(beans[0].getCode()) || beans[1] == null || ArmaUtil.hitfe(beans[1].getCode()) || beans[2] == null || 
				ArmaUtil.hitfe(beans[2].getCode())) return;
		
		String manager = "", startDate = "", endDate = "", str;
		for (int i = 0; i < beans.length; i++)
		{				
			str = beans[i].getManager();
			if (!WebUtil.empty(str)) 
			{
				if (WebUtil.empty(manager)) manager = str;
				else if (!manager.equals(str)) _logger.info("manager conflict - " + beans[i].getCode() + "|" + str + " vs " + manager);
			}
			str = beans[i].getStartDate();
			if (!WebUtil.empty(str)) 
			{
				if (WebUtil.empty(startDate)) startDate = str;
				else if (!startDate.equals(str)) _logger.info("startDate conflict - " + beans[i].getCode() + "|" + str + " vs " + startDate);
			}
			str = beans[i].getEndDate();
			if (!WebUtil.empty(str)) 
			{
				if (WebUtil.empty(endDate)) endDate = str;
				else if (!endDate.equals(str)) _logger.info("endDate conflict - " + beans[i].getCode() + "|" + str + " vs " + endDate);
			}
		}
		for (int i = 0; i < beans.length; i++)
		{
			beans[i].setManager(manager);
			beans[i].setStartDate(startDate);
			beans[i].setEndDate(endDate);
			for (int j = 0; j < InVarAM.s_names.length; j++) beans[i].addFundInfo(InVarAM.s_names[j], beans[j].getCode());
			map.put(beans[i].getCode(), beans[i]);
			list.add(beans[i].getCode());
		}
		if (InVarAM.b_show_info && (ArmaUtil.hitf(beans[0]) || ArmaUtil.hitf(beans[1]) || ArmaUtil.hitf(beans[2]))) _logger.info("find focus abm funds - " + beans[1].toText());
	}
	
	// http://hq.sinajs.cn/rn=fgu87&list=sz150265,sz150266,sz168201
	public void scanSinaQuoteJs(String list, Map<String, TQuoteDaily> last_quotes_daily)
	{
		String url = WebUtil.substituteName("{rn}", "" + System.currentTimeMillis(), InVarAM.s_hq_js);
		url = WebUtil.substituteName("{list}", list, url);
		List<String> lines = HttpUtil.lines(url, WebUtil.CHARSET_GB2312);
		if (lines == null || lines.size() < 1) return;
		
		TQuoteDaily bean;
		String now = DateUtil.date24Str(new Date(), DateUtil.df_date_time, DateUtil.zone_cn), now_date = now.substring(0, 10), now_time = now.substring(11, 16);
		String date = (ArmaUtil.trade(now_date) && InVarAM.s_trade_times[0].compareTo(now_time) < 0 ? null : ArmaUtil.shiftTrade(-1, now_date));
		for (String line : lines)
		{
			bean = fromSinaQuoteJs(line, date);
			if (bean.empty()) continue;
			last_quotes_daily.put(bean.key(null), bean);
			if (InVarAM.b_show_info && ArmaUtil.hitfc(bean.getCode())) _logger.info("origin info via scanSinaQuoteJs - " + bean.getCode() + "|" + line);
		}
	}
	
	// 股票名字, 今日开盘价, 昨日收盘价, 当前价格, 今日最高价, 今日最低价, 买一, 卖一, 成交量, 成交金额, 买一至买五成交量及报价, 卖一至卖五成交量及报价, 日期, 时间
	// var hq_str_sz150265="一带A,0.906,0.907,0.909,0.912,0.906,0.909,0.910,70646320,64248520.137,819799,0.909,253095,0.908,6623431,0.907,12772552,0.906,42900,0.905,1594419,
	//		0.910,1315495,0.911,3328240,0.912,579900,0.913,4049500,0.914,2015-06-12,15:05:37,00";
	private TQuoteDaily fromSinaQuoteJs(String line, String date)
	{
		TQuoteDaily bean = new TQuoteDaily();
		if (WebUtil.empty(line)) return bean;
		int pos1 = line.indexOf("="), pos2 = line.lastIndexOf("_");
		if (pos1 == -1 || pos2 == -1 || pos1 < pos2) return bean;
		String code = line.substring(pos2 + 3, pos1);

		pos1 = line.indexOf("\"");
		pos2 = line.lastIndexOf("\"");
		String line2= (pos1 != -1 && pos2 != -1 ? line.substring(pos1 + 1, pos2) : line);
		String[] arr = line2.split(",", -1);
		if (arr.length < 32) return bean;
		bean.setCode(code);
		bean.setNameS(arr[0]);											// used for scan symbol only
		bean.setDate(date == null ? arr[30] : date);
		bean.setTime(date == null ? arr[31] : "15:34:03");
		bean.setPrice(WebUtil.s2d_0000(arr[3]));
		bean.setClosePrev(WebUtil.s2d_0000(arr[2]));
		bean.setOpen(WebUtil.s2d_0000(arr[1]));
		bean.setHigh(WebUtil.s2d_0000(arr[4]));
		bean.setLow(WebUtil.s2d_0000(arr[5]));
		bean.setVolume(WebUtil.d2d_0000(WebUtil.str2double(arr[8]) / 100d));
		bean.setAmount(WebUtil.d2d_0000(WebUtil.str2double(arr[9]) / 10000d));
		// not used - arr[6-7], arr[10-29]
		return bean;
	}
	
	// http://hq.sinajs.cn/rn=fgu87&list=f_150265,f_150266,f_168201
	public void scanSinaNetJs(String list, Map<String, TNetDaily> last_nets_daily)
	{
		String url = WebUtil.substituteName("{rn}", "" + System.currentTimeMillis(), InVarAM.s_hq_js);
		url = WebUtil.substituteName("{list}", list, url);
		List<String> lines = HttpUtil.lines(url, WebUtil.CHARSET_GB2312);
		if (lines == null || lines.size() < 1) return;
		_logger.info("scanSinaNetJs done, count|" + lines.size());
		
		TNetDaily bean;
		for (String line : lines)
		{
			bean = fromSinaNetJs(line);
			if (bean.empty()) continue;
			last_nets_daily.put(bean.key(null), bean);
			if (InVarAM.b_show_info && ArmaUtil.hitfc(bean.getCode())) _logger.info("origin info via scanSinaNetJs - " + bean.toText() + "|" + line);
		}
	}
	
	// var hq_str_f_150265="中融一带一路分级A,1.005,1.005,1.005,2015-06-12,9.87814";
	// 基金名称, 今日净值, 累计净值, 昨日净值, 日期, 溢价
	private TNetDaily fromSinaNetJs(String line)
	{
		TNetDaily bean = new TNetDaily();
		if (WebUtil.empty(line)) return bean;
		int pos1 = line.indexOf("="), pos2 = line.lastIndexOf("_");
		if (pos1 == -1 || pos2 == -1 || pos1 < pos2) return bean;
		String code = line.substring(pos2 + 1, pos1);

		pos1 = line.indexOf("\"");
		pos2 = line.lastIndexOf("\"");
		String line2= (pos1 != -1 && pos2 != -1 ? line.substring(pos1 + 1, pos2) : line);
		String[] arr = line2.split(",", -1);
		if (arr.length < 6) return bean;
		
		bean.setCode(code);
		bean.setDate(arr[4]);
		double net = WebUtil.s2d_0000(arr[1]), net_prev = WebUtil.s2d_0000(arr[3]);
		bean.setNet(net);
		bean.setNetTotal(WebUtil.s2d_0000(arr[2]));
		bean.setGrowth(WebUtil.d2d_0000((net - net_prev) * 100d / net_prev));
		// bean.setPremium(WebUtil.s2d_0000(arr[5])); 					// wrong value, not used
		// not used - arr[0], arr[5]
		return bean;
	}
}
