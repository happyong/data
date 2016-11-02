/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.neulion.iptv.web.util.WebUtil;

public class KmsCond
{    
    private int ckeyId;
	private Keyword ckey;
    private boolean desc;
    private Keyword skey;
	private Keyword[] keys;
	private String[] bases;
    
    public KmsCond fromRequest(boolean get, HttpServletRequest request)
    {
        ckeyId = WebUtil.str2int(request.getParameter("ckeyId"));
        ckey = KmsCacher.getKey(ckeyId);
        if (ckey == null || ckey.empty()) return this;
        desc = (WebUtil.str2int(request.getParameter("desc")) > 0);
        int sortby = WebUtil.str2int(request.getParameter("sortby"));
        if (sortby > 0)
        {
            Keyword skey = KmsCacher.getKey(sortby);
            String[] sks = WebUtil.unull(ckey.getTkey().getContent()).split(WebUtil.sep_kval);
            if (skey != null && !skey.empty() && WebUtil.hit("" + sortby, sks) >= 0) this.skey = skey;
        }

        String conds0 = request.getParameter("conds");
        try { if (get) conds0 = new String(conds0.getBytes(WebUtil.CHARSET_ISO), WebUtil.CHARSET_UTF_8); } catch (Exception e) {}
        List<String> list2 = new ArrayList<String>();
        List<Keyword> list1 = new ArrayList<Keyword>();
        if (!WebUtil.empty(conds0))
        {
            String[] arr = conds0.split(";;");
            for (String str : arr)
            {
                String[] arr2 = str.split("=", 2);
                int keyId = WebUtil.str2int(arr2[0]);
                Keyword k = KmsCacher.getKey(keyId);
                if (k == null || k.empty()) continue;
                list1.add(k);
                list2.add(arr2[1]);
            }
        }
        keys = list1.toArray(new Keyword[list1.size()]);
        bases = list2.toArray(new String[list2.size()]);
        return this;
    }
    
    public String conds()
    {
        String ret = "";
        if (empty()) return ret;
        for (int i = 0; i < keys.length; i++) ret += "; " + keys[i].name() + "=" + bases[i];
        return (ret.length() > 0 ? ret.substring(2) : ret);
    }
    
    public boolean empty()
    {
        return (ckey == null || ckey.empty() || keys == null || bases == null || keys.length != bases.length);
    }

    public int getCkeyId()
    {
        return ckeyId;
    }

    public void setCkeyId(int ckeyId)
    {
        this.ckeyId = ckeyId;
    }

    public Keyword getCkey()
    {
        return ckey;
    }

    public boolean isDesc()
    {
        return desc;
    }

    public Keyword getSkey()
    {
        return (skey == null ? null : skey);
    }

    public Keyword[] getKeys()
    {
        return keys;
    }

    public String[] getBases()
    {
        return bases;
    }
}

