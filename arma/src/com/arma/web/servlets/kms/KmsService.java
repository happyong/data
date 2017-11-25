/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.kms;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arma.web.service.bean.TKnowledge;
import com.neulion.iptv.web.servlets.AbstractBaseComponent;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.SkyUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class KmsService extends AbstractBaseComponent
{
    private static final long serialVersionUID = 262574556949304272L;

    @Override
	// /servlets/kms/kmlist?ckeyId=&sortby=&desc=&conds=
    // /servlets/kms/kmlist2?ckeyId=&sortby=&desc=&conds=
    // /servlets/kms/kmdetail?kmId=
    // /servlets/kms/kmupdate?kmId=&ckeyId=&content=&keys=
    // /servlets/kms/kmdelete?kmId=
    // /servlets/kms/keydetail?keyId=
    // /servlets/kms/keyupdate?keyId=&type=&asEnum=&nameCn=&nameEn=&content=
    // /servlets/kms/keydelete?keyId=
    // /servlets/kms/knew?kmIdBase=&kmCount=&ckeyId=
    // /servlets/kms/kfmt?kmIdBase=&fmtKeys=&kmIdMin=&kmIdMax=
    // /servlets/kms/kcfmt?ckeyId=
    // /servlets/kms/kdfmt?timeBase=&minutes=&kmIdMin=&kmIdMax=
    // /servlets/kms/kccopy?kmIdMin=&kmIdMax=
    // /servlets/kms/kknew?keyId=&keyVal=&fmtKeys=&kmIdMin=&kmIdMax=
    // /servlets/kms/kknew4tags?keyId=&keyVal=&fmtKeys=&tags=
    // /servlets/kms/kkupdatetags?tags=&baseTag=
    // /servlets/kms/kkcopy?kmIdSrc=&kmIdDest=
    // /servlets/kms/kkdcopy?kmIdMin=&kmIdMax=
    // /servlets/kms/kutil?method=
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{	
		String path = "", servlet = request.getRequestURI();
		servlet = servlet.substring(servlet.lastIndexOf("/") + 1);	
		String format = (WebUtil.FORMAT_JSON.equals(request.getParameter("format")) ? WebUtil.FORMAT_JSON : WebUtil.FORMAT_XML);
		boolean json = (WebUtil.FORMAT_JSON.equals(format) || WebUtil.FORMAT_JAVA.equals(format)), usexop = true;;
		Map<String, Object> map = WebUtil.param("result", false);
		XmlOutput4j xop = null;
		if (json)
			xop = new XmlOutput4j(1, map);
		else
		{
			xop = new XmlOutput4j(1);
			xop.openTag("data", null, null);
		}

		if ("kmlist".equals(servlet)) 
		{
		    KmsCond cond = new KmsCond().fromRequest(false, request);
            List<Knowledge> kms = KmsHelper.listKm(cond);
            xop.openTag("kms", InVarKM.attrs_ckeyId, new String[]{"" + cond.getCkeyId()});
            for (Knowledge km : kms) 
            {
                TKnowledge tkm = km.getTkm();
                // String content = tkm.getContent();
                // if (content.length() > 128) content = content.substring(0, 128) + "...";
                // "kmId", "ckeyId", "content", "keys", "updateDate"
                xop.appendTag0(false, "km", null, InVarKM.attrs_km, new String[]{"" + tkm.getKmId(), null, tkm.getContent(), km.getKeys(true), DateUtil.str(tkm.getUpdateDate())});
            }
            xop.closeTag();
			map.put("result", true);
		}
		else if ("kmlist2".equals(servlet)) 
        {
            Locale locale = PortalUtil.getCurrentLocale(request);
            ResourceBundle bundle1 = PortalUtil.getBundle("i18n.portal", (locale == null ? Locale.US : locale));
            ResourceBundle bundle2 = PortalUtil.getBundle("i18n.jskms", (locale == null ? Locale.US : locale));
            KmsCond cond = new KmsCond().fromRequest(true, request);
            List<Map<String, Object>> kms = KmsHelper.listKm4Win(cond, bundle2);
            String count = "&nbsp;&nbsp;" + WebUtil.substituteName("{count}", "" + kms.size(), bundle2.getString("sc_list_count"));
            String title = bundle1.getString("kms_list") + count + (cond.empty() ? "" : "<br/><br/>" + cond.getCkey().name() + "<br/>" + cond.conds());
            String html = SkyUtil.getHtml4List(title, PortalUtil.getRemoteAddr("127.0.0.1", request), kms);
            PortalUtil.response(html, response);
            return null;
        }
		else if ("kmdetail".equals(servlet)) 
		{
            int kmId = WebUtil.str2int(request.getParameter("kmId"));
            Knowledge km = KmsHelper.getKm(kmId);
            if (km != null && !km.empty())
            {
                TKnowledge tkm = km.getTkm();
                // "kmId", "ckeyId", "content", "keys", "updateDate"
                xop.openTag("km", InVarKM.attrs_km, new String[]{"" + tkm.getKmId(), "" + tkm.getCkeyId(), tkm.getContent(), km.getKeys(false), DateUtil.str(tkm.getUpdateDate())});
                xop.closeTag();
                map.put("result", true);
            }
		}
		else if ("kmupdate".equals(servlet)) 
		{
            int updated = KmsHelper.updateKm(false, null, new Knowledge().fromRequest(request));
            if (updated > 0) map.put("result", true);
		}
        else if ("kmdelete".equals(servlet)) 
        {
            int kmId = WebUtil.str2int(request.getParameter("kmId"));
            kmId = KmsHelper.deleteKm(kmId);
            if (kmId > 0) map.put("result", true);
        }
        else if ("keydetail".equals(servlet)) 
        {
            int keyId = WebUtil.str2int(request.getParameter("keyId"));
            if (KmsCacher.outputKey(keyId, xop)) map.put("result", true);
        }
        else if ("keyupdate".equals(servlet)) 
        {
            int keyId = KmsCacher.updateKey(true, new Keyword().fromRequest(request));
            if (keyId > 0)
            {
                KmsCacher.outputMeta(xop);
                map.put("result", true);
            }
        }
        else if ("keydelete".equals(servlet)) 
        {
            int keyId = WebUtil.str2int(request.getParameter("keyId"));
            keyId = KmsCacher.deleteKey(true, keyId);
            if (keyId > 0)
            {
                KmsCacher.outputMeta(xop);
                map.put("result", true);
            }
        }
        else if ("knew".equals(servlet)) 
        {
            usexop = false;
            // new km to t_knowledge with specified base km_id, count, ckeyId
            int kmIdBase = WebUtil.str2int(request.getParameter("kmIdBase"));
            int kmCount = WebUtil.str2int(request.getParameter("kmCount"));
            int ckeyId = WebUtil.str2int(request.getParameter("ckeyId"));
            String info = KmsHelper.newKms(kmIdBase, kmCount, ckeyId, request);
            KmsHelper.resp(info, map);
        }
        else if ("kfmt".equals(servlet)) 
        {
            usexop = false;
            // format t_knowledge and t_knowkey with specified kmId list - id move, update all keys to make the id together and sort
            int kmIdBase = WebUtil.str2int(request.getParameter("kmIdBase"));
            boolean fmtKeys = (WebUtil.str2int(request.getParameter("fmtKeys")) > 0);
            int kmIdMin = WebUtil.str2int(request.getParameter("kmIdMin"));
            int kmIdMax = WebUtil.str2int(request.getParameter("kmIdMax"));
            String info = KmsHelper.fmtKmKeys(kmIdBase, fmtKeys, kmIdMin, kmIdMax);
            KmsHelper.resp(info, map);
        }
        else if ("kcfmt".equals(servlet)) 
        {
            usexop = false;
            // format all t_knowledge and t_knowkey with specified ckeyId - id move, update all keys to make the id together and sort
            int ckeyId = WebUtil.str2int(request.getParameter("ckeyId"));
            String info = KmsHelper.fmtKmKeys4ckeyId(ckeyId);
            KmsHelper.resp(info, map);
        }
        else if ("kdfmt".equals(servlet)) 
        {
            usexop = false;
            // format t_knowledge update_date for specified clock time
            // format t_knowledge update_date for specified minutes
            String timeBase = request.getParameter("timeBase");
            int minutes = WebUtil.str2int(request.getParameter("minutes"));
            int kmIdMin = WebUtil.str2int(request.getParameter("kmIdMin"));
            int kmIdMax = WebUtil.str2int(request.getParameter("kmIdMax"));
            String info = KmsHelper.fmtKmDates(timeBase, minutes, kmIdMin, kmIdMax);
            KmsHelper.resp(info, map);
        }
        else if ("kccopy".equals(servlet)) 
        {
            usexop = false;
            // copy t_knowledge content from t_knowkey tags
            int kmIdMin = WebUtil.str2int(request.getParameter("kmIdMin"));
            int kmIdMax = WebUtil.str2int(request.getParameter("kmIdMax"));
            String info = KmsHelper.copyKmContent(kmIdMin, kmIdMax);
            KmsHelper.resp(info, map);
        }
        else if ("kknew".equals(servlet)) 
        {
            usexop = false;
            // add new key to t_knowkey with specified kmIdMin, kmIdMax
            int keyId = WebUtil.str2int(request.getParameter("keyId"));
            String keyVal = request.getParameter("keyVal");
            boolean fmtKeys = (WebUtil.str2int(request.getParameter("fmtKeys")) > 0);
            int kmIdMin = WebUtil.str2int(request.getParameter("kmIdMin"));
            int kmIdMax = WebUtil.str2int(request.getParameter("kmIdMax"));
            String info = KmsHelper.newKmKey(keyId, keyVal, fmtKeys, kmIdMin, kmIdMax);
            KmsHelper.resp(info, map);
        }
        else if ("kknew4tags".equals(servlet)) 
        {
            usexop = false;
            // add new key to t_knowkey with specified tags
            int keyId = WebUtil.str2int(request.getParameter("keyId"));
            String keyVal = request.getParameter("keyVal");
            boolean fmtKeys = (WebUtil.str2int(request.getParameter("fmtKeys")) > 0);
            String tags = request.getParameter("tags");
            String info = KmsHelper.newKmKey4Tags(keyId, keyVal, fmtKeys, tags);
            KmsHelper.resp(info, map);
        }
        else if ("kkupdatetags".equals(servlet)) 
        {
            usexop = false;
            // update tags to t_knowkey with specified tag
            String tags = request.getParameter("tags");
            String baseTag = request.getParameter("baseTag");
            String info = KmsHelper.updateKmKeyTags(tags, baseTag);
            KmsHelper.resp(info, map);
        }
        else if ("kkcopy".equals(servlet)) 
        {
            usexop = false;
            // copy t_knowkey keys between two km_ids
            int kmIdSrc = WebUtil.str2int(request.getParameter("kmIdSrc"));
            int kmIdDest = WebUtil.str2int(request.getParameter("kmIdDest"));
            String info = KmsHelper.copyKmKeys(kmIdSrc, kmIdDest);
            KmsHelper.resp(info, map);
        }
        else if ("kkdcopy".equals(servlet)) 
        {
            usexop = false;
            // copy t_knowledge update_date to t_knowkey update_date
            int kmIdMin = WebUtil.str2int(request.getParameter("kmIdMin"));
            int kmIdMax = WebUtil.str2int(request.getParameter("kmIdMax"));
            String info = KmsHelper.copyKmKeyDates(kmIdMin, kmIdMax);
            KmsHelper.resp(info, map);
        }
        else if ("kutil".equals(servlet)) 
        {
            usexop = false;
            // execute the KmsUtil method
            String method = request.getParameter("method");
            if ("coast".equals(method)) map.put("result", KmsUtil.handleCoast());
            else if ("fleet".equals(method)) map.put("result", KmsUtil.handleFleet());
            else if ("rocket".equals(method)) map.put("result", KmsUtil.handleRocket());
            else if (method.startsWith("keyval::")) map.put("result", KmsUtil.handleKeyVal(method.substring(8)));
            else if ("vote".equals(method)) map.put("result", KmsUtil.handleVote());
            else if (method.startsWith("tkktest")) map.put("result", KmsUtil.handleTkkTest(WebUtil.str2int(method.substring(7))));
        }
		
		if ((Boolean)map.get("result") && !json && usexop) map.put("bodyXml", xop.output());
		return response(path, format, map, request);
	}
}
