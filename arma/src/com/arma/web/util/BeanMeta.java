/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import com.neulion.iptv.web.util.WebUtil;

public class BeanMeta
{
	private boolean cdata;
	private String type;		// String, boolean, int, Date, List, List0, Object, Bean
	private String name;		// has_away_feed
	private String name2;		// hasAwayFeed
	private String name3;		// HasAwayFeed
	private String list_cls;	// LiveScheduleStream
	private String list_xpath;	// streams/stream
	private String list_node;	// stream
	
	public BeanMeta(boolean cdata, String type, String field)
	{
		if (WebUtil.empty(field)) return;
		this.cdata = cdata;
		this.type = type;
		name = field;
		name3 = "";
		String[] arr = name.split("_");
		for (String str : arr) name3 += str.substring(0, 1).toUpperCase() + str.substring(1);
		name2 = name.substring(0, 1) + name3.substring(1);
	}
	
	public void extend(String list_cls, String list_xpath)
	{
		this.list_cls = WebUtil.unull(list_cls);
		this.list_xpath = WebUtil.unull(list_xpath);
		int pos = this.list_xpath.lastIndexOf("/");
		list_node = this.list_xpath.substring(pos + 1);
	}
	
	// setHasAwayFeed(null);
	public String constructor() 
	{
		StringBuffer sb = new StringBuffer();
		if ("String".equals(type) || "Object".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(null);").append(WebUtil.line());
		else if ("boolean".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(false);").append(WebUtil.line());
		else if ("int".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(0);").append(WebUtil.line());
		else if ("Date".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(APIUtil.gmt2d(null));").append(WebUtil.line());
		else if (isList())
		{
			sb.append(BeanMain.blanks(2)).append("// ").append(name2).append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("bean.add").append(list_cls).append("(").append(list_node).append("());").append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("bean.add").append(list_cls).append("(").append(list_node).append("());").append(WebUtil.line());
		}
		else	// Bean
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(").append(name2).append("());").append(WebUtil.line());
		return sb.toString();
	}
	
	// bean.setHasAwayFeed(getHasAwayFeed());
	public String copyit() 
	{
		StringBuffer sb = new StringBuffer();
		if ("String".equals(type) || "boolean".equals(type) || "int".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(").append(get()).append(");").append(WebUtil.line());
		else if ("Object".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(null);").append(WebUtil.line());
		else if ("Date".equals(type))
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(").append(get()).append(" == null ? null : new Date(")
				.append(get()).append(".getTime()));").append(WebUtil.line());
		else if (isList())
		{
			sb.append(BeanMain.blanks(2)).append("// ").append(name2).append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("if (").append(name2).append(" != null) for (").append(list_cls).append(" ").append(list_node)
				.append("0 : ").append(name2).append(") bean.add").append(list_cls).append("(").append(list_node).append("0.copyit());")
				.append(WebUtil.line());
		}
		else	// Bean
			sb.append(BeanMain.blanks(2)).append("bean.set").append(name3).append("(").append(get()).append(" == null ? null : ").append(get())
				.append(".copyit());").append(WebUtil.line());
		return sb.toString();
	}
	
	public String toText()
	{
		return cdata + "|" + getType0() + "|" + name2 + "|" + name3;
	}
	
	// private String hasAwayFeed;
	public String param()
	{		
		StringBuffer sb = new StringBuffer();
		sb.append(BeanMain.blanks(1)).append("private ").append(getType0()).append(" ").append(name2).append(";").append(WebUtil.line());
		return sb.toString();
	}
	
	// hasAwayFeed = ParseUtil.xpathNode("@hasAwayFeed", node);
	public String fromNode(boolean attr) 
	{
		StringBuffer sb = new StringBuffer();
		String xpath = (attr ? "@" : "") + name;
		String expr = "ParseUtil.xpathNode(\"" + xpath + "\", node)";
		if ("String".equals(type))
			sb.append(BeanMain.blanks(2)).append(name2).append(" = ").append(expr).append(";").append(WebUtil.line());
		else if ("boolean".equals(type))
			sb.append(BeanMain.blanks(2)).append(name2).append(" = \"true\".equals(").append(expr).append(");").append(WebUtil.line());
		else if ("int".equals(type))
		{
			sb.append(BeanMain.blanks(2)).append("int ").append(name2).append("0 = WebUtil.str2int(").append(expr).append(", -1);").append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("if (").append(name2).append("0 != -1) ").append(name2).append(" = ").append(name2).append("0;")
				.append(WebUtil.line());
		}
		else if ("Date".equals(type))
			sb.append(BeanMain.blanks(2)).append(name2).append(" = APIUtil.gmt2d(").append(expr).append(");").append(WebUtil.line());
		else if (isList())
		{
			sb.append(BeanMain.blanks(2)).append(name2).append(" = null;").append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("List<Node> ").append(name2).append("0 = ParseUtil.nodes(\"").append(list_xpath)
				.append("\", node);").append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("if (").append(name2).append("0 != null) for (Node n : ").append(name2).append("0) add")
				.append(list_cls).append("(new ").append(list_cls).append("().fromNode(n));").append(WebUtil.line());
		}
		else if ("Object".equals(type))
			sb.append(BeanMain.blanks(2)).append(name2).append(" = null;").append(WebUtil.line());
		else	// Bean
		{
			sb.append(BeanMain.blanks(2)).append("List<Node> ").append(name2).append("0 = ParseUtil.nodes(\"").append(xpath)
				.append("\", node);").append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("if (").append(name2).append("0 != null) ").append(name2).append(" = new ").append(type)
				.append("().fromNode(").append(name2).append("0.get(0));").append(WebUtil.line());
		}
		return sb.toString();
	}
	
	// addLiveScheduleStream(LiveScheduleStream stream)
	public String addParam()
	{
		if (!isList()) return "";
		StringBuffer sb = new StringBuffer();
		sb.append(BeanMain.mhead(false, "public void add" + list_cls + "(" + list_cls + " " + list_node + ")"));
		sb.append(BeanMain.blanks(2)).append("if (this.").append(name2).append(" == null) this.").append(name2).append(" = new ArrayList<")
			.append(list_cls).append(">();").append(WebUtil.line());
		sb.append(BeanMain.blanks(2)).append("this.").append(name2).append(".add(").append(list_node).append(");").append(WebUtil.line());
		sb.append(BeanMain.mtail());
		return sb.toString();
	}
	
	// xop.appendTag(false, "hasAwayFeed", hasAwayFeed, null, null);
	public String toXop() 
	{
		StringBuffer sb = new StringBuffer();		
		if ("String".equals(type) || "boolean".equals(type) || "int".equals(type) || "Date".equals(type) || "Object".equals(type))
		{
			String cond = ("int".equals(type) ?  "if (" + name2 + " > 0) " : "");
			sb.append(BeanMain.blanks(2)).append(cond).append("xop.appendTag(").append(cdata ? "true" : "false").append(", \"").append(name)
				.append("\", ").append(getNameValue()).append(", null, null);").append(WebUtil.line());
		}
		else if ("List".equals(type))
		{
			sb.append(BeanMain.blanks(2)).append("xop.openTag(\"").append(name).append("\", null, null);").append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("if (").append(name2).append(" != null) for (").append(list_cls).append(" ").append(list_node)
				.append("0 : ").append(name2).append(") ").append(list_node).append("0.toXop(xop);").append(WebUtil.line());
			sb.append(BeanMain.blanks(2)).append("xop.closeTag();").append(WebUtil.line());
		}
		else if ("List0".equals(type))
			sb.append(BeanMain.blanks(2)).append("if (").append(name2).append(" != null) for (").append(list_cls).append(" ").append(list_node)
				.append("0 : ").append(name2).append(") ").append(list_node).append("0.toXop(xop);").append(WebUtil.line());
		else
			sb.append(BeanMain.blanks(2)).append("if (").append(name2).append(" != null) ").append(name2).append(".toXop(xop);").append(WebUtil.line());
		
		return sb.toString();
	}
	
	// op + "WebUtil.empty(" + name + ")"
	public String empty(String op)
	{
		if ("String".equals(type))
			return op + "WebUtil.empty(" + name2 + ")";
		else if ("boolean".equals(type))
			return "";
		else if ("int".equals(type))
			return op + name2 + " < 1";
		else if ("Date".equals(type) || "Object".equals(type))
			return op + name2 + " == null";
		else if (isList())
			return op + "(" + name2 + " == null || " + name2 + ".size() < 1)";
		return op + "(" + name2 + " == null || " + name2 + ".empty())";
	}
	
	// getParam(), isParam()
	public String get()
	{
		return ("boolean".equals(type) ? "is" : "get") + name3 + "()";
	}
	
	// getHasAwayFeed(), isParam(), setHasAwayFeed(String hasAwayFeed)
	public String getset()
	{		
		StringBuffer sb = new StringBuffer();
		sb.append(BeanMain.blanks(1)).append("public ").append(getType0()).append(" ").append(get()).append(" {").append(WebUtil.line());
		sb.append(BeanMain.blanks(2)).append("return ").append(name2).append(";").append(WebUtil.line());
		sb.append(BeanMain.blanks(1)).append("}").append(WebUtil.line());
		sb.append(BeanMain.blanks(1)).append("public void set").append(name3).append("(").append(getType0()).append(" ").append(name2)
			.append(") {").append(WebUtil.line());
		sb.append(BeanMain.blanks(2)).append("this.").append(name2).append(" = ").append(name2).append(";").append(WebUtil.line());
		sb.append(BeanMain.blanks(1)).append("}").append(WebUtil.line());
		return sb.toString();
	}
	
	public boolean isCdata() {
		return cdata;
	}
	private boolean isList() {
		return ("List".equals(type) || "List0".equals(type));
	}
	private String getType0() {
		return (isList() ? "List<" + list_cls + ">" : type);
	}
	public String getNameValue() {
		if ("String".equals(type))
			return name2;
		else if ("boolean".equals(type) || "int".equals(type))
			return "\"\" + " + name2;
		else if ("Date".equals(type))
			return "APIUtil.gmt2s(" + name2 + ")";
		return "null";
	}
	public String getName() {
		return name;
	}
	public String getName2() {
		return name2;
	}
}
