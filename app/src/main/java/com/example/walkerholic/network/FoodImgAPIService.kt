package com.example.walkerholic.network

import com.example.walkerholic.data.Root
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FoodImgAPIService {
    @GET("v1/search/image")
    fun getBooksByKeyword (
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") keyword: String,
        @Query("display") display: Int,
    )  : Call<Root>
}