/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;

import com.neulion.iptv.web.util.FileUtil;
import com.neulion.iptv.web.util.WebUtil;

public class TableMain
{	
	private static boolean apply;
	private static boolean usedate;
	private static boolean usedate2;
	private static String tbl = "";				// t_requests
	private static String utbl = "";				// TRequests
	private static String utbl2 = "";			// Requests
	private static String ltbl = "";				// requests
	private static String keyop = "";			// &&, ||
	private static String[] keys;					// client+id, http_file#unc_file
	private static List<TableMeta> params = new ArrayList<TableMeta>();
	private static StringBuffer sb_invar = new StringBuffer();
    private static StringBuffer sb_dao = new StringBuffer();
	
	public static final int st = 1;
	public static final String[][] ss = new String[][]{
        new String[]{"2015 NeuLion", "com.neulion.iptv.web.", "servlets.admin.", "InVarA", "WebUtil.obj2double("},
	    new String[]{"2015 ARMA", "com.arma.web.", "servlets.kms.", "InVarKM", "ArmaUtil.obj2double("},
        new String[]{"2015 NeuLion", "com.neulion.monitor.", "live.", "InVarL", "WebUtil.obj2double("}
	};
	
	private static void spring()
	{
		String text = text();
		if (text.indexOf("DateUtil.") != -1) usedate2 = true;
		
		StringBuffer sb = new StringBuffer();
		sb.append("/*").append(WebUtil.line());
		sb.append(" * Copyright (c) ").append(ss[st][0]).append(", Inc. All Rights Reserved.").append(WebUtil.line());
		sb.append(" */").append(WebUtil.line());
		sb.append("package ").append(ss[st][1]).append("service.bean;").append(WebUtil.line());
		sb.append(WebUtil.line());
		if (usedate) sb.append("import java.util.Date;").append(WebUtil.line());
		sb.append("import java.util.HashMap;").append(WebUtil.line());
		sb.append("import java.util.Map;").append(WebUtil.line());
		sb.append(WebUtil.line());
		sb.append("import javax.servlet.http.HttpServletRequest;").append(WebUtil.line());
		sb.append(WebUtil.line());
		sb.append("import org.dom4j.Node;").append(WebUtil.line());
		sb.append(WebUtil.line());
        sb.append("import ").append(ss[st][1]).append(ss[st][2]).append(ss[st][3]).append(";").append(WebUtil.line());
        sb.append(WebUtil.line());
		sb.append("import com.neulion.iptv.web.service.BaseDaoBean;").append(WebUtil.line());
		if (usedate || usedate2) sb.append("import com.neulion.iptv.web.util.DateUtil;").append(WebUtil.line());
		sb.append("import com.neulion.iptv.web.util.WebUtil;").append(WebUtil.line());
		sb.append("import com.neulion.iptv.web.util.XmlOutput4j;").append(WebUtil.line());
		sb.append(WebUtil.line());
		sb.append("public class ").append(utbl).append(" extends BaseDaoBean").append(WebUtil.line());
		sb.append("{").append(WebUtil.line());
		
		// generate field
		for (TableMeta meta : params) sb.append(meta.param());
		sb.append(WebUtil.line());
		// generate toDbMap method
		sb.append(mhead(true, "public Map<String, Object> toDbMap()"));
		sb.append(blanks(2)).append("Map<String, Object> dbmap = new HashMap<String, Object>();").append(WebUtil.line());
		for (TableMeta meta : params) if (!meta.isAutoincr()) sb.append(blanks(2)).append(meta.toDbMap()).append(WebUtil.line());
		sb.append(blanks(2)).append("return dbmap;").append(WebUtil.line());
		sb.append(mtail());
		// generate fromDbMap method
		sb.append(mhead(true, "@SuppressWarnings(\"unchecked\")" + WebUtil.line() + blanks(1) + 
				"public <T> T fromDbMap(Class<T> cls, Map<String, Object> dbmap)"));
		sb.append(blanks(2)).append("if (dbmap == null || dbmap.size() == 0) return (T)this;").append(WebUtil.line());
		for (TableMeta meta : params) sb.append(blanks(2)).append(meta.fromDbMap(0)).append(WebUtil.line());
		sb.append(blanks(2)).append("return (T)this;").append(WebUtil.line());
		sb.append(mtail());
		// generate fromRequest method
		sb.append(mhead(false, "public " + utbl + " fromRequest(Node node, HttpServletRequest request)"));
		for (TableMeta meta : params) if (!meta.isAutoincr()) sb.append(blanks(2)).append(meta.fromDbMap(2)).append(WebUtil.line());
		sb.append(blanks(2)).append("return this;").append(WebUtil.line());
		sb.append(mtail());
		// generate copyit method
		sb.append(mhead(false, "public " + utbl + " copyit()"));
		sb.append(blanks(2)).append(utbl).append(" bean = new ").append(utbl).append("();").append(WebUtil.line());
		for (TableMeta meta : params) sb.append(meta.copyit());
		sb.append(blanks(2)).append("return bean;").append(WebUtil.line());
		sb.append(mtail());
		// generate toText method
		sb.append(mhead(true, "public String toText()"));
		sb.append(blanks(2)).append("return ").append(text).append(";").append(WebUtil.line());
		sb.append(mtail());
		// generate empty method
		sb.append(mhead(false, "public boolean empty()"));
		sb.append(blanks(2)).append("return ").append(empty()).append(";").append(WebUtil.line());
		sb.append(mtail());
		// generate append method
		sb.append(append());
		sb.append(WebUtil.line());
		// generate get/set method
		for (TableMeta meta : params) sb.append(meta.getset());
		// generate comment statement
		sb_dao.append(WebUtil.line());
		String comment = blanks(1) + "// ", str = "";
		for (TableMeta meta : params) if (!meta.isAutoincr()) str += ", " + meta.getName2();
		comment += str.substring(2) + WebUtil.line();
		// sb.append(blanks(1)).append("/*").append(WebUtil.line());
		// generate insert method and statement
		sb_dao.append(comment);
		sb_dao.append(crud("insert"));
		// generate update method and statement
		sb_dao.append(comment);
		sb_dao.append(crud("update"));
		// generate delete method and statement
		sb_dao.append(crud("delete"));
		// generate select statement
		sb_dao.append(comment);
		sb_dao.append(crud("select"));
		// sb.append(blanks(1)).append("*/").append(WebUtil.line());
		
		sb.append("}").append(WebUtil.line());
		sb.append(WebUtil.line());
		String content = sb.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
		String file = utbl + ".java", file1 = "/spring/" + file, bk0 = FileUtil.readFile(dir_out + file1), bk1 = FileUtil.readFile(dir_out_bk1 + file1);
		FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file1);
		FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file1);
		FileUtil.writeFile(content, dir_out + file1);
		if (apply) FileUtil.writeFile(content, dir_java_spring + file);
		System.out.println(content);
	}
	
	private static String append()
	{
		String str = "";
		StringBuffer sb = new StringBuffer();
		sb_invar.append(blanks(1)).append("public static final String[] attrs_").append(ltbl).append(" = new String[]{");
		for (TableMeta meta : params) str += ", \"" + meta.getName2() + "\"";
		sb_invar.append(str.substring(2)).append("};").append(WebUtil.line());
		sb.append(mhead(false, "public void append(boolean close, XmlOutput4j xop)"));
		sb.append(blanks(2)).append("xop.openTag(\"").append(ltbl).append("\", ").append(ss[st][3]).append(".attrs_").append(ltbl).append(", new String[]{");
		str = "";
		for (TableMeta meta : params) str += ", " + meta.append();
		sb.append(str.substring(2)).append("});").append(WebUtil.line());
		sb.append(blanks(2)).append("if (close) xop.closeTag();").append(WebUtil.line());
		sb.append(blanks(1)).append("}").append(WebUtil.line());
		return sb.toString();
	}
	
	// insert, update, delete, select
	// create, retrieve, update, delete
	private static String crud(String type)
	{
		String s = (utbl2.endsWith("s") ? "" : "s");
		StringBuffer sb = new StringBuffer(), sql = new StringBuffer(), p0 = new StringBuffer(), p1 = new StringBuffer(), p2 = new StringBuffer();
		sql.append(blanks(1)).append("private static final String ").append(type).append("_").append(tbl).append(" = \"");
		if (!"select".equals(type))
		{
			if ("delete".equals(type))
			{
				for (TableMeta meta : params) 
				{
					if (hit(meta.getName(), keys) == -1) continue;
					p0.append(", ").append(meta.getType()).append(" ").append(meta.getName2());
					p1.append(meta.empty(" || "));
					p2.append(blanks(2)).append("map.put(\"").append(meta.getName2()).append("\", ").append(meta.getName2()).append(");").append(WebUtil.line());
				}
				sb.append(mhead(false, "public boolean " + type + utbl2 + "(" + (p0.length() == 0 ? "" : p0.substring(2)) + ")"));
				p0 = new StringBuffer();
				sb.append(blanks(2)).append("if (").append(p1.length() == 0 ? "false" : p1.substring(4)).append(") return true;").append(WebUtil.line());
				p1 = new StringBuffer();
				sb.append(blanks(2)).append("Map<String, Object> map = new HashMap<String, Object>();").append(WebUtil.line());	
				if (p2.length() > 0) sb.append(p2);
				sb.append(blanks(2)).append("return ").append(type).append(utbl2).append(s).append("(WebUtil.params(map));").append(WebUtil.line());
			}
			else
			{
				sb.append(mhead(false, "public boolean " + type + utbl2 + "(" + utbl + " bean)"));
				sb.append(blanks(2)).append("if (bean == null || bean.empty()").append(") return true;").append(WebUtil.line());
				sb.append(blanks(2)).append("return ").append(type).append(utbl2).append(s).append("(WebUtil.params(bean.toDbMap()));").append(WebUtil.line());
			}
			sb.append(blanks(1)).append("}").append(WebUtil.line());
			sb.append(mhead(false, "public boolean " + type + utbl2 +s + "(List<Map<String, Object>> params)"));
			sb.append(blanks(2)).append("if (params == null || params.size() < 1) return true;").append(WebUtil.line());
			sb.append(blanks(2)).append("int[] rets = batchUpdate(").append(type).append("_").append(tbl).append(", params);").append(WebUtil.line());
			sb.append(blanks(2)).append("return (rets != null);").append(WebUtil.line());
			if ("insert".equals(type))
			{
				sql.append("insert into ").append(tbl).append(" (");
				for (TableMeta meta : params) 
				{
					if (meta.isAutoincr()) continue;
					p0.append(", ").append(meta.getName());
					p1.append(", :").append(meta.getName2());
				}
				sql.append(p0.substring(2)).append(") values (");
				sql.append(p1.substring(2)).append(")");
			}
			else if ("update".equals(type))
			{
				sql.append("update ").append(tbl).append(" set ");
				for (TableMeta meta : params)
				{
					if (hit(meta.getName(), keys) != -1) p0.append(" and ").append(meta.getName()).append("=:").append(meta.getName2());
					else if (!meta.isAutoincr()) p1.append(", ").append(meta.getName()).append("=:").append(meta.getName2());
				}
				sql.append(p1.substring(2)).append(" where ").append(p0.length() == 0 ? "key=:key" : p0.substring(5));
			}
			else
			{
				for (TableMeta meta : params) if (hit(meta.getName(), keys) != -1) p0.append(" and ").append(meta.getName()).append("=:").append(meta.getName2());
				sql.append("delete from ").append(tbl).append(" where ").append(p0.length() == 0 ? "key=:key" : p0.substring(5));
			}
		}
		else
		{
			sb.append(mhead(false, "public List<" + utbl + "> get" + utbl2 + s + "(String cond)"));
			sb.append(blanks(2)).append("List<").append(utbl).append("> list = new ArrayList<").append(utbl).append(">();").append(WebUtil.line());
			sb.append(blanks(2)).append("String sql = ").append(type).append("_").append(tbl).append(";").append(WebUtil.line());
			sb.append(blanks(2)).append("if (WebUtil.empty(cond)) cond = \"1=1\";").append(WebUtil.line());
			sb.append(blanks(2)).append("sql = WebUtil.substituteParam(\"cond\", cond, sql);").append(WebUtil.line());
			sb.append(blanks(2)).append("List<Map<String, Object>> results = query(sql);").append(WebUtil.line());
			sb.append(blanks(2)).append("for (Map<String, Object> result : results) list.add(new ").append(utbl);
			sb.append("().fromDbMap(").append(utbl).append(".class, result));").append(WebUtil.line());
			sb.append(blanks(2)).append("return list;").append(WebUtil.line());
			sql.append("select ");
			for (TableMeta meta : params) p0.append(", ").append(meta.getName()).append(" as ").append(meta.getName2());
			sql.append(p0.substring(2)).append(" from ").append(tbl).append(" where ${cond}");
		}
		sql.append(" \";").append(WebUtil.line());
		sb.append(blanks(1)).append("}").append(WebUtil.line());
		sb.append(sql).append(WebUtil.line());
		return sb.toString();
	}
	
	private static String text()
	{
		String expr = "", head = " + \"|\" + ";
		// for (TableMeta meta : params) if (hit(meta.getName(), keys) != -1) expr += meta.text();
		for (TableMeta meta : params) expr += meta.text();
		if (!WebUtil.empty(expr)) expr = expr.substring(head.length());
		return (expr.indexOf(head) == -1 ? expr : "(" + expr + ")");
	}
	
	private static String empty()
	{
		String expr = "";
		for (TableMeta meta : params) if (hit(meta.getName(), keys) != -1) expr += meta.empty(keyop);
		expr = (WebUtil.empty(expr) ? "false" : (WebUtil.empty(keyop) ? expr : expr.substring(keyop.length())));
		return (WebUtil.empty(keyop) || expr.indexOf(keyop) == -1 ? expr : "(" + expr + ")");
	}
	
	private static String mhead(boolean override, String text)
	{
		StringBuffer sb = new StringBuffer();
		if (override) sb.append(blanks(1)).append("@Override").append(WebUtil.line());
		sb.append(blanks(1)).append(text).append(WebUtil.line());
		sb.append(blanks(1)).append("{").append(WebUtil.line());
		return sb.toString();
	}
	
	private static String mtail()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(blanks(1)).append("}").append(WebUtil.line());
		sb.append(WebUtil.line());
		return sb.toString();
	}
	
	public static int hit(String key, String[] arr)
	{
		if (arr == null || arr.length == 0) return -1;
		for (int i = 0; i < arr.length; i++) if (key.equals(arr[i])) return i;
		return -1;
	}
	
	private static void savanna()
	{
		String text = text();
		if (text.indexOf("DateUtil.") != -1) usedate2 = true;
		
		StringBuffer sb = new StringBuffer();
		sb.append("/*").append(WebUtil.line());
		sb.append(" * Copyright (c) ").append(ss[st][0]).append(", Inc. All Rights Reserved.").append(WebUtil.line());
		sb.append(" */").append(WebUtil.line());
		sb.append("package ").append(ss[st][1]).append("service.bean;").append(WebUtil.line());
		sb.append(WebUtil.line());
		if (usedate) sb.append("import java.util.Date;").append(WebUtil.line());
		sb.append("import java.util.Map;").append(WebUtil.line());
		sb.append(WebUtil.line());
		if (usedate2) sb.append("import com.neulion.iptv.web.util.DateUtil;").append(WebUtil.line());
		sb.append("import com.neulion.iptv.web.util.WebUtil;").append(WebUtil.line());
		sb.append(WebUtil.line());
		sb.append("public class ").append(utbl).append(WebUtil.line());
		sb.append("{").append(WebUtil.line());
		
		// generate field
		for (TableMeta meta : params) sb.append(meta.param());
		sb.append(WebUtil.line());
		// generate fromDbMap method
		sb.append(mhead(false, "public " + utbl + " fromDbMap(Map<String, Object> dbmap)"));
		sb.append(blanks(2)).append("if (dbmap == null || dbmap.size() == 0) return this;").append(WebUtil.line());
		for (TableMeta meta : params) sb.append(blanks(2)).append(meta.fromDbMap(1)).append(WebUtil.line());
		sb.append(blanks(2)).append("return this;").append(WebUtil.line());
		sb.append(mtail());
		// generate copyit method
		sb.append(mhead(false, "public " + utbl + " copyit()"));
		sb.append(blanks(2)).append(utbl).append(" bean = new ").append(utbl).append("();").append(WebUtil.line());
		for (TableMeta meta : params) sb.append(meta.copyit());
		sb.append(blanks(2)).append("return bean;").append(WebUtil.line());
		sb.append(mtail());
		// generate toText method
		sb.append(mhead(false, "public String toText()"));
		sb.append(blanks(2)).append("return ").append(text).append(";").append(WebUtil.line());
		sb.append(mtail());
		// generate empty method
		sb.append(mhead(false, "public boolean empty()"));
		sb.append(blanks(2)).append("return ").append(empty()).append(";").append(WebUtil.line());
		sb.append(mtail());
		// generate get/set method
		for (TableMeta meta : params) sb.append(meta.getset());
		// generate crud methods
        sb_dao.append(WebUtil.line());
		// sb.append(blanks(1)).append("/*").append(WebUtil.line());
		// generate insert method and statement
        sb_dao.append(cruds("insert"));
		// generate update method and statement
        sb_dao.append(cruds("update"));
		// generate delete method and statement
        sb_dao.append(cruds("delete"));
		// generate select statement
        sb_dao.append(cruds("select"));
        // sb.append(blanks(1)).append("*/").append(WebUtil.line());
		
		sb.append("}").append(WebUtil.line());
		sb.append(WebUtil.line());
		String content = sb.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
		String file = utbl + ".java", file1 = "/savanna/" + file, bk0 = FileUtil.readFile(dir_out + file1), bk1 = FileUtil.readFile(dir_out_bk1 + file1);
		FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file1);
		FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file1);
		FileUtil.writeFile(content, dir_out + file1);
		if (apply) FileUtil.writeFile(content, dir_java_savanna + file);
		System.out.println(content);
	}
	
	// insert, update, delete, select
	// create, retrieve, update, delete
	private static String cruds(String type)
	{
		StringBuffer sb = new StringBuffer(), sql = new StringBuffer(), p0 = new StringBuffer();
		StringBuffer p1 = new StringBuffer(), p2 = new StringBuffer(), p3 = new StringBuffer();
		sql.append(blanks(1)).append("private static final String ").append(type).append("_").append(tbl).append(" = \"");
		if (!"select".equals(type))
		{
			sb.append(mhead(false, "public int " + type + utbl2 + "(" + utbl + " bean)"));
			sb.append(blanks(2)).append("if (bean == null").append("delete".equals(type) ? "" : " || bean.empty()").append(") return 0;")
				.append(WebUtil.line());
			sb.append(blanks(2)).append("int ret = update(").append(type).append("_").append(tbl).append(", new Object[]{");
			if ("insert".equals(type))
			{
				sql.append("INSERT INTO ").append(tbl).append(" (");
				for (TableMeta meta : params)
				{
					if (meta.isAutoincr()) continue;
					p0.append(", bean.").append(meta.get());
					p1.append(", ").append(meta.getName());
					p2.append(", ?");
				}
				sb.append(p0.substring(2)).append("});").append(WebUtil.line());
				sql.append(p1.substring(2)).append(") VALUES (");
				sql.append(p2.substring(2)).append(")");
			}
			else if ("update".equals(type))
			{
				sql.append("UPDATE ").append(tbl).append(" SET ");
				for (TableMeta meta : params)
				{
					if (hit(meta.getName(), keys) != -1)
					{
						p0.append(", bean.").append(meta.get());
						p1.append(" AND ").append(meta.getName()).append("=?");
						continue;
					}
					if (meta.isAutoincr()) continue;
					p2.append(", bean.").append(meta.get());
					p3.append(", ").append(meta.getName()).append("=?");
				}
				sb.append(p2.substring(2)).append(p0).append("});").append(WebUtil.line());
				sql.append(p3.substring(2)).append(" WHERE ");
				sql.append(p1.length() > 5 ? p1.substring(5) : p1);
			}
			else if ("delete".equals(type))
			{
				sql.append("DELETE FROM ").append(tbl).append(" WHERE ");
				for (TableMeta meta : params)
				{
					if (hit(meta.getName(), keys) == -1) continue;
					p0.append(", bean.").append(meta.get());
					p1.append(" AND ").append(meta.getName()).append("=?");
				}
				sb.append(p0.substring(2)).append("});").append(WebUtil.line());
				sql.append(p1.length() > 5 ? p1.substring(5) : p1);
			}
		}
		else
		{
			sb.append(mhead(false, "public " + utbl + "[] get" + utbl2 + (utbl2.endsWith("s") ? "" : "s") + "()"));
			sb.append(blanks(2)).append("String sql = ").append(type).append("_").append(tbl).append(";").append(WebUtil.line());
			sb.append(blanks(2)).append("Map<String, Object>[] arr = query(sql, null, new Paging(m_pageSize, 1, null));").append(WebUtil.line());
			sb.append(blanks(2)).append(utbl).append("[] ret = new ").append(utbl).append("[arr.length];").append(WebUtil.line());
			sb.append(blanks(2)).append("for (int i = 0; i < arr.length; i++) ret[i] = new ").append(utbl).append("().fromDbMap(arr[i]);").append(WebUtil.line());
			sql.append("SELECT ");
			for (TableMeta meta : params) p0.append(", ").append(meta.getName());
			sql.append(p0.substring(2)).append(" FROM ").append(tbl);
		}
		sql.append(" \";").append(WebUtil.line());
		sb.append(blanks(2)).append("return ret;").append(WebUtil.line());
		sb.append(blanks(1)).append("}").append(WebUtil.line());
		sb.append(sql).append(WebUtil.line());
		return sb.toString();
	}
	
	private static void html()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<script>").append(WebUtil.line());
		sb.append("$(function() {").append(WebUtil.line());
		sb.append(WebUtil.line());
		
		// generate chks
		String str = "";
		for (TableMeta meta : params) str += meta.chk();
		sb.append(blanks(1)).append("var chks={");
		sb.append(str.length() > 0 ? str.substring(1) : "").append("};").append(WebUtil.line());
		sb.append(WebUtil.line());
		
		sb.append("}").append(WebUtil.line());
		sb.append("</script>").append(WebUtil.line());
		sb.append(WebUtil.line());
		String content = sb.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
		String file = "/html/" + utbl + ".html", bk0 = FileUtil.readFile(dir_out + file), bk1 = FileUtil.readFile(dir_out_bk1 + file);
		FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file);
		FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file);
		FileUtil.writeFile(content, dir_out + file);
		System.out.println(content);
	}
	
	private static boolean use_tab = true;
	public static String blanks(int count)
	{
		String str = "";
		count = Math.max(0, use_tab ? count : count * 4);
		for (int i = 0; i < count; i++) str += (use_tab ? "\t" : " ");
		return str;
	}

	private static String dir_java_spring = System.getProperty("user.dir") + "/src/com/neulion/iptv/web/service/bean/";
	private static String dir_java_savanna = System.getProperty("user.dir") + "/src/com/neulion/nlss/message/service/bean/";
	private static String dir_script_spring = System.getProperty("user.dir") + "/WebContent/WEB-INF/template/allinone/script/";
	private static String dir_script_savanna = System.getProperty("user.dir") + "/script/";
	private static String dir_out = "c:/test/table";
	private static String[][] table_all = new String[][]
	{
		// table, keyfds, blackfds, boolfds
		/*{"t_symbol", "code", "", ""}, 
		{"t_net_daily", "code+date", "", ""}, 
		{"t_quote_daily", "code+date", "", ""}*/
        {"t_keytype", "type_id", "", ""},
        {"t_keyenum", "key_id", "", ""},
        {"t_keyword", "key_id", "", "as_enum"},
        {"t_keyrank", "key_id", "", ""},
        {"t_knowledge", "km_id", "", ""},
        {"t_knowkey", "id", "", ""}
	};

	// TableMain -script "nlds.mysql.0.create.sql" -table t_client -type spring -keyfds client -blackfds 3-7,13-14,23-25,33-34,36-38 
	//           -boolfds 1-2,8-9,11-12,16,18,20,26-27,29-32,35 -apply
	// TableMain -script "nlds.mysql.0.create.sql" -table t_client -type savanna -keyfds client -blackfds 1-42 -apply
	// TableMain -script "nlds.mysql.0.create.sql" -table t_requests -type savanna -keyfds client+message_type -apply
	// TableMain -script "v3_iptvvos.mysql.0.create.sql" -table league -type html -blackfds use_cdn_dns -boolfds sync -apply
	public static void main(String[] args)
	{
		Options opts = new Options();
		Option opt = new Option("script", "script", true, "the sql script to create tables");
		opt.setRequired(true);
		opts.addOption(opt);
		opt = new Option("table", "table", true, "the target table");
		opt.setRequired(true);
		opts.addOption(opt);
		opts.addOption("type", "type", true, "the output type, spring, savanna or html");
		opts.addOption("keyfds", "keyfds", true, "the key fields name or index");
		opts.addOption("blackfds", "blackfds", true, "the useless fields name or index");
		opts.addOption("boolfds", "boolfds", true, "the boolean fields name or index");
		opts.addOption("apply", "apply", false, "apply the all classes");
		String format = "TableMain -script -table [-type] [-keyfds] [-blackfds] [-boolfds] [-apply] [-h/--help] ";
		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new PosixParser();
		CommandLine cl = null;
        try { cl = parser.parse(opts, args); } catch (ParseException e) { formatter.printHelp(format, opts); }	
        if (cl == null) return;

        if(cl.hasOption("h") || cl.hasOption("help"))
        {
        	formatter.printHelp(format, opts);
        	return;
        }
        System.out.println("main args, " + WebUtil.join(" ", args));

        boolean apply = (false && cl.hasOption("apply"));
        String table = cl.getOptionValue("table").toLowerCase(), name = null;
        String script = cl.getOptionValue("script"), type = (cl.hasOption("type") ? cl.getOptionValue("type") : "");
        if ("table_all".equals(table))
        {
            name = WebUtil.unull(new File(script).getName());
            int pos = name.indexOf(".");
            name = upper(pos == -1 ? name : name.substring(0, pos));
    		// table, keyfds, blackfds, boolfds
        	for (int i = 0; i < table_all.length; i++) generate(apply, script, type, table_all[i][0], table_all[i][1], table_all[i][2], table_all[i][3]);
        }
        else
        	generate(apply, script, type, table, (cl.hasOption("keyfds") ? cl.getOptionValue("keyfds") : ""), 
        			(cl.hasOption("blackfds") ? cl.getOptionValue("blackfds") : ""), (cl.hasOption("boolfds") ? cl.getOptionValue("boolfds") : ""));
        
        if ("savanna".equals(type))
        {
            String content = sb_dao.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
            String file = ("table_all".equals(table) ? name : utbl) + "Dao.java", file1 = "/savanna/" + file, bk0 = FileUtil.readFile(dir_out + file1), bk1 = FileUtil.readFile(dir_out_bk1 + file1);
            FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file1);
            FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file1);
            FileUtil.writeFile(content, dir_out + file1);
        }
        else if (!"html".equals(type)) 
        {            
            String content = sb_dao.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
            String file = ("table_all".equals(table) ? name : utbl) + "Dao.java", file1 = "/spring/" + file, bk0 = FileUtil.readFile(dir_out + file1), bk1 = FileUtil.readFile(dir_out_bk1 + file1);
            FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file1);
            FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file1);
            FileUtil.writeFile(content, dir_out + file1);
            
            content = sb_invar.toString();
            file = ("table_all".equals(table) ? name : utbl) + "InVar.java";
            file1 = "/spring/" + file;
            bk0 = FileUtil.readFile(dir_out + file1);
            bk1 = FileUtil.readFile(dir_out_bk1 + file1);
            FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file1);
            FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file1);
            FileUtil.writeFile(content, dir_out + file1);
        }
	}
	
	// client+id, http_file#unc_file
	private static void generate(boolean apply_b, String script, String type, String table, String keyfds, String blackfds, String boolfds)
	{
		apply = apply_b;
		usedate = false;
		usedate2 = false;
		tbl = table.toLowerCase();
		utbl = upper(table);
		utbl2 = upper2(table);
		ltbl = lower(utbl2);
		keyop = "";
		keys = null;
		params.clear();
		
        if (script.indexOf("\\") == -1 && script.indexOf("/") == -1) script = ("savanna".equals(type) ? dir_script_savanna : dir_script_spring) + script;
		List<String> lines = readLines(script);
		if (lines == null || lines.size() == 0) return;

		int index = -1;
		boolean hit = false;
		List<String> blacklist = new ArrayList<String>(), boollist = new ArrayList<String>();
		boolean blackflag = readList(blackfds, blacklist), boolflag = readList(boolfds, boollist);
		for (String line : lines)
		{
			if (!hit)
			{
				line = line.toLowerCase();
				if (line.indexOf("create table") != -1 && line.indexOf("`" + table + "`") != -1) hit = true;
				continue;
			}
			index++;
			TableMeta meta = new TableMeta();
			int ret = meta.init(line, (blackflag ? index : -1), (boolflag ? index : -1), blacklist, boollist);
			if (ret == -1) break;
			if (ret == 1) params.add(meta);
		}
		if (params.size() == 0) return;

		if (keyfds.indexOf("#") != -1)
		{
			keyop = " && ";
			keys = keyfds.split("#");
		}
		else if (keyfds.indexOf("+") != -1)
		{
			keyop = " || ";
			keys = keyfds.split("\\+");
		}
		else if (!WebUtil.empty(keyfds))
			keys = new String[]{keyfds};
		
		for (TableMeta meta : params) if (meta.isDateType()) usedate = true;
		if ("savanna".equals(type)) 
			savanna();
		else if ("html".equals(type)) html();
		else spring();
	}
	
	private static boolean readList(String list, List<String> rets)
	{
		boolean useindex = true;
		if (WebUtil.empty(list) || rets == null) return useindex;
		rets.clear();
		String[] arr = list.split(",");
		for (String str : arr)
		{
			int pos = str.indexOf("-");
			int start = WebUtil.str2int(pos == -1 ? str : str.substring(0, pos), -1);
			int end = WebUtil.str2int(pos == -1 ? str : str.substring(pos + 1), -1);
			if (start == -1 || end == -1) continue;
			for (int i = start; i <= end; i++) if (!rets.contains("" + i)) rets.add("" + i);
		}
		if (rets.size() == 0) 
		{
			useindex = false;
			for (String str : arr) if (str.length() > 0 && !rets.contains(str)) rets.add(str);
		}
		return useindex;
	}
	
	private static List<String> readLines(String file)
	{
		List<String> lines = null;
		FileInputStream fis = null;
		try 
		{
			fis = new FileInputStream(file);
			lines = IOUtils.readLines(fis, WebUtil.CHARSET_UTF_8);
		} 
		catch (Exception e) {}
		finally
		{
			try { if (fis != null) fis.close(); } catch (Exception e) {}
		}
		return lines;
	}
	
	public static String upper(String name)
	{
		String ret = "";
		if (WebUtil.empty(name)) return ret;
		String[] arr = name.split("_");
		for (String str : arr) ret += str.substring(0, 1).toUpperCase() + str.substring(1);
		return ret;
	}
	
	private static String upper2(String name)
	{
		String ret = "";
		if (WebUtil.empty(name)) return ret;
		String[] arr = name.split("_");
		boolean t = "t".equalsIgnoreCase(arr[0]);
		for (String str : arr) ret += str.substring(0, 1).toUpperCase() + str.substring(1);
		return (t ? ret.substring(1) : ret);
	}
	
	public static String lower(String name)
	{
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}
}