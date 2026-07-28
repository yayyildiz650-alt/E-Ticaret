package com.example.e_ticaret.RetrofitApı

import com.example.e_ticaret.CategoriAPI.CategoryResponse
import retrofit2.http.GET
import retrofit2.http.Query


//============ GET İSTEĞİ İLE SİTEYE GİTMKE ==================
interface ApiService {

    // Json dosyamızdaki iiç dosyamızzın adı
    @GET("products.json")
    suspend fun getProducts(): ProductResponse

    // Not: "suspend" kelimesini ekledik çünkü bu işlemi arka planda (Coroutines ile)
    // uygulamanın arayüzünü dondurmadan, sessizce yapacağız.

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //============================ KATEGORİLERİ ALACAĞIMIZ API==================================================================
        // İkinci bir client sınıfına gerek yok (başka bir site olmadığı taktirde)
  @GET("categories.json")
 suspend fun getCategories(): CategoryResponse


    // Sadece Tıklanılan verileri alan fonksiyon
    @GET("products.json")
    suspend fun getProductsByCategory(
        @Query("category") categorySlug: String // Retrofit bunu otomatik olarak ?category=slug haline getirir
    ): ProductResponse

}