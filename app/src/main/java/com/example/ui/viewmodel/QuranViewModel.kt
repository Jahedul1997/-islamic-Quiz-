package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.IslamicQuizApp
import com.example.audio.QuranAudioPlayer
import com.example.audio.QuranAudioState
import com.example.data.local.QuranBookmarkEntity
import com.example.data.model.AyahItem
import com.example.data.model.SurahInfo
import com.example.data.repository.QuranRepository
import com.example.data.seed.QuranMetadataData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuranUiState(
    val selectedTab: Int = 0, // 0 = Surahs, 1 = Paras, 2 = Bookmarks
    val searchQuery: String = "",
    val activeSurah: SurahInfo? = null,
    val activeAyahs: List<AyahItem> = emptyList(),
    val isLoadingSurah: Boolean = false,
    val loadError: String? = null,
    val cachedSurahNumbers: Set<Int> = emptySet(),
    val bookmarks: List<QuranBookmarkEntity> = emptyList(),
    val lastReadSurah: Int = 1,
    val lastReadAyah: Int = 1,
    val arabicFontSize: Int = 26,
    val audioState: QuranAudioState = QuranAudioState(),
    val bulkDownloadProgress: Pair<Int, Int>? = null
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as IslamicQuizApp
    private val settingsStore = app.settingsDataStore
    private val quranRepo = QuranRepository(app.database.quranDao(), settingsStore)
    private val audioPlayer = QuranAudioPlayer()

    private val _selectedTab = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")
    private val _activeSurahNum = MutableStateFlow<Int?>(null)
    private val _isLoadingSurah = MutableStateFlow(false)
    private val _loadError = MutableStateFlow<String?>(null)
    private val _bulkProgress = MutableStateFlow<Pair<Int, Int>?>(null)

    private val activeAyahsFlow: Flow<List<AyahItem>> = _activeSurahNum.flatMapLatest { num ->
        if (num == null) flowOf(emptyList()) else quranRepo.observeSurahAyahs(num)
    }

    private data class CoreInputs(
        val tab: Int,
        val query: String,
        val surahNum: Int?,
        val ayahs: List<AyahItem>,
        val loading: Boolean
    )

    private val coreInputsFlow = combine(
        _selectedTab, _searchQuery, _activeSurahNum, activeAyahsFlow, _isLoadingSurah
    ) { tab, q, sNum, ayahs, loading ->
        CoreInputs(tab, q, sNum, ayahs, loading)
    }

    val uiState: StateFlow<QuranUiState> = combine(
        coreInputsFlow,
        _loadError,
        quranRepo.cachedSurahNumbers,
        quranRepo.allBookmarks,
        settingsStore.settingsFlow,
        audioPlayer.state,
        _bulkProgress
    ) { arr ->
        val core = arr[0] as CoreInputs
        val err = arr[1] as String?
        @Suppress("UNCHECKED_CAST")
        val cachedNums = arr[2] as List<Int>
        @Suppress("UNCHECKED_CAST")
        val bookmarks = arr[3] as List<QuranBookmarkEntity>
        val settings = arr[4] as com.example.data.local.UserSettings
        val audio = arr[5] as QuranAudioState
        @Suppress("UNCHECKED_CAST")
        val bulk = arr[6] as Pair<Int, Int>?

        QuranUiState(
            selectedTab = core.tab,
            searchQuery = core.query,
            activeSurah = core.surahNum?.let { QuranMetadataData.findSurah(it) },
            activeAyahs = core.ayahs,
            isLoadingSurah = core.loading,
            loadError = err,
            cachedSurahNumbers = cachedNums.toSet(),
            bookmarks = bookmarks,
            lastReadSurah = settings.lastReadSurah,
            lastReadAyah = settings.lastReadAyah,
            arabicFontSize = settings.quranArabicFontSize,
            audioState = audio,
            bulkDownloadProgress = bulk
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuranUiState())

    init {
        viewModelScope.launch {
            quranRepo.ensureBundledSeeded()
        }
    }

    fun selectTab(tab: Int) { _selectedTab.value = tab }
    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun openSurah(surahNumber: Int) {
        _activeSurahNum.value = surahNumber
        _loadError.value = null
        viewModelScope.launch {
            _isLoadingSurah.value = true
            val res = quranRepo.loadSurahIfNeeded(surahNumber)
            if (res.isFailure) {
                _loadError.value = "এই সূরাটি অফলাইনে এখনো ডাউনলোড হয়নি। অনুগ্রহ করে ইন্টারনেট সংযোগ চালু করে পুনরায় চেষ্টা করুন।"
            } else {
                quranRepo.saveLastRead(surahNumber, 1)
            }
            _isLoadingSurah.value = false
        }
    }

    fun closeSurah() {
        audioPlayer.stop()
        _activeSurahNum.value = null
        _loadError.value = null
    }

    fun toggleBookmark(ayah: AyahItem, surahNameBn: String, currentlyBookmarked: Boolean) {
        viewModelScope.launch {
            quranRepo.toggleBookmark(ayah, surahNameBn, currentlyBookmarked)
        }
    }

    fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            quranRepo.removeBookmark(surahNumber, ayahNumber)
        }
    }

    fun markLastReadAyah(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            quranRepo.saveLastRead(surahNumber, ayahNumber)
        }
    }

    fun changeFontSize(delta: Int) {
        viewModelScope.launch {
            settingsStore.setQuranFontSize(uiState.value.arabicFontSize + delta)
        }
    }

    fun toggleAyahAudio(index: Int) {
        audioPlayer.togglePlayPause(uiState.value.activeAyahs, index)
    }

    fun playFullSurah() {
        audioPlayer.playSurahFrom(uiState.value.activeAyahs, 0)
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    fun downloadAllForOffline() {
        if (_bulkProgress.value != null) return
        viewModelScope.launch {
            _bulkProgress.value = 0 to 114
            quranRepo.downloadAllRemainingSurahs { current, total ->
                _bulkProgress.value = current to total
            }
            _bulkProgress.value = null
        }
    }

    override fun onCleared() {
        audioPlayer.stop()
        super.onCleared()
    }
}
