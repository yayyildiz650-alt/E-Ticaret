package com.example.e_ticaret

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


// Veri tabanı kurulumu
@Database(entities = [SepetEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Veri tabanı, işlemleri yapabilmek için DAO sınıfımızı tanımalı
    abstract fun sepetDao(): SepetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "e_ticaret_veritabani"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

}