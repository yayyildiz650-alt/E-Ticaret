package com.example.e_ticaret.ViewModeller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_ticaret.AiSystem.ProductRepository
import com.example.e_ticaret.AiSystem.UrunEtkilesimTablosu
import android.util.Log
import com.example.e_ticaret.RetrofitApı.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class TeklifTuru(val baslik: String, val aciklama: String) {
    object YuzdeBesIndirim : TeklifTuru("Anında %5 İndirim!", "Bu ürünü şu an alırsan sepette %5 indirim kazanırsın.")
    object UcretsizKargo : TeklifTuru("Kargo Bizden!", "Bu ürüne özel tüm kargo masraflarını biz karşılıyoruz.")
    object IkinciUrunYuzdeYirmi : TeklifTuru("2. Ürün %20 İndirimli!", "Sepete bir tane daha ekle, ikinciyi %20 indirimle al.")
}

data class ActiveCampaign(
    val type: TeklifTuru,
    val discountPrice: Double?,
    val isProcessing: Boolean = false
)

data class CampaignUiState(
    val activeCampaign: ActiveCampaign? = null,
    val visitCount: Int = 0,
    val offerLevel: Int = 0
)

class UrunEtkilesimViewModel(
    private val repository: ProductRepository,
    private val sepetViewModel: Sepet_ViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    private val _kalanSaniye = MutableStateFlow(180)
    val kalanSaniye: StateFlow<Int> = _kalanSaniye.asStateFlow()

    private var sayacJob: Job? = null
    private var dwellTimeJob: Job? = null

    private var aktifUrunId: Int? = null
    private var aktifUrunFiyati: Double = 0.0
    private var aktifSepetteVarMi: Boolean = false

    private val VIP_BARAJI = 10000.0
    private val TEST_MODU = true

    fun etkilesimiKaydetVeKontrolEt(urunId: Int, urunFiyati: Double, sepetteVarMi: Boolean) {
        if (aktifUrunId == urunId && _uiState.value.activeCampaign != null) return

        if (aktifUrunId != urunId) teklifiKapatInternal()

        aktifUrunId = urunId
        aktifUrunFiyati = urunFiyati
        aktifSepetteVarMi = sepetteVarMi

        viewModelScope.launch {
            try {
                val etkilesim = withContext(Dispatchers.IO) { repository.urunEtkilesiminiGetir(urunId) }
                    ?: UrunEtkilesimTablosu(urunId)

                // KISITLAMA KALDIRILDI: Artık ziyaret sayısı test modunda da özgürce artacak
                // Böylece 2. defa girdiğinde anında tetiklenme gerçekleşecek!
                val yeniSayi = etkilesim.ziyaretSayisi + 1

                withContext(Dispatchers.IO) {
                    repository.urunEtkilesiminiKaydet(etkilesim.copy(ziyaretSayisi = yeniSayi))
                }

                _uiState.update { it.copy(visitCount = yeniSayi, offerLevel = etkilesim.teklifSeviyesi) }

                if (!TEST_MODU && etkilesim.teklifSeviyesi >= 2) return@launch

                val islemSeviyesi = if (TEST_MODU && etkilesim.teklifSeviyesi >= 2) 0 else etkilesim.teklifSeviyesi

                if (islemSeviyesi == 0) {
                    if (yeniSayi >= 2) {
                        Log.e("EtkilesimVM", "2. Giriş Tespit Edildi! Kart Açılıyor...")
                        teklifiBaslat(0, urunId) // Saniye beklemeden yapıştırır
                    } else {
                        Log.e("EtkilesimVM", "1. Giriş. 15sn Dwell Time Başladı.")
                        dwellTimeKontrolunuBaslat(0, urunId)
                    }
                } else if (islemSeviyesi == 1 && aktifSepetteVarMi) {
                    teklifiBaslat(1, urunId)
                }
            } catch (e: Exception) {
                Log.e("EtkilesimVM", "HATA: ${e.message}")
            }
        }
    }

    private fun dwellTimeKontrolunuBaslat(seviye: Int, urunId: Int) {
        dwellTimeJob?.cancel()
        dwellTimeJob = viewModelScope.launch {
            delay(15000)
            if (aktifUrunId == urunId) {
                Log.e("EtkilesimVM", "15 saniye doldu! Tetikleniyor.")
                teklifiBaslat(seviye, urunId)
            }
        }
    }

    private fun teklifiBaslat(seviye: Int, urunId: Int) {
        if (aktifUrunId != urunId) return

        viewModelScope.launch {
            if (aktifUrunFiyati < VIP_BARAJI) {
                Log.e("EtkilesimVM", "KAMPANYA İPTAL: Fiyat $VIP_BARAJI altında.")
                return@launch
            }

            val yeniTeklif = when (seviye) {
                0 -> TeklifTuru.YuzdeBesIndirim
                1 -> TeklifTuru.IkinciUrunYuzdeYirmi
                else -> null
            }

            if (yeniTeklif == null) return@launch

            val specialPrice = when (yeniTeklif) {
                is TeklifTuru.YuzdeBesIndirim -> kotlin.math.round(aktifUrunFiyati * 0.95 * 100) / 100.0
                is TeklifTuru.IkinciUrunYuzdeYirmi -> kotlin.math.round(aktifUrunFiyati * 0.80 * 100) / 100.0
                else -> null
            }

            Log.e("EtkilesimVM", "KAMPANYA YAYINDA: ${yeniTeklif.baslik}")
            _uiState.update { it.copy(activeCampaign = ActiveCampaign(yeniTeklif, specialPrice, false)) }

            _kalanSaniye.value = if (seviye == 1) 120 else 180

            sayacJob?.cancel()
            sayacJob = viewModelScope.launch {
                while (_kalanSaniye.value > 0) {
                    delay(1000)
                    _kalanSaniye.value -= 1
                }
                teklifiKapat()
            }
        }
    }

    fun normalSatinAlimYapildi(urunId: Int) {
        dwellTimeJob?.cancel()
        if (aktifUrunFiyati < VIP_BARAJI) return

        viewModelScope.launch {
            val etkilesim = withContext(Dispatchers.IO) { repository.urunEtkilesiminiGetir(urunId) }
                ?: UrunEtkilesimTablosu(urunId)

            if (etkilesim.teklifSeviyesi == 0 || TEST_MODU) {
                withContext(Dispatchers.IO) { repository.urunEtkilesiminiKaydet(etkilesim.copy(teklifSeviyesi = 1)) }
                delay(600)
                teklifiBaslat(1, urunId)
            }
        }
    }

    fun teklifiOnayla(urunData: ProductModel) {
        val currentCampaign = _uiState.value.activeCampaign ?: return
        _uiState.update { it.copy(activeCampaign = currentCampaign.copy(isProcessing = true)) }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val etkilesim = repository.urunEtkilesiminiGetir(urunData.id) ?: UrunEtkilesimTablosu(urunData.id)
                repository.urunEtkilesiminiKaydet(etkilesim.copy(teklifSeviyesi = etkilesim.teklifSeviyesi + 1, ziyaretSayisi = 0))
            }

            val basarili = sepetViewModel.apiUrununuSepeteEkle(urunData, currentCampaign.discountPrice)

            if (basarili) {
                _uiState.update { it.copy(activeCampaign = null) }

                if (currentCampaign.type == TeklifTuru.YuzdeBesIndirim && aktifUrunFiyati >= VIP_BARAJI) {
                    delay(600)
                    teklifiBaslat(1, urunData.id)
                }
            } else {
                _uiState.update { it.copy(activeCampaign = currentCampaign.copy(isProcessing = false)) }
            }
        }
    }

    fun teklifiKapat() {
        _uiState.update { it.copy(activeCampaign = null) }
        val id = aktifUrunId ?: return
        viewModelScope.launch {
            val etkilesim = withContext(Dispatchers.IO) { repository.urunEtkilesiminiGetir(id) } ?: return@launch
            // Çarpıya basıp reddederse sayaç sıfırlanır, bir sonraki giriş 1. giriş sayılır
            withContext(Dispatchers.IO) { repository.urunEtkilesiminiKaydet(etkilesim.copy(ziyaretSayisi = 0)) }
        }
    }

    fun teklifiKapatInternal() {
        _uiState.value = CampaignUiState()
        _kalanSaniye.value = 0
        sayacJob?.cancel()
        dwellTimeJob?.cancel()
    }
}