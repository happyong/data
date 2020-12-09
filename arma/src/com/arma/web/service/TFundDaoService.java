/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arma.web.service.bean.TNetDaily;
import com.arma.web.service.bean.TQuoteDaily;
import com.arma.web.service.bean.TSymbol;
import com.neulion.iptv.web.service.BaseDaoService;
import com.neulion.iptv.web.util.WebUtil;

public class TFundDaoService extends BaseDaoService
{
    // code, name, nameS, market, type, equity, manager, startDate, endDate, options
    public boolean insertSymbol(TSymbol bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertSymbols(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertSymbols(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_symbol, params);
        return (rets != null);
    }

    private static final String insert_t_symbol = "insert into t_symbol (code, name, name_s, market, type, equity, manager, start_date, end_date, options) values "
            + "(:code, :name, :nameS, :market, :type, :equity, :manager, :startDate, :endDate, :options) ";

    // code, name, nameS, market, type, equity, manager, startDate, endDate, options
    public boolean updateSymbol(TSymbol bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateSymbols(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateSymbols(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_symbol, params);
        return (rets != null);
    }

    private static final String update_t_symbol = "update t_symbol set name=:name, name_s=:nameS, market=:market, type=:type, equity=:equity, manager=:manager, "
            + "start_date=:startDate, end_date=:endDate, options=:options where code=:code ";

    public boolean deleteSymbol(String code)
    {
        if (WebUtil.empty(code))
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", code);
        return deleteSymbols(WebUtil.params(map));
    }

    public boolean deleteSymbols(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_symbol, params);
        return (rets != null);
    }

    private static final String delete_t_symbol = "delete from t_symbol where code=:code ";

    // code, name, nameS, market, type, manager, startDate, endDate, options
    public List<TSymbol> getSymbols(String cond)
    {
        List<TSymbol> list = new ArrayList<TSymbol>();
        String sql = select_t_symbol;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TSymbol().fromDbMap(TSymbol.class, result));
        return list;
    }

    private static final String select_t_symbol = "select id as id, code as code, name as name, name_s as nameS, market as market, type as type, equity as equity, "
            + "manager as manager, start_date as startDate, end_date as endDate, options as options from t_symbol where ${cond} ";

    // code, date, time, price, closePrev, open, high, low, volume, amount, high60, low60, volume60
    public boolean insertQuoteDaily(TQuoteDaily bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertQuoteDailys(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertQuoteDailys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_quote_daily, params);
        return (rets != null);
    }

    private static final String insert_t_quote_daily =
            "insert into t_quote_daily (code, date, time, price, close_prev, open, high, low, volume, amount, high_60, low_60, volume_60) values "
                    + "(:code, :date, :time, :price, :closePrev, :open, :high, :low, :volume, :amount, :high60, :low60, :volume60) ";

    // code, date, time, price, closePrev, open, high, low, volume, amount, high60, low60, volume60
    public boolean updateQuoteDaily(TQuoteDaily bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateQuoteDailys(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateQuoteDailys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_quote_daily, params);
        return (rets != null);
    }

    private static final String update_t_quote_daily = "update t_quote_daily set time=:time, price=:price, close_prev=:closePrev, open=:open, high=:high, low=:low, "
            + "volume=:volume, amount=:amount, high_60=:high60, low_60=:low60, volume_60=:volume60 where code=:code and date=:date ";

    public boolean deleteQuoteDaily(String code, String date)
    {
        if (WebUtil.empty(code) || WebUtil.empty(date))
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", code);
        map.put("date", date);
        return deleteQuoteDailys(WebUtil.params(map));
    }

    public boolean deleteQuoteDailys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_quote_daily, params);
        return (rets != null);
    }

    private static final String delete_t_quote_daily = "delete from t_quote_daily where code=:code and date=:date ";

    // code, date, open, high, low, close, volume, amount, high60, low60, volume60
    public List<TQuoteDaily> getQuoteDailys(String cond)
    {
        List<TQuoteDaily> list = new ArrayList<TQuoteDaily>();
        String sql = select_t_quote_daily;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TQuoteDaily().fromDbMap(TQuoteDaily.class, result));
        return list;
    }

    private static final String select_t_quote_daily = "select id as id, code as code, date as date, time as time, price as price, close_prev as closePrev, open as open, "
            + "high as high, low as low, volume as volume, amount as amount, high_60 as high60, low_60 as low60, volume_60 as volume60 from t_quote_daily where ${cond} ";

    // code, date, net, netTotal, growth, premium, premium2, premiumHigh5, premiumLow5
    public boolean insertNetDaily(TNetDaily bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertNetDailys(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertNetDailys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_net_daily, params);
        return (rets != null);
    }

    private static final String insert_t_net_daily = "insert into t_net_daily (code, date, net, net_total, growth, premium, premium2, premium_high_5, premium_low_5) values "
            + "(:code, :date, :net, :netTotal, :growth, :premium, :premium2, :premiumHigh5, :premiumLow5) ";

    // code, date, net, netTotal, growth, premium, premium2, premiumHigh5, premiumLow5
    public boolean updateNetDaily(TNetDaily bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateNetDailys(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateNetDailys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_net_daily, params);
        return (rets != null);
    }

    private static final String update_t_net_daily = "update t_net_daily set net=:net, net_total=:netTotal, growth=:growth, premium=:premium, premium2=:premium2, "
            + "premium_high_5=:premiumHigh5, premium_low_5=:premiumLow5 where code=:code and date=:date ";

    public boolean deleteNetDaily(String code, String date)
    {
        if (WebUtil.empty(code) || WebUtil.empty(date))
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", code);
        map.put("date", date);
        return deleteNetDailys(WebUtil.params(map));
    }

    public boolean deleteNetDailys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_net_daily, params);
        return (rets != null);
    }

    private static final String delete_t_net_daily = "delete from t_net_daily where code=:code and date=:date ";

    // code, date, net, netTotal, growth, premium, premiumHigh5, premiumLow5
    public List<TNetDaily> getNetDailys(String cond)
    {
        List<TNetDaily> list = new ArrayList<TNetDaily>();
        String sql = select_t_net_daily;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TNetDaily().fromDbMap(TNetDaily.class, result));
        return list;
    }

    private static final String select_t_net_daily = "select id as id, code as code, date as date, net as net, net_total as netTotal, growth as growth, "
            + "premium as premium, premium2 as premium2, premium_high_5 as premiumHigh5, premium_low_5 as premiumLow5 from t_net_daily where ${cond} ";
}
