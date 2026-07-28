package com.example.e_ticaret.ViewModeller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_ticaret.CategoriAPI.CategoryModel
import com.example.e_ticaret.RetrofitApı.RetrofitClient
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    var kategoriler by mutableStateOf<List<CategoryModel>>(emptyList())
        private set

    // 2. Loading ve Hata Durumları (Profesyonel uygulamalarda mutlaka olur)
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        // ViewModel ilk ayağa kalktığında otomatik olarak kategorileri çekmeye başlar
        KategorilerViewModel()
    }

    fun KategorilerViewModel(){
        viewModelScope.launch {
            isLoading=true
            errorMessage= null

            try {
                val veriler= RetrofitClient.apiService.getCategories()
                kategoriler= veriler.data   // Kategorilere atadık
            }
            catch (e: Exception){

            }

        }
    }


}