package com.arma.web.util.func;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.arma.web.Config;
import com.arma.web.InVarAM;
import com.arma.web.util.WebUtil;

public class TextUtil
{
    private static Log _logger = LogFactory.getLog("web.AmazonEC2Helper");

    protected void line1()
    {
        String base = "k:/other/memoire/novel/", read = "zrzs.txt", write = "zrzs2.txt";
        _logger.info("line1 start, " + read);
        try
        {
            List<String> lines0 = FileUtils.readLines(new File(base + read), WebUtil.CHARSET_GB2312), lines = new ArrayList<String>();
            int sz = (lines0 == null ? 0 : lines0.size());
            if (sz > 0)
            {
                String head = InVarAM.key_blank_gb2312 + InVarAM.key_blank_gb2312;
                for (String line0 : lines0)
                {
                    String line = line0.trim();
                    if (line.length() > 0)
                    {
                        String text = head + StringUtils.remove(line, InVarAM.char_blank_gb2312);
                        lines.add(text);
                    }
                }
                FileUtils.writeLines(new File(base + write), WebUtil.CHARSET_GB2312, lines, WebUtil.LINE_WIN);
            }
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        _logger.info("line1 stop, " + read);
    }

    public static void main(String[] args)
    {
        Config.getInstance().setUp();

        TextUtil test = new TextUtil();
        test.line1();

        Config.getInstance().tearDown();
    }
}
