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

import javax.servlet.http.HttpServletRequest;

import com.arma.web.service.bean.TKnowkey;
import com.arma.web.service.bean.TKnowledge;
import com.arma.web.servlets.kms.bean.Keyword;
import com.arma.web.servlets.kms.bean.KmKeysComparator;
import com.neulion.iptv.web.util.WebUtil;

public class Knowledge
{    
	private TKnowledge tkm;
	private List<TKnowkey> tkks;
    
    public Knowledge fromRequest(HttpServletRequest request)
    {
        Keyword ckey = KmsCacher.getKey(WebUtil.str2int(request.getParameter("ckeyId")));
        if (ckey == null || ckey.empty()) return this;
        
        TKnowledge bean = new TKnowledge();
        bean.setKmId(WebUtil.str2int(request.getParameter("kmId")));
        bean.setCkeyId(ckey.getTkey().getKeyId());
        bean.setContent(request.getParameter("content"));
        if (bean.getKmId() < 1) bean.setKmUuid(WebUtil.uuid2());
        setTkm(bean);
        
        String keys = request.getParameter("keys");
        List<TKnowkey> list = new ArrayList<TKnowkey>();
        if (!WebUtil.empty(keys))
        {
            String[] arr = keys.split(";;");
            for (String str : arr)
            {
                String[] arr2 = str.split("=", 2);
                int keyId = WebUtil.str2int(arr2[0]);
                Keyword k = KmsCacher.getKey(keyId);
                if (k == null || k.empty()) continue;
                if (KmsHelper.mkey(k.getTkey()))
                {
                    String[] arr3 = arr2[1].split(WebUtil.sep_kenum);
                    for (String str2 : arr3) tkk(bean.getKmId(), keyId, str2, list);
                }
                else tkk(bean.getKmId(), keyId, arr2[1], list);
            }
        }
        if (list.size() > 0) 
        {
            Collections.sort(list, new KmKeysComparator(ckey.getTkey().getContent()));
            setTkks(list);
        }
        
        return this;
    }
    
    private void tkk(int kmId, int keyId, String keyVal, List<TKnowkey> list)
    {
        if (keyId < 1 || WebUtil.empty(keyVal) || list == null) return;
        TKnowkey tkk = new TKnowkey();
        if (kmId > 0) tkk.setKmId(kmId);
        tkk.setKeyId(keyId);
        tkk.setKeyVal(keyVal);
        list.add(tkk);
    }
	
	public void setKmId(int kmId)
	{
	    if (kmId < 1) return;
	    if (tkm != null) tkm.setKmId(kmId);
	    if (tkks != null) for (TKnowkey bean : tkks) bean.setKmId(kmId);
	}
    
    public void updateDate(Date now)
    {
        if (now == null) now = new Date();
        if (tkm != null) tkm.setUpdateDate(now);
        if (tkks != null) for (TKnowkey bean : tkks) bean.setUpdateDate(now);
    }
	
	public String getTkk(int keyId)
	{
        if (keyId < 1 || tkks == null || tkks.size() < 1) return "";
        for (TKnowkey bean : tkks) 
            if (bean.getKeyId() == keyId)
                return bean.getKeyVal();
        return "";
	}
    
    public String getKeys(boolean list)
    {
        String ret = "";
        Keyword ckey = KmsCacher.getKey(tkm.getCkeyId());
        if (ckey == null || ckey.empty() || WebUtil.empty(ckey.getTkey().getContent()) || tkks == null || tkks.size() < 1) return ret;
        Map<String, String> map = new HashMap<String, String>();
        for (TKnowkey bean : tkks)
        {
            if (list && KmsHelper.dock(bean.getKeyId())) continue;
           String keyId = "" + bean.getKeyId(), val = bean.getKeyVal(), val0 = map.get(keyId);
           map.put(keyId, (WebUtil.empty(val0) ? "" : val0 + WebUtil.sep_kenum) + val);
        }
        String[] arr = ckey.getTkey().getContent().split(WebUtil.sep_kval);
        for (String str : arr)
        {
            String val = map.get(str);
            if (!WebUtil.empty(val)) ret += ";;" + str + "=" + val;
        }
        return (ret.length() > 0 ? ret.substring(2) : ret);
    }
    
    public boolean empty()
    {
        return (tkm == null || tkm.empty());
    }
	
    public TKnowledge getTkm() {
        return tkm;
    }
    public void setTkm(TKnowledge tkm) {
        this.tkm = tkm;
    }
    public List<TKnowkey> getTkks() {
        return tkks;
    }
    public void setTkks(List<TKnowkey> tkks) {
        this.tkks = tkks;
    }
}

