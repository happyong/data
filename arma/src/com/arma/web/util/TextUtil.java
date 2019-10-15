package com.arma.web.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neulion.iptv.web.util.FileUtil2;
import com.neulion.iptv.web.util.WebUtil;

public class TextUtil
{
    private static Log _logger = LogFactory.getLog("web.TextUtil");

    protected void fmtclx()
    {
        _logger.info("begin fmtclx, " + InVarAM.dir_text_clx);
        File src0 = new File(InVarAM.dir_text_clx + ".txt");
        File[] srcs = new File(InVarAM.dir_text_clx).listFiles();
        for (File src : srcs)
            fmtclx(src, src0);
        _logger.info("end fmtclx");
    }

    private void fmtclx(File src, File src0)
    {
        List<String> lines = FileUtil2.readLines(src.getName(), WebUtil.CHARSET_GB2312, src.getParentFile());
        List<String> lines0 = FileUtil2.readLines(src0.getName(), null, src0.getParentFile());
        int sz = size(lines), sz0 = size(lines0);
        _logger.info("fmt " + src0.getName() + " to " + src.getName() + ", (" + sz0 + " vs " + sz + ")");

        List<Chapter> head = new ArrayList<Chapter>(), list = new ArrayList<Chapter>();
        for (int i = 0; i < sz; i++)
        {
            String line = lines.get(i);
            if (InVarAM.hitclx1(line))
            {
                int pos = list.size() - 1;
                if (pos >= 0)
                    list.get(pos).lineEnd = i - 3;
                list.add(new Chapter(i, sz - 1, line));
            }
        }
        for (int i = list.size() - 1; i >= 0; i--)
        {
            Chapter ch = list.get(i);
            if (ch.isHead())
                head.add(0, list.remove(i));
            else
                _logger.debug("chapter " + ch.desc());
        }
        String name = lines.get(list.get(list.size() / 2).line - 1);
        int total = lines0.size(), start = scan(true, list.get(0).line + 1, lines, lines0), end = scan(false, list.get(list.size() - 1).lineEnd, lines, lines0);
        _logger.info("==== name " + name + ", from " + start + " to " + end + " ====");
        rmv(end + 1, total - 1, total, lines0);
        rmv(0, start - 1, total, lines0);
        for (int i = 0; i < list.size(); i++)
        {
            _logger.info("add chapter " + list.get(i).text);
            start = scan(true, list.get(i).line + 1, lines, lines0);
            if (start < 0)
                continue;
            lines0.add(start, list.get(i).text);
            lines0.add(start, name);
            lines0.add(start, "");
        }
        _logger.info("add header");
        for (int i = 0; i < head.size(); i++)
            lines0.add(i, head.get(i).text);
        lines0.add(0, name);
        _logger.info("");

        FileUtil2.writeLines(lines0, src.getName(), null, WebUtil.LINE_WIN, src0.getParentFile());
    }

    private int size(List<String> lines)
    {
        for (int i = lines.size() - 1; i >= 0; i--)
            if (lines.get(i).trim().length() > 0)
                return i + 1;
        return 0;
    }

    private int scan(boolean forward, int pos, List<String> lines, List<String> lines0)
    {
        Section section = new Section().max(forward, pos, lines);
        int sz = section.max.length(), step = Math.min(8, sz);

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i <= sz - step; i++)
        {
            String search = section.max.substring(i, i + step);
            List<int[]> list = scan0(i, search, lines0);
            if (list == null)
                continue;
            for (int[] arr : list)
            {
                int val = 100 / Math.max(1, arr[1] + 1);
                Integer count = map.get(arr[0]);
                map.put(arr[0], count == null ? val : count + val);
            }
        }
        if (map.size() > 0)
        {
            int ret = -1, count = 0;
            for (Map.Entry<Integer, Integer> entry : map.entrySet())
                if (count < entry.getValue())
                    ret = entry.getKey();
            _logger.info("scan success, hit " + ret + ", (" + section.max + " vs " + lines0.get(ret) + ")");
            return ret;
        }
        if (step < sz)
        {
            _logger.info("#### scan fail, line " + pos + ", " + section.max + " ####");
            return -1;
        }
        int delta = (forward ? 1 : -1);
        return scan(forward, pos + delta * section.lines, lines, lines0) - delta;
    }

    private void rmv(int min, int max, int total, List<String> lines0)
    {
        if (min < 0 || max < min)
            return;
        int len = max - min + 1;
        for (int i = max; i >= min; i--)
            lines0.remove(i);
        _logger.info("rmv lines, " + len + ", (" + len * 100 / total + "%)");
    }

    private List<int[]> scan0(int index, String search, List<String> lines0)
    {
        List<int[]> list = null;
        int sz0 = lines0.size();
        for (int i = 0; i < sz0; i++)
        {
            String line0 = lines0.get(i);
            int pos0 = line0.indexOf(search);
            if (pos0 == -1)
                continue;
            if (list == null)
                list = new ArrayList<int[]>();
            list.add(new int[] { i, pos0 - index });
            _logger.debug("scan hit, line " + i + " > " + (pos0 - index) + " > " + index + ", (" + search + " vs " + lines0.get(i) + ")");
        }
        return list;
    }
    
    public class Section
    {
        private int lines;
        private String max;
        
        private Section max(boolean forward, int pos, List<String> lines)
        {
            TrimText text = new TrimText(lines.get(pos)).trim();
            int count = 1;
            String line = text.line;
            if (forward)
            {
                int sz = lines.size();
                for (int i = pos + 1; i < sz; i++)
                {
                    text = new TrimText(lines.get(i)).trim();
                    if (text.newline)
                        break;
                    count++;
                    if (line.length() < text.line.length())
                        line = text.line;
                }
            }
            else if (!text.newline)
            {
                for (int i = pos - 1; i >= 0; i--)
                {
                    count++;
                    text = new TrimText(lines.get(i)).trim();
                    if (line.length() < text.line.length())
                        line = text.line;
                    if (text.newline)
                        break;
                }
            }
            this.lines = count;
            this.max = line;
            return this;
        }
    }

    public class TrimText
    {
        private boolean newline;
        private String line0;
        private String line;

        public TrimText(String line0)
        {
            this.line0 = line0;
        }

        public TrimText trim()
        {
            line = line0;
            int pos, len = InVarAM.key_blank_gb2312.length(), count = 0;
            while (line.indexOf(InVarAM.key_blank_gb2312) == 0)
            {
                count++;
                line = line.substring(len);
            }
            newline = (count > 1);
            if (line.length() > 0)
                while ((pos = line.indexOf(InVarAM.key_blank_gb2312)) == line.length() - len)
                    line = line.substring(0, pos);
            return this;
        }
    }

    public class Chapter
    {
        private int line;
        private int lineEnd;
        private String text;

        public Chapter(int line, int end, String text)
        {
            this.line = line;
            this.lineEnd = end;
            this.text = text;
        }

        public boolean isHead()
        {
            return (line >= lineEnd);
        }

        public String desc()
        {
            return "line (" + line + ", " + lineEnd + "), " + text;
        }
    }

    public static void main(String[] args)
    {
        // init log4j
        ArmaUtil.config();
        TextUtil util = new TextUtil();
        util.fmtclx();
    }
}
