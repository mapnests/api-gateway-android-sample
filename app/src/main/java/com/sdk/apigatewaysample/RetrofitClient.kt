package com.sdk.apigatewaysample

import android.content.Context
import com.technonext.network.ApiGateWayInterceptor
import io.nerdythings.okhttp.profiler.BuildConfig
import io.nerdythings.okhttp.profiler.OkHttpProfilerInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    fun create(
        baseUrl: String,
        context: Context
    ): Retrofit {

        val okHttp = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            // Your interceptors
            .addInterceptor(ClientInterceptor())
            // Api GateWay Interceptor (MUST BE AT THE BOTTOM)
            .addInterceptor(ApiGateWayInterceptor(context))

            // OkHttp Profiler (for debug only)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(OkHttpProfilerInterceptor())
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
