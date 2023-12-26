package com.example.walkerholic

import android.content.ContentValues.TAG
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.walkerholic.data.FoodItem
import com.example.walkerholic.data.Root
import com.example.walkerholic.databinding.ActivitySearchKcalBinding
import com.example.walkerholic.databinding.ActivitySearchKcalDetailBinding
import com.example.walkerholic.network.FoodImgAPIService
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SearchKcalDetailActivity : AppCompatActivity() {
    lateinit var  service : FoodImgAPIService

    val searchDetailBinding by lazy {
        ActivitySearchKcalDetailBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(searchDetailBinding.root)

        val food = intent.getSerializableExtra("food") as FoodItem

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build();

        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.naver_api_url))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        service = retrofit.create(FoodImgAPIService::class.java)

        val apiCallback = object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.isSuccessful) {
                    try {
                        val root: Root? = response.body()
                        val imgUrl = root?.items?.get(0)?.imgUrl.toString()

                        Glide.with(this@SearchKcalDetailActivity)
                            .load(imgUrl)
                            .into(searchDetailBinding.imgFood)

                        searchDetailBinding.tvFood.setText(food.foodName)
                        searchDetailBinding.tvKcal.setText(food.calories.toString())
                        searchDetailBinding.tvProtein.setText(food.protein.toString())
                        searchDetailBinding.tvSodium.setText(food.sodium.toString())
                        searchDetailBinding.tvCarbohydrates.setText(food.carbohydrates.toString())
                        searchDetailBinding.tvSugar.setText(food.sugar.toString())
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception during JSON parsing: ${e.message}")
                    }
                } else {
                    Log.d(TAG, "Unsuccessful Response")
                    Log.d(TAG, response.errorBody()!!.string()) // 응답 오류가 있을 때 상세정보 확인
                }
            }

            override fun onFailure(call: Call<Root>, t: Throwable) {
                Log.d(TAG, "OpenAPI Call Failure ${t.message}")
            }
        }

        val apiCall: Call<Root> = service.getBooksByKeyword(
            resources.getString(R.string.client_id),
            resources.getString(R.string.client_secret),
            food.foodName,
            1
        )

        apiCall.enqueue(apiCallback)
    }
}