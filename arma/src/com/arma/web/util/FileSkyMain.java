/*
 * Copyright (c) 2011 NeuLion, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dom4j.Node;

import com.neulion.iptv.web.util.FileUtil2;
import com.neulion.iptv.web.util.ParseUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

/**
 * File Sky
 * 
 * $Revision: 1.01 $, $Date: 2011/07/21 14:43:22 $
 */
// FileSkyMain -base [-revert] -simple [-recursive]
// FileSkyMain -base [-revert] -merges
// FileSkyMain -base -modified
public class FileSkyMain
{
    private static final String meta_file = ".meta.xml";
    private static final String metas_file = ".metas.xml";
    public static final int max_sub_level = 5;
    public static final String[] attrs = new String[] { "moved", "name", "name0" };
    public static final String[] attrs_merges = new String[] { "base", "subfolder" };

    public static void main(final String[] args)
    {
        // init log4j
        ArmaUtil.config();

        Options opts = new Options();
        Option opt = new Option("base", "base", true, "the base directory");
        opt.setRequired(true);
        opts.addOption(opt);
        opts.addOption("revert", "revert", false, "revert to origin name");
        opts.addOption("simple", "simple", false, "simple mode");
        opts.addOption("recursive", "recursive", false, "include subfolders for simple mode");
        opts.addOption("merges", "merges", false, "merges mode");
        opts.addOption("modified", "modified", false, "modified mode");

        String format = "FileSkyMain -base [-revert] [-simple] [-recursive] [-merges] [-modified] ";
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        CommandLine cl = null;
        try
        {
            cl = parser.parse(opts, args);
        }
        catch (ParseException e)
        {
            formatter.printHelp(format, opts);
            System.out.println("posix parser error, " + e);
        }
        if (cl == null)
            return;
        System.out.println("main args, " + WebUtil.join(" ", args) + "\n");

        boolean revert = (cl.hasOption("revert"));
        File base = new File(cl.getOptionValue("base"));
        if (!base.exists())
        {
            System.out.println("base not found, " + base.getAbsolutePath());
            return;
        }
        if (cl.hasOption("simple"))
            simple(revert, cl.hasOption("recursive"), base, base);
        else if (cl.hasOption("merges"))
            merges(revert, base);
        else if (cl.hasOption("modified"))
            modified(System.currentTimeMillis(), base);
    }

    private static void modified(long time, File base)
    {
        base.setLastModified(time);
        File[] files = base.listFiles();
        if (files == null)
            return;
        for (File file : files)
            if (file.isDirectory())
                modified(time, file);
            else
                file.setLastModified(time);
    }

    // FileSkyMain -base [-revert] -simple [-recursive]
    private static void simple(boolean revert, boolean recursive, File base, File work)
    {
        if (new File(work, metas_file).exists())
            return;

        Map<String, Integer> metas = new HashMap<String, Integer>(); // [dir/name_key, index]
        Map<String, FileSkyMeta> map_cur = new HashMap<String, FileSkyMeta>(); // [name/name0, meta]
        Map<String, FileSkyMeta> map_all = new HashMap<String, FileSkyMeta>(); // [name, meta]
        // load meta file
        List<Node> nodes = ParseUtil.parseXml(FileUtil2.read(meta_file, null, work), "//result/meta");
        load(false, revert, work, nodes, metas, map_cur, map_all);

        File[] files = work.listFiles();
        if (files == null)
            files = new File[0];
        Arrays.sort(files);
        String prefix = work.getName() + ".";
        for (File file : files)
        {
            if (rubbish(file))
                continue;
            if (meta_file.equals(file.getName()))
                continue;
            if (file.isDirectory())
            {
                if (recursive)
                    simple(revert, recursive, base, file);
                continue;
            }
            rename(revert, prefix, "", file, work, metas, map_cur, map_all);
        }
        store(false, revert, work, map_all);
    }

    // FileSkyMain -base [-revert] -merges
    private static void merges(boolean revert, File base)
    {
        if (new File(base, meta_file).exists())
            return;

        Map<String, Integer> metas = new HashMap<String, Integer>(); // [dir/name_key, index]
        Map<String, FileSkyMeta> map_cur = new HashMap<String, FileSkyMeta>(); // [name/name0, meta]
        Map<String, FileSkyMeta> map_all = new HashMap<String, FileSkyMeta>(); // [name, meta]
        // load metas file
        List<Node> nodes = ParseUtil.parseXml(FileUtil2.read(metas_file, null, base), "//result/meta");
        load(true, revert, base, nodes, metas, map_cur, map_all);
        merges(revert, base, base, metas, map_cur, map_all);
        store(true, revert, base, map_all);
    }

    // FileSkyMain -base [-revert] -merges
    private static void merges(boolean revert, File base, File work, Map<String, Integer> metas, Map<String, FileSkyMeta> map_cur, Map<String, FileSkyMeta> map_all)
    {
        if (new File(work, meta_file).exists())
            return;

        File[] files = work.listFiles();
        if (files == null)
            files = new File[0];
        Arrays.sort(files);
        String subfolder = work.getAbsolutePath().substring(base.getAbsolutePath().length());
        if (!WebUtil.empty(subfolder))
            subfolder = subfolder.substring(1) + File.separator;
        String prefix = (WebUtil.empty(subfolder) ? work : work.getParentFile()).getName() + ".";
        if (WebUtil.cn(prefix))
            prefix = WebUtil.md5(WebUtil.bytes2hexs(WebUtil.str2bytes(prefix, null))).substring(0, 6) + ".";
        for (File file : files)
        {
            if (rubbish(file))
                continue;
            if (metas_file.equals(file.getName()))
            {
                if (!revert)
                    FileUtil2.delete(file.getAbsolutePath());
                continue;
            }
            if (file.isDirectory())
            {
                merges(revert, base, file, metas, map_cur, map_all);
                continue;
            }
            rename(revert, prefix, subfolder, file, base, metas, map_cur, map_all);
        }
        if (!revert && !WebUtil.empty(subfolder))
        {
            files = work.listFiles();
            if (files == null || files.length < 1)
                FileUtil2.delete(work.getAbsolutePath());
        }
    }

    private static void rename(boolean revert, String prefix, String subfolder, File file, File work, Map<String, Integer> metas, Map<String, FileSkyMeta> map_cur,
            Map<String, FileSkyMeta> map_all)
    {
        String dest = "";
        FileSkyMeta meta = map_cur.get(subfolder + file.getName());
        if (meta != null)
            dest = (revert ? meta.getName0() : meta.getName());
        else
        {
            meta = new FileSkyMeta(subfolder + file.getName());
            meta.calcName(prefix, metas);
            dest = meta.getName();
        }
        map_all.put(meta.getName(), meta);
        if (WebUtil.empty(dest) || meta.isRevived())
            return;
        File df = new File(work.getAbsolutePath() + File.separator + dest);
        if (df.exists())
            file.delete();
        else
        {
            if (!df.getParentFile().exists())
                FileUtil2.createDirectory(df.getParent());
            if (revert && !meta.isFlat() && !new File(df.getParentFile(), metas_file).exists())
            {
                XmlOutput4j xop = new XmlOutput4j().appendHeader();
                subfolder = df.getParent().substring(work.getAbsolutePath().length());
                if (!WebUtil.empty(subfolder))
                    subfolder = subfolder.substring(1);
                xop.appendTag(false, "result", null, FileSkyMain.attrs_merges, new String[] { work.getAbsolutePath(), subfolder });
                FileUtil2.write(xop.output(), metas_file, null, df.getParentFile());
            }
            file.renameTo(df);
        }
    }

    private static boolean rubbish(File file)
    {
        if (!"Thumbs.db".equalsIgnoreCase(file.getName()))
            return false;
        FileUtil2.delete(file.getAbsolutePath());
        System.out.println("delete rubbish: " + file.getAbsolutePath());
        return true;
    }

    private static void load(boolean merges, boolean revert, File work, List<Node> nodes, Map<String, Integer> metas, Map<String, FileSkyMeta> map_cur,
            Map<String, FileSkyMeta> map_all)
    {
        if (nodes == null || nodes.size() < 1)
            return;
        for (Node node : nodes)
        {
            FileSkyMeta meta = new FileSkyMeta(null).parse(node);
            meta.scanStatus(revert, work);
            meta.scanName(merges, work, metas);
            String src = meta.getName0(), dest = meta.getName(), tmp = dest;
            if (revert)
            {
                dest = src;
                src = tmp;
            }
            (meta.isMoved() ? map_all : map_cur).put(meta.isMoved() ? tmp : (meta.isRevived() ? dest : src), meta);
        }
    }

    private static void store(boolean merges, boolean revert, File work, Map<String, FileSkyMeta> map_all)
    {
        String[] keys = map_all.keySet().toArray(new String[map_all.size()]);
        Arrays.sort(keys);
        int count = 0;
        XmlOutput4j xop = new XmlOutput4j().appendHeader();
        xop.openTag("result", FileSkyMain.attrs_merges, new String[] { merges ? work.getAbsolutePath() : null, merges ? "" : null });
        for (String key : keys)
        {
            FileSkyMeta meta = map_all.get(key);
            meta.print();
            meta.output(xop);
            if (!meta.isMoved())
                count++;
        }
        xop.closeTag();
        FileUtil2.write(xop.output(), merges ? metas_file : meta_file, null, work);
        System.out.println((merges ? "merges" : "simple") + " done: " + (revert ? "revert " : "rename ") + count + "/" + keys.length + ", " + work.getAbsolutePath() + "\n");
    }
}
