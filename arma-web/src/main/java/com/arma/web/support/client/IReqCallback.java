package com.arma.web.support.client;

import org.apache.http.HttpResponse;

public interface IReqCallback
{
    void completed(IReqScope reqScope, HttpResponse result);

    void failed(IReqScope reqScope, Exception ex);

    void cancelled(IReqScope reqScope);
}
