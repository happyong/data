package com.arma.web.util.aws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.SpotPlacement;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.arma.web.util.DateUtil;
import com.arma.web.util.WebUtil;

public class AmazonEC2Helper
{
    // key pair - log in to your instance securely
    // security group - act as a firewall for associated instances, controlling both inbound and outbound traffic at the instance level
    private static Log _logger = LogFactory.getLog("web.AmazonEC2Helper");
    private static final String KEY_AMZ_SECURITY = "security.vos.client";
    private static final int[] PORTS_AMZ_SECURITY = new int[] { 80, 443, 3389, 12800, 12802, 12804, 12806, 12808, 15100, 15108, 16100, 16108, 19100, 19108, 19135 };

    @SuppressWarnings("deprecation")
    public static String sg(AmazonEC2 ec2)
    {
        String sgid = "";
        if (ec2 == null)
            return sgid;

        try
        {
            List<IpPermission> list = new ArrayList<IpPermission>();
            List<String> ranges = new ArrayList<String>(), exists = new ArrayList<String>();
            ranges.add("0.0.0.0/0");

            DescribeSecurityGroupsRequest query_req = new DescribeSecurityGroupsRequest();
            query_req.withGroupNames(KEY_AMZ_SECURITY);
            DescribeSecurityGroupsResult query_resp = null;
            try
            {
                query_resp = ec2.describeSecurityGroups(query_req);
            }
            catch (Exception e)
            {
            }
            if (query_resp == null || query_resp.getSecurityGroups().size() == 0)
            {
                CreateSecurityGroupRequest create_req = new CreateSecurityGroupRequest(KEY_AMZ_SECURITY, KEY_AMZ_SECURITY);
                CreateSecurityGroupResult create_resp = ec2.createSecurityGroup(create_req);
                sgid = create_resp.getGroupId();
                _logger.info("AmazonService createSecurityGroup, name|" + KEY_AMZ_SECURITY);
            }
            else
            {
                int from, to, max, min;
                SecurityGroup sg = query_resp.getSecurityGroups().get(0);
                for (IpPermission p : sg.getIpPermissions())
                {
                    from = (p.getFromPort() == null ? 0 : p.getFromPort());
                    to = (p.getToPort() == null ? 0 : p.getToPort());
                    max = Math.max(from, to);
                    min = Math.min(from, to);
                    if (max == min)
                    {
                        if (max > 0 && !exists.contains(String.valueOf(max)))
                            exists.add(String.valueOf(max));
                        continue;
                    }
                    for (int i = from; i <= to; i++)
                        if (!exists.contains(String.valueOf(i)))
                            exists.add(String.valueOf(i));
                }
                sgid = sg.getGroupId();
            }

            String ports = "";
            IpPermission perm;
            for (int port : PORTS_AMZ_SECURITY)
            {
                if (exists.contains(String.valueOf(port)))
                    continue;
                perm = new IpPermission();
                perm.setIpProtocol("tcp");
                perm.setFromPort(port);
                perm.setToPort(port);
                perm.setIpRanges(ranges);
                list.add(perm);
                ports += "," + port;
            }
            if (list.size() == 0)
                return sgid;
            AuthorizeSecurityGroupIngressRequest auth_req = new AuthorizeSecurityGroupIngressRequest(KEY_AMZ_SECURITY, list);
            ec2.authorizeSecurityGroupIngress(auth_req);
            _logger.info("AmazonEC2 authorizeSecurityGroupIngress, name|" + KEY_AMZ_SECURITY + "|ports|" + WebUtil.str(ports));
        }
        catch (Exception e)
        {
            _logger.error("AmazonEC2 checkSecurityGroup fail, name|" + KEY_AMZ_SECURITY);
        }
        return sgid;
    }

    // inst(0, "c1.xlarge", "ami-b08f15d9", "Open", "vos-agent", "us-east-1a", ec2) - i-4f953a1f
    public static Instance inst(int retry, String type, String ami, String group, String key, String place, AmazonEC2 ec2)
    {
        Instance instance = null;
        if (WebUtil.empty(type) || WebUtil.empty(key) || WebUtil.empty(ami) || ec2 == null)
            return instance;

        try
        {
            RunInstancesRequest vm_req =
                    new RunInstancesRequest().withInstanceType(type).withImageId(ami).withSecurityGroupIds(group).withKeyName(key).withMinCount(1).withMaxCount(1);
            if (!WebUtil.empty(place))
                vm_req = vm_req.withPlacement(new Placement(place));
            RunInstancesResult vm_resp = ec2.runInstances(vm_req);
            List<Instance> insts = vm_resp.getReservation().getInstances();
            if (insts != null && insts.size() > 0)
            {
                instance = insts.get(0);
                _logger.info("AmazonEC2 runInstances, " + type + "|" + ami + "|" + group + "|" + key + "|" + instance.getInstanceId() + "|"
                        + instance.getPlacement().getAvailabilityZone());
            }
        }
        catch (AmazonServiceException ase)
        {
            _logger.error("AmazonEC2 runInstances fail, " + type + "|" + ami + "|" + group + "|" + key);
        }
        catch (AmazonClientException ace)
        {
            retry++;
            if (retry > VmsUtil.retry_aws)
                return instance;
            try
            {
                Thread.sleep(3000L);
            }
            catch (Exception e)
            {
            }
            _logger.error("AmazonEC2 runInstances retry, " + type + "|" + ami + "|" + group + "|" + key + "|" + retry);
            return inst(retry, type, ami, group, key, place, ec2);
        }
        return instance;
    }

    // scan instances via specified filters
    // filters.put("instance-state-name", "running");
    // filters.put("key-name", config.getKeyName());
    public static List<Instance> insts(long time, Map<String, String> filters, AmazonEC2 ec2)
    {
        List<Instance> insts = new ArrayList<Instance>();
        if (ec2 == null)
            return insts;

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
                    Date launch = it.getLaunchTime();
                    if (WebUtil.empty(it.getPublicIpAddress()) || launch == null || (time > 0 && time < launch.getTime()))
                        continue;
                    insts.add(it);
                    // if (tag(it)) insts.add(it);
                }
            }
            if (insts.size() > 0)
                _logger.info("AmazonEC2 describeInstances, count|" + insts.size() + (time > 0 ? "|" + DateUtil.str(new Date(time)) : ""));
        }
        catch (Exception e)
        {
            _logger.error("AmazonEC2 describeInstances fail");
        }
        return insts;
    }

    protected static boolean tag(Instance inst)
    {
        if (inst == null)
            return false;
        List<Tag> tags = inst.getTags();
        if (tags == null || tags.size() == 0)
            return false;
        for (Tag tag : tags)
            if ("NeuLionGroup".equalsIgnoreCase(tag.getKey()) && "AutoSchds".equalsIgnoreCase(tag.getValue()))
                return true;
        return false;
    }

    // start instance
    public static void inststart(int retry, String inst_id, AmazonEC2 ec2)
    {
        if (WebUtil.empty(inst_id) || ec2 == null)
            return;

        try
        {
            StartInstancesRequest start_req = new StartInstancesRequest().withInstanceIds(inst_id);
            ec2.startInstances(start_req);
            _logger.info("AmazonEC2 startInstances, " + inst_id);
        }
        catch (AmazonServiceException ase)
        {
            _logger.error("AmazonEC2 startInstances fail, " + inst_id);
        }
        catch (AmazonClientException ace)
        {
            retry++;
            if (retry > VmsUtil.retry_aws)
                return;
            try
            {
                Thread.sleep(3000L);
            }
            catch (Exception e)
            {
            }
            _logger.error("AmazonEC2 startInstances retry, " + inst_id + "|" + retry);
            inststart(retry, inst_id, ec2);
        }
    }

    // stop instance
    public static void inststop(int retry, String inst_id, AmazonEC2 ec2)
    {
        if (WebUtil.empty(inst_id) || ec2 == null)
            return;

        try
        {
            StopInstancesRequest stop_req = new StopInstancesRequest().withInstanceIds(inst_id);
            ec2.stopInstances(stop_req);
            _logger.info("AmazonEC2 stopInstances, " + inst_id);
        }
        catch (AmazonServiceException ase)
        {
            _logger.error("AmazonEC2 stopInstances fail, " + inst_id);
        }
        catch (AmazonClientException ace)
        {
            retry++;
            if (retry > VmsUtil.retry_aws)
                return;
            try
            {
                Thread.sleep(3000L);
            }
            catch (Exception e)
            {
            }
            _logger.error("AmazonEC2 stopInstances retry, " + inst_id + "|" + retry);
            inststop(retry, inst_id, ec2);
        }
    }

    // terminate instance
    public static void instend(int retry, String inst_id, AmazonEC2 ec2)
    {
        if (WebUtil.empty(inst_id) || ec2 == null)
            return;

        try
        {
            TerminateInstancesRequest end_req = new TerminateInstancesRequest().withInstanceIds(inst_id);
            ec2.terminateInstances(end_req);
            _logger.info("AmazonEC2 terminateInstances, " + inst_id);
        }
        catch (AmazonServiceException ase)
        {
            _logger.error("AmazonEC2 terminateInstances fail, " + inst_id);
        }
        catch (AmazonClientException ace)
        {
            retry++;
            if (retry > VmsUtil.retry_aws)
                return;
            try
            {
                Thread.sleep(3000L);
            }
            catch (Exception e)
            {
            }
            _logger.error("AmazonEC2 terminateInstances retry, " + inst_id + "|" + retry);
            instend(retry, inst_id, ec2);
        }
    }

    // confirm instance state changed to running/stopped/terminated, 0/1/2
    public static Instance inststatedone(int type, String inst_id, AmazonEC2 ec2)
    {
        if (WebUtil.empty(inst_id) || ec2 == null)
            return null;
        String str = "running";
        if (type == 1)
            str = "stopped";
        else if (type == 2)
            str = "terminated";

        try
        {
            InstanceState state;
            DescribeInstancesRequest query_req = new DescribeInstancesRequest().withInstanceIds(inst_id);
            DescribeInstancesResult query_resp = ec2.describeInstances(query_req);
            for (Reservation reservation : query_resp.getReservations())
            {
                for (Instance instance : reservation.getInstances())
                {
                    state = instance.getState();
                    // pending, running, shutting-down, terminated, stopping, stopped
                    if (state == null || !str.equals(state.getName()) || !inst_id.equals(instance.getInstanceId()) || WebUtil.empty(instance.getPublicIpAddress()))
                        continue;
                    _logger.info("AmazonEC2 instance is " + str + ", " + inst_id
                            + ("running".equals(str) ? "|" + instance.getPrivateIpAddress() + "|" + instance.getPublicIpAddress() : ""));
                    return instance;
                }
            }
        }
        catch (Exception e)
        {
            _logger.error("AmazonEC2 describeInstances fail, " + inst_id);
        }
        return null;
    }

    // spot(0, "0.5", "c1.xlarge", "ami-b08f15d9", "Open", "vos-agent", "us-east-1a", ec2) - sir-ab05364f
    public static String spot(int retry, String price, String type, String ami, String group, String key, String place, AmazonEC2 ec2)
    {
        String spot_id = null;
        if (WebUtil.empty(price) || WebUtil.empty(type) || WebUtil.empty(key) || WebUtil.empty(ami) || ec2 == null)
            return spot_id;

        try
        {
            RequestSpotInstancesRequest vm_req = new RequestSpotInstancesRequest().withSpotPrice(price).withInstanceCount(1);
            LaunchSpecification spec = new LaunchSpecification().withInstanceType(type).withImageId(ami).withSecurityGroups(group).withKeyName(key);
            if (!WebUtil.empty(place))
                spec = spec.withPlacement(new SpotPlacement(place));
            vm_req.setLaunchSpecification(spec);
            List<SpotInstanceRequest> vm_resp = ec2.requestSpotInstances(vm_req).getSpotInstanceRequests();
            if (vm_resp != null && vm_resp.size() > 0)
            {
                spot_id = vm_resp.get(0).getSpotInstanceRequestId();
                _logger.info("AmazonEC2 requestSpotInstances, " + price + "|" + type + "|" + ami + "|" + group + "|" + key + "|" + vm_resp.get(0).getLaunchedAvailabilityZone()
                        + "|" + WebUtil.unull(spot_id));
            }
        }
        catch (AmazonServiceException ase)
        {
            _logger.error("AmazonEC2 requestSpotInstances fail, " + price + "|" + type + "|" + ami + "|" + group + "|" + key);
        }
        catch (AmazonClientException ace)
        {
            retry++;
            if (retry > VmsUtil.retry_aws)
                return spot_id;
            try
            {
                Thread.sleep(3000L);
            }
            catch (Exception e)
            {
            }
            _logger.error("AmazonEC2 requestSpotInstances retry, " + price + "|" + type + "|" + ami + "|" + group + "|" + key + "|" + retry);
            return spot(retry, price, type, ami, group, key, place, ec2);
        }
        return spot_id;
    }

    // confirm spot request state changed to not open - i-4f953a1f, only for one request
    public static SpotInstanceRequest spotdone(String spot_id, AmazonEC2 ec2)
    {
        SpotInstanceRequest inst_req = null;
        if (WebUtil.empty(spot_id) || ec2 == null)
            return inst_req;

        try
        {
            DescribeSpotInstanceRequestsRequest query_req = new DescribeSpotInstanceRequestsRequest().withSpotInstanceRequestIds(spot_id);
            List<SpotInstanceRequest> query_resp = ec2.describeSpotInstanceRequests(query_req).getSpotInstanceRequests();
            // open, active, closed, cancelled, failed
            for (SpotInstanceRequest req : query_resp)
            {
                if ("active".equals(req.getState()))
                {
                    inst_req = req;
                    _logger.info("AmazonEC2 describeSpotInstanceRequests, " + spot_id + "|" + inst_req.getInstanceId() + "|" + inst_req.getLaunchedAvailabilityZone());
                    break;
                }
            }
        }
        catch (Exception e)
        {
            _logger.error("AmazonEC2 describeSpotInstanceRequests fail, " + spot_id);
        }
        return inst_req;
    }

    // stop spot request
    public static void spotstop(int retry, String spot_id, AmazonEC2 ec2)
    {
        if (WebUtil.empty(spot_id) || ec2 == null)
            return;

        try
        {
            CancelSpotInstanceRequestsRequest stop_req = new CancelSpotInstanceRequestsRequest().withSpotInstanceRequestIds(spot_id);
            ec2.cancelSpotInstanceRequests(stop_req);
            _logger.info("AmazonEC2 cancelSpotInstanceRequests, " + spot_id);
        }
        catch (AmazonServiceException ase)
        {
            _logger.error("AmazonEC2 cancelSpotInstanceRequests fail, " + spot_id);
        }
        catch (AmazonClientException ace)
        {
            retry++;
            if (retry > VmsUtil.retry_aws)
                return;
            try
            {
                Thread.sleep(3000L);
            }
            catch (Exception e)
            {
            }
            _logger.error("AmazonEC2 cancelSpotInstanceRequests fail, " + spot_id + "|" + retry);
            spotstop(retry, spot_id, ec2);
        }
    }

    // get all images
    public static DescribeImagesResult imgs(String own, AmazonEC2 ec2)
    {
        DescribeImagesResult ret = null;
        if (WebUtil.empty(own) || ec2 == null)
            return ret;

        try
        {
            DescribeImagesRequest imgs_req = new DescribeImagesRequest().withOwners(own);
            ret = ec2.describeImages(imgs_req);
            _logger.info("AmazonEC2 describeImages, " + own);
        }
        catch (Exception ase)
        {
            _logger.error("AmazonEC2 describeImages");
        }
        return ret;
    }

    // create image from instance
    public static String imgadd(String name, String desc, String instId, AmazonEC2 ec2)
    {
        String ret = null;
        if (WebUtil.empty(name) || WebUtil.empty(instId) || ec2 == null)
            return ret;

        try
        {
            CreateImageRequest imgs_req = new CreateImageRequest().withInstanceId(instId).withName(name).withDescription(WebUtil.empty(desc) ? name : desc);
            ret = ec2.createImage(imgs_req).getImageId();
            _logger.info("AmazonEC2 createImage, " + ret + "|" + name);
        }
        catch (Exception ase)
        {
            _logger.error("AmazonEC2 createImage");
        }
        return ret;
    }

    // delete image
    public static void imgdel(String imgId, AmazonEC2 ec2)
    {
        if (WebUtil.empty(imgId) || ec2 == null)
            return;
        try
        {
            DeregisterImageRequest imgs_req = new DeregisterImageRequest().withImageId(imgId);
            ec2.deregisterImage(imgs_req);
            _logger.info("AmazonEC2 deregisterImage, " + imgId);
        }
        catch (Exception ase)
        {
            _logger.error("AmazonEC2 deregisterImage");
        }
    }

    // get all snapshots
    public static DescribeSnapshotsResult ss(String own, AmazonEC2 ec2)
    {
        DescribeSnapshotsResult ret = null;
        if (WebUtil.empty(own) || ec2 == null)
            return ret;

        try
        {
            DescribeSnapshotsRequest imgs_req = new DescribeSnapshotsRequest().withOwnerIds(own);
            ret = ec2.describeSnapshots(imgs_req);
            _logger.info("AmazonEC2 describeSnapshots, " + own);
        }
        catch (Exception ase)
        {
            _logger.error("AmazonEC2 describeSnapshots");
        }
        return ret;
    }

    // delete snapshot
    public static void ssdel(String ssId, AmazonEC2 ec2)
    {
        if (WebUtil.empty(ssId) || ec2 == null)
            return;
        try
        {
            DeleteSnapshotRequest ss_req = new DeleteSnapshotRequest().withSnapshotId(ssId);
            ec2.deleteSnapshot(ss_req);
            _logger.info("AmazonEC2 deleteSnapshot, " + ssId);
        }
        catch (Exception ase)
        {
            _logger.error("AmazonEC2 deleteSnapshot");
        }
    }

    // create tags, inst_id or spot_id
    public static void tags(String id, List<Tag> tags, AmazonEC2 ec2)
    {
        if (WebUtil.empty(id) || tags == null || tags.size() == 0 || ec2 == null)
            return;

        CreateTagsRequest tags_req = new CreateTagsRequest().withResources(id).withTags(tags);
        try
        {
            ec2.createTags(tags_req);
            _logger.info("AmazonEC2 createTags, " + id);
        }
        catch (Exception e)
        {
            _logger.error("AmazonEC2 createTags fail, " + id);
        }
    }
}
