package com.example.e_ticaret.AiSystem

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {

    // 1. UYGULAMA İLK AÇILDIĞINDA: 200 ürünlük listeyi veritabanına tek seferde kaydetmek için
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun
            insertAllProducts(products: List<ProductEntity>)

    // 2. DİNAMİK RAG FİLTRESİ: Kullanıcının girdiği kelimeyi başlık, açıklama veya kategoride arar
    @Query("""
        SELECT * FROM products 
        WHERE title LIKE '%' || :keyword || '%' COLLATE NOCASE
        OR description LIKE '%' || :keyword || '%' COLLATE NOCASE
        OR category LIKE '%' || :keyword || '%' COLLATE NOCASE
        LIMIT 10
    """)
    suspend fun searchProductsByKeyword(keyword: String): List<ProductEntity>

    // 3. YEDEK PLAN: Eğer kullanıcının aradığı kelimede ürün bulamazsak,
    // Gemini'ye "Bak bu ürün yok ama mağazadaki rastgele 5 ürünü öner" diyebilmek için
    @Query("SELECT * FROM products ORDER BY RANDOM() LIMIT 5")
    suspend fun getRandomProducts(): List<ProductEntity>

    // Mağazadaki tüm kategorileri benzersiz olarak getirir
    @Query("SELECT DISTINCT category FROM products")
    suspend fun getAllCategories(): List<String>

    // 4. GELİŞMİŞ FİLTRELEME: Kategori, fiyat aralığı ve anahtar kelimeye göre arama
    @Query("""
        SELECT * FROM products 
        WHERE (:category IS NULL OR category = :category COLLATE NOCASE)
        AND (:minPrice IS NULL OR price >= :minPrice)
        AND (:maxPrice IS NULL OR price <= :maxPrice)
        AND (:keyword IS NULL OR title LIKE '%' || :keyword || '%' COLLATE NOCASE OR description LIKE '%' || :keyword || '%' COLLATE NOCASE)
        ORDER BY price ASC
        LIMIT 15
    """)
    suspend fun searchProductsByFilters(
        category: String?,
        minPrice: Double?,
        maxPrice: Double?,
        keyword: String?
    ): List<ProductEntity>

    // Uygulamanın veritabanının dolu olup olmadığını kontrol etmek için (JSON bir daha yazılmasın diye)
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    // --- TEREDDÜT AVCISI (ETKİLEŞİM) SORGULARI ---

    // Ürünün geçmiş etkileşim verilerini (ziyaret sayısı vb.) getirir
    @Query("SELECT * FROM urun_etkilesimleri WHERE urunId = :id")
    suspend fun etkilesimiGetir(id: Int): UrunEtkilesimTablosu?

    // Yeni etkileşim verisini kaydeder veya mevcut olanı günceller
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun etkilesimiKaydet(etkilesim: UrunEtkilesimTablosu)
}