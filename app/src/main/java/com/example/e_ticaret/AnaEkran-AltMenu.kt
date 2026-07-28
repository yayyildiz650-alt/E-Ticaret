package com.example.e_ticaret

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun A101CustomBottomMenu(
    currentRoute: String?,
    onNavigatetoCategori: () -> Unit,
    onNavigatetoSepet: () -> Unit,
    onNavigationAnaSyfa: () -> Unit,
    onNavigatetoFavori: () -> Unit,
    onNavigatetoAi: () -> Unit
) {
    Surface(
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Column {
            // Üst Çizgi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.LightGray)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Anasayfa
                    BottomMenuItem(
                        icon = Icons.Filled.Home,
                        label = "Anasayfa",
                        isSelected = currentRoute == "ana_ekran",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigationAnaSyfa() }
                    )

                    // 2. Kategoriler
                    BottomMenuItem(
                        icon = Icons.Filled.Category,
                        label = "Kategoriler",
                        isSelected = currentRoute == "kategoriler_ekrani" || currentRoute?.startsWith("kategori_urunleri_ekrani") == true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigatetoCategori() }
                    )

                    // 3. Favorilerim
                    BottomMenuItem(
                        icon = Icons.Filled.Favorite,
                        label = "Favorilerim",
                        isSelected = currentRoute == "favoriler_ekrani",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigatetoFavori() }
                    )




                    // 5. Sepetim
                    BottomMenuItem(
                        icon = Icons.Default.ShoppingCart,
                        label = "Sepetim",
                        isSelected = currentRoute == "sepet_ekrani",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigatetoSepet() }
                    )
                    // 4. E-Asistanım
                    BottomMenuItem(
                        icon = Icons.Filled.AutoAwesome,
                        label = "Asistan",
                        isSelected = currentRoute == "ai_chat_ekrani",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigatetoAi() }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) Color(0xFFD32F2F) else Color.Gray

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun A101CustomBottomMenuPreview() {
    A101CustomBottomMenu(
        currentRoute = "ana_ekran",
        onNavigatetoCategori = {},
        onNavigatetoSepet = {},
        onNavigationAnaSyfa = {},
        onNavigatetoFavori = {},
        onNavigatetoAi = {}
    )
}
