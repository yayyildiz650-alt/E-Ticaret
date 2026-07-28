package com.example.e_ticaret

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

import com.example.e_ticaret.ViewModeller.CategoryViewModel
import com.example.e_ticaret.ViewModeller.Sepet_ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_ticaret.ViewModeller.FilterViewModel

import com.example.e_ticaret.ViewModeller.FavoriteViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.e_ticaret.AiSystem.AiViewModel
import com.example.e_ticaret.AiSystem.ProductRepository
import com.example.e_ticaret.AiSystem.AiDatabase
import com.example.e_ticaret.AiSystem.AiChatScreen
import com.example.e_ticaret.ViewModeller.UrunEtkilesimViewModel

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    
    // Favoriler DB
    val db = remember { FavoritesDatabase.getDatabase(context) }
    
    // AI DB & Repository
    val aiDb = remember { AiDatabase.getDatabase(context) }
    val aiRepository = remember { ProductRepository(aiDb.productDao()) }

    // MERKEZİ VİEWMODELLER: Activity scope kullanılarak tüm sayfalarda AYNI hafıza hücresi garanti edilir.
    val sepetViewModel: Sepet_ViewModel = viewModel(
        viewModelStoreOwner = activity ?: return
    )

    val etkilesimViewModel: UrunEtkilesimViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return UrunEtkilesimViewModel(aiRepository, sepetViewModel) as T
            }
        }
    )

    val favoriteViewModel: FavoriteViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FavoriteViewModel(db.favoritesDao()) as T
            }
        }
    )

    val aiViewModel: AiViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val application = context.applicationContext as android.app.Application
                return AiViewModel(application, aiRepository) as T
            }
        }
    )

    val navController = rememberNavController()

    // Uygulama açıldığında AI veritabanını API'den senkronize et (Yeni ürünleri çek)
    LaunchedEffect(Unit) {
        aiRepository.syncProductsWithApi()
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Belirli sayfalarda (Detay, AI Asistan) alt menüyü gizlemek için kontrol
    val showBottomBar = when (currentRoute) {
        "ai_chat_ekrani" -> false
        else -> currentRoute?.startsWith("product_detail") != true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                A101CustomBottomMenu(
                    currentRoute = currentRoute,
                    onNavigatetoCategori = {
                        navController.navigate("kategoriler_ekrani") {
                            launchSingleTop = true
                        }
                    },
                    onNavigatetoSepet = {
                        navController.navigate("sepet_ekrani") {
                            launchSingleTop = true
                        }
                    },
                    onNavigationAnaSyfa = {
                        navController.navigate("ana_ekran") {
                            popUpTo("ana_ekran") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigatetoFavori = {
                        navController.navigate("favoriler_ekrani") {
                            launchSingleTop = true
                        }
                    },
                    onNavigatetoAi = {
                        navController.navigate("ai_chat_ekrani") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // NavHost bizim sayfalar arası geçiş sahnemizdir
        NavHost(
            navController = navController,
            startDestination = "ana_ekran",
            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {

        // 1. Rota: Ana Ekranımız
        composable("ana_ekran") {
            A101MainScreen(
                sepetViewModel = sepetViewModel,
                favoriteViewModel = favoriteViewModel,
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                }
            )
        }

        // Favoriler Rota
        composable("favoriler_ekrani") {
            val scope = rememberCoroutineScope()
            val sepetListesi by sepetViewModel.sepetListesi.collectAsState(initial = emptyList())
            val urunAdetleri = remember(sepetListesi) {
                sepetListesi.associate { it.id to it.quantity }
            }

            FavoritesScreen(
                viewModel = favoriteViewModel,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                },
                onSepeteEkleClick = { urunItem ->
                    scope.launch { sepetViewModel.urunItemSepeteEkle(urunItem) }
                },
                onAdetGuncelle = { urunItem, yeniAdet ->
                    scope.launch { sepetViewModel.urunItemAdetGuncelle(urunItem, yeniAdet) }
                },
                urunAdetleri = urunAdetleri
            )
        }

        // AI Chat Rota
        composable("ai_chat_ekrani") {
            AiChatScreen(
                viewModel = aiViewModel,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                }
            )
        }

        // 2. Rota: Kategoriler Ekranımız
        composable("kategoriler_ekrani") {
            val categoryViewModel: CategoryViewModel = viewModel()
            CategoriesScreen(
                categories = categoryViewModel.kategoriler,
                onBackClick = { navController.popBackStack() },
                onCategoryClick = { slug ->
                    navController.navigate("kategori_urunleri_ekrani/$slug")
                }
            )
        }

        // 3-) Kategori ürünleri
        composable(route = "kategori_urunleri_ekrani/{categorySlug}") { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("categorySlug") ?: ""
            val filterViewModel: com.example.e_ticaret.ViewModeller.FilterViewModel = viewModel()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val sepetListesi by sepetViewModel.sepetListesi.collectAsState(initial = emptyList())

            val urunAdetleri = remember(sepetListesi) {
                sepetListesi.associate { it.id to it.quantity }
            }

            LaunchedEffect(key1 = slug) {
                filterViewModel.filterurunlerigetir(slug)
            }

            KategoriUrunleriScreen(
                sayfaBasligi = slug.replaceFirstChar { it.uppercase() },
                urunListesi = filterViewModel.urunListesi,
                urunAdetleri = urunAdetleri,
                onGeriDonClick = { navController.popBackStack() },
                snackbarHostState = snackbarHostState,
                onSepeteEkleClick = { apiUrunu ->
                    scope.launch {
                        sepetViewModel.apiUrununuSepeteEkle(apiUrunu)
                        snackbarHostState.showSnackbar(
                            message = "Ürün başarıyla sepete eklendi",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onAdetGuncelle = { apiUrunu, yeniAdet ->

                    val urunItem = UrunItem(
                        id = apiUrunu.id,
                        ad = apiUrunu.title ?: "",
                        eskiFiyat = "", // Fiyat burada adetGuncelle için kritik değil
                        yeniFiyat = apiUrunu.price?.formatted ?: "",
                        indirimliMi = false,
                        gorselUrl = apiUrunu.thumbnail ?: "",
                        aciklama = apiUrunu.description ?: ""
                    )
                    scope.launch { sepetViewModel.urunItemAdetGuncelle(urunItem, yeniAdet) }
                },
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                }
            )
        }

        // 4-) Sepet Ekranı
        composable(route = "sepet_ekrani") {
            SepetScreen(
                onGeriClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                },
                sepetViewModel = sepetViewModel
            )
        }

                    //Detay Ekranı
            // NavHost parantezleri { } içerisine diğer composable'ların altına eklenecek:
            composable(
                route = "product_detail/{productId}"
            ) { backStackEntry ->
                // Tıklanan ürünün id'sini URL'den (rotadan) çekiyoruz
                val productIdString = backStackEntry.arguments?.getString("productId")
                val productId = productIdString?.toIntOrNull() ?: 0

                // Detay ekranını çağırıyoruz
                UrunDetayScreen(
                    productId = productId,
                    navController = navController,
                    favoriteViewModel = favoriteViewModel,
                    etkilesimViewModel = etkilesimViewModel,
                    sepetViewModel = sepetViewModel // Merkezi instance gönderiliyor
                )
            }
    }
}
}
