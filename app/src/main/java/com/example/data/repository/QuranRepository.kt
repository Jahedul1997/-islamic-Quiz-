package com.example.data.repository

import com.example.data.local.CachedAyahEntity
import com.example.data.local.QuranBookmarkEntity
import com.example.data.local.QuranDao
import com.example.data.local.SettingsDataStore
import com.example.data.model.AyahItem
import com.example.data.remote.RemoteNetworkClient
import com.example.data.seed.QuranMetadataData
import com.example.data.seed.QuranOfflineBundledData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuranRepository(
    private val quranDao: QuranDao,
    private val settingsDataStore: SettingsDataStore
) {
    val cachedSurahCount: Flow<Int> = quranDao.getCachedSurahCount()
    val cachedSurahNumbers: Flow<List<Int>> = quranDao.getCachedSurahNumbers()
    val allBookmarks: Flow<List<QuranBookmarkEntity>> = quranDao.getAllBookmarks()

    suspend fun ensureBundledSeeded() {
        val existingCount = quranDao.getAyahsBySurahOnce(1).size
        if (existingCount == 0) {
            val allBundled = QuranOfflineBundledData.bundledSurahs.values.flatten().map { it.toEntity() }
            if (allBundled.isNotEmpty()) {
                quranDao.insertAyahs(allBundled)
            }
        }
    }

    fun observeSurahAyahs(surahNumber: Int): Flow<List<AyahItem>> =
        quranDao.getAyahsBySurah(surahNumber).map { list -> list.map { it.toDomain() } }

    suspend fun loadSurahIfNeeded(surahNumber: Int): Result<Unit> {
        val local = quranDao.getAyahsBySurahOnce(surahNumber)
        if (local.isNotEmpty()) return Result.success(Unit)

        val bundled = QuranOfflineBundledData.bundledSurahs[surahNumber]
        if (!bundled.isNullOrEmpty()) {
            quranDao.insertAyahs(bundled.map { it.toEntity() })
            return Result.success(Unit)
        }

        return RemoteNetworkClient.fetchSurahAyahs(surahNumber).map { ayahs ->
            quranDao.insertAyahs(ayahs.map { it.toEntity() })
        }
    }

    suspend fun downloadAllRemainingSurahs(onProgress: (Int, Int) -> Unit): Int {
        var downloaded = 0
        val total = QuranMetadataData.allSurahs.size
        for (surah in QuranMetadataData.allSurahs) {
            val existing = quranDao.getAyahsBySurahOnce(surah.number)
            if (existing.isEmpty()) {
                val res = loadSurahIfNeeded(surah.number)
                if (res.isSuccess) downloaded++
            } else {
                downloaded++
            }
            onProgress(surah.number, total)
        }
        return downloaded
    }

    suspend fun toggleBookmark(ayah: AyahItem, surahNameBn: String, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) {
            quranDao.deleteBookmark(ayah.surahNumber, ayah.numberInSurah)
        } else {
            quranDao.insertBookmark(
                QuranBookmarkEntity(
                    surahNumber = ayah.surahNumber,
                    ayahNumber = ayah.numberInSurah,
                    surahNameBn = surahNameBn,
                    arabicSnippet = ayah.arabicText.take(80),
                    translationSnippet = ayah.translationBn.take(120)
                )
            )
        }
    }

    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        quranDao.deleteBookmark(surahNumber, ayahNumber)
    }

    suspend fun saveLastRead(surahNumber: Int, ayahNumber: Int) {
        settingsDataStore.setLastRead(surahNumber, ayahNumber)
    }

    private fun AyahItem.toEntity() = CachedAyahEntity(
        surahNumber = surahNumber,
        numberInSurah = numberInSurah,
        globalNumber = globalNumber,
        arabicText = arabicText,
        transliterationBn = transliterationBn,
        translationBn = translationBn,
        juz = juz
    )

    private fun CachedAyahEntity.toDomain() = AyahItem(
        surahNumber = surahNumber,
        numberInSurah = numberInSurah,
        globalNumber = globalNumber,
        arabicText = arabicText,
        transliterationBn = transliterationBn,
        translationBn = translationBn,
        juz = juz
    )
}
