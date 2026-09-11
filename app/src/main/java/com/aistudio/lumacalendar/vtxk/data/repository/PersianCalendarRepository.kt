package com.aistudio.lumacalendar.vtxk.data.repository

import android.content.Context
import android.util.Log
import com.aistudio.lumacalendar.vtxk.R
import com.aistudio.lumacalendar.vtxk.data.api.ApiClient
import com.aistudio.lumacalendar.vtxk.data.api.PersianCalendarApi
import com.aistudio.lumacalendar.vtxk.data.api.dto.PersianCalendarDayDto
import com.aistudio.lumacalendar.vtxk.data.api.dto.PersianCalendarYearResponseDto
import com.aistudio.lumacalendar.vtxk.data.model.PersianCalendarDay
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

interface PersianCalendarRepository {
    /**
     * Retrieves all days of the Persian year (from memory, disk cache, or live API).
     */
    suspend fun getDaysForYear(year: Int): Result<List<PersianCalendarDay>>

    /**
     * Synchronous / instant in-memory lookup for already loaded year data.
     */
    fun getCachedDaysForYear(year: Int): List<PersianCalendarDay>?

    /**
     * Retrieves all official holidays for a Persian year.
     */
    suspend fun getHolidaysForYear(year: Int): List<PersianCalendarDay>

    /**
     * Finds a Persian calendar day by canonical Gregorian date ("YYYY-MM-DD").
     */
    suspend fun getDayByGregorianDate(dateStr: String): PersianCalendarDay?

    /**
     * Finds a Persian calendar day by Shamsi date ("YYYY/MM/DD").
     */
    suspend fun getDayByShamsiDate(shamsiDate: String): PersianCalendarDay?

    /**
     * Converts a Gregorian date ("YYYY-MM-DD") to Shamsi date ("YYYY/MM/DD").
     * Uses API endpoint with local fallback.
     */
    suspend fun convertMiladiToShamsi(dateStr: String): String

    /**
     * Converts a Shamsi date ("YYYY/MM/DD") to Gregorian date ("YYYY-MM-DD").
     * Uses API endpoint with local fallback.
     */
    suspend fun convertShamsiToMiladi(shamsiDateStr: String): String
}

class PersianCalendarRepositoryImpl(
    private val context: Context,
    private val api: PersianCalendarApi = ApiClient.persianCalendarApi
) : PersianCalendarRepository {

    private val tag = "PersianCalendarRepo"

    // In-memory cache by year (Int -> List<PersianCalendarDay>)
    private val memoryCache = ConcurrentHashMap<Int, List<PersianCalendarDay>>()

    // In-flight request deduplication map to prevent multiple requests for the same year
    private val inFlightRequests = ConcurrentHashMap<Int, Deferred<Result<List<PersianCalendarDay>>>>()
    private val mutex = Mutex()

    init {
        // Pre-warm memory cache from bundled raw resources for instantaneous cold start
        loadBundledRawResource(1404, R.raw.persian_calendar_1404)
        loadBundledRawResource(1405, R.raw.persian_calendar_1405)
    }

    private fun loadBundledRawResource(year: Int, rawResId: Int) {
        try {
            val json = context.resources.openRawResource(rawResId).bufferedReader().use { it.readText() }
            val adapter = ApiClient.moshi.adapter(PersianCalendarYearResponseDto::class.java)
            val response = adapter.fromJson(json)
            if (response != null && response.data.isNotEmpty()) {
                val domainDays = response.data.map { it.toDomainModel() }
                memoryCache[year] = domainDays
                Log.d(tag, "Pre-loaded bundled calendar data for year $year (${domainDays.size} days).")
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to load bundled raw resource for year $year: ${e.message}")
        }
    }

    override fun getCachedDaysForYear(year: Int): List<PersianCalendarDay>? {
        return memoryCache[year]
    }

    override suspend fun getDaysForYear(year: Int): Result<List<PersianCalendarDay>> = withContext(Dispatchers.IO) {
        // 1. Check in-memory cache first
        val cached = memoryCache[year]
        if (cached != null && cached.isNotEmpty()) {
            return@withContext Result.success(cached)
        }

        // 2. Check persistent disk cache
        val diskCached = readFromDiskCache(year)
        if (diskCached != null && diskCached.isNotEmpty()) {
            memoryCache[year] = diskCached
            return@withContext Result.success(diskCached)
        }

        // 3. Coordinate network fetch with in-flight request deduplication
        val deferred = mutex.withLock {
            val existing = inFlightRequests[year]
            if (existing != null) {
                existing
            } else {
                val newDeferred = CoroutineScope(Dispatchers.IO).async {
                    fetchFromApiAndCache(year)
                }
                inFlightRequests[year] = newDeferred
                newDeferred
            }
        }

        val result = try {
            deferred.await()
        } finally {
            mutex.withLock {
                inFlightRequests.remove(year)
            }
        }

        result
    }

    private suspend fun fetchFromApiAndCache(year: Int): Result<List<PersianCalendarDay>> {
        return try {
            Log.d(tag, "Fetching Persian calendar data for year $year from API...")
            val response = api.getYear(year)
            if (response.data.isNotEmpty()) {
                val domainDays = response.data.map { it.toDomainModel() }
                memoryCache[year] = domainDays
                writeToDiskCache(year, response)
                Log.d(tag, "Successfully loaded and cached ${domainDays.size} days for Persian year $year.")
                Result.success(domainDays)
            } else {
                // Fallback to disk cache if available
                val fallback = readFromDiskCache(year)
                if (fallback != null && fallback.isNotEmpty()) {
                    Result.success(fallback)
                } else {
                    Result.failure(IllegalStateException("Empty calendar data returned for year $year"))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch calendar data for year $year: ${e.message}", e)
            // Fallback to disk cache on network error
            val diskFallback = readFromDiskCache(year)
            if (diskFallback != null && diskFallback.isNotEmpty()) {
                Log.i(tag, "Using disk cached fallback for year $year on network failure.")
                memoryCache[year] = diskFallback
                Result.success(diskFallback)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getHolidaysForYear(year: Int): List<PersianCalendarDay> {
        val daysResult = getDaysForYear(year)
        val days = daysResult.getOrNull() ?: emptyList()
        return days.filter { it.isHoliday }
    }

    override suspend fun getDayByGregorianDate(dateStr: String): PersianCalendarDay? {
        val (year, _) = CalendarConverter.getYearAndMonth(dateStr, com.aistudio.lumacalendar.vtxk.util.CalendarType.JALALI)
        val days = getDaysForYear(year).getOrNull() ?: emptyList()
        return days.firstOrNull { it.date == dateStr }
    }

    override suspend fun getDayByShamsiDate(shamsiDate: String): PersianCalendarDay? {
        val parts = shamsiDate.split("/", "-")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val normalizedShamsi = "%04d/%02d/%02d".format(year, parts[1].toIntOrNull() ?: 1, parts[2].toIntOrNull() ?: 1)
        val days = getDaysForYear(year).getOrNull() ?: emptyList()
        return days.firstOrNull { it.shamsiDate == normalizedShamsi || it.shamsiDate == shamsiDate }
    }

    override suspend fun convertMiladiToShamsi(dateStr: String): String = withContext(Dispatchers.IO) {
        try {
            // First check memory cache
            val day = getDayByGregorianDate(dateStr)
            if (day != null) {
                return@withContext day.shamsiDate
            }
            // If not found in cache, call API endpoint
            val safeDate = dateStr.replace("/", "-")
            val responseBody = api.convertMiladiToShamsi(safeDate)
            val result = responseBody.string().trim()
            if (result.isNotBlank() && result.contains("/")) {
                return@withContext result
            }
        } catch (e: Exception) {
            Log.w(tag, "convertMiladiToShamsi API call failed for $dateStr, using local algorithmic conversion: ${e.message}")
        }
        // Authoritative mathematical local conversion fallback
        val j = CalendarConverter.gregorianToJalali(dateStr)
        "%04d/%02d/%02d".format(j.year, j.month, j.day)
    }

    override suspend fun convertShamsiToMiladi(shamsiDateStr: String): String = withContext(Dispatchers.IO) {
        try {
            val day = getDayByShamsiDate(shamsiDateStr)
            if (day != null) {
                return@withContext day.date
            }
            val safeDate = shamsiDateStr.replace("/", "-")
            val responseBody = api.convertShamsiToMiladi(safeDate)
            val result = responseBody.string().trim()
            if (result.isNotBlank()) {
                val normalized = result.replace("/", "-")
                if (normalized.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    return@withContext normalized
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "convertShamsiToMiladi API call failed for $shamsiDateStr, using local algorithmic conversion: ${e.message}")
        }
        // Authoritative mathematical local conversion fallback
        val parts = shamsiDateStr.split("/", "-")
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: 1405
            val m = parts[1].toIntOrNull() ?: 1
            val d = parts[2].toIntOrNull() ?: 1
            return@withContext CalendarConverter.jalaliToGregorianString(y, m, d)
        }
        "2026-09-11"
    }

    private fun readFromDiskCache(year: Int): List<PersianCalendarDay>? {
        return try {
            val file = File(context.cacheDir, "persian_calendar_$year.json")
            if (!file.exists()) return null
            val json = file.readText()
            val adapter = ApiClient.moshi.adapter(PersianCalendarYearResponseDto::class.java)
            val response = adapter.fromJson(json) ?: return null
            response.data.map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.w(tag, "Error reading disk cache for year $year: ${e.message}")
            null
        }
    }

    private fun writeToDiskCache(year: Int, response: PersianCalendarYearResponseDto) {
        try {
            val file = File(context.cacheDir, "persian_calendar_$year.json")
            val adapter = ApiClient.moshi.adapter(PersianCalendarYearResponseDto::class.java)
            val json = adapter.toJson(response)
            file.writeText(json)
        } catch (e: Exception) {
            Log.w(tag, "Error writing disk cache for year $year: ${e.message}")
        }
    }

    private fun PersianCalendarDayDto.toDomainModel(): PersianCalendarDay {
        return PersianCalendarDay(
            date = this.date,
            shamsiDate = this.shamsiDate,
            isHoliday = this.isHoliday,
            holidayDescription = this.resolvedDescription
        )
    }
}
