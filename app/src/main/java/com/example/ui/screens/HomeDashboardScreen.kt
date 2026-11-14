package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.PrayerType
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.tr
import com.example.ui.components.EmeraldGoldGradientCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardUiState

@Composable
fun HomeDashboardScreen(
    uiState: DashboardUiState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.settings.language
    val times = uiState.prayerTimes
    val next = uiState.nextPrayerStatus

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Hero Islamic Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_mosque),
                    contentDescription = "ইসলামিক কুইজ ব্যানার",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Emerald950.copy(alpha = 0.65f),
                                    Emerald950.copy(alpha = 0.35f),
                                    Emerald950.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontFamily = AmiriArabicFontFamily,
                                color = Gold300,
                                fontSize = 18.sp
                            )
                            Text(
                                text = tr("ইসলামিক কুইজ", "Islamic Quiz", lang),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { onNavigate("settings") },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Emerald900.copy(alpha = 0.7f))
                                .border(1.dp, Gold400.copy(alpha = 0.5f), CircleShape)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = tr("সেটিংস", "Settings", lang),
                                tint = Gold300
                            )
                        }
                    }

                    // 3-Date Display Strip (English, Bangla, Hijri)
                    Surface(
                        color = Emerald950.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold400.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = uiState.englishDateStr,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${BengaliNumeralUtil.formatNum(uiState.banglaDate.day, lang)} ${uiState.banglaDate.monthNameBn} ${BengaliNumeralUtil.formatNum(uiState.banglaDate.year, lang)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald100
                                )
                                Text(
                                    text = "${BengaliNumeralUtil.formatNum(uiState.hijriDate.day, lang)} ${uiState.hijriDate.monthNameBn} ${BengaliNumeralUtil.formatNum(uiState.hijriDate.year, lang)} হি.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gold300,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Next Prayer Countdown & 5 Daily Prayers Card
        item {
            Spacer(Modifier.height(14.dp))
            EmeraldGoldGradientCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigate("prayer") }
                    .testTag("home_prayer_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Gold300,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (lang == AppLanguage.BENGALI) times?.locationNameBn ?: "ঢাকা" else times?.locationNameEn ?: "Dhaka",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Gold300
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            val nextName = if (lang == AppLanguage.BENGALI) next?.nextPrayer?.bnName ?: "ফজর" else next?.nextPrayer?.enName ?: "Fajr"
                            Text(
                                text = tr("পরবর্তী ওয়াক্ত: $nextName", "Next Prayer: $nextName", lang),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    "সময়: ${BengaliNumeralUtil.formatTime12h(next?.targetTimeStr ?: "05:00", lang)}",
                                    "Time: ${BengaliNumeralUtil.formatTime12h(next?.targetTimeStr ?: "05:00", lang)}",
                                    lang
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald100
                            )
                        }

                        // Live Countdown Chip
                        val rem = next?.remainingSeconds ?: 0L
                        val h = rem / 3600
                        val m = (rem % 3600) / 60
                        val s = rem % 60
                        val countdownRaw = String.format("%02d:%02d:%02d", h, m, s)
                        Surface(
                            color = Gold500.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold400)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tr("বাকি সময়", "Countdown", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gold300
                                )
                                Text(
                                    text = BengaliNumeralUtil.formatDigits(countdownRaw, lang),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Gold300,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Gold400.copy(alpha = 0.25f))
                    Spacer(Modifier.height(12.dp))

                    // Five Daily Prayer Times Row
                    if (times != null) {
                        val prayerPairs = listOf(
                            Triple(PrayerType.FAJR, times.fajr, next?.nextPrayer == PrayerType.FAJR),
                            Triple(PrayerType.DHUHR, times.dhuhr, next?.nextPrayer == PrayerType.DHUHR),
                            Triple(PrayerType.ASR, times.asr, next?.nextPrayer == PrayerType.ASR),
                            Triple(PrayerType.MAGHRIB, times.maghrib, next?.nextPrayer == PrayerType.MAGHRIB),
                            Triple(PrayerType.ISHA, times.isha, next?.nextPrayer == PrayerType.ISHA)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            prayerPairs.forEach { (pType, pTime, isNext) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isNext) Gold500.copy(alpha = 0.22f) else Color.Transparent)
                                        .padding(vertical = 6.dp, horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.BENGALI) pType.bnName else pType.enName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isNext) Gold300 else Emerald100,
                                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    val short12 = BengaliNumeralUtil.formatTime12h(pTime, lang)
                                        .replace("সকাল ", "")
                                        .replace("দুপুর ", "")
                                        .replace("বিকাল ", "")
                                        .replace("সন্ধ্যা ", "")
                                        .replace("রাত ", "")
                                        .replace(" AM", "")
                                        .replace(" PM", "")
                                    Text(
                                        text = short12,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Featured 1000-Question Quiz CTA Banner
        item {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigate("quiz") }
                    .testTag("home_quiz_featured_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_quiz_banner),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                            .background(Emerald950.copy(alpha = 0.62f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = Gold500,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tr("১০০০ প্রশ্ন • ৪টি স্তর", "1000 Questions • 4 Tiers", lang),
                                    color = Emerald950,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = tr("ইসলামিক কুইজ গেম খেলুন", "Play Islamic Quiz Game", lang),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(
                                    "সর্বোচ্চ স্কোর: ${BengaliNumeralUtil.toBengali(uiState.settings.quizHighScore)} • দৈনিক কুইজ ও লেভেল সিস্টেম",
                                    "High Score: ${uiState.settings.quizHighScore} • Daily Quiz & Levels",
                                    lang
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = Gold300
                            )
                        }
                        Surface(
                            color = Gold500,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = tr("খেলুন", "Play", lang),
                                    tint = Emerald950,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Main Features Grid (Quran, Dua, Tasbih, Quiz, Prayer, Calendar, Settings)
        item {
            Spacer(Modifier.height(18.dp))
            Text(
                text = tr("ইসলামিক ফিচারসমূহ", "Islamic Features", lang),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            Spacer(Modifier.height(10.dp))

            val tiles = listOf(
                DashboardTile("quran", tr("আল-কুরআন", "Al-Quran", lang), tr("৩০ পারা • ১১৪ সূরা", "30 Para • 114 Surah", lang), Icons.AutoMirrored.Filled.MenuBook, Emerald700),
                DashboardTile("dua", tr("ইসলামিক দোয়া", "Islamic Duas", lang), tr("৩৪+ মাসনূন দোয়া", "34+ Authentic Duas", lang), Icons.Default.Favorite, Color(0xFF1A6B5C)),
                DashboardTile("tasbih", tr("ডিজিটাল তাসবিহ", "Digital Tasbih", lang), tr("৩৩ / ৯৯ / ১০০ গণনা", "33 / 99 / 100 Counter", lang), Icons.Default.TouchApp, Color(0xFF9C7212)),
                DashboardTile("quiz", tr("কুইজ গেম", "Quiz Game", lang), tr("১০০০ যাচাইকৃত প্রশ্ন", "1000 Verified Qs", lang), Icons.Default.EmojiEvents, Color(0xFF0E5A40)),
                DashboardTile("prayer", tr("নামাজ ও আজান", "Prayer & Adhan", lang), tr("৫ ওয়াক্ত • অ্যালার্ম", "5 Times • Alarms", lang), Icons.Default.NotificationsActive, Color(0xFF155E63)),
                DashboardTile("calendar", tr("৩-ইন-১ ক্যালেন্ডার", "3-in-1 Calendar", lang), tr("ইংরেজি • বাংলা • হিজরি", "Eng • Bangla • Hijri", lang), Icons.Default.CalendarMonth, Color(0xFF8C6210))
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tiles.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { tile ->
                            DashboardTileCard(
                                tile = tile,
                                onClick = { onNavigate(tile.route) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 5. Daily Hadith / Quote Card
        item {
            Spacer(Modifier.height(18.dp))
            val q = uiState.dailyQuote
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Gold600,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = q.typeBn,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = q.arabic,
                        fontFamily = AmiriArabicFontFamily,
                        fontSize = 20.sp,
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = q.textBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "সূত্র: ${q.sourceBn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class DashboardTile(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
private fun DashboardTileCard(
    tile: DashboardTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .heightIn(min = 104.dp)
            .clickable(onClick = onClick)
            .testTag("tile_${tile.route}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tile.accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.title,
                    tint = tile.accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = tile.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tile.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
