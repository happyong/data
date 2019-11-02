package com.arma.web.support.client;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLInitializationException;

public abstract class ClientPool
{
    protected static final Log _logger = LogFactory.getLog(ClientPool.class);

    private static volatile CloseableHttpAsyncClient _client = null;
    private static volatile PoolingNHttpClientConnectionManager _manager = null;

    public static void initialize()
    {
        if (_client == null)
        {
            try
            {
                final RequestConfig requestConfig =
                        RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();

                final IOReactorConfig reactorConfig = IOReactorConfig.custom().setConnectTimeout(connectTimeout).setSoTimeout(socketTimeout).setIoThreadCount(ioThread).build();
                final ConnectingIOReactor reactor = new DefaultConnectingIOReactor(reactorConfig, ClientThreadFactory.getInstance());

                _manager = new PoolingNHttpClientConnectionManager(reactor, getDefaultRegistry());
                _manager.setMaxTotal(maxTotal);
                _manager.setDefaultMaxPerRoute(defaultMaxPerRoute);

                final HttpAsyncClientBuilder clientConfig =
                        HttpAsyncClients.custom().setConnectionManager(_manager).setDefaultRequestConfig(requestConfig).setThreadFactory(ClientThreadFactory.getInstance())
                                .setDefaultCookieStore(StorelessCookieStore.instance)
                                .setConnectionReuseStrategy(reuseConn ? DefaultConnectionReuseStrategy.INSTANCE : NoConnectionReuseStrategy.INSTANCE).setUserAgent(userAgent);

                _client = clientConfig.build();
                // _pipeline = HttpAsyncClients.createPipelining(_manager, true);

                _client.start();
                // _pipeline.start();

                if (_logger.isTraceEnabled())
                {
                    StringBuilder builder = new StringBuilder("start http async client. (");
                    builder.append("maxTotal=").append(_manager.getMaxTotal());
                    builder.append(", defaultMaxPerRoute=").append(_manager.getDefaultMaxPerRoute());
                    builder.append(", ioThread=").append(reactorConfig.getIoThreadCount());
                    builder.append(", timeout=").append(connectTimeout).append(")");

                    _logger.trace(builder.toString());
                }
            }
            catch (IOException e)
            {
                _logger.error("unexpected exception : " + e.getMessage(), e);
            }
        }
    }

    public static void terminate()
    {
        if (_client != null)
        {
            try
            {
                _client.close();
                // _pipeline.close();

                if (_logger.isTraceEnabled())
                    _logger.trace("close http async client.");
            }
            catch (Exception e)
            {
            }
            finally
            {
                _client = null;
                // _pipeline = null;
                _manager = null;
            }
        }
    }

    public static Future<HttpResponse> execute(final HttpAsyncRequestProducer producer, final HttpAsyncResponseConsumer<HttpResponse> consumer, final HttpContext context,
            final FutureCallback<HttpResponse> callback)
    {
        if (context == null)
            return _client.execute(producer, consumer, callback);
        return _client.execute(producer, consumer, context, callback);
    }

    // public static void execute(final HttpHost target,
    // final List<HttpAsyncRequestProducer> producers,
    // final List<HttpAsyncResponseConsumer<HttpResponse>> consumers,
    // final HttpContext context, final FutureCallback<List<HttpResponse>> callback)
    // {
    // if (context == null)
    // _pipeline.execute(target, producers, consumers, callback);
    // else
    // _pipeline.execute(target, producers, consumers, context, callback);
    // }

    public static int getAllRoutesPending()
    {
        try
        {
            int pending = 0;
            for (HttpRoute route : _manager.getRoutes())
            {
                pending += _manager.getStats(route).getPending();
            }
            return pending;
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e.getMessage(), e);
        }
        return -1;
    }

    public static void closeIdleConnections(final long idleTimeout, final TimeUnit tunit)
    {
        _manager.closeExpiredConnections();
        _manager.closeIdleConnections(idleTimeout, tunit);
    }

    public static StringBuilder report(final StringBuilder builder)
    {
        try
        {
            for (final HttpRoute route : _manager.getRoutes())
            {
                final PoolStats stats = _manager.getStats(route);

                builder.append('\n').append('\t').append('-').append(' ');
                builder.append(StringUtils.rightPad(route.getTargetHost().getHostName(), 30, ' '));
                builder.append("# leased:").append(StringUtils.leftPad(String.valueOf(stats.getLeased()), 4));
                builder.append(", pending:").append(StringUtils.leftPad(String.valueOf(stats.getPending()), 4));
                builder.append(", available:").append(StringUtils.leftPad(String.valueOf(stats.getAvailable()), 4));
                builder.append(", max:").append(StringUtils.leftPad(String.valueOf(stats.getMax()), 4));
            }
        }
        catch (Exception e)
        {
            // ignore
        }
        return builder;
    }

    private static Registry<SchemeIOSessionStrategy> getDefaultRegistry()
    {
        final TrustManager manager = new X509TrustManager()
        {
            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
            {
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
            {
            }
        };

        // final HostnameVerifier verifier = new HostnameVerifier()
        // {
        // public boolean verify(String arg0, SSLSession arg1)
        // {
        // return true;
        // }
        // };

        try
        {
            final SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { manager }, null);

            RegistryBuilder<SchemeIOSessionStrategy> builder = RegistryBuilder.<SchemeIOSessionStrategy> create();
            builder.register("http", NoopIOSessionStrategy.INSTANCE);
            builder.register("https", new SSLIOSessionStrategy(sslcontext));
            return builder.build();
        }
        catch (final GeneralSecurityException ex)
        {
            throw new SSLInitializationException(ex.getMessage(), ex);
        }
    }

    private static final int _processors = Runtime.getRuntime().availableProcessors();
    private static final int ioThread = _processors * 2;
    private static final int connectTimeout = 10000;
    private static final int socketTimeout = 10000;
    private static final int connectionRequestTimeout = 10000;
    private static final int maxTotal = _processors * 192;
    private static final int defaultMaxPerRoute = _processors * 6;
    private static boolean reuseConn = true;
    private static String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
}
