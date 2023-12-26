package com.example.walkerholic.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "diary_table")
data class DiaryRecord(
    @PrimaryKey(autoGenerate = true)
    val _id: Int,

    val img : String,
    var foodName: String,
    var date : String,
    var stepCount : String?,
    var kcal : String?,
    var foodKcal : String,
    var content : String?) : Serializable
{
    override fun toString(): String {
        return "$_id - $foodName "
    }
}
