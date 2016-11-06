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
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.arma.web.service.TKmsDaoService;
import com.arma.web.service.bean.TKeyword;
import com.arma.web.service.bean.TKnowkey;
import com.arma.web.service.bean.TKnowledge;
import com.arma.web.util.InVarAM;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.SkyUtil;
import com.neulion.iptv.web.util.WebUtil;

public class KmsHelper
{
    private static final Logger _logger = Logger.getLogger(KmsHelper.class);
    
    private static final int wd_short = 60;
    private static final int wd_comm = 136;
    private static final int wd_comm2 = 180;
    private static final int wd_long = 720;
    public static final int prop_keyid_kmid = 100;
    public static final int prop_kmid_ckeyid = 10000;
    public static final String key_fail = "503 - ";
    
    public static List<Knowledge> listKm(KmsCond cond)
    {
        if (cond == null || cond.empty()) return new ArrayList<Knowledge>();
        
        List<Integer> kmIds = null;
        int ckeyId = cond.getCkey().getTkey().getKeyId();
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        if (cond.getKeys().length < 1) kmIds = dao.getKmIds(ckeyId, null);
        else
        {
            Keyword[] keys = cond.getKeys();
            String[] bases = cond.getBases();
            for (int ii = 0; ii < keys.length; ii++)
            {
                TKeyword tk = keys[ii].getTkey();
                String tkkCond = "tkk.key_id=" + tk.getKeyId() + " and ";
                if (mkey(tk))
                {
                    String[] arr2 = bases[ii].split(WebUtil.sep_kenum);
                    tkkCond += (arr2.length > 1 ? "(" : "") + keyCond(false, arr2[0]);
                    for (int i = 1; i < arr2.length; i++) tkkCond += " or " + keyCond(false, arr2[i]);
                    tkkCond += (arr2.length > 1 ? ")" : "");
                }
                else tkkCond += keyCond(dkey(tk.getTypeId()), bases[ii]);
                kmIds = and(kmIds, dao.getKmIds(ckeyId, tkkCond));
                if (kmIds.size() < 1) break;
            }
        }
        List<Knowledge> kms = getKms(kmIds);
        if (cond.getSkey() != null)
        {
            TKeyword stk = cond.getSkey().getTkey();
            Collections.sort(kms, new KmComparator(cond.isDesc(), ikey(stk), stk.getKeyId()));
        }
        return kms;
    }
    
    public static List<Map<String, Object>> listKm4Win(KmsCond cond, ResourceBundle bundle)
    {
        List<Knowledge> kms = listKm(cond);
        if (kms == null || kms.size() < 1) return new ArrayList<Map<String, Object>>();
        
        int sortId = (cond.getSkey() == null ? 0 : cond.getSkey().getTkey().getKeyId()), index;
        boolean supdk = updk(sortId);
        String ckeys = cond.getCkey().getTkey().getContent(), none = "-"; //bundle.getString("label_none");
        String[] arr = (WebUtil.empty(ckeys) ? new String[0] : ckeys.split(WebUtil.sep_kval));
        List<Integer> show = new ArrayList<Integer>();
        List<Keyword> list = new ArrayList<Keyword>();
        for (Knowledge km : kms) if (km.getTkks() != null) for (TKnowkey tkk : km.getTkks()) if (!show.contains(tkk.getKeyId())) show.add(tkk.getKeyId());
        for (String str : arr) if (show.contains(WebUtil.str2int(str))) list.add(KmsCacher.getKey(WebUtil.str2int(str)));
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Knowledge km : kms) 
        {
            index = 1;
            TKnowledge tkm = km.getTkm();
            List<TKnowkey> tkks = km.getTkks();
            Map<String, Object> tr = SkyUtil.tr(index++, 80, bundle.getString("label_kmId"), tkm.getKmId());
            SkyUtil.td(index++, 150, bundle.getString("label_content"), tkm.getContent(), tr);
            if (supdk) 
            {
                SkyUtil.td(index++, 100, bundle.getString("label_updateDate"), DateUtil.str(tkm.getUpdateDate()), tr);
                for (Keyword key : list) list(index++, none, ckeys, key, tkks, tr);
            }
            else
            {
                list(index++, none, ckeys, cond.getSkey(), tkks, tr);
                for (Keyword key : list) if (sortId != key.getTkey().getKeyId()) list(index++, none, ckeys, key, tkks, tr);
                SkyUtil.td(index++, 100, bundle.getString("label_updateDate"), DateUtil.str(tkm.getUpdateDate()), tr);
            }
            ret.add(tr);
        }
        return ret;
    }
    
    private static void list(int index, String none, String ckeys, Keyword key, List<TKnowkey> tkks, Map<String, Object> tr)
    {
        if (key == null) return;
        int width = width(key.getTkey());
        String ret = "", left = (width < 240 ? "" : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        if (tkks == null) 
        {
            SkyUtil.td(index, width, key.name(), left + none, tr);
            return;
        }
        
        boolean nomkey = !mkey(key.getTkey());
        Collections.sort(tkks, new KmKeysComparator(ckeys));
        for (TKnowkey tkk : tkks) 
        {
            if (tkk.getKeyId() != key.getTkey().getKeyId()) continue;
            ret += (ret.length() == 0 ? "" : "<br/>") + left + tkk.getKeyVal(); 
            if (nomkey) break;
        }
        SkyUtil.td(index, width, key.name(), (WebUtil.empty(ret) ? left + none : ret), tr);
    }

    private static String keyCond(boolean dkey, String val)
    {
        if (!dkey) return "tkk.key_val like '" + val + "%'";
        String[] arr = val.split(WebUtil.sep_kval, 2);
        return "(tkk.key_val between '" + arr[0] +"' and '" + (WebUtil.empty(arr[1]) ? DateUtil.str2(new Date()) : arr[1]) + "')";
    }
    
    // base can be null, and should not be null
    private static List<Integer> and(List<Integer> base, List<Integer> and)
    {
        if (base == null || and.size() < 1) return and;
        for (int i = base.size() - 1; i >= 0; i--) if (!and.contains(base.get(i))) base.remove(i);
        return base;
    }
    
    private static List<Knowledge> getKms(List<Integer> kmIds)
    {
        List<Knowledge> ret = new ArrayList<Knowledge>();
        if (kmIds == null || kmIds.size() < 1) return ret;
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        String str = "";
        for (int kmId : kmIds) str += "," + kmId;
        str = str.substring(1);
        List<TKnowledge> tkms = dao.getKnowledges("km_id in (" + str + ") order by km_id ");
        if (tkms == null || tkms.size() < 1) return ret;
        Map<Integer, KmKeysComparator> map = new HashMap<Integer, KmKeysComparator>();
        for (TKnowledge tkm : tkms)
        {
            Knowledge km = new Knowledge();
            km.setTkm(tkm);
            ret.add(km);
            int ckeyId = tkm.getCkeyId();
            if (!map.containsKey(ckeyId)) map.put(ckeyId, new KmKeysComparator(KmsCacher.getKey(ckeyId).getTkey().getContent()));
        }
        
        int ckmId = -1, kmId;
        Knowledge ckm = null, km;
        List<TKnowkey> tkks = dao.getKnowkeys("km_id in (" + str + ") order by km_id, id"), tkks0;
        for (TKnowkey tkk : tkks)
        {
            kmId = tkk.getKmId();
            boolean scan = (ckmId < 1 || ckmId != kmId);
            km = (scan ? scan0(kmId, ret) : ckm);
            ckmId = kmId;
            if (scan) ckm = km;
            if (ckm == null) continue;
            tkks0 = ckm.getTkks();
            if (tkks0 == null)
            {
                tkks0 = new ArrayList<TKnowkey>();
                ckm.setTkks(tkks0);
            }
            tkks0.add(tkk);
        }
        for (Knowledge bean : ret) if (bean.getTkks() != null && map.containsKey(bean.getTkm().getCkeyId())) Collections.sort(bean.getTkks(), map.get(bean.getTkm().getCkeyId()));
        return ret;
    }
    
    private static Knowledge scan0(int kmId, List<Knowledge> kms)
    {
        if (kmId < 1 || kms == null || kms.size() < 1) return null;
        for (Knowledge km : kms) if (km.getTkm().getKmId() == kmId) return km;
        return null;
    }
    
    public static Knowledge getKm(int kmId)
    {
        if (kmId < 1) return null;
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<TKnowledge> tkms = dao.getKnowledges("km_id=" + kmId + " order by update_date desc");
        if (tkms == null || tkms.size() < 1) return null;
        Knowledge ret = new Knowledge();
        ret.setTkm(tkms.get(0));
        List<TKnowkey> tkks = dao.getKnowkeys("km_id=" + kmId + " order by key_id");
        if (tkks != null && tkks.size() > 0) ret.setTkks(tkks);
        return ret;
    }
    
    // update or create t_knowledge and t_knowkey records
    public static int updateKm(boolean onlyKeys, Date now, Knowledge km)
    {
        int ret = 0;
        if (km == null || km.empty()) return ret;
        
        km.updateDate(now);
        int kmId = km.getTkm().getKmId();
        boolean b1 = true, b2 = true, b3 = true;
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        if (kmId < 1)
        {
            b1 = dao.insertKnowledge(km.getTkm());
            List<TKnowledge> list = dao.getKnowledges("km_uuid='" + km.getTkm().getKmUuid() + "'");
            if (!b1 || list == null || list.size() < 1) return 0;
            kmId = list.get(0).getKmId();
            km.setKmId(kmId);
        }
        else 
        {
            if (!onlyKeys) b1 = dao.updateKnowledge(km.getTkm());
            b2 = b1 && (dao.deleteKeys(kmId) >= 0);
        }
        if (b2) ret++;
        if (km.getTkks() != null && b2)
        {
            List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
            for (TKnowkey bean : km.getTkks()) tkk(bean, params);
            b3 = dao.insertKnowkeys(params);
            ret += params.size();
            if (!b3) 
            {
                ret = 0;
                StringBuffer sb = new StringBuffer();
                writesql(kmId, params, sb);
                if (sb.length() > 0)  _logger.info("error while insert into database" + WebUtil.LINE_WIN + sb.toString());
            }
        }
        return ret;
    }
    
    public static int deleteKm(int kmId)
    {
        if (kmId < 1) return 0;
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        boolean ret = dao.deleteKnowledge(kmId);
        int count = dao.deleteKeys(kmId);
        return (ret && count >= 0 ? kmId : 0);
    }

    // update t_knowledge and t_knowkey with specified kmId list - id move
    // update t_knowkey with specified kmId list - add new key, update tags, update all keys to make the id together and sort
    private static String updateKmKeys(int kmIdBase, boolean fmtKeys, List<TKnowkey> newKeys, List<Integer> kmIds)
    {
        List<Knowledge> kms = getKms(kmIds);
        if (kms == null || kms.size() < 1) return resp(null, false, null, "km not found");
        boolean nokeys = (newKeys == null || newKeys.size() < 1), fmtKm = (kmIdBase < 0);
        int idmove = ((nokeys || fmtKeys) && kmIdBase > 0 ? kmIdBase - kms.get(0).getTkm().getKmId() : 0), total = 0;
        if (!fmtKeys && idmove == 0 && nokeys) return resp(null, false, null, "no idmove and no fmtkeys and no newkeys");

        String failKmIds = "";
        StringBuffer sb = new StringBuffer();
        KmsIdBuilder builder = (fmtKm ? new KmsIdBuilder() : null);
        Date date = new Date(System.currentTimeMillis() - (kms.size() / 2) * 6000);
        List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        for (Knowledge km : kms)
        {
            params.clear();
            date.setTime(date.getTime() + 4000 + WebUtil.random(4000));
            int kmId0 = km.getTkm().getKmId(), kmId = kmId0 + idmove, ckeyId = km.getTkm().getCkeyId(), count = 0;
            if (fmtKeys)
            {
                if (!nokeys) 
                {
                    if (km.getTkks() == null) km.setTkks(new ArrayList<TKnowkey>());
                    for (TKnowkey key : newKeys) km.getTkks().add(key.copyit());
                    Collections.sort(km.getTkks(), new KmKeysComparator(KmsCacher.getKey(ckeyId).getTkey().getContent()));
                }
                String content = content(false, km.getTkm(), km.getTkks());
                if (builder != null) kmId = builder.kmId(0 - kmIdBase, kmId0);
                km.setKmId(kmId);
                km.updateDate(date);
                km.getTkm().setKmUuid("");
                km.getTkm().setContent(content);
                boolean b1 = (dao.updateKm(km.getTkm(), kmId0) >= 0), b2 = b1 && (dao.deleteKeys(kmId0) >= 0), b3 = b2;
                if (km.getTkks() != null && b2) 
                {
                    for (TKnowkey bean : km.getTkks()) tkk(bean, params);
                    b3 = dao.insertKnowkeys(params);
                    if (!b3) writesql(kmId0, params, sb);
                    count = params.size();
                    total += count;
                }
                if (!b3) 
                {
                    failKmIds += ", " + kmId0 + "¡ú" + kmId;
                    if (builder != null) break;
                }
                _logger.info("result - kmId0|" + kmId0 + "|kmId|" + kmId + "|fmtKeys|" + fmtKeys + "|kmidmove|" + b1 + "|keydel|" + b2 + "|keynew|" + b3 + "|" + count);
                continue;
            }
            boolean b1 = true, b2 = true;
            if (idmove != 0)
            {
                b1 = (dao.updateKmId(kmId, kmId0) >= 0);
                b2 = b1 && (dao.updateKeyKmId(kmId, kmId0) >= 0);
            }
            boolean b3 = b2;
            if (!nokeys && b2) 
            {
                for (TKnowkey key : newKeys) 
                {
                    Map<String, Object> map = tkk(key, params);
                    map.put("kmId", kmId);
                    map.put("updateDate", date);
                }
                b3 = dao.insertKnowkeys(params);
                if (!b3) writesql(kmId0, params, sb);
                count = params.size();
                total += count;
            }
            if (!b3) failKmIds += ", " + kmId0 + ":" + kmId;
            _logger.info("result - kmId0|" + kmId0 + "|kmId|" + kmId + "|fmtKeys|" + fmtKeys + "|kmidmove|" + b1 + "|keyidmove|" + b2 + "|keynew|" + b3 + "|" + count);
        }
        if (!fmtKm) dao.alterIdBase(tkknext(dao), "t_knowkey");
        boolean pass = (failKmIds.length() == 0);
        if (sb.length() > 0)  _logger.info("failed sql statement list" + WebUtil.LINE_WIN + sb.toString());
        return resp(null, pass, null, pass ? "" + total : (builder == null ? "failKmIds|" + failKmIds.substring(2) : ""));
    }
    
    private static Map<String, Object> tkk(TKnowkey bean, List<Map<String, Object>> params)
    {
        Map<String, Object> map = bean.toDbMap();
        params.add(map);
        Keyword key = KmsCacher.getKey(bean.getKeyId());
        if (key != null && !key.empty() && key.getTkey().isAsEnum()) KmsCacher.updateKenum(true, bean.getKeyId(), bean.getKeyVal());
        return map;
    }
    
    private static void writesql(int kmId, List<Map<String, Object>> params, StringBuffer sb)
    {
        sb.append("kmId - ").append(kmId).append(WebUtil.LINE_WIN);
        for (Map<String, Object> map : params) sb.append("insert into t_knowkey (km_id, key_id, key_val, update_date) values (").append(map.get("kmId")).append(", ")
            .append(map.get("keyId")).append(", '").append(map.get("keyVal")).append("', '").append(DateUtil.str((Date)map.get("updateDate"))).append("');").append(WebUtil.LINE_WIN);
    }
    
    private static int tkknext(TKmsDaoService dao)
    {
        List<Integer> max = dao.getKeyIds("1=1 order by id desc limit 1");
        return ((max == null || max.size() < 1 ? 0 : max.get(0)) + 1); 
    }

    // new km to t_knowledge with specified base km_id, count, ckeyId
    public static String newKms(int kmIdBase, int kmCount, int ckeyId, HttpServletRequest request)
    {
        String key = "newKms", param = "kmIdBase|" + kmIdBase + "|kmCount|" + kmCount + "|ckeyId|" + ckeyId;
        if (kmIdBase < 0) return resp(key, false, param, "km id invalid");
        if (kmCount < 1) return resp(key, false, param, "km count invalid");
        Keyword ckey = KmsCacher.getKey(ckeyId);
        if (ckey == null || ckey.empty()) return resp(key, false, param, "ckey not found");

        long now = System.currentTimeMillis();
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        int alters = dao.alterIdBase(kmIdBase, "t_knowledge"), creates = 0;
        for (int i = 0; i < kmCount; i++)
        {
            if (updateKm(false, new Date(now), new Knowledge().fromRequest(request)) > 0) creates++;
            now += (2000 + WebUtil.random(6000));
        }
        return resp(key, true, param, "alter|" + alters + "|created|" + creates);
    }

    // format t_knowledge and t_knowkey with specified kmId list - id move, update all keys to make the id together and sort
    public static String fmtKmKeys(int kmIdBase, boolean fmtKeys, int kmIdMin, int kmIdMax)
    {
        String key = "fmtKmKeys", param = "kmIdBase|" + kmIdBase + "|fmtKeys|" + fmtKeys + "|kmIdMin|" + kmIdMin + "|kmIdMax|" + kmIdMax;
        if (kmIdMin > 0 && kmIdMax > 0 && kmIdMin > kmIdMax) return resp(key, false, param, "km id invalid");
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        String cond = kmIdCond(false, kmIdMin, kmIdMax) + " order by km_id ";
        List<Integer> kmIds = dao.getKmIds(false, cond);
        if (kmIds == null || kmIds.size() < 1) return resp(key, false, param, "km ids not found");

        String ret = updateKmKeys(kmIdBase, fmtKeys, null, kmIds);
        boolean pass = resp(ret, null);
        return resp(key, pass, param, (pass ? "updated|" + WebUtil.str2int(ret) : ret.substring(key_fail.length())));
    }

    // format all t_knowledge and t_knowkey with specified ckeyId - id move, update all keys to make the id together and sort
    public static String fmtKmKeys4ckeyId(int ckeyId)
    {
        String key = "fmtKmKeys4ckeyId", param = "ckeyId|" + ckeyId;
        Keyword ckey = KmsCacher.getKey(ckeyId);
        if (ckey == null || ckey.empty()) return resp(key, false, param, "ckey not found");
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<Integer> kmIds = dao.getKmIds(ckeyId, null);
        if (kmIds == null || kmIds.size() < 1) return resp(key, false, param, "km ids not found");
        String invalid = checkIds(true, ckeyId, dao);
        if (!WebUtil.empty(invalid)) return resp(key, false, param, "km ids should be pre-handled manually, kmIds|" + invalid);
        invalid = checkIds(false, ckeyId, dao);
        if (!WebUtil.empty(invalid)) return resp(key, false, param, "key ids should be pre-handled manually, kkIds|" + invalid);

        // max value: 2147483647
        List<Integer> list = dao.getKmIds(false, "1=1 order by km_id desc limit 1");
        int max1 = (list == null || list.size() < 1 ? 0 : list.get(0) / prop_kmid_ckeyid), max2 = KmsCacher.ckeyId(true);
        int ckeyId2 = Math.min(2140, (Math.max(max1, max2) / 10 + WebUtil.random(10) + 1) * 10);
        String ret = updateKmKeys(0 - ckeyId2, true, null, kmIds);

        int base = prop_kmid_ckeyid, min = ckeyId2 * base, max = min + base - 1, deta = (ckeyId - ckeyId2) * base;
        if (deta != 0) dao.batchUpdate("update t_knowledge set km_id = km_id + " + deta + " where (km_id between " + min + " and " + max + ") and ckey_id = " + ckeyId, null);
        fmtkkids(min, max, deta, dao);
        dao.alterIdBase(tkknext(dao), "t_knowkey");
        boolean pass = resp(ret, null);
        return resp(key, pass, param, (pass ? "updated|" + WebUtil.str2int(ret) : ret.substring(key_fail.length())));
    }
    
    private static String checkIds(boolean km, int ckeyId, TKmsDaoService dao)
    {
        int base = prop_kmid_ckeyid * (km ? 1 : prop_keyid_kmid), min = ckeyId * base, max = (ckeyId + 1) * base - 1;
        List<Integer> ids = (km ? dao.getKmIds(false, "(km_id between " + min + " and " + max + ") and ckey_id != " + ckeyId + " order by km_id") : 
            dao.getKeyIds2("(tkk.id between " + min + " and " + max + ") and tkm.ckey_id != " + ckeyId + " order by tkk.id"));
        String ret = "";
        if (ids == null || ids.size() < 1) return ret;
        for (Integer id : ids) ret += ", " + id;
        return ret.substring(2);
    }
    
    public static void fmtkkids(int min, int max, int deta, TKmsDaoService dao)
    {
        KmsIdBuilder builder = new KmsIdBuilder();
        List<Integer> id0s = new ArrayList<Integer>();
        int next = tkknext(dao);
        List<Map<String, Object>> params = dao.getKeyKmIds("km_id between " + min + " and " + max + " order by id"), second = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : params) id0s.add((Integer)map.get("id0"));
        for (Map<String, Object> map : params)
        {
            Integer kmId0 = (Integer)map.get("kmId"), id0 = (Integer)map.get("id0");
            int kmId = (kmId0 == null ? 0 : kmId0) + deta, id = builder.kkId(kmId);
            boolean conf = (id0 != id && id0s.contains(id));
            map.put("kmId", kmId);
            map.put("id", (conf ? next : id));
            if (conf)
            {
                map = WebUtil.param("id0", next);
                map.put("kmId", kmId);
                map.put("id", id);
                second.add(map);
                next++;
            }
        }
        dao.updateKeyIds(params);
        dao.updateKeyIds(second);
    }

    // format t_knowledge update_date for specified clock time
    // format t_knowledge update_date for specified minutes
    public static String fmtKmDates(String timeBase, int minutes, int kmIdMin, int kmIdMax)
    {
        String key = "fmtKmDates", param = "kmIdMin|" + kmIdMin + "|kmIdMax|" + kmIdMax;
        if (kmIdMin > 0 && kmIdMax > 0 && kmIdMin > kmIdMax) return resp(key, false, param, "km id invalid");
        
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        String cond = kmIdCond(false, kmIdMin, kmIdMax);
        if (minutes != 0)
        {
            int count = dao.updateKmDates(minutes, cond);
            return resp(key, true, "minutes|" + minutes + "|" + param, "updated|" + count);
        }
        Date date = DateUtil.date(timeBase);
        long now = (date == null ? System.currentTimeMillis() : date.getTime());
        List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
        List<TKnowledge> tkms = dao.getKnowledges(cond + " order by km_id ");
        for (TKnowledge tkm : tkms) 
        {
            Map<String, Object> map = WebUtil.param("kmId", tkm.getKmId());
            map.put("updateDate", new Date(now));
            params.add(map);
            now += (2000 + WebUtil.random(6000));
        }
        int count = dao.updateKmDates(params);
        return resp(key, true, "timeBase|" + timeBase + "|" + param, "updated|" + count);
    }

    // copy t_knowledge content from t_knowkey tags
    public static String copyKmContent(int kmIdMin, int kmIdMax)
    {
        String key = "copyKmContent", param = "kmIdMin|" + kmIdMin + "|kmIdMax|" + kmIdMax;
        if (kmIdMin > 0 && kmIdMax > 0 && kmIdMin > kmIdMax) return resp(key, false, param, "km id invalid");
        
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
        String cond = kmIdCond(false, kmIdMin, kmIdMax) + " order by km_id ";
        List<TKnowledge> tkms = dao.getKnowledges(cond);
        List<TKnowkey> tags = dao.getKnowkeys("key_id != 2001 and " + cond + " desc, id ");
        for (TKnowledge tkm : tkms)
        {
            Map<String, Object> map = tkm.toDbMap();
            map.put("kmUuid", "");
            map.put("content", content(true, tkm, tags));
            params.add(map);
        }
        boolean pass = dao.updateKnowledges(params);
        return resp(key, pass, param, (pass ? "updated|" + params.size() : "database exception"));
    }
    
    private static String content(boolean del, TKnowledge tkm, List<TKnowkey> tkks)
    {
        String content = "";
        if (tkks == null || tkks.size() < 1) return content;
        
        int ckeyId = tkm.getCkeyId();
        String rtime = "", rtype = "", raddr = "";
        for (int i = tkks.size() - 1; i >= 0; i--)
        {
            TKnowkey tkk = tkks.get(i);
            int km_id = tkk.getKmId(), key_id = tkk.getKeyId();
            if (km_id > tkm.getKmId()) break;
            else if (km_id < tkm.getKmId()) continue;
            if (del) tkks.remove(i);
            String val = tkk.getKeyVal();
            if (WebUtil.empty(val)) continue;
            if (ckeyId == 150)
            {
            	if (key_id == 1501) rtime = val;
            	else if (key_id == 1503) rtype = val;
            	else if (key_id == 1504) raddr = (val.length() > 1 ? val.substring(0, 2) : val);
            }
            else if (key_id == tagk())
            {
                if (ckeyId == 120)
                {
                    char c = val.charAt(0);
                    if (Character.isDigit(c) || Character.isUpperCase(c)) content = val;
                }
                else if (ckeyId == 130)
                {
                    if (content.length() < 1 && Character.isDigit(val.charAt(val.length() - 1))) content = val;
                }
            }
        }
        if (ckeyId == 150 && !WebUtil.empty(rtime) && !WebUtil.empty(rtype))
        {
            TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
            List<Integer> kmIds = dao.getKmIds(ckeyId, "tkk.key_id=1503 and tkk.key_val='" + rtype + "'");
            kmIds = and(kmIds, dao.getKmIds(ckeyId, "tkk.key_id=1501 and tkk.key_val<'" + rtime + "'"));
            content = (WebUtil.empty(raddr) ? "" : raddr + ", ") + rtype + "#" + WebUtil.f2s000(kmIds.size() + 1);
        }
        return content;
    }
    
    private static String kmIdCond(boolean unull, int kmIdMin, int kmIdMax)
    {
        if (unull) return "km_id >= " + kmIdMin + " and km_id <= " + kmIdMax;
        String cond = "1=1";
        if (kmIdMin > 0) cond += " and km_id >= " + kmIdMin;
        if (kmIdMax > 0) cond += " and km_id <= " + kmIdMax;
        return cond;
    }

    // add new key to t_knowkey with specified kmIdMin, kmIdMax
    public static String newKmKey(int keyId, String keyVal, boolean fmtKeys, int kmIdMin, int kmIdMax)
    {
        String key = "newKmKey", param = "keyId|" + keyId + "|keyVal|" + keyVal + "|fmtKeys|" + fmtKeys + "|kmIdMin|" + kmIdMin + "|kmIdMax|" + kmIdMax;
        Keyword kk = KmsCacher.getKey(keyId);
        if (kk == null || kk.empty()) return resp(key, false, param, "key not found");
        if (WebUtil.empty(keyVal)) return resp(key, false, param, "key val invalid");
        if (kmIdMin < 1 || kmIdMax < 1 || kmIdMin > kmIdMax) return resp(key, false, param, "km id invalid");
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        String cond = kmIdCond(true, kmIdMin, kmIdMax) + " order by km_id ";
        List<Integer> kmIds = dao.getKmIds(false, cond);
        if (kmIds == null || kmIds.size() < 1) return resp(key, false, param, "km ids not found");
        
        List<TKnowkey> newKeys = new ArrayList<TKnowkey>();
        TKnowkey bean = new TKnowkey();
        bean.setKeyId(keyId);
        bean.setKeyVal(keyVal);
        newKeys.add(bean);
        String ret = updateKmKeys(0, fmtKeys, newKeys, kmIds);
        boolean pass = resp(ret, null);
        return resp(key, pass, param, (pass ? "updated|" + WebUtil.str2int(ret) : ret.substring(key_fail.length())));
    }

    // add new key to t_knowkey with specified tags
    public static String newKmKey4Tags(int keyId, String keyVal, boolean fmtKeys, String tags)
    {
        String key = "newKmKey4Tags", param = "keyId|" + keyId + "|keyVal|" + keyVal + "|fmtKeys|" + fmtKeys + "|tags|" + tags;
        Keyword kk = KmsCacher.getKey(keyId);
        if (kk == null || kk.empty()) return resp(key, false, param, "key not found");
        if (WebUtil.empty(keyVal)) return resp(key, false, param, "key val invalid");
        if (WebUtil.empty(tags)) return resp(key, false, param, "tags invalid");
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
    	String[] arr = tags.split(InVarAM.s_sep1);
        String cond = "key_id = " + tagk() + " and (";
        for (int i = 0; i < arr.length; i++) cond += (i == 0 ? "" : " or ") + "key_val='" + (arr[i].indexOf(InVarAM.s_sep2) == -1 ? arr[i] : arr[i].substring(0, arr[i].indexOf(InVarAM.s_sep2))) + "'";
        cond += ") order by km_id ";
        List<Integer> kmIds = dao.getKmIds(true, cond);
        if (kmIds == null || kmIds.size() < 1) return resp(key, false, param, "tags not found");
        
        List<TKnowkey> newKeys = new ArrayList<TKnowkey>();
        TKnowkey bean = new TKnowkey();
        bean.setKeyId(keyId);
        bean.setKeyVal(keyVal);
        newKeys.add(bean);
        String ret = updateKmKeys(0, fmtKeys, newKeys, kmIds);
        boolean pass = resp(ret, null);
        return resp(key, pass, param, (pass ? "updated|" + WebUtil.str2int(ret) : ret.substring(key_fail.length())));
    }

    // update tags to t_knowkey with specified tag
    public static String updateKmKeyTags(String tags, String baseTag)
    {
        String key = "updateKmKeyTags", param = "tags|" + tags + "|baseTag|" + baseTag;
    	String[] arr = (WebUtil.empty(tags) ? null : tags.split("¡ú"));
    	baseTag = (arr == null || arr.length < 1 ? null : (WebUtil.empty(baseTag) ? arr[arr.length - 1] : baseTag));
        if (WebUtil.empty(baseTag)) return resp(key, false, param, "base tag invalid");
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<Integer> kmIds = dao.getKmIds(true, "key_id = " + tagk() + " and key_val='" + baseTag + "' order by km_id ");
        if (kmIds == null || kmIds.size() != 1) return resp(key, false, param, "base tag not found");
        
        List<TKnowkey> newKeys = new ArrayList<TKnowkey>();
        int kmId = kmIds.get(0), dels = dao.deleteKeys(tagk(), kmId);
        for (String tag : arr)
        {
            TKnowkey bean = new TKnowkey();
            bean.setKeyId(tagk());
            bean.setKeyVal(tag);
            newKeys.add(bean);
        }
        String ret = updateKmKeys(0, false, newKeys, kmIds);
        boolean pass = resp(ret, null);
        return resp(key, pass, param, (pass ? "dels|" + dels + "|updated|" + WebUtil.str2int(ret) : ret.substring(key_fail.length())));
    }

    // copy t_knowkey keys between two km_ids
    public static String copyKmKeys(int kmIdSrc, int kmIdDest)
    {
        String key = "copyKmKeys", param = "kmIdSrc|" + kmIdSrc + "|kmIdDest|" + kmIdDest;
        if (kmIdSrc < 1 || kmIdDest < 1) return resp(key, false, param, "km id invalid");
        Knowledge km_src = getKm(kmIdSrc), km_dest = getKm(kmIdDest);
        if (km_src == null) return resp(key, false, param, "src km not found");
        if (km_dest == null) return resp(key, false, param, "dest km not found");
        if (km_src.getTkm().getCkeyId() != km_dest.getTkm().getCkeyId()) return resp(key, false, param, "ckey id not match");
        Keyword ckey = KmsCacher.getKey(km_src.getTkm().getCkeyId());
        if (ckey == null || ckey.empty()) return resp(key, false, param, "ckey not found");
        
        List<TKnowkey> tkks = km_src.getTkks();
        if (tkks != null) Collections.sort(tkks, new KmKeysComparator(ckey.getTkey().getContent()));
        km_dest.setTkks(tkks);
        km_dest.setKmId(kmIdDest);
        int updated = updateKm(true, null, km_dest);
        boolean pass = (updated > 0);
        return resp(key, pass, param, (pass ? "updated|" + updated : null));
    }

    // copy t_knowledge update_date to t_knowkey update_date
    public static String copyKmKeyDates(int kmIdMin, int kmIdMax)
    {
        String key = "copyKmKeyDates", param = "kmIdMin|" + kmIdMin + "|kmIdMax|" + kmIdMax;
        if (kmIdMin > 0 && kmIdMax > 0 && kmIdMin > kmIdMax) return resp(key, false, param, "km id invalid");
        TKmsDaoService dao = GlobalCache.getInstance().getBean(TKmsDaoService.class);
        List<TKnowledge> tkms = dao.getKnowledges(kmIdCond(false, kmIdMin, kmIdMax));
        if (tkms == null || tkms.size() < 1) return resp(key, false, param, "km not found");

        List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
        for (TKnowledge tkm : tkms) params.add(tkm.toDbMap());
        int count = dao.updateKeyDates(params);
        return resp(key, true, param, "updated|" + count);
    }
    
    public static String resp(String key, boolean pass, String param, String msg)
    {
        boolean b1 = WebUtil.empty(param), b2 = WebUtil.empty(msg);
        String info = (pass ? (b1 ? "" : param) + (b2 ? "" : (b1 ? "" : "|") + msg) : (b2 ? "" : msg + (b1 ? "" : ", ")) + (b1 ? "" : param));
        if (!WebUtil.empty(key)) _logger.info(key + (pass ? ", " : " fail - ") + info);
        return (pass ? "" : key_fail) + info;
    }
    
    public static boolean resp(String info, Map<String, Object> map)
    {
        info = WebUtil.unull(info);
        boolean pass = (!info.startsWith(key_fail));
        if (map == null) return pass;
        map.put("result", pass);
        map.put(pass ? "info" : "error", pass ? info : info.substring(key_fail.length()));
        return pass;
    }
    
    // table width
    private static int width(TKeyword tkey)
    {
        return (tkey.getKeyId() == 1005 || tkey.getKeyId() == 2001 ? wd_long : (tkey.getKeyId() == 1206 || tkey.getTypeId() == 12 || tkey.getTypeId() == 13 || 
                tkey.getKeyId() == 1505 ? wd_short : (tkey.getKeyId() == 1208 || tkey.getKeyId() == 1502 || tkey.getKeyId() == 1504 ? wd_comm2 : wd_comm)));
    }
    
    // updateTime type
    public static boolean allk(TKeyword tkey)
    {
        return (tkey == null || tkey.empty() ? false : tkey.getKeyId() == 1);
    }
    // document type
    public static boolean dock(int keyId)
    {
        return (keyId == 1005 || keyId == 2001);
    }
    // updateDate type
    public static boolean updk(int keyId)
    {
        return (keyId == 3000);
    }
    // tag type
    public static int tagk()
    {
        return 2000;
    }
    
    // category type
    public static boolean ckey(TKeyword tkey)
    {
        return (tkey == null || tkey.empty() ? false : tkey.getTypeId() == 1);
    }
    // date type
    public static boolean dkey(int typeId)
    {
        return (typeId == 11);
    }
    // int type
    public static boolean ikey(TKeyword tkey)
    {
        return (tkey == null || tkey.empty() ? true : (tkey.getTypeId() == 2 || tkey.getTypeId() == 13));
    }
    // keyword field type or category type
    public static boolean kkey(TKeyword tkey)
    {
        return (tkey == null || tkey.empty() ? false : tkey.getTypeId() < 10);
    }
    // multi-value type
    public static boolean mkey(TKeyword tkey)
    {
        return (tkey == null || tkey.empty() ? false : tkey.getTypeId() >= 50);
    }
}
