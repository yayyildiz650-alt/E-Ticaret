package com.example.e_ticaret

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.e_ticaret.RetrofitApı.ProductModel
import com.example.e_ticaret.Utils.HapticHelper
import com.example.e_ticaret.Utils.ProductUtils
import com.example.e_ticaret.ViewModeller.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun UrunDetayScreen(
    productId: Int,
    navController: NavController,
    favoriteViewModel: FavoriteViewModel,
    etkilesimViewModel: UrunEtkilesimViewModel,
    sepetViewModel: Sepet_ViewModel,
    viewModel: FilterViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var urun by remember(productId) { mutableStateOf<ProductModel?>(null) }
    var isLoading by remember(productId) { mutableStateOf(true) }

    val sepetListesi by sepetViewModel.sepetListesi.collectAsState()
    val favorites by favoriteViewModel.favoriteList.collectAsState()

    val urunAdedi = remember(sepetListesi, productId) {
        sepetListesi.find { it.id == productId }?.quantity ?: 0
    }

    val isFavorite = remember(favorites, productId) {
        favorites.any { it.id == productId }
    }

    DisposableEffect(productId) {
        onDispose {
            etkilesimViewModel.teklifiKapatInternal()
        }
    }

    LaunchedEffect(productId) {
        isLoading = true
        viewModel.urunDetayiniGetir(productId) { fetchedProduct ->
            urun = fetchedProduct
            isLoading = false
            fetchedProduct?.let { product ->
                val temizFiyat = ProductUtils.cleanPrice(product.price?.formatted)
                etkilesimViewModel.etkilesimiKaydetVeKontrolEt(productId, temizFiyat, urunAdedi > 0)
            }
        }
    }

    UrunDetayContent(
        urun = urun,
        isLoading = isLoading,
        urunAdedi = urunAdedi,
        isFavorite = isFavorite,
        etkilesimViewModel = etkilesimViewModel,
        sepetViewModel = sepetViewModel,
        scope = scope,
        onBackClick = { navController.popBackStack() },
        onToggleFavorite = { product -> favoriteViewModel.toggleFavoriteWithProduct(product) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrunDetayContent(
    urun: ProductModel?,
    isLoading: Boolean,
    urunAdedi: Int,
    isFavorite: Boolean,
    etkilesimViewModel: UrunEtkilesimViewModel,
    sepetViewModel: Sepet_ViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onBackClick: () -> Unit,
    onToggleFavorite: (ProductModel) -> Unit
) {
    val context = LocalContext.current
    if (isLoading || urun == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFD32F2F)) }
        return
    }

    val urunData = urun
    val uiState by etkilesimViewModel.uiState.collectAsState()

    val originalPriceValue = ProductUtils.cleanPrice(urunData.price?.formatted)
    val displayPrice = uiState.activeCampaign?.discountPrice ?: originalPriceValue
    val discountActive = displayPrice < (originalPriceValue - 0.01)

    LaunchedEffect(uiState.activeCampaign?.type) {
        if (uiState.activeCampaign != null) HapticHelper.playWarning(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(urunData.title ?: "Ürün Detayı", maxLines = 1, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Geri Dön") } }
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 8.dp, shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { HapticHelper.playSuccess(context); onToggleFavorite(urunData) },
                            modifier = Modifier.size(50.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        ) {
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favori", tint = if (isFavorite) Color(0xFFD32F2F) else Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        if (urunAdedi > 0) {
                            Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.5.dp, Color(0xFFD32F2F), RoundedCornerShape(12.dp)), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { scope.launch { sepetViewModel.sepetListesi.value.find { it.id == urunData.id }?.let { sepetViewModel.adetGuncelle(it, urunAdedi - 1) } } }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Remove, "Azalt", tint = Color(0xFFD32F2F)) }
                                Text(urunAdedi.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 16.dp))
                                IconButton(onClick = {
                                    scope.launch {
                                        if (uiState.activeCampaign != null) etkilesimViewModel.teklifiOnayla(urunData)
                                        else sepetViewModel.apiUrununuSepeteEkle(urunData, null)
                                    }
                                }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, "Artır", tint = Color(0xFFD32F2F)) }
                            }
                        } else {
                            Button(
                                onClick = {
                                    HapticHelper.playSuccess(context)
                                    scope.launch {
                                        if (uiState.activeCampaign != null) {
                                            etkilesimViewModel.teklifiOnayla(urunData)
                                        } else {
                                            sepetViewModel.apiUrununuSepeteEkle(urunData, null)
                                            etkilesimViewModel.normalSatinAlimYapildi(urunData.id)
                                        }
                                    }
                                },
                                enabled = uiState.activeCampaign?.isProcessing != true,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) { Text("Sepete Ekle", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(urunData.images.firstOrNull() ?: urunData.thumbnail).crossfade(true).build(), contentDescription = urunData.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(350.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = urunData.title ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (discountActive) {
                            Text(urunData.price?.formatted ?: "", fontSize = 18.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough, modifier = Modifier.padding(bottom = 4.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("₺%,.2f".format(displayPrice), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD32F2F))
                        } else {
                            Text(urunData.price?.formatted ?: "", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD32F2F))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ürün Detayları", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(urunData.description ?: "", fontSize = 14.sp, lineHeight = 20.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        val campaign = uiState.activeCampaign
        AnimatedVisibility(
            visible = campaign != null,
            enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 90.dp)
                .zIndex(Float.MAX_VALUE)
        ) {
            campaign?.let { aktifKampanya ->
                val kalanSaniye by etkilesimViewModel.kalanSaniye.collectAsState()
                KampanyaKarti(
                    baslik = aktifKampanya.type.baslik,
                    aciklama = aktifKampanya.type.aciklama,
                    kalanSaniye = kalanSaniye,
                    onKapat = { etkilesimViewModel.teklifiKapat() },
                    onKullan = { if (!aktifKampanya.isProcessing) etkilesimViewModel.teklifiOnayla(urunData) }
                )
            }
        }
    }
}

@Composable
fun KampanyaKarti(
    baslik: String,
    aciklama: String,
    kalanSaniye: Int,
    onKapat: () -> Unit,
    onKullan: () -> Unit
) {
    val dakika = kalanSaniye / 60
    val saniye = kalanSaniye % 60
    val zamanMetni = String.format(Locale.getDefault(), "%02d:%02d", dakika, saniye)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).shadow(16.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD32F2F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFD32F2F), androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SİZE ÖZEL FIRSAT!", color = Color(0xFFD32F2F), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
                IconButton(onClick = onKapat, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.rotate(45f), tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(baslik, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text(aciklama, fontSize = 13.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Kalan Süre", fontSize = 11.sp, color = Color.Gray)
                    Text(zamanMetni, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD32F2F))
                }
                Button(onClick = onKullan, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(10.dp)) {
                    Text("FIRSATI YAKALA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}