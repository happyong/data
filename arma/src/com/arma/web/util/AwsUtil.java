package com.arma.web.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.neulion.iptv.web.util.DateUtil;
import com.neulion.iptv.web.util.WebUtil;
import com.neulion.iptv.web.util.aws.AmazonEC2Helper;

public class AwsUtil
{
    private final AmazonEC2 ec2;
    private static Log _logger = LogFactory.getLog("web.AmazonEC2Helper");

    public AwsUtil()
    {
        AmazonEC2 ec2 = null;
        try
        {
            AWSCredentials cred = new PropertiesCredentials(new File("c:/credentials4.properties"));
            // us-east-1*, ap-northeast-1*
            ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(cred)).withRegion("ap-northeast-1").build();
        }
        catch (Exception e)
        {
            _logger.error("ec2 init error", e);
        }
        this.ec2 = ec2;
    }

    protected void ec2list()
    {
        Map<String, String> filters = new HashMap<String, String>();
        filters.put("key-name", "client_test");

        List<Instance> insts = new ArrayList<Instance>();
        try
        {
            DescribeInstancesRequest query_req = new DescribeInstancesRequest();
            if (filters != null && filters.size() > 0)
            {
                List<Filter> list = new ArrayList<Filter>();
                for (Map.Entry<String, String> entry : filters.entrySet())
                    list.add(new Filter(entry.getKey()).withValues(entry.getValue()));
                query_req = query_req.withFilters(list);
            }
            DescribeInstancesResult query_resp = ec2.describeInstances(query_req);

            for (Reservation reservation : query_resp.getReservations())
            {
                for (Instance it : reservation.getInstances())
                {
                    // Date launch = it.getLaunchTime();
                    // if (WebUtil.empty(it.getPublicIpAddress()) || launch == null)
                    // continue;
                    insts.add(it);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Collections.sort(insts, new Comparator<Instance>()
        {
            public int compare(Instance obj1, Instance obj2)
            {
                return WebUtil.compare(obj1.getLaunchTime().getTime() - obj2.getLaunchTime().getTime());
            }
        });
        for (Instance inst : insts)
            System.out.println("ec2list, " + DateUtil.str(inst.getLaunchTime()) + "|" + inst.getInstanceId() + "|" + inst.getPublicIpAddress());
    }

    protected void ec2Start(String instId)
    {
        AmazonEC2Helper.inststart(0, instId, ec2);
        _logger.info("starting " + instId);
        Instance inst = null;
        for (int i = 0; inst == null && i < 20; i++)
        {
            inst = AmazonEC2Helper.inststatedone(0, instId, ec2);
            WebUtil.sleep(2000);
        }
        _logger.info("started " + instId + ", " + (inst == null ? "fail" : "done"));
    }

    protected void ec2Stop(String instId)
    {
        AmazonEC2Helper.inststop(0, instId, ec2);
        _logger.info("stopping " + instId);
        Instance inst = null;
        for (int i = 0; inst == null && i < 20; i++)
        {
            inst = AmazonEC2Helper.inststatedone(1, instId, ec2);
            WebUtil.sleep(5000);
        }
        _logger.info("stopped " + instId + ", " + (inst == null ? "fail" : "done"));
    }

    public static void main(String[] args)
    {
        // init log4j
        ArmaUtil.config();
        AwsUtil test = new AwsUtil();
        // test.ec2list();
        // test.ec2Start("i-0ded5bd3f19f13ae3");
        test.ec2Stop("i-0ded5bd3f19f13ae3");
    }
}
