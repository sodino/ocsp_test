package com.ocsp.sodino.ocsp_test;

import android.util.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sodino on 2017/5/4.
 */

public class HttpDNSInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originRequest = chain.request();
        HttpUrl httpUrl = originRequest.url();

        String url = httpUrl.toString();
        String host = httpUrl.host();
        Log.e("Test", "origin url:" + url);
        Log.e("Test", "origin host:" + host);

        String hostIP = "27.148.145.195:443";

        Request.Builder builder = originRequest.newBuilder();
        if (hostIP != null) {
            builder.url("https://27.148.145.195:443");
            builder.header("host", host);
            Log.e("HttpDNS", "the host has replaced with ip " + hostIP);
        } else {
            Log.e("HttpDNS", "can't get the ip , can't replace the host");
        }

        Request newRequest = builder.build();
        Log.e("Test", "newUrl:" + newRequest.url());
        Response newResponse = chain.proceed(newRequest);
        return newResponse;
    }
}
