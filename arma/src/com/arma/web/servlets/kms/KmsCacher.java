/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.arma.web.service.TKmsDaoService;
import com.arma.web.service.bean.TKeyenum;
import com.arma.web.service.bean.TKeyrank;
import com.arma.web.service.bean.TKeytype;
import com.arma.web.service.bean.TKeyword;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class KmsCacher
{
    // all key type map: [type_id, TKeyType]
    private static Map<Integer, TKeytype> _tktypes = new ConcurrentHashMap<Integer, TKeytype>();
    // all key enum map: [key_id, enum_vals]
    private static Map<Integer, String> _kenums = new ConcurrentHashMap<Integer, String>();
    // all keyword map: [key_id, Keyword]
    private static Map<Integer, Keyword> _keys = new ConcurrentHashMap<Integer, Keyword>();
    // all keyword map: [name_cn, key_id], [name_en, key_id]
    private static Map<String, Integer> _nkeys = new ConcurrentHashMap<String, Integer>();
	
	private static final Logger _logger = Logger.getLogger(KmsCacher.class);
	
	public static void setup()
	{
	    loadKeys();
	}
	
	private static void loadKeys()
	{
	    _tktypes.clear();
	    _kenums.clear();
	    _keys.clear();
	    _nkeys.clear();
	    TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<TKeytype> tktypes = dao.getKeytypes(null);
        for (TKeytype bean : tktypes) _tktypes.put(bean.getTypeId(), bean);
        List<TKeyenum> kenums = dao.getKeyenums(null);
        for (TKeyenum bean : kenums) updateKenum(false, bean.getKeyId(), bean.getEnumVal());
	    List<TKeyword> keys = dao.getKeywords(null);
        for (TKeyword bean : keys)
        {
            Keyword key = new Keyword();
            key.setTkey(bean);
            updateKey(false, key);
        }
        _logger.info("setup: load keywords from database done, keys|" + _keys.size());
	}
    
    public static TKeytype getTktype(int typeId)
    {
        return (typeId < 1 ? null : _tktypes.get(typeId));
    }
    
    public static String getKenums(int keyId)
    {
        return (keyId < 1 ? null : _kenums.get(keyId));
    }
    
    public static int updateKenum(boolean db, int keyId, String kenum)
    {
        if (keyId < 1 || WebUtil.empty(kenum)) return 0;
        String ret = getKenums(keyId);
        if (hasKenum(kenum, ret)) return 0;
        boolean create = (WebUtil.empty(ret));
        ret = (create ? "" : ret + WebUtil.sep_kenum) + kenum;
        if (db) 
        {
            TKeyenum bean = new TKeyenum();
            bean.setKeyId(keyId);
            bean.setEnumVal(ret);
            bean.setUpdateDate(new Date());
            TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
            if (create) dao.insertKeyenum(bean);
            else dao.updateKeyenum(bean);
        }
        _kenums.put(keyId, ret);
        return keyId;
    }
    
    private static boolean hasKenum(String key, String base)
    {
        if (WebUtil.empty(key) || WebUtil.empty(base)) return false;
        String[] arr = base.split(WebUtil.sep_kenum);
        for (String str : arr) if (key.equals(str)) return true;
        return false;
    }
	
	public static Keyword getKey(int keyId)
	{
        return (keyId < 1 ? null : _keys.get(keyId));
	}
	
	public static int updateKey(boolean db, Keyword key)
	{
	    if (key == null || key.empty()) return 0;
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        int keyId = key.getTkey().getKeyId();
        String name_cn = key.getTkey().getNameCn(), name_en = key.getTkey().getNameEn();
        if (keyId < 1)
        {
            dao.insertKeyword(key.getTkey());
            List<TKeyword> list = dao.getKeywords("name_cn='" + name_cn + "' and name_en='" + name_en + "' order by update_date desc");
            if (list == null || list.size() < 1) return 0;
            keyId = list.get(0).getKeyId();
            key.setKeyId(keyId);
        }
        else 
        {
            if (db) dao.updateKeyword(key.getTkey());
            deleteKey(false, keyId);
        }
        _keys.put(keyId, key);
        if (!WebUtil.empty(name_cn)) _nkeys.put(name_cn, keyId);
        if (!WebUtil.empty(name_en)) _nkeys.put(name_en, keyId);
        return keyId;
	}
    
    public static int deleteKey(boolean db, int keyId)
    {
        Keyword key = getKey(keyId);
        if (key == null) return 0;
        if (db) GlobalCache.getInstance().getBean(TKmsDaoService.class).deleteKeyword(keyId);
        String name_cn = key.getTkey().getNameCn(), name_en = key.getTkey().getNameEn();
        _keys.remove(keyId);
        if (!WebUtil.empty(name_cn)) _nkeys.remove(name_cn);
        if (!WebUtil.empty(name_en)) _nkeys.remove(name_en);
        return keyId;
    }
    
    private static String ckeys()
    {
        String cks = "";
        int size = _keys.size();
        if (size < 1) return cks;
        List<Integer> ckeys = new ArrayList<Integer>(), sort_ckeys = new ArrayList<Integer>();
        for (Integer keyId : _keys.keySet()) if (KmsHelper.ckey(_keys.get(keyId).getTkey())) ckeys.add(keyId);
        Collections.sort(ckeys);
        List<TKeyrank> ranks = GlobalCache.getInstance().getBean(TKmsDaoService.class).getKeyranks("1=1 order by rank");
        for (TKeyrank rank : ranks)
        {
            Integer keyId = rank.getKeyId();
            if (!ckeys.contains(keyId)) continue;
            sort_ckeys.add(keyId);
            cks += WebUtil.sep_kval + keyId;
        }
        for (Integer keyId : ckeys) if (!sort_ckeys.contains(keyId)) cks += WebUtil.sep_kval + keyId;
        return (cks.length() > 0 ? cks.substring(WebUtil.sep_kval.length()) : cks);
    }
    
    public static int ckeyId(boolean max)
    {
        int ret = 0;
        for (Integer keyId : _keys.keySet()) if (KmsHelper.ckey(_keys.get(keyId).getTkey()) && ((max && keyId > ret) || !max && (ret < 1 || keyId < ret))) ret = keyId;
        return ret;
    }
    
    public static boolean outputKey(int keyId, XmlOutput4j xop)
    {
        Keyword key = getKey(keyId);
        if (key == null || xop == null) return false;
        TKeyword tt = _keys.get(keyId).getTkey();
        xop.openTag("key", InVarKM.attrs_keyword, new String[]{"" + keyId, tt.getContent(), tt.getNameCn(), tt.getNameEn(), 
                "" + tt.getTypeId(), "" + tt.isAsEnum(), DateUtil.str(tt.getUpdateDate())});
        xop.closeTag();
        return true;
    }
    
    public static void outputMeta(XmlOutput4j xop)
    {
        List<Integer> list = new ArrayList<Integer>();
        xop.openTag("km", InVarKM.attrs_ckeys, new String[]{ckeys()});
        
        // ktypes list
        xop.openTag("kts", null, null);
        for (Integer typeId : _tktypes.keySet()) list.add(typeId);
        Collections.sort(list);
        for (Integer typeId : list) 
        {
            TKeytype tt = _tktypes.get(typeId);
            // "typeId", "nameCn", "nameEn", "demoCn", "demoEn", "updateDate"
            xop.appendTag0(false, "kt", null, InVarKM.attrs_keytype, new String[]{"" + typeId, tt.getNameCn(), tt.getNameEn(), tt.getDemoCn(), tt.getDemoEn(), null});
        }
        xop.closeTag();
        list.clear();
        
        // kenums list
        xop.openTag("kes", null, null);
        for (Integer keyId : _kenums.keySet()) list.add(keyId);
        Collections.sort(list);
        // "keyId", "enumVal", "updateDate"
        for (Integer keyId : list) xop.appendTag0(false, "ke", null, InVarKM.attrs_keyenum, new String[]{"" + keyId, _kenums.get(keyId), null});
        xop.closeTag();
        list.clear();
        
        // keys list
        xop.openTag("ks", null, null);
        for (Integer keyId : _keys.keySet()) list.add(keyId);
        Collections.sort(list);
        for (Integer keyId : list) 
        {
            TKeyword tt = _keys.get(keyId).getTkey();
            // "keyId", "content", "nameCn", "nameEn", "typeId", "asEnum", "updateDate"
            xop.appendTag0(false, "k", null, InVarKM.attrs_keyword, new String[]{"" + keyId, (KmsHelper.kkey(tt) ? tt.getContent() : null), tt.getNameCn(), tt.getNameEn(), 
                    "" + tt.getTypeId(), "" + tt.isAsEnum(), DateUtil.str(tt.getUpdateDate())});
        }
        xop.closeTag();
        list.clear();
        
        xop.closeTag();
    }
    
    public static String outputMeta()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        outputMeta(new XmlOutput4j(0, map));
        String json = JSONObject.fromObject(map).toString();
        return json;
    }
}
