package com.aistudio.lumacalendar.vtxk.data.api

import com.aistudio.lumacalendar.vtxk.data.api.dto.PersianCalendarYearResponseDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface PersianCalendarApi {

    /**
     * Retrieves all days of a Persian/Jalali year (e.g. 1403, 1404, 1405).
     */
    @GET("get-year/{year}")
    suspend fun getYear(
        @Path("year") year: Int
    ): PersianCalendarYearResponseDto

    /**
     * Retrieves all days of a specific Persian month.
     * Note: Month should be 2 digits e.g. "01", "12".
     */
    @GET("get-month/{year}/{month}")
    suspend fun getMonth(
        @Path("year") year: Int,
        @Path("month") month: String
    ): PersianCalendarYearResponseDto

    /**
     * Retrieves only official holidays of a Persian year.
     */
    @GET("{year}/holidays")
    suspend fun getHolidays(
        @Path("year") year: Int
    ): PersianCalendarYearResponseDto

    /**
     * Checks if a specific Shamsi date (YYYY-MM-DD) is a holiday.
     */
    @GET("{date}/is-holiday")
    suspend fun isHoliday(
        @Path("date") date: String
    ): ResponseBody

    /**
     * Checks if a Persian year is a leap year.
     */
    @GET("{year}/is-leap-year")
    suspend fun isLeapYear(
        @Path("year") year: Int
    ): ResponseBody

    /**
     * Converts a Shamsi date (YYYY-MM-DD) to Gregorian (YYYY/MM/DD).
     */
    @GET("convert-shamsi-to-miladi/{date}")
    suspend fun convertShamsiToMiladi(
        @Path("date") date: String
    ): ResponseBody

    /**
     * Converts a Gregorian date (YYYY-MM-DD) to Shamsi (YYYY/MM/DD).
     */
    @GET("convert-miladi-to-shamsi/{date}")
    suspend fun convertMiladiToShamsi(
        @Path("date") date: String
    ): ResponseBody
}
