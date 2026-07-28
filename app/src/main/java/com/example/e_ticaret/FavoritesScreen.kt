package com.example.e_ticaret

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_ticaret.ViewModeller.FavoriteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoriteViewModel,
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onSepeteEkleClick: (UrunItem) -> Unit,
    onAdetGuncelle: (UrunItem, Int) -> Unit,
    urunAdetleri: Map<Int, Int>
) {
    val favoriteList by viewModel.favoriteList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorilerim", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (favoriteList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz favori ürününüz yok.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteList) { favorite ->
                    val urunItem = UrunItem(
                        id = favorite.id,
                        ad = favorite.title,
                        eskiFiyat = "",
                        yeniFiyat = favorite.formatfiyat,
                        indirimliMi = false,
                        gorselUrl = favorite.imageUrl,
                        aciklama = "",
                        isFavorite = true
                    )
                    
                    UrunKarti(
                        urun = urunItem,
                        urunAdedi = urunAdetleri[favorite.id] ?: 0,
                        onSepeteEkleClick = onSepeteEkleClick,
                        onAdetGuncelle = onAdetGuncelle,
                        onProductClick = { onProductClick(favorite.id) },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(favorite)
                        }
                    )
                }
            }
        }
    }
}
