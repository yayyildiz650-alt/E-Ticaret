package com.example.e_ticaret.ViewModeller

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_ticaret.FavoritesDao
import com.example.e_ticaret.FavoritesModel
import com.example.e_ticaret.RetrofitApı.ProductModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(private val favoriteDao: FavoritesDao) : ViewModel() {

    // Room Flow'u StateFlow'a çevirerek UI'ın otomatik güncellenmesini sağlıyoruz
    val favoriteList: StateFlow<List<FavoritesModel>> = favoriteDao.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Kalp ikonuna tıklandığında çalışacak ana fonksiyon
    fun toggleFavorite(product: FavoritesModel) {
        viewModelScope.launch {
            val isFavorite = favoriteList.value.any { it.id == product.id }
            if (isFavorite) {
                favoriteDao.removeFavorite(product)
            } else {
                favoriteDao.addFavorite(product)
            }
        }
    }

    // ProductModel üzerinden favori ekleme/çıkarma (Ana ekrandan çağırmak için)
    fun toggleFavoriteWithProduct(product: ProductModel) {
        val favorite = FavoritesModel(
            id = product.id,
            title = product.title ?: "",
            formatfiyat = product.price?.formatted ?: "",
            imageUrl = product.thumbnail ?: ""
        )
        toggleFavorite(favorite)
    }

    // Belirli bir ID'nin favori olup olmadığını kontrol eden yardımcı fonksiyon
    fun isFavorite(productId: Int): Boolean {
        return favoriteList.value.any { it.id == productId }
    }
}
