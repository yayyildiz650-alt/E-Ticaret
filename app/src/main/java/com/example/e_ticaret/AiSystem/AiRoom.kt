package com.example.e_ticaret.AiSystem

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProductEntity::class, UrunEtkilesimTablosu::class], version = 5, exportSchema = false)
abstract class AiDatabase : RoomDatabase() {

    // ViewModel'in veritabanına ulaşmak için kullanacağı kapı
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AiDatabase? = null

        fun getDatabase(context: Context): AiDatabase {
            // Eğer INSTANCE boş değilse onu döndür, boşsa kilitli bir blok içinde oluştur
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AiDatabase::class.java,
                    "ai_product_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
