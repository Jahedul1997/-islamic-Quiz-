package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.seed.DuaStaticData
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.tr
import com.example.ui.components.IslamicScreenHeader
import com.example.ui.components.IslamicStarBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.DuaViewModel

@Composable
fun DuaScreen(
    viewModel: DuaViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val playingDuaId by viewModel.playingDuaId.collectAsState()

    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyBookmarks by remember { mutableStateOf(false) }

    val filteredDuas = remember(selectedCategory, searchQuery, showOnlyBookmarks, bookmarkedIds) {
        val q = searchQuery.trim().lowercase()
        DuaStaticData.allDuas.filter { dua ->
            val catOk = selectedCategory == "all" || dua.categoryId == selectedCategory
            val bmOk = !showOnlyBookmarks || dua.id in bookmarkedIds
            val searchOk = q.isEmpty() ||
                dua.titleBn.lowercase().contains(q) ||
                dua.titleEn.lowercase().contains(q) ||
                dua.translationBn.lowercase().contains(q)
            catOk && bmOk && searchOk
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopAudio() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = tr("ইসলামিক দোয়া ও যিকির", "Islamic Duas & Adhkar", language),
            subtitle = tr(
                "${BengaliNumeralUtil.toBengali(DuaStaticData.allDuas.size)}টি যাচাইকৃত দোয়া • অডিও ও ফজিলতসহ",
                "${DuaStaticData.allDuas.size} Authentic Duas with Audio",
                language
            ),
            actions = {
                IconButton(
                    onClick = { showOnlyBookmarks = !showOnlyBookmarks },
                    modifier = Modifier.size(48.dp).testTag("dua_favorites_filter_btn")
                ) {
                    Icon(
                        imageVector = if (showOnlyBookmarks) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = tr("পছন্দের দোয়া", "Favorites", language),
                        tint = Gold300
                    )
                }
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(tr("দোয়া খুঁজুন (ঘুম, সফর, খাবার, বিপদ...)", "Search Duas...", language)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("dua_search_input")
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DuaStaticData.categories) { (catId, catBn) ->
                FilterChip(
                    selected = selectedCategory == catId,
                    onClick = { selectedCategory = catId },
                    label = { Text(catBn) },
                    modifier = Modifier.testTag("dua_cat_$catId")
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        if (filteredDuas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(tr("কোনো দোয়া পাওয়া যায়নি", "No Duas found", language))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDuas, key = { it.id }) { dua ->
                    val isFav = dua.id in bookmarkedIds
                    val isPlaying = playingDuaId == dua.id

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.testTag("dua_card_${dua.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IslamicStarBadge(label = BengaliNumeralUtil.formatNum(dua.id, language))
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) dua.titleBn else "${dua.titleBn} (${dua.titleEn})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = dua.categoryBn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.playDuaAudio(dua) },
                                    modifier = Modifier.size(44.dp).testTag("dua_audio_btn_${dua.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.StopCircle else Icons.Default.VolumeUp,
                                        contentDescription = "দোয়া অডিও শুনুন",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleBookmark(dua.id, isFav) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "পছন্দ",
                                        tint = if (isFav) ErrorCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = dua.arabic,
                                    fontFamily = AmiriArabicFontFamily,
                                    fontSize = 23.sp,
                                    lineHeight = 36.sp,
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "উচ্চারণ: ${dua.transliterationBn}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "অর্থ: ${dua.translationBn}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "ফজিলত: ${dua.fazilatBn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = "সূত্র: ${dua.referenceBn}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
