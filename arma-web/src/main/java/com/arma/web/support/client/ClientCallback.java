package com.arma.web.support.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

public class ClientCallback implements FutureCallback<HttpResponse>, IReqScope
{
    private IReqCallback _callback;
    private final long _startTime = System.currentTimeMillis();
    private volatile String _context = null;
    protected static final Log _logger = LogFactory.getLog(ClientCallback.class);

    public ClientCallback(IReqCallback callback)
    {
        _callback = callback;
    }

    @Override
    public String getContext()
    {
        return _context;
    }

    @Override
    public void setContext(String context)
    {
        this._context = context;
    }

    @Override
    public long getClientCost()
    {
        return (System.currentTimeMillis() - _startTime);
    }

    @Override
    public void cancelled()
    {
        try
        {
            _callback.cancelled(this);
        }
        catch (Throwable e)
        {
            _logger.error("unexpected exception : " + e.getMessage(), e);
        }
    }

    @Override
    public void failed(Exception ex)
    {
        try
        {
            _callback.failed(this, ex);
        }
        catch (Throwable e)
        {
            _logger.error("unexpected exception : " + e.getMessage(), e);
        }
    }

    @Override
    public void completed(HttpResponse result)
    {
        try
        {
            _callback.completed(this, result);
        }
        catch (Throwable e)
        {
            _logger.error("unexpected exception : " + e.getMessage(), e);
        }
    }
}
