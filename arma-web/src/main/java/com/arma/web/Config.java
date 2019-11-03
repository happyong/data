package com.arma.web;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import com.arma.web.server.JettyServer;
import com.arma.web.support.ThreadPool;
import com.arma.web.support.client.ClientPool;
import com.arma.web.util.WebUtil;

public class Config extends DefaultHandler2
{
    private final JettySetting dummySetting = new JettySetting();

    private volatile String text; // only use for load configuration

    private static volatile Config instance = null;
    protected static final Log _logger = LogFactory.getLog(Config.class);

    private Config()
    {
    }

    public static Config getInstance()
    {
        if (instance == null)
        {
            instance = new Config();
            instance.load("./webapps/dummy/WEB-INF/conf/config.xml");
        }
        return instance;
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

    private volatile JettyServer dummyServer;

    public String setUp()
    {
        String user_dir = System.getProperty("user.dir");
        System.setProperty("arma.home", user_dir);
        DOMConfigurator.configure(user_dir + "/webapps/dummy/WEB-INF/conf/log4j.xml");
        dummyServer = server(dummySetting);
        ClientPool.initialize();
        ThreadPool.initialize();
        return user_dir;
    }

    public void tearDown()
    {
        ThreadPool.terminate();
        ClientPool.terminate();
        if (dummyServer != null)
            dummyServer.destroy();
    }

    private JettyServer server(JettySetting setting)
    {
        if (setting == null || setting.empty())
            return null;
        JettyServer server = new JettyServer(setting.getPort(), -1, null, setting.getWebBase());
        if (server.init())
            return server;
        _logger.error("jetty server startup fail, " + setting.getPort() + " on " + setting.getWebBase());
        return null;
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
