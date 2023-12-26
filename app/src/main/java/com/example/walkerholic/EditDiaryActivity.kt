package com.example.walkerholic

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.walkerholic.data.DiaryRecord
import com.example.walkerholic.data.DiaryRecordDao
import com.example.walkerholic.data.DiaryRecordDatabase
import com.example.walkerholic.data.FoodItem
import com.example.walkerholic.data.Root
import com.example.walkerholic.databinding.ActivityDiaryBinding
import com.example.walkerholic.databinding.ActivityEditDiaryBinding
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

class EditDiaryActivity : AppCompatActivity() {
    val editDiaryBinding by lazy {
        ActivityEditDiaryBinding.inflate(layoutInflater)
    }
    lateinit var db : DiaryRecordDatabase
    lateinit var diaryRecordDao : DiaryRecordDao
    lateinit var  service : FoodImgAPIService
    var imgUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(editDiaryBinding.root)

        db = DiaryRecordDatabase.getDatabase(this)
        diaryRecordDao = db.diaryRecordDao()
        val record = intent.getSerializableExtra("record") as DiaryRecord

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

        Log.d("di", "${record.foodName}")
        val intent = Intent(this, DiaryActivity::class.java)
        editDiaryBinding.btnUpdate.setOnClickListener {
            modifyFood(record.foodName.toString(), record.date)
            startActivity(intent)
            finish()
        }

        editDiaryBinding.btnDelete.setOnClickListener {
            removeFood(record.foodName.toString(), record.date)
            startActivity(intent)
            finish()
        }
        editDiaryBinding.share.setOnClickListener{
            val intent = Intent(Intent.ACTION_SEND)
            val chooserTitle = "친구와 함께 운동하기"
            val content = "날짜: ${record.date}, 오늘의 운동소모량은 ${record.kcal}kcal로 ${record.foodName}를 먹었습니다! 함께 운동해볼까요?"
            intent.type = "text/plain"

            intent.putExtra(Intent.EXTRA_TEXT, "$content\n\n$imgUrl")
            startActivity(Intent.createChooser(intent, chooserTitle))
        }

        service = retrofit.create(FoodImgAPIService::class.java)

        val apiCallback = object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.isSuccessful) {
                    try {
                        val root: Root? = response.body()
                        imgUrl = root?.items?.get(0)?.imgUrl.toString()

                        Glide.with(this@EditDiaryActivity)
                            .load(imgUrl)
                            .into(editDiaryBinding.imgFood)

                        editDiaryBinding.tvDate.setText(record.date)
                        editDiaryBinding.tvStepCount.setText(record.stepCount)
                        editDiaryBinding.tvKcal.setText(record.kcal)
                        editDiaryBinding.tvFood.setText(record.foodName)
                        editDiaryBinding.tvFoodKcal.setText(record.foodKcal)
                        editDiaryBinding.etContent.setText(record.content)

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
            record.foodName,
            1
        )

        apiCall.enqueue(apiCallback)
    }

    fun modifyFood(foodName: String, date : String) {
        CoroutineScope(Dispatchers.IO).launch {
            diaryRecordDao.updateDiary(foodName, date, editDiaryBinding.etContent.text.toString())
        }
    }

    fun removeFood(foodName: String , date : String) {
        CoroutineScope(Dispatchers.IO).launch {
            diaryRecordDao.deleteDiary(foodName, date)
        }
    }
}