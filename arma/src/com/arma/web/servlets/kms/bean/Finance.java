/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms.bean;

import com.arma.web.service.bean.TFinance;
import com.neulion.iptv.web.util.WebUtil;

public class Finance
{
    public double gdpUsd = -1001.00;
    public int agdpUsd = -1001;
    public double outlaysTotalUsd = -1001.00;
    public double defenseTotalUsd = -1001.00;

    public double gdpUsRatio = -1001.00;
    public double populationUsRatio = -1001.00;
    public double agdpUsRatio = -1001.00;
    public double outlaysTotalUsRatio = -1001.00;
    public double outlaysTotalGdpRatio = -1001.00;
    public double defenseTotalUsRatio = -1001.00;
    public double defenseTotalOutlaysRatio = -1001.00;
    public double defenseTotalGdpRatio = -1001.00;
    public double usaOutlaysTotalGdpRatio = -1001.00;
    public double usaDefenseTotalOutlaysRatio = -1001.00;
    public double usaDefenseTotalGdpRatio = -1001.00;

    public double gdpGrowth = -1001.00;
    public double gdpUsdGrowth = -1001.00;
    public double populationGrowth = -1001.00;
    public double agdpGrowth = -1001.00;
    public double agdpUsdGrowth = -1001.00;
    public double outlaysTotalGrowth = -1001.00;
    public double outlaysTotalUsdGrowth = -1001.00;
    public double outlaysCentralGrowth = -1001.00;
    public double defenseTotalGrowth = -1001.00;
    public double defenseTotalUsdGrowth = -1001.00;
    public double defenseCentralGrowth = -1001.00;
    public double usaGdpGrowth = -1001.00;
    public double usaPopulationGrowth = -1001.00;
    public double usaAgdpGrowth = -1001.00;
    public double usaOutlaysTotalGrowth = -1001.00;
    public double usaDefenseTotalGrowth = -1001.00;
    public double exchangeUsdGrowth = -1001.00;

    public TFinance tfinance;

    public Finance(TFinance tfinance)
    {
        this.tfinance = tfinance;
    }

    public void stat(Finance last)
    {
        double usd = tfinance.getExchangeUsd();
        gdpUsd = usd(tfinance.getGdp(), usd);
        agdpUsd = (int) usd(tfinance.getAgdp(), usd);
        outlaysTotalUsd = usd(tfinance.getOutlaysTotal(), usd);
        defenseTotalUsd = usd(tfinance.getDefenseTotal(), usd);

        gdpUsRatio = ratio(gdpUsd, tfinance.getUsaGdp());
        populationUsRatio = ratio(tfinance.getPopulation(), tfinance.getUsaPopulation());
        agdpUsRatio = ratio(agdpUsd, tfinance.getUsaAgdp());
        outlaysTotalUsRatio = ratio(outlaysTotalUsd, tfinance.getUsaOutlaysTotal());
        outlaysTotalGdpRatio = ratio(tfinance.getOutlaysTotal(), tfinance.getGdp());
        defenseTotalUsRatio = ratio(defenseTotalUsd, tfinance.getUsaDefenseTotal());
        defenseTotalOutlaysRatio = ratio(tfinance.getDefenseTotal(), tfinance.getOutlaysTotal());
        defenseTotalGdpRatio = ratio(tfinance.getDefenseTotal(), tfinance.getGdp());
        usaOutlaysTotalGdpRatio = ratio(tfinance.getUsaOutlaysTotal(), tfinance.getUsaGdp());
        usaDefenseTotalOutlaysRatio = ratio(tfinance.getUsaDefenseTotal(), tfinance.getUsaOutlaysTotal());
        usaDefenseTotalGdpRatio = ratio(tfinance.getUsaDefenseTotal(), tfinance.getUsaGdp());

        if (last == null)
            return;
        gdpGrowth = growth(tfinance.getGdp(), last.tfinance.getGdp());
        gdpUsdGrowth = growth(gdpUsd, last.gdpUsd);
        populationGrowth = growth(tfinance.getPopulation(), last.tfinance.getPopulation());
        agdpGrowth = growth(tfinance.getAgdp(), last.tfinance.getAgdp());
        agdpUsdGrowth = growth(agdpUsd, last.agdpUsd);
        outlaysTotalGrowth = growth(tfinance.getOutlaysTotal(), last.tfinance.getOutlaysTotal());
        outlaysTotalUsdGrowth = growth(outlaysTotalUsd, last.outlaysTotalUsd);
        outlaysCentralGrowth = growth(tfinance.getOutlaysCentral(), last.tfinance.getOutlaysCentral());
        defenseTotalGrowth = growth(tfinance.getDefenseTotal(), last.tfinance.getDefenseTotal());
        defenseTotalUsdGrowth = growth(defenseTotalUsd, last.defenseTotalUsd);
        defenseCentralGrowth = growth(tfinance.getDefenseCentral(), last.tfinance.getDefenseCentral());
        usaGdpGrowth = growth(tfinance.getUsaGdp(), last.tfinance.getUsaGdp());
        usaPopulationGrowth = growth(tfinance.getUsaPopulation(), last.tfinance.getUsaPopulation());
        usaAgdpGrowth = growth(tfinance.getUsaAgdp(), last.tfinance.getUsaAgdp());
        usaOutlaysTotalGrowth = growth(tfinance.getUsaOutlaysTotal(), last.tfinance.getUsaOutlaysTotal());
        usaDefenseTotalGrowth = growth(tfinance.getUsaDefenseTotal(), last.tfinance.getUsaDefenseTotal());
        exchangeUsdGrowth = growth(tfinance.getExchangeUsd(), last.tfinance.getExchangeUsd());
    }

    private double usd(double d, double usd)
    {
        if (usd <= 0.0001 || d < 1)
            return -1001;
        return (d / usd);
    }

    private double ratio(double d, double base)
    {
        if (d < 1 || base < 1)
            return -1001;
        return (d * 100 / base);
    }

    private double growth(double d, double base)
    {
        if (d < 1 || base < 1)
            return -1001;
        return ratio(d, base) - 100;
    }

    private String str(int bit, double d)
    {
        if (d < -1000)
            return "";
        return WebUtil.d2s(bit, d);
    }

    public String toText(int type)
    {
        StringBuilder sb = new StringBuilder(InVarAM.s_sep5).append(InVarAM.s_sep5);
        if (type >= 0 && type < 100)
        {
            // 2013年，中国国防预算为7410.62亿元，10.74%，合1196.57亿美元，12.87%，中央本级为7177.37亿元，10.74%。
            // 2013年，中国财政支出为140212.10亿元，11.32%，合22639.69亿美元，13.47%，中央本级为20471.76亿元，9.10%，国防预算占比5.29%。
            // 2013年，中国GDP为595244.40亿元，10.16%，合96112.58亿美元，12.28%，财政支出相比为23.56%，国防预算相比为1.24%。
            // 2013年，中国人均GDP为43852元，9.61%，合7080美元，11.72%。
            // 2013年，中国总人口为136072.00万人，0.4933%。
            // 2013年，人民币对美元平均汇率为6.1932，-1.89%。
            sb.append(tfinance.getYear()).append("年，中国国防预算为").append(str(2, tfinance.getDefenseTotal())).append("亿元，").append(str(2, defenseTotalGrowth)).append("%，");
            sb.append("合").append(str(2, defenseTotalUsd)).append("亿美元，").append(str(2, defenseTotalUsdGrowth)).append("%，");
            sb.append("中央本级为").append(str(2, tfinance.getDefenseCentral())).append("亿元，").append(str(2, defenseCentralGrowth)).append("%。");
            sb.append(tfinance.getYear()).append("年，中国财政支出为").append(str(2, tfinance.getOutlaysTotal())).append("亿元，").append(str(2, outlaysTotalGrowth)).append("%，");
            sb.append("合").append(str(2, outlaysTotalUsd)).append("亿美元，").append(str(2, outlaysTotalUsdGrowth)).append("%，");
            sb.append("中央本级为").append(str(2, tfinance.getOutlaysCentral())).append("亿元，").append(str(2, outlaysCentralGrowth)).append("%，");
            sb.append("国防预算占比").append(str(2, defenseTotalOutlaysRatio)).append("%。");
            sb.append(tfinance.getYear()).append("年，中国GDP为").append(str(2, tfinance.getGdp())).append("亿元，").append(str(2, gdpGrowth)).append("%，");
            sb.append("合").append(str(2, gdpUsd)).append("亿美元，").append(str(2, gdpUsdGrowth)).append("%，");
            sb.append("财政支出相比为").append(str(2, outlaysTotalGdpRatio)).append("%，");
            sb.append("国防预算相比为").append(str(2, defenseTotalGdpRatio)).append("%。");
            sb.append(tfinance.getYear()).append("年，中国人均GDP为").append(tfinance.getAgdp()).append("元，").append(str(2, agdpGrowth)).append("%，");
            sb.append("合").append(agdpUsd).append("美元，").append(str(2, agdpUsdGrowth)).append("%。");
            sb.append(tfinance.getYear()).append("年，中国总人口为").append(str(2, tfinance.getPopulation())).append("万人，").append(str(4, populationGrowth)).append("%。");
            sb.append(tfinance.getYear()).append("年，人民币对美元平均汇率为").append(str(4, tfinance.getExchangeUsd())).append("，").append(str(2, exchangeUsdGrowth)).append("%。");
        }
        if (type == 0 || (type >= 100 && type < 200))
        {
            // 2013年，美国国防预算为6334.46亿美元，-6.55%，中国相比为18.89%。
            // 2013年，美国财政支出为34546.48亿美元，-2.33%，中国相比为65.53%，国防预算占比18.34%。
            // 2013年，美国GDP为166915.00亿美元，3.32%，中国相比为57.58%，财政支出相比为20.70%，国防预算相比为3.80%。
            // 2013年，美国人均GDP为52726美元，2.60%，中国相比为13.43%。
            // 2013年，美国总人口为31657.00万人，0.6966%，中国相比为429.83%。
            sb.append(tfinance.getYear()).append("年，美国国防预算为").append(str(2, tfinance.getUsaDefenseTotal())).append("亿美元，").append(str(2, usaDefenseTotalGrowth)).append("%，");
            sb.append("中国相比为").append(str(2, defenseTotalUsRatio)).append("%。");
            sb.append(tfinance.getYear()).append("年，美国财政支出为").append(str(2, tfinance.getUsaOutlaysTotal())).append("亿美元，").append(str(2, usaOutlaysTotalGrowth)).append("%，");
            sb.append("中国相比为").append(str(2, outlaysTotalUsRatio)).append("%，");
            sb.append("国防预算占比").append(str(2, usaDefenseTotalOutlaysRatio)).append("%。");
            sb.append(tfinance.getYear()).append("年，美国GDP为").append(str(2, tfinance.getUsaGdp())).append("亿美元，").append(str(2, usaGdpGrowth)).append("%，");
            sb.append("中国相比为").append(str(2, gdpUsRatio)).append("%，");
            sb.append("财政支出相比为").append(str(2, usaOutlaysTotalGdpRatio)).append("%，");
            sb.append("国防预算相比为").append(str(2, usaDefenseTotalGdpRatio)).append("%。");
            sb.append(tfinance.getYear()).append("年，美国人均GDP为").append(tfinance.getUsaAgdp()).append("美元，").append(str(2, usaAgdpGrowth)).append("%，");
            sb.append("中国相比为").append(str(2, agdpUsRatio)).append("%。");
            sb.append(tfinance.getYear()).append("年，美国总人口为").append(str(2, tfinance.getUsaPopulation())).append("万人，").append(str(4, usaPopulationGrowth)).append("%，");
            sb.append("中国相比为").append(str(2, populationUsRatio)).append("%。");
        }
        return sb.toString();
    }
}
