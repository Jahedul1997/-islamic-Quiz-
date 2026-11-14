package com.example

import com.example.data.model.AsrMadhab
import com.example.data.model.CalculationMethod
import com.example.data.seed.DistrictsData
import com.example.data.seed.DuaStaticData
import com.example.data.seed.QuranMetadataData
import com.example.data.util.BanglaCalendarConverter
import com.example.data.util.HijriCalendarConverter
import com.example.data.util.PrayerTimeCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
    @Test
    fun `bangla calendar pohela boishakh aligns with 14 april`() {
        val b = BanglaCalendarConverter.fromGregorian(LocalDate.of(2025, 4, 14))
        assertEquals(1, b.day)
        assertEquals(0, b.monthIndex)
        assertEquals("বৈশাখ", b.monthNameBn)
        assertEquals(1432, b.year)
    }

    @Test
    fun `hijri calendar conversion and offset work`() {
        val h0 = HijriCalendarConverter.fromGregorian(LocalDate.of(2025, 3, 1), 0)
        val hPlus1 = HijriCalendarConverter.fromGregorian(LocalDate.of(2025, 3, 1), 1)
        assertTrue(h0.year >= 1446)
        assertNotEquals(h0.day to h0.monthIndex, hPlus1.day to hPlus1.monthIndex)
    }

    @Test
    fun `prayer times calculation produces chronological valid times for dhaka`() {
        val dhaka = DistrictsData.findById("dhaka")
        val times = PrayerTimeCalculator.calculate(
            date = LocalDate.of(2025, 3, 15),
            lat = dhaka.lat,
            lon = dhaka.lon,
            locationNameBn = dhaka.bnName,
            locationNameEn = dhaka.enName,
            method = CalculationMethod.KARACHI,
            madhab = AsrMadhab.HANAFI
        )
        assertTrue(times.fajr < times.sunrise)
        assertTrue(times.sunrise < times.dhuhr)
        assertTrue(times.dhuhr < times.asr)
        assertTrue(times.asr < times.maghrib)
        assertTrue(times.maghrib < times.isha)
    }

    @Test
    fun `seed metadata completeness checks`() {
        assertEquals(64, DistrictsData.allDistricts.size)
        assertTrue(DuaStaticData.allDuas.size >= 30)
        assertEquals(114, QuranMetadataData.allSurahs.size)
        assertEquals(30, QuranMetadataData.allParas.size)
    }
}
