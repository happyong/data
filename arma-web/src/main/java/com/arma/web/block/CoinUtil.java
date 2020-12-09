package com.arma.web.block;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;

import com.arma.web.Config;
import com.arma.web.support.client.BufferedResponseConsumer;
import com.arma.web.support.client.ClientPool;
import com.arma.web.util.ArmaUtil;
import com.arma.web.util.WebUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

public class CoinUtil implements FutureCallback<HttpResponse>
{
    private static Log _logger = LogFactory.getLog("web.TextUtil");

    private volatile CountDownLatch latch;
    private volatile List<CoinItem> list;

    protected void coindetail()
    {
        _logger.info("begin coindetail, " + zt_base);

        try
        {
            latch = new CountDownLatch(leader.length);
            list = new ArrayList<CoinItem>();
            // got leader detail
            for (String code : leader)
                coinget(code);
            latch.await();
            Collections.sort(list, new CoinComp());

            // got follower detail
            List<String> follows = new ArrayList<String>();
            for (String code : follower)
                if (WebUtil.pos(code, leader) == -1)
                    follows.add(code);
            latch = new CountDownLatch(follows.size());
            for (String code : follows)
                coinget(code);
            latch.await();

            StringBuilder builder = new StringBuilder();
            for (CoinItem item : list)
                item.toText(builder);
            FileUtils.write(new File("d:/test/coin.txt"), builder, WebUtil.CHARSET_UTF_8);
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        _logger.info("end coindetail");
    }

    private void coinget(String code)
    {
        HttpUriRequest request = new HttpGet(zt_base + code);
        final HttpAsyncRequestProducer producer = HttpAsyncMethods.create(request);
        final HttpAsyncResponseConsumer<HttpResponse> consumer = new BufferedResponseConsumer(); // new BasicAsyncResponseConsumer();
        ClientPool.execute(producer, consumer, null, this);
    }

    @Override
    public void cancelled()
    {
        latch.countDown();
    }

    @Override
    public void completed(HttpResponse resp)
    {
        try
        {
            CoinZT bean = ArmaUtil.mapper.readValue(resp.getEntity().getContent(), CoinZT.class);
            if (bean != null)
            {
                list.add(bean.result);
                _logger.info("got zt deltail, " + zt_base + bean.result.code);
            }
        }
        catch (Exception e)
        {
            _logger.error("unexpected exception : " + e, e);
        }
        latch.countDown();
    }

    @Override
    public void failed(Exception e)
    {
        latch.countDown();
        _logger.error("unexpected exception : " + e, e);
    }

    public static class CoinZT
    {
        private int code; // 0
        private String message;
        private CoinItem result;

        public int getCode()
        {
            return code;
        }

        public String getMessage()
        {
            return message;
        }

        public CoinItem getResult()
        {
            return result;
        }
    }

    @JsonRootName("result")
    public static class CoinItem
    {
        @JsonProperty("asset_code")
        private String code; // "ETH"
        @JsonProperty("full_name")
        private String name; // "Ethereum"
        @JsonProperty("official_website")
        private String website; // "https://www.ethereum.org/"
        @JsonProperty("block_query")
        private String scan; // "https://etherscan.io/"
        @JsonProperty("white_paper")
        private String docs; // "https://github.com/ethereum/wiki/wiki/White-Paper"
        @JsonProperty("release_time")
        private String releaseTime; // "2015-03-20"
        @JsonProperty("release_total")
        private String releaseTotal; // "72,000,000+18,720,000/年"
        @JsonProperty("DeletedAt")
        private String dieTime;
        @JsonProperty("detail")
        private String detail;

        public void toText(StringBuilder builder)
        {
            builder.append(WebUtil.LINE_WIN).append(code).append('\t').append(name).append(", ").append(fmtTime(releaseTime)).append(", ").append(releaseTotal);
            if (!WebUtil.empty(dieTime))
                builder.append(", ").append(fmtTime(dieTime));
            builder.append(WebUtil.LINE_WIN).append(website).append(WebUtil.LINE_WIN);
            builder.append(scan).append(WebUtil.LINE_WIN);
            builder.append(docs).append(WebUtil.LINE_WIN);
            builder.append(detail).append(WebUtil.LINE_WIN);
        }

        private String fmtTime(String time)
        {
            return time.substring(0, 10);
        }
    }

    public class CoinComp implements Comparator<CoinItem>
    {
        public int compare(CoinItem bean1, CoinItem bean2)
        {
            return (bean1.releaseTime == null ? -1 : bean1.releaseTime.compareTo(bean2.releaseTime));
        }
    }

    public static void main(String[] args)
    {
        Config.getInstance().setUp();

        CoinUtil util = new CoinUtil();
        util.coindetail();

        Config.getInstance().tearDown();
    }

    private static final String zt_base = "https://www.zt.com/api/v1/assetIntro/";
    private static final String[] leader = new String[] { "LTC", "ETH", "EOS", "ETC", "BTC", "XRP", "USDT", "DASH" };
    private static final String[] follower = new String[] {};
}
