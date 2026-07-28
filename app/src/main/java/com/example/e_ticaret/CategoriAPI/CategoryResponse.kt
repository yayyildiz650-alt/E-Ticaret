package com.example.e_ticaret.CategoriAPI

data class CategoryResponse (
    val data: List<CategoryModel>
)


//Kategorileri çekmek için response sınıfı
data class CategoryModel(
    val slug: String,
    val name: String,
    val productCount: Int, // Stok adedi========
    val image: String
)