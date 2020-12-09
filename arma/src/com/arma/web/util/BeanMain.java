/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dom4j.Element;
import org.dom4j.Node;

import com.neulion.iptv.web.util.FileUtil;
import com.neulion.iptv.web.util.ParseUtil;
import com.neulion.iptv.web.util.WebUtil;

public class BeanMain
{
    private boolean apply;
    private boolean close;
    private boolean nlss;
    private int index;
    private String file; // "1.a.req updateLiveSchedule.xml"
    private String[] xpaths; // //updateLiveSchedule/programs/program/streams/stream
    private String[] nodes; // stream
    private String[] cnames; // LiveScheduleStream
    private String[] keyfds; // client+id, http_file#unc_file
    private String[] boolfds;
    private String[] intfds;
    private String[] datefds;

    private boolean message;
    private boolean subtype;
    private boolean usedate;
    private boolean uselist;
    private boolean useweb;
    private boolean single;
    private String keyop; // &&, ||
    private String[] keys; // client+id, http_file#unc_file
    private BeanMeta sbean;
    private List<BeanMeta> attrs = new ArrayList<BeanMeta>();
    private List<BeanMeta> params = new ArrayList<BeanMeta>();

    private static final String s_copyright = "2015 ARMA";
    private static final String s_package = "com.arma.web.";

    public BeanMain(boolean apply, String file, String xpaths, String attrs, String cnames, String keyfds, String boolfds, String intfds, String datefds)
    {
        if (WebUtil.empty(file) || WebUtil.empty(xpaths))
            return;
        String[] xpaths0 = xpaths.split(",", -1), cnames0 = parse(xpaths0.length, cnames);
        int len = xpaths0.length;
        nodes = parse(len, null);
        for (int i = 0; i < len; i++)
        {
            String xpath = xpaths0[i];
            if (xpath.length() < xpaths0[index].length())
                index = i;
            int pos = xpath.lastIndexOf("/");
            nodes[i] = xpath.substring(pos + 1);
            if (WebUtil.empty(cnames0[i]))
                cnames0[i] = (nodes[i].substring(0, 1).toUpperCase() + nodes[i].substring(1));
        }
        this.apply = apply;
        String[] attrs0 = attrs.split(",", -1);
        this.close = ("close".equals(attrs0[0]));
        this.nlss = ("nlss".equals(attrs0[1]));
        this.file = file;
        this.xpaths = xpaths0;
        this.cnames = cnames0;
        this.keyfds = parse(len, keyfds);
        this.boolfds = boolfds.split(",", -1);
        this.intfds = intfds.split(",", -1);
        this.datefds = datefds.split(",", -1);
    }

    private String[] parse(int len, String str)
    {
        if (WebUtil.empty(str))
            return new String[len];
        String[] arr = str.split(",", -1);
        return (arr.length == len ? arr : new String[len]);
    }

    private String mcname()
    {
        return (cnames == null || cnames.length <= index ? "" : cnames[index]);
    }

    public void beans(boolean req, StringBuffer demo)
    {
        for (int i = 0; i < cnames.length; i++)
        {
            // init params
            message = mcname().equals(cnames[i]);
            subtype = false;
            usedate = false;
            uselist = false;
            useweb = false;
            single = false;
            keyop = "";
            keys = null;
            sbean = null;
            attrs.clear();
            params.clear();

            // generate bean java code
            bean(xpaths[i], nodes[i], cnames[i], WebUtil.unull(keyfds[i]));

            // generate constructor method to demo
            String constructor = (message ? (req ? "request" : "response") : nodes[i]);
            demo.append(mhead(false, "private " + cnames[i] + " " + constructor + "()"));
            demo.append(blanks(2)).append(cnames[i]).append(" bean = new ").append(cnames[i]).append("();").append(WebUtil.line());
            for (BeanMeta meta : attrs)
                demo.append(meta.constructor());
            for (BeanMeta meta : params)
                demo.append(meta.constructor());
            if (sbean != null)
                demo.append(sbean.constructor());
            demo.append(blanks(2)).append("return bean;").append(WebUtil.line());
            demo.append(mtail());
        }
    }

    public void bean(String xpath, String node, String cname, String keyfd)
    {
        scan(xpath, keyfd);
        String empty = empty();
        if (empty.indexOf("WebUtil.empty(") != -1)
            useweb = true;

        StringBuffer sb = new StringBuffer();
        sb.append(head(cname));
        // generate field
        if (message)
            sb.append(blanks(1)).append("public static final String TAG_NAME = \"").append(node).append("\";").append(WebUtil.line()).append(WebUtil.line());
        for (BeanMeta meta : attrs)
            sb.append(meta.param());
        for (BeanMeta meta : params)
            sb.append(meta.param());
        if (sbean != null)
            sb.append(sbean.param());
        sb.append(WebUtil.line());
        // generate copyit method
        sb.append(mhead(false, "public " + cname + " copyit()"));
        sb.append(blanks(2)).append(cname).append(" bean = new ").append(cname).append("();").append(WebUtil.line());
        for (BeanMeta meta : attrs)
            sb.append(meta.copyit());
        for (BeanMeta meta : params)
            sb.append(meta.copyit());
        if (sbean != null)
            sb.append(sbean.copyit());
        sb.append(blanks(2)).append("return bean;").append(WebUtil.line());
        sb.append(mtail());
        // generate fromNode method
        sb.append(mhead(false, "public " + cname + " fromNode(Node node)"));
        for (BeanMeta meta : attrs)
            sb.append(meta.fromNode(true));
        for (BeanMeta meta : params)
            sb.append(meta.fromNode(false));
        if (sbean != null)
            sb.append(blanks(2)).append("if (node != null) ").append(sbean.getName2()).append(" = node.getText();").append(WebUtil.line());
        if (message && !nlss)
        {
            sb.append(blanks(2)).append("if (WebUtil.empty(internalId)) internalId = WebUtil.uuid2();").append(WebUtil.line());
            sb.append(blanks(2)).append("if (WebUtil.empty(id)) id = internalId;").append(WebUtil.line());
        }
        sb.append(blanks(2)).append("return this;").append(WebUtil.line());
        sb.append(mtail());
        // generate addParam method
        for (BeanMeta meta : params)
            sb.append(meta.addParam());
        // generate toXop method
        sb.append(mhead(false, "public void toXop(XmlOutput4j xop)"));
        String names = "", values = "";
        for (BeanMeta meta : attrs)
        {
            names += ", \"" + meta.getName() + "\"";
            values += ", " + meta.getNameValue();
        }
        String str =
                (WebUtil.empty(names) ? "null, null" : "new String[]{" + names.substring(2) + "}, " + (attrs.size() > 3 ? WebUtil.line() + blanks(3) : "") + "new String[]{"
                        + values.substring(2) + "}");
        String tag = (message ? "TAG_NAME" : "\"" + node + "\""), xpath0 = (message ? "\"//\" + TAG_NAME" : "\"" + xpath + "\"");
        if (single)
        {
            sb.append(blanks(2)).append("xop.appendTag(").append(sbean != null && sbean.isCdata() ? "true" : "false").append(", ").append(tag).append(", ")
                    .append(sbean != null ? sbean.getName2() : "null").append(", ").append(str).append(");").append(WebUtil.line());
        }
        else
        {
            sb.append(blanks(2)).append("xop.openTag(").append(tag).append(", ").append(str).append(");").append(WebUtil.line());
            for (BeanMeta meta : params)
                sb.append(meta.toXop());
            sb.append(blanks(2)).append("xop.closeTag();").append(WebUtil.line());
        }
        sb.append(mtail());
        if (message)
        {
            // generate fromXml method
            sb.append(mhead(true, "public " + (nlss ? "INLSSMessage" : "ICMSMessage") + " fromXml(String xml)"));
            sb.append(blanks(2)).append(cname).append(" bean = new ").append(cname).append("().fromNode(ParseUtil.parseXml0(xml, ").append(xpath0).append("));")
                    .append(WebUtil.line());
            sb.append(blanks(2)).append("return (bean.empty() ? null : bean);").append(WebUtil.line());
            sb.append(mtail());
            // generate toXml method
            sb.append(mhead(true, "public String toXml()"));
            sb.append(blanks(2)).append("if (empty()) return \"\";").append(WebUtil.line());
            sb.append(blanks(2)).append("XmlOutput4j xop = new XmlOutput4j().appendHeader();").append(WebUtil.line());
            sb.append(blanks(2)).append("toXop(xop);").append(WebUtil.line());
            sb.append(blanks(2)).append("return xop.output();").append(WebUtil.line());
            sb.append(mtail());
            if (!nlss)
            {
                // generate toDb method
                sb.append(mhead(true, "public TRequests toDb()"));
                sb.append(blanks(2)).append("if (empty()) return null;").append(WebUtil.line());
                sb.append(blanks(2)).append("TRequests bean = new TRequests();").append(WebUtil.line());
                sb.append(blanks(2)).append("bean.setInternalId(getInternalId());").append(WebUtil.line());
                sb.append(blanks(2)).append("bean.setClient(getClient());").append(WebUtil.line());
                sb.append(blanks(2)).append("bean.setMessageType(TAG_NAME);").append(WebUtil.line());
                if (false && subtype)
                    sb.append(blanks(2)).append("bean.setSubType(getSubType());").append(WebUtil.line());
                sb.append(blanks(2)).append("bean.setCreationTime(new Date());").append(WebUtil.line());
                sb.append(blanks(2)).append("bean.setRequest(this);").append(WebUtil.line());
                sb.append(blanks(2)).append("return bean;").append(WebUtil.line());
                sb.append(mtail());
            }
        }
        // generate empty method
        sb.append(mhead(message, "public boolean empty()"));
        sb.append(blanks(2)).append("return ").append(empty).append(";").append(WebUtil.line());
        sb.append(mtail());
        // generate get/set method
        for (BeanMeta meta : attrs)
            sb.append(meta.getset());
        for (BeanMeta meta : params)
            sb.append(meta.getset());
        if (sbean != null)
            sb.append(sbean.getset());
        // sb.append(WebUtil.line());

        sb.append("}").append(WebUtil.line());
        sb.append(WebUtil.line());
        String content = sb.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
        String file = "/bean/" + cname + ".java", bk0 = FileUtil.readFile(dir_out + file), bk1 = FileUtil.readFile(dir_out_bk1 + file);
        FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file);
        FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file);
        FileUtil.writeFile(content, dir_out + file);
        if (apply)
            FileUtil.writeFile(content, dir_java + file);
        System.out.println(content);
    }

    @SuppressWarnings("unchecked")
    private void scan(String xpath, String keyfd)
    {
        System.out.println("begin to scan, " + xpath + "|" + file);

        String xml = FileUtil.readFile(System.getProperty("user.dir") + "/src/com/neulion/nlss/message/xml/" + file);
        List<Node> nodes = ParseUtil.parseXml(xml, xpath);
        if (nodes == null || nodes.size() < 1)
        {
            System.out.println("empty to ignore");
            return;
        }
        if (message)
        {
            add(false, false, "id", null, attrs);
            add(false, false, "internalId", null, attrs);
        }
        Element ele = (Element) nodes.get(0);
        int count = ele.attributeCount();
        for (int i = 0; i < count; i++)
            add(false, false, ele.attribute(i).getName(), null, attrs);
        System.out.println("above are all attributes");
        List<Element> list = ele.elements();
        if (list == null || list.size() == 0)
        {
            System.out.println("no elements to use single node mode");
            single = true;
            if (!WebUtil.empty(ele.getText()))
                sbean = new BeanMeta(ParseUtil.isCDATA(ele), "String", "value");
        }
        else
        {
            if (same(list))
                add(false, true, list.get(0).getName(), null, params);
            else
                for (Element e : list)
                    add(ParseUtil.isCDATA(e), false, e.getName(), e, params);
        }
        System.out.println("above are all params");
        System.out.println("end to scan, " + xpath + "|" + file);
        System.out.println();

        if (keyfd.indexOf("#") != -1)
        {
            keyop = " && ";
            keys = keyfd.split("#");
        }
        else if (keyfd.indexOf("+") != -1)
        {
            keyop = " || ";
            keys = keyfd.split("\\+");
        }
        else if (!WebUtil.empty(keyfd))
            keys = new String[] { keyfd };
    }

    // vodRequest/profiles/profile
    private boolean same(List<Element> list)
    {
        String name = list.get(0).getName();
        for (Element e : list)
            if (!e.getName().equals(name))
                return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    private void add(boolean cdata, boolean blist, String name, Element e, List<BeanMeta> list)
    {
        if (WebUtil.empty(name))
            return;
        String list_cls = null, list_xpath = null, type = "String"; // String, boolean, int, Date, List, List0, Object, Bean
        List<Element> es = (e == null ? null : e.elements());
        if (blist)
        {
            int i1 = end("/" + name, xpaths);
            // List0 - list_cls = "VodProfile", list_xpath = "profile", 10.vodRequest.xml
            if (i1 == -1)
                type = "Object";
            else
            {
                type = "List0";
                uselist = true;
                list_cls = cnames[i1];
                list_xpath = name;
            }
        }
        else if (es != null && es.size() > 0)
        {
            type = "Object";
            // Bean - type = "LivePushUrl", x1 = "/pushUrl", x2 = "/pushUrl/url", 6.updateLiveProgram.xml
            // List - list_cls = "LiveScheduleProgram", list_xpath = "programs/program", x2 = "/programs/program", 1.updateLiveSchedule.xml
            // List - list_cls = "LivePlaybackUrl", list_xpath = "playbackUrls/playbackUrl", x2 = "/playbackUrls/playbackUrl", 6.updateLiveProgram.xml
            // List - list_cls = "VodDevice", list_xpath = "devices/device", x2 = "/devices/device", 10.vodRequest.xml
            // List0 - list_cls = "VodProfiles", list_xpath = "profiles", x2 = "/profiles/profile", 10.vodRequest.xml
            String x1 = "/" + name, x2 = x1 + "/" + es.get(0).getName();
            int i1 = end(x1, xpaths), i2 = end(x2, xpaths);
            if (i1 != -1 && i2 == -1)
                type = cnames[i1];
            else if (i2 != -1)
            {
                type = (i1 == -1 ? "List" : "List0");
                uselist = true;
                list_cls = cnames[i1 == -1 ? i2 : i1];
                list_xpath = (i1 == -1 ? x2.substring(1) : name);
            }
        }
        else
        {
            if (hit(name, boolfds) != -1)
                type = "boolean";
            else if (hit(name, intfds) != -1)
            {
                type = "int";
                useweb = true;
            }
            else if (hit(name, datefds) != -1)
            {
                type = "Date";
                usedate = true;
            }
        }
        BeanMeta m = new BeanMeta(cdata, type, name);
        if (list_cls != null && list_xpath != null)
            m.extend(list_cls, list_xpath);
        for (BeanMeta meta : list)
            if (name.equals(meta.getName()))
                return;
        System.out.println("add param, " + name + "|" + m.toText());
        if ("subType".equals(m.getName2()))
            subtype = true;
        list.add(m);
    }

    private int end(String key, String[] arr)
    {
        for (int i = 0; i < arr.length; i++)
            if (arr[i].endsWith(key))
                return i;
        return -1;
    }

    private String empty()
    {
        String expr = "";
        for (BeanMeta meta : attrs)
            if (hit(meta.getName(), keys) != -1)
                expr += meta.empty(keyop);
        for (BeanMeta meta : params)
            if (hit(meta.getName(), keys) != -1)
                expr += meta.empty(keyop);
        if (sbean != null && hit(sbean.getName(), keys) != -1)
            expr += sbean.empty(keyop);
        expr = (WebUtil.empty(expr) ? "false" : (WebUtil.empty(keyop) ? expr : expr.substring(keyop.length())));
        return (WebUtil.empty(keyop) || expr.indexOf(keyop) == -1 ? expr : "(" + expr + ")");
    }

    private String head(String cname)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("/*").append(WebUtil.line());
        sb.append(" * Copyright (c) ").append(s_copyright).append(", Inc. All Rights Reserved.").append(WebUtil.line());
        sb.append(" */").append(WebUtil.line());
        sb.append("package ").append(s_package).append("bean;").append(WebUtil.line());
        sb.append(WebUtil.line());
        if (uselist)
            sb.append("import java.util.ArrayList;").append(WebUtil.line());
        if (usedate || (message && !nlss))
            sb.append("import java.util.Date;").append(WebUtil.line());
        if (uselist)
            sb.append("import java.util.List;").append(WebUtil.line());
        if (usedate || (message && !nlss) || uselist)
            sb.append(WebUtil.line());
        sb.append("import org.dom4j.Node;").append(WebUtil.line());
        sb.append(WebUtil.line());
        sb.append("import com.neulion.iptv.web.util.ParseUtil;").append(WebUtil.line());
        if (useweb || (message && !nlss))
            sb.append("import com.neulion.iptv.web.util.WebUtil;").append(WebUtil.line());
        sb.append("import com.neulion.iptv.web.util.XmlOutput4j;").append(WebUtil.line());
        if (usedate)
            sb.append("import com.neulion.nlss.message.APIUtil;").append(WebUtil.line());
        if (message && !nlss)
            sb.append("import com.neulion.nlss.message.service.bean.TRequests;").append(WebUtil.line());
        sb.append(WebUtil.line());
        sb.append("public class ").append(cname).append(message ? " implements " + (nlss ? "INLSSMessage" : "ICMSMessage") : "").append(WebUtil.line());
        sb.append("{").append(WebUtil.line());
        return sb.toString();
    }

    public static String mhead(boolean override, String text)
    {
        StringBuffer sb = new StringBuffer();
        if (override)
            sb.append(blanks(1)).append("@Override").append(WebUtil.line());
        sb.append(blanks(1)).append(text).append(WebUtil.line());
        sb.append(blanks(1)).append("{").append(WebUtil.line());
        return sb.toString();
    }

    public static String mtail()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(blanks(1)).append("}").append(WebUtil.line());
        sb.append(WebUtil.line());
        return sb.toString();
    }

    public static int hit(String key, String[] arr)
    {
        if (arr == null || arr.length == 0)
            return -1;
        for (int i = 0; i < arr.length; i++)
            if (key.equals(arr[i]))
                return i;
        return -1;
    }

    private static boolean use_tab = true;

    public static String blanks(int count)
    {
        String str = "";
        count = Math.max(0, use_tab ? count : count * 4);
        for (int i = 0; i < count; i++)
            str += (use_tab ? "\t" : " ");
        return str;
    }

    private static void demo(String append, boolean apply, String api, StringBuffer demo, BeanMain request, BeanMain response)
    {
        String cname = (api.substring(0, 1).toUpperCase() + api.substring(1)) + "Demo";
        StringBuffer sb = new StringBuffer();
        sb.append("/*").append(WebUtil.line());
        sb.append(" * Copyright (c) ").append(s_copyright).append(", Inc. All Rights Reserved.").append(WebUtil.line());
        sb.append(" */").append(WebUtil.line());
        sb.append("package ").append(s_package).append("demo;").append(WebUtil.line());
        sb.append(WebUtil.line());
        sb.append("import java.io.File;").append(WebUtil.line());
        sb.append(WebUtil.line());
        sb.append("import com.neulion.iptv.web.util.FileUtil;").append(WebUtil.line());
        sb.append("import com.neulion.nlss.message.APIUtil;").append(WebUtil.line());
        List<String> list = new ArrayList<String>();
        for (String str : request.cnames)
            list.add(str);
        for (String str : response.cnames)
            list.add(str);
        Collections.sort(list);
        for (String str : list)
            sb.append("import com.neulion.nlss.message.bean.").append(str).append(";").append(WebUtil.line());
        sb.append(WebUtil.line());
        sb.append("public class ").append(cname).append(WebUtil.line());
        sb.append("{").append(WebUtil.line());
        sb.append(blanks(1)).append("private String dir_in = System.getProperty(\"user.dir\") + \"/src/com/neulion/nlss/message/xml/\";").append(WebUtil.line());
        sb.append(blanks(1)).append("private String dir_out = \"").append(dir_out).append("/xml/\";").append(WebUtil.line());
        sb.append(WebUtil.line());

        // generate api method
        sb.append(blanks(1)).append("// ").append(api).append(" request and response").append(WebUtil.line());
        sb.append(mhead(false, "private void " + api + "(String req_file, String resp_file)"));

        String req_mcname = request.mcname(), resp_mcname = response.mcname();
        output(true, true, req_mcname, sb);
        sb.append(WebUtil.line());
        output(true, false, req_mcname, sb);
        sb.append(WebUtil.line());
        output(false, true, resp_mcname, sb);
        sb.append(WebUtil.line());
        output(false, false, resp_mcname, sb);
        sb.append(mtail());

        // generate constructor method
        sb.append(demo.toString());
        // generate main method
        sb.append(mhead(false, "public static void main(String[] args)"));
        sb.append(blanks(2)).append(cname).append(" demo = new ").append(cname).append("();").append(WebUtil.line());
        sb.append(blanks(2)).append(WebUtil.line());
        sb.append(blanks(2)).append("demo.request();").append(WebUtil.line());
        sb.append(blanks(2)).append("demo.response();").append(WebUtil.line());
        sb.append(blanks(2)).append("// ").append(api).append(" request and response").append(WebUtil.line());
        String freq = request.file, fresp = response.file, s = blanks(2) + "demo." + api + "(\"", e = "\");" + WebUtil.line();
        sb.append(s).append(freq).append("\", \"").append(fresp).append(e);

        int count = WebUtil.str2int(append);
        if (count > 1)
        {
            int pos1 = freq.indexOf("."), i1 = (pos1 == -1 ? -1 : WebUtil.str2int(freq.substring(0, pos1), -1));
            int pos2 = fresp.indexOf("."), i2 = (pos2 == -1 ? -1 : WebUtil.str2int(fresp.substring(0, pos2), -1));
            if (i1 != -1 && i2 != -1)
            {
                String e1 = freq.substring(pos1), e2 = fresp.substring(pos2);
                for (int i = 1; i < count; i++)
                    sb.append(s).append((i1 + i) + e1).append("\", \"").append((i2 + i) + e2).append(e);
            }
        }
        else if (append.startsWith("req:"))
        {
            String[] arr = append.substring(4).split(",");
            for (String str : arr)
                sb.append(s).append(str).append("\", \"").append(fresp).append(e);
        }
        else if (append.startsWith("resp:"))
        {
            String[] arr = append.substring(5).split(",");
            for (String str : arr)
                sb.append(s).append(freq).append("\", \"").append(str).append(e);
        }
        sb.append(blanks(1)).append("}").append(WebUtil.line());

        sb.append("}").append(WebUtil.line());
        sb.append(WebUtil.line());
        String content = sb.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
        String file = "/demo/" + cname + ".java", bk0 = FileUtil.readFile(dir_out + file), bk1 = FileUtil.readFile(dir_out_bk1 + file);
        FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file);
        FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file);
        FileUtil.writeFile(content, dir_out + file);
        if (apply)
            FileUtil.writeFile(content, dir_java + file);

        // API methods in APIUtil.java
        util(request, response);
    }

    private static void util(BeanMain request, BeanMain response)
    {
        String file = "/APIUtil.java", req_mcname = request.mcname(), resp_mcname = response.mcname();
        StringBuffer sb = new StringBuffer(FileUtil.exist(1, dir_out + file) == null ? WebUtil.line() : FileUtil.readFile(dir_out + file));
        String head = blanks(1) + "// parse request xml to " + req_mcname + " bean" + WebUtil.line();
        String body = blanks(1) + "public static boolean is" + req_mcname + "(String xml)" + WebUtil.line();
        body += blanks(1) + "{" + WebUtil.line();
        body +=
                blanks(2) + "return (xml.indexOf(\"<\" + " + req_mcname + ".TAG_NAME + \" \") != -1"
                        + (request.close ? " || xml.indexOf(\"<\" + " + req_mcname + ".TAG_NAME + \">\") != -1" : "") + ");" + WebUtil.line();
        body += blanks(1) + "}" + WebUtil.line();
        body += blanks(1) + "public static " + req_mcname + " parse" + req_mcname + "(String xml)" + WebUtil.line();
        body += blanks(1) + "{" + WebUtil.line();
        body += blanks(2) + "return (" + req_mcname + ")new " + req_mcname + "().fromXml(xml);" + WebUtil.line();
        String tail = blanks(1) + "}" + WebUtil.line() + WebUtil.line();
        api(head, body, tail, sb);
        head = blanks(1) + "// parse response xml to " + resp_mcname + " bean" + WebUtil.line();
        body = blanks(1) + "public static boolean is" + resp_mcname + "(String xml)" + WebUtil.line();
        body += blanks(1) + "{" + WebUtil.line();
        body +=
                blanks(2) + "return (xml.indexOf(\"<\" + " + resp_mcname + ".TAG_NAME + \" \") != -1"
                        + (response.close ? " || xml.indexOf(\"<\" + " + resp_mcname + ".TAG_NAME + \">\") != -1" : "") + ");" + WebUtil.line();
        body += blanks(1) + "}" + WebUtil.line();
        body += blanks(1) + "public static " + resp_mcname + " parse" + resp_mcname + "(String xml)" + WebUtil.line();
        body += blanks(1) + "{" + WebUtil.line();
        body += blanks(2) + "return (" + resp_mcname + ")new " + resp_mcname + "().fromXml(xml);" + WebUtil.line();
        api(head, body, tail, sb);
        String content = sb.toString(), dir_out_bk1 = dir_out + "_bk1", dir_out_bk2 = dir_out + "_bk2";
        String bk0 = FileUtil.readFile(dir_out + file), bk1 = FileUtil.readFile(dir_out_bk1 + file);
        FileUtil.writeFile((WebUtil.empty(bk1) ? (WebUtil.empty(bk0) ? content : bk0) : bk1), dir_out_bk2 + file);
        FileUtil.writeFile((WebUtil.empty(bk0) ? content : bk0), dir_out_bk1 + file);
        FileUtil.writeFile(content, dir_out + file);
    }

    private static void api(String head, String body, String tail, StringBuffer sb)
    {
        int pos1 = sb.indexOf(head);
        int pos2 = sb.indexOf(tail, pos1);
        if (pos1 != -1 && pos2 != -1)
            sb.replace(pos1, pos2, head + body);
        else
            sb.append(head).append(body).append(tail);
        System.out.println(head + body + tail);
    }

    private static void output(boolean req, boolean in, String cls, StringBuffer sb)
    {
        String req0 = (req ? "req" : "resp"), in0 = (in ? "in" : "out"), xml0 = (req ? "request" : "response") + " xml";
        String comment = "parse " + (in ? xml0 + " to " + cls + " bean" : cls + " bean to " + xml0), var = req0 + "_" + in0;
        sb.append(blanks(2)).append("// ").append(comment).append(WebUtil.line());
        sb.append(blanks(2)).append("String ").append(var).append(" = new File(dir_").append(in0).append(" + ").append(req0).append("_file).getAbsolutePath();")
                .append(WebUtil.line());
        String xml = (in ? "FileUtil.readFile(" + var + ");" : req0 + ".toXml();");
        sb.append(blanks(2)).append("String ").append(var).append("_xml = ").append(xml).append(WebUtil.line());
        String func = (in ? cls + " " + req0 + " = APIUtil.parse" + cls + "(" + var + "_xml);" : "FileUtil.writeFile(" + var + "_xml, " + var + ");");
        sb.append(blanks(2)).append(func).append(WebUtil.line());
        sb.append(blanks(2)).append("System.out.println(\"").append(comment).append(" - \" + ").append(var).append(");").append(WebUtil.line());
        sb.append(blanks(2)).append("System.out.println(").append(var).append("_xml);").append(WebUtil.line());
        sb.append(blanks(2)).append("System.out.println();").append(WebUtil.line());
    }

    private static String[] parse(String str)
    {
        if (WebUtil.empty(str))
            return new String[] { "", "" };
        int pos = str.lastIndexOf("|");
        return pos == -1 ? new String[] { str, "" } : new String[] { str.substring(0, pos), str.substring(pos + 1) };
    }

    private static void generate(boolean apply, String api, String files, String xpaths, String attrs, String cnames, String keyfds, String boolfds, String intfds, String datefds,
            String append)
    {
        String[][] arr = new String[][] { parse(files), parse(xpaths), parse(attrs), parse(cnames), parse(keyfds), parse(boolfds), parse(intfds), parse(datefds) };
        BeanMain request = new BeanMain(apply, arr[0][0], arr[1][0], arr[2][0], arr[3][0], arr[4][0], arr[5][0], arr[6][0], arr[7][0]);
        BeanMain response = new BeanMain(apply, arr[0][1], arr[1][1], arr[2][1], arr[3][1], arr[4][1], arr[5][1], arr[6][1], arr[7][1]);

        StringBuffer sb = new StringBuffer();
        request.beans(true, sb);
        response.beans(false, sb);
        demo(append, apply, api, sb, request, response);
    }

    private static String dir_java = System.getProperty("user.dir") + "/src/com/neulion/nlss/message";
    private static String dir_out = "c:/test/message";
    private static String[][] api_all = new String[][] {
            // api, files, xpaths, attrs, cnames, keyfds, boolfds, intfds, datefds, append
            { "updateLiveSchedule", "1.updateLiveSchedule.xml|1.updateLiveScheduleResponse.xml",
                    "//updateLiveSchedule,//updateLiveSchedule/programs/program,//updateLiveSchedule/programs/program/streams/stream|//updateLiveScheduleResponse", "close,|,nlss",
                    "UpdateLiveScheduleRequest,LiveScheduleProgram,LiveScheduleStream|", "client#name,programId+type,|client+nlssId", "cc|",
                    "season,gameType,stopEncoderAfter,programId,numberOfCameras|rc", "beginDate,endDate,beginDatetime,endDatetime|", "2" },
            { "deleteLiveSchedule", "3.deleteLiveSchedule.xml|3.deleteLiveScheduleResponse.xml", "//deleteLiveSchedule|//deleteLiveScheduleResponse", "close,|,nlss",
                    "DeleteLiveScheduleRequest|", "client|client+nlssId", "", "programId|rc", "", "" },
            { "endLiveSchedule", "4.endLiveSchedule.xml|4.endLiveScheduleResponse.xml", "//endLiveSchedule|//endLiveScheduleResponse", "close,|,nlss", "EndLiveScheduleRequest|",
                    "client|client+nlssId", "", "programId|rc", "", "" },
            { "syncProgram", "5.syncProgram.xml|5.syncProgramResponse.xml", "//syncProgram|//syncProgramResponse", "close,|,nlss", "SyncProgramRequest|", "client|client+nlssId",
                    "", "programId|rc", "", "" },
            {
                    "updateLiveProgram",
                    "6.updateLiveProgram.xml|6.updateLiveProgramResponse.xml",
                    "//updateLiveProgram,//updateLiveProgram/programs/program,//updateLiveProgram/programs/program/pushUrl,"
                            + "//updateLiveProgram/programs/program/playbackUrls/playbackUrl|//updateLiveProgramResponse", "close,nlss|,",
                    "UpdateLiveProgramRequest,LiveProgram,LivePushUrl,LivePlaybackUrl|", "client,programId,url,device+url|client+nlssId", "", "programId,bitrate|rc", "", "2" },
            { "updateArchiveProgram", "8.updateArchiveProgram.xml|8.updateArchiveProgramResponse.xml",
                    "//updateArchiveProgram,//updateArchiveProgram/playbackUrls/playbackUrl|//updateArchiveProgramResponse", "close,nlss|,",
                    "UpdateArchiveProgramRequest,LiveAdmuxPlaybackUrl|", "client,device+url|client+nlssId", "additionalAudio|",
                    "programId,cmsProgramId,duration,bitrate|rc,cmsProgramId", "", "" },
            { "updateHighlightProgram", "9.updateHighlightProgram.xml|9.updateHighlightProgramResponse.xml",
                    "//updateHighlightProgram,//updateHighlightProgram/playbackUrls/playbackUrl|//updateHighlightProgramResponse", "close,nlss|,",
                    "UpdateHighlightProgramRequest,LiveAdmuxPlaybackUrl|", "client,device+url|client+nlssId", "additionalAudio|",
                    "programId,cmsProgramId,duration,bitrate|rc,cmsProgramId", "", "" },
            {
                    "vodRequest",
                    "10.vodRequest.xml|10.vodStatusResponse.xml",
                    "//vodRequest,//vodRequest/devices/device,//vodRequest/profiles,//vodRequest/profiles/profile,"
                            + "//vodRequest/sources/source|//vodStatusResponse,//vodStatusResponse/playbackUrls/playbackUrl", ",|,nlss",
                    ",VodDevice,VodProfiles,VodProfile,VodSource|,VodPlaybackUrl", "client+id,name+protocol,name+profile,name," + "http_file#unc_file|client+id,device+url",
                    "|cancelled,finished,has_error", "prog_id|priority,percent_complete,bitrate,prog_id,status,total_duration,total_file_size,total_proc_time",
                    "creation_time|creation_time,execute_time,last_update", "resp:10.vodStatusResponse0.xml" } };

    // BeanMain -api updateLiveSchedule -files "1.updateLiveSchedule.xml|1.updateLiveScheduleResponse.xml"
    // -xpaths "//updateLiveSchedule/programs/program/streams/stream,//updateLiveSchedule,//updateLiveSchedule/programs/program|//updateLiveScheduleResponse"
    // -attrs "close,|,nlss" -cnames "LiveScheduleStream,UpdateLiveScheduleRequest,LiveScheduleProgram|"
    // -keyfds ",client#name,programID+type|client+nlssId"
    // -boolfds "cc|"
    // -intfds "season,gameType,stopEncoderAfter,programID,numberOfCameras|rc"
    // -datefds "beginDate,endDate,beginDatetime,endDatetime|"
    // -append 2 -apply
    public static void main(String[] args)
    {
        test();
        Options opts = new Options();
        Option opt = new Option("api", "api", true, "the message api name");
        opt.setRequired(true);
        opts.addOption(opt);
        opt = new Option("files", "files", true, "the message xml files name, request|response");
        opt.setRequired(true);
        opts.addOption(opt);
        opt = new Option("xpaths", "xpaths", true, "the xpaths name, request|response");
        opt.setRequired(true);
        opts.addOption(opt);
        opts.addOption("attrs", "attrs", true, "the root node has attrs or not, request|response");
        opts.addOption("cnames", "cnames", true, "the bean classes name, request|response");
        opts.addOption("keyfds", "keyfds", true, "the bean key fields name, request|response");
        opts.addOption("boolfds", "boolfds", true, "the boolean fields name, request|response");
        opts.addOption("intfds", "intfds", true, "the int fields name, request|response");
        opts.addOption("datefds", "datefds", true, "the date fields name, request|response");
        opts.addOption("append", "append", true, "the demo append count");
        opts.addOption("apply", "apply", false, "apply the all classes");
        String format = "BeanMain -api -files -xpaths [-attrs] [-cnames] [-keyfds] [-boolfds] [-intfds] [-datefds] [-append] [-apply] [-h/--help] ";
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        CommandLine cl = null;
        try
        {
            cl = parser.parse(opts, args);
        }
        catch (ParseException e)
        {
            formatter.printHelp(format, opts);
        }
        if (cl == null)
            return;

        if (cl.hasOption("h") || cl.hasOption("help"))
        {
            formatter.printHelp(format, opts);
            return;
        }
        System.out.println("main args, " + WebUtil.join(" ", args));

        boolean apply = cl.hasOption("apply");
        String api = cl.getOptionValue("api");
        if ("api_all".equals(api))
            // api, files, xpaths, attrs, cnames, keyfds, boolfds, intfds, datefds, append
            for (int i = 0; i < api_all.length; i++)
                generate(apply, api_all[i][0], api_all[i][1], api_all[i][2], api_all[i][3], api_all[i][4], api_all[i][5], api_all[i][6], api_all[i][7], api_all[i][8],
                        api_all[i][9]);
        else
            generate(apply, api, cl.getOptionValue("files"), cl.getOptionValue("xpaths"), (cl.hasOption("attrs") ? cl.getOptionValue("attrs") : ""),
                    (cl.hasOption("cnames") ? cl.getOptionValue("cnames") : ""), (cl.hasOption("keyfds") ? cl.getOptionValue("keyfds") : ""),
                    (cl.hasOption("boolfds") ? cl.getOptionValue("boolfds") : ""), (cl.hasOption("intfds") ? cl.getOptionValue("intfds") : ""),
                    (cl.hasOption("datefds") ? cl.getOptionValue("datefds") : ""), (cl.hasOption("append") ? cl.getOptionValue("append") : ""));
    }

    private static void test()
    {
        System.out.println();
    }
}
