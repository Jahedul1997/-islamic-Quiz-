package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.seed.QuranOfflineBundledData
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.tr
import com.example.ui.components.IslamicScreenHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.TasbihUiState
import com.example.ui.viewmodel.TasbihViewModel

@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel,
    uiState: TasbihUiState,
    language: AppLanguage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf(uiState.targetCount.toString()) }
    var tapBounce by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (tapBounce) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        finishedListener = { tapBounce = false },
        label = "tasbihScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = tr("ডিজিটাল তাসবিহ", "Digital Tasbih", language),
            subtitle = tr("অফলাইন গণনা ও দৈনিক হিসাব", "Offline Counter & Daily History", language),
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = { viewModel.toggleVibration(!uiState.vibrationEnabled) },
                    modifier = Modifier.size(44.dp).testTag("tasbih_vibration_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "কম্পন",
                        tint = if (uiState.vibrationEnabled) Gold300 else Color.White.copy(alpha = 0.4f)
                    )
                }
                IconButton(
                    onClick = { viewModel.toggleSound(!uiState.soundEnabled) },
                    modifier = Modifier.size(44.dp).testTag("tasbih_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "সাউন্ড",
                        tint = if (uiState.soundEnabled) Gold300 else Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dhikr Selector Chips
            item {
                Text(
                    text = tr("তাসবিহ নির্বাচন করুন", "Select Dhikr", language),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QuranOfflineBundledData.tasbihList, key = { it.id }) { dhikr ->
                        FilterChip(
                            selected = uiState.selectedDhikr.id == dhikr.id,
                            onClick = { viewModel.selectDhikr(dhikr) },
                            label = { Text(dhikr.titleBn) },
                            modifier = Modifier.testTag("tasbih_dhikr_${dhikr.id}")
                        )
                    }
                }
            }

            // Active Dhikr Arabic & Meaning Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.selectedDhikr.arabic,
                            fontFamily = AmiriArabicFontFamily,
                            fontSize = 30.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = uiState.selectedDhikr.titleBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.selectedDhikr.meaningBn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Target Presets (33, 99, 100, Custom)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(33, 99, 100).forEach { target ->
                        FilterChip(
                            selected = uiState.targetCount == target,
                            onClick = { viewModel.setTarget(target) },
                            label = { Text(BengaliNumeralUtil.formatNum(target, language)) },
                            modifier = Modifier.weight(1f).testTag("tasbih_target_$target")
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            customInput = uiState.targetCount.toString()
                            showCustomTargetDialog = true
                        },
                        modifier = Modifier.weight(1.3f).testTag("tasbih_custom_target_btn")
                    ) {
                        Text(tr("কাস্টম", "Custom", language), maxLines = 1)
                    }
                }
            }

            // Giant Tactile Circular Counter
            item {
                val progress = (uiState.currentCount.toFloat() / uiState.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .clickable {
                                tapBounce = true
                                viewModel.increment()
                            }
                            .testTag("tasbih_tap_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 14.dp.toPx()
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Emerald800, Emerald950)),
                                radius = size.minDimension / 2f - stroke
                            )
                            drawCircle(
                                color = Gold400.copy(alpha = 0.22f),
                                radius = size.minDimension / 2f - stroke / 2f,
                                style = Stroke(width = stroke)
                            )
                            drawArc(
                                color = if (uiState.milestoneReached) SuccessGreen else Gold400,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = BengaliNumeralUtil.formatNum(uiState.currentCount, language),
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold300
                            )
                            Text(
                                text = "/ ${BengaliNumeralUtil.formatNum(uiState.targetCount, language)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Emerald100
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (uiState.milestoneReached) tr("লক্ষ্য পূর্ণ! ✓", "Target Complete! ✓", language) else tr("ট্যাপ করুন", "Tap to Count", language),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.milestoneReached) Color(0xFF7BF2B0) else Gold300
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetCounter() },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("tasbih_reset_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(tr("রিসেট", "Reset", language))
                        }
                    }
                }
            }

            // Daily & Lifetime Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(tr("আজকের মোট", "Today's Total", language), style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = BengaliNumeralUtil.formatNum(uiState.todayTotal, language),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(tr("সর্বমোট হিসাব", "Lifetime Total", language), style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = BengaliNumeralUtil.formatNum(uiState.lifetimeTotal, language),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Today's breakdown
            if (uiState.recentLogs.isNotEmpty()) {
                item {
                    Text(
                        text = tr("দৈনিক তাসবিহ ইতিহাস", "Daily Dhikr History", language),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.recentLogs.take(15)) { log ->
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.dhikrTitleBn, style = MaterialTheme.typography.titleSmall)
                                Text(BengaliNumeralUtil.formatDigits(log.dateIso, language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "${BengaliNumeralUtil.formatNum(log.count, language)} বার",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomTargetDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = { Text(tr("নিজের লক্ষ্য নির্ধারণ করুন", "Set Custom Target", language)) },
            text = {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) customInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text(tr("সংখ্যা লিখুন (যেমন ৫০০)", "Enter target count", language)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val v = customInput.toIntOrNull() ?: 33
                    viewModel.setTarget(v)
                    showCustomTargetDialog = false
                }) {
                    Text(tr("সংরক্ষণ", "Save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) {
                    Text(tr("বাতিল", "Cancel", language))
                }
            }
        )
    }
}
