package com.example.walkerholic.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DiaryRecord :: class], version = 1)
abstract class DiaryRecordDatabase : RoomDatabase() {
    abstract fun diaryRecordDao() : DiaryRecordDao

    companion object {
        private var INSTANCE : DiaryRecordDatabase? = null

        fun getDatabase(context: Context) : DiaryRecordDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext, DiaryRecordDatabase::class.java, "diary_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}