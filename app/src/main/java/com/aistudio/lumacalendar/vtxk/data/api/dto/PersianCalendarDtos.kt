package com.aistudio.lumacalendar.vtxk.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PersianCalendarYearResponseDto(
    @Json(name = "data") val data: List<PersianCalendarDayDto> = emptyList(),
    @Json(name = "totalCount") val totalCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class PersianCalendarDayDto(
    @Json(name = "date") val date: String, // Gregorian date "YYYY-MM-DD"
    @Json(name = "shamsiDate") val shamsiDate: String, // Shamsi date "YYYY/MM/DD"
    @Json(name = "isHoliday") val isHoliday: Boolean = false,
    @Json(name = "holidayDescription") val holidayDescription: String? = null,
    // The Persian Calendar API response JSON has a minor typo in the field name ("holidayDesription")
    @Json(name = "holidayDesription") val holidayDesription: String? = null
) {
    val resolvedDescription: String?
        get() = (holidayDescription ?: holidayDesription)?.takeIf { it.isNotBlank() }
}
