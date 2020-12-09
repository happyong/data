/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

public class InVarAM
{
    // 显示聚焦基金列表信息与否，以及其它调试信息
    public static final boolean b_show_info = false;
    // 对于非交易时间，扫描实时统计信息与否
    public static final boolean _b_rt_stats_anytime = false;
    // 扫描基金列表与否
    public static final boolean _b_scan_symbols_setup = false;
    // 三月最高统计指标的回溯天数
    public static final int i_quote_stats = 60;
    // 五日最高溢价率统计指标的回溯天数
    public static final int i_net_stats = 5;
    // 分级基金A的类型值
    public static final int i_fund_type = 310;
    // 交易时间点
    public static final String[] s_trade_times = new String[] { "09:15", "09:30", "11:30", "13:00", "15:00", "15:05", "15:15" };
    // 交易日历, 除正常周末外的其它休市日列表
    // 元旦, 春节*, 清明节*, 劳动节*, 端午节, 中秋节, 国庆节*
    public static final String[][] s_trading_close = { { "2014-01-01" }, { "2014-01-31", "2014-02-06" }, { "2014-04-05", "2014-04-07" }, { "2014-05-01", "2014-05-03" },
            { "2014-05-31", "2014-06-02" }, { "2014-09-06", "2014-09-08" }, { "2014-10-01", "2014-10-07" }, { "2015-01-01", "2015-01-03" }, { "2015-02-18", "2015-02-24" },
            { "2015-04-04", "2015-04-06" }, { "2015-05-01", "2015-05-03" }, { "2015-06-20", "2015-06-22" }, { "2015-09-03", "2015-09-04" }, { "2015-09-26", "2015-09-27" },
            { "2015-10-01", "2015-10-07" } };
    public static final String[] s_names = new String[] { "codeA", "codeB", "codeM" };
    // 特别聚焦的基金列表, 可用分级基金ABM的代码/名称/名称缩写来表示
    // "一带一B", "地产B", "国防B", "国企改B", "互联网B", "创业板B", "传媒B", "高铁B"
    // "150276", "150193", "150206", "150210", "150195", "150153", "150204", "150278"
    public static String[] s_fund_focus = new String[] { "一带一B", "地产B", "国防B", "国企改B", "互联网B", "创业板B", "传媒B", "高铁B" };
    // 白名单, 分级基金AB的代码列表或范围
    public static final String[][] s_fund_include = { { "150117" }, { "150130" }, { "150148" }, { "150150" }, { "150152" }, { "150157" }, { "150171", "150999" } };
    // 黑名单, 分级基金AB的代码列表或范围
    public static final String[][] s_fund_exclude = { { "150175" }, { "150263" } };
    public static final String s_fund_ebk = "5分级股.EBK";
    /*
     * // http://hq.sinajs.cn/rn=fgu87&list=s_sh000001,s_sz399001,s_sz399003,s_sz399006,int_hangseng,int_dji,int_nasdaq,int_sp500,int_ftse,int_nikkei var
     * hq_str_s_sh000001="上证指数,5166.350,44.757,0.87,6256278,106016743.62601"; var hq_str_s_sz399001="深证成指,18098.274,208.585,1.17,385071961,90484463"; var
     * hq_str_s_sz399003="成份Ｂ指,9093.353,2.165,0.02,173643,16565"; var hq_str_s_sz399006="创业板指,3899.705,37.567,0.97,17990346,7721965"; var
     * hq_str_int_hangseng="恒生指数,27280.54,372.69,1.39"; var hq_str_int_dji="道琼斯,17898.84,-140.53,-0.78"; var hq_str_int_nasdaq="纳斯达克,5051.10,-31.41,-0.62"; var
     * hq_str_int_sp500="标普指数,2094.11,-14.75,-0.70"; var hq_str_int_ftse="伦敦指数,6784.92,-61.82,-0.90"; var hq_str_int_nikkei="日经指数,20407.08,24.11,0.12";
     * 
     * // http://hq.sinajs.cn/rn=1318986550609&amp;list=hf_CL,hf_GC,hf_SI,hf_CAD,hf_ZSD,hf_S,hf_C,hf_W var
     * hq_str_hf_CL="59.94,-1.3658,59.92,60.01,60.63,59.73,05:14:56,60.77,60.56,22453,0,0,2015-06-13,NYMEX原油"; var
     * hq_str_hf_GC="1180.8,0.0339,1180.3,1181.2,1183.7,1175.6,05:14:57,1180.4,1180.8,9938,0,0,2015-06-13,COMEX黄金"; var
     * hq_str_hf_SI="15.920,-0.2506,15.900,15.950,16.000,15.780,05:14:48,15.960,15.990,1612,0,0,2015-06-13,COMEX白银"; var
     * hq_str_hf_CAD="5915.00,0.5952,5913.50,5916.00,5916.00,5858.50,01:58:22,5880.00,5888.00,3770,0,0,2015-06-13,LME铜"; var
     * hq_str_hf_ZSD="2123.00,0.1888,2126.00,2129.50,2133.00,2095.00,03:00:00,2119.00,2126.00,1758,0,0,2015-06-13,LME锌"; var
     * hq_str_hf_S="940.00,0.0000,940.00,940.00,947.00,933.25,02:30:25,940.00,939.50,3277,0,0,2015-06-13,CBOT黄豆"; var
     * hq_str_hf_C="353.00,-0.9818,352.25,352.25,356.75,352.25,02:21:59,356.50,356.00,2299,0,0,2015-06-13,CBOT玉米"; var
     * hq_str_hf_W="503.75,-0.0992,504.00,505.75,510.75,500.50,02:30:28,504.25,503.75,2553,0,0,2015-06-13,CBOT小麦";
     * 
     * // http://hq.sinajs.cn/rn=1318986628214&list=USDCNY,USDHKD,EURCNY,GBPCNY,USDJPY,EURUSD,GBPUSD var
     * hq_str_USDCNY="16:30:09,6.2083,6.2084,6.2066,26,6.2067,6.2085,6.2059,6.2083,美元人民币"; var hq_str_USDHKD="05:20:10,7.7524,7.7534,7.7524,21,7.7524,7.7537,7.7516,7.7524,美元港元";
     * var hq_str_EURCNY="05:59:58,6.9902,6.9927,6.9809,792,6.9771,7.0063,6.9271,6.9902,欧元人民币"; var
     * hq_str_GBPCNY="05:59:58,9.6629,9.6654,9.6287,738,9.6299,9.6766,9.6028,9.6629,英镑人民币"; var hq_str_USDJPY="05:02:25,123.41,123.43,123.49,7000,123.48,123.82,123.12,123.41,美元日元";
     * var hq_str_EURUSD="05:03:08,1.1257,1.1265,1.1239,146,1.1238,1.1296,1.1150,1.1257,欧元美元"; var
     * hq_str_GBPUSD="05:03:31,1.5565,1.5568,1.5510,132,1.5511,1.5598,1.5466,1.5565,英镑美元";
     * 
     * // http://hq.sinajs.cn/rn=fgu87&list=f_150265,sz150265,f_150266,sz150266,f_168201 var hq_str_f_150265="中融一带一路分级A,1.005,1.005,1.005,2015-06-12,9.87814"; var
     * hq_str_sz150265="一带A,0.906,0.907,0.909,0.912,0.906,0.909,0.910,70646320,64248520.137,819799,0.909,253095,0.908,6623431,0.907,12772552,0.906,42900,0.905,1594419,
     * 0.910,1315495,0.911,3328240,0.912,579900,0.913,4049500,0.914,2015-06-12,15:05:37,00"; var hq_str_f_150266="中融一带一路分级B,1.144,1.144,1.135,2015-06-12,9.87814"; var
     * hq_str_sz150266="一带B,1.235,1.236,1.234,1.244,1.216,1.234,1.235,294243738,361745107.354,581400,1.234,407972,1.233,502800,1.232,993365,1.231,879100,1.230,
     * 1222709,1.235,1059800,1.236,1193800,1.237,1933171,1.238,1485756,1.239,2015-06-12,15:05:37,00"; var hq_str_f_168201="中融一带一路分级,1.074,1.074,1.07,2015-06-12,0.924475";
     * 0："中融一带一路分级B", 基金名称 1："1.144″, 净值 2："1.144″, 累计净值 3："1.135″, 昨日净值 4："2015-06-12″, 当前日期 5："9.87814″, 溢价率
     * 
     * 0："一带B", 股票名称 1："1.235″, 今开 2："1.236″, 昨收 3："1.234″, 市价或今收 4："1.244″, 最高价 5："1.216″, 最低价 6："1.234″, 竞买价, 即"买一"报价 7："1.235″, 竞卖价, 即"卖一"报价 8："294243738″, 成交的股票数,
     * 由于股票交易以一百股为基本单位, 通常把该值除以一百 9："361745107.354″, 成交金额, 单位为"元", 通常以"万元"为成交金额的单位, 所以通常把该值除以一万 10："581400″, "买一"申请581400股, 即5814手 11："1.234″, "买一"报价 (12, 13), (14, 15), (16,17),
     * (18, 19)分别为"买二"至"买五"的情况 20："1222709″, "卖一"申报1222709股, 即12227手 21："1.235″, "卖一"报价 (22, 23), (24, 25), (26,27), (28, 29)分别为"卖二"至"卖五"的情况 30："2015-06-12″, 当前日期 31："15:05:37,00″,
     * 当前时间
     */
    public static final String s_hq_js = "http://hq.sinajs.cn/rn={rn}&list={list}";
    public static final String s_fund_iframe_js = "http://finance.sina.com.cn/iframe/286/20091218/26.js?rn={rn}";
    private static final String s_fund_net_base = "http://vip.stock.finance.sina.com.cn/fund_center/data/";
    /*
     * <item> <symbol>168201</symbol> <sname>中融一带一路分级</sname> <per_nav>1.0740</per_nav> <total_nav>1.0740</total_nav> <yesterday_nav>1.07</yesterday_nav>
     * <nav_rate>0.3738</nav_rate> <nav_a>0.004</nav_a> <sg_states>开放</sg_states> <nav_date>2015-06-12</nav_date> <fund_manager>赵菲</fund_manager> <jjlx>偏股型基金</jjlx>
     * <jjzfe>92447500</jjzfe> </item>
     */
    // 名称, 基金经理, 净值, 累计净值, 昨日净值, 净值增长率, 当前日期
    public static final String s_fund_net_open_json = s_fund_net_base + "json.php/NetValue_Service.getNetValueOpen?rn={rn}&page=1&num={num}&"
            + "sort=symbol&asc=1&ccode=&type2=2&type3=8";
    public static final String s_fund_net_open_xml = s_fund_net_base + "xml.php/NetValue_Service.getNetValueOpen?rn={rn}&page=1&num={num}&"
            + "sort=symbol&asc=1&ccode=&type2=2&type3=8";
    public static final String s_fund_net_open2_json = s_fund_net_base + "json.php/NetValue_Service.getNetValueOpen?rn={rn}&page=1&num={num}&" + "sort=symbol&asc=1&ccode=&type2=2";
    public static final String s_fund_net_open2_xml = s_fund_net_base + "xml.php/NetValue_Service.getNetValueOpen?rn={rn}&page=1&num={num}&" + "sort=symbol&asc=1&ccode=&type2=2";
    /*
     * <item> <symbol>150266</symbol> <sname>中融一带一路分级B</sname> <per_nav>1.1440</per_nav> <nav_date>2015-06-12</nav_date> <total_nav>1.1440</total_nav> <nav_rate>0.793</nav_rate>
     * <discount_rate>7.86713</discount_rate> <start_date>2015-05-14</start_date> <end_date/> <fund_manager>赵菲</fund_manager> <jjlx>偏股型基金</jjlx> <zjzfe>206808</zjzfe> </item>
     */
    // 名称, 基金经理, 上市日, 退市日, 净值, 累计净值, 净值增长率, 当前日期, 溢价率
    public static final String s_fund_net_cx_json = s_fund_net_base + "json.php/NetValue_Service.getNetValueCX?rn={rn}&page=1&num={num}&" + "sort=symbol&asc=1&ccode=&type2=0";
    public static final String s_fund_net_cx_xml = s_fund_net_base + "xml.php/NetValue_Service.getNetValueCX?rn={rn}&page=1&num={num}&" + "sort=symbol&asc=1&ccode=&type2=0";
    // http://image.sinajs.cn/newchart/min/n/sz150266.gif
    // type - min, daily, weekly, monthly
    public static final String s_chart_img = "http://image.sinajs.cn/newchart/{type}/n/{market}{symbol}.gif";

    public static final String[] attrs_count = new String[] { "count" };
    public static final String[] attrs_rtstats_base = new String[] { "code", "e", "m", "sd", "ed" };
    public static final String[] attrs_rtstats_detail = new String[] { "code", "n", "n2", "t", "e", "q", "qm", "minute0", "net", "code2" };
    public static final String[] attrs_net = new String[] { "t", "net", "g", "p", "p2", "ph5", "pl5" };
    public static final String[] attrs_net_min = new String[] { "t", "p", "p2", "ph5", "pl5" };
    public static final String[] attrs_symbol = new String[] { "code", "name", "nameS", "market", "type", "equity", "manager", "startDate", "endDate" };
    public static final String[] attrs_quoteDaily = new String[] { "code", "date", "time", "price", "closePrev", "open", "high", "low", "volume", "amount" };
    public static final String[] attrs_netDaily = new String[] { "code", "date", "net", "netTotal", "growth" };

    public static final String s_sep1 = "、";
    public static final String s_sep2 = "(";
    public static final String s_sep3 = ", ";
    public static final String s_sep4 = "#";
    public static final String s_sep5 = "　";

    public static final String[] s_coasts = new String[] { "海警", "海监", "渔政" };
    public static final String[][] s_rockets = { { "长征一号", "长征二号", "长征二号甲" }, { "长征二号丙", "长征二号丁", "长征三号", "长征二号捆" },
            { "长征二号F", "长征三号甲", "长征三号乙", "长征三号丙", "长征四号甲", "长征四号乙", "长征四号丙" }, { "长征五号", "长征六号", "长征七号", "长征八号", "长征十一号" } };

    public static final String[] s_fleet_seps = new String[] { "", "，", "-", "-", "", "" };
    public static final String[] s_fleet_tabs = new String[] { "num", "fleet，addr", "date1-date4", "date2-date3", "list", "info" };
    public static final String s_fleet_tag = "索马里护航";
    public static final String s_fleet_key = "护航编队#";
    public static final String s_fleet_val = s_fleet_key + "{num00}，{date1}，中国海军第{num}批护航编队从{fleet}驻{addr}某军港解缆起航，执行护航任务。{date2}护航开始，{date3}护航结束，{date4}归港。护航编队由{list}组成。{info}";

    public static final String[] s_vote_seps = new String[] { "】", "摇摆州", "超级", "重要", "；" };
    public static final String[] s_vote_cands = new String[] { "特朗普", "希拉里" };
    public static final String s_vote_key = "党{cand}赢得{name1}({name2})共{count}张";
    public static final String s_vote_val = "    {no}\t{name1}\t\t{name2}\t\t{count}\t{cand}\t\t{v1}-{v2}\t\t东部07:00开票，19:00闭票，北京{time}结果{info}";

    public static final String key_blank_gb2312 = "　";
    public static final String dir_text_clx = "D:/Data/诗文/手机读本/古龙全集/楚留香系列";
    public static final String dir_naval_usa = "D:/Data/生活/maps/militray/美军动向/";
    public static final String url_naval_usa = "https://www.stratfor.com/sites/default/files/naval-update-11142018.png";
    public static final String key_user_agent = "User-Agent";
    public static final String def_user_agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    public static boolean hitclx1(String line)
    {
        return (line.startsWith("第") && (line.indexOf("章") > 0 || line.endsWith("部"))) || "楔　子".equals(line);
    }
}
