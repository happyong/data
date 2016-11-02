/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.common.timer;

import org.quartz.JobExecutionContext;

import com.arma.web.config.ConfigHelper;
import com.arma.web.servlets.fund.FundCacher;
import com.neulion.iptv.web.util.timer.MethodJob;

public class FundTimerTask extends MethodJob
{
	// flush quotes daily and net daily via scanSinaQuoteJs, scanSinaNetJs, also normalize them, for the date after last_trade, per 30 minutes
	public void updateFundCacher(JobExecutionContext context)
	{
		if (!ConfigHelper.rebooted) return;
		FundCacher.flush();
	}

	// flush real-time stats via scanSinaQuoteJs and normalize real-time stats to fill the high_60, low_60, premium, premium2, premium_high_5, premium_low_5, per 50 seconds
	public void updateFundRtStats(JobExecutionContext context)
	{
		if (!FundCacher.needUpdateRtStats()) return;
		FundCacher.flushRtStats();
	}
}
