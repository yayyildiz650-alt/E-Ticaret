package com.example.e_ticaret

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.e_ticaret.Utils.HapticHelper
import com.example.e_ticaret.ViewModeller.Sepet_ViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SepetScreen(
    onGeriClick: () -> Unit,
    onProductClick: (Int) -> Unit = {},
    sepetViewModel: Sepet_ViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sepetListesi by sepetViewModel.sepetListesi.collectAsState()
    
    // Silme onay diyaloğu için state'ler
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<SepetEntity?>(null) }

    // Toplam tutarı hesapla
    val toplamTutar = sepetListesi.sumOf { it.price * it.quantity }

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text(text = "Sepetim", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = onGeriClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (sepetListesi.isNotEmpty()) {
                SepetAltBar(toplamTutar)
            }
        },
        containerColor = Color(0xFFF2F2F2)
    ) { paddingValues ->
        if (sepetListesi.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Sepetiniz henüz boş.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sepetListesi) { urun ->
                    SepetUrunKarti(
                        urun = urun,
                        onArtir = { scope.launch { sepetViewModel.adetGuncelle(urun, urun.quantity + 1) } },
                        onAzalt = { scope.launch { sepetViewModel.adetGuncelle(urun, urun.quantity - 1) } },
                        onSil = { 
                            productToDelete = urun
                            showDeleteDialog = true
                        },
                        onClick = { onProductClick(urun.id) }
                    )
                }
            }
        }

        // --- PROFESYONEL SİLME ONAY DİYALOĞU ---
        if (showDeleteDialog && productToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Ürünü Sil?",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                text = {
                    Text(
                        text = "${productToDelete?.title} sepetinizden kaldırılacaktır. Bu işlemi onaylıyor musunuz?",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            HapticHelper.playWarning(context)
                            productToDelete?.let { scope.launch { sepetViewModel.sepettenSil(it) } }
                            showDeleteDialog = false
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        Text("Evet, Sil", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        Text("Vazgeç", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun SepetUrunKarti(
    urun: SepetEntity,
    onArtir: () -> Unit,
    onAzalt: () -> Unit,
    onSil: () -> Unit,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    // KART İSKELETİ: Ürünün etrafındaki beyaz, hafif gölgeli ve köşeleri yuvarlatılmış dış çerçeve
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // YATAY HİZALAMA (Row): Resim, Bilgiler ve Butonların yan yana durmasını sağlar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically // İçindeki her şeyi dikeyde tam ortalar
        ) {

            // 1. BÖLÜM: ÜRÜN GÖRSELİ
            // AsyncImage, ürünün resmini internet URL'sinden arka planda uygulamayı dondurmadan (asenkron) yükler
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(urun.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = urun.title,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop // Resim kutuya sığmazsa kenarlarından kırparak oturtur
            )

            // Resim ile yazılar arasına 12 birimlik boşluk atar
            Spacer(modifier = Modifier.width(12.dp))

            // 2. BÖLÜM: ÜRÜN BİLGİLERİ (İsim ve Fiyat)
            // Column, isim ve fiyatın alt alta durmasını sağlar
            Column(
                modifier = Modifier.weight(1f), // weight(1f): Sağdaki butonlardan kalan tüm boşluğu yazılara tahsis eder
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Ürünün Adı
                Text(
                    text = urun.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2, // İsim çok uzunsa 2 satırda keser
                    overflow = TextOverflow.Ellipsis // 2 satıra sığmayan kısmın sonuna üç nokta (...) koyar
                )
                // Ürünün Fiyatı (Binlik ayraçlı format)
                Text(
                    text = "₺%,.2f".format(urun.price),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }

            // 3. BÖLÜM: ADET KONTROLÜ VE SİLME BUTONLARI
            // Silme ikonu üstte, artır/azalt çubuğu altta duracağı için tekrar Column kullanıyoruz
            Column(
                horizontalAlignment = Alignment.End, // Butonları sağa (sona) yaslar
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Silme (Çöp Tenekesi) Butonu
                IconButton(
                    onClick = onSil, // Tıklanınca dışarıdan gelen onSil fonksiyonunu çalıştırır
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Gray)
                }

                // Artırma / Azaltma Barı (Yan yana duracakları için Row içinde)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)) // Butonların arkasındaki açık gri hap şeklindeki alan
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    // Eksi (-) Butonu
                    IconButton(
                        onClick = {
                            HapticHelper.playTick(context)
                            onAzalt()
                        }, 
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(16.dp))
                    }

                    // Ortadaki Adet Yazısı
                    Text(
                        text = urun.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Artı (+) Butonu
                    IconButton(
                        onClick = {
                            HapticHelper.playTick(context)
                            onArtir()
                        }, 
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Artır", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SepetAltBar(toplam: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Toplam", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = "₺%,.2f".format(toplam),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }
            Button(
                onClick = { /* Ödeme sayfasına git */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = "Sepeti Onayla", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}



