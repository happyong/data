/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms.bean;

import java.util.HashMap;
import java.util.Map;

import com.arma.web.servlets.kms.KmsHelper;

public class KmsIdBuilder
{
    private int index;
    private int nextIndex0;

    private Map<Integer, Integer> kk;
    private Map<Integer, KmsIdBuilder> km;

    public int kkId(int kmId0)
    {
        if (kmId0 < 1)
            return 0;
        if (kk == null)
            kk = new HashMap<Integer, Integer>();
        Integer i = kk.get(kmId0);
        int ii = (i == null ? 1 : i + 1);
        kk.put(kmId0, ii);
        return (kmId0 * KmsHelper.prop_keyid_kmid + ii);
    }

    public int kmId(int ckeyId, int kmId0)
    {
        if (ckeyId < 1 || kmId0 < 1)
            return 0;
        if (km == null)
            km = new HashMap<Integer, KmsIdBuilder>();
        KmsIdBuilder builder = km.get(ckeyId);
        int index0 = (kmId0 % KmsHelper.prop_kmid_ckeyid), bit = (index0 % 10);
        int index = (builder == null ? index0 - bit + 1 : (builder.nextIndex0 == index0 || bit != 1 ? builder.index + 1 : index0));
        if (builder == null)
        {
            builder = new KmsIdBuilder();
            km.put(ckeyId, builder);
        }
        builder.index = index;
        builder.nextIndex0 = index0 + 1;
        return (ckeyId * KmsHelper.prop_kmid_ckeyid + index);
    }
}
