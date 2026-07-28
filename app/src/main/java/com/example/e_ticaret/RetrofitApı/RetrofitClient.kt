package com.example.e_ticaret.RetrofitApı

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//===================== RETROFİT KURULUMU VE ADRESE GİDEN YER ///////////
object RetrofitClient {

    // Senin GitHub linkinin "products.json" kısmına kadar olan kök dizini (Sonunda / var)
    private const val BASE_URL = "https://raw.githubusercontent.com/yayyildiz650-alt/e-ticaret-mock-data3/refs/heads/main/"

    val apiService: ApiService by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(ProductResponse::class.java, SafeProductResponseDeserializer())
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

// İkinci bir client sınıfına gerek yok (başka bir site olmadığı taktirde)