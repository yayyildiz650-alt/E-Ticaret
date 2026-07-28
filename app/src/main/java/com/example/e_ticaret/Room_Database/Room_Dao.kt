package com.example.e_ticaret

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface SepetDao {

    @Query("SELECT * FROM sepet_tablosu")
    fun sepetiGetir(): Flow<List<SepetEntity>>

    @Query("SELECT * FROM sepet_tablosu WHERE id = :id")
    suspend fun urunGetir(id: Int): SepetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sepeteEkle(urun: SepetEntity)

    @Transaction
    suspend fun sepeteKesinEkle(id: Int, title: String, price: Double, originalPrice: Double, imageUrl: String, forceUpdatePrice: Boolean) {
        val mevcut = urunGetir(id)
        if (mevcut != null) {
            val yeniAdet = mevcut.quantity + 1
            val yeniBirimFiyat = if (forceUpdatePrice) {
                // KAMPANYA MÜHRÜ: Sadece eklenen YENİ ürün indirimli fiyattan hesaplanır
                ((mevcut.price * mevcut.quantity) + price) / yeniAdet
            } else {
                ((mevcut.price * mevcut.quantity) + originalPrice) / yeniAdet
            }
            sepeteEkle(mevcut.copy(price = yeniBirimFiyat, originalPrice = originalPrice, quantity = yeniAdet))
        } else {
            sepeteEkle(SepetEntity(id, title, price, originalPrice, 1, imageUrl))
        }
    }

    @Update
    suspend fun adetGuncelle(urun: SepetEntity)

    @Delete
    suspend fun sepettenSil(urun: SepetEntity)
}