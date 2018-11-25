/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Node;

import com.arma.web.servlets.kms.InVarKM;
import com.neulion.iptv.web.service.BaseDaoBean;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class TKnowledge extends BaseDaoBean
{
    private int kmId;
    private String kmUuid;
    private int ckeyId;
    private String content;
    private Date updateDate;

    @Override
    public Map<String, Object> toDbMap()
    {
        Map<String, Object> dbmap = new HashMap<String, Object>();
        dbmap.put("kmId", kmId);
        dbmap.put("kmUuid", WebUtil.unull(kmUuid));
        dbmap.put("ckeyId", ckeyId);
        dbmap.put("content", WebUtil.unull(content));
        dbmap.put("updateDate", updateDate);
        return dbmap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
    {
        if (dbmap == null || dbmap.size() == 0)
            return (T) this;
        kmId = WebUtil.obj2int(dbmap.get("kmId"));
        kmUuid = (String) dbmap.get("kmUuid");
        ckeyId = WebUtil.obj2int(dbmap.get("ckeyId"));
        content = (String) dbmap.get("content");
        updateDate = (Date) dbmap.get("updateDate");
        return (T) this;
    }

    public TKnowledge fromRequest(Node node, HttpServletRequest request)
    {
        kmId = WebUtil.obj2int(WebUtil.scan_str("km_id", node, request));
        kmUuid = WebUtil.scan_str("km_uuid", node, request);
        ckeyId = WebUtil.obj2int(WebUtil.scan_str("ckey_id", node, request));
        content = WebUtil.scan_str("content", node, request);
        updateDate = DateUtil.date(WebUtil.scan_str("update_date", node, request));
        return this;
    }

    public TKnowledge copyit()
    {
        TKnowledge bean = new TKnowledge();
        bean.setKmId(getKmId());
        bean.setKmUuid(getKmUuid());
        bean.setCkeyId(getCkeyId());
        bean.setContent(getContent());
        bean.setUpdateDate(getUpdateDate() == null ? null : new Date(getUpdateDate().getTime()));
        return bean;
    }

    @Override
    public String toText()
    {
        return (kmId + "|" + WebUtil.unull(kmUuid) + "|" + ckeyId + "|" + WebUtil.unull(content) + "|" + DateUtil.date24Str(updateDate, DateUtil.df_long));
    }

    public boolean empty()
    {
        return ((kmId < 1 && WebUtil.empty(kmUuid)) || ckeyId < 1);
    }

    public void append(boolean close, XmlOutput4j xop)
    {
        xop.openTag("knowledge", InVarKM.attrs_knowledge, new String[] { "" + getKmId(), null, "" + getCkeyId(), getContent(), DateUtil.str(getUpdateDate()) });
        if (close)
            xop.closeTag();
    }

    public int getKmId()
    {
        return kmId;
    }

    public void setKmId(int kmId)
    {
        this.kmId = kmId;
    }

    public String getKmUuid()
    {
        return kmUuid;
    }

    public void setKmUuid(String kmUuid)
    {
        this.kmUuid = kmUuid;
    }

    public int getCkeyId()
    {
        return ckeyId;
    }

    public void setCkeyId(int ckeyId)
    {
        this.ckeyId = ckeyId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Date getUpdateDate()
    {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
    }
}
