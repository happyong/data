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

public class TKeyenum extends BaseDaoBean
{
    private int keyId;
    private String enumVal;
    private Date updateDate;

    @Override
    public Map<String, Object> toDbMap()
    {
        Map<String, Object> dbmap = new HashMap<String, Object>();
        dbmap.put("keyId", keyId);
        dbmap.put("enumVal", WebUtil.unull(enumVal));
        dbmap.put("updateDate", updateDate);
        return dbmap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)
    {
        if (dbmap == null || dbmap.size() == 0)
            return (T) this;
        keyId = WebUtil.obj2int(dbmap.get("keyId"));
        enumVal = (String) dbmap.get("enumVal");
        updateDate = (Date) dbmap.get("updateDate");
        return (T) this;
    }

    public TKeyenum fromRequest(Node node, HttpServletRequest request)
    {
        keyId = WebUtil.obj2int(WebUtil.scan_str("key_id", node, request));
        enumVal = WebUtil.scan_str("enum_val", node, request);
        updateDate = DateUtil.date(WebUtil.scan_str("update_date", node, request));
        return this;
    }

    public TKeyenum copyit()
    {
        TKeyenum bean = new TKeyenum();
        bean.setKeyId(getKeyId());
        bean.setEnumVal(getEnumVal());
        bean.setUpdateDate(getUpdateDate() == null ? null : new Date(getUpdateDate().getTime()));
        return bean;
    }

    @Override
    public String toText()
    {
        return (keyId + "|" + WebUtil.unull(enumVal) + "|" + DateUtil.date24Str(updateDate, DateUtil.df_long));
    }

    public boolean empty()
    {
        return (keyId < 1);
    }

    public void append(boolean close, XmlOutput4j xop)
    {
        xop.openTag("keyenum", InVarKM.attrs_keyenum, new String[] { "" + getKeyId(), getEnumVal(), DateUtil.str(getUpdateDate()) });
        if (close)
            xop.closeTag();
    }

    public int getKeyId()
    {
        return keyId;
    }

    public void setKeyId(int keyId)
    {
        this.keyId = keyId;
    }

    public String getEnumVal()
    {
        return enumVal;
    }

    public void setEnumVal(String enumVal)
    {
        this.enumVal = enumVal;
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
