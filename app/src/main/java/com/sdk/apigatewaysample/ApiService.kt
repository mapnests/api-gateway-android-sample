package com.sdk.apigatewaysample

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("load-test/api/auth-casbin-success-plugin-test")
    fun getRandomDog(): Call<ResponseBody>
}