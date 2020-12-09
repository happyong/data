/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.service;

import java.util.Map;

import com.arma.web.service.bean.Paging;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.savanna.common.db.DBIterator;
import com.neulion.savanna.common.db.DBSession;
import com.neulion.savanna.common.db.DBSource;

public abstract class AbstractBaseService
{
    protected DBSource m_ds = null;
    protected long m_pageSize = 100;

    @SuppressWarnings("unchecked")
    protected Map<String, Object>[] query(String sql, Object[] args, Paging paging)
    {
        if (WebUtil.empty(sql))
            return new Map[0];

        Map[] arr = null;
        DBSession session = null;
        long start = (paging == null ? 0L : paging.getStartIndex()), max = (paging == null ? -1L : paging.getPageSize());
        try
        {
            session = m_ds.createSession();
            DBIterator it = session.query(null, sql, args, start, max);
            arr = (Map<String, Object>[]) it.toArray();
            if (max >= 0)
                paging.setCount(it.getRecordCount());
        }
        finally
        {
            if (session != null)
                session.close();
        }
        return (arr == null ? new Map[0] : arr);
    }

    protected int update(String sql, Object[] args)
    {
        int ret = -1;
        DBSession session = null;
        try
        {
            session = m_ds.createSession();
            ret = session.update(sql, args);
        }
        finally
        {
            if (session != null)
            {
                session.commit();
                session.close();
            }
        }
        return ret;
    }

    public void setDbSource(DBSource dbsource)
    {
        this.m_ds = dbsource;
    }
}
