package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.IslamicQuizApp
import com.example.audio.SoundEffectPlayer
import com.example.audio.TtsFeedbackManager
import com.example.data.model.QuizCategory
import com.example.data.model.QuizDifficulty
import com.example.data.model.QuizLevelInfo
import com.example.data.model.QuizQuestion
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class QuizPhase {
    LOBBY, PLAYING, RESULT
}

data class QuizUiState(
    val phase: QuizPhase = QuizPhase.LOBBY,
    val selectedCategory: QuizCategory = QuizCategory.ALL,
    val selectedDifficulty: QuizDifficulty = QuizDifficulty.ALL,
    val questionCountChoice: Int = 15,
    val totalQuestionsAvailable: Int = 1000,
    val answeredCount: Int = 0,
    val totalCorrectLifetime: Int = 0,
    val highScore: Int = 0,
    val dailyQuizStreak: Int = 0,
    val dailyQuizCompletedToday: Boolean = false,
    val levels: List<QuizLevelInfo> = emptyList(),
    val timerEnabled: Boolean = true,
    val timerDurationSeconds: Int = 30,

    // Active session
    val sessionQuestions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val remainingSeconds: Int = 30,
    val timeExpired: Boolean = false,
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val sessionScore: Int = 0,
    val sessionBonusPoints: Int = 0,
    val sessionCorrectCount: Int = 0,
    val sessionWrongCount: Int = 0,
    val activeLevelNumber: Int? = null,
    val isDailyQuizSession: Boolean = false,
    val feedbackBanner: String? = null
) {
    val currentQuestion: QuizQuestion?
        get() = sessionQuestions.getOrNull(currentIndex)
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as IslamicQuizApp
    private val settingsStore = app.settingsDataStore
    private val quizRepo = QuizRepository(app, app.database.quizDao(), settingsStore)
    private val soundPlayer = SoundEffectPlayer(app)
    private val ttsManager = TtsFeedbackManager(app)

    private val _state = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private val sessionResults = mutableListOf<Pair<Int, Boolean>>()

    init {
        viewModelScope.launch {
            quizRepo.getAllQuestions()
        }
        viewModelScope.launch {
            combine(
                quizRepo.answeredIdsFlow,
                quizRepo.totalCorrectFlow,
                quizRepo.levelProgressFlow,
                settingsStore.settingsFlow
            ) { answeredIds, totalCorrect, lvlProgress, settings ->
                val todayStr = LocalDate.now().toString()
                Quad(answeredIds.size, totalCorrect, quizRepo.buildLevels(lvlProgress), settings to (settings.dailyQuizLastDate == todayStr))
            }.collect { (ansCount, correctCount, levels, settingsPair) ->
                val (settings, dailyDone) = settingsPair
                _state.update {
                    it.copy(
                        answeredCount = ansCount,
                        totalCorrectLifetime = correctCount,
                        levels = levels,
                        highScore = settings.quizHighScore,
                        dailyQuizStreak = settings.dailyQuizStreak,
                        dailyQuizCompletedToday = dailyDone,
                        timerEnabled = settings.quizTimerEnabled,
                        timerDurationSeconds = settings.quizTimerSeconds
                    )
                }
            }
        }
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    fun setCategory(cat: QuizCategory) { _state.update { it.copy(selectedCategory = cat) } }
    fun setDifficulty(diff: QuizDifficulty) { _state.update { it.copy(selectedDifficulty = diff) } }
    fun setQuestionCount(count: Int) { _state.update { it.copy(questionCountChoice = count) } }

    fun setTimerOption(enabled: Boolean, seconds: Int) {
        viewModelScope.launch {
            settingsStore.setQuizTimerEnabled(enabled)
            settingsStore.setQuizTimerSeconds(seconds)
        }
    }

    fun startPracticeQuiz() {
        val st = _state.value
        viewModelScope.launch {
            val questions = quizRepo.getSessionQuestions(
                difficulty = st.selectedDifficulty,
                category = st.selectedCategory,
                count = st.questionCountChoice
            )
            beginSession(questions, levelNumber = null, isDaily = false)
        }
    }

    fun startLevelQuiz(levelNumber: Int) {
        viewModelScope.launch {
            val questions = quizRepo.getSessionQuestions(
                difficulty = QuizDifficulty.ALL,
                category = QuizCategory.ALL,
                count = 20,
                levelNumber = levelNumber
            )
            beginSession(questions, levelNumber = levelNumber, isDaily = false)
        }
    }

    fun startDailyQuiz() {
        viewModelScope.launch {
            val questions = quizRepo.getDailyQuizQuestions()
            beginSession(questions, levelNumber = null, isDaily = true)
        }
    }

    private fun beginSession(questions: List<QuizQuestion>, levelNumber: Int?, isDaily: Boolean) {
        if (questions.isEmpty()) return
        sessionResults.clear()
        val dur = _state.value.timerDurationSeconds
        _state.update {
            it.copy(
                phase = QuizPhase.PLAYING,
                sessionQuestions = questions,
                currentIndex = 0,
                remainingSeconds = dur,
                timeExpired = false,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                sessionScore = 0,
                sessionBonusPoints = 0,
                sessionCorrectCount = 0,
                sessionWrongCount = 0,
                activeLevelNumber = levelNumber,
                isDailyQuizSession = isDaily,
                feedbackBanner = null
            )
        }
        startQuestionTimer()
    }

    private fun startQuestionTimer() {
        timerJob?.cancel()
        val st = _state.value
        if (!st.timerEnabled || st.timerDurationSeconds <= 0) return

        _state.update { it.copy(remainingSeconds = st.timerDurationSeconds, timeExpired = false) }
        timerJob = viewModelScope.launch {
            var left = st.timerDurationSeconds
            while (left > 0) {
                delay(1000L)
                left--
                _state.update { it.copy(remainingSeconds = left) }
            }
            onTimeOut()
        }
    }

    private fun onTimeOut() {
        val st = _state.value
        if (st.isAnswerRevealed) return
        val q = st.currentQuestion ?: return
        sessionResults.add(q.id to false)

        val settings = settingsStore.settingsFlow
        viewModelScope.launch {
            val s = settings.first()
            if (s.soundEnabled) soundPlayer.playWrongSad()
            if (s.soundEnabled && s.voiceFeedbackEnabled) {
                ttsManager.speakBengali("সময় শেষ! আপনার উত্তর ভুল হয়েছে। ইনশাআল্লাহ, পরবর্তী চেষ্টা করুন।")
            }
        }

        _state.update {
            it.copy(
                timeExpired = true,
                isAnswerRevealed = true,
                selectedOptionIndex = null,
                sessionWrongCount = it.sessionWrongCount + 1,
                feedbackBanner = "সময় শেষ হয়ে গেছে!"
            )
        }
    }

    fun selectAnswer(optionIndex: Int) {
        val st = _state.value
        if (st.isAnswerRevealed || st.timeExpired) return
        val q = st.currentQuestion ?: return

        timerJob?.cancel()
        val isCorrect = optionIndex == q.ans
        sessionResults.add(q.id to isCorrect)

        val bonus = if (isCorrect && st.timerEnabled && st.timerDurationSeconds > 0) {
            val half = st.timerDurationSeconds / 2
            when {
                st.remainingSeconds >= (st.timerDurationSeconds * 0.7).toInt() -> 5
                st.remainingSeconds >= half -> 3
                else -> 1
            }
        } else 0

        viewModelScope.launch {
            val s = settingsStore.settingsFlow.first()
            if (isCorrect) {
                if (s.soundEnabled) soundPlayer.playApplause()
                if (s.soundEnabled && s.voiceFeedbackEnabled) {
                    ttsManager.speakBengali("জাযাকাল্লাহু খাইরান")
                }
            } else {
                if (s.soundEnabled) soundPlayer.playWrongSad()
                if (s.soundEnabled && s.voiceFeedbackEnabled) {
                    ttsManager.speakBengali("আপনার উত্তর ভুল হয়েছে। ইনশাআল্লাহ, পরবর্তী চেষ্টা করুন।")
                }
            }
        }

        _state.update {
            it.copy(
                selectedOptionIndex = optionIndex,
                isAnswerRevealed = true,
                sessionScore = it.sessionScore + if (isCorrect) (10 + bonus) else 0,
                sessionBonusPoints = it.sessionBonusPoints + bonus,
                sessionCorrectCount = it.sessionCorrectCount + if (isCorrect) 1 else 0,
                sessionWrongCount = it.sessionWrongCount + if (isCorrect) 0 else 1,
                feedbackBanner = if (isCorrect) {
                    if (bonus > 0) "জাযাকাল্লাহু খাইরান! (+১০ এবং দ্রুত উত্তর বোনাস +$bonus)" else "জাযাকাল্লাহু খাইরান! (+১০ পয়েন্ট)"
                } else {
                    "আপনার উত্তর ভুল হয়েছে। ইনশাআল্লাহ, পরবর্তী চেষ্টা করুন।"
                }
            )
        }
    }

    fun nextQuestion() {
        ttsManager.stop()
        val st = _state.value
        if (st.currentIndex + 1 < st.sessionQuestions.size) {
            _state.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerRevealed = false,
                    timeExpired = false,
                    feedbackBanner = null
                )
            }
            startQuestionTimer()
        } else {
            finishSession()
        }
    }

    private fun finishSession() {
        timerJob?.cancel()
        val st = _state.value
        val total = st.sessionQuestions.size.coerceAtLeast(1)
        val pct = (st.sessionCorrectCount * 100) / total
        viewModelScope.launch {
            quizRepo.recordSessionResults(
                results = sessionResults.toList(),
                totalScore = st.sessionScore,
                percentage = pct,
                levelNumber = st.activeLevelNumber,
                isDailyQuiz = st.isDailyQuizSession
            )
        }
        _state.update { it.copy(phase = QuizPhase.RESULT) }
    }

    fun exitToLobby() {
        timerJob?.cancel()
        ttsManager.stop()
        _state.update { it.copy(phase = QuizPhase.LOBBY) }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            quizRepo.resetQuizProgress()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        soundPlayer.release()
        ttsManager.shutdown()
        super.onCleared()
    }
}
