/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.arma.web.service.bean.TKeyword;
import com.neulion.iptv.web.util.WebUtil;

public class Keyword
{
	private TKeyword tkey;
	
	public Keyword fromRequest(HttpServletRequest request)
	{
        TKeyword bean = new TKeyword();
        bean.setKeyId(WebUtil.str2int(request.getParameter("keyId")));
        bean.setContent(request.getParameter("content"));
        bean.setNameCn(request.getParameter("nameCn"));
        bean.setNameEn(request.getParameter("nameEn"));
        bean.setTypeId(WebUtil.str2int(request.getParameter("type")));
        bean.setAsEnum(WebUtil.str2int(request.getParameter("asEnum")) > 0);
        bean.setUpdateDate(new Date());
        setTkey(bean);
        return this;
	}
    
    public void setKeyId(int keyId)
    {
        if (keyId < 1) return;
        if (tkey != null) tkey.setKeyId(keyId);
    }
    
    public boolean empty()
    {
        return (tkey == null || tkey.empty());
    }
    
    public String name()
    {
        return (empty() ? "" : (WebUtil.empty(tkey.getNameCn()) ? WebUtil.unull(tkey.getNameEn()) : tkey.getNameCn()));
    }

    public TKeyword getTkey() {
        return tkey;
    }
    public void setTkey(TKeyword tkey) {
        if (tkey == null || tkey.empty()) return;
        this.tkey = tkey;
    }
}

