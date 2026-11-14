package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.seed.QuranMetadataData
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.tr
import com.example.ui.components.EmeraldGoldGradientCard
import com.example.ui.components.IslamicScreenHeader
import com.example.ui.components.IslamicStarBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    uiState: QuranUiState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val activeSurah = uiState.activeSurah
    if (activeSurah != null) {
        BackHandler { viewModel.closeSurah() }
        SurahDetailView(
            viewModel = viewModel,
            uiState = uiState,
            language = language,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = tr("পবিত্র আল-কুরআন", "Holy Al-Quran", language),
            subtitle = tr(
                "৩০ পারা • ১১৪ সূরা • অফলাইনে সংরক্ষিত: ${BengaliNumeralUtil.toBengali(uiState.cachedSurahNumbers.size)}টি সূরা",
                "30 Para • 114 Surahs • Offline Ready: ${uiState.cachedSurahNumbers.size}",
                language
            ),
            actions = {
                IconButton(
                    onClick = { viewModel.downloadAllForOffline() },
                    modifier = Modifier.size(48.dp).testTag("quran_download_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = tr("সব সূরা ডাউনলোড", "Download All Offline", language),
                        tint = Gold300
                    )
                }
            }
        )

        uiState.bulkDownloadProgress?.let { (curr, total) ->
            LinearProgressIndicator(
                progress = { curr.toFloat() / total.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
                color = Gold500
            )
            Text(
                text = tr(
                    "অফলাইনের জন্য ডাউনলোড হচ্ছে: ${BengaliNumeralUtil.toBengali(curr)} / ${BengaliNumeralUtil.toBengali(total)} সূরা",
                    "Downloading for offline: $curr / $total",
                    language
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Last Read Resume Card
        val lastSurahInfo = QuranMetadataData.findSurah(uiState.lastReadSurah)
        EmeraldGoldGradientCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable { viewModel.openSurah(uiState.lastReadSurah) }
                .testTag("quran_resume_last_read")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr("সর্বশেষ পাঠ", "Last Read", language),
                        style = MaterialTheme.typography.labelMedium,
                        color = Gold300
                    )
                    Text(
                        text = "${lastSurahInfo.nameBn} (${lastSurahInfo.nameArabic}) — ${tr("আয়াত", "Ayah", language)} ${BengaliNumeralUtil.formatNum(uiState.lastReadAyah, language)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                FilledTonalButton(
                    onClick = { viewModel.openSurah(uiState.lastReadSurah) },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Gold500, contentColor = Emerald950)
                ) {
                    Text(tr("পড়ুন", "Resume", language), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text(tr("সূরা খুঁজুন (নাম বা নম্বর)...", "Search Surah...", language)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("quran_search_input")
        )

        Spacer(Modifier.height(10.dp))

        // Tabs: Surahs / Paras / Bookmarks
        val tabs = listOf(
            tr("সূরা (১১৪)", "Surahs (114)", language),
            tr("পারা (৩০)", "Paras (30)", language),
            tr("বুকমার্ক (${BengaliNumeralUtil.toBengali(uiState.bookmarks.size)})", "Bookmarks (${uiState.bookmarks.size})", language)
        )
        TabRow(selectedTabIndex = uiState.selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = uiState.selectedTab == idx,
                    onClick = { viewModel.selectTab(idx) },
                    text = { Text(label, maxLines = 1) },
                    modifier = Modifier.testTag("quran_tab_$idx")
                )
            }
        }

        when (uiState.selectedTab) {
            0 -> {
                val filtered = remember(uiState.searchQuery) {
                    val q = uiState.searchQuery.trim().lowercase()
                    if (q.isEmpty()) QuranMetadataData.allSurahs
                    else QuranMetadataData.allSurahs.filter {
                        it.nameBn.lowercase().contains(q) ||
                            it.nameEn.lowercase().contains(q) ||
                            it.number.toString() == q
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.number }) { surah ->
                        val isCached = surah.number in uiState.cachedSurahNumbers
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openSurah(surah.number) }
                                .testTag("surah_item_${surah.number}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IslamicStarBadge(label = BengaliNumeralUtil.formatNum(surah.number, language))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) surah.nameBn else surah.nameEn,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${surah.meaningBn} • ${surah.revelationTypeBn} • ${BengaliNumeralUtil.formatNum(surah.ayahCount, language)} ${tr("আয়াত", "Ayahs", language)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = surah.nameArabic,
                                        fontFamily = AmiriArabicFontFamily,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isCached) {
                                        Text(
                                            text = tr("অফলাইন ✓", "Offline ✓", language),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(QuranMetadataData.allParas, key = { it.juzNumber }) { para ->
                        val startSurah = QuranMetadataData.findSurah(para.startSurah)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openSurah(para.startSurah) }
                                .testTag("para_item_${para.juzNumber}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IslamicStarBadge(label = BengaliNumeralUtil.formatNum(para.juzNumber, language))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = para.nameBn,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "শুরু: ${startSurah.nameBn} (${BengaliNumeralUtil.formatNum(para.startAyah, language)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = para.nameArabic,
                                    fontFamily = AmiriArabicFontFamily,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                if (uiState.bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(10.dp))
                            Text(tr("কোনো আয়াত বুকমার্ক করা হয়নি", "No bookmarked ayahs yet", language), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.bookmarks, key = { "${it.surahNumber}_${it.ayahNumber}" }) { bm ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openSurah(bm.surahNumber) },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${bm.surahNameBn} • আয়াত ${BengaliNumeralUtil.formatNum(bm.ayahNumber, language)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(bm.arabicSnippet, fontFamily = AmiriArabicFontFamily, fontSize = 18.sp)
                                        Text(bm.translationSnippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeBookmark(bm.surahNumber, bm.ayahNumber) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = ErrorCrimson)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahDetailView(
    viewModel: QuranViewModel,
    uiState: QuranUiState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val surah = uiState.activeSurah ?: return
    val bookmarkedSet = remember(uiState.bookmarks) {
        uiState.bookmarks.map { it.surahNumber to it.ayahNumber }.toSet()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = "${surah.nameBn} (${surah.nameArabic})",
            subtitle = "${surah.meaningBn} • ${surah.revelationTypeBn} • ${BengaliNumeralUtil.formatNum(surah.ayahCount, language)} আয়াত",
            onBack = { viewModel.closeSurah() },
            actions = {
                IconButton(onClick = { viewModel.changeFontSize(-2) }, modifier = Modifier.size(44.dp)) {
                    Text("A-", color = Gold300, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { viewModel.changeFontSize(+2) }, modifier = Modifier.size(44.dp)) {
                    Text("A+", color = Gold300, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = {
                        if (uiState.audioState.isPlaying) viewModel.stopAudio()
                        else viewModel.playFullSurah()
                    },
                    modifier = Modifier.size(48.dp).testTag("surah_play_all_btn")
                ) {
                    Icon(
                        imageVector = if (uiState.audioState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "তিলাওয়াত শুনুন",
                        tint = Gold300
                    )
                }
            }
        )

        uiState.audioState.errorMessage?.let { err ->
            Surface(color = ErrorCrimson.copy(alpha = 0.14f), modifier = Modifier.fillMaxWidth()) {
                Text(err, color = ErrorCrimson, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
            }
        }

        if (uiState.isLoadingSurah) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    Text(tr("সূরা লোড হচ্ছে...", "Loading Surah...", language))
                }
            }
        } else if (uiState.loadError != null && uiState.activeAyahs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = WarningAmber)
                    Spacer(Modifier.height(10.dp))
                    Text(uiState.loadError, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.openSurah(surah.number) }) {
                        Text(tr("পুনরায় চেষ্টা করুন", "Retry", language))
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (surah.number != 9 && surah.number != 1) {
                    item {
                        Surface(
                            color = Emerald800,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontFamily = AmiriArabicFontFamily,
                                fontSize = 24.sp,
                                color = Gold300,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
                            )
                        }
                    }
                }

                itemsIndexed(uiState.activeAyahs, key = { _, a -> a.numberInSurah }) { index, ayah ->
                    val isBookmarked = (ayah.surahNumber to ayah.numberInSurah) in bookmarkedSet
                    val isPlayingThis = uiState.audioState.currentSurah == ayah.surahNumber &&
                        uiState.audioState.currentAyahInSurah == ayah.numberInSurah &&
                        (uiState.audioState.isPlaying || uiState.audioState.isLoading)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayingThis) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IslamicStarBadge(label = BengaliNumeralUtil.formatNum(ayah.numberInSurah, language))
                                Row {
                                    IconButton(
                                        onClick = { viewModel.markLastReadAyah(ayah.surahNumber, ayah.numberInSurah) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.lastReadSurah == ayah.surahNumber && uiState.lastReadAyah == ayah.numberInSurah)
                                                Icons.Default.CheckCircle else Icons.Default.OutlinedFlag,
                                            contentDescription = "সর্বশেষ পাঠ সংরক্ষণ",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleBookmark(ayah, surah.nameBn, isBookmarked) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "বুকমার্ক",
                                            tint = Gold600
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleAyahAudio(index) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        if (uiState.audioState.isLoading && isPlayingThis) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(
                                                imageVector = if (isPlayingThis && uiState.audioState.isPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                                                contentDescription = "আয়াত তিলাওয়াত",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = ayah.arabicText,
                                fontFamily = AmiriArabicFontFamily,
                                fontSize = uiState.arabicFontSize.sp,
                                lineHeight = (uiState.arabicFontSize + 16).sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (ayah.transliterationBn.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "উচ্চারণ: ${ayah.transliterationBn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "অর্থ: ${ayah.translationBn}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
