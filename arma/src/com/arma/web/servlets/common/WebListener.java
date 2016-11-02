/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.common;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.quartz.SimpleScheduleBuilder;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.util.WebUtils;

import com.arma.web.config.CommonConfig;
import com.arma.web.config.ConfigHelper;
import com.arma.web.config.ConfigHelper.PortalMenu;
import com.arma.web.servlets.common.timer.FundTimerTask;
import com.arma.web.servlets.fund.FundCacher;
import com.arma.web.servlets.kms.KmsCacher;
import com.neulion.iptv.web.GlobalCache;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.KeyUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.timer.QuartzHelper;

public class WebListener implements ServletContextListener
{		
	private static final Logger _logger = Logger.getLogger(WebListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		// initialize
		ServletContext sc = sce.getServletContext();

		ConfigHelper.sep = WebUtil.sep(WebUtil.unix());
		GlobalCache._config_root = WebUtil.path0(PortalUtil.getRealPath0(WebUtil.FOLDER_CONFIG, sc));
		ConfigHelper.web = WebUtil.pathadd(WebUtil.path(GlobalCache._config_root + ConfigHelper.sep + ".."));
		WebUtils.setWebAppRootSystemProperty(sc);
		initLog4j();
		_logger.info("key.aes is " + (KeyUtil.check(true) ? "valid" : "invalid"));
		WebUtil.printSystemInfo(WebUtil.builds());
		GlobalCache.getInstance().initThreadPools(-1);
		boolean valid = checkSpringConfig();
		if (!valid) return;

		new Thread()
		{
			public void run() 
			{
		        if (ConfigHelper.common.show(PortalMenu.funds))
		        {
    				try { Thread.sleep(5000L); } catch (Exception e) {}
    				FundCacher.setup();
		        }
                if (ConfigHelper.common.show(PortalMenu.kms)) KmsCacher.setup();
				try { Thread.sleep(1000L); } catch (Exception e) {}
				startTimer();
				try { Thread.sleep(5000L); } catch (Exception e) {}
				ConfigHelper.rebooted = true;
			}
		}.start();
		_logger.info("bootstrap, root|" + ConfigHelper.web);
	}
	
	private void initLog4j()
	{
		String path = GlobalCache._config_root + ConfigHelper.sep + "log4j.xml";
		DOMConfigurator.configure(path);
	}

	private boolean checkSpringConfig()
	{
		String path = GlobalCache._config_root + ConfigHelper.sep + "config.xml";
		FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(path);
		GlobalCache.getInstance().setBeanFactory(ctx);
		GlobalCache.getInstance().setCache("ctx_", ctx);
		
		// set environment variables
		ConfigHelper.reboot = System.currentTimeMillis();
		ConfigHelper.common = new CommonConfig();
		_logger.info("ARMA.Portals=" + ConfigHelper.common.getPortals());
		return true;
	}
	
	private void startTimer()
	{		
		SimpleScheduleBuilder ssb;
		Map<String, Object> map = new HashMap<String, Object>();

		if (ConfigHelper.common.show(PortalMenu.funds))
		{
    		// update fund cacher, per 30 minutes
    		map.put(QuartzHelper.METHOD, "updateFundCacher");
    		ssb = simpleSchedule().withIntervalInMinutes(30).repeatForever();
    		QuartzHelper.start(null, map, FundTimerTask.class, ssb); 
    		try { Thread.sleep(100L); } catch (Exception e) {}
    
    		// update fund real-time stats, per 50 seconds
    		map.put(QuartzHelper.METHOD, "updateFundRtStats");
    		ssb = simpleSchedule().withIntervalInSeconds(50).repeatForever();
    		QuartzHelper.start(null, map, FundTimerTask.class, ssb); 
    		try { Thread.sleep(100L); } catch (Exception e) {}
		}

        if (ConfigHelper.common.show(PortalMenu.kms))
        {
            // flush keywords with rank, per 2 minutes, not used
            /*
            map.put(QuartzHelper.METHOD, "refreshKeywords");
            ssb = simpleSchedule().withIntervalInMinutes(2).repeatForever();
            QuartzHelper.start(null, map, KmsTimerTask.class, ssb); 
            try { Thread.sleep(100L); } catch (Exception e) {}*/
        }
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) 
	{		
		GlobalCache.getInstance().release();
		_logger.info("terminate, release system resources");
	}
}
