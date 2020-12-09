package com.arma.web.util.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.arma.web.util.KeyUtil;
import com.arma.web.util.WebUtil;

public class VmsUtil
{
    public static final int retry_aws = 2;
    public static final String _aws_key = "001600";

    @SuppressWarnings("deprecation")
    public static AmazonS3Client decrypt(String s3key)
    {
        if (WebUtil.empty(s3key))
            return null;
        String keys = KeyUtil.decrypt(s3key);
        int pos = keys.indexOf(_aws_key);
        return (pos == -1 ? null : new AmazonS3Client(new BasicAWSCredentials(keys.substring(0, pos), keys.substring(pos + _aws_key.length()))));
    }
}
