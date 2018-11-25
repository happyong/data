/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import java.util.HashMap;
import java.util.Map;

import org.quartz.SimpleScheduleBuilder;

import com.arma.web.servlets.common.timer.FundTimerTask;
import com.arma.web.util.ArmaUtil;
import com.neulion.iptv.web.util.timer.QuartzHelper;

public class FundMain
{
    public static void main(String[] args)
    {
        FundMain util = new FundMain();
        // load configuration and setup
        util.setup();
    }

    // load configuration and setup
    private void setup()
    {
        ArmaUtil.config();

        SimpleScheduleBuilder ssb;
        Map<String, Object> map = new HashMap<String, Object>();

        // update fund cacher, per 30 minutes
        map.put(QuartzHelper.METHOD, "updateFundCacher");
        ssb = simpleSchedule().withIntervalInMinutes(30).repeatForever();
        QuartzHelper.start(null, map, FundTimerTask.class, ssb);
        try
        {
            Thread.sleep(100L);
        }
        catch (Exception e)
        {
        }

        // update fund real-time stats, per 50 seconds
        map.put(QuartzHelper.METHOD, "updateFundRtStats");
        ssb = simpleSchedule().withIntervalInSeconds(50).repeatForever();
        QuartzHelper.start(null, map, FundTimerTask.class, ssb);
        try
        {
            Thread.sleep(100L);
        }
        catch (Exception e)
        {
        }
    }
}
