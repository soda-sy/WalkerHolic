package com.example.walkerholic.data

import java.io.Serializable

data class FoodItem(
    var foodCode: String,
    var foodName: String,
    var calories: Double,
    var carbohydrates: Double,
    var protein: Double,
    var sugar: Double,
    var sodium: Double
) : Serializable {
    constructor() : this("", "", 0.0, 0.0, 0.0, 0.0, 0.0)
}
