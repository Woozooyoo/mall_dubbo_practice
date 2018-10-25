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


    public static  String doGet(String url){

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet httpGet=new HttpGet(url);
        String result=null;
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
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
