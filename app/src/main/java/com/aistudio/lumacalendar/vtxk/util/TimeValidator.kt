package com.aistudio.lumacalendar.vtxk.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale

/**
 * Validated representation of time of day.
 */
data class ParsedTime(
    val hour: Int, // 0..23
    val minute: Int // 0..59
) {
    init {
        require(hour in 0..23) { "Hour must be 0..23, was $hour" }
        require(minute in 0..59) { "Minute must be 0..59, was $minute" }
    }

    /** Canonical 24-hour time string "HH:mm" (e.g., "09:00", "14:30") */
    val canonicalTime: String
        get() = String.format(Locale.US, "%02d:%02d", hour, minute)

    /** 12-hour display value (1..12) */
    val hour12: Int
        get() {
            val mod = hour % 12
            return if (mod == 0) 12 else mod
        }

    /** Whether this time represents PM */
    val isPm: Boolean
        get() = hour >= 12

    /** Total minutes elapsed since midnight */
    val minutesOfDay: Int
        get() = hour * 60 + minute

    /**
     * Converts to [LocalTime] safely without risk of DateTimeException.
     */
    fun toLocalTime(): LocalTime = LocalTime.of(hour, minute)

    /**
     * Formats this time for 12-hour display with AM/PM indicator.
     */
    fun format12Hour(isRtl: Boolean = false): String {
        val amPm = if (isPm) {
            if (isRtl) "ب.ظ" else "PM"
        } else {
            if (isRtl) "ق.ظ" else "AM"
        }
        val raw = String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm)
        return if (isRtl) LocalizationManager.formatDigits(raw) else raw
    }

    /**
     * Formats this time for 24-hour display.
     */
    fun format24Hour(isRtl: Boolean = false): String {
        val raw = canonicalTime
        return if (isRtl) LocalizationManager.formatDigits(raw) else raw
    }

    companion object {
        val DEFAULT_START = ParsedTime(9, 0)
        val DEFAULT_END = ParsedTime(10, 0)

        fun ofSafe(hour: Int, minute: Int): ParsedTime {
            val safeHour = hour.coerceIn(0, 23)
            val safeMinute = minute.coerceIn(0, 59)
            return ParsedTime(safeHour, safeMinute)
        }
    }
}

/**
 * Enterprise-grade validation, normalization, and parsing utility for all time operations.
 * Guarantees zero crashes on any user input, malformed strings, out-of-bounds hours/minutes,
 * or RTL/LTR localized digits.
 */
object TimeValidator {

    /**
     * Normalizes Eastern Arabic (٠-٩) and Persian (۰-۹) digits into standard ASCII digits (0-9).
     */
    fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (char in input) {
            when (char) {
                in '0'..'9' -> sb.append(char)
                in '\u0660'..'\u0669' -> sb.append((char - '\u0660' + '0'.code).toChar())
                in '\u06F0'..'\u06F9' -> sb.append((char - '\u06F0' + '0'.code).toChar())
                else -> sb.append(char)
            }
        }
        return sb.toString()
    }

    /**
     * Safely parses any time string into a valid [ParsedTime].
     * Never throws exceptions under any circumstance.
     * Supports:
     * - "HH:mm" (24-hour, e.g. "14:30")
     * - "H:m" (e.g. "9:5")
     * - "hh:mm AM/PM" (12-hour, e.g. "2:30 PM", "09:00 am")
     * - Localized Persian/Arabic AM/PM markers ("ق.ظ", "ب.ظ")
     * - Localized Persian/Arabic numerals
     * - Null, empty, blank, or malformed inputs (falls back to default)
     */
    fun parseTime(
        timeStr: String?,
        defaultHour: Int = 9,
        defaultMinute: Int = 0
    ): ParsedTime {
        if (timeStr.isNullOrBlank()) {
            return ParsedTime.ofSafe(defaultHour, defaultMinute)
        }

        try {
            val normalized = normalizeDigits(timeStr).trim()
            val lower = normalized.lowercase(Locale.ROOT)

            // Check for AM / PM indicators
            val hasPm = lower.contains("pm") || lower.contains("ب.ظ") || lower.contains("عصر") || lower.contains("شب")
            val hasAm = lower.contains("am") || lower.contains("ق.ظ") || lower.contains("صبح")

            // Remove non-digit and non-colon characters except separators
            val clean = lower
                .replace("pm", "")
                .replace("am", "")
                .replace("ب.ظ", "")
                .replace("ق.ظ", "")
                .replace("عصر", "")
                .replace("صبح", "")
                .replace("شب", "")
                .trim()

            val parts = clean.split(':', '.', '-', ' ')
                .filter { it.isNotBlank() && it.any { c -> c.isDigit() } }

            var rawHour: Int = defaultHour
            var rawMinute: Int = defaultMinute

            when {
                parts.size >= 2 -> {
                    rawHour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: defaultHour
                    rawMinute = parts[1].filter { it.isDigit() }.toIntOrNull() ?: defaultMinute
                }
                parts.size == 1 -> {
                    val singlePart = parts[0].filter { it.isDigit() }
                    when {
                        singlePart.length == 4 -> { // e.g. "0930"
                            rawHour = singlePart.substring(0, 2).toIntOrNull() ?: defaultHour
                            rawMinute = singlePart.substring(2, 4).toIntOrNull() ?: defaultMinute
                        }
                        singlePart.length == 3 -> { // e.g. "930"
                            rawHour = singlePart.substring(0, 1).toIntOrNull() ?: defaultHour
                            rawMinute = singlePart.substring(1, 3).toIntOrNull() ?: defaultMinute
                        }
                        else -> {
                            rawHour = singlePart.toIntOrNull() ?: defaultHour
                            rawMinute = 0
                        }
                    }
                }
            }

            // Adjust 12-hour AM/PM if specified
            var finalHour = rawHour
            if (hasPm) {
                if (finalHour in 1..11) {
                    finalHour += 12
                }
            } else if (hasAm) {
                if (finalHour == 12) {
                    finalHour = 0
                }
            }

            return ParsedTime.ofSafe(finalHour, rawMinute)
        } catch (_: Exception) {
            return ParsedTime.ofSafe(defaultHour, defaultMinute)
        }
    }

    /**
     * Checks if a string represents a valid, strictly formatted "HH:mm" time within standard bounds (00:00 to 23:59).
     */
    fun isValidTime(timeStr: String?): Boolean {
        if (timeStr.isNullOrBlank()) return false
        val normalized = normalizeDigits(timeStr).trim()
        val parts = normalized.split(':')
        if (parts.size != 2) return false
        val hour = parts[0].trim().toIntOrNull() ?: return false
        val minute = parts[1].trim().toIntOrNull() ?: return false
        return hour in 0..23 && minute in 0..59
    }

    /**
     * Normalizes any input time string to a canonical "HH:mm" format.
     */
    fun normalizeTime(timeStr: String?, fallback: String = "09:00"): String {
        val defaultParsed = parseTime(fallback, 9, 0)
        val parsed = parseTime(timeStr, defaultParsed.hour, defaultParsed.minute)
        return parsed.canonicalTime
    }

    /**
     * Formats an hour and minute safely as canonical "HH:mm".
     */
    fun formatTime(hour: Int, minute: Int): String {
        return ParsedTime.ofSafe(hour, minute).canonicalTime
    }

    /**
     * Validates and creates a [LocalTime] instance safely.
     * Guaranteed to never throw [java.time.DateTimeException].
     */
    fun createSafeLocalTime(hour: Int, minute: Int): LocalTime {
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }

    /**
     * Safely converts a canonical date ("YYYY-MM-DD") and canonical time ("HH:mm")
     * into a [LocalDateTime].
     * Never crashes if date or time are invalid.
     */
    fun createSafeLocalDateTime(dateStr: String?, timeStr: String?): LocalDateTime {
        val parsedTime = parseTime(timeStr)
        val localTime = createSafeLocalTime(parsedTime.hour, parsedTime.minute)

        val localDate = try {
            if (!dateStr.isNullOrBlank()) {
                val parts = dateStr.trim().split('-')
                if (parts.size == 3) {
                    val y = parts[0].toIntOrNull()?.coerceIn(1900, 2100) ?: 2026
                    val m = parts[1].toIntOrNull()?.coerceIn(1, 12) ?: 9
                    val d = parts[2].toIntOrNull()?.coerceIn(1, 31) ?: 11
                    LocalDate.of(y, m, d)
                } else {
                    DateUtils.getRealDeviceLocalDate()
                }
            } else {
                DateUtils.getRealDeviceLocalDate()
            }
        } catch (_: Exception) {
            DateUtils.getRealDeviceLocalDate()
        }

        return LocalDateTime.of(localDate, localTime)
    }

    /**
     * Ensures that endTime is strictly after startTime.
     * If endTime is less than or equal to startTime, automatically adjusts endTime
     * to (startTime + defaultDurationMinutes), capped safely at 23:59.
     */
    fun ensureValidRange(
        startTime: String?,
        endTime: String?,
        defaultDurationMinutes: Int = 60
    ): Pair<String, String> {
        val start = parseTime(startTime, 9, 0)
        val end = parseTime(endTime, 10, 0)

        val startMinutes = start.minutesOfDay
        val endMinutes = end.minutesOfDay

        return if (endMinutes <= startMinutes) {
            val newEndMinutes = (startMinutes + defaultDurationMinutes).coerceAtMost(23 * 60 + 59)
            val newEndHour = newEndMinutes / 60
            val newEndMinute = newEndMinutes % 60
            Pair(start.canonicalTime, formatTime(newEndHour, newEndMinute))
        } else {
            Pair(start.canonicalTime, end.canonicalTime)
        }
    }

    /**
     * Calculates duration in minutes between start and end times.
     */
    fun calculateDurationMinutes(startTime: String?, endTime: String?): Int {
        val start = parseTime(startTime, 9, 0)
        val end = parseTime(endTime, 10, 0)
        val diff = end.minutesOfDay - start.minutesOfDay
        return if (diff > 0) diff else 0
    }

    /**
     * Adds minutes to a time string and returns the new canonical time.
     */
    fun addMinutes(timeStr: String?, minutesToAdd: Int): String {
        val parsed = parseTime(timeStr, 9, 0)
        val newTotal = (parsed.minutesOfDay + minutesToAdd).coerceIn(0, 23 * 60 + 59)
        return formatTime(newTotal / 60, newTotal % 60)
    }
}
