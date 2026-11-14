package com.example.data.util

import com.example.data.model.AppLanguage
import java.time.DayOfWeek
import java.time.LocalDate

object BengaliNumeralUtil {
    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengali(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBengali(number: Int): String = toBengali(number.toString())
    fun toBengali(number: Long): String = toBengali(number.toString())

    fun formatNum(number: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) toBengali(number) else number.toString()

    fun formatDigits(text: String, lang: AppLanguage): String =
        if (lang == AppLanguage.BENGALI) toBengali(text) else text

    val englishMonthsBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    val englishMonthsEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun dayOfWeekBn(dow: DayOfWeek): String = when (dow) {
        DayOfWeek.SATURDAY -> "শনিবার"
        DayOfWeek.SUNDAY -> "রবিবার"
        DayOfWeek.MONDAY -> "সোমবার"
        DayOfWeek.TUESDAY -> "মঙ্গলবার"
        DayOfWeek.WEDNESDAY -> "বুধবার"
        DayOfWeek.THURSDAY -> "বৃহস্পতিবার"
        DayOfWeek.FRIDAY -> "শুক্রবার"
    }

    fun dayOfWeekShortBn(dow: DayOfWeek): String = when (dow) {
        DayOfWeek.SATURDAY -> "শনি"
        DayOfWeek.SUNDAY -> "রবি"
        DayOfWeek.MONDAY -> "সোম"
        DayOfWeek.TUESDAY -> "মঙ্গল"
        DayOfWeek.WEDNESDAY -> "বুধ"
        DayOfWeek.THURSDAY -> "বৃহঃ"
        DayOfWeek.FRIDAY -> "শুক্র"
    }

    fun formatGregorianDate(date: LocalDate, lang: AppLanguage): String {
        return if (lang == AppLanguage.BENGALI) {
            val d = toBengali(date.dayOfMonth)
            val m = englishMonthsBn[date.monthValue - 1]
            val y = toBengali(date.year)
            val dow = dayOfWeekBn(date.dayOfWeek)
            "$d $m $y, $dow"
        } else {
            val m = englishMonthsEn[date.monthValue - 1]
            val dow = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            "${date.dayOfMonth} $m ${date.year}, $dow"
        }
    }

    fun formatTime12h(hhmm: String, lang: AppLanguage): String {
        val parts = hhmm.split(":")
        if (parts.size < 2) return hhmm
        val h = parts[0].toIntOrNull() ?: return hhmm
        val m = parts[1].toIntOrNull() ?: return hhmm
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val amPmBn = when {
            h < 4 -> "রাত"
            h < 12 -> "সকাল"
            h < 16 -> "দুপুর"
            h < 18 -> "বিকাল"
            h < 20 -> "সন্ধ্যা"
            else -> "রাত"
        }
        val amPmEn = if (h < 12) "AM" else "PM"
        val raw = String.format("%d:%02d", h12, m)
        return if (lang == AppLanguage.BENGALI) {
            "$amPmBn ${toBengali(raw)}"
        } else {
            "$raw $amPmEn"
        }
    }
}

fun tr(bn: String, en: String, lang: AppLanguage): String =
    if (lang == AppLanguage.BENGALI) bn else en
