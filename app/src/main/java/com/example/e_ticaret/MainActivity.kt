package com.example.e_ticaret

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.e_ticaret.ViewModeller.Sepet_ViewModel
import com.example.e_ticaret.ViewModeller.UrunlerViewModel
import com.example.e_ticaret.RetrofitApı.ProductModel
import com.example.e_ticaret.RetrofitApı.PriceInfo
import com.example.e_ticaret.ui.theme.ETicaretTheme


import com.example.e_ticaret.ViewModeller.FavoriteViewModel
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize



//TOKEN SORGU KODU  DebugAppCheckProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase ve App Check'i başlat
        Firebase.initialize(context = this)
        initFirebaseAppCheck()

        setContent {
            ETicaretTheme(darkTheme = false, dynamicColor = false) {
                AppNavigation()
            }
        }
    }

    private fun initFirebaseAppCheck() {
        val firebaseAppCheck = Firebase.appCheck

        // DİKKAT: Telefonda USB ile test yaparken BuildConfig.DEBUG true döner.
        // Ancak telefonun kendi debug token'ını konsola eklemediğimiz için Play Integrity veya
        // standart debug sağlayıcı hata verebilir.
        // Geliştirme aşamasında her iki cihazın da (Emülatör ve Telefon) takılmadan
        // geçmesi için Debug Provider'ı zorluyoruz:
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}

// ============================================================================
// 1. ANA EKRAN İSKELETİ (Tüm parçaların birleştiği yer)
// ============================================================================

@Composable
fun A101MainScreen(
    viewModel: UrunlerViewModel = viewModel(),
    sepetViewModel: Sepet_ViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel,
    onProductClick: (Int) -> Unit = {} // YENİ: Tıklama callback'i
) {
    val apiUrunleri by viewModel.urunler
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val sepetListesi by sepetViewModel.sepetListesi.collectAsState()
    val favorites by favoriteViewModel.favoriteList.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Sepetteki ürünlerin adetlerini map olarak tutalım: Map<ID, Adet>
    val urunAdetleri = remember(sepetListesi) {
        sepetListesi.associate { it.id to it.quantity }
    }

    val favoriteIds = remember(favorites) {
        favorites.map { it.id }.toSet()
    }

    A101MainScreenContent(
        apiUrunleri = apiUrunleri,
        urunAdetleri = urunAdetleri,
        favoriteIds = favoriteIds,
        isLoading = isLoading,
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState,
        onRetry = { viewModel.urunleriGetir() },
        onSepeteEkleClick = { urun ->
            scope.launch {
                sepetViewModel.urunItemSepeteEkle(urun)
                snackbarHostState.showSnackbar(
                    message = "Ürün başarıyla sepete eklendi",
                    duration = SnackbarDuration.Short
                )
            }
        },
        onAdetGuncelle = { urun, yeniAdet ->
            scope.launch { sepetViewModel.urunItemAdetGuncelle(urun, yeniAdet) }
        },
        onProductClick = onProductClick,
        onToggleFavorite = { urun ->
            favoriteViewModel.toggleFavorite(
                FavoritesModel(
                    id = urun.id,
                    title = urun.ad,
                    formatfiyat = urun.yeniFiyat,
                    imageUrl = urun.gorselUrl
                )
            )
        }
    )
}

@Composable
fun A101MainScreenContent(
    apiUrunleri: List<ProductModel>,
    urunAdetleri: Map<Int, Int>,
    favoriteIds: Set<Int>,
    isLoading: Boolean,
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onSepeteEkleClick: (UrunItem) -> Unit,
    onAdetGuncelle: (UrunItem, Int) -> Unit,
    onProductClick: (Int) -> Unit = {},
    onToggleFavorite: (UrunItem) -> Unit = {}
) {
    val apiVerileriDonusmus = apiUrunleri.map { product ->
        UrunItem(

            id = product.id,
            ad = product.title ?: "İsimsiz Ürün",
            eskiFiyat = product.price?.compareAtFormatted ?: "",
            yeniFiyat = product.price?.formatted ?: "",
            indirimliMi = product.price?.compareAtFormatted != null,
            gorselUrl = product.thumbnail ?: "",
            aciklama = product.description ?: "",
            isFavorite = favoriteIds.contains(product.id)
        )
    }

    val cokSatanlar = apiVerileriDonusmus.take(10)
    val flashUrunler = if (apiVerileriDonusmus.size > 10) apiVerileriDonusmus.drop(10)
        .take(10) else emptyList()
    val tukeniyorUrunler = if (apiVerileriDonusmus.size > 20) apiVerileriDonusmus.drop(20)
        .take(10) else emptyList()
    val elitUrunler =
        if (apiVerileriDonusmus.size > 30) apiVerileriDonusmus.drop(30) else emptyList()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF000000),
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
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                SearchBarComponent()
            }

            if (isLoading && apiUrunleri.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD32F2F))
                    }
                }
            } else if (errorMessage != null && apiUrunleri.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Bir hata oluştu: $errorMessage", color = Color.Red)
                        Button(onClick = onRetry) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (cokSatanlar.isNotEmpty()) {
                    item {
                        CategoryTitle("Çok Satanlar", Color(0xFFD32F2F))
                        UrunKayanListe(
                            urunler = cokSatanlar,
                            urunAdetleri = urunAdetleri,
                            onSepeteEkleClick = onSepeteEkleClick,
                            onAdetGuncelle = onAdetGuncelle,
                            onProductClick = onProductClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }

                if (flashUrunler.isNotEmpty()) {
                    item {
                        CategoryTitle("Flash Ürünler", Color(0xFFFF5722))
                        UrunKayanListe(
                            urunler = flashUrunler,
                            urunAdetleri = urunAdetleri,
                            onSepeteEkleClick = onSepeteEkleClick,
                            onAdetGuncelle = onAdetGuncelle,
                            onProductClick = onProductClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }

                if (tukeniyorUrunler.isNotEmpty()) {
                    item {
                        CategoryTitle("Tükeniyor", Color(0xFFD32F2F))
                        UrunKayanListe(
                            urunler = tukeniyorUrunler,
                            urunAdetleri = urunAdetleri,
                            onSepeteEkleClick = onSepeteEkleClick,
                            onAdetGuncelle = onAdetGuncelle,
                            onProductClick = onProductClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }

                if (elitUrunler.isNotEmpty()) {
                    item {
                        CategoryTitle("Elit Ürünler", Color(0xFFD32F2F))
                        UrunKayanListe(
                            urunler = elitUrunler,
                            urunAdetleri = urunAdetleri,
                            onSepeteEkleClick = onSepeteEkleClick,
                            onAdetGuncelle = onAdetGuncelle,
                            onProductClick = onProductClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTitle(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

// ============================================================================
// 5. ÖNİZLEME (PREVIEW)
// ============================================================================
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun A101MainScreenPreview() {
    val sampleProducts = List(40) { index ->
        ProductModel(
            id = index,
            title = if (index % 2 == 0) "Süt $index" else "Ekmek $index",
            thumbnail = "",
            category = "Gıda",
            badges = emptyList(),
            images = emptyList(),
            price = PriceInfo(
                formatted = "₺${20 + index},00",
                compareAtFormatted = if (index % 3 == 0) "₺${30 + index},00" else null,
                discountPercentage = if (index % 3 == 0) 10.0 else null
            ),
            description = "Bu ürünün açıklaması $index"
        )
    }

    ETicaretTheme {
        A101MainScreenContent(
            apiUrunleri = sampleProducts,
            urunAdetleri = emptyMap(),
            favoriteIds = emptySet(),
            isLoading = false,
            errorMessage = null,
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onSepeteEkleClick = {},
            onAdetGuncelle = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryTitlePreview() {
    ETicaretTheme {
        CategoryTitle(title = "Çok Satanlar", color = Color(0xFFD32F2F))
    }
}

@Preview(showBackground = true)
@Composable
fun BottomMenuItemPreview() {
    MaterialTheme {
        BottomMenuItem(
            icon = Icons.Filled.CheckCircle,
            label = "Sepetim",
            isSelected = true
        )
    }
}