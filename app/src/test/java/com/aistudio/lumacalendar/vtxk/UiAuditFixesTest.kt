package com.aistudio.lumacalendar.vtxk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.notification.NotificationPreferences
import com.aistudio.lumacalendar.vtxk.ui.components.sanitizeEventTitle
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentElectricBlue
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentRoyalViolet
import com.aistudio.lumacalendar.vtxk.ui.theme.LumaThemeMode
import com.aistudio.lumacalendar.vtxk.ui.theme.resolveAppearance
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.AccentPresets
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.filterVisibleEvents
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers the testing requirements for specs/009-fix-ui-audit-issues:
 * TR-001 category filtering + visibility round-trip, TR-002 appearance
 * round-trip + first-run defaults, TR-003 blank-title validation.
 */
@RunWith(RobolectricTestRunner::class)
class UiAuditFixesTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun clearPreferences() {
        // SP-001: all keys share the existing notification_preferences file.
        context.getSharedPreferences("notification_preferences", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    // --- TR-002: first-run defaults ---

    @Test
    fun firstRunUsesDocumentedDefaults() {
        assertEquals(0, NotificationPreferences.getAccentColorIndex(context))
        assertFalse(NotificationPreferences.getFirstDayMonday(context))
        assertFalse(NotificationPreferences.getShowWeekNumbers(context))
        assertEquals("Liquid Glass (Dark)", NotificationPreferences.getThemeName(context))

        assertTrue(NotificationPreferences.getCategoryPersonalVisible(context))
        assertTrue(NotificationPreferences.getCategoryWorkVisible(context))
        assertTrue(NotificationPreferences.getCategoryHolidaysVisible(context))

        // Pre-existing keys keep their own defaults
        assertEquals(CalendarType.JALALI, NotificationPreferences.getCalendarType(context))
        assertEquals(60, NotificationPreferences.getSnoozeMinutes(context))
    }

    // --- TR-002: appearance round-trip ---

    @Test
    fun appearanceSettingsRoundTrip() {
        NotificationPreferences.setAccentColorIndex(context, 3)
        NotificationPreferences.setFirstDayMonday(context, true)
        NotificationPreferences.setShowWeekNumbers(context, true)
        NotificationPreferences.setThemeName(context, "OLED Deep")

        assertEquals(3, NotificationPreferences.getAccentColorIndex(context))
        assertTrue(NotificationPreferences.getFirstDayMonday(context))
        assertTrue(NotificationPreferences.getShowWeekNumbers(context))
        assertEquals("OLED Deep", NotificationPreferences.getThemeName(context))
    }

    // --- TR-001: category visibility round-trip ---

    @Test
    fun categoryVisibilityRoundTrip() {
        NotificationPreferences.setCategoryPersonalVisible(context, false)
        NotificationPreferences.setCategoryWorkVisible(context, false)
        NotificationPreferences.setCategoryHolidaysVisible(context, false)

        assertFalse(NotificationPreferences.getCategoryPersonalVisible(context))
        assertFalse(NotificationPreferences.getCategoryWorkVisible(context))
        assertFalse(NotificationPreferences.getCategoryHolidaysVisible(context))

        NotificationPreferences.setCategoryWorkVisible(context, true)
        assertTrue(NotificationPreferences.getCategoryWorkVisible(context))
        assertFalse(NotificationPreferences.getCategoryPersonalVisible(context))
    }

    @Test
    fun newPreferencesLeaveExistingKeysUntouched() {
        NotificationPreferences.setCalendarType(context, CalendarType.GREGORIAN)
        NotificationPreferences.setSnoozeMinutes(context, 30)
        NotificationPreferences.setDailyNotificationEnabled(context, false)

        NotificationPreferences.setAccentColorIndex(context, 2)
        NotificationPreferences.setCategoryWorkVisible(context, false)
        NotificationPreferences.setFirstDayMonday(context, true)

        assertEquals(CalendarType.GREGORIAN, NotificationPreferences.getCalendarType(context))
        assertEquals(30, NotificationPreferences.getSnoozeMinutes(context))
        assertFalse(NotificationPreferences.isDailyNotificationEnabled(context))
    }

    // --- TR-001: category filtering ---

    @Test
    fun filteringHidesOnlyTheDisabledCategories() {
        val events = listOf(
            event("personal", "Personal"),
            event("work", "Work"),
            event("holidays", "Holidays")
        )

        assertEquals(
            listOf("personal", "work", "holidays"),
            filterVisibleEvents(events, personalVisible = true, workVisible = true, holidaysVisible = true)
                .map { it.title }
        )

        assertEquals(
            listOf("personal", "holidays"),
            filterVisibleEvents(events, personalVisible = true, workVisible = false, holidaysVisible = true)
                .map { it.title }
        )

        assertEquals(
            listOf("work"),
            filterVisibleEvents(events, personalVisible = false, workVisible = true, holidaysVisible = false)
                .map { it.title }
        )

        // All three off -> no user events shown (SC-001 acceptance 3)
        assertTrue(
            filterVisibleEvents(events, personalVisible = false, workVisible = false, holidaysVisible = false)
                .isEmpty()
        )
    }

    @Test
    fun filteringLeavesUnknownCalendarTypesVisible() {
        val events = listOf(
            event("personal", "Personal"),
            event("other", "Something Else"),
            event("blank", "")
        )

        assertEquals(
            listOf("other", "blank"),
            filterVisibleEvents(events, personalVisible = false, workVisible = false, holidaysVisible = false)
                .map { it.title }
        )
    }

    @Test
    fun filteringWithEmptyEventListDoesNotFail() {
        assertTrue(
            filterVisibleEvents(emptyList(), personalVisible = false, workVisible = false, holidaysVisible = false)
                .isEmpty()
        )
    }

    // --- TR-003: blank-title validation ---

    @Test
    fun blankTitlesAreRejected() {
        assertNull(sanitizeEventTitle(""))
        assertNull(sanitizeEventTitle("   "))
        assertNull(sanitizeEventTitle("\t\n\r"))
        // Unicode whitespace must count as blank (VB-001): NBSP, EM SPACE, LINE SEPARATOR
        assertNull(sanitizeEventTitle(" "))
        assertNull(sanitizeEventTitle(" "))
        assertNull(sanitizeEventTitle(" "))
        assertNull(sanitizeEventTitle("    "))
    }

    @Test
    fun validTitlesPassTrimmed() {
        assertEquals("Standup", sanitizeEventTitle("  Standup  "))
        assertEquals("جلسه تیم", sanitizeEventTitle("  جلسه تیم  "))
        assertEquals("09:00 sync", sanitizeEventTitle("09:00 sync"))
        // Only the edges are trimmed; interior whitespace is preserved
        assertEquals("Team sync", sanitizeEventTitle("  Team sync  "))
        // Interior NBSP survives trimming
        assertEquals("A B", sanitizeEventTitle("  A B  "))
    }

    // --- spec 010: appearance selection round-trips through persistence (SC-003/SC-006) ---

    @Test
    fun appearanceSelectionRoundTripsThroughPersistence() {
        NotificationPreferences.setThemeName(context, "OLED Deep")
        NotificationPreferences.setAccentColorIndex(context, 2)

        val appearance = resolveAppearance(
            NotificationPreferences.getThemeName(context),
            NotificationPreferences.getAccentColorIndex(context)
        )

        assertEquals(LumaThemeMode.OLED_DEEP, appearance.theme)
        assertEquals(AccentPresets[2].primary, appearance.accentPrimary)
        assertEquals(AccentPresets[2].secondary, appearance.accentSecondary)

        NotificationPreferences.setThemeName(context, "Liquid Glass")
        NotificationPreferences.setAccentColorIndex(context, 4)
        val second = resolveAppearance(
            NotificationPreferences.getThemeName(context),
            NotificationPreferences.getAccentColorIndex(context)
        )
        assertEquals(LumaThemeMode.LIQUID_GLASS, second.theme)
        assertEquals(AccentPresets[4].primary, second.accentPrimary)
    }

    @Test
    fun firstRunAppearanceResolvesToBaseline() {
        // @Before cleared the prefs file, so both getters return their documented defaults
        val appearance = resolveAppearance(
            NotificationPreferences.getThemeName(context),
            NotificationPreferences.getAccentColorIndex(context)
        )

        assertEquals(LumaThemeMode.LIQUID_GLASS, appearance.theme)
        assertEquals(AccentElectricBlue, appearance.accentPrimary)
        assertEquals(AccentRoyalViolet, appearance.accentSecondary)
    }

    @Test
    fun appearanceChangesLeaveEveryOtherSettingUntouched() {
        // Arrange: pin every unrelated preference to a non-default value
        NotificationPreferences.setCalendarType(context, CalendarType.GREGORIAN)
        NotificationPreferences.setSnoozeMinutes(context, 45)
        NotificationPreferences.setCategoryPersonalVisible(context, false)
        NotificationPreferences.setCategoryWorkVisible(context, false)
        NotificationPreferences.setCategoryHolidaysVisible(context, false)
        NotificationPreferences.setFirstDayMonday(context, true)
        NotificationPreferences.setShowWeekNumbers(context, true)
        NotificationPreferences.setDailyNotificationEnabled(context, false)
        NotificationPreferences.setEventRemindersEnabled(context, false)
        NotificationPreferences.markPermissionRequested(context)

        // Act: change only the two appearance preferences
        NotificationPreferences.setThemeName(context, "OLED Deep")
        NotificationPreferences.setAccentColorIndex(context, 3)
        NotificationPreferences.setThemeName(context, "Liquid Glass (Dark)")
        NotificationPreferences.setAccentColorIndex(context, 1)

        // Assert: every other key retains its stored value
        assertEquals(CalendarType.GREGORIAN, NotificationPreferences.getCalendarType(context))
        assertEquals(45, NotificationPreferences.getSnoozeMinutes(context))
        assertFalse(NotificationPreferences.getCategoryPersonalVisible(context))
        assertFalse(NotificationPreferences.getCategoryWorkVisible(context))
        assertFalse(NotificationPreferences.getCategoryHolidaysVisible(context))
        assertTrue(NotificationPreferences.getFirstDayMonday(context))
        assertTrue(NotificationPreferences.getShowWeekNumbers(context))
        assertFalse(NotificationPreferences.isDailyNotificationEnabled(context))
        assertFalse(NotificationPreferences.areEventRemindersEnabled(context))
        assertTrue(NotificationPreferences.wasPermissionRequested(context))
    }

    private fun event(title: String, calendarType: String) = CalendarEvent(
        title = title,
        date = "2026-09-22",
        startTime = "09:00",
        category = "Personal",
        colorHex = "#38BDF8",
        calendarType = calendarType
    )
}
