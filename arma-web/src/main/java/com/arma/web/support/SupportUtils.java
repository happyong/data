package com.arma.web.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.util.EntityUtils;

public abstract class SupportUtils
{
    protected static final Log _logger = LogFactory.getLog(SupportUtils.class);

    public static String[] readLines(HttpResponse response)
    {
        if (response == null)
            return new String[0];
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;
        try
        {
            reader = getBufferedReader(response);
            String line = null;
            while ((line = StringUtils.trim(reader.readLine())) != null)
                if (line.length() > 0)
                    lines.add(line);
        }
        catch (IOException e)
        {
            _logger.error("unexpected exception : " + e.getMessage(), e);
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (Exception e)
            {
            }
        }
        return lines.toArray(new String[lines.size()]);
    }

    public static BufferedReader getBufferedReader(HttpResponse response) throws IOException
    {
        return new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Consts.UTF_8));
    }

    public static String getBody(HttpResponse response)
    {
        if (response == null)
            return null;

        try
        {
            return EntityUtils.toString(response.getEntity());
        }
        catch (IOException e)
        {
            String message = "unexpected exception : " + e.getMessage();
            _logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public static HttpHost target(String url)
    {
        HttpHost target = null;
        try
        {
            final URI uri = new URI(url);
            if (uri.isAbsolute())
            {
                target = URIUtils.extractHost(uri);
                if (target == null)
                    _logger.error("URI does not specify a valid host name: " + uri);
            }
        }
        catch (final URISyntaxException ex)
        {
        }
        return target;
    }
}
