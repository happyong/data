/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.common.timer;

import org.quartz.JobExecutionContext;

import com.neulion.iptv.web.util.timer.MethodJob;

public class KmsTimerTask extends MethodJob
{
    // flush keywords with rank, per 2 minutes, not used
    public void refreshKeywords(JobExecutionContext context)
    {
        // KmsCacher.refreshKeywords();
    }
}
