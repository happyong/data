/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.arma.web.config.ConfigHelper;
import com.arma.web.service.ConfigDaoService;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.FileUtil;
import com.neulion.iptv.web.util.HttpUtil;
import com.neulion.iptv.web.util.KeyUtil;
import com.neulion.iptv.web.util.SkyUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;
import com.neulion.iptv.web.util.ZipUtil;
import com.neulion.iptv.web.util.http.HttpClient4;

public class SkyHelper
{	
	private static final Logger _logger = Logger.getLogger(SkyHelper.class);
	
	public static boolean sky(String servlet, HttpServletRequest request, HttpServletResponse response)
	{		
		String type = request.getParameter("type");
		String p1 = request.getParameter("p1");
		String address = getAddress(request);
		if (!ConfigHelper.NAME_SKY_ADMIN.equals(servlet) || WebUtil.empty(type)) return false;

		boolean success = false;
		String html = "No Content", attachment = "", content_type = WebUtil.CONTENT_TYPE_HTML;
		if ("cache".equals(type))
		{
			html = SkyUtil.getHtml4Text(address, "Finished Clearing Cache");
			success = true;
		}
		else if ("sql".equals(type))
		{
			html = (WebUtil.empty(p1) ? SkyUtil.getHtml4SqlPage(address) : 
				SkyUtil.getHtml4SqlContent(p1, address, GlobalCache.getInstance().getBean(ConfigDaoService.class)));
			success = true;
		}
		else if ("file".equals(type))
		{
			boolean absolute = "absolute".equals(request.getParameter("p2"));
			if (!WebUtil.empty(p1) && "true".equalsIgnoreCase(request.getParameter("download"))) 
			{
				String path = (absolute ? p1 :  PortalUtil.getRealPath(p1, request));
				int pos = p1.lastIndexOf("/");
				attachment = (pos == -1 ? p1 : p1.substring(pos + 1));
				boolean zip = "zip".equals(request.getParameter("p3"));
				if (zip)
				{
					ZipUtil.zip(path, path + ".zip");
					path += ".zip";
					attachment += ".zip";
				}
				PortalUtil.response0(attachment, content_type, path, response);
				if (zip)
				{
					try { Thread.sleep(1000L); } catch (Exception e) {}
					FileUtil.delete(path);
				}
				return true;
			}
			else
			{
				if (p1.startsWith("{armalog}"))
				{
					String log = (WebUtil.unix() ? "" : "c:/neulion/logs/arma.log");
					if (FileUtil.exist(1, log) == null) log = WebUtil.path(GlobalCache._config_root + "/../logs/arma.log");
					p1 = WebUtil.substituteName("{armalog}", log, p1);
					absolute = true;
				}
				html = (WebUtil.empty(p1) ? SkyUtil.getHtml4FilePage(address) : SkyUtil.getHtml4FileContent(absolute, p1, address, request));
			}
			success = true;
		}
		else if ("xml".equals(type))
		{
			if (!WebUtil.empty(p1)) 
			{
				long now = System.currentTimeMillis();
				boolean client = "client".equals(request.getParameter("p2"));
				String xml = "", url2 = p1 + (p1.indexOf("?") == -1 ? "?" : "&") + "ct=" + System.currentTimeMillis();
				for (int i = 0; i < 3; i++)
				{
					xml = (client ? HttpClient4.createInstance().http(url2, null) : HttpUtil.postXml(url2, ""));
					if (!WebUtil.empty(xml)) break;
					try { Thread.sleep(1000L); } catch (Exception ex) {}
				}
				if (!WebUtil.empty(xml)) 
				{
					content_type = WebUtil.CONTENT_TYPE_XML;
					html = xml;
				}
				_logger.info((client ? "sky4xml client [" : "sky4xml post [") + WebUtil.time2str(now) + "s], " + url2 + " - " + html);
			}
		}
		PortalUtil.response(attachment, content_type, html, response);
		return success;
	}
	
	public static void encrypt(String code, Map<String, Object> map)
	{
		if (WebUtil.empty(code)) return;
		
		map.put("code", code);
		map.put("encrypt", KeyUtil.encrypt(code));
		map.put("result", true);
	}
	
	public static void system(String format, Map<String, Object> map)
	{
		boolean json = (WebUtil.FORMAT_JSON.equals(format) || WebUtil.FORMAT_JAVA.equals(format));
		XmlOutput4j xop = null;
		if (json)
			xop = new XmlOutput4j(1, map);
		else
		{
			xop = new XmlOutput4j(1);
			xop.openTag("data", null, null);
		}
		
		Map<String, String> builds = WebUtil.builds();
		xop.appendTag(false, "hostStartup", DateUtil.date24Str(new Date(ConfigHelper.reboot), DateUtil.df_zone_long), null, null);
		xop.appendTag(false, "hostName", WebUtil.getHostName(), null, null);
		xop.appendTag(false, "build4Java", System.getProperty("java.runtime.version"), null, null);
		xop.appendTag(false, "build4Server", WebUtil.build("arma", builds.get("arma.jar")), null, null);
		xop.appendTag(false, "build4Common", WebUtil.build("commons-web", builds.get("commons-web.jar")), null, null);
		xop.appendTag(false, "build4Savanna", WebUtil.build(null, builds.get("savanna-common.jar")), null, null);
		if (!json) map.put("bodyXml", xop.output());
		map.put("result", true);
	}
	
	public static void shutdown(String user, String ip, Map<String, Object> map)
	{
		_logger.info("sky arma shutdown, user|" + user + "|ip|" + ip);
		new Thread()
		{
			public void run() 
			{
				try { Thread.sleep(300L); } catch (Exception e) {}	
				System.exit(0);	
			}
		}.start();
		map.put("result", true);
	}
	
	public static String getAddress(HttpServletRequest request)
	{
		return PortalUtil.getRemoteAddr(null, request);
	}
}
