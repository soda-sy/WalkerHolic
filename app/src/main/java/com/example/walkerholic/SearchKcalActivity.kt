package com.example.walkerholic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.walkerholic.databinding.ActivitySearchKcalBinding
import com.example.walkerholic.manager.ExcelManager
import com.example.walkerholic.ui.FoodAdapter

class SearchKcalActivity : AppCompatActivity() {
    private val TAG = "SearchKcalActivity"

    val seachBinding by lazy {
        ActivitySearchKcalBinding.inflate(layoutInflater)
    }

    val adapter by lazy {
        FoodAdapter()
    }

    lateinit var excelManager: ExcelManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(seachBinding.root)
        excelManager = ExcelManager(assets)


        seachBinding.rvFood.adapter = adapter
        seachBinding.rvFood.layoutManager = LinearLayoutManager(this)

        adapter.setOnItemClickListener(object : FoodAdapter.OnItemClickListner {
            override fun onItemClick(view: View, position: Int) {
                val selectedFood = adapter.foods?.get(position)
                val intent = Intent(applicationContext, SearchKcalDetailActivity::class.java)
                intent.putExtra("food", selectedFood)
                startActivity(intent)
            }
        })

        seachBinding.btnSearch.setOnClickListener {
            val keyword = seachBinding.etSearchFood.text.toString()

            var foodItemList = excelManager.readExcel(keyword)
            Log.d("ddd", "${foodItemList.size}")
            adapter.foods = foodItemList
            adapter.notifyDataSetChanged()
        }
    }
}