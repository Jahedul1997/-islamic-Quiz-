package com.example.data.remote

import com.example.data.model.AyahItem
import com.example.data.model.DailyPrayerTimes
import com.example.data.util.TransliterationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object RemoteNetworkClient {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .build()
    }

    suspend fun fetchSurahAyahs(surahNumber: Int): Result<List<AyahItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,bn.bengali,en.transliteration"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val body = response.body?.string() ?: error("Empty body")
                val root = JSONObject(body)
                val editions = root.getJSONArray("data")
                if (editions.length() < 3) error("Incomplete editions")

                val arAyahs = editions.getJSONObject(0).getJSONArray("ayahs")
                val bnAyahs = editions.getJSONObject(1).getJSONArray("ayahs")
                val trAyahs = editions.getJSONObject(2).getJSONArray("ayahs")

                val result = ArrayList<AyahItem>(arAyahs.length())
                for (i in 0 until arAyahs.length()) {
                    val arObj = arAyahs.getJSONObject(i)
                    val bnObj = bnAyahs.optJSONObject(i)
                    val trObj = trAyahs.optJSONObject(i)
                    val latinTr = trObj?.optString("text", "") ?: ""
                    result.add(
                        AyahItem(
                            surahNumber = surahNumber,
                            numberInSurah = arObj.getInt("numberInSurah"),
                            globalNumber = arObj.getInt("number"),
                            arabicText = arObj.getString("text"),
                            transliterationBn = TransliterationHelper.toBengaliPronunciation(latinTr),
                            translationBn = bnObj?.optString("text", "") ?: "",
                            juz = arObj.optInt("juz", 1)
                        )
                    )
                }
                result
            }
        }
    }

    suspend fun fetchAladhanTimings(
        dateDdMmYyyy: String,
        dateIso: String,
        lat: Double,
        lon: Double,
        methodId: Int,
        schoolId: Int,
        locationNameBn: String,
        locationNameEn: String
    ): Result<DailyPrayerTimes> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://api.aladhan.com/v1/timings/$dateDdMmYyyy?latitude=$lat&longitude=$lon&method=$methodId&school=$schoolId"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val body = response.body?.string() ?: error("Empty body")
                val root = JSONObject(body)
                val timings = root.getJSONObject("data").getJSONObject("timings")
                fun clean(key: String) = timings.getString(key).substringBefore(" ").trim()
                DailyPrayerTimes(
                    dateIso = dateIso,
                    locationNameBn = locationNameBn,
                    locationNameEn = locationNameEn,
                    fajr = clean("Fajr"),
                    sunrise = clean("Sunrise"),
                    dhuhr = clean("Dhuhr"),
                    asr = clean("Asr"),
                    maghrib = clean("Maghrib"),
                    isha = clean("Isha"),
                    source = "online"
                )
            }
        }
    }
}
