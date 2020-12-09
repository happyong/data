package com.arma.web.support.client;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

@Contract(threading = ThreadingBehavior.SAFE)
public class StorelessCookieStore implements CookieStore, Serializable
{
    private static final long serialVersionUID = 3394255905060312861L;
    private static final List<Cookie> empty = new LinkedList<Cookie>();
    protected static final StorelessCookieStore instance = new StorelessCookieStore();

    @Override
    public void addCookie(Cookie cookie)
    {
    }

    @Override
    public List<Cookie> getCookies()
    {
        return empty;
    }

    @Override
    public boolean clearExpired(Date date)
    {
        return false;
    }

    @Override
    public void clear()
    {
    }
}
