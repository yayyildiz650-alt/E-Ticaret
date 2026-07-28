package com.example.e_ticaret.AiSystem

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Kullanıcının bir ürünle olan etkileşim verilerini saklar.
 * Tereddüt Avcısı (Dwell Time) ve FOMO özelliklerini tetiklemek için kullanılır.
 */
@Entity(tableName = "urun_etkilesimleri")
data class UrunEtkilesimTablosu(
    @PrimaryKey val urunId: Int,
    val ziyaretSayisi: Int = 0,
    val teklifSeviyesi: Int = 0 // 0: Yeni, 1: İlk Teklif Kullanıldı, 2: İkinci Teklif Kullanıldı
)
