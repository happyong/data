package com.arma.web.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Format;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.arma.web.Consts;
import com.arma.web.util.WebUtil;

public abstract class AbstractServlet extends HttpServlet
{
    private static final long serialVersionUID = 4788423047794227475L;
    protected static Log _logger = LogFactory.getLog(AbstractServlet.class);

    @Override
    public void init() throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
    {
        doGet(request, response);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
    {
        try
        {
            String path = processRequest(request, response);
            if (path == null)
                return;

            if (!response.isCommitted())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                // request.getRequestDispatcher(path).forward(request, response);
            }
        }
        catch (Throwable t)
        {
            String message = "unexpected throwable : " + t.getMessage();
            _logger.error(message, t);
            throw new ServletException(message, t);
        }
    }

    protected int response(int maxAge, String contentType, byte[] bytes, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        if (bytes == null)
            bytes = new byte[0];

        ByteArrayOutputStream baos = null;
        boolean gzip = (!Consts.key_ct_stream.equals(contentType) && gzip(httpRequest, httpResponse));
        if (gzip)
        {
            baos = new ByteArrayOutputStream();
            try (final OutputStream gout = new GZIPOutputStream(baos))
            {
                gout.write(bytes);
            }
        }

        int size = (gzip ? baos.size() : bytes.length);
        setHeaders(maxAge, contentType, httpRequest, httpResponse);
        httpResponse.setContentLength(size);
        try (final OutputStream out = httpResponse.getOutputStream())
        {
            if (gzip)
                baos.writeTo(out);
            else
                out.write(bytes);
        }
        return size;
    }

    // only gzip for m3u8/mpd, not for key/ts/...
    private boolean gzip(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        // request - Accept-Encoding: gzip, deflate
        // response - Content-Encoding: gzip
        String encoding = httpRequest.getHeader("Accept-Encoding");
        boolean gzip = (encoding != null && encoding.indexOf("gzip") > -1);
        if (gzip && httpResponse != null)
            httpResponse.setHeader("Content-Encoding", "gzip");
        return gzip;
    }

    // maxAge - -1 not set, 0 no-cache, >0 max-age=
    private void setHeaders(int maxAge, String contentType, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        if (maxAge == 0)
            httpResponse.setHeader("Cache-Control", "max-age=0, no-cache, no-store");
        else if (maxAge > 0)
        {
            httpResponse.setHeader("Cache-Control", "max-age=" + maxAge);
            httpResponse.setDateHeader("Expires", System.currentTimeMillis() + maxAge * 1000);
        }
        // httpResponse.setDateHeader("Date", System.currentTimeMillis());
        httpResponse.setContentType(WebUtil.empty(contentType) ? WebUtil.CONTENT_TYPE_XML_TEXT : contentType);
        httpResponse.setHeader("Connection", "Keep-Alive");
        httpResponse.setHeader("Proxy-Connection", "Keep-Alive");

        String origin = (httpRequest.getHeader("Origin"));
        httpResponse.setHeader("Access-Control-Allow-Origin", (WebUtil.empty(origin) ? "*" : origin));
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
    }

    protected String time()
    {
        return df_log_lo2nz.format(System.currentTimeMillis());
    }

    protected String param(String name, HttpServletRequest request)
    {
        String value = request.getParameter(name);
        return (WebUtil.empty(value) ? "-" : value);
    }

    abstract protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    protected static final Format df_log_lo2nz = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
}
