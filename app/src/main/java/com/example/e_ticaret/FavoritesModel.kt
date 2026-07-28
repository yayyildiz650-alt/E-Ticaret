package com.example.e_ticaret

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "favorites")
data class FavoritesModel (
    @PrimaryKey
    val id: Int,
    val title: String,
    val formatfiyat: String,
    val imageUrl: String
)

