package com.example.e_ticaret

// --- 1. ÜRÜN VERİ MODELİ ---
data class UrunItem(
    val id: Int,
    val ad: String,
    val eskiFiyat: String,
    val yeniFiyat: String,
    val indirimliMi: Boolean,
    val gorselUrl: String = "",
    val aciklama: String = "",
    val isFavorite: Boolean = false
)
