package com.arma.web.support.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.ContentTooLongException;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

public class BufferedResponseConsumer extends AbstractAsyncResponseConsumer<HttpResponse>
{
    private static final int bufferSize = 8 * 1024;
    private volatile HttpResponse response;
    private volatile HttpEntity entity;

    private volatile byte[] buffer = null;
    private volatile ByteBuffer byteBuffer = null;
    private volatile int length = -1;

    @Override
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException
    {
        this.response = response;
    }

    @Override
    protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException
    {
        long len = entity.getContentLength();
        if (len > Integer.MAX_VALUE)
        {
            throw new ContentTooLongException("Entity content is too long: " + len);
        }
        else if (len <= 0)
        {
            len = bufferSize;
        }

        this.entity = entity;
        this.buffer = new byte[(int) len];
        this.byteBuffer = ByteBuffer.wrap(buffer);
        this.length = 0;
    }

    @Override
    protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException
    {
        int bytesRead;
        while ((bytesRead = decoder.read(byteBuffer)) > -1)
        {
            if (bytesRead > 0)
            {
                length += bytesRead;
            }
            else if (byteBuffer.remaining() <= 0)
            {
                final int newLength;
                if (buffer.length % bufferSize == 0)
                {
                    newLength = buffer.length * 2;
                }
                else
                {
                    newLength = (buffer.length / bufferSize + 1) * bufferSize;
                }

                buffer = Arrays.copyOf(buffer, newLength);
                byteBuffer = ByteBuffer.wrap(buffer);
                byteBuffer.position(length);
            }
            else
            {
                break;
            }
        }
        // System.out.println(" ==== onContentReceived " + bytesRead + ", " + length + ", " + entity.getContentLength());
    }

    @Override
    protected HttpResponse buildResult(HttpContext context) throws Exception
    {
        if (entity != null && buffer != null && length > -1)
        {
            boolean gzip = false;
            final Header contentEncoding = entity.getContentEncoding();

            if (contentEncoding != null)
            {
                for (final HeaderElement element : contentEncoding.getElements())
                {
                    final String name = StringUtils.lowerCase(element.getName());
                    if ("gzip".equals(name) || "x-gzip".equals(name)) // https://tools.ietf.org/html/rfc2616#section-3.5
                    {
                        gzip = true;
                        break;
                    }
                }
            }
            response.setEntity(new BufferedEntity(entity, buffer, length, gzip));
        }
        return response;
    }

    @Override
    protected void releaseResources()
    {
        this.response = null;
        this.entity = null;
        this.buffer = null;
        this.byteBuffer = null;
    }

    public class BufferedEntity implements HttpEntity
    {
        private final Header contentType;
        private final Header contentEncoding;
        private final boolean chunked;

        private final byte[] buffer;
        private final int length;
        private final boolean gzip;

        protected BufferedEntity(final HttpEntity entity, final byte[] buffer, final int length, final boolean gzip)
        {
            this.contentType = entity.getContentType();
            this.contentEncoding = entity.getContentEncoding();
            this.chunked = entity.isChunked();

            this.buffer = buffer;
            this.length = length;
            this.gzip = gzip;
        }

        @Override
        public boolean isRepeatable()
        {
            return true;
        }

        @Override
        public boolean isStreaming()
        {
            return false;
        }

        @Override
        public boolean isChunked()
        {
            return chunked;
        }

        @Override
        public long getContentLength()
        {
            return this.length;
        }

        @Override
        public Header getContentType()
        {

            return contentType;
        }

        @Override
        public Header getContentEncoding()
        {
            return contentEncoding;
        }

        @Override
        public InputStream getContent() throws IOException
        {
            final InputStream input = new ByteArrayInputStream(this.buffer, 0, this.length);
            if (gzip)
            {
                return new GZIPInputStream(input);
            }
            return input;
        }

        @Override
        public void writeTo(final OutputStream output) throws IOException
        {
            if (gzip)
            {
                try (final InputStream input = getContent())
                {
                    IOUtils.copyLarge(input, output);
                }
            }
            else
            {
                output.write(this.buffer, 0, this.length);
            }
        }

        @Override
        public void consumeContent() throws IOException
        {
        }

        // ------------ self method ------------

        public byte[] getBuffer()
        {
            return buffer;
        }

        public int getLength()
        {
            return length;
        }

        public boolean isGzip()
        {
            return gzip;
        }
    }
}
