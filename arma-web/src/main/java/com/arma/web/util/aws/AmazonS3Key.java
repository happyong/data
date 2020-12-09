package com.arma.web.util.aws;

public class AmazonS3Key
{
    private boolean file;
    private long size;
    private String key;

    public AmazonS3Key(boolean file, long size, String key)
    {
        this.file = file;
        if (size > 0)
            this.size = size;
        this.key = key;
    }

    public boolean isFile()
    {
        return file;
    }

    public long getSize()
    {
        return size;
    }

    public String getKey()
    {
        return key;
    }
}
