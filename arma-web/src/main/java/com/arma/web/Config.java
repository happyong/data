package com.arma.web;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import com.arma.web.util.WebUtil;

public class Config extends DefaultHandler2
{
    private final JettySetting dummySetting = new JettySetting();

    private String text; // only use for load configuration

    private static final Config instance = new Config();
    protected static final Log _logger = LogFactory.getLog(Config.class);

    static
    {
        instance.load("./webapps/dummy/WEB-INF/conf/config.xml");
    }

    private Config()
    {
    }

    public static JettySetting getDummySetting()
    {
        return instance.dummySetting;
    }

    private void load(String file)
    {
        try (final InputStream is = new FileInputStream(file))
        {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(is, this);
            _logger.info("load configuration, " + file);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
    }

    @Override
    public void startDocument() throws SAXException
    {
        text = null;
    }

    @Override
    public void endDocument() throws SAXException
    {
        text = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        text = "";
        if ("jetty-dummy".equals(qName))
        {
            dummySetting.enable = !"false".equals(attributes.getValue("enable"));
            dummySetting.port = WebUtil.str2int(attributes.getValue("port"), 80);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if ("dummy-base".equals(qName))
            dummySetting.webBase = text;
        text = null;
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException
    {
        if (text != null)
            text = new String(ch, start, length);

        // StringBuilder builder = new StringBuilder().append("ch, ").append(start).append('|').append(length).append('|').append(ch.length);
        // builder.append(", ").append(new String(ch, start, length).trim());
        // System.out.println(builder);
    }

    public class JettySetting
    {
        private boolean enable = true;
        private int port;
        private String webBase;

        public boolean empty()
        {
            return (!enable || port < 1 || WebUtil.empty(webBase));
        }

        public boolean isEnable()
        {
            return enable;
        }

        public int getPort()
        {
            return port;
        }

        public String getWebBase()
        {
            return webBase;
        }
    }
}
