package com.example.walkerholic

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.walkerholic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    val mainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        val pref: SharedPreferences = getSharedPreferences("my_info", 0)
        val editor: SharedPreferences.Editor = pref.edit()
        var myWeight = pref.getString("myWeight", null).toString()
        if (!myWeight.equals(null)) {
            mainBinding.etWeight.setText(myWeight)
        }

        mainBinding.btnSave.setOnClickListener {
            editor.putString("myWeight", mainBinding.etWeight.text.toString()).commit()
            Toast.makeText(this, "몸무게 저장 완료!", Toast.LENGTH_SHORT).show()
        }

        mainBinding.btnExercise.setOnClickListener {
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }
        mainBinding.btnKcalSearch.setOnClickListener {
            val intent = Intent(this, SearchKcalActivity::class.java)
            startActivity(intent)
        }
        mainBinding.btnDiary.setOnClickListener {
            val intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }
    }
}
