package com.example.walkerholic

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.walkerholic.data.DiaryRecord
import com.example.walkerholic.data.DiaryRecordDao
import com.example.walkerholic.data.DiaryRecordDatabase
import com.example.walkerholic.data.FoodItem
import com.example.walkerholic.data.Root
import com.example.walkerholic.databinding.ActivitySearchKcalDetailBinding
import com.example.walkerholic.databinding.ActivityWriteDiaryBinding
import com.example.walkerholic.network.FoodImgAPIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WriteDiaryActivity : AppCompatActivity() {

    val writeDiaryBinding by lazy {
        ActivityWriteDiaryBinding.inflate(layoutInflater)
    }

    lateinit var  service : FoodImgAPIService
    lateinit var db : DiaryRecordDatabase
    lateinit var diaryRecordDao : DiaryRecordDao

    var imgUrl : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(writeDiaryBinding.root)

        db = DiaryRecordDatabase.getDatabase(this)
        diaryRecordDao = db.diaryRecordDao()

        val food = intent.getSerializableExtra("food") as FoodItem
        val todayDate = intent.getStringExtra("todayDate")
        val stepCount = intent.getStringExtra("stepCount")
        val kcal = intent.getStringExtra("kcal")
        Log.d("ddd", "넘어온값 : ${food.foodName} ${stepCount} ${kcal}")
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

        writeDiaryBinding.btnBack.setOnClickListener{
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }

        writeDiaryBinding.btnSave.setOnClickListener{
            var content = writeDiaryBinding.etContent.text.toString()

            var addRecord = DiaryRecord(0, imgUrl, food.foodName, todayDate.toString(), stepCount, kcal, food.calories.toString(), content)
            addDiaryRecord(addRecord)

            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
            finish()
        }

        service = retrofit.create(FoodImgAPIService::class.java)

        val apiCallback = object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.isSuccessful) {
                    try {
                        val root: Root? = response.body()
                        imgUrl = root?.items?.get(0)?.imgUrl.toString()


                        Glide.with(this@WriteDiaryActivity)
                            .load(imgUrl)
                            .into(writeDiaryBinding.imgFood)

                        writeDiaryBinding.tvDate.setText(todayDate)
                        writeDiaryBinding.tvStepCount.setText(stepCount)
                        writeDiaryBinding.tvKcal.setText(kcal)
                        writeDiaryBinding.tvFood.setText(food.foodName)
                        writeDiaryBinding.tvFoodKcal.setText(food.calories.toString())

                    } catch (e: Exception) {
                        Log.e(ContentValues.TAG, "Exception during JSON parsing: ${e.message}")
                    }
                } else {
                    Log.d(ContentValues.TAG, "Unsuccessful Response")
                    Log.d(ContentValues.TAG, response.errorBody()!!.string()) // 응답 오류가 있을 때 상세정보 확인
                }
            }

            override fun onFailure(call: Call<Root>, t: Throwable) {
                Log.d(ContentValues.TAG, "OpenAPI Call Failure ${t.message}")
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

    fun addDiaryRecord(record: DiaryRecord) {
        CoroutineScope(Dispatchers.IO).launch {
            diaryRecordDao.insertDiary(record)
        }
    }
}