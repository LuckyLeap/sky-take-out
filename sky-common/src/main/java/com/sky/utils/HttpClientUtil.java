package com.sky.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Http工具类
 */
public class HttpClientUtil {

    private static final Logger LOGGER = Logger.getLogger(HttpClientUtil.class.getName()); // 设置日志记录器
    private static final String CHARSET = "UTF-8"; // 设置字符集
    private static final int TIMEOUT_MSEC = 5 * 1000; // 设置请求超时时间

    // 使用连接池管理 HttpClient 实例
    private static final PoolingHttpClientConnectionManager CONNECTION_MANAGER = new PoolingHttpClientConnectionManager();
    private static final CloseableHttpClient HTTP_CLIENT;

    static {
        CONNECTION_MANAGER.setMaxTotal(50); // 设置最大连接数
        CONNECTION_MANAGER.setDefaultMaxPerRoute(10); // 设置每个路由的最大连接数
        HTTP_CLIENT = HttpClients.custom().setConnectionManager(CONNECTION_MANAGER).build(); // 创建HttpClient实例
    }

    /**
     * 发送GET方式请求
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL 不能为空");
        }

        try {
            URIBuilder builder = new URIBuilder(url);
            if (paramMap != null) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if (entry.getValue() != null) {
                        builder.addParameter(entry.getKey(), entry.getValue());
                    }
                }
            }
            URI uri = builder.build();

            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(builderRequestConfig());

            return executeRequest(httpGet);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "无效的URL语法", e);
            return "";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "GET请求发生错误", e);
            return "";
        }
    }

    /**
     * 发送POST方式请求
     */
    public static String doPost(String url, Map<String, String> paramMap) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL 不能为空");
        }

        try {
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if (entry.getValue() != null) {
                        paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, CHARSET);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            return executeRequest(httpPost);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "POST请求发生错误", e);
            return "";
        }
    }

    /**
     * 发送POST方式请求（JSON格式）
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL 不能为空");
        }

        try {
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                StringBuilder jsonBuilder = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if (entry.getValue() != null) {
                        if (!first) {
                            jsonBuilder.append(",");
                        }
                        jsonBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                        first = false;
                    }
                }
                jsonBuilder.append("}");

                StringEntity entity = new StringEntity(jsonBuilder.toString(), CHARSET);
                entity.setContentEncoding(CHARSET);
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            return executeRequest(httpPost);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "JSON POST请求发生错误", e);
            return "";
        }
    }

    /**
     * 执行HTTP请求并返回结果
     */
    private static String executeRequest(org.apache.http.client.methods.HttpRequestBase request) throws IOException {
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), CHARSET);
            } else {
                LOGGER.log(Level.WARNING, "请求失败，状态码: " + response.getStatusLine().getStatusCode());
                return "";
            }
        }
    }

    /**
     * 设置请求超时时间
     */
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC)
                .build();
    }
}