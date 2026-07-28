package com.example.e_ticaret

import androidx.room.Entity
import androidx.room.PrimaryKey

// Bu sınıf veri tabanında "sepet_tablosu" adında bir tabloya dönüşecek
@Entity(tableName = "sepet_tablosu")
data class SepetEntity(
    @PrimaryKey val id: Int, 
    val title: String,
    val price: Double, // Bu artık "Ortalama Birim Fiyat" olarak kullanılacak
    val originalPrice: Double, // Ürünün indirimsiz ham fiyatı
    val quantity: Int, 
    val imageUrl: String
)