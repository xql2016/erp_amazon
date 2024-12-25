package tools;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import model.constant.CommonConstant;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/7/23
 */
//@Slf4j
public class HttpUtils {

    private static final int DEFAULT_CONNECT_TIMEOUT = 1000;

    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 3000;

    private static final int DEFAULT_SOCAT_TIMEOUT = 10000;

    public static String doGet(String url, Map<String,String> params) {
        URI uri =null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            uri = uriBuilder.build();

        } catch (URISyntaxException e) {
            //log.debug("url:{},param:{}", url, JSON.toJSONString(params), e);
            // todo exception throw

        }

        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(uri);
            // 设置请求头信息，鉴权
            httpGet.addHeader("Accept", "application/json; charset=utf-8");
            // 设置配置请求参数
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 连接主机服务超时时间
                    .setConnectionRequestTimeout(35000)// 请求超时时间
                    .setSocketTimeout(60000)// 数据读取超时时间
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                //log.debug("http statusCode error,uri:{},statusCode:{}", uri, response.getStatusLine().getStatusCode());
            }
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            //log.debug("ClientProtocolException,uri:{}", uri, e);

            // todo exception throw
        } catch (IOException e) {
            //log.debug("IOException,uri:{}", uri, e);
            // todo exception throw
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    //log.debug("httpResponse close has IOException,uri:{}", uri, e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    //log.debug("httpClient close has IOException,uri:{}", uri, e);
                }
            }
        }
        return result;
    }


    public static <T> String doPost(String url, Map<String, String> header, T body, Integer connectTimeout, Integer connectionRequestTimeout, Integer socketTimeout) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = "";
        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);
        // 配置请求参数实例
        connectTimeout = connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
        connectionRequestTimeout = connectionRequestTimeout == null ? DEFAULT_CONNECTION_REQUEST_TIMEOUT : connectionRequestTimeout;

        socketTimeout = socketTimeout == null ? DEFAULT_SOCAT_TIMEOUT : socketTimeout;
        RequestConfig requestConfig = RequestConfig.custom()
                // 设置连接主机服务超时时间
                .setConnectTimeout(connectTimeout)
                // 设置连接请求超时时间
                .setConnectionRequestTimeout(connectionRequestTimeout)
                // 设置读取数据连接超时时间
                .setSocketTimeout(socketTimeout)
                .build();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        httpPost.addHeader("Accept", "application/json; charset=utf-8");
        if(MapUtils.isNotEmpty(header)) {
            for(String key : header.keySet()) {
                httpPost.addHeader(key, header.get(key));
            }
        }

        // 封装post请求参数
        String bodyStr = JSON.toJSONString(body);
        StringEntity se = new StringEntity(bodyStr, "utf-8");
        httpPost.setEntity(se);
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                //log.debug("http statusCode error,statusCode:{}", httpResponse.getStatusLine().getStatusCode());
                if (httpResponse.getStatusLine().getStatusCode() == 401) {
                    return CommonConstant.INVALID_TOKEN;
                } else {
                    return CommonConstant.HTTP_ERROR;
                }
            }
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);

        } catch (ClientProtocolException e) {
            //log.debug("ClientProtocolException,body:{}", bodyStr);

        } catch (IOException e) {
            //log.debug("IOException,body:{}", bodyStr);

        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    //log.debug("httpResponse close has IOException,url:{},body:{}", url, bodyStr);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    //log.debug("httpclient close has IOException,url:{},body:{}", url, bodyStr);
                }
            }
        }
        return result;
    }
}
