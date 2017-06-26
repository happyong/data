package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;

import com.neulion.iptv.web.util.FileUtil2;
import com.neulion.iptv.web.util.HttpUtil;
import com.neulion.iptv.web.util.WebUtil;

public class UtilTest
{
	private static File user = new File("C:/Users/Administrator/Downloads");
    public static void config()
    {
        String user_dir = System.getProperty("user.dir");
        if (!new File(user_dir + "/log4j.xml").exists())
            user_dir = WebUtil.path0(user_dir + "/WebContent");
        DOMConfigurator.configure(user_dir + "/WEB-INF/config/log4j.xml");
        if (!user.exists()) user = new File("C:/Users/Guoen.Yong/Downloads");
    }

    public static void main(String[] args)
    {
        config();

        // rename();
        getmap();
    }

    protected static void rename()
    {
        String[] arr = InVarT.s_rename;
        long base = System.currentTimeMillis(), count = 0;
        File[] files = new File(user, "militray").listFiles();
        for (File file : files)
        {
            String name1 = file.getName();
            if (!file.isFile() || !name1.startsWith(arr[0])) continue;
            count++;
            String name2 = arr[1] + name1.substring(arr[0].length());
            file.renameTo(new File(file.getParent(), name2));
            System.out.println("rename, " + name1 + " -> " + name2);
        }
        System.out.println("rename done, " + WebUtil.time(base) + " ms, " + count);
    }
    
    protected static void getmap()
    {
        String[][] arr = {{"world", "http://blog.sina.com.cn/s/blog_afb342c90102vo80.html"}};
        for (String[] arr2 : arr) getmap(arr2[0], arr2[1]);
    }

    private static void getmap(String key, String blog)
    {
        String file = key + ".txt", parent = "";
        File dir = new File(user, "maps/" + key);
        if (!dir.exists()) dir.mkdirs();
        FileUtil2.write(HttpUtil.getXml(blog, null, null, null), file, null, dir);
        WebUtil.sleep(500);
        List<String> list = new ArrayList<String>(), lines = FileUtil2.readLines(file, null, dir);
        if (lines == null || lines.size() < 1)
        {
            System.out.println("getmap ignore, file not found, " + file + ", " + dir.getAbsolutePath());
            return;
        }
        for (File f : dir.listFiles()) if (f.isFile() && !file.equals(f.getName())) list.add(f.getName());
        
        long base = System.currentTimeMillis();
        String[] keys = new String[]{"<td", "http://www.onegreen.net/maps/m/", ".htm\">", ">", "</FONT></A></TD>", "<img src=\"", "\""};
        int len2 = keys[2].length() - 2, len5 = keys[5].length(), get = 0, fail = 0, ignore = 0, exist = 0, delete = 0, last = -1;
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            int pos4 = line.indexOf(keys[4]);
            if (!line.startsWith(keys[0]) || pos4 == -1) continue;
            int pos1 = line.indexOf(keys[1]), pos2 = (pos1 == -1 ? -1 : line.indexOf(keys[2], pos1)), pos3 = line.lastIndexOf(keys[3], pos4);
            if (pos2 == -1 || pos3 == -1 || pos3 < pos2 || pos4 < pos3) continue;
            if (last == -1 || last - i != 1)
            {
                String p = parent(i, lines);
                if (p.length() > 0) parent = p;
            }
            last = i;
            String url1 = line.substring(pos1, pos2 + len2), name0 = line.substring(pos3 + 1, pos4), name = name0;
            String map = HttpUtil.getXml(url1, null, null, null);
            int pos5 = map.indexOf(keys[5]), pos6 = (pos5 == -1 ? -1 : map.indexOf(keys[6], pos5 + len5));
            String url2 = (pos6 == -1 ? "" : map.substring(pos5 + len5, pos6));
            if (pos6 == -1)
            {
                ignore++;
                System.out.println("=== download ignore === " + name);
                continue;
            }
            name = format(name);
            name = (WebUtil.pos(name, InVarT.s_map_city) == -1 ? "2" + (parent.length() > 0 ? parent + "." : "") + name + InVarT.s_map[1] : "4" + name + InVarT.s_map[2]);
            File f = new File(dir, name);
            if (f.exists() && f.length() > 10240)
            {
                exist++;
                list.remove(name);
                continue;
            }
            url2 = keys[1] + url2;
            if (!getFile(url2, new File(dir, name)))
            {
                fail++;
                System.out.println("=== download fail === " + name + " <- " + url2);
                continue;
            }
            get++;
            System.out.println("download, " + name + " <- " + url2);
        }
        new File(dir, file).delete();
        boolean deleted = false;
        for (String str : list) 
        {
            if (deleted) 
            {
                delete++;
                new File(dir, str).delete(); 
            }
            System.out.println("=== " + (deleted ? "" : "to be ") + "deleted === " + str);
        }
        System.out.println("getmap done, " + WebUtil.time(base) + " ms, get " + (get + fail) + ", fail " + fail + ", ignore " + ignore + ", exist " + exist + ", delete " + delete);
    }
    
    private static String parent(int end, List<String> lines)
    {
        if (end < 1 || lines == null) return "";
        String[] arr = new String[]{"</FONT></B></A></TD>", ">"};
        for (int i = end - 1; i >= 0 && end - i < 10; i--)
        {
            String line = lines.get(i);
            int pos0 = line.indexOf(arr[0]), pos1 = (pos0 == -1 ? -1 : line.lastIndexOf(arr[1], pos0));
            if (pos1 != -1) return format(line.substring(pos1 + arr[1].length(), pos0));
        }
        return "";
    }
    
    private static String format(String name)
    {
        return (name.endsWith(InVarT.s_map[0]) ? name.substring(0, name.length() - InVarT.s_map[0].length()) : name);
    }
    
    private static boolean getFile(String url, File dest)
    {
        if (WebUtil.empty(url)) return false;

        int code = -1;
        InputStream in = null;
        FileOutputStream fo = null; 
        HttpURLConnection hc = null;
        try 
        {
            if (!dest.exists()) dest.createNewFile();
            hc = HttpUtil.open(url);
            hc.setUseCaches(false);
            hc.setConnectTimeout(30000);
            hc.setReadTimeout(8000);
            hc.setRequestProperty("Content-Type", WebUtil.CONTENT_TYPE_XML_TEXT);
            code = hc.getResponseCode();
            in = hc.getInputStream();
            fo = new FileOutputStream(dest);
            if (code >= 200 && code < 400) 
            {
                HttpUtil.copyLarge(in, fo, null);
                return true;
            }
        } 
        catch (Exception e) 
        {
        }
        if (code < 200 || code >= 400)
        {
            try { if (hc != null) hc.disconnect(); } catch (Exception e) {}
        }
        return false;
    }
}