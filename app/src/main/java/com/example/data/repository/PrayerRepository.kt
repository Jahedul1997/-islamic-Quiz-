package com.example.data.repository

import com.example.data.local.CachedPrayerTimesEntity
import com.example.data.local.PrayerCacheDao
import com.example.data.local.UserSettings
import com.example.data.model.DailyPrayerTimes
import com.example.data.remote.RemoteNetworkClient
import com.example.data.seed.DistrictsData
import com.example.data.util.PrayerTimeCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PrayerRepository(
    private val prayerCacheDao: PrayerCacheDao
) {
    suspend fun getPrayerTimes(
        date: LocalDate,
        settings: UserSettings,
        tryOnlineSync: Boolean = true
    ): DailyPrayerTimes {
        val (lat, lon, locBn, locEn) = if (settings.useGpsLocation) {
            listOf(settings.gpsLat, settings.gpsLon, settings.gpsLocationName, settings.gpsLocationName)
        } else {
            val d = DistrictsData.findById(settings.selectedDistrictId)
            listOf(d.lat, d.lon, d.bnName, d.enName)
        }
        val latD = lat as Double
        val lonD = lon as Double
        val nameBn = locBn as String
        val nameEn = locEn as String

        val key = "${date}|${"%.2f".format(latD)},${"%.2f".format(lonD)}|${settings.calculationMethod.id}|${settings.asrMadhab.id}"
        val cached = prayerCacheDao.getByKey(key)

        if (cached != null && cached.source == "online") {
            return cached.toDomain()
        }

        if (tryOnlineSync) {
            val ddMmYyyy = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val remote = RemoteNetworkClient.fetchAladhanTimings(
                dateDdMmYyyy = ddMmYyyy,
                dateIso = date.toString(),
                lat = latD,
                lon = lonD,
                methodId = settings.calculationMethod.id,
                schoolId = settings.asrMadhab.id,
                locationNameBn = nameBn,
                locationNameEn = nameEn
            )
            if (remote.isSuccess) {
                val times = remote.getOrThrow()
                prayerCacheDao.insert(times.toEntity(key))
                return times
            }
        }

        if (cached != null) {
            return cached.copy(source = "cached").toDomain()
        }

        // Offline astronomical calculation fallback (always available!)
        val calculated = PrayerTimeCalculator.calculate(
            date = date,
            lat = latD,
            lon = lonD,
            locationNameBn = nameBn,
            locationNameEn = nameEn,
            method = settings.calculationMethod,
            madhab = settings.asrMadhab
        )
        prayerCacheDao.insert(calculated.toEntity(key))
        return calculated
    }

    private fun CachedPrayerTimesEntity.toDomain() = DailyPrayerTimes(
        dateIso = dateIso,
        locationNameBn = locationNameBn,
        locationNameEn = locationNameEn,
        fajr = fajr,
        sunrise = sunrise,
        dhuhr = dhuhr,
        asr = asr,
        maghrib = maghrib,
        isha = isha,
        source = source
    )

    private fun DailyPrayerTimes.toEntity(key: String) = CachedPrayerTimesEntity(
        cacheKey = key,
        dateIso = dateIso,
        locationNameBn = locationNameBn,
        locationNameEn = locationNameEn,
        fajr = fajr,
        sunrise = sunrise,
        dhuhr = dhuhr,
        asr = asr,
        maghrib = maghrib,
        isha = isha,
        source = source
    )
}
