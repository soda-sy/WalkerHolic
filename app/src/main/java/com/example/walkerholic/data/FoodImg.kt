package com.example.walkerholic.data

import com.google.gson.annotations.SerializedName

data class Root(
    val items: List<Item>,
)

data class Item(
    @SerializedName("title")
    val foodName: String,
    @SerializedName("link")
    val imgUrl: String,
    val thumbnail: String,
)
