package com.arma.web.util;

import org.apache.log4j.xml.DOMConfigurator;

public class ArmaUtil
{
    public static String config()
    {
        String user_dir = System.getProperty("user.dir");
        DOMConfigurator.configure(user_dir + "/webapps/dummy/WEB-INF/conf/log4j.xml");
        return user_dir;
    }
}
