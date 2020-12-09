package com.arma.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

public class XmlOutput4j
{
    private int _indent;
    private List<String> _tags = new ArrayList<String>();
    private StringBuilder _sb = new StringBuilder();
    private Map<String, Object> _map = null;
    private Map<String, Object> _cur_map = null;

    public XmlOutput4j()
    {
        this(0, null);
    }

    public XmlOutput4j(int indent)
    {
        this(indent, null);
    }

    public XmlOutput4j(int indent, Map<String, Object> map)
    {
        _map = map;
        _cur_map = map;
        _indent = Math.max(0, indent);
    }

    public XmlOutput4j appendHeader()
    {
        if (_map != null)
            return this;
        _sb.append(WebUtil.KEY_XML);
        return this;
    }

    public XmlOutput4j appendLine(int indent)
    {
        if (_map != null)
            return this;
        _sb.append(WebUtil.line());
        indent += _indent;
        for (int i = 0; i < indent; i++)
            _sb.append("\t");
        return this;
    }

    public XmlOutput4j openTag(String tag, String[] attrs, String[] values)
    {
        if (_map != null)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            appendAttrs(attrs, values, map);
            put(tag, map, _cur_map);
            _tags.add(tag);
            _cur_map = map;
            return this;
        }

        appendLine(_tags.size());
        _sb.append('<').append(tag);
        _sb.append(appendAttrs(attrs, values, null));
        _sb.append('>');
        _tags.add(tag);
        return this;
    }

    @SuppressWarnings("unchecked")
    public XmlOutput4j closeTag()
    {
        int index = _tags.size() - 1;
        if (index < 0)
            return this;
        String tag = _tags.get(index);
        _tags.remove(index);

        if (_map != null)
        {
            Object obj;
            Map<String, Object> map = _map;
            List<Map<String, Object>> list;
            for (String str : _tags)
            {
                if (map == null)
                    continue;
                obj = map.get(str);
                if (obj == null)
                    continue;
                if (obj instanceof List)
                {
                    list = (List<Map<String, Object>>) obj;
                    if (list.size() > 0)
                        map = list.get(list.size() - 1);
                }
                else if (obj instanceof Map)
                    map = (Map<String, Object>) obj;
            }
            _cur_map = (map == null ? new HashMap<String, Object>() : map);
            return this;
        }
        appendLine(index);
        _sb.append("</").append(tag).append('>');
        return this;
    }

    public XmlOutput4j appendString(String value)
    {
        if (_map != null)
            return this;
        _sb.append(WebUtil.unull(value));
        return this;
    }

    public XmlOutput4j appendNode(Node node)
    {
        if (node == null || !(node instanceof Element))
            return this;

        Element elem = (Element) node;
        List<String> attrs_name = new ArrayList<String>(), attrs_value = new ArrayList<String>();
        List<Attribute> attrs_list = elem.attributes();
        if (attrs_list != null)
        {
            for (Attribute attr : attrs_list)
            {
                attrs_name.add(attr.getName());
                attrs_value.add(attr.getValue());
            }
        }
        String tag = node.getName();
        if (!elem.isTextOnly())
        {
            openTag(tag, attrs_name.toArray(new String[attrs_name.size()]), attrs_value.toArray(new String[attrs_value.size()]));
            List<Element> children = ParseUtil.elements(elem);
            if (children != null)
                for (Node e : children)
                    appendNode(e);
            closeTag();
        }
        else
        {
            boolean cdata = ParseUtil.isCDATA(node);
            String text = node.getText();
            if (text != null)
                text = text.trim();
            if (WebUtil.empty(text))
                text = null;
            appendTag(cdata, tag, text, attrs_name.toArray(new String[attrs_name.size()]), attrs_value.toArray(new String[attrs_value.size()]));
        }
        return this;
    }

    public XmlOutput4j appendTag(boolean cdata, String tag, String value, String[] attrs, String[] values)
    {
        if (WebUtil.empty(tag))
            return this;

        if (_map != null)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            appendAttrs(attrs, values, map);
            put("value", value, map);
            put(tag, map, _cur_map);
            return this;
        }

        String attr_str = appendAttrs(attrs, values, null);
        if ((WebUtil.empty(attr_str) && WebUtil.empty(value)))
            return this;
        appendLine(_tags.size());
        _sb.append('<').append(tag);
        _sb.append(attr_str);
        if (value != null)
        {
            _sb.append('>');
            _sb.append(cdata && value.length() > 0 ? "<![CDATA[" + value + "]]>" : value);
            _sb.append("</").append(tag).append('>');
        }
        else
            _sb.append(" />");
        return this;
    }

    @SuppressWarnings("unchecked")
    public XmlOutput4j appendTag0(boolean cdata, String tag, String value, String[] attrs, String[] values)
    {
        if (WebUtil.empty(tag))
            return this;

        if (_map != null)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            appendAttrs(attrs, values, map);
            if (!WebUtil.empty(value))
                map.put("value", value);
            Object old = _cur_map.get(tag);
            List<Map<String, Object>> list = null;
            if (old == null)
            {
                list = new ArrayList<Map<String, Object>>();
                _cur_map.put(tag, list);
            }
            else if (old instanceof List)
                list = (List<Map<String, Object>>) old;
            if (list != null)
                list.add(map);
            return this;
        }
        return appendTag(cdata, tag, value, attrs, values);
    }

    private String appendAttrs(String[] attrs, String[] values, Map<String, Object> map)
    {
        if (attrs == null || values == null || attrs.length == 0 || attrs.length != values.length)
            return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attrs.length; i++)
        {
            if (map != null)
                sb.append(put(attrs[i], values[i], map));
            else if (values[i] != null)
                sb.append(" ").append(attrs[i]).append("=\"").append(values[i]).append("\"");
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String put(String key, Object value, Map<String, Object> map)
    {
        if (WebUtil.empty(key) || value == null || map == null)
            return "";

        Object old = map.get(key);
        boolean is_null = (old == null);
        boolean is_map = (!is_null && old instanceof Map);
        boolean is_list = (!is_null && !is_map && old instanceof List);
        if (is_null || (!is_map && !is_list))
        {
            map.put(key, value);
            return key;
        }

        List<Map<String, Object>> list = null;
        if (is_list)
            list = (List<Map<String, Object>>) old;
        else if (is_map)
        {
            list = new ArrayList<Map<String, Object>>();
            list.add((Map) old);
        }

        if (list == null || !(value instanceof Map))
            return "";
        list.add((Map) value);
        map.put(key, list);
        return key;
    }

    public String output()
    {
        for (int i = _tags.size() - 1; i >= 0; i--)
            closeTag();
        return _sb.toString();
    }
}
