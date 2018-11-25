/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arma.web.service.bean.TFinance;
import com.arma.web.service.bean.TKeyenum;
import com.arma.web.service.bean.TKeyrank;
import com.arma.web.service.bean.TKeytype;
import com.arma.web.service.bean.TKeyword;
import com.arma.web.service.bean.TKnowkey;
import com.arma.web.service.bean.TKnowledge;
import com.neulion.iptv.web.service.BaseDaoService;
import com.neulion.iptv.web.util.WebUtil;

public class TKmsDaoService extends BaseDaoService
{
    // nameCn, nameEn, demoCn, demoEn, updateDate
    public boolean insertKeytype(TKeytype bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertKeytypes(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertKeytypes(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_keytype, params);
        return (rets != null);
    }

    private static final String insert_t_keytype =
            "insert into t_keytype (name_cn, name_en, demo_cn, demo_en, update_date) values (:nameCn, :nameEn, :demoCn, :demoEn, :updateDate) ";

    // nameCn, nameEn, demoCn, demoEn, updateDate
    public boolean updateKeytype(TKeytype bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateKeytypes(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateKeytypes(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_keytype, params);
        return (rets != null);
    }

    private static final String update_t_keytype =
            "update t_keytype set name_cn=:nameCn, name_en=:nameEn, demo_cn=:demoCn, demo_en=:demoEn, update_date=:updateDate where type_id=:typeId ";

    public boolean deleteKeytype(int typeId)
    {
        if (typeId < 1)
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("typeId", typeId);
        return deleteKeytypes(WebUtil.params(map));
    }

    public boolean deleteKeytypes(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_keytype, params);
        return (rets != null);
    }

    private static final String delete_t_keytype = "delete from t_keytype where type_id=:typeId ";

    // nameCn, nameEn, demoCn, demoEn, updateDate
    public List<TKeytype> getKeytypes(String cond)
    {
        List<TKeytype> list = new ArrayList<TKeytype>();
        String sql = select_t_keytype;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TKeytype().fromDbMap(TKeytype.class, result));
        return list;
    }

    private static final String select_t_keytype =
            "select type_id as typeId, name_cn as nameCn, name_en as nameEn, demo_cn as demoCn, demo_en as demoEn, update_date as updateDate from t_keytype where ${cond} ";

    // keyId, enumVal, updateDate
    public boolean insertKeyenum(TKeyenum bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertKeyenums(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertKeyenums(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_keyenum, params);
        return (rets != null);
    }

    private static final String insert_t_keyenum = "insert into t_keyenum (key_id, enum_val, update_date) values (:keyId, :enumVal, :updateDate) ";

    // keyId, enumVal, updateDate
    public boolean updateKeyenum(TKeyenum bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateKeyenums(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateKeyenums(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_keyenum, params);
        return (rets != null);
    }

    private static final String update_t_keyenum = "update t_keyenum set enum_val=:enumVal, update_date=:updateDate where key_id=:keyId ";

    public boolean deleteKeyenum(int keyId)
    {
        if (keyId < 1)
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("keyId", keyId);
        return deleteKeyenums(WebUtil.params(map));
    }

    public boolean deleteKeyenums(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_keyenum, params);
        return (rets != null);
    }

    private static final String delete_t_keyenum = "delete from t_keyenum where key_id=:keyId ";

    // keyId, enumVal, updateDate
    public List<TKeyenum> getKeyenums(String cond)
    {
        List<TKeyenum> list = new ArrayList<TKeyenum>();
        String sql = select_t_keyenum;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TKeyenum().fromDbMap(TKeyenum.class, result));
        return list;
    }

    private static final String select_t_keyenum = "select key_id as keyId, enum_val as enumVal, update_date as updateDate from t_keyenum where ${cond} ";

    // content, nameCn, nameEn, typeId, asEnum, updateDate
    public boolean insertKeyword(TKeyword bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertKeywords(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertKeywords(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_keyword, params);
        return (rets != null);
    }

    private static final String insert_t_keyword =
            "insert into t_keyword (content, name_cn, name_en, type_id, as_enum, update_date) values (:content, :nameCn, :nameEn, :typeId, :asEnum, :updateDate) ";

    // content, nameCn, nameEn, typeId, asEnum, updateDate
    public boolean updateKeyword(TKeyword bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateKeywords(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateKeywords(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_keyword, params);
        return (rets != null);
    }

    private static final String update_t_keyword =
            "update t_keyword set content=:content, name_cn=:nameCn, name_en=:nameEn, type_id=:typeId, as_enum=:asEnum, update_date=:updateDate where key_id=:keyId ";

    public boolean deleteKeyword(int keyId)
    {
        if (keyId < 1)
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("keyId", keyId);
        return deleteKeywords(WebUtil.params(map));
    }

    public boolean deleteKeywords(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_keyword, params);
        return (rets != null);
    }

    private static final String delete_t_keyword = "delete from t_keyword where key_id=:keyId ";

    // content, nameCn, nameEn, typeId, asEnum, updateDate
    public List<TKeyword> getKeywords(String cond)
    {
        List<TKeyword> list = new ArrayList<TKeyword>();
        String sql = select_t_keyword;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TKeyword().fromDbMap(TKeyword.class, result));
        return list;
    }

    private static final String select_t_keyword =
            "select key_id as keyId, content as content, name_cn as nameCn, name_en as nameEn, type_id as typeId, as_enum as asEnum, update_date as updateDate from t_keyword where ${cond} ";

    // keyId, rank, updateDate
    public boolean insertKeyrank(TKeyrank bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertKeyranks(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertKeyranks(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_keyrank, params);
        return (rets != null);
    }

    private static final String insert_t_keyrank = "insert into t_keyrank (key_id, rank, update_date) values (:keyId, :rank, :updateDate) ";

    // keyId, rank, updateDate
    public boolean updateKeyrank(TKeyrank bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateKeyranks(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateKeyranks(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_keyrank, params);
        return (rets != null);
    }

    private static final String update_t_keyrank = "update t_keyrank set rank=:rank, update_date=:updateDate where key_id=:keyId ";

    public boolean deleteKeyrank(int keyId)
    {
        if (keyId < 1)
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("keyId", keyId);
        return deleteKeyranks(WebUtil.params(map));
    }

    public boolean deleteKeyranks(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_keyrank, params);
        return (rets != null);
    }

    private static final String delete_t_keyrank = "delete from t_keyrank where key_id=:keyId ";

    // keyId, rank, updateDate
    public List<TKeyrank> getKeyranks(String cond)
    {
        List<TKeyrank> list = new ArrayList<TKeyrank>();
        String sql = select_t_keyrank;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TKeyrank().fromDbMap(TKeyrank.class, result));
        return list;
    }

    private static final String select_t_keyrank = "select key_id as keyId, rank as rank, update_date as updateDate from t_keyrank where ${cond} ";

    // kmUuid, ckeyId, content, updateDate
    public boolean insertKnowledge(TKnowledge bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertKnowledges(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertKnowledges(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_knowledge, params);
        return (rets != null);
    }

    private static final String insert_t_knowledge = "insert into t_knowledge (km_uuid, ckey_id, content, update_date) values (:kmUuid, :ckeyId, :content, :updateDate) ";

    // kmUuid, ckeyId, content, updateDate
    public boolean updateKnowledge(TKnowledge bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateKnowledges(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateKnowledges(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_knowledge, params);
        return (rets != null);
    }

    private static final String update_t_knowledge = "update t_knowledge set km_uuid=:kmUuid, ckey_id=:ckeyId, content=:content, update_date=:updateDate where km_id=:kmId ";

    public boolean deleteKnowledge(int kmId)
    {
        if (kmId < 1)
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("kmId", kmId);
        return deleteKnowledges(WebUtil.params(map));
    }

    public boolean deleteKnowledges(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_knowledge, params);
        return (rets != null);
    }

    private static final String delete_t_knowledge = "delete from t_knowledge where km_id=:kmId ";

    // kmUuid, ckeyId, content, updateDate
    public List<TKnowledge> getKnowledges(String cond)
    {
        List<TKnowledge> list = new ArrayList<TKnowledge>();
        String sql = select_t_knowledge;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TKnowledge().fromDbMap(TKnowledge.class, result));
        return list;
    }

    private static final String select_t_knowledge =
            "select km_id as kmId, km_uuid as kmUuid, ckey_id as ckeyId, content as content, update_date as updateDate from t_knowledge where ${cond} ";

    // kmId, keyId, keyVal, updateDate
    public boolean insertKnowkey(TKnowkey bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertKnowkeys(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertKnowkeys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_knowkey, params);
        return (rets != null);
    }

    private static final String insert_t_knowkey = "insert into t_knowkey (km_id, key_id, key_val, update_date) values (:kmId, :keyId, :keyVal, :updateDate) ";

    // kmId, keyId, keyVal, updateDate
    public boolean updateKnowkey(TKnowkey bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateKnowkeys(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateKnowkeys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_knowkey, params);
        return (rets != null);
    }

    private static final String update_t_knowkey = "update t_knowkey set km_id=:kmId, key_id=:keyId, key_val=:keyVal, update_date=:updateDate where id=:id ";

    public boolean deleteKnowkey(int id)
    {
        if (id < 1)
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        return deleteKnowkeys(WebUtil.params(map));
    }

    public boolean deleteKnowkeys(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_knowkey, params);
        return (rets != null);
    }

    private static final String delete_t_knowkey = "delete from t_knowkey where id=:id ";

    // kmId, keyId, keyVal, updateDate
    public List<TKnowkey> getKnowkeys(String cond)
    {
        List<TKnowkey> list = new ArrayList<TKnowkey>();
        String sql = select_t_knowkey;
        if (WebUtil.empty(cond))
            cond = "1=1";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TKnowkey().fromDbMap(TKnowkey.class, result));
        return list;
    }

    private static final String select_t_knowkey = "select id as id, km_id as kmId, key_id as keyId, key_val as keyVal, update_date as updateDate from t_knowkey where ${cond} ";

    public int count(int[] counts)
    {
        return (counts == null ? -1 : getCount(counts));
    }

    public int updateKm(TKnowledge bean, int kmId0)
    {
        if (bean == null || bean.empty() || kmId0 < 1)
            return 0;
        Map<String, Object> map = bean.toDbMap();
        map.put("kmId0", kmId0);
        return count(batchUpdate(update_t_knowledge_km, WebUtil.params(map)));
    }

    public int updateKmId(int kmId, int kmId0)
    {
        if (kmId < 1 || kmId0 < 1)
            return 0;
        Map<String, Object> map = WebUtil.param("kmId", kmId);
        map.put("kmId0", kmId0);
        return count(batchUpdate(update_t_knowledge_kmid, WebUtil.params(map)));
    }

    public int updateKmDates(int minutes, String cond)
    {
        if (minutes == 0 || WebUtil.empty(cond))
            return 0;
        // update t_knowledge set update_date=date_add(update_date, interval 1 minute) where km_id >= 22101 and km_id <= 22101
        String sql = WebUtil.substituteParam("cond", cond, update_t_knowledge_date1);
        sql = WebUtil.substituteParam("datefunc", "date_" + (minutes > 0 ? "add" : "sub") + "(update_date, interval " + Math.abs(minutes) + " minute)", sql);
        int[] rets = batchUpdate(sql, null);
        return count(rets);
    }

    public int updateKmDates(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return 0;
        return count(batchUpdate(update_t_knowledge_date2, params));
    }

    private static final String update_t_knowledge_km = "update t_knowledge set km_id=:kmId, km_uuid=:kmUuid, ckey_id=:ckeyId, content=:content, "
            + "update_date=:updateDate where km_id=:kmId0 ";
    private static final String update_t_knowledge_kmid = "update t_knowledge set km_id=:kmId where km_id=:kmId0 ";
    private static final String update_t_knowledge_date1 = "update t_knowledge set update_date=${datefunc} where {cond} ";
    private static final String update_t_knowledge_date2 = "update t_knowledge set update_date=:updateDate where km_id=:kmId ";

    public List<Integer> getKmIds(boolean tkk, String cond)
    {
        if (WebUtil.empty(cond))
            return new ArrayList<Integer>();
        String sql = WebUtil.substituteParam("cond", cond, (tkk ? select_t_knowkey_kmids : select_t_knowledge_kmids));
        return ids("kmId", sql);
    }

    public List<Integer> getKmIds(int ckeyId, String tkkCond)
    {
        if (ckeyId < 1)
            return new ArrayList<Integer>();
        if (WebUtil.empty(tkkCond))
            return getKmIds(false, "ckey_id=" + ckeyId + " order by km_id ");
        String cond = "tkm.ckey_id=" + ckeyId + " and (" + tkkCond + ")";
        String sql = WebUtil.substituteParam("cond", cond, select_t_kmkey_kmids);
        return ids("kmId", sql);
    }

    private static final String select_t_knowledge_kmids = "select km_id as kmId from t_knowledge where ${cond} ";
    private static final String select_t_knowkey_kmids = "select distinct km_id as kmId from t_knowkey where ${cond} ";
    private static final String select_t_kmkey_kmids = "select distinct tkk.km_id as kmId from t_knowkey tkk left join t_knowledge tkm on tkk.km_id=tkm.km_id where ${cond} ";

    public int updateKeyIds(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return 0;
        return count(batchUpdate(update_t_knowkey_kmkeyid, params));
    }

    public int updateKeyKmId(int kmId, int kmId0)
    {
        if (kmId < 1 || kmId0 < 1)
            return 0;
        Map<String, Object> map = WebUtil.param("kmId", kmId);
        map.put("kmId0", kmId0);
        return count(batchUpdate(update_t_knowkey_kmid, WebUtil.params(map)));
    }

    public int updateKeyDates(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return 0;
        return count(batchUpdate(update_t_knowkey_date, params));
    }

    private static final String update_t_knowkey_kmkeyid = "update t_knowkey set id=:id, km_id=:kmId where id=:id0 ";
    private static final String update_t_knowkey_kmid = "update t_knowkey set km_id=:kmId where km_id=:kmId0 ";
    private static final String update_t_knowkey_date = "update t_knowkey set update_date=:updateDate where km_id=:kmId ";

    public int deleteKeys(int kmId)
    {
        if (kmId < 1)
            return 0;
        return count(batchUpdate(delete_t_knowkey_kmid, WebUtil.params(WebUtil.param("kmId", kmId))));
    }

    public int deleteKeys(int keyId, int kmId)
    {
        if (keyId < 1 || kmId < 1)
            return 0;
        Map<String, Object> map = WebUtil.param("kmId", kmId);
        map.put("keyId", keyId);
        return count(batchUpdate(delete_t_knowkey_kmkey, WebUtil.params(map)));
    }

    private static final String delete_t_knowkey_kmid = "delete from t_knowkey where km_id=:kmId ";
    private static final String delete_t_knowkey_kmkey = "delete from t_knowkey where key_id=:keyId and km_id=:kmId ";

    // kmId, keyId, keyVal, updateDate
    public List<Map<String, Object>> getKeys(String cond)
    {
        if (WebUtil.empty(cond))
            return new ArrayList<Map<String, Object>>();
        return query(WebUtil.substituteParam("cond", cond, select_t_knowkey));
    }

    public List<Map<String, Object>> getKeyKmIds(String cond)
    {
        if (WebUtil.empty(cond))
            return new ArrayList<Map<String, Object>>();
        String sql = WebUtil.substituteParam("cond", cond, select_t_knowkey_kmkeyid);
        return query(sql);
    }

    public List<Integer> getKeyIds(String cond)
    {
        if (WebUtil.empty(cond))
            return new ArrayList<Integer>();
        return ids("id", WebUtil.substituteParam("cond", cond, select_t_knowkey));
    }

    public List<Integer> getKeyIds2(String cond)
    {
        if (WebUtil.empty(cond))
            return new ArrayList<Integer>();
        return ids("id", WebUtil.substituteParam("cond", cond, select_t_knowkey_keyid));
    }

    private static final String select_t_knowkey_kmkeyid = "select id as id0, km_id as kmId from t_knowkey where ${cond} ";
    private static final String select_t_knowkey_keyid = "select tkk.id as id from t_knowkey tkk left join t_knowledge tkm on tkk.km_id=tkm.km_id where ${cond} ";

    private List<Integer> ids(String key, String sql)
    {
        List<Integer> rets = new ArrayList<Integer>();
        List<Map<String, Object>> results = query(sql);
        for (int i = 0; i < results.size(); i++)
            rets.add((Integer) results.get(i).get(key));
        return rets;
    }

    public int alterIdBase(int idBase, String tbl)
    {
        if (idBase < 1 || WebUtil.empty(tbl))
            return 0;
        return count(batchUpdate("alter table " + tbl + " auto_increment=" + idBase, null));
    }

    // year, gdp, population, agdp, outlaysTotal, outlaysCentral, defenseTotal, defenseCentral, usaGdp, usaPopulation, usaAgdp, usaOutlaysTotal, usaDefenseTotal, exchangeUsd
    public boolean insertFinance(TFinance bean)
    {
        if (bean == null || bean.empty())
            return true;
        return insertFinances(WebUtil.params(bean.toDbMap()));
    }

    public boolean insertFinances(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(insert_t_finance, params);
        return (rets != null);
    }

    private static final String insert_t_finance =
            "insert into t_finance (year, gdp, population, agdp, outlays_total, outlays_central, defense_total, defense_central, usa_gdp, usa_population, usa_agdp, usa_outlays_total, usa_defense_total, exchange_usd) values (:year, :gdp, :population, :agdp, :outlaysTotal, :outlaysCentral, :defenseTotal, :defenseCentral, :usaGdp, :usaPopulation, :usaAgdp, :usaOutlaysTotal, :usaDefenseTotal, :exchangeUsd) ";

    // year, gdp, population, agdp, outlaysTotal, outlaysCentral, defenseTotal, defenseCentral, usaGdp, usaPopulation, usaAgdp, usaOutlaysTotal, usaDefenseTotal, exchangeUsd
    public boolean updateFinance(TFinance bean)
    {
        if (bean == null || bean.empty())
            return true;
        return updateFinances(WebUtil.params(bean.toDbMap()));
    }

    public boolean updateFinances(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(update_t_finance, params);
        return (rets != null);
    }

    private static final String update_t_finance =
            "update t_finance set year=:year, gdp=:gdp, population=:population, agdp=:agdp, outlays_total=:outlaysTotal, outlays_central=:outlaysCentral, defense_total=:defenseTotal, defense_central=:defenseCentral, usa_gdp=:usaGdp, usa_population=:usaPopulation, usa_agdp=:usaAgdp, usa_outlays_total=:usaOutlaysTotal, usa_defense_total=:usaDefenseTotal, exchange_usd=:exchangeUsd where id=:id ";

    public boolean deleteFinance(int id)
    {
        if (id < 1)
            return true;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        return deleteFinances(WebUtil.params(map));
    }

    public boolean deleteFinances(List<Map<String, Object>> params)
    {
        if (params == null || params.size() < 1)
            return true;
        int[] rets = batchUpdate(delete_t_finance, params);
        return (rets != null);
    }

    private static final String delete_t_finance = "delete from t_finance where id=:id ";

    // year, gdp, population, agdp, outlaysTotal, outlaysCentral, defenseTotal, defenseCentral, usaGdp, usaPopulation, usaAgdp, usaOutlaysTotal, usaDefenseTotal, exchangeUsd
    public List<TFinance> getFinances(String cond)
    {
        List<TFinance> list = new ArrayList<TFinance>();
        String sql = select_t_finance;
        if (WebUtil.empty(cond))
            cond = "1=1 order by year";
        sql = WebUtil.substituteParam("cond", cond, sql);
        List<Map<String, Object>> results = query(sql);
        for (Map<String, Object> result : results)
            list.add(new TFinance().fromDbMap(TFinance.class, result));
        return list;
    }

    private static final String select_t_finance =
            "select id as id, year as year, gdp as gdp, population as population, agdp as agdp, outlays_total as outlaysTotal, outlays_central as outlaysCentral, defense_total as defenseTotal, defense_central as defenseCentral, usa_gdp as usaGdp, usa_population as usaPopulation, usa_agdp as usaAgdp, usa_outlays_total as usaOutlaysTotal, usa_defense_total as usaDefenseTotal, exchange_usd as exchangeUsd from t_finance where ${cond} ";

}
