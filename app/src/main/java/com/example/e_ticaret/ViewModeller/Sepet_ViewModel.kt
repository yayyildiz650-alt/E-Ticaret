package com.example.e_ticaret.ViewModeller

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_ticaret.AppDatabase
import com.example.e_ticaret.SepetEntity
import com.example.e_ticaret.Utils.ProductUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * Uygulama genelindeki sepet verilerini yöneten MERKEZİ ViewModel.
 * Atomik veritabanı işlemleri ve StateFlow ile %100 senkronizasyon sağlar.
 */
class Sepet_ViewModel(application: Application): AndroidViewModel(application) {

    private val sepetDao = AppDatabase.getDatabase(application).sepetDao()

    // MERKEZİ VERİ AKIŞI: stateIn ile tüm ekranların aynı atomik veriye bakması mühürlendi.
    val sepetListesi: StateFlow<List<SepetEntity>> = sepetDao.sepetiGetir()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()

        )

    /**
     * GERÇEK SUSPEND: İşlem bitene kadar caller'ı (UI) bekletir.
     * @return İşlemin başarı durumu
     */
    suspend fun sepeteEkle(urun: SepetEntity, fiyatGuncelle: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val mevcutUrun = sepetDao.urunGetir(urun.id)
                
                if (mevcutUrun != null) {
                    val guncelFiyat = if (fiyatGuncelle) urun.price else mevcutUrun.price
                    val guncelUrun = mevcutUrun.copy(
                        quantity = mevcutUrun.quantity + 1,
                        price = guncelFiyat
                    )
                    Log.d("SepetVM", ">>> MÜHÜR BAŞARILI: GÜNCELLEME <<<")
                    Log.d("SepetVM", "Ürün: ${urun.title}, Eski: ${mevcutUrun.price}, Yeni Mühür: $guncelFiyat")
                    sepetDao.sepeteEkle(guncelUrun)
                } else {
                    Log.d("SepetVM", ">>> MÜHÜR BAŞARILI: YENİ EKLEME <<<")
                    Log.d("SepetVM", "Ürün: ${urun.title}, Fiyat: ${urun.price}")
                    sepetDao.sepeteEkle(urun)
                }
                true
            } catch (e: Exception) {
                Log.e("SepetVM", "!!! MÜHÜRLEME KRİZİ !!!: ${e.message}")
                false
            }
        }
    }

    suspend fun sepettenSil(urun: SepetEntity) {
        withContext(Dispatchers.IO) {
            Log.d("SepetVM", "Ürün Siliniyor -> ID: ${urun.id}")
            sepetDao.sepettenSil(urun)
        }
    }

    /**
     * NİHAİ MÜHÜR METODU: Room Transaction kullanarak fiyatı ve adedi mühürler.
     * @return Başarı durumu (ViewModel orkestrasyonu için kritik)
     */
    suspend fun apiUrununuSepeteEkle(
        apiUrunu: com.example.e_ticaret.RetrofitApı.ProductModel,
        ozelFiyat: Double? = null
    ): Boolean {
        val originalPrice = ProductUtils.cleanPrice(apiUrunu.price?.formatted)
        val finalPrice = ozelFiyat ?: originalPrice

        return withContext(Dispatchers.IO) {
            try {
                Log.d("SepetVM", ">>> NİHAİ MÜHÜR: TRANSACTION BAŞLATILDI <<<")
                Log.d("SepetVM", "Ürün: ${apiUrunu.title}, Orijinal: $originalPrice, Mühürlenen: $finalPrice")
                
                sepetDao.sepeteKesinEkle(
                    id = apiUrunu.id,
                    title = apiUrunu.title ?: "",
                    price = finalPrice,
                    imageUrl = apiUrunu.thumbnail ?: "",
                    forceUpdatePrice = ozelFiyat != null,
                    originalPrice = originalPrice
                )
                
                Log.d("SepetVM", ">>> NİHAİ MÜHÜR: İŞLEM BAŞARILI <<<")
                true
            } catch (e: Exception) {
                Log.e("SepetVM", "!!! MÜHÜRLEME KRİZİ !!!: ${e.message}")
                false
            }
        }
    }

    /**
     * FİYAT KORUMALI ADET GÜNCELLEME:
     * Yeni eklenen her adet ORİJİNAL fiyattan eklenir. 
     * Çıkarılan her adet ORİJİNAL fiyattan düşülür (böylece indirimli hak korunur).
     */
    suspend fun adetGuncelle(urun: SepetEntity, yeniAdet: Int) {
        withContext(Dispatchers.IO) {
            if (yeniAdet <= 0) {
                sepetDao.sepettenSil(urun)
                return@withContext
            }

            val guncelUrun = when {
                yeniAdet > urun.quantity -> {
                    // Adet Artışı: (Mevcut Toplam + Orijinal Fiyat) / Yeni Adet
                    val yeniToplam = (urun.price * urun.quantity) + urun.originalPrice
                    val yeniOrtalamaFiyat = yeniToplam / yeniAdet
                    urun.copy(quantity = yeniAdet, price = yeniOrtalamaFiyat)
                }
                yeniAdet < urun.quantity -> {
                    // Adet Azalışı: (Mevcut Toplam - Orijinal Fiyat) / Yeni Adet
                    // Not: Bu sayede sepette kalan son ürün indirimli fiyatını korumuş olur.
                    val yeniToplam = (urun.price * urun.quantity) - urun.originalPrice
                    val yeniOrtalamaFiyat = yeniToplam / yeniAdet
                    urun.copy(quantity = yeniAdet, price = yeniOrtalamaFiyat)
                }
                else -> urun
            }

            Log.d("SepetVM", "Adet Güncellendi: ${urun.title}, Yeni Adet: $yeniAdet, Yeni Birim Fiyat: ${guncelUrun.price}")
            sepetDao.sepeteEkle(guncelUrun)
        }
    }

    suspend fun urunItemSepeteEkle(urun: com.example.e_ticaret.UrunItem) {
        val cleanPrice = ProductUtils.cleanPrice(urun.yeniFiyat)
        val entity = SepetEntity(
            id = urun.id,
            title = urun.ad,
            price = cleanPrice,
            originalPrice = cleanPrice, 
            quantity = 1,
            imageUrl = urun.gorselUrl
        )
        sepeteEkle(entity)
    }

    suspend fun urunItemAdetGuncelle(urun: com.example.e_ticaret.UrunItem, yeniAdet: Int) {
        val mevcutUrun = withContext(Dispatchers.IO) { sepetDao.urunGetir(urun.id) }
        if (mevcutUrun != null) {
            adetGuncelle(mevcutUrun, yeniAdet)
        } else if (yeniAdet > 0) {
            urunItemSepeteEkle(urun)
        }
    }
}
