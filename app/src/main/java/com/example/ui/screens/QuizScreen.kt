package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdMobBanner
import com.example.ads.AdMobManager
import com.example.data.model.AppLanguage
import com.example.data.model.QuizCategory
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizLevelInfo
import com.example.data.util.BengaliNumeralUtil
import com.example.data.util.tr
import com.example.ui.components.EmeraldGoldGradientCard
import com.example.ui.components.IslamicScreenHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuizPhase
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    uiState: QuizUiState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    when (uiState.phase) {
        QuizPhase.LOBBY -> QuizLobbyView(
            viewModel = viewModel,
            uiState = uiState,
            language = language,
            modifier = modifier
        )
        QuizPhase.PLAYING -> QuizPlayView(
            viewModel = viewModel,
            uiState = uiState,
            language = language,
            modifier = modifier
        )
        QuizPhase.RESULT -> QuizResultView(
            viewModel = viewModel,
            uiState = uiState,
            language = language,
            modifier = modifier
        )
    }
}

@Composable
private fun QuizLobbyView(
    viewModel: QuizViewModel,
    uiState: QuizUiState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicScreenHeader(
            title = tr("ইসলামিক কুইজ গেম", "Islamic Quiz Game", language),
            subtitle = tr(
                "১০০০ যাচাইকৃত প্রশ্ন • ২০টি লেভেল",
                "1000 Verified Questions • 20 Levels",
                language
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Stats & Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_quiz_banner),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Emerald950.copy(alpha = 0.65f))
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(color = Gold500, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            text = tr("জ্ঞান অর্জন অভিযান", "Knowledge Quest", language),
                                            color = Emerald950,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "${BengaliNumeralUtil.formatNum(uiState.answeredCount, language)} / ${BengaliNumeralUtil.formatNum(uiState.totalQuestionsAvailable, language)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Gold300,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    val progress = (uiState.answeredCount.toFloat() / uiState.totalQuestionsAvailable.coerceAtLeast(1)).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        color = Gold400,
                                        trackColor = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = tr(
                                                "সর্বোচ্চ স্কোর: ${BengaliNumeralUtil.toBengali(uiState.highScore)}",
                                                "High Score: ${uiState.highScore}",
                                                language
                                            ),
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = tr(
                                                "সর্বমোট সঠিক: ${BengaliNumeralUtil.toBengali(uiState.totalCorrectLifetime)}",
                                                "Total Correct: ${uiState.totalCorrectLifetime}",
                                                language
                                            ),
                                            color = Emerald100,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Daily Quiz Card
            item {
                EmeraldGoldGradientCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = tr("দৈনিক ইসলামিক কুইজ", "Daily Islamic Quiz", language),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = tr(
                                    "স্ট্রিক: ${BengaliNumeralUtil.toBengali(uiState.dailyQuizStreak)} দিন • ১০টি বাছাইকৃত প্রশ্ন",
                                    "Streak: ${uiState.dailyQuizStreak} days • 10 curated questions",
                                    language
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald100
                            )
                            if (uiState.dailyQuizCompletedToday) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = tr("আজকের দৈনিক কুইজ সম্পন্ন ✓", "Completed Today ✓", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gold300,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.startDailyQuiz() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold500,
                                contentColor = Emerald950
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("quiz_daily_start_btn")
                        ) {
                            Text(
                                text = if (uiState.dailyQuizCompletedToday) {
                                    tr("পুনরায় খেলুন", "Replay", language)
                                } else {
                                    tr("শুরু করুন", "Play", language)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Timer Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = tr("প্রশ্নের টাইমার", "Question Timer", language),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Switch(
                                checked = uiState.timerEnabled,
                                onCheckedChange = { enabled ->
                                    val sec = if (enabled && uiState.timerDurationSeconds == 0) 30 else uiState.timerDurationSeconds
                                    viewModel.setTimerOption(enabled, sec)
                                },
                                modifier = Modifier.testTag("quiz_timer_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 60, 0).forEach { sec ->
                                val selected = if (sec == 0) {
                                    !uiState.timerEnabled || uiState.timerDurationSeconds == 0
                                } else {
                                    uiState.timerEnabled && uiState.timerDurationSeconds == sec
                                }
                                val label = if (sec == 0) {
                                    tr("সীমাহীন", "None", language)
                                } else {
                                    tr("${BengaliNumeralUtil.toBengali(sec)} সে.", "${sec}s", language)
                                }
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setTimerOption(enabled = sec > 0, seconds = sec) },
                                    label = {
                                        Text(
                                            text = label,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quiz_timer_chip_$sec")
                                )
                            }
                        }
                    }
                }
            }

            // 4. Custom Practice Builder Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = tr("কাস্টম প্র্যাকটিস কুইজ", "Custom Practice Quiz", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = tr("কঠিনতার স্তর", "Difficulty Level", language),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(QuizDifficulty.entries) { diff ->
                                FilterChip(
                                    selected = uiState.selectedDifficulty == diff,
                                    onClick = { viewModel.setDifficulty(diff) },
                                    label = { Text(if (language == AppLanguage.BENGALI) diff.bnName else diff.enName) },
                                    modifier = Modifier.testTag("quiz_diff_${diff.code}")
                                )
                            }
                        }

                        Text(
                            text = tr("বিষয় নির্বাচন", "Select Category", language),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(QuizCategory.entries) { cat ->
                                FilterChip(
                                    selected = uiState.selectedCategory == cat,
                                    onClick = { viewModel.setCategory(cat) },
                                    label = { Text(if (language == AppLanguage.BENGALI) cat.bnName else cat.enName) },
                                    modifier = Modifier.testTag("quiz_cat_${cat.code}")
                                )
                            }
                        }

                        Text(
                            text = tr("প্রশ্নের সংখ্যা", "Number of Questions", language),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(10, 15, 20, 30).forEach { cnt ->
                                FilterChip(
                                    selected = uiState.questionCountChoice == cnt,
                                    onClick = { viewModel.setQuestionCount(cnt) },
                                    label = {
                                        Text(
                                            text = BengaliNumeralUtil.formatNum(cnt, language),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quiz_count_$cnt")
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { viewModel.startPracticeQuiz() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("quiz_start_practice_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = tr("কুইজ শুরু করুন", "Start Practice Quiz", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 5. 20 Progressive Levels
            item {
                Text(
                    text = tr("২০টি ধারাবাহিক লেভেল (১–১০০০ প্রশ্ন)", "20 Progressive Levels (1–1000 Qs)", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(uiState.levels.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { level ->
                        QuizLevelCard(
                            level = level,
                            language = language,
                            onClick = {
                                if (level.unlocked) viewModel.startLevelQuiz(level.levelNumber)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        AdMobBanner(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun QuizLevelCard(
    level: QuizLevelInfo,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(enabled = level.unlocked, onClick = onClick)
            .testTag("quiz_level_${level.levelNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (level.unlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        border = if (level.bestPercentage >= 50) {
            BorderStroke(1.5.dp, SuccessGreen.copy(alpha = 0.7f))
        } else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = level.titleBn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (level.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = when {
                        !level.unlocked -> Icons.Default.Lock
                        level.bestPercentage >= 50 -> Icons.Default.CheckCircle
                        else -> Icons.Default.PlayCircle
                    },
                    contentDescription = null,
                    tint = when {
                        !level.unlocked -> MaterialTheme.colorScheme.onSurfaceVariant
                        level.bestPercentage >= 50 -> SuccessGreen
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = if (language == AppLanguage.BENGALI) level.difficulty.bnName else level.difficulty.enName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (level.bestPercentage > 0) {
                    tr("সেরা: ${BengaliNumeralUtil.toBengali(level.bestPercentage)}%", "Best: ${level.bestPercentage}%", language)
                } else if (level.unlocked) {
                    tr("${BengaliNumeralUtil.toBengali(level.questionCount)}টি প্রশ্ন • খেলুন", "${level.questionCount} Qs • Play", language)
                } else {
                    tr("পূর্বের লেভেলে ৫০% পান", "Score 50% in prev level", language)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (level.bestPercentage >= 50) SuccessGreen else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun QuizPlayView(
    viewModel: QuizViewModel,
    uiState: QuizUiState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.exitToLobby() }
    val question = uiState.currentQuestion ?: return
    val total = uiState.sessionQuestions.size.coerceAtLeast(1)
    val progress = (uiState.currentIndex + 1).toFloat() / total
    val optionLabels = listOf("ক", "খ", "গ", "ঘ")

    val categoryName = QuizCategory.entries.find { it.code == question.cat }?.let {
        if (language == AppLanguage.BENGALI) it.bnName else it.enName
    } ?: question.cat
    val diffName = QuizDifficulty.entries.find { it.code == question.level }?.let {
        if (language == AppLanguage.BENGALI) it.bnName else it.enName
    } ?: question.level

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.exitToLobby() },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("quiz_exit_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = tr("প্রস্থান", "Exit", language))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = tr(
                        "প্রশ্ন ${BengaliNumeralUtil.toBengali(uiState.currentIndex + 1)} / ${BengaliNumeralUtil.toBengali(total)}",
                        "Question ${uiState.currentIndex + 1} / $total",
                        language
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.activeLevelNumber != null) {
                    Text(
                        text = tr("লেভেল ${BengaliNumeralUtil.toBengali(uiState.activeLevelNumber)}", "Level ${uiState.activeLevelNumber}", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Stars,
                        contentDescription = null,
                        tint = Gold600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = BengaliNumeralUtil.formatNum(uiState.sessionScore, language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            color = Gold500,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Timer + Metadata Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = diffName,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (uiState.timerEnabled && uiState.timerDurationSeconds > 0) {
                        val timerFraction = (uiState.remainingSeconds.toFloat() / uiState.timerDurationSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val isLowTime = uiState.remainingSeconds <= 6
                        val ringColor = if (isLowTime) ErrorCrimson else Emerald600
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = ringColor.copy(alpha = 0.2f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * timerFraction,
                                    useCenter = false,
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = BengaliNumeralUtil.formatNum(uiState.remainingSeconds, language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ringColor
                            )
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = tr("সীমাহীন সময়", "No Limit", language),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Question Card
            item {
                EmeraldGoldGradientCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = question.q,
                        style = MaterialTheme.typography.titleLarge,
                        lineHeight = 30.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            // 4 Answer Options
            items(question.opts.size) { index ->
                val optionText = question.opts[index]
                val revealed = uiState.isAnswerRevealed
                val isSelected = uiState.selectedOptionIndex == index
                val isCorrectOption = index == question.ans

                val containerColor = when {
                    revealed && isCorrectOption -> SuccessGreen.copy(alpha = 0.18f)
                    revealed && isSelected && !isCorrectOption -> ErrorCrimson.copy(alpha = 0.18f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    revealed && isCorrectOption -> SuccessGreen
                    revealed && isSelected && !isCorrectOption -> ErrorCrimson
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                }

                Surface(
                    onClick = { viewModel.selectAnswer(index) },
                    enabled = !revealed && !uiState.timeExpired,
                    shape = RoundedCornerShape(14.dp),
                    color = containerColor,
                    border = BorderStroke(1.5.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("quiz_option_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        revealed && isCorrectOption -> SuccessGreen
                                        revealed && isSelected && !isCorrectOption -> ErrorCrimson
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optionLabels.getOrElse(index) { "${index + 1}" },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (revealed && (isCorrectOption || isSelected)) {
                                    Color.White
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (revealed && isCorrectOption) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        } else if (revealed && isSelected && !isCorrectOption) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = ErrorCrimson)
                        }
                    }
                }
            }

            // Feedback Banner + Correct Answer Reveal
            if (uiState.isAnswerRevealed && uiState.feedbackBanner != null) {
                val answeredCorrect = uiState.selectedOptionIndex == question.ans
                item {
                    Surface(
                        color = if (answeredCorrect) SuccessGreen.copy(alpha = 0.14f) else ErrorCrimson.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (answeredCorrect) SuccessGreen else ErrorCrimson),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = uiState.feedbackBanner,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (answeredCorrect) SuccessGreen else ErrorCrimson
                            )
                            if (!answeredCorrect) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = tr(
                                        "সঠিক উত্তর: ${question.opts.getOrElse(question.ans) { "" }}",
                                        "Correct Answer: ${question.opts.getOrElse(question.ans) { "" }}",
                                        language
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Next Button
        if (uiState.isAnswerRevealed) {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.nextQuestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .heightIn(min = 52.dp)
                        .testTag("quiz_next_question_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    val isLast = uiState.currentIndex + 1 >= total
                    Text(
                        text = if (isLast) {
                            tr("ফলাফল দেখুন", "View Results", language)
                        } else {
                            tr("পরবর্তী প্রশ্ন", "Next Question", language)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun QuizResultView(
    viewModel: QuizViewModel,
    uiState: QuizUiState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.exitToLobby() }
    val activity = LocalContext.current as? Activity
    LaunchedEffect(Unit) {
        AdMobManager.showInterstitialOnGameEntry(activity)
    }
    val total = uiState.sessionQuestions.size.coerceAtLeast(1)
    val pct = (uiState.sessionCorrectCount * 100) / total
    val praiseMsg = when {
        pct >= 80 -> tr("মাশাআল্লাহ! অসাধারণ ফলাফল!", "MashaAllah! Outstanding Result!", language)
        pct >= 50 -> tr("আলহামদুলিল্লাহ! খুব ভালো হয়েছে!", "Alhamdulillah! Well Done!", language)
        else -> tr("ইনশাআল্লাহ, অনুশীলন চালিয়ে যান!", "Keep practicing, InshaAllah you'll excel!", language)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmeraldGoldGradientCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Gold400,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = praiseMsg,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = Gold500,
                    shape = CircleShape,
                    modifier = Modifier.size(92.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${BengaliNumeralUtil.formatNum(pct, language)}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald950
                        )
                    }
                }

                HorizontalDivider(color = Gold400.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ResultStatPill(tr("মোট", "Total", language), BengaliNumeralUtil.formatNum(total, language), Color.White)
                    ResultStatPill(tr("সঠিক", "Correct", language), BengaliNumeralUtil.formatNum(uiState.sessionCorrectCount, language), Emerald200)
                    ResultStatPill(tr("ভুল", "Wrong", language), BengaliNumeralUtil.formatNum(uiState.sessionWrongCount, language), Gold300)
                }

                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tr(
                                "মোট স্কোর: ${BengaliNumeralUtil.toBengali(uiState.sessionScore)} (দ্রুত উত্তর বোনাস: +${BengaliNumeralUtil.toBengali(uiState.sessionBonusPoints)})",
                                "Total Score: ${uiState.sessionScore} (Speed Bonus: +${uiState.sessionBonusPoints})",
                                language
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gold300,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        val nextLevel = uiState.activeLevelNumber
        if (nextLevel != null && nextLevel < 20 && pct >= 50) {
            Button(
                onClick = { viewModel.startLevelQuiz(nextLevel + 1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("quiz_result_next_level_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = tr(
                        "পরবর্তী লেভেল (${BengaliNumeralUtil.toBengali(nextLevel + 1)}) খেলুন",
                        "Play Next Level (${nextLevel + 1})",
                        language
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        FilledTonalButton(
            onClick = {
                when {
                    uiState.isDailyQuizSession -> viewModel.startDailyQuiz()
                    uiState.activeLevelNumber != null -> viewModel.startLevelQuiz(uiState.activeLevelNumber)
                    else -> viewModel.startPracticeQuiz()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("quiz_result_replay_btn"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(tr("আবার খেলুন", "Play Again", language), fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.exitToLobby() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("quiz_result_lobby_btn"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(tr("লবিতে ফিরুন", "Back to Lobby", language))
        }
    }
}

@Composable
private fun ResultStatPill(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Emerald100)
    }
}
