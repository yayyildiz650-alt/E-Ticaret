package com.example.e_ticaret.AiSystem


import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_ticaret.Utils.NetworkUtils
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Mesaj yapısı
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val products: List<ProductEntity> = emptyList()
)

// UI Durumu
sealed class AiUiState {
    object Idle : AiUiState()
    object Loading : AiUiState()
    object Success : AiUiState()
    data class Error(val errorMessage: String) : AiUiState()
}

class AiViewModel(application: Application, private val repository: ProductRepository) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    // Mesaj listesi
    private val _chatMessages = mutableStateListOf<ChatMessage>()
    val chatMessages: List<ChatMessage> = _chatMessages

    // 1. Fonksiyonları Tanımlıyoruz (Declaration)
    private val urunleriGetirFonksiyonu = FunctionDeclaration(
        name = "urunleri_getir",
        description = "Kullanıcı ürünleri, fiyatları veya belirli bir ihtiyacı (örn: kamp, spor) sorduğunda veritabanından güncel ürün listesini getirmek için kullanılır.",
        parameters = mapOf(
            "category" to Schema.string("Filtrelenecek kategori adı.", true),
            "maxPrice" to Schema.double("Maksimum fiyat sınırı.", true),
            "minPrice" to Schema.double("Minimum fiyat sınırı.", true),
            "keyword" to Schema.string("Ürün adında veya açıklamasında aranacak kelime (örn: 'koşu ayakkabısı', 'çadır').", true)
        )
    )

    private val kategorileriGetirFonksiyonu = FunctionDeclaration(
        name = "kategorileri_getir",
        description = "Mağazada hangi kategorilerin bulunduğunu veya kaç kategori olduğunu öğrenmek için kullanılır.",
        parameters = mapOf() // Parametre gerektirmez
    )

    // 2. Modeli Araçlarla (Tools) Başlatma
    private val generativeModel = Firebase.vertexAI.generativeModel(
        modelName = "gemini-2.5-flash",
        tools = listOf(Tool.functionDeclarations(listOf(urunleriGetirFonksiyonu, kategorileriGetirFonksiyonu))),
        systemInstruction = content {
            text("Sen bu e-ticaret uygulamasının en yetkili ve bilgili alışveriş asistanısın. " +
                    "TEMEL GÖREVİN: Kullanıcılara mağazadaki ürünleri bulmak ve bilgi vermektir. " +
                    "TEMEL ÇALIŞMA PRENSİBİ: " +
                    "1. 'Bilmiyorum' veya 'ürün yok' demeden önce mutlaka 'urunleri_getir' fonksiyonunu çağır. " +
                    "2. ARAMA STRATEJİSİ: Eğer kullanıcı genel bir şey (örn: 'kamp', 'telefon') sorarsa, bunu 'keyword' parametresiyle ara. Kategorileri sadece 'kategorileri_getir'den öğrendiğin tam sluglar ile kullan. " +
                    "3. ÜRÜN BULMA ZORUNLULUĞU: Bir arama sonucunda ürün gelmezse, hemen pes etme! Daha genel bir anahtar kelimeyle tekrar ara. " +
                    "4. Sadece Türkçe konuş ve profesyonel bir üslup kullan. " +
                    "5. ÜRÜN LİNKLEME KURALI: Metin içinde bir üründen bahsettiğinde MUTLAKA ismini şu formatta yaz: **Ürün Adı** (ID: 123). Örn: '**iPhone 15**(ID: 12)' gibi. Bu format dışında (ID: 12) gibi ek açıklamalar yazma.")
        }
    )

    // Sohbet geçmişini başlatan chat nesnesi
    private val chat = generativeModel.startChat()

    fun sendMessage(userQuery: String) {
        if (userQuery.isBlank()) return

        // İnternet kontrolü
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            val errorMsg = "İnternet bağlantısı yok. Lütfen bağlantınızı kontrol edip tekrar deneyin."
            _chatMessages.add(ChatMessage(errorMsg, false))
            _uiState.value = AiUiState.Error(errorMsg)
            return
        }

        // Kullanıcı mesajını listeye ekle
        _chatMessages.add(ChatMessage(userQuery, true))
        Log.d("AiViewModel", "User Message Added: $userQuery")

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            val currentSessionProducts = mutableListOf<ProductEntity>()

            try {
                // 3. İstek Atma
                var response = chat.sendMessage(userQuery)

                // 4 & 5. Otonom Döngü: Model fonksiyon çağırmak istediği sürece devam et
                while (response.functionCalls.isNotEmpty()) {
                    val functionResponses = mutableListOf<FunctionResponsePart>()

                    for (functionCall in response.functionCalls) {
                        when (functionCall.name) {
                            "urunleri_getir" -> {
                                val category = functionCall.args["category"]?.jsonPrimitive?.contentOrNull
                                val maxPriceStr = functionCall.args["maxPrice"]?.jsonPrimitive?.contentOrNull
                                val minPriceStr = functionCall.args["minPrice"]?.jsonPrimitive?.contentOrNull
                                val keyword = functionCall.args["keyword"]?.jsonPrimitive?.contentOrNull

                                val maxPrice = maxPriceStr?.toDoubleOrNull()

                                val minPrice = minPriceStr?.toDoubleOrNull()

                                Log.d("AiViewModel", "ÜRÜN SORGUSU: Kat=$category, Max=$maxPrice, Kelime=$keyword")

                                val products = repository.getFilteredProductsForAi(category, minPrice, maxPrice, keyword)
                                
                                // Bulunan ürünleri listeye ekle
                                currentSessionProducts.addAll(products)

                                val resultString = if (products.isEmpty()) {
                                    "Maalesef bu kriterlere uygun ürün bulunamadı."
                                } else {
                                    products.joinToString("\n") { 
                                        "[ID: ${it.id} | ÜRÜN: ${it.title} | FİYAT: ${it.formattedPrice} | KATEGORİ: ${it.category} | AÇIKLAMA: ${it.description}]"
                                    }
                                }

                                functionResponses.add(FunctionResponsePart(functionCall.name, buildJsonObject {
                                    put("urunler", resultString)
                                }))
                            }
                            "kategorileri_getir" -> {
                                Log.d("AiViewModel", "KATEGORİ SORGUSU ÇALIŞTI")
                                val categories = repository.getAllCategories()
                                val resultString = if (categories.isEmpty()) {
                                    "Sistemde henüz kategori tanımlanmamış."
                                } else {
                                    "Mevcut Kategoriler: " + categories.joinToString(", ")
                                }

                                functionResponses.add(FunctionResponsePart(functionCall.name, buildJsonObject {
                                    put("kategoriler", resultString)
                                }))
                            }
                        }
                    }

                    // Yanıtları topluca gönder
                    response = chat.sendMessage(content("function") {
                        functionResponses.forEach { part(it) }
                    })
                }

                // 6. Nihai Cevap
                val aiReply = response.text ?: "Size şu an yardımcı olamıyorum."
                _chatMessages.add(ChatMessage(aiReply, false, products = currentSessionProducts.distinctBy { it.id }))
                _uiState.value = AiUiState.Success

            } catch (e: Exception) {
                Log.e("AiViewModel", "Error: ${e.localizedMessage}", e)
                
                val errorMsg = when {
                    e is UserRecoverableAuthException -> {
                        "Google hesabınızla ilgili bir sorun var (Cihaz yönetimi gerekebilir). Lütfen hesap ayarlarınızı kontrol edin."
                    }
                    e.message?.contains("DeviceManagementRequired", ignoreCase = true) == true -> {
                        "Bu özellik için cihaz yönetimi (MDM) gereklidir. Lütfen kurumsal hesap kısıtlamalarını kontrol edin."
                    }
                    else -> "Yapay zeka yanıt verirken bir sorun oluştu: ${e.localizedMessage}"
                }
                
                _uiState.value = AiUiState.Error(errorMsg)
                _chatMessages.add(ChatMessage(errorMsg, false))
            }
        }
    }

    fun clearChat() {
        _chatMessages.clear()
        _uiState.value = AiUiState.Idle
    }
}
