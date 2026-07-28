package com.example.e_ticaret

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Listeyi doğrudan dönmek için import edildi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage // Resim indirme kütüphanesi eklendi
import coil.request.ImageRequest
import com.example.e_ticaret.CategoriAPI.CategoryModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<CategoryModel>, // Veya projendeki adıyla CategoryyModel
    onBackClick: () -> Unit,
    onCategoryClick: (String) -> Unit // YENİ: Kategoriye tıklanınca slug'ı fırlatacak
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tüm Kategoriler",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Geri Dön")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 3'lü dizilim harika karar
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // YENİ: items(categories.size) yerine doğrudan listeyi verdik
            items(categories) { category ->

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)) // Tıklama efekti dışarı taşmasın diye
                        .clickable { onCategoryClick(category.slug) } // YENİ: Tıklama özelliği
                        .padding(4.dp) // Tıklama efektinin etrafında hafif boşluk
                ) {

                    // YENİ: İkon yerine Coil ile internetten gelen gerçek resim
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(category.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = category.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentScale = ContentScale.Crop // Resim yamulmasın, kutuya tam otursun
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = category.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}