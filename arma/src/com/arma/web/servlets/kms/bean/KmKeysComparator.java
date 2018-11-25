/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms.bean;

import java.util.Comparator;

import com.arma.web.service.bean.TKnowkey;
import com.arma.web.servlets.kms.KmsHelper;
import com.neulion.iptv.web.util.WebUtil;

public class KmKeysComparator implements Comparator<TKnowkey>
{
    private String[] order;

    public KmKeysComparator(String order)
    {
        this.order = (WebUtil.empty(order) ? null : order.split(WebUtil.sep_kval));
    }

    public int compare(TKnowkey key1, TKnowkey key2)
    {
        if (order == null)
            return 0;
        int pos1 = WebUtil.pos("" + key1.getKeyId(), order), pos2 = WebUtil.pos("" + key2.getKeyId(), order), ret = pos1 - pos2;
        if (ret == 0 && key1.getKeyId() != KmsHelper.tagk())
            ret = key1.getKeyVal().compareTo(key2.getKeyVal());
        return (ret > 0 ? 1 : (ret < 0 ? -1 : 0));
    }
}
