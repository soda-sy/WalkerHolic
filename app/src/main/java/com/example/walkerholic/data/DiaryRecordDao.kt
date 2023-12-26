package com.example.walkerholic.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryRecordDao {
    @Insert
    suspend fun insertDiary(vararg diaryRecord:DiaryRecord)

    @Query("UPDATE diary_table SET content = :content WHERE foodName = :foodName And date = :date")
    suspend fun updateDiary(foodName : String, date : String, content : String)

    @Query("DELETE FROM diary_table WHERE foodName = :foodName And date = :date")
    suspend fun deleteDiary(foodName : String, date : String)
//
    @Query("SELECT * FROM diary_table")
    fun getAllRecords(): Flow<List<DiaryRecord>>

    @Query("SELECT * FROM diary_table WHERE foodName = :foodName")
    fun getRecordByName(foodName : String) : Flow<List<DiaryRecord>>
}

