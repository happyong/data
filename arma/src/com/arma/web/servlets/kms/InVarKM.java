/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms;

public class InVarKM
{
    public static final String[] attrs_keytype = new String[]{"typeId", "nameCn", "nameEn", "demoCn", "demoEn", "updateDate"};
    public static final String[] attrs_keyenum = new String[]{"keyId", "enumVal", "updateDate"};
    public static final String[] attrs_keyword = new String[]{"keyId", "content", "nameCn", "nameEn", "typeId", "asEnum", "updateDate"};
    public static final String[] attrs_keyrank = new String[]{"keyId", "rank", "updateDate"};
    public static final String[] attrs_knowledge = new String[]{"kmId", "kmUuid", "ckeyId", "content", "updateDate"};
    public static final String[] attrs_knowkey = new String[]{"kmId", "keyId", "keyVal", "updateDate"};
    public static final String[] attrs_finance = new String[]{"id", "year", "gdp", "population", "agdp", "outlaysTotal", "outlaysCentral", "defenseTotal", "defenseCentral", "usaGdp", "usaPopulation", "usaAgdp", "usaOutlaysTotal", "usaDefenseTotal", "exchangeUsd"};

    public static final String[] attrs_km = new String[]{"kmId", "ckeyId", "content", "keys", "updateDate"};
    public static final String[] attrs_ckeyId = new String[]{"ckeyId"};
    public static final String[] attrs_ckeys = new String[]{"ckeys"};
    public static final String[] attrs_count = new String[]{"count"};
}

