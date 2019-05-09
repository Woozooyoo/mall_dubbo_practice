package com.atguigu.gmall1129.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @param
 * @return
 */
public class HttpClientUtil {

    /** 模拟一个浏览器  在第三方接口经常会用
     * @param url
     * @return
     */
    public static  String doGet(String url){

        //模拟一个浏览器
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        //get请求，参数拼在url里面
        HttpGet httpGet=new HttpGet(url);
        String result=null;
        CloseableHttpResponse httpResponse = null;
        try {
            //执行一个请求
            httpResponse = httpClient.execute(httpGet);
            //结果是一个io流
            HttpEntity entity = httpResponse.getEntity();
            //io流 用EntityUtils得到string，得到get请求后的结果
            result = EntityUtils.toString(entity);

            EntityUtils.consume(entity);
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static void main(String[] args) throws IOException {


    }
}
