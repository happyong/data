package com.arma.web.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import com.arma.web.util.WebUtil;

public class JettyServer
{
    private int port = 6680;
    private int maxThreads = -1;
    private String contextPath = String.valueOf('/');
    private String resourceBase = null;

    private String name = _name_server + port;
    private Server server;

    private static final String _name_server = JettyServer.class.getName() + ".SERVER@";
    private static final Log _logger = LogFactory.getLog(JettyServer.class);

    public JettyServer(int port, int maxThreads, String contextPath, String resourceBase)
    {
        if (port > 1024)
            this.port = port;
        this.maxThreads = maxThreads;
        if (!WebUtil.empty(contextPath))
            this.contextPath = contextPath;
        this.resourceBase = resourceBase;
        this.name = _name_server + port;
    }

    public boolean init()
    {
        boolean result = false;
        if (port > 0 && !WebUtil.empty(resourceBase))
        {
            final Server server = new Server(new QueuedThreadPool(Math.max(maxThreads, 32)));
            final ServerConnector connector = new ServerConnector(server, null, null, null, -1, -1, new HttpConnectionFactory());
            connector.setPort(port);
            server.addConnector(connector);

            final WebAppContext context = new WebAppContext();
            context.setContextPath(contextPath);
            context.setResourceBase(resourceBase);
            server.setHandler(context);

            try
            {
                server.start();
                result = true;
            }
            catch (final Exception e)
            {
                _logger.error("unexpected exception: " + e, e);
            }
            finally
            {
                if (result)
                    this.server = server;
                else
                {
                    try
                    {
                        server.stop();
                    }
                    catch (final Exception e)
                    {
                        _logger.error("unexpected exception: " + e, e);
                    }
                }
            }
        }
        return result;
    }

    public boolean destroy()
    {
        if (server != null)
        {
            try
            {
                server.stop();
                return true;
            }
            catch (final Exception e)
            {
                _logger.error("unexpected exception: " + e, e);
            }
        }
        return false;
    }

    public String getName()
    {
        return name;
    }
}
