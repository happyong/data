/*
 * Copyright (c) 2011 NeuLion, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arma.web.service.TKmsDaoService;
import com.arma.web.service.bean.TFinance;
import com.arma.web.service.bean.TKnowledge;
import com.arma.web.servlets.kms.bean.Finance;
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
	    List<String> lines = FileUtil2.readLines("arma.coast.txt", null, new File("c:"));
        if (lines == null || lines.size() < 1) return false;

        List<String> result = new ArrayList<String>();
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
        if (lines == null || lines.size() < 1) return false;
        
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

        // String tags = "";
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
            // List<Integer> kkids = dao.getKeyIds("km_id=" + kmId + " and key_id=2000 and key_val='" + InVarAM.s_fleet_tag + "'");
            // if (kkids == null || kkids.size() < 1) tags += InVarAM.s_sep1 + tag;
            List<Map<String, Object>> list = dao.getKeys("km_id=" + kmId + " and key_id=2001 and key_val like '" + InVarAM.s_fleet_key + "%' order by key_val");
            for (String val : fleets.get(tag))
            {
                if (hit(val, list)) update = true;
                else KmsHelper.newKmKey4Tags(2001, val, false, tag);
            }
            if (update) dao.updateKnowkeys(list);
        }
        // if (tags.length() > 0) KmsHelper.newKmKey4Tags(2000, InVarAM.s_fleet_tag, false, tags.substring(InVarAM.s_sep1.length()));
        return true;
    }
    
    public static boolean handleDate()
    {
        List<String> lines = FileUtil2.readLines("arma.date.txt", null, new File("c:")), lines2 = new ArrayList<String>();
        if (lines == null || lines.size() < 1) return false;
        
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
            if (line.length() < 12 || !Character.isDigit(line.charAt(0))) continue;
            lines2.add(line);
        }
        Collections.sort(lines2);
        lines.clear();
        for (String line : lines2)
            lines.add(0, "    " + line);
        lines.add(0, "");
        FileUtil2.writeLines(lines, "arma.date2.txt", null, WebUtil.LINE_WIN, new File("c:"));
        return true;
    }
    
    public static boolean handleRocket()
    {
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<TKnowledge> list = dao.getKnowledges("ckey_id=150");
        Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
        for (TKnowledge bean : list)
        {
            String context = bean.getContent();
            int pos1 = context.indexOf(InVarAM.s_sep3), pos2 = context.indexOf(InVarAM.s_sep4);
            if (pos1 < 1 || pos2 < pos1) continue;
            String key = context.substring(pos1 + 1, pos2).trim();
            int gen = gen(key);
            List<Integer> list2 = map.get(gen);
            if (list2 == null)
            {
                list2 = new ArrayList<Integer>();
                map.put(gen,  list2);
            }
            list2.add(bean.getKmId());
        }
        List<Integer> list2 = map.get(0);
        if (list2 != null && list2.size() > 0) return false;
        
        List<Map<String, Object>> params = WebUtil.params(null);
        for (int gen : map.keySet()) update(gen, map.get(gen), params);
        dao.batchUpdate("update t_knowledge set km_id=:nkmid where km_id=:okmid", params);
        dao.batchUpdate("update t_knowkey set km_id=:nkmid where km_id=:okmid", params);
        dao.batchUpdate("update t_knowledge set km_id=km_id+1490000 where km_id<20000", null);
        dao.batchUpdate("update t_knowkey set km_id=km_id+1490000 where km_id<20000", null);
        return true;
    }    

    private static void update(int gen, List<Integer> list, List<Map<String, Object>> params)
    {
        int sz = (list == null ? 0 : list.size());
        for (int i = 0; i < sz; i++)
        {
            Map<String, Object> map = WebUtil.param("okmid", list.get(i));
            map.put("nkmid",10000 + gen * 1000 + 101 + i);
            params.add(map);
        }
    }
    
    private static int gen(String key)
    {
        for (int i = 0; i < InVarAM.s_rockets.length; i++) if (WebUtil.pos(key, InVarAM.s_rockets[i]) != -1) return (i + 1);
        return 0;
    }
    
    public static boolean handleKeyVal(String param)
    {
        String[] params = param.split("::");
        if (params == null || params.length < 3) return false;
        String prefix = params[0], tags = params[1], newv = params[2], oldv = (params.length > 3 ? params[3] : tags);
        String[] arr = WebUtil.unull(tags).split(InVarAM.s_sep1);
        if (arr == null || arr.length < 1) return false;
        
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        for (String tag : arr) 
        {
            int pos = tag.indexOf(InVarAM.s_sep2);
            if (pos > 0) tag = tag.substring(0, pos);
            List<Integer> kmids = dao.getKmIds(true, "key_id=2000 and key_val='" + tag + "'");
            if (kmids.size() < 1)
            {
                System.out.println(tag + " not found");
                continue;
            }
            String kmId = "" + kmids.get(0);
            List<Map<String, Object>> list = dao.getKeys("km_id=" + kmId + " and key_id=2001 and key_val like '" + prefix + "%" + oldv + "%' order by key_val");
            if (list.size() < 1)
            {
                System.out.println(tag + " not found the key_val with prefix " + prefix);
                continue;
            }
            for (Map<String, Object> map : list)
            {
                String val0 = (String) map.get("keyVal"), val2 = WebUtil.substituteName(oldv, newv, val0);
                map.put("keyVal", val2);
            }
            dao.updateKnowkeys(list);
        }
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
        map.put("num00", WebUtil.i2s(2, WebUtil.str2int(map.get("num"))));
        return map;
    }
    
    private static int unull(int start, String[] arr)
    {
        if (start < 0 || start >= arr.length) return -1;
        for (int i = start; i < arr.length; i++) if (!WebUtil.empty(arr[i])) return i;
        return -1;
    }
    
    public static boolean handleVote()
    {
        List<String> lines = FileUtil2.readLines("arma.vote.txt", null, new File("c:"));
        if (lines == null || lines.size() < 1) return false;
        
        int pos1 = -1, pos2 = -1;
        String key1 = "{", key2 = "}", key = InVarAM.s_vote_key;
        List<String[]> keys = new ArrayList<String[]>();
        while (true)
        {
            int p1 = key.indexOf(key1, pos1 + 1), p2 = key.indexOf(key2, pos2 + 1);
            if (p1 == -1 || p2 == -1 || p2 <= p1) break;
            int t2 = key.indexOf(key1, p2 + 1);
            String[] arr = new String[3];
            arr[0] = key.substring(p1 + 1, p2);
            arr[1] = key.substring(pos2 == -1 ? 0 : pos2 + 1, p1);
            arr[2] = key.substring(p2 + 1, (t2 == -1 ? key.length() : t2));
            keys.add(arr);
            pos1 = p1;
            pos2 = p2;
        };
        
        int v1 = 0, v2 = 0, no = 0;
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = lines.size() - 1; i >= 0; i--)
        {
            if (i < 1) continue;
            String line = lines.get(i).trim();
            if (line.length() < 1 || line.indexOf(InVarAM.s_vote_seps[0]) < 1) continue;
            String line2 = lines.get(i - 1).trim();
            int pos = (line2.startsWith("2016") ? line2.indexOf(" ") : -1);
            if (pos < 1 || line2.length() < pos + 6) continue;
            no++;
            Map<String, String> map = new HashMap<String, String>();
            for (String[] arr : keys)
            {
                pos1 = (arr[1].length() == 0 ? 0 : line.indexOf(arr[1]));
                pos2 = (arr[2].length() == 0 ? line.length() : line.indexOf(arr[2]));
                if (pos1 == -1 || pos2 == -1) System.out.println("scan invalid - " + arr[0] + "_" + arr[1] + "_" + arr[2]);
                else 
                {
                    String value = line.substring(pos1 + arr[1].length(), pos2);
                    if ("cand".equals(arr[0]) && WebUtil.pos(value, InVarAM.s_vote_cands) == -1) System.out.println("cand invalid - " + value);
                    else if ("count".equals(arr[0]) && WebUtil.str2int(value) < 1) System.out.println("count invalid - " + value);
                    else map.put(arr[0], line.substring(pos1 + arr[1].length(), pos2));
                }
            }
            if (map.size() < keys.size()) System.out.println("line invalid - " + line);
            else 
            {
                int count = WebUtil.str2int(map.get("count"));
                boolean is1 = (InVarAM.s_vote_cands[0].equals(map.get("cand")));
                v1 += (is1 ? count : 0);
                v2 += (is1 ? 0 : count);
                String info = (line.indexOf(InVarAM.s_vote_seps[1]) == -1 ? "" : InVarAM.s_vote_seps[1]);
                if (info.length() > 0 && line.indexOf(InVarAM.s_vote_seps[2]) != -1) info = InVarAM.s_vote_seps[2] + info;
                else if (info.length() > 0 && line.indexOf(InVarAM.s_vote_seps[3]) != -1) info = InVarAM.s_vote_seps[3] + info;
                map.put("no", "" + no);
                map.put("v1", "" + v1);
                map.put("v2", "" + v2);
                map.put("time", line2.substring(pos + 1, pos + 6));
                map.put("info", (info.length() > 0 ? InVarAM.s_vote_seps[4] + info : ""));
                list.add(map);
            }
            i--;
        }
        boolean revert = (v2 > v1);
        List<String> result = new ArrayList<String>();
        for (int i = list.size() - 1; i >= 0; i--)
        {
            Map<String, String> map = list.get(i);
            String line = InVarAM.s_vote_val;
            if (revert)
            {
                String str = map.get("v1");
                map.put("v1", map.get("v2"));
                map.put("v2", str);
            }
            for (String str : map.keySet()) line = WebUtil.substituteName(key1 + str + key2, map.get(str), line);
            result.add(line);
        }
        FileUtil2.writeLines(result, "arma.vote.fmt.txt", null, WebUtil.LINE_WIN, new File("c:"));
        return true;
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

    public static boolean handleFinance(int type)
    {
        Finance last = null;
        List<String> lines = new ArrayList<String>();
        List<TFinance> list = GlobalCache.getInstance().getBean(TKmsDaoService.class).getFinances(null);
        for (TFinance bean : list)
        {
            Finance cur = new Finance(bean);
            cur.stat(last);
            lines.add(0, cur.toText(type));
            last = cur;
        }
        FileUtil2.writeLines(lines, "arma.finance" + type + ".txt", null, WebUtil.LINE_WIN, new File("c:"));
        return true;
    }
}
