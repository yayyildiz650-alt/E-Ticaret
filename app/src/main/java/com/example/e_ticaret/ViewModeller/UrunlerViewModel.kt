package com.example.e_ticaret.ViewModeller

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.e_ticaret.RetrofitApı.ProductModel

import android.util.Log

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.e_ticaret.RetrofitApı.RetrofitClient
import com.example.e_ticaret.Utils.ProductUtils

// VERİLERİMİZİ VİEWMODELDE ŞİMDİ ÇEKİYORUZU

class UrunlerViewModel : ViewModel() {

    private val _urunler = mutableStateOf<List<ProductModel>>(emptyList())
    val urunler: State<List<ProductModel>> = _urunler

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        urunleriGetir()
    }

    fun urunleriGetir() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.apiService.getProducts()
                
                // Ürünleri akıllı kategorizasyon ile güncelliyoruz
                val categorizedProducts = response.data.map { product ->
                    product.copy(
                        category = ProductUtils.determineCategory(
                            product.title,
                            product.description,
                            product.category
                        )
                    )
                }
                
                _urunler.value = categorizedProducts
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.localizedMessage
                Log.e("API_TEST", "❌ HATA OLUŞTU: ${e.localizedMessage}")
            }
        }
    }
}

