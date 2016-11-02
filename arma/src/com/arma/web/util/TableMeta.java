/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.util.List;

import com.neulion.iptv.web.util.WebUtil;

public class TableMeta
{
	private String name;		// has_away_feed
	private String type;		// String, Date, int, double, boolean
	private String def;
	private boolean autoincr;
	
	private String name2;		// hasAwayFeed
	private String name3;		// HasAwayFeed
	
	public int init(String line, int blackindex, int boolindex, List<String> blackfds, List<String> boolfds)
	{
		if (WebUtil.empty(line)) return -1;
		line = line.trim();
		if (!line.startsWith("`")) return -1;
		int pos = line.indexOf("`", 1);
		if (pos == -1) return -1;
		name = line.substring(1, pos).toLowerCase();
		if (hit(blackindex, blackfds))
		{
			System.out.println("ignore - " + line);
			return 0;
		}
		
		String line2 = line.substring(pos + 1).trim(), line2l = line2.toLowerCase();
		if (line2l.startsWith("datetime"))
			type = "Date";
		else if (line2l.startsWith("int") || line2l.startsWith("tinyint") || line2l.startsWith("smallint"))
		{
			type = (hit(boolindex, boolfds) ? "boolean" : "int");
			String defval = defval(line2, line2l);
			if (defval != null)
			{
				if ("int".equals(type) && !"0".equals(defval)) def = defval;
				if ("boolean".equals(type) && "1".equals(defval)) def = "true";
			}
		}
		else if (line2l.startsWith("float") || line2l.startsWith("double") || line2l.startsWith("decimal")) 
		{
			type = "double";
			String defval = defval(line2, line2l);
			if (defval != null && !"0".equals(defval)) def = defval;
		}
		else
		{
			type = "String";
			String defval = defval(line2, line2l);
			if (defval != null) def = defval;
		}
		if (line2l.indexOf("auto_increment") != -1) autoincr = true;
		
		name3 = TableMain.upper(name);
		name2 = TableMain.lower(name3);
		System.out.println(type + "|" + WebUtil.unull(def) + "|" + name + "|" + name2 + "|" + name3 + " - " + line);
		return 1;
	}
	
	private boolean hit(int index, List<String> list)
	{
		if (list == null || list.size() == 0) return false;
		return (list.contains(index < 0 ? name : "" + index));
	}
	
	private String defval(String line2, String line2l)
	{
		String key = "default";
		int pos  = line2l.indexOf(key);
		if (pos == -1) return null;
		String str = line2.substring(pos + key.length()).trim();
		if (str.endsWith(",")) str = str.substring(0, str.length() - 1);
		pos = str.indexOf(" ");
		String val = (pos == -1 ? str : str.substring(0, pos));
		val = WebUtil.substituteName("'", "", val);
		val = WebUtil.substituteName("\"", "", val);
		return (val.length() == 0 || "null".equalsIgnoreCase(val) ? null : val);
	}
	
	// private String param = "1";
	public String param()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(TableMain.blanks(1)).append("private ").append(type).append(" ").append(name2);
		if (def != null) sb.append(" = ").append("String".equals(type) ? "\"" + def + "\"" : def);
		sb.append(";").append(WebUtil.line());
		return sb.toString();
	}
	
	// bean.setParam(getParam());
	public String copyit() 
	{
		StringBuffer sb = new StringBuffer();
		if ("Date".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(").append(get()).append(" == null ? null : new Date(")
				.append(get()).append(".getTime()));").append(WebUtil.line());
		else
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(").append(get()).append(");").append(WebUtil.line());
		return sb.toString();
	}
	
	// dbmap.put("param", WebUtil.unull(param));
	public String toDbMap() 
	{
		String to = name2;
		if ("String".equals(type))
			to = "WebUtil.unull(" + name2 + ")";
		else if ("boolean".equals(type))
			to = name2 + " ? 1 : 0";
		return "dbmap.put(\"" + name2 + "\", " + to + ");";
	}
	
	// from_type: 0 - spring mode, 1 - savanna mode, 2 - html mode
	// fromDbMap, fromRequest
	// param = WebUtil.obj2int(WebUtil.scan_str("param", node, request));
	// param = WebUtil.obj2int(dbmap.get("param"));
	public String fromDbMap(int from_type) 
	{
		String val = (from_type == 2 ? "WebUtil.scan_str(\"" + name + "\", node, request)" : "dbmap.get(\"" + (from_type == 1 ? name : name2) + "\")");
		StringBuffer sb = new StringBuffer();
		sb.append(name2).append(" = ");
		if (isDateType())
		{
			if (from_type == 2)
				sb.append("DateUtil.date(").append(val).append(")");
			else
				sb.append("(Date)").append(val);
		}
		else if ("int".equals(type))
			sb.append("WebUtil.obj2int(").append(val).append(")");
		else if ("double".equals(type))
			sb.append(TableMain.ss[TableMain.st][4]).append(val).append(")");
		else if ("boolean".equals(type))
			sb.append("(WebUtil.obj2int(").append(val).append(") == 1)");
		else
			sb.append(from_type == 2 ? "" : "(String)").append(val);
		sb.append(";");
		return sb.toString();
	}
	
	// getParam(), DateUtil.str(getParam()), "" + getParam();
	public String append()
	{
		StringBuffer sb = new StringBuffer();
		if (isDateType())
			sb.append("DateUtil.str(").append(get()).append(")");
		else if ("String".equals(type))
			sb.append(get());
		else
			sb.append("\"\" + ").append(get());
		return sb.toString();
	}
	
	// " + \"|\" + WebUtil.empty(" + name + ")"
	public String text()
	{
		String head = " + \"|\" + ";
		if ("boolean".equals(type) || "int".equals(type) || "double".equals(type))
			return head + name2;
		else if ("Date".equals(type))
			return head + "DateUtil.date24Str(" + name2 + ", DateUtil.df_long)";
		return head + "WebUtil.unull(" + name2 + ")";
	}
	
	// op + "WebUtil.empty(" + name + ")"
	public String empty(String op)
	{
		if ("boolean".equals(type))
			return "";
		else if ("int".equals(type))
			return op + name2 + " < 1";
		else if ("double".equals(type))
			return op + name2 + " = 0";
		else if ("Date".equals(type))
			return op + name2 + " == null";
		return op + "WebUtil.empty(" + name2 + ")";
	}
	
	// getParam(), isParam()
	public String get()
	{
		return ("boolean".equals(type) ? "is" : "get") + name3 + "()";
	}
	
	// getParam(), isParam()
	public String getset()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(TableMain.blanks(1)).append("public ").append(type).append(" ").append(get()).append(" {").append(WebUtil.line());
		sb.append(TableMain.blanks(2)).append("return ").append(name2).append(";").append(WebUtil.line());
		sb.append(TableMain.blanks(1)).append("}").append(WebUtil.line());
		sb.append(TableMain.blanks(1)).append("public void set").append(name3).append("(").append(type).append(" ").append(name2)
			.append(") {").append(WebUtil.line());
		sb.append(TableMain.blanks(2)).append("this.").append(name2).append(" = ").append(name2).append(";").append(WebUtil.line());
		sb.append(TableMain.blanks(1)).append("}").append(WebUtil.line());
		return sb.toString();
	}
	
	// ,"has_multi_audio":"0"
	public String chk()
	{
		return ("boolean".equals(type) ? ",\"" + name + "\":" + ("true".equals(def) ? "\"1\"" : "\"0\"") : "");
	}

	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public boolean isDateType() {
		return ("Date".equals(type));
	}
	public boolean isAutoincr() {
		return autoincr;
	}
	public String getName2() {
		return name2;
	}
}
