package com.arma.web.util.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.arma.web.util.WebUtil;

public class AmazonS3Helper
{
    private static Log _logger = LogFactory.getLog("web.AmazonS3Helper");

    // bucket: "neu-video"
    // key: "video/nfl", "video/nfl/demo_1600.mp4"
    @SuppressWarnings("deprecation")
    public static void upload(boolean sync, String bucket, String key, String srcFile, AmazonS3 s3, ProgressListener progress)
    {
        if (WebUtil.empty(bucket) || WebUtil.empty(key) || WebUtil.empty(srcFile) || s3 == null)
            return;

        String uri = bucket + "/" + key;
        TransferManager tm = new TransferManager(s3);
        PutObjectRequest request = new PutObjectRequest(bucket, key, new File(srcFile));
        if (progress != null)
            request.setGeneralProgressListener(progress);
        Upload upload = tm.upload(request);
        try
        {
            // block and wait for the upload to finish
            if (sync)
                upload.waitForCompletion();
            _logger.info("AmazonS3 simple upload, path|" + uri + "|srcFile|" + srcFile);
        }
        catch (Exception e)
        {
            if (tm != null)
                tm.shutdownNow(false);
            _logger.info("AmazonS3 simple upload fail, path|" + uri + "|srcFile|" + srcFile + "|" + e);
        }
    }

    // bucket: "neu-video"
    // key: "video/nfl", "video/nfl/demo_1600.mp4"
    public static void upload(long size4part, String bucket, String key, String srcFile, AmazonS3 s3, ProgressListener progress)
    {
        if (WebUtil.empty(bucket) || WebUtil.empty(key) || WebUtil.empty(srcFile) || s3 == null)
            return;

        String uri = bucket + "/" + key;
        s3.setEndpoint("http://s3.amazonaws.com");
        // create a list of UploadPartResponse objects, and get one of these for each part upload.
        List<PartETag> list = new ArrayList<PartETag>();
        // step 1: initialize
        String id = s3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucket, key)).getUploadId();

        File file = new File(srcFile);
        long length = file.length();
        size4part = Math.min(Math.max(5242880L, size4part), 5368709120L); // 5MB ~ 5TB
        try
        {
            long pos = 0;
            UploadPartRequest request;
            // step 2: upload parts
            for (int i = 1; pos < length; i++)
            {
                // last part can be less than 5 MB, adjust part size
                size4part = Math.min(size4part, (length - pos));
                // create request to upload a part
                request = new UploadPartRequest().withBucketName(bucket).withKey(key).withUploadId(id).withPartNumber(i).withFileOffset(pos).withFile(file).withPartSize(size4part);
                if (progress != null)
                    request.setGeneralProgressListener(progress);
                // upload part and add response to our list
                list.add(s3.uploadPart(request).getPartETag());
                pos += size4part;
            }
            // step 3: complete
            CompleteMultipartUploadRequest request2 = new CompleteMultipartUploadRequest(bucket, key, id, list);
            s3.completeMultipartUpload(request2);
            _logger.info("AmazonS3 multipart upload, path|" + uri + "|srcFile|" + srcFile);
        }
        catch (Exception e)
        {
            s3.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, key, id));
            _logger.info("AmazonS3 multipart upload fail, path|" + uri + "|srcFile|" + srcFile + "|" + e);
        }
    }

    // bucket: "neu-video"
    // key: "video/nfl", "video/nfl/demo_1600.mp4"
    public static void download(long start, long end, String bucket, String key, String destFile, AmazonS3 s3)
    {
        if (WebUtil.empty(bucket) || WebUtil.empty(key) || WebUtil.empty(destFile) || s3 == null)
            return;

        String uri = bucket + "/" + key;
        GetObjectRequest request = new GetObjectRequest(bucket, key);
        if (start >= 0 && end > start)
            request.setRange(start, end);

        InputStream in = null;
        FileOutputStream out = null;
        try
        {
            in = s3.getObject(request).getObjectContent();
            out = new FileOutputStream(destFile);
            long size = IOUtils.copyLarge(in, out);
            _logger.info("AmazonS3 download, path|" + uri + "|size|" + size + "|destFile|" + destFile);
        }
        catch (Exception e)
        {
            _logger.info("AmazonS3 download fail, path|" + uri + "|destFile|" + destFile + "|" + e);
        }
        finally
        {
            WebUtil.closeOut(out);
            WebUtil.closeIn(in);
        }
    }

    // onelevel: ture for just one level, false for this level and all sub folders
    // bucket: "neu-video"
    // prefix: "video/nfl", or "vide"
    public static AmazonS3Key[] list(boolean onelevel, String bucket, String prefix, AmazonS3 s3)
    {
        if (WebUtil.empty(bucket) || s3 == null)
            return new AmazonS3Key[0];
        String marker = "";
        List<AmazonS3Key> rets = new ArrayList<AmazonS3Key>();
        while (marker != null)
            marker = list(onelevel, bucket, prefix, marker, s3, rets);
        return rets.toArray(new AmazonS3Key[rets.size()]);
    }

    public static String list(boolean onelevel, String bucket, String prefix, String marker, AmazonS3 s3, List<AmazonS3Key> rets)
    {
        if (WebUtil.empty(bucket) || marker == null || s3 == null || rets == null)
            return null;

        ListObjectsRequest request = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix);
        if (onelevel)
            request.withDelimiter("/");
        if (marker.length() > 0)
            request.withMarker(marker);
        try
        {
            String file;
            int files = 0, dirs = 0;
            ObjectListing list = s3.listObjects(request);
            for (S3ObjectSummary obj : list.getObjectSummaries())
            {
                file = obj.getKey();
                if (file.endsWith("/"))
                    continue;
                rets.add(new AmazonS3Key(true, obj.getSize(), file));
                files++;
            }
            if (onelevel)
            {
                for (String dir : list.getCommonPrefixes())
                {
                    rets.add(new AmazonS3Key(false, 0, dir));
                    dirs++;
                }
            }
            _logger.info("AmazonS3 list, onelevel|" + onelevel + "|path|" + bucket + "|prefix|" + prefix + "|marker|" + marker + "|files|" + files + "|dirs|" + dirs);
            return list.getNextMarker();
        }
        catch (Exception e)
        {
            _logger.info("AmazonS3 list fail, onelevel|" + onelevel + "|path|" + bucket + "|prefix|" + prefix + "|marker|" + marker + "|" + e);
        }
        return null;
    }

    // to delete folder key, please delete the object key under this folder at first
    // bucket: "neu-video"
    // key: "video/nfl", "video/nfl/demo_1600.mp4"
    public static void delete(String bucket, String key, AmazonS3 s3)
    {
        if (WebUtil.empty(bucket) || WebUtil.empty(key) || s3 == null)
            return;

        String uri = bucket + "/" + key;
        try
        {
            s3.deleteObject(bucket, key);
            _logger.info("AmazonS3 delete, path|" + uri);
        }
        catch (Exception e)
        {
            _logger.info("AmazonS3 delete fail, path|" + uri + "|" + e);
        }
    }
}
