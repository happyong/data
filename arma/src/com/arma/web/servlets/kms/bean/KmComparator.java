/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms.bean;

import java.util.Comparator;

import com.arma.web.servlets.kms.KmsHelper;
import com.arma.web.servlets.kms.Knowledge;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;

public class KmComparator implements Comparator<Knowledge>
{
    private boolean desc;
    private boolean parseInt;
    private int sortby;

    public KmComparator(boolean desc, boolean parseInt, int sortby)
    {
        this.desc = desc;
        this.parseInt = parseInt;
        this.sortby = sortby;
    }

    public int compare(Knowledge km1, Knowledge km2)
    {
        boolean updk = (KmsHelper.updk(sortby));
        String val1 = (updk ? DateUtil.str(km1.getTkm().getUpdateDate()) : km1.getTkk(sortby)), val2 = (updk ? DateUtil.str(km2.getTkm().getUpdateDate()) : km2.getTkk(sortby));
        int ret = (parseInt ? WebUtil.str2int(val1) - WebUtil.str2int(val2) : val1.compareTo(val2));
        if (ret == 0)
            ret = km1.getTkm().getKmId() - km2.getTkm().getKmId();
        ret = (ret > 0 ? 1 : (ret < 0 ? -1 : 0));
        return (desc ? 0 - ret : ret);
    }
}
