/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service.bean;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Node;

import com.arma.web.servlets.kms.InVarKM;
import com.neulion.iptv.web.service.BaseDaoBean;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class TFinance extends BaseDaoBean
{
    private int id;
    private int year;
    private double gdp;
    private int population;
    private int agdp;
    private double outlaysTotal;
    private double outlaysCentral;
    private double defenseTotal;
    private double defenseCentral;
    private double usaGdp;
    private int usaPopulation;
    private int usaAgdp;
    private double usaOutlaysTotal;
    private double usaDefenseTotal;
    private double exchangeUsd;

    @Override
    public Map<String, Object> toDbMap()
    {
        Map<String, Object> dbmap = new HashMap<String, Object>();
        dbmap.put("year", year);
        dbmap.put("gdp", gdp);
        dbmap.put("population", population);
        dbmap.put("agdp", agdp);
        dbmap.put("outlaysTotal", outlaysTotal);
        dbmap.put("outlaysCentral", outlaysCentral);
        dbmap.put("defenseTotal", defenseTotal);
        dbmap.put("defenseCentral", defenseCentral);
        dbmap.put("usaGdp", usaGdp);
        dbmap.put("usaPopulation", usaPopulation);
        dbmap.put("usaAgdp", usaAgdp);
        dbmap.put("usaOutlaysTotal", usaOutlaysTotal);
        dbmap.put("usaDefenseTotal", usaDefenseTotal);
        dbmap.put("exchangeUsd", exchangeUsd);
        return dbmap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
    {
        if (dbmap == null || dbmap.size() == 0)
            return (T) this;
        id = WebUtil.obj2int(dbmap.get("id"));
        year = WebUtil.obj2int(dbmap.get("year"));
        gdp = WebUtil.obj2double(dbmap.get("gdp"));
        population = WebUtil.obj2int(dbmap.get("population"));
        agdp = WebUtil.obj2int(dbmap.get("agdp"));
        outlaysTotal = WebUtil.obj2double(dbmap.get("outlaysTotal"));
        outlaysCentral = WebUtil.obj2double(dbmap.get("outlaysCentral"));
        defenseTotal = WebUtil.obj2double(dbmap.get("defenseTotal"));
        defenseCentral = WebUtil.obj2double(dbmap.get("defenseCentral"));
        usaGdp = WebUtil.obj2double(dbmap.get("usaGdp"));
        usaPopulation = WebUtil.obj2int(dbmap.get("usaPopulation"));
        usaAgdp = WebUtil.obj2int(dbmap.get("usaAgdp"));
        usaOutlaysTotal = WebUtil.obj2double(dbmap.get("usaOutlaysTotal"));
        usaDefenseTotal = WebUtil.obj2double(dbmap.get("usaDefenseTotal"));
        exchangeUsd = WebUtil.obj2double(dbmap.get("exchangeUsd"));
        return (T) this;
    }

    public TFinance fromRequest(Node node, HttpServletRequest request)
    {
        year = WebUtil.obj2int(WebUtil.scan_str("year", node, request));
        gdp = WebUtil.obj2double(WebUtil.scan_str("gdp", node, request));
        population = WebUtil.obj2int(WebUtil.scan_str("population", node, request));
        agdp = WebUtil.obj2int(WebUtil.scan_str("agdp", node, request));
        outlaysTotal = WebUtil.obj2double(WebUtil.scan_str("outlays_total", node, request));
        outlaysCentral = WebUtil.obj2double(WebUtil.scan_str("outlays_central", node, request));
        defenseTotal = WebUtil.obj2double(WebUtil.scan_str("defense_total", node, request));
        defenseCentral = WebUtil.obj2double(WebUtil.scan_str("defense_central", node, request));
        usaGdp = WebUtil.obj2double(WebUtil.scan_str("usa_gdp", node, request));
        usaPopulation = WebUtil.obj2int(WebUtil.scan_str("usa_population", node, request));
        usaAgdp = WebUtil.obj2int(WebUtil.scan_str("usa_agdp", node, request));
        usaOutlaysTotal = WebUtil.obj2double(WebUtil.scan_str("usa_outlays_total", node, request));
        usaDefenseTotal = WebUtil.obj2double(WebUtil.scan_str("usa_defense_total", node, request));
        exchangeUsd = WebUtil.obj2double(WebUtil.scan_str("exchange_usd", node, request));
        return this;
    }

    public TFinance copyit()
    {
        TFinance bean = new TFinance();
        bean.setId(getId());
        bean.setYear(getYear());
        bean.setGdp(getGdp());
        bean.setPopulation(getPopulation());
        bean.setAgdp(getAgdp());
        bean.setOutlaysTotal(getOutlaysTotal());
        bean.setOutlaysCentral(getOutlaysCentral());
        bean.setDefenseTotal(getDefenseTotal());
        bean.setDefenseCentral(getDefenseCentral());
        bean.setUsaGdp(getUsaGdp());
        bean.setUsaPopulation(getUsaPopulation());
        bean.setUsaAgdp(getUsaAgdp());
        bean.setUsaOutlaysTotal(getUsaOutlaysTotal());
        bean.setUsaDefenseTotal(getUsaDefenseTotal());
        bean.setExchangeUsd(getExchangeUsd());
        return bean;
    }

    @Override
    public String toText()
    {
        return (id + "|" + year + "|" + WebUtil.d2s(2, gdp) + "|" + population + "|" + agdp + "|" + WebUtil.d2s(2, outlaysTotal) + "|" + WebUtil.d2s(2, outlaysCentral) + "|"
                + WebUtil.d2s(2, defenseTotal) + "|" + WebUtil.d2s(2, defenseCentral) + "|" + WebUtil.d2s(2, usaGdp) + "|" + usaPopulation + "|" + usaAgdp + "|"
                + WebUtil.d2s(2, usaOutlaysTotal) + "|" + WebUtil.d2s(2, usaDefenseTotal) + "|" + WebUtil.d2s(4, exchangeUsd));
    }

    public boolean empty()
    {
        return id < 1;
    }

    public void append(boolean close, XmlOutput4j xop)
    {
        xop.openTag(
                "finance",
                InVarKM.attrs_finance,
                new String[] { "" + getId(), "" + getYear(), WebUtil.d2s(2, getGdp()), "" + getPopulation(), "" + getAgdp(), WebUtil.d2s(2, getOutlaysTotal()),
                        WebUtil.d2s(2, getOutlaysCentral()), WebUtil.d2s(2, getDefenseTotal()), WebUtil.d2s(2, getDefenseCentral()), WebUtil.d2s(2, getUsaGdp()),
                        "" + getUsaPopulation(), "" + getUsaAgdp(), WebUtil.d2s(2, getUsaOutlaysTotal()), WebUtil.d2s(2, getUsaDefenseTotal()), WebUtil.d2s(4, getExchangeUsd()) });
        if (close)
            xop.closeTag();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public double getGdp()
    {
        return gdp;
    }

    public void setGdp(double gdp)
    {
        this.gdp = gdp;
    }

    public int getPopulation()
    {
        return population;
    }

    public void setPopulation(int population)
    {
        this.population = population;
    }

    public int getAgdp()
    {
        return agdp;
    }

    public void setAgdp(int agdp)
    {
        this.agdp = agdp;
    }

    public double getOutlaysTotal()
    {
        return outlaysTotal;
    }

    public void setOutlaysTotal(double outlaysTotal)
    {
        this.outlaysTotal = outlaysTotal;
    }

    public double getOutlaysCentral()
    {
        return outlaysCentral;
    }

    public void setOutlaysCentral(double outlaysCentral)
    {
        this.outlaysCentral = outlaysCentral;
    }

    public double getDefenseTotal()
    {
        return defenseTotal;
    }

    public void setDefenseTotal(double defenseTotal)
    {
        this.defenseTotal = defenseTotal;
    }

    public double getDefenseCentral()
    {
        return defenseCentral;
    }

    public void setDefenseCentral(double defenseCentral)
    {
        this.defenseCentral = defenseCentral;
    }

    public double getUsaGdp()
    {
        return usaGdp;
    }

    public void setUsaGdp(double usaGdp)
    {
        this.usaGdp = usaGdp;
    }

    public int getUsaPopulation()
    {
        return usaPopulation;
    }

    public void setUsaPopulation(int usaPopulation)
    {
        this.usaPopulation = usaPopulation;
    }

    public int getUsaAgdp()
    {
        return usaAgdp;
    }

    public void setUsaAgdp(int usaAgdp)
    {
        this.usaAgdp = usaAgdp;
    }

    public double getUsaOutlaysTotal()
    {
        return usaOutlaysTotal;
    }

    public void setUsaOutlaysTotal(double usaOutlaysTotal)
    {
        this.usaOutlaysTotal = usaOutlaysTotal;
    }

    public double getUsaDefenseTotal()
    {
        return usaDefenseTotal;
    }

    public void setUsaDefenseTotal(double usaDefenseTotal)
    {
        this.usaDefenseTotal = usaDefenseTotal;
    }

    public double getExchangeUsd()
    {
        return exchangeUsd;
    }

    public void setExchangeUsd(double exchangeUsd)
    {
        this.exchangeUsd = exchangeUsd;
    }
}
