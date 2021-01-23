package com.example.common.utils;

import com.example.common.models.DuplicateParamEntry;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NetworkUtil {

    public static void postData(String url, String body, String authorization) throws IOException {
        new Thread(() -> {

            CloseableHttpClient client = HttpClients.createDefault();
            String newUrl = url.replace(" ", "%20");
            HttpPost httpPost = new HttpPost(newUrl);

            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body, "UTF-8");
                httpPost.setEntity(entity);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setHeader("Authorization", authorization);
            }
            try {
                CloseableHttpResponse response = client.execute(httpPost);
                client.close();
                response.close();
            } catch (IOException ignored) {

            }

        }).start();
//        HttpEntity e = response.getEntity();
//        InputStream is = e.getContent();
    }

    public static CloseableHttpResponse postFormData(String url, String authorization, Map<String, String> bodyParams) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url.replace(" ", "%20"));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("Authorization", authorization);

        List<NameValuePair> params = bodyParams
                .entrySet()
                .stream()
                .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        return client.execute(httpPost);
    }

    public static CloseableHttpResponse postFormData(String url, String authorization, List<DuplicateParamEntry> bodyParams) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url.replace(" ", "%20"));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("Authorization", authorization);

        List<NameValuePair> params = bodyParams
                .stream()
                .map(a -> new BasicNameValuePair(a.getKey(), a.getValue())).collect(Collectors.toList());
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        return client.execute(httpPost);
    }


}
