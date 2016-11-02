/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.fund;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.arma.web.service.TFundDaoService;
import com.arma.web.service.bean.TNetDaily;
import com.arma.web.service.bean.TQuoteDaily;
import com.arma.web.service.bean.TSymbol;
import com.arma.web.util.ArmaUtil;
import com.arma.web.util.InVarAM;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class FundCacher
{
	private static boolean _is_trade_day;
	private static boolean _need_quotes_daily_flush = false;
	private static boolean _finish_quotes_daily_flush = false;
	private static boolean _is_rt_stats_clear = true;
	private static boolean _is_rt_stats_first = true;					// run at least one time to fill real-time data
	private static String _last_trade;
	private static String[] _hq_quote;
	private static String[] _hq_net;
	private static Map<String, TSymbol> _symbols = new HashMap<String, TSymbol>();
	private static Map<String, TQuoteDaily> _quotes_daily = new HashMap<String, TQuoteDaily>();
	private static Map<String, TNetDaily> _nets_daily = new HashMap<String, TNetDaily>();
	private static Map<String, FundRtStats> _rt_stats_daily = new HashMap<String, FundRtStats>();
	private static Map<String, FundRtStats> _rt_stats_min = new HashMap<String, FundRtStats>();
	private static Map<String, List<TNetDaily>> _nets_daily_list = new HashMap<String, List<TNetDaily>>();
	
	private static final Logger _logger = Logger.getLogger(FundCacher.class);
	
	public static void setup()
	{
		String now = DateUtil.date24Str(new Date(), DateUtil.df_date_time, DateUtil.zone_cn), now_date = now.substring(0, 10), now_time = now.substring(11, 16);
		_is_trade_day = ArmaUtil.trade(now_date);
		_last_trade = ArmaUtil.shiftTrade(-1, now_date);
		String min_trade = ArmaUtil.shiftTrade(-1, _last_trade);
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);

		// load symbols from database
		List<TSymbol> symbols = dao.getSymbols(null);
		List<String> cur_codes = new ArrayList<String>();
		for (TSymbol bean : symbols) 
		{
			if (!cur_codes.contains(bean.getCode())) cur_codes.add(bean.getCode());
			_symbols.put(bean.getCode(), bean);
		}
		_logger.info("setup: load symbols from database done, " + _symbols.size());
		flushFocus(_symbols);
		// flush symbols via scanSinaNetJs, scanSinaNetOpen, scanSinaNetCx, scanSinaQuoteJs
		if (InVarAM._b_scan_symbols_setup)
		{
			flushSymbols();
			_logger.info("setup: flush symbols via scan done, " + _symbols.size());
		}
		if (_symbols.size() < 1) return;

		List<TQuoteDaily> quotes = dao.getQuoteDailys("(high_60<=0.0001 or low_60<=0.0001 or volume_60<=0.0001) order by code, date desc");
		// normalize quotes daily to fill the high_60, low_60, volume_60 excluded the last_trade 
		normalizeQuoteDaily(quotes);
		_logger.info("setup: normalize quotes daily in database done, " + quotes.size());
		quotes = dao.getQuoteDailys("date >='" + min_trade + "' order by code, date desc");
		for (TQuoteDaily bean : quotes) putQuoteDaily(bean);
		// flush quotes daily via scanSinaQuoteJs and normalize quotes daily to fill the high_60, low_60, volume_60
		_need_quotes_daily_flush = (!_is_trade_day || InVarAM.s_trade_times[0].compareTo(now_time) > 0 || InVarAM.s_trade_times[5].compareTo(now_time) < 0);
		flushQuoteDaily(null);
		for (String code : _symbols.keySet()) 
		{
			if (_quotes_daily.containsKey(code) || _symbols.get(code).getType() == InVarAM.i_fund_type + 2) continue;
			quotes = dao.getQuoteDailys("code='" + code + "' order by code, date desc limit 1");
			if (quotes.size() > 0) putQuoteDaily(quotes.get(0));
			else _logger.info("setup: no quote daily - " + _symbols.get(code).toText());
		}
		_logger.info("setup: load quotes daily from database done, " + _quotes_daily.size());

		List<TNetDaily> nets = dao.getNetDailys("(premium<-101 or premium2<-101 or premium_high_5<-101 or premium_low_5<-101) order by code, date desc");
		// normalize nets daily to fill the premium, premium2, premium_high_5, premium_low_5 excluded the last_trade 
		normalizeNetDaily(nets);
		_logger.info("setup: normalize nets daily in database done, " + nets.size());
		nets = dao.getNetDailys("date >='" + min_trade + "' order by code, date desc");
		for (TNetDaily bean : nets) putNetDaily(bean);
		// flush nets daily via scanSinaNetJs and normalize nets daily to fill the premium, premium2, premium_high_5, premium_low_5
		flushNetDaily();
		for (String code : _symbols.keySet()) 
		{
			if (_nets_daily.containsKey(code)) continue;
			nets = dao.getNetDailys("code='" + code + "' order by code, date desc limit 1");
			if (nets.size() > 0) putNetDaily(nets.get(0));
			else _logger.info("setup: no net daily - " + _symbols.get(code).toText());
		}
		_logger.info("setup: load nets daily from database done, " + _nets_daily.size());
		_logger.info(" **************** setup done, " + now + "|" + _last_trade + "|" + _is_trade_day + " **************** ");
	}
	
	private static void putQuoteDaily(TQuoteDaily bean)
	{
		String code = (bean == null ? null : bean.getCode());
		if (WebUtil.empty(code)) return;
		TQuoteDaily cur = _quotes_daily.get(code);
		if (cur == null || cur.getDate().compareTo(bean.getDate()) < 0) _quotes_daily.put(code, bean);
	}
	
	private static void putNetDaily(TNetDaily bean)
	{
		String code = (bean == null ? null : bean.getCode());
		if (WebUtil.empty(code)) return;
		TNetDaily cur = _nets_daily.get(code);
		if (cur == null || cur.getDate().compareTo(bean.getDate()) < 0) _nets_daily.put(code, bean);
	}
	
	// flush quotes daily and net daily via scanSinaQuoteJs, scanSinaNetJs, also normalize them, for the date after last_trade, per 30 minutes
	public static void flush()
	{
		String now = DateUtil.date24Str(new Date(), DateUtil.df_date_time, DateUtil.zone_cn), now_date = now.substring(0, 10);
		String last_trade = ArmaUtil.shiftTrade(-1, now_date);
		if (!_last_trade.equals(last_trade))
		{
			_is_trade_day = ArmaUtil.trade(now_date);
			_is_rt_stats_clear = false;
			_last_trade = last_trade;
			_nets_daily_list.clear();
			if (_is_trade_day && InVarAM._b_scan_symbols_setup) flushSymbols();
			_logger.info("flush: reset trade params, " + now + "|" + _last_trade + "|" + _is_trade_day);
		}
		flushQuoteDaily(null);
		flushNetDaily();
		_logger.info(" **************** flush done, " + now + "|" + _last_trade + "|" + _is_trade_day + " **************** ");
	}

	// flush symbols via scanSinaNetJs, scanSinaNetOpen, scanSinaNetCx, scanSinaQuoteJs
	public static void flushSymbols()
	{
		// scan symbols via scanSinaNetJs, scanSinaNetOpen, scanSinaNetCx, scanSinaQuoteJs
		long start = System.currentTimeMillis();
		Map<String, TSymbol> last_symbols = new HashMap<String, TSymbol>();
		List<String> last_codes = new FundSpider().scanFundSymbol(last_symbols);
		
		String cur_text;
		TSymbol cur, last;
		List<String> cur_codes = new ArrayList<String>();
		List<Map<String, Object>> insert_params = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> update_params = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> delete_params = new ArrayList<Map<String, Object>>();
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		if (last_symbols.size() > 0)
		{
			flushFocus(last_symbols);
			
			// update symbols into cacher and database
			for (String code : last_codes)
			{
				cur = _symbols.get(code);
				last = last_symbols.get(code);
				cur_text = (cur == null ? "" : cur.toText());
				if (cur == null)
				{
					_symbols.put(code, last);
					insert_params.add(last.toDbMap());
					_logger.info("symbol insert, " + last.toText());
				}
				else if (cur.merge(last))
				{
					update_params.add(cur.toDbMap());
					_logger.info("symbol update, new [" + cur.toText() + "], cur [" + cur_text + "]");
				}
				cur_codes.remove(code);
			}
			for (String code : cur_codes) 
			{
				if (true) continue;
				cur = _symbols.remove(code);
				if (cur == null) continue;
				delete_params.add(cur.toDbMap());
				_logger.info("symbol delete, " + cur.toText());
			}
			dao.insertSymbols(insert_params);
			dao.updateSymbols(update_params);
			dao.deleteSymbols(delete_params);
			_hq_quote = null;
			_hq_net = null;
		}
		_logger.info(" **************** symbol flush done, " + WebUtil.time2str(start) + "|" + insert_params.size() + "|" + update_params.size() + "|" + delete_params.size() + " **************** ");
	}
	
	private static  void flushFocus(Map<String, TSymbol> symbols)
	{
		TSymbol bean;
		List<String> focus = new ArrayList<String>();
		for (String str : InVarAM.s_fund_focus) 
		{
			bean = hitFocus(str, symbols);
			focus.add(bean == null ? str : bean.getCode());
		}
		InVarAM.s_fund_focus = focus.toArray(new String[focus.size()]);
	}
	
	private static TSymbol hitFocus(String focus, Map<String, TSymbol> symbols)
	{
		for (TSymbol bean : symbols.values()) if (bean.hit(focus)) return bean;
		return null;
	}
	
	// generate the scan list string with scanSinaQuoteJs, scanSinaNetJs
	private static String[] getHqList(boolean quote)
	{
		if (_hq_quote == null || _hq_net == null)
		{
			int i_quote = 0, i_net = 0;
			String str_quote = "", str_net = "";
			List<String> hq_quote = new ArrayList<String>(), hq_net = new ArrayList<String>();
			for (TSymbol bean : _symbols.values())
			{
				i_net++;
				str_net +="," + bean.hqlist(false);
				if (i_net > 99)
				{
					i_net = 0;
					hq_net.add(str_net.substring(1));
					str_net = "";
				}
				if (bean.getType() == InVarAM.i_fund_type + 2) continue;
				i_quote++;
				str_quote +="," + bean.hqlist(true);
				if (i_quote > 99)
				{
					i_quote = 0;
					hq_quote.add(str_quote.substring(1));
					str_quote = "";
				}
			}
			if (str_quote.length() > 0) hq_quote.add(str_quote.substring(1));
			_hq_quote = hq_quote.toArray(new String[hq_quote.size()]);
			if (str_net.length() > 0) hq_net.add(str_net.substring(1));
			_hq_net = hq_net.toArray(new String[hq_net.size()]);
		}
		return (quote ? _hq_quote : _hq_net);
	}

	// flush quotes daily via scanSinaQuoteJs and normalize quotes daily to fill the high_60, low_60, volume_60
	public static void flushQuoteDaily(Map<String, TQuoteDaily> quotes_daily)
	{
		if (!_need_quotes_daily_flush || _finish_quotes_daily_flush) return;
		
		long start = System.currentTimeMillis();
		// scan quotes daily via scanSinaQuoteJs
		if (quotes_daily == null || quotes_daily.size() < 1) 
		{
			if (quotes_daily == null) quotes_daily = new HashMap<String, TQuoteDaily>();
			for (String list : getHqList(true)) new FundSpider().scanSinaQuoteJs(list, quotes_daily);
		}
		if (quotes_daily.size() > 0) 
		{
			_need_quotes_daily_flush = false;
			_finish_quotes_daily_flush = true;
		}
		// merge quotes daily to cacher
		String cur_text;
		TQuoteDaily cur, last;
		List<Map<String, Object>> insert_params = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> update_params = new ArrayList<Map<String, Object>>();
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		for (String key : quotes_daily.keySet())
		{
			last = quotes_daily.get(key);
			if (key.compareTo(last.key(_last_trade)) < 0) continue;
			cur = _quotes_daily.get(last.getCode());
			cur_text = (cur == null ? "" : cur.toText());
			if (cur == null || !cur.getDate().equals(last.getDate()))
			{
				_quotes_daily.put(last.getCode(), last);
				insert_params.add(last.toDbMap());
				_logger.info("quote daily insert, " + _symbols.get(last.getCode()).getNameS() + "|" + last.toText());
			}
			else if (cur.merge(last))
			{
				update_params.add(cur.toDbMap());
				_logger.info("quote daily update, new [" + _symbols.get(cur.getCode()).getNameS() + "|" + cur.toText() + "], cur [" + cur_text + "]");
			}
		}
		dao.insertQuoteDailys(insert_params);
		dao.updateQuoteDailys(update_params);
		_logger.info(" **************** quotes daily flush done, " + WebUtil.time2str(start) + "|" + insert_params.size() + "|" + update_params.size() + " **************** ");

		// normalize quotes daily to fill the high_60, low_60, volume_60
		List<TQuoteDaily> quotes = new ArrayList<TQuoteDaily>();
		for (TQuoteDaily bean : _quotes_daily.values()) if (!bean.normalize()) quotes.add(bean);
		normalizeQuoteDaily(quotes);
	}

	// normalize quotes daily to fill the high_60, low_60, volume_60
	private static void normalizeQuoteDaily(List<TQuoteDaily> quotes)
	{
		int count = 0;
		StringBuffer sb = new StringBuffer();
		long start = System.currentTimeMillis();
		List<String> keys = new ArrayList<String>();
		String date_s = null, date_e = null, code_last = null;
		Map<String, TQuoteDaily> quotes_daily = new HashMap<String, TQuoteDaily>();
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		List<Map<String, Object>> update_params = new ArrayList<Map<String, Object>>();
		for (TQuoteDaily bean : quotes) 
		{
			keys.add(bean.key(null));
			if (code_last != null && !code_last.equals(bean.getCode()))
			{
				sb.append("or (date>'").append(ArmaUtil.shiftTrade(-InVarAM.i_quote_stats, date_s)).append("' and date <='").append(date_e);
				sb.append("' and code='").append(code_last).append("') ");
				count += ArmaUtil.daysTrade(date_s, date_e) + InVarAM.i_quote_stats;
				if (count >= 300)
				{
					List<TQuoteDaily> ret = dao.getQuoteDailys(sb.substring(3) + "order by code, date desc");
					for (TQuoteDaily tqd : ret)  
					{
						putQuoteDaily(tqd);
						quotes_daily.put(tqd.key(null), tqd);
					}
					for (String key : keys) normalizeQuoteDaily(key, quotes_daily, update_params);
					count = 0;
					sb = new StringBuffer();
					keys.clear();
					quotes_daily.clear();
				}
				date_s = null;
				date_e = null;
				code_last = null;
			}
			if (date_s == null || date_s.compareTo(bean.getDate()) > 0) date_s = bean.getDate();
			if (date_e == null || date_e.compareTo(bean.getDate()) < 0) date_e = bean.getDate();
			if (code_last == null) code_last = bean.getCode();
		}
		if (date_s != null && date_s != null && code_last != null)
		{
			sb.append("or (date>'").append(ArmaUtil.shiftTrade(-InVarAM.i_quote_stats, date_s)).append("' and date <='").append(date_e);
			sb.append("' and code='").append(code_last).append("') ");
			List<TQuoteDaily> ret = dao.getQuoteDailys(sb.substring(3) + "order by code, date desc");
			for (TQuoteDaily tqd : ret)
			{
				putQuoteDaily(tqd);
				quotes_daily.put(tqd.key(null), tqd);
			}
			for (String key : keys) normalizeQuoteDaily(key, quotes_daily, update_params);
		}
		dao.updateQuoteDailys(update_params);
		_logger.info("normalize quotes daily done - " + WebUtil.time2str(start) + "|" + quotes.size() + "|" + update_params.size());
	}
	
	// normalize quote daily to fill the high_60, low_60, volume_60
	private static void normalizeQuoteDaily(String key, Map<String, TQuoteDaily> quotes_daily, List<Map<String, Object>> update_params)
	{
		String[] arr = key.split("_");
		TQuoteDaily quote_daily = quotes_daily.get(key), bean;
		if (quote_daily == null) return;
		String text = quote_daily.toText();

		int count = 0;
		double h60 = -1d, l60 = -1d, v60 = 0d;
		String start_key = quote_daily.key(ArmaUtil.shiftTrade(-InVarAM.i_quote_stats, arr[1]));
		for (String key2 : quotes_daily.keySet()) 
		{
			if (key2.compareTo(start_key) < 0 || key2.compareTo(key) > 0) continue;
			bean = quotes_daily.get(key2);
			if (bean == null) continue;
			if (h60 < 0d || h60 < bean.getHigh()) h60 = bean.getHigh();
			if (l60 < 0d || l60 > bean.getLow()) l60 = bean.getLow();
			if (bean.getVolume() < 0d) continue;
			count++;
			v60 += bean.getVolume();
		}
		v60 = WebUtil.d2d_0000(count == 0 ? -1d : v60 / count);
		boolean success = quote_daily.normalize(h60, l60, v60);
		if (success) 
		{
			update_params.add(quote_daily.toDbMap());
			_logger.info("quote daily normalize " + (success ? "done" : "failed") + ", new [" + _symbols.get(quote_daily.getCode()).getNameS() + "|" + quote_daily.toText() + "], cur [" + text + "]");
		}
	}

	// flush nets daily via scanSinaNetJs and normalize nets daily to fill the premium, premium2, premium_high_5, premium_low_5
	public static void flushNetDaily()
	{
		long start = System.currentTimeMillis();
		// scan nets daily via scanSinaNetJs
		Map<String, TNetDaily> nets_daily = new HashMap<String, TNetDaily>();
		for (String list : getHqList(false)) new FundSpider().scanSinaNetJs(list, nets_daily);
		// merge nets daily to cacher
		String cur_text;
		TNetDaily cur, last;
		List<Map<String, Object>> insert_params = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> update_params = new ArrayList<Map<String, Object>>();
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		for (String key : nets_daily.keySet())
		{
			last = nets_daily.get(key);
			if (key.compareTo(last.key(_last_trade)) < 0) continue;
			cur = _nets_daily.get(last.getCode());
			cur_text = (cur == null ? "" : cur.toText());
			if (cur == null || !cur.getDate().equals(last.getDate()))
			{
				_nets_daily.put(last.getCode(), last);
				insert_params.add(last.toDbMap());
				_logger.info("net daily insert, " + _symbols.get(last.getCode()).getNameS() + "|" + last.toText());
			}
			else if (cur.merge(last))
			{
				update_params.add(cur.toDbMap());
				_logger.info("net daily update, new [" + _symbols.get(cur.getCode()).getNameS() + "|" + cur.toText() + "], cur [" + cur_text + "]");
			}
		}
		dao.insertNetDailys(insert_params);
		dao.updateNetDailys(update_params);
		_logger.info(" **************** nets daily flush done, " + WebUtil.time2str(start) + "|" + insert_params.size() + "|" + update_params.size() + " **************** ");

		// normalize nets daily to fill the premium, premium2, premium_high_5, premium_low_5
		List<TNetDaily> nets = new ArrayList<TNetDaily>();
		for (TNetDaily bean : _nets_daily.values()) if (!bean.normalize()) nets.add(bean);
		normalizeNetDaily(nets);
	}

	// normalize nets daily to fill the premium, premium2, premium_high_5, premium_low_5
	private static void normalizeNetDaily(List<TNetDaily> nets)
	{
		int count = 0;
		StringBuffer sb = new StringBuffer();
		long start = System.currentTimeMillis();
		List<String> keys = new ArrayList<String>();
		String date_s = null, date_e = null, code_last = null;
		Map<String, TNetDaily> nets_daily = new HashMap<String, TNetDaily>();
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		List<Map<String, Object>> update_params = new ArrayList<Map<String, Object>>();
		for (TNetDaily bean : nets) 
		{
			keys.add(bean.key(null));
			if (code_last != null && !code_last.equals(bean.getCode()))
			{
				sb.append("or (date>'").append(ArmaUtil.shiftTrade(-InVarAM.i_net_stats, date_s)).append("' and date <='").append(date_e);
				sb.append("' and code='").append(code_last).append("') ");
				count += ArmaUtil.daysTrade(date_s, date_e) + InVarAM.i_net_stats;
				if (count >= 300)
				{
					List<TNetDaily> ret = dao.getNetDailys(sb.substring(3) + "order by code, date desc");
					for (TNetDaily tnd : ret)
					{
						putNetDaily(tnd);
						nets_daily.put(tnd.key(null), tnd);
					}
					for (String key : keys) normalizeNetDaily(key, nets_daily, update_params);
					count = 0;
					sb = new StringBuffer();
					keys.clear();
					nets_daily.clear();
				}
				date_s = null;
				date_e = null;
				code_last = null;
			}
			if (date_s == null || date_s.compareTo(bean.getDate()) > 0) date_s = bean.getDate();
			if (date_e == null || date_e.compareTo(bean.getDate()) < 0) date_e = bean.getDate();
			if (code_last == null) code_last = bean.getCode();
		}
		if (date_s != null && date_s != null && code_last != null)
		{
			sb.append("or (date>'").append(ArmaUtil.shiftTrade(-InVarAM.i_net_stats, date_s)).append("' and date <='").append(date_e);
			sb.append("' and code='").append(code_last).append("') ");
			List<TNetDaily> ret = dao.getNetDailys(sb.substring(3) + "order by code, date desc");
			for (TNetDaily tnd : ret)
			{
				putNetDaily(tnd);
				nets_daily.put(tnd.key(null), tnd);
			}
			for (String key : keys) normalizeNetDaily(key, nets_daily, update_params);
		}
		dao.updateNetDailys(update_params);
		_logger.info("normalize nets daily finish - " + WebUtil.time2str(start) + "|" + nets.size() + "|" + update_params.size());
	}

	// normalize net daily to fill the premium, premium2, premium_high_5, premium_low_5
	private static void normalizeNetDaily(String key, Map<String, TNetDaily> nets_daily, List<Map<String, Object>> update_params)
	{
		String[] arr = key.split("_");
		TSymbol symbol = _symbols.get(arr[0]);
		TNetDaily net_daily = nets_daily.get(key), bean;
		if (symbol == null || net_daily == null) return;
		String text = net_daily.toText();

		String start_key = net_daily.key(ArmaUtil.shiftTrade(-InVarAM.i_net_stats, arr[1]));
		boolean success = net_daily.normalize(premium(0, null, null, net_daily, symbol), premium(1, null, null, net_daily, symbol));
		double ph5 = -200d, pl5 = -200d, price;
		for (String key2 : nets_daily.keySet()) 
		{
			if (key2.compareTo(start_key) < 0 || key2.compareTo(key) > 0) continue;
			bean = nets_daily.get(key2);
			if (bean == null) continue;
			price = premium(2, null, null, net_daily, symbol);
			if (ph5 < -101d || ph5 < price) ph5 = price;
			price = premium(3, null, null, net_daily, symbol);
			if (pl5 < -101d || pl5 > price) pl5 = price;
		}
		success = net_daily.normalize5(ph5, pl5) && success;
		if (success) 
		{
			update_params.add(net_daily.toDbMap());
			_logger.info("net daily normalize " + (success ? "done" : "failed") + ", new [" + symbol.getNameS() + "|" + net_daily.toText() + "], cur [" + text + "]");
		}
	}

	// 0: premium vs net, 1: premium vs netPrev, 2: high premium vs netPrev, 3 : low premium vs netPrev
	// 4: real-time premium vs net, 5: real-time premium vs netPrev, 6: high real-time premium vs netPrev, 7: low real-time premium vs netPrev
	private static double premium(int type, TQuoteDaily quote_rt_a, TQuoteDaily quote_rt_b, TNetDaily net_daily, TSymbol symbol)
	{
		if (net_daily == null || net_daily.empty() || symbol == null || symbol.empty()) return -200d;
		double net = net_daily.getNet() / (type == 0 || type == 4 ? 1 : 1 + net_daily.getGrowth() / 100d);
		if (symbol.getType() - InVarAM.i_fund_type == 2)
		{
			TQuoteDaily quote_daily_a = _quotes_daily.get(symbol.code(0));
			TQuoteDaily quote_daily_b = _quotes_daily.get(symbol.code(1));
			if (quote_daily_a != null && quote_daily_b != null) 
				return WebUtil.d2d_0000((price(type, quote_daily_a, quote_rt_a) + price(type, quote_daily_b, quote_rt_b) - net * 2d) * 50d / net);
		}
		else
		{
			TQuoteDaily quote_daily = _quotes_daily.get(symbol.getCode());
			TQuoteDaily quote_rt = (quote_rt_a != null && quote_rt_a.key(null).startsWith(symbol.getCode()) ? quote_rt_a : quote_rt_b);
			if (quote_daily != null) return WebUtil.d2d_0000((price(type, quote_daily, quote_rt) - net) * 100d / net);
		}
		return -200d;
	}

	// 0: premium vs net, 1: premium vs netPrev, 2: high premium vs netPrev, 3 : low premium vs netPrev
	// 4: real-time premium vs net, 5: real-time premium vs netPrev, 6: high real-time premium vs netPrev, 7: low real-time premium vs netPrev
	private static double price(int type, TQuoteDaily quote_daily, TQuoteDaily quote_rt)
	{
		return (type == 0 || type == 1 ? quote_daily.getPrice() : (type == 2 ? quote_daily.getHigh() : (type == 3 ? quote_daily.getLow() :  
			(quote_rt == null ? -200d : (type == 4 || type == 5 ? quote_rt.getPrice() : (type == 6 ? quote_rt.getHigh() : quote_rt.getLow()))))));
	}
	
	public static boolean needUpdateRtStats()
	{
		if (_is_rt_stats_first) return true;
		if (!_is_trade_day) return false;
		String now = DateUtil.date24Str(new Date(), DateUtil.df_date_time, DateUtil.zone_cn), now_time = now.substring(11, 16);
		boolean start = InVarAM.s_trade_times[0].compareTo(now_time) < 0;
		if (!_is_rt_stats_clear && start)
		{
			_rt_stats_daily.clear();
			_rt_stats_min.clear();
			_finish_quotes_daily_flush = false;
			_is_rt_stats_clear = true;
			_logger.info("rtstats: clear outdated cacher data, " + now + "|" + _last_trade + "|" + _is_trade_day);
		}
		if (_is_trade_day && InVarAM.s_trade_times[5].compareTo(now_time) < 0) _need_quotes_daily_flush = true;
		return (InVarAM._b_rt_stats_anytime || (start && InVarAM.s_trade_times[6].compareTo(now_time) > 0));
	}

	// flush real-time stats via scanSinaQuoteJs and normalize real-time stats to fill the high_60, low_60, premium, premium2, premium_high_5, premium_low_5, per 50 seconds
	public static void flushRtStats()
	{
		long start = System.currentTimeMillis();
		// scan quotes rt via scanSinaQuoteJs
		Map<String, TQuoteDaily> quotes_rt = new HashMap<String, TQuoteDaily>();
		for (String list : getHqList(true)) new FundSpider().scanSinaQuoteJs(list, quotes_rt);
		flushQuoteDaily(quotes_rt);
		// merge quotes rt to cacher
		TSymbol symbol;
		TQuoteDaily quote_rt;
		List<String> cur_codes = new ArrayList<String>();
		String time = null, str;
		for (String key : quotes_rt.keySet())
		{
			quote_rt = quotes_rt.get(key);
			if (key.compareTo(quote_rt.key(_last_trade)) < 0 || cur_codes.contains(quote_rt.getCode())) continue;
			str = quote_rt.getDate() + "T" + quote_rt.getTime();
			if (time == null || time.compareTo(str) < 0) time = str;
			symbol = normalizeRtStats(key, quotes_rt);
			if (symbol == null) continue;
			for (int i = 0; i < InVarAM.s_names.length; i++) if (!cur_codes.contains(symbol.code(i))) cur_codes.add(symbol.code(i));
		}
		if (_is_rt_stats_first) _is_rt_stats_first = false;
		_logger.info(" **************** real-time stats flush done, " + WebUtil.time2str(start) + "|" + WebUtil.unull(time) + "|" + cur_codes.size() + "|" + quotes_rt.size() + " **************** ");
	}

	// normalize real-time stats to fill the high_60, low_60, premium, premium2, premium_high_5, premium_low_5
	private static TSymbol normalizeRtStats(String key, Map<String, TQuoteDaily> quotes_rt)
	{
		boolean show_info = false, success;
		String[] codes = new String[3], arr = key.split("_");
		TSymbol symbol = _symbols.get(arr[0]);
		FundRtStats[] rt_stats = new FundRtStats[3];
		TSymbol[] symbols = new TSymbol[3];
		TQuoteDaily[] quote_daily = new TQuoteDaily[2], quote_rt = new TQuoteDaily[2];
		TNetDaily[] net_daily = new TNetDaily[3];
		for (int i = 0; i < InVarAM.s_names.length; i++)
		{
			codes[i] = symbol.code(i);
			rt_stats[i] = new FundRtStats(codes[i]);
			symbols[i] = _symbols.get(codes[i]);
			net_daily[i] = _nets_daily.get(codes[i]);
			if (symbols[i] == null || net_daily[i] == null) continue;
			rt_stats[i].merge(net_daily[i]);
			show_info = show_info || ArmaUtil.hitf(symbols[i]);
			if (i < 2)
			{
				quote_daily[i] = _quotes_daily.get(codes[i]);
				quote_rt[i] = quotes_rt.get(codes[i] + "_" + arr[1]);
				if (quote_daily[i] == null || quote_rt[i] == null) continue;
				rt_stats[i].merge2(quote_rt[i]);
				rt_stats[i].setDateQuote(quote_rt[i].getDate());
			}
			else
			{
				rt_stats[i].setDateQuote(quote_rt[1].getDate());
				rt_stats[i].setMinute(quote_rt[1].getTime());
			}
		}
		double h60, l60, p, p2, ph5, pl5;
		for (int i = 0; i < InVarAM.s_names.length; i++)
		{
			success = true;
			if (i < 2)
			{
				h60 = Math.max(rt_stats[i].getHigh(), (quote_daily[i] == null ? 0d : quote_daily[i].getHigh60()));
				l60 = Math.min(rt_stats[i].getLow(), (quote_daily[i] == null ? 0d : quote_daily[i].getLow60()));
				success = success && rt_stats[i].normalize(h60, l60, (quote_daily[i] == null ? 0d : quote_daily[i].getVolume60()));
			}
			p = premium(4, quote_rt[0], quote_rt[1], net_daily[i], symbols[i]);
			p2 = premium(5, quote_rt[0], quote_rt[1], net_daily[i], symbols[i]);
			success = success && rt_stats[i].normalize(p, p2);
			ph5 = premium(6, quote_rt[0], quote_rt[1], net_daily[i], symbols[i]);
			pl5 = premium(7, quote_rt[0], quote_rt[1], net_daily[i], symbols[i]);
			success = success && rt_stats[i].normalize5(ph5, pl5);
			//if (success) 
			{
				_rt_stats_daily.put(rt_stats[i].getCode(), rt_stats[i]);
				_rt_stats_min.put(rt_stats[i].key(), rt_stats[i]);
			}
		}
		return symbols[1];
	}
	
	public static List<String> filter(String symbol)
	{
		List<String> codes_b = new ArrayList<String>(), includes = new ArrayList<String>(), excludes = new ArrayList<String>(), list;
		if (!WebUtil.empty(symbol))
		{
			int pos = -1, pos2 = -1;
			String[] arr = symbol.split("\\+|-");
			list = (symbol.startsWith("-") ? excludes : includes);
			list.add(arr[0]);
			for (int i = 1; i < arr.length; i++)
			{
				pos2 = symbol.indexOf(arr[i - 1] + "-" + arr[i], pos);
				if (pos2 != -1) list = excludes;
				else
				{
					pos2 = symbol.indexOf(arr[i - 1] + "+" + arr[i], pos);
					if (pos2 != -1) list = includes;
				}
				if (pos2 == -1) continue;
				pos = pos2 + arr[i - 1].length() + 1;
				if (!includes.contains(arr[i]) && !excludes.contains(arr[i])) list.add(arr[i]);
			}
		}
		for (TSymbol bean : _symbols.values()) if (bean.filter(includes, excludes) && !codes_b.contains(bean.code(1))) codes_b.add(bean.code(1));
		return codes_b;
	}

	public static int compare(boolean asc, int sort_type, String code_b1, String code_b2)
	{
		TSymbol s1 = _symbols.get(code_b1), s2 = _symbols.get(code_b2);
		int ret = 0, ret2 = comparte(s1.getCode(), s2.getCode());
		
		if (sort_type == 111) ret = ret2;
		else if (sort_type == 112) ret = comparte(s1.getNameS(), s2.getNameS());
		else if (sort_type == 113) ret = comparte(s1.getStartDate(), s2.getStartDate());
		else if (sort_type == 114) ret = comparte(s1.getManager(), s2.getManager());
		else if (sort_type == 115) 
		{
			double d1 = 0, d2 = 0;
			for (int i = 0; i < InVarAM.s_names.length; i++)
			{
				TSymbol ts1 = _symbols.get(s1.code(i)), ts2 = _symbols.get(s2.code(i));
				if (ts1 != null) d1 += ts1.getEquity();
				if (ts2 != null) d2 += ts2.getEquity();
			}
			ret = comparte(d1, d2);
		}
		else if (sort_type == 406 || sort_type == 416 || sort_type == 426) 
		{
			int pos = (((sort_type % 100) / 10) % 3);
			TSymbol ts1 = _symbols.get(s1.code(pos)), ts2 = _symbols.get(s2.code(pos));
			ret = comparte((ts1 == null ? 0 : ts1.getEquity()), (ts2 == null ? 0 : ts2.getEquity()));
		}
		else
		{
			int pos = (((sort_type % 100) / 10) % 3);
			FundRtStats stat1 = _rt_stats_daily.get(s1.code(pos)), stat2 = _rt_stats_daily.get(s2.code(pos));
			if (stat1 == null && stat2 == null) return (asc ? ret2 : 0 - ret2);
			else if (stat1 == null) return 1;
			else if (stat2 == null) return -1;

			if (sort_type == 201 || sort_type == 211) ret = comparte(stat1.getPrice(), stat2.getPrice());
			else if (sort_type == 202 || sort_type == 212) 
			{
				double growth1 = (stat1.getClosePrev() < 0.0001d ? -110d : WebUtil.d2d_00((stat1.getPrice() - stat1.getClosePrev()) * 100d / stat1.getClosePrev()));
				double growth2 = (stat2.getClosePrev() < 0.0001d ? -110d : WebUtil.d2d_00((stat2.getPrice() - stat2.getClosePrev()) * 100d / stat2.getClosePrev()));
				ret = comparte2(growth1, growth2);
			}
			else if (sort_type == 203 || sort_type == 213) ret = comparte(stat1.getAmount(), stat2.getAmount());
			else if (sort_type == 241) ret = comparte(stat1.getClosePrev(), stat1.getOpen(), stat2.getClosePrev(), stat2.getOpen());
			else if (sort_type == 242) ret = comparte(stat1.getHigh(), stat1.getPrice(), stat2.getHigh(), stat2.getPrice());
			else if (sort_type == 243) ret = comparte(stat1.getLow(), stat1.getPrice(), stat2.getLow(), stat2.getPrice());
			else if (sort_type == 244) ret = comparte(stat1.getHigh60(), stat1.getPrice(), stat2.getHigh60(), stat2.getPrice());
			else if (sort_type == 245) ret = comparte(stat1.getLow60(), stat1.getPrice(), stat2.getLow60(), stat2.getPrice());
			else if (sort_type == 331 || sort_type == 341) ret = comparte(stat1.getVolume60(), stat1.getVolume(), stat2.getVolume60(), stat2.getVolume());
			else if (sort_type == 411 || sort_type == 421) ret = comparte(stat1.getNet(), stat2.getNet());
			else if (sort_type == 412 || sort_type == 422) ret = comparte(stat1.getNetTotal(), stat2.getNetTotal());
			else if (sort_type == 413 || sort_type == 423) ret = comparte2(stat1.getGrowth(), stat2.getGrowth());
			else if (sort_type == 414 || sort_type == 424) ret = comparte2(stat2.getPremium(), stat1.getPremium());
			else if (sort_type == 415 || sort_type == 425) ret = comparte2(stat2.getPremium2(), stat1.getPremium2());
			else if (sort_type == 441 || sort_type == 451) ret = comparte2(stat2.getPremiumHigh5(), stat2.getPremium2(), stat1.getPremiumHigh5(), stat1.getPremium2());
			else if (sort_type == 442 || sort_type == 452) ret = comparte2(stat2.getPremiumLow5(), stat2.getPremium2(), stat1.getPremiumLow5(), stat1.getPremium2());
		}
		
		if (ret >1) return 1;
		else if (ret < -1) return -1;
		else if (ret == 0) return (asc ? ret2 : 0 - ret2);
		return (asc ? ret : 0 - ret);
	}
	
	// compare string field
	private static int comparte(String s1, String s2)
	{
		if (WebUtil.empty(s1) && WebUtil.empty(s2)) return 0;
		else if (WebUtil.empty(s1)) return 2;
		else if (WebUtil.empty(s2)) return -2;
		return Math.min(1, Math.max(-1, s1.compareTo(s2)));
	}
	
	// compare double field, for absolute value
	private static int comparte(double d1, double d2)
	{
		if (d1 <= 0d && d2 <= 0d) return 0;
		else if (d1 <= 0d) return 2;
		else if (d2 <= 0d) return -2;
		return (d1 == d2 ? 0 : (d1 > d2 ? 1 : -1));
	}
	
	// compare double field, for ratio
	private static int comparte2(double d1, double d2)
	{
		if (d1 < -100d && d2 < -100d) return 0;
		else if (d1 < -100d) return 2;
		else if (d2 < -100d) return -2;
		return (d1 == d2 ? 0 : (d1 > d2 ? 1 : -1));
	}
	
	// compare bias with two double fields, for absolute value
	private static int comparte(double base1, double d1, double base2, double d2)
	{
		if ((base1 <= 0d || base1 == 0d || d1 <= 0d) && (base2 <= 0d || base2 == 0d || d2 <= 0d)) return 0;
		else if (base1 <= 0d || base1 == 0d || d1 <= 0d) return 2;
		else if (base2 <= 0d || base2 == 0d || d2 <= 0d) return -2;
		double bias1 = (d1 / base1), bias2 = (d2 / base2);
		return (bias1 == bias2 ? 0 : (bias1 > bias2 ? 1 : -1));
	}
	
	// compare bias with two double fields, for ratio
	private static int comparte2(double base1, double d1, double base2, double d2)
	{
		if ((base1 < -100d || base1 == 0d || d1 < -100d) && (base2 < -100d || base2 == 0d || d2 < -100d)) return 0;
		else if (base1 < -100d || base1 == 0d || d1 < -100d) return 2;
		else if (base2 < -100d || base2 == 0d || d2 < -100d) return -2;
		double bias1 = (d1 - base1), bias2 = (d2 - base2);
		return (bias1 == bias2 ? 0 : (bias1 > bias2 ? 1 : -1));
	}

	public static void appendFundEbk(String code_b, StringBuffer sb)
	{
		TSymbol s1 = _symbols.get(code_b);
		sb.append("0").append(s1.code(1)).append(WebUtil.line());
		for (int i = 0; i < InVarAM.s_names.length; i++) if (i != 1) sb.append("0").append(s1.code(i)).append(WebUtil.line());
	}

	@SuppressWarnings("unchecked")
	public static void appendFund(int type, String code_b, XmlOutput4j xop)
	{
		String code, quote, quoteMin, net;
		TSymbol s1 = _symbols.get(code_b);
		TSymbol[] arr_s = new TSymbol[3];
		FundRtStats[] arr_stat = new FundRtStats[3];
		List<TNetDaily>[] arr_nets = new List[3];
		for (int i = 0; i < InVarAM.s_names.length; i++)
		{
			code = s1.code(i);
			arr_s[i] =  _symbols.get(code);
			arr_stat[i] = _rt_stats_daily.get(code);
			if (arr_stat[i] == null) 
			{
				// _logger.info("portal: no rt_stat_daily - " + arr_s[i].toText());
				return;
			}
			if (type == 1) arr_nets[i] = getNetDailysList(code);
		}
		
		FundRtStats stat, stat2;
		long from_m = DateUtil.str2min(InVarAM.s_trade_times[1]), to_m = DateUtil.str2min(InVarAM.s_trade_times[2]);
		long from_a = DateUtil.str2min(InVarAM.s_trade_times[3]), to_a = DateUtil.str2min(InVarAM.s_trade_times[4]);
		int min_m = (int)((to_m - from_m) / 60000L) + 1, min_a = (int)((to_a - from_a) / 60000L) + 1;
		xop.openTag("fund", InVarAM.attrs_rtstats_base, new String[]{s1.getCode(), "" + s1.getEquity(), s1.getManager(), s1.getStartDate(), s1.getEndDate()});
		for (int i = 0; i < InVarAM.s_names.length; i++)
		{
			stat = arr_stat[i];
			// "minute", "growth", "price", "closePrev", "open", "high", "low", "high60", "low60", "volume"
			double growth = (i == 2 ||  stat.getClosePrev() < 0.0001d ? 0.00d : WebUtil.d2d_00((stat.getPrice() - stat.getClosePrev()) * 100d / stat.getClosePrev()));
			quoteMin = (i == 2 ? null : stat.getDateQuote() + ", " + stat.getMinute());
			quote = (i == 2 ? null : (type == 1 ? quoteMin + ", " : "") + growth + ", " + stat.getPrice() + ", " + stat.getClosePrev() + ", " + stat.getOpen() + ", " + 
					stat.getHigh() + ", " + stat.getLow() + ", " + stat.getHigh60() + ", " + stat.getLow60() + ", " + stat.getVolume());
			// "date", "net", "netTotal", "growth", "premium", "premium2", "premiumHigh5", "premiumLow5"
			net = stat.getDate() + ", " + stat.getNet() + ", " + stat.getNetTotal() + ", " + stat.getGrowth() + ", " + stat.getPremium() + ", " + stat.getPremium2() + ", " + 
					stat.getPremiumHigh5() + ", " + stat.getPremiumLow5();
			xop.appendTag(false, "stat" + i, null, InVarAM.attrs_rtstats_detail, new String[]{arr_s[i].getCode(), arr_s[i].getName(), arr_s[i].getNameS(), "" + arr_s[i].getType(), 
					"" + arr_s[i].getEquity(), quote, quoteMin, stat.getMinute0(), net, arr_s[i].hqlist(true)});
			if (type == 1 && i > 0)
			{
				xop.openTag("net" + i, null, null);
				for (TNetDaily bean : arr_nets[i]) xop.appendTag(false, "net", null, InVarAM.attrs_net, new String[]{"" + DateUtil.date2(bean.getDate()).getTime(), 
						"" + bean.getNet(), "" + "" + bean.getGrowth(), "" + bean.getPremium(), "" + bean.getPremium2(),  "" + bean.getPremiumHigh5(), "" + bean.getPremiumLow5()});
				xop.closeTag();
				xop.openTag("netm" + i, null, null);
				for (int ii = 0; ii < min_m; ii++) 
				{
					stat2 = _rt_stats_min.get(stat.key(DateUtil.shiftMinute(ii, from_m)));
					if (stat2 != null) xop.appendTag(false, "net", null, InVarAM.attrs_net_min, new String[]{"" + DateUtil.date(stat2.getDate() + " " + stat2.getMinute0() + ":00").getTime(), 
							"" + stat2.getPremium(), "" + stat2.getPremium2(), "" + stat2.getPremiumHigh5(), "" + stat2.getPremiumLow5()});
				}
				for (int ii = 0; ii < min_a; ii++) 
				{
					stat2 = _rt_stats_min.get(stat.key(DateUtil.shiftMinute(ii, from_a)));
					if (stat2 != null) xop.appendTag(false, "net", null, InVarAM.attrs_net_min, new String[]{"" + DateUtil.date(stat2.getDate() + " " + stat2.getMinute0() + ":00").getTime(), 
							"" + stat2.getPremium(), "" + stat2.getPremium2(), "" + stat2.getPremiumHigh5(), "" + stat2.getPremiumLow5()});
				}
				xop.closeTag();
			}
		}
		xop.closeTag();
	}
	
	private static List<TNetDaily> getNetDailysList(String code)
	{
		List<TNetDaily> ret = _nets_daily_list.get(code);
		if (ret == null)
		{
			String min_date = ArmaUtil.shiftTrade(-10, _last_trade);
			ret = GlobalCache.getInstance().getBean(TFundDaoService.class).getNetDailys("date >'" + min_date + "' and code ='" + code + "' order by code, date asc");
			_nets_daily_list.put(code, ret);
		}
		return ret;
	}
	
	public static void appendSymbolDetail(String code_b, XmlOutput4j xop)
	{
		TSymbol s1 = _symbols.get(code_b), symbol;
		xop.openTag("symbols", InVarAM.attrs_rtstats_base, new String[]{s1.getCode(), null, null, null, null});
		for (int i = 0; i < InVarAM.s_names.length; i++)
		{
			symbol =  _symbols.get(s1.code(i));
			xop.appendTag(false, "symbol" + i, null, InVarAM.attrs_symbol, new String[]{symbol.getCode(), symbol.getName(), symbol.getNameS(), symbol.getMarket(), 
					"" + symbol.getType(), "" + symbol.getEquity(), symbol.getManager(), symbol.getStartDate(), symbol.getEndDate()});
		}
		xop.closeTag();
	}

	public static void updateSymbol(TSymbol bean) 
	{
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		List<TSymbol> list = dao.getSymbols("code = '" + bean.getCode() + "'");
		if (list == null || list.size() < 1)
		{
			dao.insertSymbol(bean);
			_symbols.put(bean.getCode(), bean);
			return;
		}
		TSymbol symbol = list.get(0);
		boolean change = symbol.merge(bean);
		if (change) 
		{
			dao.updateSymbol(symbol);
			_symbols.put(bean.getCode(), bean);
		}
	}
	
	public static void appendQuoteDetail(String code_b, String date, XmlOutput4j xop)
	{
		String codes = "";
		TSymbol s1 = _symbols.get(code_b);
		for (int i = 0; i < InVarAM.s_names.length; i++) codes += ", '" + s1.code(i) + "'";
		String cond = "date ='" + date + "' and code in (" + codes.substring(2) + ") order by code, date desc";
		List<TQuoteDaily> quotes_daily = GlobalCache.getInstance().getBean(TFundDaoService.class).getQuoteDailys(cond);
		xop.openTag("quotes", InVarAM.attrs_rtstats_base, new String[]{s1.getCode(), null, null, null, null});
		for (int i = 0; i < InVarAM.s_names.length; i++)
		{
			for (TQuoteDaily quote_daily : quotes_daily)
			{
				if (!s1.code(i).equals(quote_daily.getCode())) continue;
				xop.appendTag(false, "quote" + i, null, InVarAM.attrs_quoteDaily, new String[]{quote_daily.getCode(), quote_daily.getDate(), quote_daily.getTime(), 
						"" + quote_daily.getPrice(), "" + quote_daily.getClosePrev(), "" + quote_daily.getOpen(), "" + quote_daily.getHigh(), "" + quote_daily.getLow(), 
						"" + quote_daily.getVolume(), "" + quote_daily.getAmount()});
				break;
			}
		}
		xop.closeTag();
	}

	public static void updateQuoteDaily(TQuoteDaily bean) 
	{
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		List<TQuoteDaily> list = dao.getQuoteDailys("code='" + bean.getCode() + "' and date='" + bean.getDate() + "'");
		if (list == null || list.size() < 1)
		{
			dao.insertQuoteDaily(bean);
			list = new ArrayList<TQuoteDaily>();
			list.add(bean);
			normalizeQuoteDaily(list);
			return;
		}
		TQuoteDaily quote_daily = list.get(0);
		boolean change = quote_daily.merge(bean);
		if (change) 
		{
			dao.updateQuoteDaily(quote_daily);
			list.clear();
			list.add(quote_daily);
			normalizeQuoteDaily(list);
		}
	}
	
	public static void appendNetDetail(String code_b, String date, XmlOutput4j xop)
	{
		String codes = "";
		TSymbol s1 = _symbols.get(code_b);
		for (int i = 0; i < InVarAM.s_names.length; i++) codes += ", '" + s1.code(i) + "'";
		String cond = "date ='" + date + "' and code in (" + codes.substring(2) + ") order by code, date desc";
		List<TNetDaily> nets_daily = GlobalCache.getInstance().getBean(TFundDaoService.class).getNetDailys(cond);
		xop.openTag("nets", InVarAM.attrs_rtstats_base, new String[]{s1.getCode(), null, null, null, null});
		for (int i = 0; i < InVarAM.s_names.length; i++)
		{
			for (TNetDaily net_daily : nets_daily)
			{
				if (!s1.code(i).equals(net_daily.getCode())) continue;
				xop.appendTag(false, "net" + i, null, InVarAM.attrs_netDaily, new String[]{net_daily.getCode(), net_daily.getDate(), "" + net_daily.getNet(), 
						"" + net_daily.getNetTotal(), "" + net_daily.getGrowth()});
				break;
			}
		}
		xop.closeTag();
	}

	public static void updateNetDaily(TNetDaily bean) 
	{
		TFundDaoService dao = GlobalCache.getInstance().getBean(TFundDaoService.class);
		List<TNetDaily> list = dao.getNetDailys("code='" + bean.getCode() + "' and date='" + bean.getDate() + "'");
		if (list == null || list.size() < 1)
		{
			dao.insertNetDaily(bean);
			list = new ArrayList<TNetDaily>();
			list.add(bean);
			normalizeNetDaily(list);
			return;
		}
		TNetDaily net_daily = list.get(0);
		boolean change = net_daily.merge(bean);
		if (change) 
		{
			dao.updateNetDaily(net_daily);
			list.clear();
			list.add(net_daily);
			normalizeNetDaily(list);
		}
	}
}
