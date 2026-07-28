package com.example.e_ticaret

// Gerekli Compose ve Android kütüphanelerinin projeye dahil edilmesi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_ticaret.RetrofitApı.ProductModel

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar henüz deneysel bir bileşen olduğu için uyarıyı susturur
@Composable
fun KategoriUrunleriScreen(
    sayfaBasligi: String,
    urunListesi: List<ProductModel>,
    urunAdetleri: Map<Int, Int>, // YENİ
    onGeriDonClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onSepeteEkleClick: (ProductModel) -> Unit,
    onAdetGuncelle: (ProductModel, Int) -> Unit,
    onProductClick: (Int) -> Unit = {}
) {
    // ...
    // SCAFFOLD: Sayfanın iskeletidir. Bize üst bar (TopAppBar) ve ana içerik için hazır bir şablon sunar.
    Scaffold(
        topBar = {
            // EKRANIN ÜST KISMI (TopAppBar)
            TopAppBar(
                title = {
                    // Başlık metnini kalın (Bold) ve 18 punto olarak ayarlıyoruz
                    Text(text = sayfaBasligi, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    // Sol üst köşedeki tıklanabilir "Geri Dön" ok butonu
                    IconButton(onClick = onGeriDonClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Dön")
                    }
                },
                // Üst barın arka plan rengini beyaz yapıyoruz
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        // Tost mesajı ayarları
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF000000)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFFFEB3B), // Yeşil onay ikonu
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = data.visuals.message,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // paddingValues: Üst barın altında kalan güvenli bölgeyi temsil eder.
        // İçeriğimizin üst barın altına saklanmasını (taşmasını) engeller.

        // 1. DURUM: İNTERNETTEN VERİ BEKLENİRKEN (Liste boşsa)
        if (urunListesi.isEmpty()) {
            // Box: İçindeki elemanları hizalamak için kullandığımız kapsayıcı kutu
            Box(
                modifier = Modifier
                    .fillMaxSize() // Kutuyu tüm ekrana yay
                    .padding(paddingValues), // Üst barın sınırlarına saygı duy (altında kal)
                contentAlignment = Alignment.Center // İçindeki her şeyi tam ortaya (merkeze) hizala
            ) {
                // Kırmızı temalı yükleniyor çarkı
                CircularProgressIndicator(color = Color(0xFFD32F2F))
            }
        }
        // 2. DURUM: VERİLER GELDİĞİNDE (Liste dolduğunda)
        else {
            // LazyVerticalGrid: Aşağı doğru kaydırılabilen, çok yüksek performanslı ızgara listesi
            // Ekranda sadece o an görünen kartları çizer, görünmeyenleri hafızada tutmaz.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Sayfayı dikeyde tam olarak 2 eşit sütuna böl (Yan yana 2 kart)
                modifier = Modifier
                    .fillMaxSize() // Tüm boşlukları doldur
                    .padding(paddingValues) // Üst barın altında kal
                    .background(Color(0xFFF2F2F2)), // Arka planı açık gri yap ki beyaz kartlar öne çıksın
                contentPadding = PaddingValues(12.dp), // Listenin dört bir yanından (dıştan) 12 birim boşluk bırak
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Yan yana duran 2 kartın arasına 8 birim yatay boşluk koy
                verticalArrangement = Arrangement.spacedBy(12.dp) // Alt alta duran kartların arasına 12 birim dikey boşluk koy
            ) {
                // items: Kotlin'deki "for döngüsü" gibi çalışır. Gelen listedeki her bir ürünü sırayla alır.
                items(urunListesi) { urun ->
                    // Döngüdeki her bir 'urun' bilgisini bu karta paslıyoruz ve ekrana çizdiriyoruz.
                    val urunItem = UrunItem(
                        id = urun.id,
                        ad = urun.title ?: "İsimsiz Ürün",
                        eskiFiyat = urun.price?.compareAtFormatted ?: "",
                        yeniFiyat = urun.price?.formatted ?: "",
                        indirimliMi = urun.price?.compareAtFormatted != null,
                        gorselUrl = urun.thumbnail ?: "",
                        aciklama = urun.description ?: ""
                    )
                    // TIKLAMA YAPILDIĞI ZAMAN İlk TIKLAMA YAKALANIR- .
                    // VERİLER NAVİGATİONA AKTARILIR (Ürün bilgileri)
                    UrunKarti(
                        urun = urunItem,
                        urunAdedi = urunAdetleri[urun.id] ?: 0,
                        onSepeteEkleClick = { onSepeteEkleClick(urun) },
                        onAdetGuncelle = { _, yeniAdet -> onAdetGuncelle(urun, yeniAdet) },
                        onProductClick = { onProductClick(urun.id) },
                        showFavorite = false
                    )
                }
            }
        }
    }
}