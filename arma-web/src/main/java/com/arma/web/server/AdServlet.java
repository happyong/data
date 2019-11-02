package com.arma.web.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arma.web.util.WebUtil;

public class AdServlet extends AbstractServlet
{
    private static final long serialVersionUID = -226180772759579820L;

    @Override
    public void init() throws ServletException
    {
    }

    @Override
    protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // http://127.0.0.1:4680/dummy/ad/vwrap?sdur=30&id=0021&playId=123
        String contentType = WebUtil.CONTENT_TYPE_XML_TEXT, xml = "";
        response(0, contentType, WebUtil.str2bytes(xml, null), request, response);
        return null;
    }
}
