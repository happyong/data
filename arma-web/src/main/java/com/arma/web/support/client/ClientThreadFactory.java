package com.arma.web.support.client;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientThreadFactory implements ThreadFactory
{
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private static final ClientThreadFactory _instance = new ClientThreadFactory();

    public static ClientThreadFactory getInstance()
    {
        return _instance;
    }

    private ClientThreadFactory()
    {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r)
    {
        return newThread(r, "client-");
    }

    public Thread newThread(Runnable r, String name)
    {
        final int priority = Thread.NORM_PRIORITY + 2;
        final Thread t = new Thread(group, r, name + threadNumber.getAndIncrement(), 0);

        if (t.isDaemon())
        {
            t.setDaemon(false);
        }
        if (t.getPriority() != priority)
        {
            t.setPriority(priority);
        }
        return t;
    }

    public int getThreadNumber()
    {
        return threadNumber.get();
    }
}
