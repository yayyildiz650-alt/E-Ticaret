package com.example.e_ticaret

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    // Favorilere ekle (Aynı ürün eklenirse üzerine yazar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(product: FavoritesModel)

    // Favorilerden çıkar
    @Delete
    suspend fun removeFavorite(product:  FavoritesModel)

    // Tüm favorileri getir (Flow sayesinde veritabanı değiştikçe UI anında güncellenir)
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoritesModel>>

    // Belirli bir ürünün favorilerde olup olmadığını kontrol et (Kalp ikonunu dolu/boş yapmak için)
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :productId)")
    fun isFavorite(productId: Int): Flow<Boolean>

}
