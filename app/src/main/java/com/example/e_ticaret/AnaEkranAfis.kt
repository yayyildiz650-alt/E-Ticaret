package com.example.e_ticaret

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.e_ticaret.Utils.HapticHelper
import androidx.compose.ui.layout.ContentScale


// --- 2. YANA KAYAN ÜRÜN LİSTESİ (LazyRow) ---
@Composable
fun UrunKayanListe(
    urunler: List<UrunItem>,
    urunAdetleri: Map<Int, Int>, // Sepetteki adetler
    onSepeteEkleClick: (UrunItem) -> Unit,
    onAdetGuncelle: (UrunItem, Int) -> Unit, // Adet güncelleme callback'i
    onProductClick: (Int) -> Unit = {}, // YENİ: Tıklama callback'i
    onToggleFavorite: (UrunItem) -> Unit = {} // FAVORİ callback'i
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp), // Ekran kenarlarından boşluk
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Kartlar arası boşluk
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, top = 8.dp)
    ) {
        items(urunler) { urun ->
            UrunKarti(
                urun = urun,
                urunAdedi = urunAdetleri[urun.id] ?: 0,
                onSepeteEkleClick = onSepeteEkleClick,
                onAdetGuncelle = onAdetGuncelle,
                onProductClick = { onProductClick(urun.id) },
                onToggleFavorite = { onToggleFavorite(urun) }
            )
        }
    }
}

// --- 3. TEK BİR ÜRÜN KARTININ TASARIMI ---
@Composable
fun UrunKarti(
    urun: UrunItem,
    urunAdedi: Int,
    onSepeteEkleClick: (UrunItem) -> Unit,
    onAdetGuncelle: (UrunItem, Int) -> Unit,
    onProductClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    showFavorite: Boolean = true
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(280.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onProductClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. ÜRÜN GÖRSELİ ALANI
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(urun.gorselUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = urun.ad,
                        modifier = Modifier.size(100.dp),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Fit,
                        error = rememberVectorPainter(Icons.Filled.ShoppingCart),
                        placeholder = rememberVectorPainter(Icons.Filled.ShoppingCart)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. ÜRÜN BİLGİLERİ
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = urun.ad,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.height(36.dp),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // FİYATLAR (Alt alta dizilerek sığma sorunu çözüldü)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (urun.indirimliMi) {
                            Text(
                                text = urun.eskiFiyat,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = urun.yeniFiyat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 3. BUTON / ADET SEÇİCİ
                if (urunAdedi > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(10.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                HapticHelper.playTick(context)
                                onAdetGuncelle(urun, urunAdedi - 1) 
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        }
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .fillMaxHeight()
                                .background(Color(0xFFD32F2F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = urunAdedi.toString(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { 
                                HapticHelper.playTick(context)
                                onAdetGuncelle(urun, urunAdedi + 1) 
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Add, null, Modifier.size(18.dp), tint = Color(0xFFD32F2F))
                        }
                    }
                } else {
                    Button(
                        onClick = { 
                            HapticHelper.playSuccess(context)
                            onSepeteEkleClick(urun) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ShoppingCart, null, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekle", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // 4. FAVORİ BUTONU (Yuvarlak Köşeli Kare & Çerçeveli)
            if (showFavorite) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    IconButton(
                        onClick = {
                            HapticHelper.playSuccess(context)
                            onToggleFavorite()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (urun.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (urun.isFavorite) Color(0xFFD32F2F) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}