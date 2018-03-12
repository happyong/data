/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.util;

import java.io.File;
import java.util.Map;

import org.dom4j.Node;

import com.neulion.iptv.web.util.ParseUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class FileSkyMeta
{
	private String[] name0;	// origin name, "a", "b", "c", "3  za14.001.avi" means "a\b\c\3  za14.001.avi"
	private String level0;		// "3  "
	private String level;		// "33"
	private String ext;			// ".avi"

	private String name;		// renamed name
	private int status;			// 0: normal (s+d-), 1: create (out .meta file), 2: moved (s-d-), 3: revive (s-d+), 4: update (s+d+, will del dest and keep src)
	
	public FileSkyMeta(String str)
	{
		setName0(str);
		this.status = 1;
	}
	
	public FileSkyMeta copy()
	{
		FileSkyMeta meta = new FileSkyMeta(null);
		meta.name0 = name0;
		meta.level0 = level0;
		meta.level = level;
		meta.ext = ext;
		meta.name = getName();
		meta.status = status;
		return meta;
	}
	
	public FileSkyMeta parse(Node node)
	{
		setName0(ParseUtil.xpathNode("@name0", node));
		this.name = ParseUtil.xpathNode("@name", node);
		this.status = ("true".equals(ParseUtil.xpathNode("@moved", node)) ? 2 : 0);
		return this;
	}
	
	public void output(XmlOutput4j xop)
	{
		xop.appendTag(false, "meta", null, FileSkyMain.attrs, new String[]{(isMoved() ? "true" : null), getName(), getName0()});
	}
	
	public void scanStatus(boolean revert, File base)
	{
		String src = (revert ? getName() : getName0()), dest = (revert ? getName0() : getName());
		// 0: normal (s+d-), 1: create (out .meta file), 2: moved (s-d-), 3: revive (s-d+), 4: update (s+d+, will del dest and keep src)
		boolean src_exist = new File(base.getAbsolutePath() + File.separator + src).exists();
		boolean dest_exist = new File(base.getAbsolutePath() + File.separator + dest).exists();
		status = (!src_exist && !dest_exist ? 2 : (!src_exist && dest_exist ? 3 : 0));
		if (src_exist && dest_exist) 
		{
			new File(base.getAbsolutePath() + File.separator + dest).delete();
			status = 4;
		}
	}
	
	public void scanName(boolean merges, File work, Map<String, Integer> metas)
	{
		File file = new File(work, getName0());
		String prefix = (merges && !isFlat() ? file.getParentFile() : file).getParentFile().getName() + ".";
		if (merges && WebUtil.cn(prefix)) prefix = WebUtil.md5(WebUtil.bytes2hexs(WebUtil.str2bytes(prefix, null))).substring(0, 6) + ".";
		String[] arr = name.substring(prefix.length()).split("\\.", -1);
		if (arr.length < 2)
		{
			System.out.println("name wrong : " + getName() + " = " + getName0());
			return;
		}
		String level = arr[1].substring(0, 2);
		if (!level.equals(this.level))
		{
			System.out.println("level diff : " + getName() + " = " + getName0() + ", calc.level = " + this.level);
			return;
		}
		String key = (md5file(arr[0]));
		metas.put(key, WebUtil.str2int(arr[1].substring(2), 999));
		if (!merges || isFlat()) return;
		for (int deep = 1; deep < FileSkyMain.max_sub_level + 1; deep++) 
		{
			key = md5dir(deep);
			if (WebUtil.empty(key)) continue;
			int val = WebUtil.str2int("" + arr[0].charAt(deep - 1));
			if (val > 0) metas.put(key, val);
		}
	}
	
	public void calcName(String prefix, Map<String, Integer> metas)
	{
		String name = "";
		for (int deep = 1; deep < FileSkyMain.max_sub_level + 1; deep++) name += name(deep, null, metas);
		name = prefix + name + "." + name(-1, name, metas);
		this.name = name;
	}
	
	private String name(int deep, String name, Map<String, Integer> metas)
	{
		boolean is_dir = (deep > 0);
		if (is_dir && isFlat()) return "0";
		String key = (is_dir ? md5dir(deep) : md5file(WebUtil.unull(name)));
		if (WebUtil.empty(key)) return "0";
		Integer i = metas.get(key);
		if (is_dir)
		{
			if (i != null) return "" + i.intValue();
			String key2 = deep + "_" + WebUtil.md5((deep > 1 ? md5dir(deep - 1) : "") + ":max");
			i = metas.get(key2);
			int val = (i == null ? 1 : i.intValue() + 1);
			if (val > 9) val = 1 + WebUtil.random(9);
			metas.put(key, val);
			metas.put(key2, val);
			return "" + val;
		}
		int val = (i == null ? 1 : i.intValue() + 1);
		if (val > 999) 
		{
			String name0 = this.name0[this.name0.length - 1];
			return level + name0.substring(level0.length(), name0.length() - ext.length()) + ext;
		}
		metas.put(key, val);
		return level + WebUtil.i2s(3, val) + ext;
	}
	
	private String md5file(String name)
	{
		if (WebUtil.empty(name)) return "";
		return (FileSkyMain.max_sub_level + 1) + "_" + name + "." + level;
	}
	
	// deep: [1, 5]
	private String md5dir(int deep)
	{
		if (deep < 1 || deep > FileSkyMain.max_sub_level || deep > name0.length - 1) return "";
		String path = path(deep);
		if (WebUtil.empty(path)) return "";
		return deep + "_" + WebUtil.md5(path);
	}

	// deep: [1, n]
	private String path(int deep)
	{
		if (name0 == null || name0.length < 1 || deep < 1 || deep > name0.length) return "";
		String path = "";
		for (int i = 0; i < deep; i++) path += File.separator + name0[i];
		return path.substring(1);
	}
	
	public void print()
	{
		if (status == 0) return;
		// 0: normal (s+d-), 1: create (out .meta file), 2: moved (s-d-), 3: revive (s-d+), 4: update (s+d+, will del dest and keep src)
		String text = (status == 0 ? "normal" : (status == 1 ? "create" : (isMoved() ? "moved" : (status == 3 ? "revive" : "update"))));
		System.out.println(text + " : " + getName() + " = " + getName0());
	}
	
	public boolean isMoved()
	{
		return status == 2;
	}
	
	public boolean isRevived()
	{
		return status == 3;
	}
	
	public boolean isFlat()
	{
		return (name0 == null || name0.length < 2);
	}

	public String getName0() {
		return path(name0.length);
	}
	private void setName0(String str)
	{
		if (WebUtil.empty(str)) return;
		str = WebUtil.path0(true, str);
		this.name0 = str.split("/", -1);
		this.level0 = "";
		this.level = "99";
		String name0 = this.name0[this.name0.length - 1];
		int one = WebUtil.str2int(name0.length() > 0 ?  "" + name0.charAt(0) : "", -1);
		if (one != -1)
		{
			int i = 1;
			// [1, 4] is " ", max 4 " "
			for (; i < 5; i++) if (name0.length() > i && name0.charAt(i) != ' ') break;
			if (i != 1) 
				this.level = one + "" + (5 - i);
			else
			{
				int two = WebUtil.str2int(name0.length() > 1 ?  "" + name0.charAt(1) : "", -1);
				if (two != -1) i++;
				this.level = one + "" + (two == -1 ? 9 :  Math.min(9,  (4 + two)));
			}
			this.level0 = name0.substring(0, i);
		}
		String ext = WebUtil.ext(name0)[1];
		this.ext = (WebUtil.empty(ext) ? "" : "." + ext.toLowerCase());
	}
	public String getName() {
		return name;
	}
}
