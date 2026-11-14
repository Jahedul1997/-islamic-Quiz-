package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.data.seed.DistrictsData
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.tr
import com.example.ui.components.EmeraldGoldGradientCard
import com.example.ui.components.IslamicScreenHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppStateViewModel
import com.example.ui.viewmodel.DashboardUiState

@Composable
fun PrayerTimesScreen(
    viewModel: AppStateViewModel,
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = uiState.settings
    val lang = settings.language
    val times = uiState.prayerTimes
    val next = uiState.nextPrayerStatus

    var showDistrictPicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.detectGpsLocation()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = tr("নামাজের সময়সূচি ও আজান", "Prayer Times & Adhan", lang),
            subtitle = "${settings.calculationMethod.bnName} • ${settings.asrMadhab.bnName}",
            actions = {
                IconButton(
                    onClick = { viewModel.refreshPrayerTimesNow() },
                    modifier = Modifier.size(48.dp).testTag("prayer_refresh_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "আপডেট", tint = Gold300)
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Next Prayer Hero Card
            item {
                EmeraldGoldGradientCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (lang == AppLanguage.BENGALI) times?.locationNameBn ?: "ঢাকা" else times?.locationNameEn ?: "Dhaka",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Gold300
                                )
                                val nextName = if (lang == AppLanguage.BENGALI) next?.nextPrayer?.bnName ?: "ফজর" else next?.nextPrayer?.enName ?: "Fajr"
                                Text(
                                    text = tr("পরবর্তী ওয়াক্ত: $nextName", "Next: $nextName", lang),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                val sourceText = when (times?.source) {
                                    "online" -> tr("সর্বশেষ অনলাইন সিঙ্কড ✓", "Online Synced ✓", lang)
                                    "cached" -> tr("সংরক্ষিত সময়সূচি (অফলাইন)", "Cached Offline", lang)
                                    else -> tr("অফলাইন জ্যোতির্বিজ্ঞান গণনা ✓", "Offline Calculation ✓", lang)
                                }
                                Text(sourceText, style = MaterialTheme.typography.labelSmall, color = Emerald100)
                            }

                            val rem = next?.remainingSeconds ?: 0L
                            val countdown = String.format("%02d:%02d:%02d", rem / 3600, (rem % 3600) / 60, rem % 60)
                            Surface(
                                color = Gold500,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(tr("বাকি", "In", lang), style = MaterialTheme.typography.labelSmall, color = Emerald950)
                                    Text(
                                        text = BengaliNumeralUtil.formatDigits(countdown, lang),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald950
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showDistrictPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.16f), contentColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("prayer_district_picker_btn")
                            ) {
                                Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(tr("জেলা নির্বাচন", "District", lang), maxLines = 1)
                            }
                            Button(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (granted) viewModel.detectGpsLocation()
                                    else locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold500, contentColor = Emerald950),
                                modifier = Modifier.weight(1f).testTag("prayer_gps_btn")
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(tr("GPS লোকেশন", "Use GPS", lang), fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Notification Permission Notice for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notifGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (!notifGranted) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f))) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tr("আজান ও নামাজের নোটিফিকেশন পেতে অনুমতি দিন", "Allow notifications for Prayer & Adhan alerts", lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                    Text(tr("অনুমতি", "Allow", lang))
                                }
                            }
                        }
                    }
                }
            }

            // Master Adhan Sound Toggle & Test Audio
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tr("আজানের অডিও চালু রাখুন", "Enable Adhan Audio", lang), style = MaterialTheme.typography.titleMedium)
                                Text(tr("বন্ধ রাখলে শুধু সাইলেন্ট নোটিফিকেশন আসবে", "If off, only silent notifications will show", lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.adhanMasterEnabled,
                                onCheckedChange = { viewModel.setAdhanMaster(it) },
                                modifier = Modifier.testTag("prayer_adhan_master_switch")
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.testAdhanNow() },
                                modifier = Modifier.weight(1f).testTag("prayer_test_adhan_btn")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(tr("আজান শুনুন (টেস্ট)", "Test Adhan", lang))
                            }
                            OutlinedButton(
                                onClick = { viewModel.stopAdhanNow() },
                                modifier = Modifier.weight(1f).testTag("prayer_stop_adhan_btn")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(tr("বন্ধ করুন", "Stop", lang))
                            }
                        }
                    }
                }
            }

            // 5 Prayers + Sunrise Cards with Per-Prayer Alert Mode
            if (times != null) {
                val itemsList = listOf(
                    PrayerType.FAJR to times.fajr,
                    PrayerType.SUNRISE to times.sunrise,
                    PrayerType.DHUHR to times.dhuhr,
                    PrayerType.ASR to times.asr,
                    PrayerType.MAGHRIB to times.maghrib,
                    PrayerType.ISHA to times.isha
                )
                items(itemsList, key = { it.first.key }) { (prayerType, timeStr) ->
                    val isNext = next?.nextPrayer == prayerType
                    val mode = settings.alertModeFor(prayerType)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (lang == AppLanguage.BENGALI) prayerType.bnName else prayerType.enName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isNext) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(color = Gold500, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = tr("পরবর্তী", "Next", lang),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Emerald950,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (prayerType != PrayerType.SUNRISE) {
                                    Text(
                                        text = if (lang == AppLanguage.BENGALI) mode.bnName else mode.enName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = tr("ফজরের ওয়াক্ত সমাপ্তি", "Fajr ends", lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = BengaliNumeralUtil.formatTime12h(timeStr, lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (prayerType != PrayerType.SUNRISE) {
                                    Spacer(Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            val nextMode = when (mode) {
                                                PrayerAlertMode.ADHAN -> PrayerAlertMode.NOTIFICATION
                                                PrayerAlertMode.NOTIFICATION -> PrayerAlertMode.OFF
                                                PrayerAlertMode.OFF -> PrayerAlertMode.ADHAN
                                            }
                                            viewModel.setPrayerAlertMode(prayerType, nextMode)
                                        },
                                        modifier = Modifier.size(48.dp).testTag("prayer_alert_toggle_${prayerType.key}")
                                    ) {
                                        Icon(
                                            imageVector = when (mode) {
                                                PrayerAlertMode.ADHAN -> Icons.Default.NotificationsActive
                                                PrayerAlertMode.NOTIFICATION -> Icons.Default.Notifications
                                                PrayerAlertMode.OFF -> Icons.Default.NotificationsOff
                                            },
                                            contentDescription = "আজান মোড পরিবর্তন",
                                            tint = when (mode) {
                                                PrayerAlertMode.ADHAN -> Gold600
                                                PrayerAlertMode.NOTIFICATION -> MaterialTheme.colorScheme.primary
                                                PrayerAlertMode.OFF -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
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

    if (showDistrictPicker) {
        DistrictPickerDialog(
            selectedId = settings.selectedDistrictId,
            language = lang,
            onSelect = {
                viewModel.selectDistrict(it.id)
                showDistrictPicker = false
            },
            onDismiss = { showDistrictPicker = false }
        )
    }
}

@Composable
fun DistrictPickerDialog(
    selectedId: String,
    language: AppLanguage,
    onSelect: (DistrictLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) DistrictsData.allDistricts
        else DistrictsData.allDistricts.filter {
            it.bnName.lowercase().contains(q) || it.enName.lowercase().contains(q) || it.divisionBn.contains(q)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("জেলা নির্বাচন করুন (৬৪ জেলা)", "Select District (64)", language)) },
        text = {
            Column(modifier = Modifier.heightIn(max = 420.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(tr("জেলার নাম লিখুন...", "Search district...", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filtered, key = { it.id }) { dist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(dist) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${dist.bnName} (${dist.enName})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (dist.id == selectedId) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${dist.divisionBn} বিভাগ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (dist.id == selectedId) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("বন্ধ করুন", "Close", language))
            }
        }
    )
}
