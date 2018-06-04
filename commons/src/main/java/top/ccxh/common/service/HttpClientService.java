package top.ccxh.common.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
/**
 * 简化http操作
 * @author shaw
 */
public class HttpClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);

    @Autowired(required = false)
    private CloseableHttpClient httpClient;

    @Autowired(required = false)
    private RequestConfig requestConfig;

    /**
     * 执行get请求
     *
     * @param url
     * @return
     * @throws Exception
     */
    public String doGet(String url, Map<String, String> params, Map<String, String> header, String encode) throws Exception {
        if (null != params) {
            URIBuilder builder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.setParameter(entry.getKey(), entry.getValue());
            }
            url = builder.build().toString();
        }
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(url);
        if (null != header) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }

        httpGet.setConfig(requestConfig);
        return execute(httpGet);
    }

    public String doGet(String url, String encode) throws Exception {
        return this.doGet(url, null, null, encode);
    }

    public String doGet(String url) throws Exception {
        return this.doGet(url, null, null, null);
    }

    public String doGetSetHaeader(String url, Map<String, String> header) throws Exception {
        return this.doGet(url, null, header, null);
    }


    /**
     * 带参数的get请求
     *
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public String doGet(String url, Map<String, String> params) throws Exception {
        return this.doGet(url, params, null, null);
    }

    /**
     * 执行POST请求
     *
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public String doPost(String url, Map<String, String> params, String encode,Map<String, String> header) throws Exception {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        if (header!=null){
            for (Map.Entry<String,String> entry:header.entrySet()){
                httpPost.setHeader(entry.getKey(),entry.getValue());
            }

        }
        if (null != params) {
            // 设置2个post参数，一个是scope、一个是q
            List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // 构造一个form表单式的实体
            UrlEncodedFormEntity formEntity = null;
            if (encode != null) {
                formEntity = new UrlEncodedFormEntity(parameters, encode);
            } else {
                formEntity = new UrlEncodedFormEntity(parameters);
            }
            // 将请求实体设置到httpPost对象中
            httpPost.setEntity(formEntity);
        }
        return execute(httpPost);
    }

    /**
     * 执行提交
     *
     * @param httpMethod
     * @return
     */
    private String execute(HttpRequestBase httpMethod) {
        CloseableHttpResponse response = null;
        LOGGER.debug("执行{}请求，URL = {}",httpMethod.getMethod(),httpMethod.getURI());
        // 执行请求
        try {
            response = httpClient.execute(httpMethod);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }else {
                LOGGER.info("httpCode:{}",response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(response);
        }
        return null;
    }

    /**
     * 执行POST请求
     *
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public String doPost(String url, Map<String, String> params) throws Exception {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        if (null != params) {
            // 设置2个post参数，一个是scope、一个是q
            List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // 构造一个form表单式的实体
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
            // 将请求实体设置到httpPost对象中
            httpPost.setEntity(formEntity);
        }
        return execute(httpPost);

    }

    public String doPostJson(String url, String json) throws Exception {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        if (null != json) {
            //设置请求体为 字符串
            StringEntity stringEntity = new StringEntity(json, "UTF-8");
            httpPost.setEntity(stringEntity);
        }

        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpClient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    public CloseableHttpResponse doResponse(String url) {
        LOGGER.debug("执行GET请求，URL = {}", url);
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //非200 状态 关闭respones org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
        HttpClientService.closeIO(response);
        return null;
    }

    public CloseableHttpClient gethttpClient() {
        return this.httpClient;
    }

    public RequestConfig getrequestConfig() {
        return this.requestConfig;
    }

    public static void closeIO(Closeable response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                response = null;
            }
        }
    }

    /**
     * 上传文件
     * @param file
     * @param url 上传地址
     * @param fileuploadName 上传时的key
     * @param params 其他参数
     * @param header 其他参数
     * @return
     * @throws FileNotFoundException
     */
    public String uploadFile(File file, String url, String fileuploadName, Map<String, String> params, Map<String, String> header) throws FileNotFoundException {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(fileuploadName, new FileInputStream(file), ContentType.MULTIPART_FORM_DATA, file.getName());// 文件流
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addTextBody(entry.getKey(), entry.getValue());
            }
        }
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity entity = builder.build();
        httpPost.setEntity(entity);
        return execute(httpPost);
    }

    public String doPostSetHeander(String url, Map<String,String> map) throws Exception {
        return  this.doPost(url,null,null,map);
    }
}
