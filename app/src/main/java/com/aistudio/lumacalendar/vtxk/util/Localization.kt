package com.aistudio.lumacalendar.vtxk.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

/**
 * Reusable localization contract containing all application text strings.
 */
data class AppStrings(
    // Bottom Tab Bar
    val tabCalendar: String,
    val tabAgenda: String,
    val tabSearch: String,
    val tabSettings: String,

    // Calendar View Modes
    val viewMonth: String,
    val viewWeek: String,
    val viewDay: String,

    // Calendar Types
    val calJalali: String,
    val calGregorian: String,
    val calHijri: String,

    // Calendar Header & Actions
    val today: String,
    val previousMonth: String,
    val nextMonth: String,
    val addEvent: String,
    val newEvent: String,
    val eventsCountSuffix: String,
    val noEventsForDate: String,
    val addEventPrompt: String,
    val todayPrefix: String,

    // Agenda Screen
    val agendaTitle: String,
    val agendaSubtitle: String,
    val noEventsFound: String,
    val tapToCreateEvent: String,

    // Search Screen
    val searchTitle: String,
    val searchSubtitle: String,
    val searchPlaceholder: String,
    val findEventsTitle: String,
    val findEventsSubtitle: String,
    val noMatchingEvents: String,
    val tryDifferentKeywords: String,
    val allCategories: String,
    val catWork: String,
    val catPersonal: String,
    val catMeeting: String,
    val catBirthday: String,
    val catFocus: String,
    val catSpecial: String,

    // Settings Screen
    val settingsTitle: String,
    val settingsSubtitle: String,
    val accentColorTitle: String,
    val calendarPreferencesTitle: String,
    val firstDayMondayTitle: String,
    val firstDayMondayDesc: String,
    val showWeekNumbersTitle: String,
    val showWeekNumbersDesc: String,
    val notificationsTitle: String,
    val notificationsDesc: String,
    val visibleCalendarsTitle: String,
    val calPersonalTitle: String,
    val calWorkTitle: String,
    val calHolidaysTitle: String,
    val appearanceTitle: String,
    val themeLiquidGlass: String,
    val themeDeepAmoled: String,
    val themeMidnightBlue: String,
    val themeEmeraldGlow: String,
    val dataManagementTitle: String,
    val resetSampleTitle: String,
    val resetSampleDesc: String,
    val clearAllTitle: String,
    val clearAllDesc: String,
    val resetSuccessTitle: String,
    val resetSuccessDesc: String,
    val clearSuccessTitle: String,
    val clearSuccessDesc: String,
    val aboutTitle: String,
    val aboutSubtitle: String,
    val aboutVersion: String,

    // Add / Edit Event Sheet
    val editEventTitle: String,
    val newEventSubtitle: String,
    val editEventSubtitle: String,
    val save: String,
    val cancel: String,
    val titleLabel: String,
    val titlePlaceholder: String,
    val calendarSystemLabel: String,
    val dateTimeSection: String,
    val dateLabel: String,
    val startLabel: String,
    val endLabel: String,
    val duration15m: String,
    val duration30m: String,
    val duration1h: String,
    val duration2h: String,
    val categorySection: String,
    val locationLabel: String,
    val locationPlaceholder: String,
    val notesLabel: String,
    val notesPlaceholder: String,
    val reminderSection: String,
    val reminderAtTime: String,
    val reminder5m: String,
    val reminder10m: String,
    val reminder15m: String,
    val reminder30m: String,
    val reminder1h: String,
    val reminder1d: String,
    val reminderNone: String,
    val titleRequiredError: String,

    // Event Detail Sheet
    val edit: String,
    val delete: String,
    val close: String,
    val timeLabel: String,
    val calendarLabel: String,
    val deleteDialogTitle: String,
    val deleteDialogMessage: String,
    val deleteDialogConfirm: String,
    val deleteDialogCancel: String
) {
    val editEvent: String get() = editEventTitle
    val titleSection: String get() = titleLabel
    val eventTitlePlaceholder: String get() = titlePlaceholder
    val categoryAndColor: String get() = categorySection
    val date: String get() = dateLabel
    val tomorrow: String get() = if (tabCalendar == "تقویم") "فردا" else "Tomorrow"
    val time: String get() = timeLabel
    val locationSection: String get() = locationLabel
    val remindersAndCalendar: String get() = reminderSection
    val alert: String get() = reminderSection
    val calendarSection: String get() = calendarPreferencesTitle
    val categoryPersonal: String get() = catPersonal
    val categoryWork: String get() = catWork
    val categoryHolidays: String get() = calHolidaysTitle
    val notesSection: String get() = notesLabel
    val location: String get() = locationLabel
    val reminder: String get() = reminderSection
    val notes: String get() = notesLabel
    val deleteEventConfirm: String get() = deleteDialogTitle
    val actionCannotBeUndone: String get() = deleteDialogMessage
    val agenda: String get() = agendaTitle
    val all: String get() = allCategories
    val searchEvents: String get() = searchTitle
    val findAnyEvent: String get() = findEventsTitle
    val typeKeywordsPrompt: String get() = findEventsSubtitle
    val tryAnotherKeywordPrompt: String get() = tryDifferentKeywords
    val settings: String get() = settingsTitle
    val appearance: String get() = appearanceTitle
    val themeSurface: String get() = appearanceTitle
    val accentPalette: String get() = accentColorTitle
    val startWeekMonday: String get() = firstDayMondayTitle
    val showWeekNumbers: String get() = showWeekNumbersTitle
    val notifications: String get() = notificationsTitle
    val eventReminders: String get() = notificationsTitle
    val eventRemindersDesc: String get() = notificationsDesc
    val calendarsVisibility: String get() = visibleCalendarsTitle
    val sampleDataTitle: String get() = dataManagementTitle
    val resetSampleButton: String get() = resetSampleTitle
    val about: String get() = aboutTitle
    val lumaCalendar: String get() = aboutTitle
    val lumaVersion: String get() = aboutVersion
    val lumaDescription: String get() = aboutSubtitle

    fun formatReminder(minutes: Int?): String = LocalizationManager.getReminderLabel(minutes, this)

    companion object {
        val Persian = AppStrings(
            tabCalendar = "تقویم",
            tabAgenda = "رویدادها",
            tabSearch = "جستجو",
            tabSettings = "تنظیمات",

            viewMonth = "ماه",
            viewWeek = "هفته",
            viewDay = "روز",

            calJalali = "شمسی",
            calGregorian = "میلادی",
            calHijri = "قمری",

            today = "امروز",
            previousMonth = "ماه قبل",
            nextMonth = "ماه بعد",
            addEvent = "+ افزودن رویداد",
            newEvent = "رویداد جدید",
            eventsCountSuffix = "رویداد",
            noEventsForDate = "هیچ رویدادی برای این روز ثبت نشده است",
            addEventPrompt = "برای مدیریت بهتر برنامه‌ها، رویداد جدیدی اضافه کنید.",
            todayPrefix = "امروز • ",

            agendaTitle = "برنامه زمانی",
            agendaSubtitle = "همه رویدادها و یادآوری‌های زمان‌بندی شده",
            noEventsFound = "رویدادی یافت نشد",
            tapToCreateEvent = "برای ثبت رویداد جدید، دکمه + را لمس کنید",

            searchTitle = "جستجو",
            searchSubtitle = "جستجو در میان تمام رویدادها، یادداشت‌ها و مکان‌ها",
            searchPlaceholder = "جستجوی رویداد، مکان، یادداشت...",
            findEventsTitle = "یافتن سریع رویدادها",
            findEventsSubtitle = "عنوان یا کلمه‌ای را در کادر بالا جستجو کنید",
            noMatchingEvents = "رویداد مرتبطی پیدا نشد",
            tryDifferentKeywords = "کلمات کلیدی دیگری را جستجو کنید",
            allCategories = "همه",
            catWork = "کاری",
            catPersonal = "شخصی",
            catMeeting = "جلسه",
            catBirthday = "تولد",
            catFocus = "تمرکز",
            catSpecial = "خاص",

            settingsTitle = "تنظیمات",
            settingsSubtitle = "شخصی‌سازی و تنظیمات تقویم",
            accentColorTitle = "رنگ شاخص",
            calendarPreferencesTitle = "تنظیمات تقویم",
            firstDayMondayTitle = "شروع هفته از دوشنبه",
            firstDayMondayDesc = "شروع هفته از دوشنبه به جای یکشنبه در تقویم میلادی",
            showWeekNumbersTitle = "نمایش شماره هفته",
            showWeekNumbersDesc = "نمایش شماره هفته در ستون کناری تقویم",
            notificationsTitle = "اعلان‌ها و یادآوری‌ها",
            notificationsDesc = "دریافت هشدار قبل از شروع هر رویداد",
            visibleCalendarsTitle = "تقویم‌های فعال",
            calPersonalTitle = "رویدادهای شخصی",
            calWorkTitle = "کاری و شغلی",
            calHolidaysTitle = "تعطیلات رسمی",
            appearanceTitle = "طراحی و پوسته",
            themeLiquidGlass = "شیشه‌ای شفاف",
            themeDeepAmoled = "مشکی عمیق",
            themeMidnightBlue = "آبی شب",
            themeEmeraldGlow = "زمرد درخشان",
            dataManagementTitle = "مدیریت داده‌ها",
            resetSampleTitle = "بازنشانی اطلاعات نمونه",
            resetSampleDesc = "بارگذاری مجدد رویدادهای پیش‌فرض در تقویم",
            clearAllTitle = "پاک کردن تمام رویدادها",
            clearAllDesc = "حذف دائمی تمام رویدادهای ذخیره‌شده",
            resetSuccessTitle = "بازنشانی انجام شد",
            resetSuccessDesc = "رویدادهای نمونه با موفقیت بازیابی شدند.",
            clearSuccessTitle = "اطلاعات پاک شد",
            clearSuccessDesc = "تمام رویدادها با موفقیت حذف شدند.",
            aboutTitle = "درباره تقویم لوما",
            aboutSubtitle = "تقویم هوشمند سه‌گانه • شمسی • میلادی • قمری",
            aboutVersion = "نسخه ۱.۰.۰ (طراحی شیشه‌ای)",

            editEventTitle = "ویرایش رویداد",
            newEventSubtitle = "برنامه‌ریزی و ثبت رویداد جدید",
            editEventSubtitle = "به‌روزرسانی جزئیات رویداد",
            save = "ذخیره",
            cancel = "انصراف",
            titleLabel = "عنوان رویداد",
            titlePlaceholder = "مثلاً جلسه کاری، دندانپزشکی...",
            calendarSystemLabel = "سیستم تقویم",
            dateTimeSection = "تاریخ و زمان",
            dateLabel = "تاریخ",
            startLabel = "شروع",
            endLabel = "پایان",
            duration15m = "۱۵ دقیقه",
            duration30m = "۳۰ دقیقه",
            duration1h = "۱ ساعت",
            duration2h = "۲ ساعت",
            categorySection = "دسته‌بندی",
            locationLabel = "مکان",
            locationPlaceholder = "افزودن آدرس یا پیوند جلسه آنلاین",
            notesLabel = "یادداشت‌ها",
            notesPlaceholder = "جزئیات، سرفصل‌ها یا یادداشت‌های دیگر...",
            reminderSection = "یادآوری",
            reminderAtTime = "هم‌زمان با رویداد",
            reminder5m = "۵ دقیقه قبل",
            reminder10m = "۱۰ دقیقه قبل",
            reminder15m = "۱۵ دقیقه قبل",
            reminder30m = "۳۰ دقیقه قبل",
            reminder1h = "۱ ساعت قبل",
            reminder1d = "۱ روز قبل",
            reminderNone = "بدون یادآوری",
            titleRequiredError = "لطفاً عنوانی برای رویداد وارد کنید",

            edit = "ویرایش",
            delete = "حذف",
            close = "بستن",
            timeLabel = "زمان",
            calendarLabel = "تقویم",
            deleteDialogTitle = "حذف این رویداد؟",
            deleteDialogMessage = "این رویداد به صورت دائمی حذف خواهد شد و غیرقابل بازگشت است.",
            deleteDialogConfirm = "حذف",
            deleteDialogCancel = "انصراف"
        )

        val English = AppStrings(
            tabCalendar = "Calendar",
            tabAgenda = "Agenda",
            tabSearch = "Search",
            tabSettings = "Settings",

            viewMonth = "Month",
            viewWeek = "Week",
            viewDay = "Day",

            calJalali = "Jalali",
            calGregorian = "Gregorian",
            calHijri = "Hijri",

            today = "Today",
            previousMonth = "Previous Month",
            nextMonth = "Next Month",
            addEvent = "+ Add Event",
            newEvent = "New Event",
            eventsCountSuffix = "events",
            noEventsForDate = "No events scheduled for this date",
            addEventPrompt = "Add an event to keep your schedule organized.",
            todayPrefix = "Today • ",

            agendaTitle = "Agenda",
            agendaSubtitle = "All scheduled events and reminders",
            noEventsFound = "No events found",
            tapToCreateEvent = "Tap the + button to create a new event",

            searchTitle = "Search",
            searchSubtitle = "Search across all events, notes & locations",
            searchPlaceholder = "Search events, tags, locations...",
            findEventsTitle = "Find any event instantly",
            findEventsSubtitle = "Type a keyword above to find events",
            noMatchingEvents = "No matching events found",
            tryDifferentKeywords = "Try searching with different keywords",
            allCategories = "All",
            catWork = "Work",
            catPersonal = "Personal",
            catMeeting = "Meeting",
            catBirthday = "Birthday",
            catFocus = "Focus",
            catSpecial = "Special",

            settingsTitle = "Settings",
            settingsSubtitle = "Preferences & Appearance",
            accentColorTitle = "Accent Color",
            calendarPreferencesTitle = "Calendar Preferences",
            firstDayMondayTitle = "First day of week Monday",
            firstDayMondayDesc = "Start calendar grid from Monday instead of Sunday",
            showWeekNumbersTitle = "Show week numbers",
            showWeekNumbersDesc = "Display ISO week numbers along the calendar",
            notificationsTitle = "Notifications & Reminders",
            notificationsDesc = "Receive alerts before scheduled events",
            visibleCalendarsTitle = "Visible Calendars",
            calPersonalTitle = "Personal Events",
            calWorkTitle = "Work & Business",
            calHolidaysTitle = "Holidays & Celebrations",
            appearanceTitle = "Appearance & Theme",
            themeLiquidGlass = "Liquid Glass",
            themeDeepAmoled = "Deep AMOLED",
            themeMidnightBlue = "Midnight Blue",
            themeEmeraldGlow = "Emerald Glow",
            dataManagementTitle = "Data Management",
            resetSampleTitle = "Reset Sample Data",
            resetSampleDesc = "Populate calendar with default schedule",
            clearAllTitle = "Clear All Events",
            clearAllDesc = "Permanently delete all saved events",
            resetSuccessTitle = "Data Reset",
            resetSuccessDesc = "Sample events have been restored successfully.",
            clearSuccessTitle = "All Data Cleared",
            clearSuccessDesc = "All events have been removed.",
            aboutTitle = "About Luma Calendar",
            aboutSubtitle = "Universal Triple-Calendar • Jalali • Gregorian • Hijri",
            aboutVersion = "Version 1.0.0 (Glass Edition)",

            editEventTitle = "Edit Event",
            newEventSubtitle = "Schedule something on your calendar",
            editEventSubtitle = "Update your event details",
            save = "Save",
            cancel = "Cancel",
            titleLabel = "Event title",
            titlePlaceholder = "e.g., Team Sync or Dentist",
            calendarSystemLabel = "Calendar System",
            dateTimeSection = "Date & Time",
            dateLabel = "Date",
            startLabel = "Start",
            endLabel = "End",
            duration15m = "15m",
            duration30m = "30m",
            duration1h = "1h",
            duration2h = "2h",
            categorySection = "Category",
            locationLabel = "Location",
            locationPlaceholder = "Add a location or link",
            notesLabel = "Notes",
            notesPlaceholder = "Additional notes or agenda items...",
            reminderSection = "Reminder",
            reminderAtTime = "At time of event",
            reminder5m = "5 minutes before",
            reminder10m = "10 minutes before",
            reminder15m = "15 minutes before",
            reminder30m = "30 minutes before",
            reminder1h = "1 hour before",
            reminder1d = "1 day before",
            reminderNone = "None",
            titleRequiredError = "Please enter a title for the event",

            edit = "Edit",
            delete = "Delete",
            close = "Close",
            timeLabel = "Time",
            calendarLabel = "Calendar",
            deleteDialogTitle = "Delete this event?",
            deleteDialogMessage = "This action cannot be undone and will permanently remove this event.",
            deleteDialogConfirm = "Delete",
            deleteDialogCancel = "Cancel"
        )
    }
}

val LocalAppStrings = staticCompositionLocalOf { AppStrings.English }
val LocalCalendarType = staticCompositionLocalOf { CalendarType.JALALI }

/**
 * Convenience accessor for the current localized AppStrings.
 */
val strings: AppStrings
    @Composable
    get() = LocalAppStrings.current

/**
 * Localization and Direction Manager.
 * Governs bidirectional layout, typography rules, number formatting, and text resolution.
 */
object LocalizationManager {

    /**
     * Jalali and Hijri calendars operate in Persian language with RTL layout.
     * Gregorian calendar operates in English language with LTR layout.
     */
    fun isRtl(calendarType: CalendarType): Boolean =
        calendarType == CalendarType.JALALI || calendarType == CalendarType.HIJRI

    fun getLayoutDirection(calendarType: CalendarType): LayoutDirection =
        if (isRtl(calendarType)) LayoutDirection.Rtl else LayoutDirection.Ltr

    fun getLocale(calendarType: CalendarType): Locale =
        if (isRtl(calendarType)) Locale("fa") else Locale.US

    fun getStrings(calendarType: CalendarType): AppStrings =
        if (isRtl(calendarType)) AppStrings.Persian else AppStrings.English

    /**
     * Formats numeric digits according to the active calendar mode.
     */
    fun formatDigits(text: String): String = CalendarConverter.toPersianDigits(text)

    fun formatDigits(text: String, isRtl: Boolean): String =
        if (isRtl) CalendarConverter.toPersianDigits(text) else text

    fun formatDigits(text: String, calendarType: CalendarType): String {
        return when (calendarType) {
            CalendarType.JALALI, CalendarType.HIJRI -> CalendarConverter.toPersianDigits(text)
            CalendarType.GREGORIAN -> text
        }
    }

    /**
     * Formats a time string (e.g., "09:30") with localized numerals.
     */
    fun formatTime(timeStr: String, calendarType: CalendarType): String {
        return formatDigits(timeStr, calendarType)
    }

    /**
     * Formats a time range (e.g., "09:00 – 10:30") with localized numerals.
     */
    fun formatTimeRange(start: String, end: String, isRtl: Boolean): String {
        val s = if (isRtl) formatDigits(start) else start
        val e = if (isRtl) formatDigits(end) else end
        return "$s – $e"
    }

    fun formatTimeRange(start: String, end: String, calendarType: CalendarType): String {
        val s = formatDigits(start, calendarType)
        val e = formatDigits(end, calendarType)
        return "$s – $e"
    }

    /**
     * Returns the localized display name for a category.
     */
    fun getCategoryName(category: String, strings: AppStrings): String {
        return when (category.lowercase(Locale.ROOT)) {
            "work" -> strings.catWork
            "personal" -> strings.catPersonal
            "meeting" -> strings.catMeeting
            "birthday" -> strings.catBirthday
            "focus" -> strings.catFocus
            "special" -> strings.catSpecial
            else -> category
        }
    }

    /**
     * Returns the localized display text for a reminder offset in minutes.
     */
    fun getReminderLabel(minutes: Int?, strings: AppStrings): String {
        return when (minutes) {
            0 -> strings.reminderAtTime
            5 -> strings.reminder5m
            10 -> strings.reminder10m
            15 -> strings.reminder15m
            30 -> strings.reminder30m
            60 -> strings.reminder1h
            1440 -> strings.reminder1d
            else -> strings.reminderNone
        }
    }

    fun formatReminder(minutes: Int?, strings: AppStrings): String = getReminderLabel(minutes, strings)

    /**
     * Returns the localized display text for calendar type.
     */
    fun getCalendarTypeName(type: CalendarType, strings: AppStrings): String {
        return when (type) {
            CalendarType.JALALI -> strings.calJalali
            CalendarType.GREGORIAN -> strings.calGregorian
            CalendarType.HIJRI -> strings.calHijri
        }
    }

    /**
     * Returns the localized display text for calendar view mode.
     */
    fun getViewModeName(mode: String, strings: AppStrings): String {
        return when (mode.lowercase(Locale.ROOT)) {
            "month", "ماه" -> strings.viewMonth
            "week", "هفته" -> strings.viewWeek
            "day", "روز" -> strings.viewDay
            else -> mode
        }
    }
}
