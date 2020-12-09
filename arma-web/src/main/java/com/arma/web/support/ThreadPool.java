package com.arma.web.support;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.arma.web.support.client.ClientThreadFactory;

public class ThreadPool
{
    private static volatile ScheduledThreadPoolExecutor _threadPool = null;

    public static void initialize()
    {
        if (_threadPool == null)
            _threadPool = new ScheduledThreadPoolExecutor(threadSize, ClientThreadFactory.getInstance());
    }

    public static ScheduledFuture<?> schedule(Runnable command, long delay)
    {
        return _threadPool.schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    public static void execute(Runnable command)
    {
        _threadPool.execute(command);
    }

    public static Executor getExecutor()
    {
        return _threadPool;
    }

    public static ScheduledExecutorService getScheduler()
    {
        return _threadPool;
    }

    public static void terminate()
    {
        if (_threadPool == null)
        {
            return;
        }
        try
        {
            _threadPool.shutdown();
            for (;;)
            {
                try
                {
                    if (_threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS))
                        return;
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        finally
        {
            _threadPool = null;
        }
    }

    public static StringBuilder report(final StringBuilder builder)
    {
        if (_threadPool != null)
        {
            final String text = _threadPool.toString();
            final int i;
            if ((i = text.indexOf('[')) > -1)
            {
                builder.append('\n').append('\t').append('-').append(' ');
                builder.append(StringUtils.replace(text.substring(i), " =", ":"));
            }
        }
        return builder;
    }

    private static int threadSize = 32;
}
