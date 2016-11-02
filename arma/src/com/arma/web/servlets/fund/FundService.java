/*
 * Copyright (c) 2015 ARMA, Inc. All Rights Reserved.
 */
package com.arma.web.servlets.fund;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arma.web.service.bean.TNetDaily;
import com.arma.web.service.bean.TQuoteDaily;
import com.arma.web.service.bean.TSymbol;
import com.arma.web.util.ArmaUtil;
import com.arma.web.util.InVarAM;
import com.neulion.iptv.web.servlets.AbstractBaseComponent;
import com.neulion.iptv.web.servlets.PortalUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.XmlOutput4j;

public class FundService extends AbstractBaseComponent
{
	private static final long serialVersionUID = 1782648720768230724L;

	@Override
	// /servlets/funds/funds?symbol=150266&sortType=111&sort=asc
	// /servlets/funds/funddetail?codeB=150266
	// /servlets/funds/fundflush?type=nets_daily/quotes_daily/daily/symbols/all
	// /servlets/funds/fundsymboldetail?codeB=150266
	// /servlets/funds/fundsymbolupdate?code=150266&...
	// /servlets/funds/fundquotedetail?codeB=150266&date=2015-07-18
	// /servlets/funds/fundquoteupdate?code=150266&date=2015-07-18&...
	// /servlets/funds/fundnetdetail?codeB=150266&date=2015-07-18
	// /servlets/funds/fundnetupdate?code=150266&date=2015-07-18&...
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{	
		String path = "", servlet = request.getRequestURI();
		servlet = servlet.substring(servlet.lastIndexOf("/") + 1);	
		String format = (WebUtil.FORMAT_JSON.equals(request.getParameter("format")) ? WebUtil.FORMAT_JSON : WebUtil.FORMAT_XML);
		boolean json = (WebUtil.FORMAT_JSON.equals(format) || WebUtil.FORMAT_JAVA.equals(format));
		Map<String, Object> map = WebUtil.param("result", false);
		XmlOutput4j xop = null;
		if (json)
			xop = new XmlOutput4j(1, map);
		else
		{
			xop = new XmlOutput4j(1);
			xop.openTag("data", null, null);
		}

		if ("funds".equals(servlet)) 
		{
			boolean asc = !"desc".equalsIgnoreCase(request.getParameter("sort"));
			int sort_type = WebUtil.str2int(request.getParameter("sortType"));
			String symbol = request.getParameter("symbol");
			List<String> codes_b = FundCacher.filter(symbol);
			Collections.sort(codes_b, new FundComparator(asc, sort_type));
			xop.openTag("funds", InVarAM.attrs_count, new String[]{"" + codes_b.size()}); 
			for (String code_b : codes_b) FundCacher.appendFund(0, code_b, xop);
			xop.closeTag();			
			map.put("result", true);
		}
		else if ("fundebk".equals(servlet)) 
		{
			boolean asc = !"desc".equalsIgnoreCase(request.getParameter("sort"));
			int sort_type = WebUtil.str2int(request.getParameter("sortType"));
			String symbol = request.getParameter("symbol");
			List<String> codes_b = FundCacher.filter(symbol);
			Collections.sort(codes_b, new FundComparator(asc, sort_type));
			StringBuffer sb = new StringBuffer();
			sb.append("1999999").append(WebUtil.line());
			for (String code_b : codes_b) FundCacher.appendFundEbk(code_b, sb);
			String ebk = sb.toString();
			PortalUtil.response(new String(InVarAM.s_fund_ebk.getBytes(WebUtil.CHARSET_UTF_8), WebUtil.CHARSET_ISO), WebUtil.CONTENT_TYPE_TEXT, ebk, response);
			return null;
		}
		else if ("funddetail".equals(servlet)) 
		{
			String code_b = request.getParameter("codeB");
			if (!WebUtil.empty(code_b))
			{
				FundCacher.appendFund(1, code_b, xop);
				map.put("result", true);
			}
		}
		else if ("fundflush".equals(servlet)) 
		{
			String type = request.getParameter("type");
			if ("all".equals(type))
			{
				FundCacher.flushSymbols();
				FundCacher.flush();
			}
			else if ("symbols".equals(type))
				FundCacher.flushSymbols();
			else if ("daily".equals(type))
			{
				FundCacher.flushQuoteDaily(null);
				FundCacher.flushNetDaily();
			}
			else if ("quotes_daily".equals(type))
				FundCacher.flushQuoteDaily(null);
			else if ("nets_daily".equals(type))
				FundCacher.flushNetDaily();
			map.put("result", true);
		}
		else if ("fundsymboldetail".equals(servlet)) 
		{
			String code_b = request.getParameter("codeB");
			FundCacher.appendSymbolDetail(code_b, xop);
			map.put("result", true);
		}
		else if ("fundsymbolupdate".equals(servlet)) 
		{
			TSymbol[] beans = new TSymbol[]{new TSymbol().fromRequest(0, null, request), new TSymbol().fromRequest(1, null, request), new TSymbol().fromRequest(2, null, request)};
			if (beans[0] != null && !beans[0].empty() && beans[1] != null && !beans[1].empty() && beans[2] != null && !beans[2].empty())
			{
				for (int i = 0; i < beans.length; i++) 
				{
					for (int j = 0; j < InVarAM.s_names.length; j++) beans[i].addFundInfo(InVarAM.s_names[j], beans[j].getCode());
					FundCacher.updateSymbol(beans[i]);
				}
				map.put("result", true);
			}
		}
		else if ("fundquotedetail".equals(servlet)) 
		{
			String code_b = request.getParameter("codeB");
			String date = request.getParameter("date");
			do
			{
				if (!ArmaUtil.trade(date)) break;
				FundCacher.appendQuoteDetail(code_b, date, xop);
				map.put("result", true);
			}
			while(false);
			if (!(Boolean)map.get("result")) map.put("error", date + " is not trade date");
		}
		else if ("fundquoteupdate".equals(servlet)) 
		{
			TQuoteDaily bean = new TQuoteDaily().fromRequest(null, request);
			do
			{
				if (bean == null || bean.empty() || !ArmaUtil.trade(bean.getDate())) break;
				FundCacher.updateQuoteDaily(bean);
				map.put("result", true);
			}
			while(false);
			if (!(Boolean)map.get("result")) map.put("error", (bean == null ? "" : bean.getDate()) + " is not trade date");
		}
		else if ("fundnetdetail".equals(servlet)) 
		{
			String code_b = request.getParameter("codeB");
			String date = request.getParameter("date");
			do
			{
				if (!ArmaUtil.trade(date)) break;
				FundCacher.appendNetDetail(code_b, date, xop);
				map.put("result", true);
			}
			while(false);
			if (!(Boolean)map.get("result")) map.put("error", date + " is not trade date");
		}
		else if ("fundnetupdate".equals(servlet)) 
		{
			TNetDaily bean = new TNetDaily().fromRequest(null, request);
			do
			{
				if (bean == null || bean.empty() || !ArmaUtil.trade(bean.getDate())) break;
				FundCacher.updateNetDaily(bean);
				map.put("result", true);
			}
			while(false);
			if (!(Boolean)map.get("result")) map.put("error", (bean == null ? "" : bean.getDate()) + " is not trade date");
		}
		
		if ((Boolean)map.get("result") && !json) map.put("bodyXml", xop.output());
		return response(path, format, map, request);
	}
}
