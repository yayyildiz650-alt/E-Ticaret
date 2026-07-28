package com.example.e_ticaret.AiSystem

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products") //ürün
data class ProductEntity(
    @PrimaryKey val id: Int,
    val sku: String,
    val title: String,
    val description: String,
    val category: String,
    val price: Double, // Filtreleme (örn: 500 TL altı) yapabilmek için sayısal değer
    val formattedPrice: String, // Gemini'ye ve UI'a doğrudan yollamak için (örn: "₺450,00")
    val imageUrl: String // Ürünün görsel linki
)