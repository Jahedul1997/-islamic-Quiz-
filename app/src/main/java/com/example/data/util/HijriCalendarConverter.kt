package com.example.data.util

import com.example.data.model.HijriDateInfo
import java.time.LocalDate
import kotlin.math.floor

/**
 * Astronomical / Kuwaiti Tabular Hijri conversion with user-adjustable moon-sighting offset (-2..+2 days).
 */
object HijriCalendarConverter {

    val hijriMonthsBn = listOf(
        "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল উলা", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    val hijriMonthsAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhul Qi'dah", "Dhul Hijjah"
    )

    fun fromGregorian(date: LocalDate, adjustmentDays: Int = 0): HijriDateInfo {
        val adjusted = date.plusDays(adjustmentDays.toLong())
        val day = adjusted.dayOfMonth
        val month = adjusted.monthValue
        val year = adjusted.year

        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        var b = 2 - a + floor(a / 4.0)
        if (y < 1583) b = 0.0
        if (y == 1582) {
            if (m > 10) b = -10.0
            if (m == 10) {
                b = 0.0
                if (day > 4) b = -10.0
            }
        }
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.0

        val iYear = 10631.0 / 30.0
        val epochAstro = 1948084.0
        val shift1 = 8.01 / 60.0

        var z = jd - epochAstro
        val cyc = floor(z / 10631.0)
        z -= 10631.0 * cyc
        val j = floor((z - shift1) / iYear)
        val iy = (30 * cyc + j).toInt()
        z -= floor(j * iYear + shift1)
        var im = floor((z + 28.5001) / 29.5).toInt()
        if (im == 13) im = 12
        val id = (z - floor(29.5001 * im - 29)).toInt().coerceIn(1, 30)
        val monthIdx = (im - 1).coerceIn(0, 11)

        return HijriDateInfo(
            day = id,
            monthIndex = monthIdx,
            monthNameBn = hijriMonthsBn[monthIdx],
            monthNameAr = hijriMonthsAr[monthIdx],
            year = iy,
            islamicEventBn = getIslamicEvent(monthIdx + 1, id)
        )
    }

    fun getIslamicEvent(hijriMonth: Int, hijriDay: Int): String? {
        return when (hijriMonth to hijriDay) {
            1 to 1 -> "হিজরি নববর্ষ"
            1 to 10 -> "পবিত্র আশুরা"
            3 to 12 -> "১২ রবিউল আউয়াল (সিরাতুন্নবী সা.)"
            7 to 27 -> "শবে মেরাজ"
            8 to 15 -> "শবে বরাত (লাইলাতুল বারাআত)"
            9 to 1 -> "পবিত্র রমজান শুরু"
            9 to 17 -> "ঐতিহাসিক বদর দিবস"
            9 to 27 -> "শবে কদর (সম্ভাব্য)"
            10 to 1 -> "পবিত্র ঈদুল ফিতর"
            12 to 9 -> "পবিত্র আরাফার দিন (ইয়াওমুল আরাফা)"
            12 to 10 -> "পবিত্র ঈদুল আজহা"
            else -> null
        }
    }
}
