/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service.bean;

import javax.servlet.http.HttpServletRequest;

public class Paging
{
    public static final String PAGE_NUMBER = "pn";
    public static final String PAGE_SIZE = "ps";
    public static final String SORT = "sort";

    private long m_startIndex = 0;
    private long m_pageNumber = 1;
    private long m_pageSize = 12;
    private long m_totalCount = 0;
    private String m_sort = null;
    private long m_pages = 0;
    private long m_navStart = 1;
    private long m_navEnd = 7;

    public Paging()
    {
    }

    public Paging(long pageSize, long pageNumber, String sort)
    {
        m_pageSize = pageSize;
        m_pageNumber = pageNumber;
        if (m_pageNumber < 1)
            m_pageNumber = 1;
        m_startIndex = (m_pageNumber - 1) * m_pageSize;
        if (sort != null)
            m_sort = sort;
    }

    public Paging(HttpServletRequest request)
    {
        String param = request.getParameter(PAGE_SIZE);
        if (param != null)
        {
            try
            {
                m_pageSize = Long.parseLong(param);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        param = request.getParameter(PAGE_NUMBER);
        if (param != null)
        {
            try
            {
                m_pageNumber = Long.parseLong(param);
                if (m_pageNumber < 1)
                    m_pageNumber = 1;
            }
            catch (NumberFormatException ex)
            {
            }
            m_startIndex = (m_pageNumber - 1) * m_pageSize;
        }
        param = request.getParameter(SORT);
        if (param != null)
        {
            m_sort = param;
        }
    }

    public long getStartIndex()
    {
        return m_startIndex;
    }

    public long getPageNumber()
    {
        return m_pageNumber;
    }

    public long getPageSize()
    {
        return m_pageSize;
    }

    public String getSort()
    {
        return m_sort;
    }

    public long getPages()
    {
        return m_pages;
    }

    public long getNavStart()
    {
        return m_navStart;
    }

    public long getNavEnd()
    {
        return m_navEnd;
    }

    public void setCount(long count)
    {
        m_pages = (long) Math.ceil((new Double(count)).doubleValue() / m_pageSize);

        long startCount, endCount;

        if (m_pageNumber < 4)
        {
            startCount = 1;
            endCount = 7;
        }
        else
        {
            startCount = m_pageNumber - 3;
            endCount = startCount + 6;
        }

        if (m_pageNumber > m_pages - 4)
        {
            endCount = m_pages;
            startCount = endCount - 6;
        }

        if (startCount < 1)
            startCount = 1;

        if (endCount > m_pages)
            endCount = m_pages;

        m_navStart = startCount;
        m_navEnd = endCount;

        m_totalCount = count;
    }

    public long getCount()
    {
        return m_totalCount;
    }
}
