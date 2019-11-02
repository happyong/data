package com.arma.web.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Branch;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class ParseUtil
{
    private static final Log _logger = LogFactory.getLog("web.ParseUtil");

    public static Element parseRoot(String xml)
    {
        return parseRoot(xml, null);
    }

    public static Element parseRoot(String xml, String encoding)
    {
        Element root = null;
        if (WebUtil.empty(xml))
            return root;

        long start = WebUtil.time(0);
        try
        {
            int pos = xml.indexOf("<?xml ");
            if (pos > 0)
                xml = xml.substring(pos);
            SAXReader reader = new SAXReader();
            reader.setEncoding(WebUtil.empty(encoding) ? WebUtil.CHARSET_UTF_8 : encoding);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            root = reader.read(new StringReader(xml)).getRootElement();
        }
        catch (Exception e)
        {
            _logger.error("PARSE ERROR parseRoot, message|" + e);
        }
        if (_logger.isDebugEnabled())
            _logger.debug("PARSE parseRoot, time|" + WebUtil.time2str(start) + "|length|" + xml.length());

        return root;
    }

    public static boolean isCDATA(Node node)
    {
        if (!node.hasContent())
            return false;
        Iterator<Node> iterator = ((Branch) node).content().iterator();
        while (iterator.hasNext())
        {
            Node n = (Node) iterator.next();
            if (Node.CDATA_SECTION_NODE == n.getNodeType())
                return true;
        }
        return false;
    }

    public static List<Element> elements(Node node)
    {
        return (node == null || !(node instanceof Element) ? null : ((Element) node).elements());
    }

    public static Element node(String[] names, Element node)
    {
        List<Element> result = new ArrayList<Element>(), childs = ParseUtil.elements(node);
        if (childs != null)
            for (Element child : childs)
                if (!nodes(true, 0, names, result, child))
                    break;
        return (result.size() > 0 ? result.get(0) : null);
    }

    public static List<Element> nodes(String[] names, Element node)
    {
        List<Element> result = new ArrayList<Element>(), childs = ParseUtil.elements(node);
        if (childs != null)
            for (Element child : childs)
                nodes(false, 0, names, result, child);
        return result;
    }

    // return - false: break the recursion
    private static boolean nodes(boolean one, int depth, String[] names, List<Element> result, Element node)
    {
        int sz = names.length;
        String name = (depth >= 0 && depth < sz && node != null ? names[depth] : null);
        if (WebUtil.empty(name) || !name.equals(node.getName()))
            return true;
        // leaf
        if (depth == sz - 1)
        {
            result.add(node);
            return !one;
        }
        List<Element> childs = ParseUtil.elements(node);
        if (childs != null)
            for (Element child : childs)
                if (!nodes(one, depth + 1, names, result, child))
                    return false;
        return true;
    }

    // xpath - //root/node, node/subnode, node[position()=last()]/subnode, node/@attr
    public static String xpathNode(String xpath, Node node)
    {
        String str = "";
        if (WebUtil.empty(xpath) || node == null)
            return str;
        Node sub = node.selectSingleNode(xpath);
        if (sub != null)
            str = sub.getText();
        return str == null ? "" : str.trim();
    }
}
