/*
 * Copyright (c) 2011 NeuLion, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arma.web.service.TKmsDaoService;
import com.arma.web.util.InVarAM;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.FileUtil2;
import com.neulion.iptv.web.util.WebUtil;

/**
 * Kms Sky
 * 
 * $Revision: 1.01 $, $Date: 2011/07/21 14:43:22 $
 */
public class KmsUtil 
{
	public static boolean handleCoast()
	{
        List<String> result = new ArrayList<String>();
	    List<String> lines = FileUtil2.readLines("arma.coast.txt", null, new File("c:"));
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
	    for (int i = lines.size() - 1; i >= 0; i--)
	    {
            int pos = 0;
	        String line = lines.get(i).trim();
	        while ((pos < line.length()) && !Character.isDigit(line.charAt(pos))) pos++;
	        line = line.substring(pos);
	        if (line.length() < 1) continue;
            List<Integer> kkids = dao.getKeyIds("key_id=2001 and key_val='" + line + "'");
            if (kkids != null && kkids.size() > 0) continue;
            result.clear();
	        for (String key : InVarAM.s_coasts)
	        {
	            int len = key.length(), start = 0 - len;
	            while (true)
	            {
	                start = line.indexOf(key, start + len);
	                if (start < 0) break;
	                scan(start + len, key, line, result);
	            }
	        }
	        if (result.size() < 1) continue;
	        String tags = WebUtil.join(InVarAM.s_sep1, result.toArray(new String[result.size()]));
	        KmsHelper.newKmKey4Tags(2001, line, false, tags);
	    }
	    return true;
	}
	
	private static void scan(int pos, String key, String line, List<String> result)
	{
	    String str = "";
        for (int i = pos; i < line.length(); i++)
        {
            char c = line.charAt(i);
            if (InVarAM.s_sep1.equals("" + c))
            {
                if (str.length() == 0) break;
                result.add(key + str);
                str = "";
            }
            else if (Character.isDigit(c)) str += c;
            else 
            {
                if (str.length() > 0) result.add(key + str);
                break;
            }
        }
	}
	
    public static boolean handleFleet()
    {
        List<String> lines = FileUtil2.readLines("arma.fleet.txt", null, new File("c:"));
        Map<String, List<String>> fleets = new HashMap<String, List<String>>();
        for (String line : lines)
        {
            if (line.length() < 5 || !line.startsWith("    ") || !Character.isDigit(line.charAt(4))) continue;
            Map<String, String> map = map(line.substring(4).split("\t"));
            String val = InVarAM.s_fleet_val;
            for (String str : map.keySet()) val = WebUtil.substituteName("{" + str + "}", map.get(str), val);
            String[] arr = WebUtil.unull(map.get("list")).split(InVarAM.s_sep1);
            for (String tag : arr) 
            {
                int pos = tag.indexOf(InVarAM.s_sep2);
                if (pos > 0) tag = tag.substring(0, pos);
                List<String> list = fleets.get(tag);
                if (list == null)
                {
                    list = new ArrayList<String>();
                    fleets.put(tag, list);
                }
                list.add(val);
            }
        }

        String tags = "";
        Map<String, String> kmIds = new HashMap<String, String>();
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        for (String tag : fleets.keySet())
        {
            List<Integer> kmids = dao.getKmIds(true, "key_id=2000 and key_val='" + tag + "'");
            if (kmids == null || kmids.size() != 1)
            {
                System.out.println("tag invalid - " + tag);
                return false;
            }
            kmIds.put(tag, "" + kmids.get(0));
        }
        for (String tag : fleets.keySet())
        {
            boolean update = false;
            String kmId = kmIds.get(tag);
            List<Integer> kkids = dao.getKeyIds("km_id=" + kmId + " and key_id=2000 and key_val='" + InVarAM.s_fleet_tag + "'");
            if (kkids == null || kkids.size() < 1) tags += InVarAM.s_sep1 + tag;
            List<Map<String, Object>> list = dao.getKeys("km_id=" + kmId + " and key_id=2001 and key_val like '" + InVarAM.s_fleet_key + "%' order by key_val");
            for (String val : fleets.get(tag))
            {
                if (hit(val, list)) update = true;
                else KmsHelper.newKmKey4Tags(2001, val, false, tag);
            }
            if (update) dao.updateKnowkeys(list);
        }
        if (tags.length() > 0) KmsHelper.newKmKey4Tags(2000, InVarAM.s_fleet_tag, false, tags.substring(InVarAM.s_sep1.length()));
        return true;
    }
    
    private static boolean hit(String newval, List<Map<String, Object>> list)
    {
        String prefix = newval.substring(0, InVarAM.s_fleet_key.length() + 2);
        for (int i = 0; i < list.size(); i++) 
        {
            if (!((String)list.get(i).get("keyVal")).startsWith(prefix)) continue;
            list.get(i).put("keyVal", newval);
            return true;
        }
        return false;
    }
    
    private static Map<String, String> map(String[] arr)
    {
        Map<String, String> map = new HashMap<String, String>();
        int len = (arr == null ? 0 : arr.length), tlen = InVarAM.s_fleet_tabs.length, start = 0;
        if (len < tlen - 1) return map;
        for (int i = 0; i < tlen; i++)
        {
            start = unull(start, arr);
            String val = (start < 0 || start >= len ? "" : arr[start]), tab = InVarAM.s_fleet_tabs[i], sep = InVarAM.s_fleet_seps[i];
            int pos = (WebUtil.empty(sep) ? -1 : tab.indexOf(sep));
            if (pos == -1) map.put(tab, val);
            else
            {
                String[] arr2 = val.split(sep, 2);
                map.put(tab.substring(0, pos), arr2[0]);
                map.put(tab.substring(pos + 1), (arr2.length < 2 ? "" : arr2[1]));
            }
            start++;
        }
        map.put("num00", WebUtil.f2s00(map.get("num")));
        return map;
    }
    
    private static int unull(int start, String[] arr)
    {
        if (start < 0 || start >= arr.length) return -1;
        for (int i = start; i < arr.length; i++) if (!WebUtil.empty(arr[i])) return i;
        return -1;
    }
    
    public static boolean handleTkkTest(int min)
    {
        if (min < 100) min = 100;
        int max = min + 5, kmid = min / 100, deta = 0;
        String sql = "insert into t_knowkey (id, km_id, key_id, key_val, update_date)", now = DateUtil.str(new Date());
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<Integer> list = dao.getKeyIds("id between " + min + " and " + max + " order by id");
        if (list != null && list.size() > 0) return false;
        for (int i = min; i <= max; i++) dao.batchUpdate(sql + " values (" + i + ", " + kmid + ", 2000, '', '" + now + "')", null);
        KmsHelper.fmtkkids(kmid, kmid, deta, dao);
        dao.batchUpdate("delete from t_knowkey where km_id=" + kmid + " and update_date='" + now + "'", null);
        return true;
    }
}
