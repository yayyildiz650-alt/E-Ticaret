package com.example.e_ticaret.RetrofitApı

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Bu sınıf, JSON dosyasındaki bozuk verileri (örneğin nesne beklenen yerde string gelmesi)
 * tespit eder ve bu bozuk elemanları atlayarak uygulamanın çökmesini engeller.
 */
class SafeProductResponseDeserializer : JsonDeserializer<ProductResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ProductResponse {
        val validProducts = mutableListOf<ProductModel>()
        
        try {
            val jsonObject = json?.asJsonObject
            val dataArray = jsonObject?.getAsJsonArray("data")

            dataArray?.forEach { element ->
                // Eğer eleman bir JSON nesnesi ({...}) ise işlemeye çalış
                if (element.isJsonObject) {
                    try {
                        val product: ProductModel? = context?.deserialize(element, ProductModel::class.java)
                        // Akıllı Filtreleme: İsmi veya fiyatı olmayan ürünü dükkana alma
                        if (product != null && !product.title.isNullOrBlank() && product.price != null) {
                            validProducts.add(product)
                        }
                    } catch (e: Exception) {
                        // Ürün bazlı bir parse hatası olursa o ürünü atla
                    }
                } else {
                    // Eleman bir nesne değilse (String vb.) sessizce atla (Hatanın asıl çözümü burası)
                }
            }
        } catch (e: Exception) {
            // Genel bir hata durumunda boş liste döndür
        }

        return ProductResponse(validProducts)
    }
}
