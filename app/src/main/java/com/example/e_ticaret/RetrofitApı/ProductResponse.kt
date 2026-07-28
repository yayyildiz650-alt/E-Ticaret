package com.example.e_ticaret.RetrofitApı

import com.google.gson.annotations.SerializedName


//=======================RESPONSE MODELLERİMİZ======================================
data class ProductResponse(
    @SerializedName("data")
    val data: List<ProductModel>
)

data class ProductModel(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String?,

    @SerializedName("thumbnail")  // Tutacağımız küçük resim
    val thumbnail: String?,

    @SerializedName("category") // Hata sebebiydi: Kategori slug bilgisini tutan alan
    val category: String?,

    @SerializedName("badges")  // küçük etiketler
    val badges: List<String>,

    @SerializedName("images")
    val images: List<String>, // Ürünün tüm fotoğraflarının listesi

    @SerializedName("price")
    val price: PriceInfo?,

    @SerializedName("description")
      val description: String? // YENİ EKLENDİ!
)


// 3. Fiyat bilgileri ayrı bir süslü parantez ({}) içinde olduğu için ona da ayrı sınıf açtık
data class PriceInfo(
    @SerializedName("formatted")
    val formatted: String, // Örn: "₺115,00"

    @SerializedName("compareAtFormatted")
    val compareAtFormatted: String?, // Örn: "₺245,00" (İndirim yoksa null gelebilir diye '?' koyduk)

    @SerializedName("discountPercentage")
    val discountPercentage: Double?
)
