package com.arma.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.net.InetAddresses;

public class WebUtil
{
    private static String hostName = null;
    private static SecureRandom random = null;

    public static final long kb = 1024L;
    public static final long mb = kb * kb;
    public static final long gb = mb * kb;
    public static final long tb = gb * kb;

    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_XML_TEXT = "text/xml";
    public static final String CONTENT_TYPE_HTML = "text/html";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_FORM_FILE = "multipart/form-data";
    public static final String CONTENT_TYPE_DEFAULT = "application/octet-stream";

    public static final String CHARSET_UTF_8 = "UTF-8";
    public static final String CHARSET_ISO = "ISO-8859-1";
    public static final String CHARSET_GB2312 = "GB2312";
    public static final String CHARSET_GBK = "GBK";

    // private static final String LINE_MAC = "\r";
    public static final String LINE_UNIX = "\n";
    public static final String LINE_WIN = "\r\n";

    public static final String http_myip = "http://nlcfgadmin.neulion.com/nlds/whatismyip.php";
    public static final String html_start = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>test page</title></head><body>\r\n";
    public static final String html_link = "<h1><a href=\"{link}\">{name}</a></h1>\r\n";
    public static final String html_end = "</body></html>\r\n";

    public static final String KEY_SEP_UNIX = "/";
    public static final String KEY_SEP_WIN = "\\";
    public static final String KEY_ADMIN = "YsH10gE";
    public static final String KEY_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
    public static final String FORMAT_XML = "xml";
    public static final String FORMAT_JSON = "json";
    public static final Pattern PATTERN_CN = Pattern.compile("[\u4e00-\u9fa5]");
    public static final Pattern field_pairs = Pattern.compile("[\\?\\&]([^=]+)=([^\\&]+)");
    public static final Pattern pt_duration = Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)D)?" + "(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?",
            Pattern.CASE_INSENSITIVE);

    private static final Log _logger = LogFactory.getLog("web.WebUtil");

    // fill 0 after number for fraction part
    // 1.534876 - 0 2, 2 1.53, 4 1.5349, 8 1.53487600
    public static String d2s(int bit, double d)
    {
        return String.format("%." + Math.max(0, bit) + "f", d);
    }

    // fill 0 before number
    // 210 - 2 210, 4 0210, 8 00000210
    public static String i2s(int bit, int i)
    {
        return String.format("%0" + Math.max(1, bit) + "d", i);
    }

    // 1.200 -> 1.2
    public static String ss(String str)
    {
        if (empty(str))
            return "";
        if (str.indexOf(".") == -1)
            return str;
        StringBuilder sb = new StringBuilder(str);
        while (sb.charAt(sb.length() - 1) == '0')
            sb.setLength(sb.length() - 1);
        if (sb.charAt(sb.length() - 1) == '.')
            sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static byte[] str2bytes(String str, String charsetName)
    {
        byte[] ret = new byte[0];
        try
        {
            if (!empty(str))
                ret = str.getBytes(empty(charsetName) ? CHARSET_UTF_8 : charsetName);
        }
        catch (Exception e)
        {
        }
        return ret;
    }

    public static int obj2int(Object obj)
    {
        if (obj == null)
            return 0;
        return str2int(obj.toString());
    }

    public static int str2int(String str)
    {
        return str2int(str, 0);
    }

    public static int str2int(String str, int def)
    {
        int ret = def;
        if (str == null)
            return ret;
        try
        {
            ret = Integer.parseInt(str);
        }
        catch (Exception e)
        {
        }
        return ret;
    }

    public static long str2long(String str)
    {
        return str2long(str, 0);
    }

    public static long str2long(String str, long def)
    {
        long ret = def;
        if (str == null)
            return ret;
        try
        {
            ret = Long.parseLong(str);
        }
        catch (Exception e)
        {
        }
        return ret;
    }

    public static String time2str(long base)
    {
        return String.valueOf(time(base) / 1000.00);
    }

    public static long time(long base)
    {
        return base > 0 ? System.currentTimeMillis() - base : System.currentTimeMillis();
    }

    public static String length(long length)
    {
        if (length < 1)
            return "0KB";
        else if (length < kb)
            return "1KB";
        else if (length < mb)
            return Math.max(1, (length / kb)) + "KB";
        else if (length < gb)
            return Math.max(1, (length / mb)) + "MB";
        else if (length < tb)
            return Math.max(1, (length / gb)) + "GB";
        return Math.max(1, (length / tb)) + "TB";
    }

    public static boolean cn(String str)
    {
        Matcher matcher = PATTERN_CN.matcher(str);
        return matcher.find();
    }

    // "PT20.345S" -- parses as "20.345 seconds"
    // "PT15M" -- parses as "15 minutes" (where a minute is 60 seconds)
    // "PT10H" -- parses as "10 hours" (where an hour is 3600 seconds)
    // "P2D" -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
    // "P2DT3H4M" -- parses as "2 days, 3 hours and 4 minutes"
    // "P-6H3M" -- parses as "-6 hours and +3 minutes"
    // "-P6H3M" -- parses as "-6 hours and -3 minutes"
    // "-P-6H+3M" -- parses as "+6 hours and -3 minutes"
    public static long dur2mms(String duration, long def)
    {
        if (empty(duration))
            return def;
        Matcher matcher = pt_duration.matcher(duration);
        if (matcher.matches())
        {
            // check for letter T but no time sections
            if (!"T".equals(matcher.group(3)))
            {
                boolean negate = "-".equals(matcher.group(1));
                String days = matcher.group(2), hours = matcher.group(4), minutes = matcher.group(5), seconds = matcher.group(6), fraction = matcher.group(7);
                if (days != null || hours != null || minutes != null || seconds != null)
                {
                    long ss = str2long(seconds) * 1000000L, mms = (fraction == null ? 0L : str2long((fraction + "000000").substring(0, 6)));
                    long sum = str2long(days) * 86400000000L + str2long(hours) * 3600000000L + str2long(minutes) * 60000000L + ss + (ss < 0 ? 0 - mms : mms);
                    return (negate ? 0 - sum : sum);
                }
            }
        }
        return def;
    }

    public static long dur2mms(String duration)
    {
        return dur2mms(duration, 0L);
    }

    public static String ms2dur(boolean second, long ms)
    {
        if (ms < 1)
            return "PT0S";
        if (second)
            return "PT" + ms2str(ms) + "S";
        long h = ms / 3600000, m = (ms % 3600000) / 60000, s = ms % 60000;
        return "PT" + (h > 0 ? h + "H" : "") + (m > 0 ? m + "M" : "") + (s > 0 ? ms2str(s) + "S" : "");
    }

    // ms 1123 -> return "1.123"
    public static String ms2str(long ms)
    {
        if (ms == 0)
            return "";
        long s = (ms / 1000L), fraction = (ms % 1000L);
        if (fraction > 0)
        {
            String str = "000" + fraction;
            str = str.substring(str.length() - 3, str.length());
            return ss(s + "." + str);
        }
        return String.valueOf(s);
    }

    public static String mms2dur(boolean second, long mms)
    {
        if (mms < 1)
            return "PT0S";
        if (second)
            return "PT" + mms2str(mms) + "S";
        long h = mms / 3600000000L, m = (mms % 3600000000L) / 60000000L, s = mms % 60000000L;
        return "PT" + (h > 0 ? h + "H" : "") + (m > 0 ? m + "M" : "") + (s > 0 ? mms2str(s) + "S" : "");
    }

    // mms 1123456 -> return "1.123456"
    public static String mms2str(long mms)
    {
        if (mms == 0)
            return "";
        long s = (mms / 1000000L), fraction = (mms % 1000000L);
        if (fraction > 0)
        {
            String str = "000000" + fraction;
            str = str.substring(str.length() - 6, str.length());
            return ss(s + "." + str);
        }
        return String.valueOf(s);
    }

    public static String uri(boolean suffix, String uri)
    {
        if (empty(uri))
            return "";
        int pos = uri.lastIndexOf("/");
        if (suffix)
            return (pos == -1 ? "" : uri.substring(pos + 1));
        return (pos == -1 ? uri : uri.substring(0, pos + 1));
    }

    // uri - http://172.16.0.23:18009/live/t3_hd.mpd
    // return http://172.16.0.23:18009
    public static String uribase(String uri)
    {
        if (empty(uri))
            return "";
        int pos1 = uri.indexOf("://");
        if (pos1 < 1)
            return "";
        int pos2 = uri.indexOf("/", pos1 + 3);
        return uri.substring(0, (pos2 < 0 ? uri.length() : pos2));
    }

    // uri - univision_west_hd_3000_20171123081510.ts
    // prefix - http://172.16.0.23:18009/live/
    // prefix0 - http://172.16.0.23:18009
    // return http://172.16.0.23:18009/live/univision_west_hd_3000_20171123081510.ts
    public static String url(String uri, String prefix, String prefix0)
    {
        if (empty(uri))
            return "";
        return (uri.indexOf("://") > 0 ? uri : (uri.charAt(0) == '/' ? prefix0 : prefix) + uri);
    }

    public static boolean empty(String str)
    {
        return (str == null || str.length() == 0);
    }

    public static String unull(Object str)
    {
        return (str == null ? "" : str.toString());
    }

    public static String str(String str)
    {
        if (empty(str))
            return "";
        return str.substring(1);
    }

    public static int pos(String key, String[] arr)
    {
        if (empty(key) || arr == null || arr.length == 0)
            return -1;
        for (int i = 0; i < arr.length; i++)
            if (key.equals(arr[i]))
                return i;
        return -1;
    }

    public static int pos2(String key, String[] arr)
    {
        if (empty(key) || arr == null || arr.length == 0)
            return -1;
        for (int i = 0; i < arr.length; i++)
            if (key.equalsIgnoreCase(arr[i]))
                return i;
        return -1;
    }

    public static int posl(String key, List<String> list)
    {
        if (empty(key) || list == null || list.size() == 0)
            return -1;
        for (int i = 0; i < list.size(); i++)
            if (key.equals(list.get(i)))
                return i;
        return -1;
    }

    public static int posl2(String key, List<String> list)
    {
        if (empty(key) || list == null || list.size() == 0)
            return -1;
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).startsWith(key))
                return i;
        return -1;
    }

    public static int poslast(int count, char sep, String path)
    {
        int pos = path.length();
        for (int i = count; i > 0; i--)
        {
            pos = path.lastIndexOf(sep, pos - 2);
            if (pos < 0)
                return -1;
        }
        return pos;
    }

    // type - 0: [a, b], 1: (a, b), 2: [a, b), 3: (a, b]
    public static boolean within(int type, long val, long min, long max)
    {
        if (type == 1)
            return (val > min && val < max);
        else if (type == 2)
            return (val >= min && val < max);
        else if (type == 3)
            return (val > min && val <= max);
        return (val >= min && val <= max);
    }

    public static String line()
    {
        return unix() ? LINE_UNIX : LINE_WIN;
    }

    // file separator via sepecified format
    public static String sep(boolean unix)
    {
        return unix ? KEY_SEP_UNIX : KEY_SEP_WIN;
    }

    // os is unix format or not
    public static boolean unix()
    {
        return ":".equals(File.pathSeparator);
    }

    public static String md5(String origin)
    {
        if (empty(origin))
            return "";
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            byte[] result = md.digest(origin.getBytes());
            return bytes2hexs(result);
        }
        catch (Exception ex)
        {
        }
        return "";
    }

    public static String bytes2hexs(byte[] data)
    {
        if (data == null || data.length == 0)
            return "";

        final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        final int length = data.length;
        final char[] chars = new char[length * 2];

        for (int i = 0; i < length; i++)
        {
            final int n = ((int) data[i]) & 0xff;
            chars[2 * i] = digits[n / 0x10];
            chars[2 * i + 1] = digits[n % 0x10];
        }
        return new String(chars);
    }

    public static String bytes2hexs2(byte[] data)
    {
        if (data == null || data.length == 0)
            return "";

        final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        final int length = data.length;
        final char[] chars = new char[length * 2];

        for (int i = 0; i < length; i++)
        {
            final int n = ((int) data[i]) & 0xff;
            chars[2 * i] = digits[n / 0x10];
            chars[2 * i + 1] = digits[n % 0x10];
        }
        return new String(chars);
    }

    public static byte[] hexs2bytes(String s)
    {
        if (empty(s))
            return new byte[0];
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        return data;
    }

    public static String decode(String content, String encoding)
    {
        if (empty(content))
            return "";
        try
        {
            return URLDecoder.decode(content, encoding != null ? encoding : CHARSET_ISO);
        }
        catch (Exception e)
        {
        }
        return "";
    }

    public static String encode(String content, String encoding)
    {
        return encode(false, content, encoding);
    }

    public static String encode(boolean rpc2396, String content, String encoding)
    {
        if (empty(content))
            return "";
        try
        {
            String result = URLEncoder.encode(content, encoding != null ? encoding : CHARSET_ISO);
            return (rpc2396 ? result.replaceAll("\\+", "%20") : result);
        }
        catch (Exception e)
        {
        }
        return "";
    }

    // 4cdbc040-657a-4847-b266-7e31d9e2c3d9
    public static String uuid()
    {
        return UUID.randomUUID().toString();
    }

    // 4cdbc040657a4847b2667e31d9e2c3d9
    public static String uuid2()
    {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // bound, the bound on the random number to be returned
    public static int random(int bound)
    {
        if (random == null)
            random = new SecureRandom();
        bound = Math.max(1, bound);
        return random.nextInt(bound);
    }

    public static long ip2long(String ip)
    {
        if (empty(ip))
            return 0L;
        String[] ipArr = ip.split(".");
        return (long) (Long.parseLong(ipArr[0]) * (long) Math.pow(2, 24)) + (Long.parseLong(ipArr[1]) * (long) Math.pow(2, 16))
                + (Long.parseLong(ipArr[2]) * (long) Math.pow(2, 8)) + (Long.parseLong(ipArr[3]));
    }

    public static String long2ip(long ipIntFormat)
    {
        String retVal = "";

        ipIntFormat = ipIntFormat & 0xFFFFFFFF;
        retVal = retVal + ((ipIntFormat >> 24) & 0xff) + "." + ((ipIntFormat >> 16) & 0xff) + "." + ((ipIntFormat >> 8) & 0xff) + "." + (ipIntFormat & 0xff);
        return retVal;
    }

    public static int ip2int(String ip)
    {
        return (empty(ip) ? 0 : InetAddresses.coerceToInteger(InetAddresses.forString(ip)));
    }

    public static String int2ip(int i)
    {
        return InetAddresses.toAddrString(InetAddresses.fromInteger(i));
    }

    public static String getMaskIp(HttpServletRequest req, int ipmaskNum)
    {
        int ip = ip2int(getRealIp(req));
        int ipmask = (int) (0xFFFFFFFFL << ipmaskNum);
        return InetAddresses.toAddrString(InetAddresses.fromInteger(ip & ipmask));
    }

    public static String getMaskIp(String strip, int ipmaskNum)
    {
        int ip = ip2int(strip);
        int ipmask = (int) (0xFFFFFFFFL << ipmaskNum);
        return InetAddresses.toAddrString(InetAddresses.fromInteger(ip & ipmask));
    }

    public static String getRealIp(HttpServletRequest req)
    {
        // X-Forwarded-For, Proxy-Client-IP, WL-Proxy-Client-IP
        // String ip = getHeaderIp("X-Forwarded-For", req);
        return (req == null ? "127.0.0.1" : req.getRemoteAddr());
    }

    public static String getHostName()
    {
        try
        {
            if (hostName == null)
            {
                InetAddress ia = InetAddress.getLocalHost();
                hostName = ia.getHostName();
                if (!ia.getHostAddress().equals(hostName))
                {
                    int k = hostName.indexOf(".");
                    if (k != -1)
                        hostName = hostName.substring(0, k);
                }
            }
        }
        catch (Exception e)
        {
            hostName = "localhost";
        }
        return hostName;
    }

    // test/bean/data/playbacks.txt
    public static String readRes(String path)
    {
        String ret = null;
        InputStream in = null;
        try
        {
            in = WebUtil.class.getClassLoader().getResourceAsStream(path);
            ret = IOUtils.toString(in, CHARSET_UTF_8);
        }
        catch (Exception e)
        {
        }
        finally
        {
            closeIn(in);
        }
        return ret;
    }

    // test/bean/data/playbacks.txt
    public static List<String> readLinesRes(String path)
    {
        List<String> lines = null;
        InputStream in = null;
        try
        {
            in = WebUtil.class.getClassLoader().getResourceAsStream(path);
            lines = IOUtils.readLines(in, CHARSET_UTF_8);
        }
        catch (Exception e)
        {
        }
        finally
        {
            closeIn(in);
        }
        return lines;
    }

    public static void printSystemInfo(Map<String, String> builds)
    {
        _logger.info(System.getProperty("java.runtime.name") + " version " + System.getProperty("java.runtime.version"));
        _logger.info("HostName=" + getHostName());
        if (builds == null || builds.size() == 0)
            return;
        for (Map.Entry<String, String> entry : builds.entrySet())
            _logger.info(entry.getKey() + "=" + unull(entry.getValue()));
    }

    public static String build(String name, String build)
    {
        if (empty(build))
            return "";
        build = StringUtils.replace(build, " build ", ".");
        build = StringUtils.replace(build, " ", "_");
        if (!empty(name))
            build = name + "_v" + build;
        return build;
    }

    public static Map<String, String> builds()
    {
        Enumeration<URL> res = null;
        ClassLoader cl = _logger.getClass().getClassLoader();
        Map<String, String> map = new HashMap<String, String>();
        try
        {
            res = cl.getResources("META-INF/MANIFEST.MF");
        }
        catch (Exception e)
        {
        }
        if (res == null)
            return map;

        URL url;
        int k1, k2;
        Properties prop;
        InputStream in = null;
        String vendor, version, module;
        while (res.hasMoreElements())
        {
            url = (URL) res.nextElement();
            if (!"jar".equals(url.getProtocol()))
                continue;

            try
            {
                prop = new Properties();
                in = url.openConnection().getInputStream();
                prop.load(in);

                vendor = prop.getProperty("Implementation-Vendor");
                if (vendor == null)
                    continue;
                version = prop.getProperty("Implementation-Version");
                if (empty(version))
                    continue;

                module = url.getFile();
                k1 = module.indexOf("!");
                if (k1 > 0)
                {
                    k2 = module.lastIndexOf("/", k1);
                    if (k2 > 0)
                        module = module.substring(k2 + 1, k1);
                }
                map.put(module, version);
            }
            catch (Exception e)
            {
            }
            finally
            {
                closeIn(in);
            }
        }
        return map;
    }

    public static Map<String, String> extract(char sep, String base)
    {
        Map<String, String> map = new HashMap<String, String>();
        if (empty(base))
            return map;
        String[] pairs = StringUtils.split(base, sep);
        for (String pair : pairs)
        {
            pair = pair.trim();
            int pos = pair.indexOf('=');
            if (pos >= 0)
                map.put(pair.substring(0, pos), pair.substring(pos + 1));
        }
        return map;
    }

    public static int compare(long ret)
    {
        return (ret > 0 ? 1 : (ret < 0 ? -1 : 0));
    }

    public static Process exec(boolean use_file, String cmd, File path) throws IOException
    {
        if (use_file)
        {
            File file = (exist(1, cmd));
            if (file == null)
                return null;
            if (path == null)
                path = file.getParentFile();
        }

        Process process = null;
        if (unix())
            process = new ProcessBuilder(new String[] { "/bin/bash", "-c", cmd }).directory(path).start();
        else if (use_file)
            process = new ProcessBuilder("\"" + cmd + "\"").directory(path).start();
        else
            process = Runtime.getRuntime().exec(cmd, null, path);
        return process;
    }

    // 0 means dir, 1 means file, others means both
    public static File exist(int type, String name)
    {
        if (empty(name))
            return null;
        File file = new File(name);
        if (!file.exists() || (type == 0 && !file.isDirectory()) || (type == 1 && file.isDirectory()))
            return null;
        return file;
    }

    public static void closeIn(InputStream in)
    {
        try
        {
            if (in != null)
                in.close();
        }
        catch (IOException e)
        {
        }
    }

    public static void closeOut(OutputStream out)
    {
        try
        {
            if (out != null)
                out.close();
        }
        catch (IOException e)
        {
        }
    }

    public static void sleep(long time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (Exception e)
        {
        }
    }
}
