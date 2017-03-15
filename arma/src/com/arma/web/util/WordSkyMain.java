/*
 * Copyright (c) 2011 NeuLion, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.arma.web.FundMain;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.FileUtil2;
import com.neulion.iptv.web.util.WebUtil;

/**
 * File Sky
 * 
 * $Revision: 1.01 $, $Date: 2011/07/21 14:43:22 $
 */
public class WordSkyMain
{
    private static boolean prompt = true;
    private static final Logger _logger = Logger.getLogger(WordSkyMain.class);

    public static void main(final String[] args)
    {
        FundMain.config();
        if (!file.exists()) file = new File("D:/etc/中国军史人物.txt");
        
        dotext();
        // test1();
        // testblank();
        // testdate1();
        // testdate2();
    }
    
    private static List<String> lines;
    private static File file = new File("F:/etc/中国军史人物.txt");
    protected static void dotext()
    {
        if (!file.exists()) return;
        Date date = new Date();
        final String time = DateUtil.date24Str(date, "yyyyMMdd_HHmm");
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            public void run() 
            {
                if (lines == null) return;
                long now  = System.currentTimeMillis();
                File file2 = new File(file.getParentFile(), file.getName() + "." + time + ".txt");
                FileUtil2.writeLines(lines, file2.getName(), null, WebUtil.LINE_WIN, file2.getParentFile());
                _logger.info("\nwrite file, time|" + WebUtil.time(now) + "ms|length|" + file2.length() + "byte|" + file2.getAbsolutePath());
            }
        });
        
        prompt = false;
        long now  = System.currentTimeMillis();
        _logger.info("dotext begin, " + DateUtil.str(date));
        lines = FileUtil2.readLines(file.getName(), null, file.getParentFile());
        int size = lines.size(); 
        _logger.info("\nread file, time|" + WebUtil.time(now) + "ms|length|" + file.length() + "byte|lines|" + size + "|" + file.getAbsolutePath() + "\n");
        now  = System.currentTimeMillis();
        for (int i = 0; i < size; i++)
        {
            boolean changed = false;
            String line = lines.get(i), line1 = line, line2;
            // symbol: 对于","如果前后是数字变为""否则为"，", 对于"."如果前后是数字保留否则为"。"
            line2 = fmtsymbol(line1);
            if (line2 != null)
            {
                changed = true;
                line1 = line2;
            }
            // blank: 删除所有除行首的空格以及行尾空格，保留字母之间的一个空格
            line2 = fmtblank(line1);
            if (line2 != null)
            {
                changed = true;
                line1 = line2;
            }
            // date1: 2007.3, 2017.1.8 -> 2007.03, 2017.01.08
            line2 = fmtdate1(line1);
            if (line2 != null)
            {
                changed = true;
                line1 = line2;
            }
            // date2: 2016年1月29日, 1992年3月 -> 2016年01月29日, 1992年03月
            line2 = fmtdate2(line1);
            if (line2 != null)
            {
                changed = true;
                line1 = line2;
            }
            if (changed)
            {
                lines.set(i, line1);
                _logger.info("Line " + i + ", length " + line.length() + ", time " + WebUtil.time(now));
                _logger.info("[" + line + "]");
                _logger.info("[" + line1 + "]\n");
            }
        }
        prompt = true;
        _logger.info("dotext done, " + DateUtil.str(new Date()));
    }

    // symbol: 对于","如果前后是数字变为""否则为"，", 对于"."如果前后是数字或前面是字母保留否则为"。"
    protected static String fmtsymbol(String line)
    {
        int length = line.length();
        Matcher m = SYMBOL.matcher(line);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find(), changed = false;
        while (result)
        {
            String orig = m.group(), dest = "";
            int start = m.start(), end = m.end();
            boolean b = (start > 0 && end < length && dchar(line.charAt(start - 1)) && dchar(line.charAt(end)));
            if (",".equals(orig)) dest = (b ? "" : "，");
            else if (".".equals(orig))
            {
                if (b || (start > 0 && lchar(line.charAt(start - 1)))) 
                {
                    m.appendReplacement(sb, orig);
                    result = m.find();
                    continue;
                }
                else dest = "。";
            }
            String head = (start < 1 ? "" : line.substring(Math.max(0, start - 6), start)), tail = (end < length ? line.substring(end, Math.min(length, end + 6)) : "");
            String info = "symbol(" + start + "," + end + "): " + head + "[" + orig + "]" + tail + " -> " + head + "[" + dest + "]" + tail;
            boolean flag = (prompt ? !"N".equalsIgnoreCase(readin(info)) : true);
            if (flag) changed = true;
            m.appendReplacement(sb, flag ? dest : orig);
            _logger.info((flag ? "apply" : "left") + " - " + info);
            result = m.find();
        }
        m.appendTail(sb);
        return (changed ? sb.toString() : null);
    }

    // blank: 删除所有除行首的空格以及行尾空格，保留字母之间的一个空格
    protected static String fmtblank(String line)
    {
        int length = line.length();
        Matcher m = BLANK.matcher(line);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find(), changed = false;
        while (result)
        {
            String orig = m.group(), dest = "";
            int start = m.start(), end = m.end();
            boolean b = (end < length && (luchar(line.charAt(end)) || (start > 0 && ldchar(line.charAt(start - 1)) && ldchar(line.charAt(end)))));
            if ((start == 0 && orig.length() > 3) || (b && (end - start == 1))) 
            {
                m.appendReplacement(sb, orig);
                result = m.find();
                continue;
            }
            else if (b) dest = " ";
            String head = (start < 1 ? "" : line.substring(Math.max(0, start - 6), start)), tail = (end < length ? line.substring(end, Math.min(length, end + 6)) : "");
            String info = "blank(" + start + "," + end + "): " + head + "[" + orig + "]" + tail + " -> " + head + "[" + dest + "]" + tail;
            boolean flag = (prompt ? !"N".equalsIgnoreCase(readin(info)) : true);
            if (flag) changed = true;
            m.appendReplacement(sb, flag ? dest : orig);
            _logger.info((flag ? "apply" : "left") + " - " + info);
            result = m.find();
        }
        m.appendTail(sb);
        return (changed ? sb.toString() : null);
    }

    // date1: 2017.1.8, 2007.3 -> 2017.01.08, 2007.03
    protected static String fmtdate1(String line)
    {
        Matcher m = DATE1.matcher(line);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find(), changed = false;
        while (result)
        {
            String orig = m.group();
            String[] arr = orig.split("\\.", -1);
            String dest = arr[0];
            for (int i = 1; i < arr.length; i++) dest += "." + (WebUtil.empty(arr[i]) ? "" : "0" + arr[i]);
            String info = "date1(" + m.start() + "," + m.end() + "): [" + orig + "] -> [" + dest + "]";
            boolean flag = (prompt ? !"N".equalsIgnoreCase(readin(info)) : true);
            if (flag) changed = true;
            m.appendReplacement(sb, flag ? dest : orig);
            _logger.info((flag ? "apply" : "left") + " - " + info);
            result = m.find();
        }
        m.appendTail(sb);
        return (changed ? sb.toString() : null);
    }

    // date2: 2016年1月29日, 1992年3月 -> 2016年01月29日, 1992年03月
    protected static String fmtdate2(String line)
    {
        Matcher m = DATE2.matcher(line);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find(), changed = false;
        while (result)
        {
            String orig = m.group(), dest = orig;
            int pos1 = orig.indexOf("年"), pos2 = orig.indexOf("月"), pos3 = orig.indexOf("日");
            if (pos1 == -1)
                dest = (pos2 == 1 ? "0" : "") + orig.substring(0, pos2) + "." + (pos3 - pos2 == 2 ? "0" : "") + orig.substring(pos2 + 1, pos3);
            else
            {
                dest = orig.substring(0, pos1) + "." + (pos2 - pos1 == 2 ? "0" : "") + orig.substring(pos1 + 1, pos2);
                if (pos3 != -1) dest += "." + (pos3 - pos2 == 2 ? "0" : "") + orig.substring(pos2 + 1, pos3);
            }
            if (orig.equals(dest)) 
            {
                m.appendReplacement(sb, orig);
                result = m.find();
                continue;
            }
            String info = "date2(" + m.start() + "," + m.end() + "): [" + orig + "] -> [" + dest + "]";
            boolean flag = (prompt ? !"N".equalsIgnoreCase(readin(info)) : true);
            if (flag) changed = true;
            m.appendReplacement(sb, flag ? dest : orig);
            _logger.info((flag ? "apply" : "left") + " - " + info);
            result = m.find();
        }
        m.appendTail(sb);
        return (changed ? sb.toString() : null);
    }

    protected static void test1()
    {
        String str1 = "10元 1000人民币 10000元 100000RMB", str2 = str1.replaceAll("(\\d+)(元|人民币|RMB)", "￥$1");
        _logger.info("test1 done, [" + str1 + "] -> [" + str2 + "]");
    }
    
    protected static void testblank()
    {
        String line = fmtblank(content);
        _logger.info("\ntestblank done\n[" + content + "]\n" + (line == null ? "not changed" : "[" + line + "]"));
    }
    
    protected static void testdate1()
    {
        String line = fmtdate1(content);
        _logger.info("\ntestdate1 done\n[" + content + "]\n" + (line == null ? "not changed" : "[" + line + "]"));
    }
    
    protected static void testdate2()
    {
        String line = fmtdate2(content);
        _logger.info("\ntestdate2 done\n[" + content + "]\n" + (line == null ? "not changed" : "[" + line + "]"));
    }
    
    public static boolean ldchar(char c)
    {
        return (lchar(c) || dchar(c));
    }
    
    public static boolean lchar(char c)
    {
        return (luchar(c) || Character.isLowerCase(c));
    }
    
    public static boolean luchar(char c)
    {
        return Character.isUpperCase(c);
    }
    
    public static boolean dchar(char c)
    {
        return (c >= '0' && c <= '9');
    }
    
    public static String readin(String prompt)
    {
        String result = "";
        try 
        {
            do
            {
                System.out.print(prompt + ", press N will not make change.");
                InputStreamReader is_reader = new InputStreamReader(System.in);
                result = new BufferedReader(is_reader).readLine();
                if ("Q".equalsIgnoreCase(result) || "quit".equalsIgnoreCase(result)) 
                {
                    System.exit(0);
                    return "N";
                }
            }
            while(false);
        } 
        catch (Exception e) {}
        
        return result;
    }

    private static final Pattern SYMBOL = Pattern.compile("(,)|(\\.)");
    private static final Pattern BLANK = Pattern.compile("( {1,})|(\\s{1,}$)");
    private static final Pattern DATE1 = Pattern.compile("((19|20)\\d{2})\\.\\d{1}((\\.\\d{1}[^\\d\\.])|([^\\d]))");
    private static final Pattern DATE2 = Pattern.compile("((\\d{1,4})\\年\\d{1,2}\\月(\\d{1,2}\\日)?)|(\\d{1,2}\\月\\d{1,2}\\日)");
    
    private static final String content = "    大型补2007.3给门架，只是2016年1月29日在中 部每  舷2017.1.8各2017.1.08装1992年3月了两个   小艇投放架";
}
