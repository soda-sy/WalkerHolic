package com.example.walkerholic.manager

import android.content.res.AssetManager
import android.util.Log
import com.example.walkerholic.data.FoodItem
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import java.io.IOException

class ExcelManager(private val assets: AssetManager) {

    fun readExcel(searchFoodName: String): List<FoodItem> {
        val foodItemList = mutableListOf<FoodItem>()

        var inputStream = assets.open("FoodNutritionsDB_API.xlsx")
        var myWorkBook = WorkbookFactory.create(inputStream)

        try {
            val sheet = myWorkBook.getSheetAt(0)
            val rowIterator = sheet.iterator()

            while (rowIterator.hasNext()) {
                val myRow = rowIterator.next() as XSSFRow
                val foodItem = processRow(myRow, searchFoodName)
                if (foodItem.foodName == searchFoodName) {
                    foodItemList.add(foodItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return foodItemList
    }

    fun readExcelByKcal(kcal: String): List<FoodItem> {
        val foodItemList = mutableListOf<FoodItem>()

        var inputStream = assets.open("FoodNutritionsDB_API.xlsx")
        var myWorkBook = WorkbookFactory.create(inputStream)

        try {
            val sheet = myWorkBook.getSheetAt(0)
            val rowIterator = sheet.iterator()

            while (rowIterator.hasNext()) {
                val myRow = rowIterator.next() as XSSFRow
                val foodItem = processRow(myRow, kcal)
                if (foodItem.calories > 0 && foodItem.calories >= kcal.toDouble() - 3
                    && foodItem.calories <= kcal.toDouble()) {
                    foodItemList.add(foodItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return foodItemList
    }

    private fun processRow(myRow: XSSFRow, searchFoodName: String): FoodItem {
        var foodItem = FoodItem() // FoodItem 객체 생성

        val cellIterator = myRow.iterator()

        var colNo = 0

        while (cellIterator.hasNext()) {
            val myCell = cellIterator.next() as XSSFCell

            // 모든 열 읽기 (number 열을 생략)
            when (colNo) {
                0 -> foodItem.foodCode = myCell.toString()
                1 -> foodItem.foodName = myCell.toString()
                2 -> foodItem.calories = myCell.toString().toDoubleOrNull() ?: 0.0
                3 -> foodItem.carbohydrates = myCell.toString().toDoubleOrNull() ?: 0.0
                4 -> foodItem.protein = myCell.toString().toDoubleOrNull() ?: 0.0
                5 -> foodItem.sugar = myCell.toString().toDoubleOrNull() ?: 0.0
                6 -> foodItem.sodium = myCell.toString().toDoubleOrNull() ?: 0.0
            }

            colNo++
        }
        return foodItem
    }
}
