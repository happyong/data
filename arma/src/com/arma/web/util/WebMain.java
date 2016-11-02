/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.neulion.iptv.web.util.HttpUtil;
import com.neulion.iptv.web.util.WebUtil;

public class WebMain
{
    private static String url = "http://www.huisongshu.com/ldjy/";

    public static void main(String[] args)
    {
        String dir = "d:/tmp/ldjy/", text = url2text(HttpUtil.connect(url + "left.htm", null, null));
        String key1 = "<TD><A href=", key2 = "</A></TD></TR>", key3 = "target=mainFrame>□";
        Map<String, Integer> urls = new HashMap<String, Integer>();
        int pos1 = text.indexOf(key1, -1), pos2 = text.indexOf(key2, pos1);
        while (pos1 > 0 && pos2 > pos1)
        {
            String all = text.substring(pos1 + key1.length(), pos2);
            if (all.startsWith("\""))
                all = all.substring(1);
            int pos3 = all.indexOf("\""), pos4 = all.indexOf(key3);
            if (pos3 < 0 || pos4 < 0)
            {
                System.out.println("miss " + all);
                continue;
            }
            String uri = all.substring(0, pos3);
            int pos5 = uri.lastIndexOf("/");
            String key = (pos5 == -1 ? "" : uri.substring(0, pos5));
            Integer last = urls.get(key);
            int cur = (last == null ? 1 : last.intValue() + 1);
            urls.put(key, cur);
            String url0 = url + uri;
            String name = "历史" + key + WebUtil.f2s000(cur) + " " + all.substring(pos4 + key3.length()) + ".jpg";
            if (!new File(dir + name).exists())
                HttpUtil.url2file(null, dir + name, HttpUtil.connect(url0, null, null), null);
            pos1 = text.indexOf(key1, pos2);
            pos2 = text.indexOf(key2, pos1);
        }
        for (Map.Entry<String, Integer> entry : urls.entrySet())
        {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    public static String url2text(HttpURLConnection hc)
    {
        String text = "";
        if (hc == null)
            return text;

        try
        {
            text = IOUtils.toString(hc.getInputStream(), WebUtil.CHARSET_GB2312);
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                if (hc != null)
                    hc.disconnect();
            }
            catch (Exception e)
            {
            }
        }
        return text;
    }
}