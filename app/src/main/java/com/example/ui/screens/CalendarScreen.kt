package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserSettings
import com.example.data.model.AppLanguage
import com.example.data.util.BanglaCalendarConverter
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.HijriCalendarConverter
import com.example.data.util.tr
import com.example.ui.components.EmeraldGoldGradientCard
import com.example.ui.components.IslamicScreenHeader
import com.example.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private data class MonthEventItem(
    val date: LocalDate,
    val hijriLabel: String,
    val eventTitle: String
)

@Composable
fun CalendarScreen(
    settings: UserSettings,
    onHijriOffsetChange: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = settings.language
    val today = remember { LocalDate.now() }
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }

    val selectedBangla = remember(selectedDate) {
        BanglaCalendarConverter.fromGregorian(selectedDate)
    }
    val selectedHijri = remember(selectedDate, settings.hijriAdjustmentDays) {
        HijriCalendarConverter.fromGregorian(selectedDate, settings.hijriAdjustmentDays)
    }

    val monthFirstBangla = remember(displayedMonth) {
        BanglaCalendarConverter.fromGregorian(displayedMonth.atDay(1))
    }
    val monthLastBangla = remember(displayedMonth) {
        BanglaCalendarConverter.fromGregorian(displayedMonth.atEndOfMonth())
    }
    val monthFirstHijri = remember(displayedMonth, settings.hijriAdjustmentDays) {
        HijriCalendarConverter.fromGregorian(displayedMonth.atDay(1), settings.hijriAdjustmentDays)
    }
    val monthLastHijri = remember(displayedMonth, settings.hijriAdjustmentDays) {
        HijriCalendarConverter.fromGregorian(displayedMonth.atEndOfMonth(), settings.hijriAdjustmentDays)
    }

    val monthEvents = remember(displayedMonth, settings.hijriAdjustmentDays, lang) {
        val list = mutableListOf<MonthEventItem>()
        for (d in 1..displayedMonth.lengthOfMonth()) {
            val gDate = displayedMonth.atDay(d)
            val h = HijriCalendarConverter.fromGregorian(gDate, settings.hijriAdjustmentDays)
            if (h.islamicEventBn != null) {
                list.add(
                    MonthEventItem(
                        date = gDate,
                        hijriLabel = "${BengaliNumeralUtil.formatNum(h.day, lang)} ${h.monthNameBn} ${BengaliNumeralUtil.formatNum(h.year, lang)}",
                        eventTitle = h.islamicEventBn
                    )
                )
            }
        }
        list
    }

    val annualMajorEvents = remember {
        listOf(
            "১ মহররম" to "হিজরি নববর্ষ",
            "১০ মহররম" to "পবিত্র আশুরা",
            "পবিত্র মিলাদুন্নবী (দঃ)" to "১২ রবিউল আউয়াল",
            "২৭ রজব" to "শবে মেরাজ",
            "১৫ শাবান" to "শবে বরাত (লাইলাতুল বারাআত)",
            "১ রমজান" to "পবিত্র রমজান শুরু",
            "২৭ রমজান" to "শবে কদর (সম্ভাব্য)",
            "১ শাওয়াল" to "পবিত্র ঈদুল ফিতর",
            "৯ জিলহজ" to "পবিত্র আরাফার দিন",
            "১০ জিলহজ" to "পবিত্র ঈদুল আজহা"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = tr("৩-ইন-১ ক্যালেন্ডার", "3-in-1 Calendar", lang),
            subtitle = tr("ইংরেজি • বাংলা • আরবি/হিজরি", "Gregorian • Bangla • Hijri", lang),
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Selected Date 3-in-1 Card
            item {
                EmeraldGoldGradientCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = BengaliNumeralUtil.formatGregorianDate(selectedDate, lang),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedDate == today) {
                                Surface(color = Gold500, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = tr("আজ", "Today", lang),
                                        color = Emerald950,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Gold400.copy(alpha = 0.25f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bangla Date Subcard
                            Surface(
                                color = Color.White.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = tr("বাংলা তারিখ", "Bangla Date", lang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Emerald100
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${BengaliNumeralUtil.formatNum(selectedBangla.day, lang)} ${selectedBangla.monthNameBn}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${BengaliNumeralUtil.formatNum(selectedBangla.year, lang)} বঙ্গাব্দ • ${selectedBangla.seasonBn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Emerald100
                                    )
                                }
                            }

                            // Hijri Date Subcard
                            Surface(
                                color = Gold500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Gold400.copy(alpha = 0.45f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = tr("আরবি / হিজরি", "Arabic / Hijri", lang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Gold300
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${BengaliNumeralUtil.formatNum(selectedHijri.day, lang)} ${selectedHijri.monthNameBn}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Gold300,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${selectedHijri.monthNameAr} • ${BengaliNumeralUtil.formatNum(selectedHijri.year, lang)} হি.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (selectedHijri.islamicEventBn != null) {
                            Surface(
                                color = Gold500,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Emerald950,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = selectedHijri.islamicEventBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Emerald950,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Hijri Moon-Sighting Offset Adjustment
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = null,
                                tint = Gold600,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = tr("হিজরি চাঁদ দেখা সমন্বয় (দিন)", "Hijri Moon-Sighting Offset (Days)", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(-2, -1, 0, 1, 2).forEach { offset ->
                                val selected = settings.hijriAdjustmentDays == offset
                                val label = when {
                                    offset > 0 -> "+${BengaliNumeralUtil.formatNum(offset, lang)}"
                                    offset < 0 -> "-${BengaliNumeralUtil.formatNum(-offset, lang)}"
                                    else -> BengaliNumeralUtil.formatNum(0, lang)
                                }
                                FilterChip(
                                    selected = selected,
                                    onClick = { onHijriOffsetChange(offset) },
                                    label = {
                                        Text(
                                            text = label,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("calendar_hijri_offset_$offset")
                                )
                            }
                        }
                    }
                }
            }

            // 3. Interactive Monthly Grid
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Month Header Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("calendar_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = tr("পূর্ববর্তী মাস", "Previous Month", lang)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                val engMonthName = if (lang == AppLanguage.BENGALI) {
                                    BengaliNumeralUtil.englishMonthsBn[displayedMonth.monthValue - 1]
                                } else {
                                    BengaliNumeralUtil.englishMonthsEn[displayedMonth.monthValue - 1]
                                }
                                Text(
                                    text = "$engMonthName ${BengaliNumeralUtil.formatNum(displayedMonth.year, lang)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val banglaSpan = if (monthFirstBangla.monthNameBn == monthLastBangla.monthNameBn) {
                                    monthFirstBangla.monthNameBn
                                } else {
                                    "${monthFirstBangla.monthNameBn} – ${monthLastBangla.monthNameBn}"
                                }
                                val hijriSpan = if (monthFirstHijri.monthNameBn == monthLastHijri.monthNameBn) {
                                    monthFirstHijri.monthNameBn
                                } else {
                                    "${monthFirstHijri.monthNameBn} – ${monthLastHijri.monthNameBn}"
                                }
                                Text(
                                    text = "$banglaSpan • $hijriSpan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("calendar_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = tr("পরবর্তী মাস", "Next Month", lang)
                                )
                            }
                        }

                        if (displayedMonth != YearMonth.from(today) || selectedDate != today) {
                            TextButton(
                                onClick = {
                                    displayedMonth = YearMonth.from(today)
                                    selectedDate = today
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .testTag("calendar_go_today")
                            ) {
                                Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(tr("আজকের তারিখে যান", "Jump to Today", lang))
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Weekday Header (Sun..Sat)
                        val weekHeaders = listOf(
                            DayOfWeek.SUNDAY to tr("রবি", "Sun", lang),
                            DayOfWeek.MONDAY to tr("সোম", "Mon", lang),
                            DayOfWeek.TUESDAY to tr("মঙ্গল", "Tue", lang),
                            DayOfWeek.WEDNESDAY to tr("বুধ", "Wed", lang),
                            DayOfWeek.THURSDAY to tr("বৃহঃ", "Thu", lang),
                            DayOfWeek.FRIDAY to tr("শুক্র", "Fri", lang),
                            DayOfWeek.SATURDAY to tr("শনি", "Sat", lang)
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekHeaders.forEach { (dow, label) ->
                                val isFri = dow == DayOfWeek.FRIDAY
                                Text(
                                    text = label,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFri) FridayGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Calendar Days Grid
                        val firstDayOfMonth = displayedMonth.atDay(1)
                        // Sunday = 0, Monday = 1, ..., Saturday = 6
                        val leadingEmpty = firstDayOfMonth.dayOfWeek.value % 7
                        val daysInMonth = displayedMonth.lengthOfMonth()
                        val totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (rowStart in 0 until totalCells step 7) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (col in 0 until 7) {
                                        val cellIdx = rowStart + col
                                        val dayNum = cellIdx - leadingEmpty + 1
                                        if (dayNum in 1..daysInMonth) {
                                            val cellDate = displayedMonth.atDay(dayNum)
                                            val isToday = cellDate == today
                                            val isSelected = cellDate == selectedDate
                                            val isFriday = cellDate.dayOfWeek == DayOfWeek.FRIDAY
                                            val hijriCell = remember(cellDate, settings.hijriAdjustmentDays) {
                                                HijriCalendarConverter.fromGregorian(cellDate, settings.hijriAdjustmentDays)
                                            }
                                            val hasEvent = hijriCell.islamicEventBn != null

                                            val bgColor = when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                                isFriday -> FridayGold.copy(alpha = 0.13f)
                                                else -> Color.Transparent
                                            }
                                            val borderMod = if (isToday && !isSelected) {
                                                Modifier.border(
                                                    1.5.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(10.dp)
                                                )
                                            } else {
                                                Modifier
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 50.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(bgColor)
                                                    .then(borderMod)
                                                    .clickable { selectedDate = cellDate }
                                                    .padding(vertical = 4.dp)
                                                    .testTag("calendar_day_$dayNum"),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = BengaliNumeralUtil.formatNum(dayNum, lang),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday || isFriday) FontWeight.Bold else FontWeight.Medium,
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                                        isFriday -> FridayGold
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                Text(
                                                    text = BengaliNumeralUtil.formatNum(hijriCell.day, lang),
                                                    fontSize = 10.sp,
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                                        else -> Gold600
                                                    }
                                                )
                                                if (hasEvent) {
                                                    Spacer(Modifier.height(1.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Gold300 else Gold500)
                                                    )
                                                }
                                            }
                                        } else {
                                            Spacer(Modifier.weight(1f).height(50.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Islamic Events in Current Month (if any)
            if (monthEvents.isNotEmpty()) {
                item {
                    Text(
                        text = tr("এই মাসের ইসলামিক দিবসসমূহ", "Islamic Events This Month", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(monthEvents) { evt ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.clickable { selectedDate = evt.date }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = Gold600
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = evt.eventTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${BengaliNumeralUtil.formatGregorianDate(evt.date, lang)} • ${evt.hijriLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 5. Annual Important Islamic Calendar Dates Reference
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = tr("হিজরি বর্ষের গুরুত্বপূর্ণ দিবসসমূহ", "Key Dates of the Hijri Year", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        annualMajorEvents.forEachIndexed { idx, (hijriDateLabel, title) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = hijriDateLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (idx < annualMajorEvents.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            }
                        }
                    }
                }
            }
        }
    }
}
