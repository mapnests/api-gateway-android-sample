package com.sdk.apigatewaysample;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ClientInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request newRequest = originalRequest.newBuilder()
                .addHeader("Client-Header-Name1", "xxxxxx")
                .addHeader("Client-Header-Name2", "yyyyyy")
                .build();

        return chain.proceed(newRequest);
    }
}
