package com.example.e_ticaret.AiSystem

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.e_ticaret.AiSystem.AiUiState
import com.example.e_ticaret.AiSystem.AiViewModel
import com.example.e_ticaret.AiSystem.ChatMessage
import com.example.e_ticaret.AiSystem.ProductEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiViewModel,
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages = viewModel.chatMessages
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // Pull to Refresh Handler
    val onRefresh: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            viewModel.clearChat()
            kotlinx.coroutines.delay(1000) // Animasyonun görünmesi için kısa bir bekleme
            isRefreshing = false
        }
    }

    // Yeni mesaj geldiğinde en alta kaydır
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("E-Asistanım", fontWeight = FontWeight.ExtraBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF2F2F2))
        ) {
            // Mesaj Listesi (Yenileme Özelliği ile)
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.weight(1f)
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        WelcomeCard(
                            onSuggestionClick = { suggestion ->
                                viewModel.sendMessage(suggestion)
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            ChatBubble(message, onProductClick)
                        }

                        if (uiState is AiUiState.Loading) {
                            item {
                                LoadingBubble()
                            }
                        }
                    }
                }
            }

            // Giriş Alanı
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text("Mesajınızı yazın...", color = Color.Gray, fontSize = 15.sp)
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onProductClick: (Int) -> Unit) {
    val isUser = message.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) Color(0xFFD32F2F) else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = if (isUser) 2.dp else 4.dp,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            val annotatedText = if (isUser) {
                buildAnnotatedString { append(message.text) }
            } else {
                parseChatText(message.text, Color(0xFFD32F2F))
            }

            // ClickableText yerine son Compose sürümlerinde Text + onTextLayout veya LinkAnnotation önerilir
            // Ancak en stabil ve basit çözüm SelectionContainer içinde Text veya ClickableText'tir.
            // Burada ClickableText kullanarak link tıklamalarını yakalayacağız.
            androidx.compose.foundation.text.ClickableText(
                text = annotatedText,
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = if (isUser) Color.White else Color.Black
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "product_id", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            val productId = annotation.item.toIntOrNull()
                            if (productId != null) {
                                onProductClick(productId)
                            }
                        }
                }
            )
        }
    }
}
/**
 * Yapay zeka metnini tarayıp **[Ad]**(id:[ID]) formatındaki ürünleri linke dönüştürür.
 */
fun parseChatText(text: String, primaryColor: Color): AnnotatedString {
    return buildAnnotatedString {
        // Gelişmiş Regex: **Ürün Adı** (ID: 123) veya (id: 123) formatını çok daha esnek yakalar
        val regex = Regex("""\*\*([^*]+)\*\*\s*\((?:ID|id|Id):\s*(\d+)\)""")
        var lastIndex = 0

        regex.findAll(text).forEach { matchResult ->
            // Linkten önceki düz metni ekle
            val prevText = text.substring(lastIndex, matchResult.range.first)
            append(prevText)

            val productName = matchResult.groupValues[1].trim()
            val productId = matchResult.groupValues[2]

            // Ürün ismini stil vererek ekle (Kesin Bold ve Underline)
            pushStringAnnotation(tag = "product_id", annotation = productId)
            withStyle(
                style = SpanStyle(
                    color = primaryColor,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(productName)
            }
            pop()

            lastIndex = matchResult.range.last + 1
        }

        // Kalan son metni ekle
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
fun WelcomeCard(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            color = Color.White,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFD32F2F).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Size Nasıl Yardımcı Olabilirim?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Benimle mağazadaki ürünleri bulabilir, fiyatları karşılaştırabilir ve size en uygun seçenekleri keşfedebilirsiniz.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Öneriler",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SuggestionItem("En uygun fiyatlı telefonlar hangileri?", onSuggestionClick)
                SuggestionItem("Kamp ile ilgili ürünleri getir.", onSuggestionClick)
                SuggestionItem("Bana kulaklık önerir misin?", onSuggestionClick)
            }
        }
    }
}

@Composable
fun SuggestionItem(text: String, onClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(text) },
        color = Color(0xFFE0E0E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("E-Asistanım düşünüyor...", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
