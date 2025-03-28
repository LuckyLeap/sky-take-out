package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * 微信支付工具类
 */
@Component
public class WeChatPayUtil {

    //微信支付下单接口地址
    public static final String JSAPI = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";

    //申请退款接口地址
    public static final String REFUNDS = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";


    private static final String SIGN_ALGORITHM = "SHA256withRSA"; // 提取为常量
    private static final String SIGN_TYPE = "RSA"; // 提取为常量

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 获取调用微信接口的客户端工具对象
     */
    private CloseableHttpClient getClient() {
        PrivateKey merchantPrivateKey;
        try {
            //merchantPrivateKey商户API私钥，如何加载商户API私钥请看常见问题
            merchantPrivateKey = PemUtil.loadPrivateKey(new FileInputStream(weChatProperties.getPrivateKeyFilePath()));
            //加载平台证书文件
            X509Certificate x509Certificate = PemUtil.loadCertificate(new FileInputStream(weChatProperties.getWeChatPayCertFilePath()));
            //wechatPayCertificates微信支付平台证书列表。
            List<X509Certificate> wechatPayCertificates = Collections.singletonList(x509Certificate);

            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(weChatProperties.getMchid(), weChatProperties.getMchSerialNo(), merchantPrivateKey)
                    .withWechatPay(wechatPayCertificates);

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            return builder.build();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("加载商户API私钥失败，请检查商户API私钥文件路径是否正确配置。");
        }
    }

    // 枚举定义 HTTP 方法
    enum HttpMethod {
        GET, POST
    }

    /**
     * 发送post方式请求
     */
    private String post(String url, String body) throws Exception {
        return executeRequest(url, body, HttpMethod.POST);
    }

    /**
     * 发送get方式请求
     */
    private String get(String url) throws Exception {
        return executeRequest(url, null, HttpMethod.GET);
    }

    /**
     * 通用请求执行方法
     */
    private String executeRequest(String url, String body, HttpMethod method) throws Exception {
        // 校验 Wechatpay-Serial 是否为空
        String mchSerialNo = weChatProperties.getMchSerialNo();
        if (mchSerialNo == null || mchSerialNo.isEmpty()) {
            throw new IllegalArgumentException("微信支付序列号（Wechatpay-Serial）不能为空，请检查配置文件是否正确设置。");
        }

        try (CloseableHttpClient httpClient = getClient()) {
            HttpUriRequest request;
            if (method == HttpMethod.POST) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
                httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                httpPost.addHeader("Wechatpay-Serial", mchSerialNo);

                if (body != null) {
                    httpPost.setEntity(new StringEntity(body, "UTF-8"));
                }
                request = httpPost;
            } else {
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
                httpGet.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                httpGet.addHeader("Wechatpay-Serial", mchSerialNo);
                request = httpGet;
            }

            if (httpClient != null) {
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        return EntityUtils.toString(entity);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("执行 HTTP 请求时发生错误: " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * jsapi下单
     */
    private String jsapi(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        if (orderNum == null || total == null || total.compareTo(BigDecimal.ZERO) <= 0 || description == null || openid == null) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appid", weChatProperties.getAppid());
        jsonObject.put("mchid", weChatProperties.getMchid());
        jsonObject.put("description", description);
        jsonObject.put("out_trade_no", orderNum);
        jsonObject.put("notify_url", weChatProperties.getNotifyUrl());

        JSONObject amount = new JSONObject();
        amount.put("total", convertToInteger(total));
        amount.put("currency", "CNY");

        jsonObject.put("amount", amount);

        JSONObject payer = new JSONObject();
        payer.put("openid", openid);

        jsonObject.put("payer", payer);

        String body = jsonObject.toJSONString();
        return post(JSAPI, body);
    }

    /**
     * 小程序支付
     */
    public JSONObject pay(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        if (orderNum == null || total == null || total.compareTo(BigDecimal.ZERO) <= 0 || description == null || openid == null) {
            throw new IllegalArgumentException("输入参数无效，请检查 orderNum、total、description 和 openid 是否正确设置。");
        }

        // 统一下单，生成预支付交易单
        String bodyAsString = jsapi(orderNum, total, description, openid);

        if (bodyAsString == null || bodyAsString.isEmpty()) {
            throw new RuntimeException("统一下单请求失败或返回空响应，请检查接口调用是否正常");
        }

        // 解析返回结果
        JSONObject jsonObject = JSON.parseObject(bodyAsString);

        String prepayId = jsonObject.getString("prepay_id");
        if (prepayId == null || prepayId.isEmpty()) {
            throw new RuntimeException("响应中缺少预支付 ID (prepay_id)，请检查接口返回结果是否正确");
        }

        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = generateSecureNonce();

        // 二次签名
        String packageSign = signMessage(List.of(
                weChatProperties.getAppid(),
                timeStamp,
                nonceStr,
                "prepay_id=" + prepayId
        ));

        // 构造数据给微信小程序，用于调起微信支付
        JSONObject jo = new JSONObject();
        jo.put("timeStamp", timeStamp);
        jo.put("nonceStr", nonceStr);
        jo.put("package", "prepay_id=" + prepayId);
        jo.put("signType", SIGN_TYPE);
        jo.put("paySign", packageSign);

        return jo;
    }

    /**
     * 申请退款
     */
    public String refund(String outTradeNo, String outRefundNo, BigDecimal refund, BigDecimal total) throws Exception {
        if (outTradeNo == null || outRefundNo == null || refund == null || refund.compareTo(BigDecimal.ZERO) <= 0 || total == null || total.compareTo(refund) < 0) {
            throw new IllegalArgumentException("无效的输入参数");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("out_refund_no", outRefundNo);

        JSONObject amount = new JSONObject();
        amount.put("refund", convertToInteger(refund));
        amount.put("total", convertToInteger(total));
        amount.put("currency", "CNY");

        jsonObject.put("amount", amount);
        jsonObject.put("notify_url", weChatProperties.getRefundNotifyUrl());

        String body = jsonObject.toJSONString();

        // 调用申请退款接口
        String result = post(REFUNDS, body);

        if (result == null || result.isEmpty()) {
            throw new RuntimeException("退款请求失败或返回空响应");
        }

        return result;
    }

    /**
     * 签名方法
     */
    private String signMessage(List<String> elements) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (String element : elements) {
            stringBuilder.append(element).append("\n");
        }
        byte[] message = stringBuilder.toString().getBytes();

        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        PrivateKey privateKey = PemUtil.loadPrivateKey(new FileInputStream(weChatProperties.getPrivateKeyFilePath()));
        signature.initSign(privateKey);
        signature.update(message);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 生成安全的随机数
     */
    private String generateSecureNonce() {
        return RandomStringUtils.randomAlphanumeric(32); // 使用更安全的随机数生成器
    }

    /**
     * 将金额转换为整数（分）
     */
    private int convertToInteger(BigDecimal amount) {
        return amount.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).intValue();

    }
}