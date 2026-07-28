package com.example.e_ticaret.AiSystem

import android.util.Log
import com.example.e_ticaret.RetrofitApı.RetrofitClient
import com.example.e_ticaret.Utils.ProductUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(private val productDao: ProductDao) {

    // 1. ÜRÜN SENKRONİZASYON MEKANİZMASI
    // API'den gelen verileri yerel veritabanı ile eşitler. Yeni ürünleri ekler, mevcutları günceller.
    suspend fun syncProductsWithApi() {
        withContext(Dispatchers.IO) {
            try {
                val localCount = productDao.getProductCount()
                Log.d("AiRepository", "Senkronizasyon başladı. Yerel ürün sayısı: $localCount")

                // API'den güncel verileri çekiyoruz
                val response = RetrofitClient.apiService.getProducts()
                val apiProducts = response.data
                val apiCount = apiProducts.size
                Log.d("AiRepository", "API'den $apiCount ürün çekildi.")

                // Eğer yerel veritabanı API'den eksikse veya güncellenmesi gerekiyorsa
                if (apiCount != localCount) {
                    Log.d("AiRepository", "Veri uyuşmazlığı tespit edildi. Güncelleniyor...")
                    
                    val entities = apiProducts.map { product ->
                        val finalCategory = ProductUtils.determineCategory(
                            product.title ?: "", 
                            product.description, 
                            product.category
                        )

                        ProductEntity(
                            id = product.id,
                            sku = "SKU-${product.id}",
                            title = product.title ?: "",
                            description = product.description ?: "",
                            category = finalCategory,
                            price = ProductUtils.cleanPrice(product.price?.formatted),
                            formattedPrice = product.price?.formatted ?: "",
                            imageUrl = product.thumbnail ?: "" // Görsel linkini kaydediyoruz
                        )
                    }
                    productDao.insertAllProducts(entities)
                    val newLocalCount = productDao.getProductCount()
                    Log.d("AiRepository", "Senkronizasyon tamamlandı. Yeni yerel ürün sayısı: $newLocalCount")
                } else {
                    Log.d("AiRepository", "Veriler zaten güncel.")
                }
            } catch (e: Exception) {
                Log.e("AiRepository", "Senkronizasyon HATA: ${e.message}", e)
            }
        }
    }


    // 4. KATEGORİLERİ GETİRME
    suspend fun getAllCategories(): List<String> {
        return withContext(Dispatchers.IO) {
            productDao.getAllCategories()
        }
    }

    // 5. AI İÇİN GELİŞMİŞ SORGULAMA
    suspend fun getFilteredProductsForAi(
        category: String?,
        minPrice: Double?,
        maxPrice: Double?,
        keyword: String?
    ): List<ProductEntity> {
        return withContext(Dispatchers.IO) {
            val products = productDao.searchProductsByFilters(category, minPrice, maxPrice, keyword)

            Log.d("AiRepository", "Sorgu sonucu: ${products.size} ürün bulundu. (Kategori: $category, Fiyat: $minPrice-$maxPrice, Kelime: $keyword)")
            products
        }
    }

    // --- TEREDDÜT AVCISI (ETKİLEŞİM) METODLARI ---

    suspend fun urunEtkilesiminiGetir(urunId: Int): UrunEtkilesimTablosu? {
        return withContext(Dispatchers.IO) {
            productDao.etkilesimiGetir(urunId)
        }
    }

    suspend fun urunEtkilesiminiKaydet(etkilesim: UrunEtkilesimTablosu) {
        withContext(Dispatchers.IO) {
            productDao.etkilesimiKaydet(etkilesim)
        }
    }
}
