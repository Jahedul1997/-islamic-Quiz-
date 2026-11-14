package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.tr
import com.example.ui.components.IslamicScreenHeader
import com.example.ui.viewmodel.AppStateViewModel
import com.example.ui.viewmodel.DashboardUiState
import com.example.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: AppStateViewModel,
    quizViewModel: QuizViewModel,
    uiState: DashboardUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = uiState.settings
    val lang = settings.language

    var showDistrictDialog by remember { mutableStateOf(false) }
    var showCalcMethodDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.detectGpsLocation()
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.exportBackupJson()
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                }.onSuccess {
                    Toast.makeText(context, tr("ব্যাকআপ সফলভাবে সংরক্ষিত হয়েছে!", "Backup saved!", lang), Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, tr("ব্যাকআপ সংরক্ষণ ব্যর্থ হয়েছে", "Failed to save backup", lang), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val raw = runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
                val ok = if (raw != null) viewModel.importBackupJson(raw) else false
                Toast.makeText(
                    context,
                    if (ok) tr("ব্যাকআপ পুনরুদ্ধার সম্পন্ন!", "Backup restored!", lang) else tr("অবৈধ ব্যাকআপ ফাইল", "Invalid backup file", lang),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = tr("সেটিংস", "Settings", lang),
            subtitle = tr("ভাষা, থিম, আজান, কুইজ ও ব্যাকআপ", "Language, Theme, Adhan, Quiz & Backup", lang),
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Language & Theme
            item {
                SettingsSectionCard(title = tr("ভাষা ও থিম", "Language & Theme", lang)) {
                    Text(tr("অ্যাপের ভাষা", "App Language", lang), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { option ->
                            FilterChip(
                                selected = settings.language == option,
                                onClick = { viewModel.setLanguage(option) },
                                label = { Text(option.displayName) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_lang_${option.code}")
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(tr("অ্যাপ থিম", "App Theme", lang), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val themes = listOf(
                            ThemePreference.SYSTEM to tr("সিস্টেম", "System", lang),
                            ThemePreference.LIGHT to tr("লাইট", "Light", lang),
                            ThemePreference.DARK to tr("ডার্ক", "Dark", lang)
                        )
                        themes.forEach { (pref, label) ->
                            FilterChip(
                                selected = settings.theme == pref,
                                onClick = { viewModel.setTheme(pref) },
                                label = { Text(label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_theme_${pref.code}")
                            )
                        }
                    }
                }
            }

            // 2. Sound & Voice Feedback
            item {
                SettingsSectionCard(title = tr("সাউন্ড ও ভয়েস ফিডব্যাক", "Sound & Voice", lang)) {
                    SettingsSwitchRow(
                        title = tr("কুইজ সাউন্ড এফেক্ট", "Quiz Sound Effects", lang),
                        subtitle = tr("সঠিক/ভুল উত্তরে সাউন্ড বাজাবে", "Play applause/sad sound on answers", lang),
                        checked = settings.soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) },
                        testTag = "settings_sound_switch"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    SettingsSwitchRow(
                        title = tr("বাংলা ভয়েস ফিডব্যাক", "Bengali Voice Feedback", lang),
                        subtitle = tr("সঠিক উত্তরে \"জাযাকাল্লাহু খাইরান\" বলবে", "Speak praise/encouragement in Bengali", lang),
                        checked = settings.voiceFeedbackEnabled,
                        onCheckedChange = { viewModel.setVoiceFeedback(it) },
                        testTag = "settings_voice_switch"
                    )
                }
            }

            // 3. Prayer Location & Calculation
            item {
                SettingsSectionCard(title = tr("নামাজের সময় ও অবস্থান", "Prayer Location & Calculation", lang)) {
                    val currentLocationLabel = if (settings.useGpsLocation) {
                        "${settings.gpsLocationName} (GPS)"
                    } else {
                        uiState.prayerTimes?.locationNameBn ?: "ঢাকা"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDistrictDialog = true }
                            .padding(vertical = 6.dp)
                            .testTag("settings_district_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("বর্তমান অবস্থান / জেলা", "Current Location / District", lang), style = MaterialTheme.typography.titleSmall)
                            Text(currentLocationLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showDistrictDialog = true },
                            modifier = Modifier.weight(1f).testTag("settings_pick_district_btn")
                        ) {
                            Text(tr("জেলা বাছুন", "Pick District", lang))
                        }
                        Button(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (granted) viewModel.detectGpsLocation()
                                else locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            },
                            modifier = Modifier.weight(1f).testTag("settings_gps_btn")
                        ) {
                            Text(tr("GPS ব্যবহার", "Use GPS", lang))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCalcMethodDialog = true }
                            .padding(vertical = 6.dp)
                            .testTag("settings_calc_method_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("হিসাব পদ্ধতি (Calculation)", "Calculation Method", lang), style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (lang == AppLanguage.BENGALI) settings.calculationMethod.bnName else settings.calculationMethod.enName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(tr("আসর মাজহাব", "Asr Madhab", lang), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AsrMadhab.entries.forEach { madhab ->
                            FilterChip(
                                selected = settings.asrMadhab == madhab,
                                onClick = { viewModel.setAsrMadhab(madhab) },
                                label = { Text(if (lang == AppLanguage.BENGALI) madhab.bnName else madhab.enName) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_madhab_${madhab.id}")
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(tr("হিজরি তারিখ সমন্বয় (দিন)", "Hijri Offset (Days)", lang), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-2, -1, 0, 1, 2).forEach { off ->
                            FilterChip(
                                selected = settings.hijriAdjustmentDays == off,
                                onClick = { viewModel.setHijriAdjustment(off) },
                                label = { Text(if (off > 0) "+$off" else "$off") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Adhan & Notifications
            item {
                SettingsSectionCard(title = tr("আজান ও নোটিফিকেশন", "Adhan & Notifications", lang)) {
                    SettingsSwitchRow(
                        title = tr("আজানের অডিও মাস্টার সুইচ", "Adhan Master Audio", lang),
                        subtitle = tr("নির্ধারিত নামাজের সময় আজান বাজাবে", "Play Adhan audio at scheduled prayer times", lang),
                        checked = settings.adhanMasterEnabled,
                        onCheckedChange = { viewModel.setAdhanMaster(it) },
                        testTag = "settings_adhan_switch"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    SettingsSwitchRow(
                        title = tr("দৈনিক হাদিস ও দোয়া নোটিফিকেশন", "Daily Hadith & Dua Alert", lang),
                        subtitle = tr("সকাল ও সন্ধ্যায় ইসলামিক বার্তা", "Daily reminder notifications", lang),
                        checked = settings.dailyNotificationsEnabled,
                        onCheckedChange = { viewModel.setDailyNotifications(it) },
                        testTag = "settings_daily_notif_switch"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    SettingsSwitchRow(
                        title = tr("ইসলামিক দিবস রিমাইন্ডার", "Islamic Event Reminders", lang),
                        subtitle = tr("রমজান, ঈদ, শবে বরাত ইত্যাদি স্মরণ", "Remind on special Hijri dates", lang),
                        checked = settings.islamicEventRemindersEnabled,
                        onCheckedChange = { viewModel.setEventReminders(it) },
                        testTag = "settings_event_notif_switch"
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { viewModel.testAdhanNow() },
                            modifier = Modifier.weight(1f).testTag("settings_test_adhan_btn")
                        ) {
                            Text(tr("টেস্ট আজান", "Test Adhan", lang))
                        }
                        FilledTonalButton(
                            onClick = { viewModel.testDailyNotificationNow() },
                            modifier = Modifier.weight(1f).testTag("settings_test_notif_btn")
                        ) {
                            Text(tr("টেস্ট নোটিফিকেশন", "Test Alert", lang))
                        }
                    }
                }
            }

            // 5. Quiz Defaults
            item {
                SettingsSectionCard(title = tr("কুইজ টাইমার সেটিংস", "Quiz Timer Settings", lang)) {
                    SettingsSwitchRow(
                        title = tr("টাইমার সক্রিয় রাখুন", "Enable Quiz Timer", lang),
                        subtitle = tr("দ্রুত উত্তর দিলে অতিরিক্ত বোনাস পয়েন্ট", "Earn speed bonus for fast answers", lang),
                        checked = settings.quizTimerEnabled,
                        onCheckedChange = { viewModel.setQuizTimerEnabled(it) },
                        testTag = "settings_quiz_timer_switch"
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 60, 0).forEach { sec ->
                            val label = if (sec == 0) tr("বন্ধ", "Off", lang) else "${BengaliNumeralUtil.formatNum(sec, lang)}s"
                            FilterChip(
                                selected = settings.quizTimerSeconds == sec,
                                onClick = {
                                    viewModel.setQuizTimerEnabled(sec > 0)
                                    viewModel.setQuizTimerSeconds(sec)
                                },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 6. Backup & Reset
            item {
                SettingsSectionCard(title = tr("ব্যাকআপ ও ডাটা", "Backup & Data", lang)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { exportBackupLauncher.launch("islamic_quiz_backup.json") },
                            modifier = Modifier.weight(1f).testTag("settings_export_btn")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(tr("ব্যাকআপ সেভ", "Export", lang))
                        }
                        OutlinedButton(
                            onClick = { importBackupLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f).testTag("settings_import_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(tr("রিস্টোর", "Import", lang))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("settings_reset_quiz_btn")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(tr("কুইজ প্রগ্রেস রিসেট করুন", "Reset Quiz Progress", lang))
                    }
                }
            }
        }
    }

    if (showDistrictDialog) {
        DistrictPickerDialog(
            selectedId = settings.selectedDistrictId,
            language = lang,
            onSelect = {
                viewModel.selectDistrict(it.id)
                showDistrictDialog = false
            },
            onDismiss = { showDistrictDialog = false }
        )
    }

    if (showCalcMethodDialog) {
        AlertDialog(
            onDismissRequest = { showCalcMethodDialog = false },
            title = { Text(tr("নামাজের হিসাব পদ্ধতি", "Prayer Calculation Method", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalculationMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCalculationMethod(method)
                                    showCalcMethodDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.calculationMethod == method,
                                onClick = {
                                    viewModel.setCalculationMethod(method)
                                    showCalcMethodDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (lang == AppLanguage.BENGALI) method.bnName else method.enName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalcMethodDialog = false }) {
                    Text(tr("বন্ধ", "Close", lang))
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(tr("কুইজ প্রগ্রেস রিসেট?", "Reset Quiz Progress?", lang)) },
            text = { Text(tr("আপনার কুইজ লেভেল ও উত্তর দেওয়ার ইতিহাস মুছে যাবে। আপনি কি নিশ্চিত?", "This will clear all answered quiz history and level progress. Continue?", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        quizViewModel.resetAllProgress()
                        showResetConfirm = false
                        Toast.makeText(context, tr("কুইজ প্রগ্রেস রিসেট হয়েছে", "Quiz progress reset", lang), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(tr("রিসেট", "Reset", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(tr("বাতিল", "Cancel", lang))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
