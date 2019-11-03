package com.arma.web.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Format;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class ArmaUtil
{
    protected final static Log _logger = LogFactory.getLog(ArmaUtil.class);

    public static final Format df_log_lonz = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final XmlMapper xmlMapper = new XmlMapper();
    static
    {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        xmlMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xmlMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // JacksonXmlModule module = new JacksonXmlModule();
        // and then configure, for example:
        // module.setDefaultUseWrapper(false);
    }

    public static byte[] toJsonBytes(Object obj)
    {
        if (obj == null)
            return new byte[0];
        try
        {
            return mapper.writeValueAsBytes(obj);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return new byte[0];
    }

    public static <T> T readJsonBytes(byte[] value, Class<T> cls)
    {
        if (value == null || value.length == 0 || cls == null)
            return null;
        try
        {
            return mapper.readValue(value, cls);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return null;
    }

    public static String toJsonString(Object obj)
    {
        if (obj == null)
            return "";
        try
        {
            return mapper.writeValueAsString(obj);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return "";
    }

    public static <T> T readJsonString(String value, Class<T> cls)
    {
        if (WebUtil.empty(value) || cls == null)
            return null;
        try
        {
            return mapper.readValue(value, cls);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return null;
    }

    public static byte[] toXmlBytes(Object obj)
    {
        if (obj == null)
            return new byte[0];
        try
        {
            return xmlMapper.writeValueAsBytes(obj);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return new byte[0];
    }

    public static <T> T readXmlBytes(byte[] value, Class<T> cls)
    {
        if (value == null || value.length == 0 || cls == null)
            return null;
        try
        {
            return xmlMapper.readValue(value, cls);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return null;
    }

    public static String toXmlString(Object obj)
    {
        if (obj == null)
            return "";
        try
        {
            return xmlMapper.writeValueAsString(obj);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return "";
    }

    public static <T> T readXmlString(String value, Class<T> cls)
    {
        if (WebUtil.empty(value) || cls == null)
            return null;
        try
        {
            return xmlMapper.readValue(value, cls);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        return null;
    }

    public static byte[] toZipBytes(byte[] value)
    {
        if (value == null || value.length == 0)
            return new byte[0];
        OutputStream zipOutputStream = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            zipOutputStream = new DeflaterOutputStream(out, new Deflater(value.length > 20480 ? Deflater.DEFAULT_COMPRESSION : Deflater.BEST_SPEED, true), 2048);
            // zipOutputStream =new XZCompressorOutputStream(out);
            zipOutputStream.write(value);
            zipOutputStream.flush();
        }
        catch (IOException e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        finally
        {
            WebUtil.closeOut(zipOutputStream);
        }
        byte[] ret = out.toByteArray();
        WebUtil.closeOut(out);
        zipOutputStream = null;
        out = null;
        return ret;
    }

    public static byte[] toUnZipBytes(byte[] value)
    {
        if (value == null || value.length == 0)
            return new byte[0];
        InputStream zipInputStream = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            zipInputStream = new InflaterInputStream(new ByteArrayInputStream(value), new Inflater(true), 2048);
            IOUtils.copy(zipInputStream, out);
        }
        catch (IOException e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        finally
        {
            WebUtil.closeIn(zipInputStream);
        }
        byte[] ret = out.toByteArray();
        WebUtil.closeOut(out);
        zipInputStream = null;
        out = null;
        return ret;
    }

    public static byte[] zipContextBytes(Object obj)
    {
        byte[] json = toJsonBytes(obj);
        if (json == null || json.length < 1)
            return new byte[0];
        json = toZipBytes(json);
        return json;
    }

    public static <T> T unzipContextBytes(byte[] json, Class<T> cls)
    {
        int json_length = (json == null ? 0 : json.length);
        json = (json_length <= 0 ? null : toUnZipBytes(json));
        if (json == null || json.length < 1)
            return null;
        T ret = readJsonBytes(json, cls);
        return ret;
    }
}
