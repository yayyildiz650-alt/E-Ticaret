package com.example.e_ticaret

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarComponent() {
    var searchText by remember { mutableStateOf("") }

    // OutlinedTextFieldDefaults.DecorationBox kullanarak tamamen özelleştirilmiş ince bir kutu yapıyoruz
    BasicTextField(
        value = searchText,
        onValueChange = { searchText = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp) // Çok daha ince yaptık
            .padding(horizontal = 12.dp),
        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
        singleLine = true,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = searchText,
                visualTransformation = VisualTransformation.None,
                innerTextField = innerTextField,
                placeholder = {
                    Text("Ürün,kategori veya marka ara", color = Color(0xFFD32F2F), fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.padding(start = 4.dp))
                },
                trailingIcon = {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.padding(end = 4.dp))
                },
                singleLine = true,
                enabled = true,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp), // Dikey boşluğu sıfırladık
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        focusedBorderThickness = 1.dp,
                        unfocusedBorderThickness = 1.dp
                    )
                }
            )
        }
    )
}