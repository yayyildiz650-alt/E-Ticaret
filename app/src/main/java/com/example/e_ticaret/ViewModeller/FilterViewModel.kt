package com.example.e_ticaret.ViewModeller

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_ticaret.RetrofitApı.RetrofitClient
import com.example.e_ticaret.RetrofitApı.ProductModel
import com.example.e_ticaret.Utils.ProductUtils
import kotlinx.coroutines.launch

class FilterViewModel : ViewModel() {

    // Ekrana basılacak ürünlerin listesi
    // ÖNEMLİ: ProductModel (kendi modelimiz) kullanıyoruz, Google Analytics Product değil!
    var urunListesi = mutableStateListOf<ProductModel>()
        private set

    fun filterurunlerigetir(categorySlug: String) {
        viewModelScope.launch {
            try {
                // 1. GitHub'dan TÜM ürünleri (products.json) indiriyoruz
                val response = RetrofitClient.apiService.getProducts()
                val gelenTumUrunler = response.data

                // 2. Akıllı kategorizasyon uyguluyoruz ve süzüyoruz
                val sadecO_KategoriyeAitUrunler = gelenTumUrunler.map { urun ->
                    urun.copy(
                        category = ProductUtils.determineCategory(
                            urun.title,
                            urun.description,
                            urun.category
                        )
                    )
                }.filter { urun ->
                    // Kategori slug ile eşleşme kontrolü (Küçük harf duyarsız)
                    urun.category.equals(categorySlug, ignoreCase = true)
                }

                // 3. Ekranda gösterilecek listeye, ayıkladığımız bu temiz listeyi veriyoruz
                urunListesi.clear()
                urunListesi.addAll(sadecO_KategoriyeAitUrunler)

            } catch (e: Exception) {
                // Hata durumu (İnternet yok vs.)
                println("Hata oluştu: ${e.message}")
            }
        }
    }
    fun getProductById(productId: Int): ProductModel? {
        // 1. Önce kategorisi filtrelenmiş listede ara
        val filteredProduct = urunListesi.find { it.id == productId }
        if (filteredProduct != null) return filteredProduct

        // 2. Eğer filtrelenmiş listede yoksa (Ana sayfadan tıklandıysa), tüm ürünleri tekrar kontrol et
        // Not: Burada daha sağlam bir yapı için genel bir veri havuzu kullanılabilir.
        return null
    }

    // YENİ: Tek bir ürünü ID ile çekmek için fonksiyon (Eğer liste boşsa)
    fun urunDetayiniGetir(productId: Int, onResult: (ProductModel?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getProducts()
                val urun = response.data.find { it.id == productId }
                onResult(urun)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
