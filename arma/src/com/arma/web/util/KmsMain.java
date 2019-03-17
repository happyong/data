/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

import com.arma.web.servlets.kms.KmsUtil;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.FileUtil;
import com.neulion.iptv.web.util.HttpUtil;
import com.neulion.iptv.web.util.WebUtil;

public class KmsMain
{
    public static void main(String[] args)
    {
        // kmsdate();
        stratfor();
    }

    protected static void kmsdate()
    {
        KmsUtil.handleDate();
    }

    protected static void stratfor()
    {
        int pos1 = InVarAM.url_naval_usa.lastIndexOf("/"), pos2 = InVarAM.url_naval_usa.lastIndexOf("-"), pos3 = InVarAM.url_naval_usa.lastIndexOf("."), fetch = 0;
        if (pos1 < 1 || pos2 <= pos1 || pos3 <= pos2)
            return;
        Date baseDate = DateUtil.str24Date(InVarAM.url_naval_usa.substring(pos2 + 1, pos3), "MMddyyyy");
        if (baseDate == null)
            return;
        long baseTime = baseDate.getTime(), now = System.currentTimeMillis(), step = 86400000L * 7;
        String baseUrl = InVarAM.url_naval_usa.substring(0, pos1 + 1), baseDir = InVarAM.dir_naval_usa;
        String baseName = InVarAM.url_naval_usa.substring(pos1 + 1, pos2 + 1) + "{time}" + InVarAM.url_naval_usa.substring(pos3);
        System.out.println(" ====== stratfor start,  " + baseUrl + baseName + ", " + baseDir + " ====== ");
        System.out.println(" ====== stratfor forward ====== ");
        for (long time = baseTime; time <= now; time += step)
            fetch = stratfor(time, baseName, baseUrl, baseDir);
        // System.out.println(" ====== stratfor backward ====== ");
        // for (long time = baseTime; fetch >= 0; time -= step)
        //     fetch = stratfor(time, baseName, baseUrl, baseDir);
        System.out.println(" ====== stratfor end " + fetch + " ====== ");
    }

    private static int stratfor(long ltime, String baseName, String baseUrl, String baseDir)
    {
        long now = System.currentTimeMillis();
        Date date = new Date(ltime);
        String url = baseUrl + WebUtil.substituteName("{time}", DateUtil.date24Str(date, "MMddyyyy"), baseName);
        String file = baseDir + WebUtil.substituteName("{time}", DateUtil.date24Str(date, "yyyyMMdd"), baseName);
        if (FileUtil.exist(1, file) != null)
            return 0;
        boolean success = false;
        try
        {
            HttpURLConnection hc = HttpUtil.open(url);
            hc.addRequestProperty(InVarAM.key_user_agent, InVarAM.def_user_agent);
            success = HttpUtil.url2file(null, file, hc, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println(url + (success ? " done, " : " fail, ") + WebUtil.time(now) + " ms");
        return (success ? 1 : -1);
    }
}