package com.example.common.utils;

import com.example.common.models.DuplicateParamEntry;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NetworkUtil {

    public static void postData(String url, String body, String authorization, boolean ignoreCert) throws IOException {
        new Thread(() -> {
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
                CloseableHttpClient client = ignoreCert ?
                        HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom()
                                        .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                                        .build()
                                )
                        ).build()
                        : HttpClients.createDefault();

                CloseableHttpResponse response = client.execute(httpPost);
                client.close();
                response.close();
            } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    public static CloseableHttpResponse postFormData(String url, String authorization, Map<String, String> bodyParams) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url.replace(" ", "%20"));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        if (authorization != null && !authorization.isEmpty()) {
            httpPost.setHeader("Authorization", authorization);
        }

        List<NameValuePair> params = bodyParams
                .entrySet()
                .stream()
                .map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        return client.execute(httpPost);
    }

    public static CloseableHttpResponse postFormData(
            String url, String authorization,
            List<DuplicateParamEntry> bodyParams,
            ContentType contentType
    ) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url.replace(" ", "%20"));
        httpPost.setHeader("Content-Type", contentType.getMimeType());
        if (authorization != null && !authorization.isEmpty()) {
            httpPost.setHeader("Authorization", authorization);
        }

        List<NameValuePair> params = bodyParams
                .stream()
                .map(a -> new BasicNameValuePair(a.getKey(), a.getValue())).collect(Collectors.toList());
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        return client.execute(httpPost);
    }

    public static String getResponseFromHttpPut(String url, String body, String authorization) throws IOException {
        String jsonString = null;
        CloseableHttpClient client = HttpClients.createDefault();
        String newUrl = url.replace(" ", "%20");
        HttpPut httpPost = new HttpPut(newUrl);

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        httpPost.setConfig(requestConfig);

        if (body != null && !body.isEmpty()) {
            StringEntity entity = new StringEntity(body, "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", authorization);
            System.out.println("\n-------------------------------- Request Body to URL -> " + url + " -----------------------------------------------\n");
            System.out.println(EntityUtils.toString(entity));
        }
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            jsonString = EntityUtils.toString(response.getEntity());
            System.out.println(response);
            System.out.println("\n-------------------------------- Response for URL -> " + url + " -----------------------------------------------\n");
            System.out.println(jsonString);
            client.close();
            response.close();
        } catch (IOException ignored) {

        }
        return jsonString;
    }

    public static String getResponseFromGETRequest(String url, String authorization) throws IOException {

        String jsonString = null;
        CloseableHttpClient client = HttpClients.createDefault();
        String newUrl = url.replace(" ", "%20");
        HttpGet httpGet = new HttpGet(newUrl);

        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");
        httpGet.setHeader("Authorization", authorization);

        try {
            CloseableHttpResponse response = client.execute(httpGet);
            jsonString = EntityUtils.toString(response.getEntity());
            System.out.println(response);
            System.out.println("\n-------------------------------- Response for URL -> " + url + " -----------------------------------------------\n");
            System.out.println(jsonString);
            client.close();
            response.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return jsonString;
    }

}
