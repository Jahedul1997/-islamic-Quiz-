package com.example.data.util

import com.example.data.model.AsrMadhab
import com.example.data.model.CalculationMethod
import com.example.data.model.DailyPrayerTimes
import com.example.data.model.PrayerType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.*

/**
 * Pure Kotlin offline astronomical prayer time calculator (Jean Meeus solar coordinates).
 * Supports Karachi (Bangladesh standard), MWL, ISNA, Umm Al-Qura, Egyptian, and Hanafi/Shafi Asr.
 */
object PrayerTimeCalculator {

    fun calculate(
        date: LocalDate,
        lat: Double,
        lon: Double,
        locationNameBn: String,
        locationNameEn: String,
        method: CalculationMethod = CalculationMethod.KARACHI,
        madhab: AsrMadhab = AsrMadhab.HANAFI
    ): DailyPrayerTimes {
        val zoneId = if (lat in 20.0..27.0 && lon in 87.5..93.5) {
            ZoneId.of("Asia/Dhaka")
        } else {
            ZoneId.systemDefault()
        }
        val tzOffsetHours = zoneId.rules.getOffset(date.atTime(12, 0).atZone(zoneId).toInstant()).totalSeconds / 3600.0
        val jd = julianDate(date.year, date.monthValue, date.dayOfMonth) - lon / (15.0 * 24.0)

        val (decl, eqt) = sunPosition(jd)
        val solarNoon = 12.0 + tzOffsetHours - lon / 15.0 - eqt

        val sunriseAngle = 0.833
        val sunrise = solarNoon - hourAngle(lat, decl, sunriseAngle) / 15.0
        val sunset = solarNoon + hourAngle(lat, decl, sunriseAngle) / 15.0

        val fajr = solarNoon - hourAngle(lat, decl, method.fajrAngle) / 15.0
        val dhuhr = solarNoon + 1.0 / 60.0 // 1 min safety margin after zenith
        val shadowFactor = if (madhab == AsrMadhab.HANAFI) 2.0 else 1.0
        val asrAngle = -Math.toDegrees(atan(1.0 / (shadowFactor + tan(Math.toRadians(abs(lat - decl))))))
        val asr = solarNoon + hourAngle(lat, decl, asrAngle) / 15.0
        val maghrib = sunset
        val isha = if (method == CalculationMethod.UMM_AL_QURA) {
            maghrib + 1.5 // 90 mins after Maghrib
        } else {
            solarNoon + hourAngle(lat, decl, method.ishaAngle) / 15.0
        }

        return DailyPrayerTimes(
            dateIso = date.toString(),
            locationNameBn = locationNameBn,
            locationNameEn = locationNameEn,
            fajr = formatHours(fajr),
            sunrise = formatHours(sunrise),
            dhuhr = formatHours(dhuhr),
            asr = formatHours(asr),
            maghrib = formatHours(maghrib),
            isha = formatHours(isha),
            source = "offline_calc"
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        return decl to eqt
    }

    private fun hourAngle(lat: Double, decl: Double, angle: Double): Double {
        val latR = Math.toRadians(lat)
        val declR = Math.toRadians(decl)
        val term = (-sin(Math.toRadians(angle)) - sin(latR) * sin(declR)) / (cos(latR) * cos(declR))
        val clamped = term.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped))
    }

    private fun fixAngle(a: Double): Double = a - 360.0 * floor(a / 360.0)
    private fun fixHour(a: Double): Double = a - 24.0 * floor(a / 24.0)

    private fun formatHours(hours: Double): String {
        val hFixed = fixHour(hours + 0.5 / 60.0)
        val h = floor(hFixed).toInt()
        val m = floor((hFixed - h) * 60.0).toInt().coerceIn(0, 59)
        return String.format("%02d:%02d", h, m)
    }

    data class NextPrayerStatus(
        val nextPrayer: PrayerType,
        val targetTimeStr: String,
        val remainingSeconds: Long,
        val currentPrayer: PrayerType
    )

    fun computeNextPrayer(times: DailyPrayerTimes, now: ZonedDateTime = ZonedDateTime.now()): NextPrayerStatus {
        val today = now.toLocalDate()
        val ordered = listOf(
            PrayerType.FAJR to parseLocalTime(times.fajr),
            PrayerType.SUNRISE to parseLocalTime(times.sunrise),
            PrayerType.DHUHR to parseLocalTime(times.dhuhr),
            PrayerType.ASR to parseLocalTime(times.asr),
            PrayerType.MAGHRIB to parseLocalTime(times.maghrib),
            PrayerType.ISHA to parseLocalTime(times.isha)
        )

        var currentPrayer = PrayerType.ISHA
        for ((type, t) in ordered) {
            val zdt = ZonedDateTime.of(today, t, now.zone)
            if (zdt.isAfter(now)) {
                val secs = ChronoUnit.SECONDS.between(now, zdt).coerceAtLeast(0)
                return NextPrayerStatus(
                    nextPrayer = type,
                    targetTimeStr = formatLocalTime(t),
                    remainingSeconds = secs,
                    currentPrayer = currentPrayer
                )
            }
            if (type != PrayerType.SUNRISE) currentPrayer = type
        }

        // Next is tomorrow's Fajr
        val tomorrowFajr = ZonedDateTime.of(today.plusDays(1), ordered.first().second, now.zone)
        val secs = ChronoUnit.SECONDS.between(now, tomorrowFajr).coerceAtLeast(0)
        return NextPrayerStatus(
            nextPrayer = PrayerType.FAJR,
            targetTimeStr = times.fajr,
            remainingSeconds = secs,
            currentPrayer = PrayerType.ISHA
        )
    }

    fun parseLocalTime(hhmm: String): LocalTime {
        val clean = hhmm.trim().substringBefore(" ")
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 5
        val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        return LocalTime.of(h, m)
    }

    private fun formatLocalTime(t: LocalTime): String = String.format("%02d:%02d", t.hour, t.minute)
}
