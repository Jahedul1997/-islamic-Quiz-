package com.example.data.repository

import android.content.Context
import com.example.data.local.QuizAnsweredEntity
import com.example.data.local.QuizDao
import com.example.data.local.QuizLevelProgressEntity
import com.example.data.local.SettingsDataStore
import com.example.data.model.QuizCategory
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizLevelInfo
import com.example.data.model.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.LocalDate
import kotlin.random.Random

class QuizRepository(
    private val context: Context,
    private val quizDao: QuizDao,
    private val settingsDataStore: SettingsDataStore
) {
    private var cachedQuestions: List<QuizQuestion>? = null

    val answeredIdsFlow: Flow<List<Int>> = quizDao.getAnsweredQuestionIds()
    val totalCorrectFlow: Flow<Int> = quizDao.getTotalCorrectCount()
    val levelProgressFlow: Flow<List<QuizLevelProgressEntity>> = quizDao.getLevelProgress()

    suspend fun getAllQuestions(): List<QuizQuestion> = withContext(Dispatchers.IO) {
        cachedQuestions?.let { return@withContext it }
        val assetFiles = listOf(
            "quiz_1_easy.json",
            "quiz_2_medium.json",
            "quiz_3_hard_a.json",
            "quiz_4_hard_expert.json",
            "quiz_5_expert.json"
        )
        val loaded = ArrayList<QuizQuestion>(1000)
        for (file in assetFiles) {
            val text = context.assets.open(file).bufferedReader(Charsets.UTF_8).use { it.readText() }
            val arr = JSONArray(text)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val optsArr = obj.getJSONArray("opts")
                val opts = List(optsArr.length()) { idx -> optsArr.getString(idx) }
                loaded.add(
                    QuizQuestion(
                        id = obj.getInt("id"),
                        level = obj.getString("level"),
                        cat = obj.getString("cat"),
                        q = obj.getString("q"),
                        opts = opts,
                        ans = obj.getInt("ans")
                    )
                )
            }
        }
        cachedQuestions = loaded.sortedBy { it.id }
        cachedQuestions!!
    }

    /**
     * Returns up to [count] questions avoiding already-answered IDs where possible.
     */
    suspend fun getSessionQuestions(
        difficulty: QuizDifficulty,
        category: QuizCategory,
        count: Int = 15,
        levelNumber: Int? = null
    ): List<QuizQuestion> {
        val all = getAllQuestions()
        if (levelNumber != null) {
            val startIdx = (levelNumber - 1) * 50
            val slice = all.drop(startIdx).take(50)
            return slice.take(count.coerceAtMost(slice.size))
        }

        val answeredSet = quizDao.getAnsweredQuestionIdsOnce().toSet()
        val filtered = all.filter { q ->
            val diffMatch = difficulty == QuizDifficulty.ALL || q.id in difficulty.idRange
            val catMatch = category == QuizCategory.ALL || q.cat == category.code
            diffMatch && catMatch
        }

        val unseen = filtered.filter { it.id !in answeredSet }.shuffled()
        return if (unseen.size >= count) {
            unseen.take(count)
        } else {
            val seenFallback = filtered.filter { it.id in answeredSet }.shuffled()
            (unseen + seenFallback).take(count)
        }
    }

    suspend fun getDailyQuizQuestions(date: LocalDate = LocalDate.now()): List<QuizQuestion> {
        val all = getAllQuestions()
        val seed = date.year * 10000L + date.monthValue * 100L + date.dayOfMonth
        return all.shuffled(Random(seed)).take(10)
    }

    suspend fun recordSessionResults(
        results: List<Pair<Int, Boolean>>,
        totalScore: Int,
        percentage: Int,
        levelNumber: Int?,
        isDailyQuiz: Boolean
    ) {
        if (results.isNotEmpty()) {
            quizDao.markAnswered(results.map { (id, ok) -> QuizAnsweredEntity(id, ok) })
        }
        settingsDataStore.updateHighScoreIfBetter(totalScore)

        if (levelNumber != null) {
            val existing = quizDao.getLevelProgress().first().find { it.levelNumber == levelNumber }
            val bestPct = maxOf(existing?.bestPercentage ?: 0, percentage)
            val bestScore = maxOf(existing?.bestScore ?: 0, totalScore)
            quizDao.upsertLevelProgress(
                QuizLevelProgressEntity(
                    levelNumber = levelNumber,
                    unlocked = true,
                    bestPercentage = bestPct,
                    bestScore = bestScore
                )
            )
            // Unlock next level if >= 50%
            if (percentage >= 50 && levelNumber < 20) {
                val nextExisting = quizDao.getLevelProgress().first().find { it.levelNumber == levelNumber + 1 }
                if (nextExisting == null || !nextExisting.unlocked) {
                    quizDao.upsertLevelProgress(
                        QuizLevelProgressEntity(
                            levelNumber = levelNumber + 1,
                            unlocked = true,
                            bestPercentage = nextExisting?.bestPercentage ?: 0,
                            bestScore = nextExisting?.bestScore ?: 0
                        )
                    )
                }
            }
        }

        if (isDailyQuiz) {
            val today = LocalDate.now()
            settingsDataStore.recordDailyQuizPlayed(today.toString(), today.minusDays(1).toString())
        }
    }

    fun buildLevels(progressList: List<QuizLevelProgressEntity>): List<QuizLevelInfo> {
        val map = progressList.associateBy { it.levelNumber }
        return (1..20).map { lvl ->
            val diff = when (lvl) {
                in 1..4 -> QuizDifficulty.EASY
                in 5..8 -> QuizDifficulty.MEDIUM
                in 9..14 -> QuizDifficulty.HARD
                else -> QuizDifficulty.EXPERT
            }
            val saved = map[lvl]
            val unlocked = lvl == 1 || (saved?.unlocked == true)
            QuizLevelInfo(
                levelNumber = lvl,
                titleBn = "লেভেল $lvl (${diff.bnName.substringBefore(" ")})",
                difficulty = diff,
                questionCount = 20, // 20 questions per level round from that 50-Q tier
                unlocked = unlocked,
                bestPercentage = saved?.bestPercentage ?: 0
            )
        }
    }

    suspend fun resetQuizProgress() {
        quizDao.clearAnsweredQuestions()
        quizDao.clearLevelProgress()
    }
}
