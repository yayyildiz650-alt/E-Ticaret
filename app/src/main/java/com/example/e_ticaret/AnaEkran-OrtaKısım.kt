package com.example.e_ticaret

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// KOD TEKRARINI ÖNLEYEN ORTAK KALIP FONKSİYONU
@Composable
fun CampaignCard(
    bgColor: Color, // Kartın arka plan rengi
    borderColor: Color = Color.Transparent, // Kartın çerçeve rengi
    modifier: Modifier = Modifier, // Dışarıdan weight/tıklama verebilmek için
    content: @Composable () -> Unit // Kartın içine konulacak yazılar
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}




// --- 3. FİLTRE SEKMELERİ ("Çok Satanlar", "Aldın Aldın" vb.) ---
@Composable
fun Urunliste(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Çok Satanlar", "Flash Ürünler", "Tükeniyor", "Elit Ürünler")
    val tabColors = listOf(
        Color(0xFFD32F2F), // Profesyonel Kırmızı
        Color(0xFFFF5722), // Turuncu (Flaşh için)
        Color(0xFFD32F2F), // Pembe (Tükeniyor için)
        Color(0xFF673AB7)  // Mor (Elit için)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tabs.size) { index ->
            val isSelected = selectedTabIndex == index

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) tabColors[index] else Color(0xFFE0E0E0))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabs[index],
                    color = if (isSelected) {
                        if (index == 1) Color.Black else Color.White
                    } else Color.DarkGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
