package com.example.data.util

import com.example.data.model.BanglaDateInfo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Implements the Bangla Academy Revised Calendar (2019 revision):
 * - Pohela Boishakh = 14 April
 * - Boishakh..Ashwin (6 months): 31 days each
 * - Kartik..Magh, Chaitro (5 months): 30 days each
 * - Falgun: 29 days (30 days in Gregorian leap years)
 */
object BanglaCalendarConverter {
    val banglaMonthsBn = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    val banglaMonthsEn = listOf(
        "Boishakh", "Jyoishtho", "Asharh", "Srabon", "Bhadro", "Ashwin",
        "Kartik", "Agrahayon", "Poush", "Magh", "Falgun", "Chaitro"
    )

    private val seasonsBn = listOf(
        "গ্রীষ্ম", "গ্রীষ্ম", "বর্ষা", "বর্ষা", "শরৎ", "শরৎ",
        "হেমন্ত", "হেমন্ত", "শীত", "শীত", "বসন্ত", "বসন্ত"
    )

    fun fromGregorian(date: LocalDate): BanglaDateInfo {
        val gYear = date.year
        val pohelaThisYear = LocalDate.of(gYear, 4, 14)

        val (startDate, banglaYear, leapCheckGregorianYear) = if (!date.isBefore(pohelaThisYear)) {
            Triple(pohelaThisYear, gYear - 593, gYear + 1)
        } else {
            Triple(LocalDate.of(gYear - 1, 4, 14), gYear - 594, gYear)
        }

        val isLeap = java.time.Year.isLeap(leapCheckGregorianYear.toLong())
        val monthLengths = intArrayOf(
            31, 31, 31, 31, 31, 31, // Boishakh to Ashwin
            30, 30, 30, 30,          // Kartik to Magh
            if (isLeap) 30 else 29,  // Falgun
            30                       // Chaitro
        )

        var daysElapsed = ChronoUnit.DAYS.between(startDate, date).toInt()
        var mIdx = 0
        while (mIdx < 11 && daysElapsed >= monthLengths[mIdx]) {
            daysElapsed -= monthLengths[mIdx]
            mIdx++
        }
        val day = daysElapsed + 1

        return BanglaDateInfo(
            day = day,
            monthIndex = mIdx,
            monthNameBn = banglaMonthsBn[mIdx],
            year = banglaYear,
            seasonBn = seasonsBn[mIdx]
        )
    }
}
