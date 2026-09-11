package com.aistudio.lumacalendar.vtxk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aistudio.lumacalendar.vtxk.R

/**
 * Vazirmatn local font resources:
 * vazirmatn_regular.ttf   -> Regular (FontWeight.Normal / 400)
 * vazirmatn_medium.ttf    -> Medium (FontWeight.Medium / 500)
 * vazirmatn_semibold.ttf  -> SemiBold (FontWeight.SemiBold / 600)
 * vazirmatn_bold.ttf      -> Bold (FontWeight.Bold / 700)
 */
val VazirmatnRegular = Font(R.font.vazirmatn_regular, FontWeight.Normal)
val VazirmatnMedium = Font(R.font.vazirmatn_medium, FontWeight.Medium)
val VazirmatnSemiBold = Font(R.font.vazirmatn_semibold, FontWeight.SemiBold)
val VazirmatnBold = Font(R.font.vazirmatn_bold, FontWeight.Bold)

val VazirmatnFontFamily = FontFamily(
    VazirmatnRegular,
    VazirmatnMedium,
    VazirmatnSemiBold,
    VazirmatnBold
)

// Shorthand alias
val Vazirmatn = VazirmatnFontFamily

/**
 * Reusable typography system strictly enforcing the Vazirmatn Font Weight Rules:
 *
 * REGULAR (Weight 400):
 * - Normal body text
 * - Calendar day numbers
 * - Secondary information
 * - Event descriptions
 * - Notes
 * - Locations
 * - Empty-state text
 * - Supporting labels
 *
 * MEDIUM (Weight 500):
 * - Buttons
 * - Navigation items
 * - Tabs
 * - Calendar weekday names
 * - Time labels
 * - Small UI labels
 * - Category names
 * - Reminder labels
 * - Input text
 * - Selected calendar information
 *
 * SEMIBOLD (Weight 600):
 * - Month names
 * - Calendar year
 * - Section titles
 * - Event titles
 * - Screen titles
 * - Important dates
 * - Dialog titles
 * - Card titles
 * - Selected/active labels
 * - Important numbers
 *
 * BOLD (Weight 700 - ONLY for):
 * - Main screen title when needed
 * - Very important highlighted information
 * - Major headings
 * - Special emphasis
 * - Important alert/error text
 */
object VazirmatnTypography {
    // --- REGULAR WEIGHT (FontWeight.Normal) ---
    val bodyText = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextWhitePrimary
    )
    val calendarDayNumber = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextWhitePrimary
    )
    val secondaryInfo = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextWhiteSecondary
    )
    val eventDescription = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextWhitePrimary
    )
    val notes = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextWhitePrimary
    )
    val location = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextWhiteSecondary
    )
    val emptyState = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextWhiteSecondary
    )
    val supportingLabel = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextWhiteMuted
    )

    // --- MEDIUM WEIGHT (FontWeight.Medium) ---
    val button = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextWhitePrimary
    )
    val navItem = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = TextWhiteSecondary
    )
    val tab = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextWhiteSecondary
    )
    val weekdayName = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextWhiteMuted
    )
    val timeLabel = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextWhiteSecondary
    )
    val smallLabel = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextWhiteSecondary
    )
    val categoryName = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextWhitePrimary
    )
    val reminderLabel = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextWhitePrimary
    )
    val inputText = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextWhitePrimary
    )
    val selectedCalendarInfo = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextWhitePrimary
    )

    // --- SEMIBOLD WEIGHT (FontWeight.SemiBold) ---
    val monthName = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    )
    val calendarYear = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = TextWhiteSecondary
    )
    val sectionTitle = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    )
    val eventTitle = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TextWhitePrimary
    )
    val screenTitle = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = TextWhitePrimary
    )
    val importantDate = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = AccentElectricBlue
    )
    val dialogTitle = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TextWhitePrimary
    )
    val cardTitle = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TextWhitePrimary
    )
    val activeLabel = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextWhitePrimary
    )
    val importantNumber = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextWhitePrimary
    )

    // --- BOLD WEIGHT (FontWeight.Bold - ONLY for main titles, headings, highlights, alert/error) ---
    val mainScreenTitle = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    )
    val highlightedInfo = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = AccentElectricBlue
    )
    val majorHeading = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    )
    val specialEmphasis = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextWhitePrimary
    )
    val alertErrorText = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = CategorySpecial
    )
}

/**
 * Standard Material 3 Typography mapping using VazirmatnFontFamily.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        color = TextWhitePrimary
    ),
    displayMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        color = TextWhitePrimary
    ),
    displaySmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = TextWhitePrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold, // Main screen title when needed
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold, // Screen titles
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold, // Screen subtitles & dialog titles
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    ),
    titleLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold, // Section titles, card titles
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    ),
    titleMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold, // Event titles, section subheadings
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    ),
    titleSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium, // Selected calendar information, category names
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = TextWhiteSecondary
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal, // Normal body text, event descriptions
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal, // Secondary information, notes, locations
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = TextWhiteSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal, // Empty-state text, supporting labels
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = TextWhiteMuted
    ),
    labelLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium, // Buttons
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = TextWhitePrimary
    ),
    labelMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium, // Tabs, time labels, small UI labels
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = TextWhiteSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium, // Navigation items, calendar weekday names
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = TextWhiteMuted
    )
)

// Extension properties for semantic access directly on Typography
val Typography.bodyText: TextStyle get() = VazirmatnTypography.bodyText
val Typography.calendarDayNumber: TextStyle get() = VazirmatnTypography.calendarDayNumber
val Typography.secondaryInfo: TextStyle get() = VazirmatnTypography.secondaryInfo
val Typography.eventDescription: TextStyle get() = VazirmatnTypography.eventDescription
val Typography.notes: TextStyle get() = VazirmatnTypography.notes
val Typography.location: TextStyle get() = VazirmatnTypography.location
val Typography.emptyState: TextStyle get() = VazirmatnTypography.emptyState
val Typography.supportingLabel: TextStyle get() = VazirmatnTypography.supportingLabel

val Typography.button: TextStyle get() = VazirmatnTypography.button
val Typography.navItem: TextStyle get() = VazirmatnTypography.navItem
val Typography.tab: TextStyle get() = VazirmatnTypography.tab
val Typography.weekdayName: TextStyle get() = VazirmatnTypography.weekdayName
val Typography.timeLabel: TextStyle get() = VazirmatnTypography.timeLabel
val Typography.smallLabel: TextStyle get() = VazirmatnTypography.smallLabel
val Typography.categoryName: TextStyle get() = VazirmatnTypography.categoryName
val Typography.reminderLabel: TextStyle get() = VazirmatnTypography.reminderLabel
val Typography.inputText: TextStyle get() = VazirmatnTypography.inputText
val Typography.selectedCalendarInfo: TextStyle get() = VazirmatnTypography.selectedCalendarInfo

val Typography.monthName: TextStyle get() = VazirmatnTypography.monthName
val Typography.calendarYear: TextStyle get() = VazirmatnTypography.calendarYear
val Typography.sectionTitle: TextStyle get() = VazirmatnTypography.sectionTitle
val Typography.eventTitle: TextStyle get() = VazirmatnTypography.eventTitle
val Typography.screenTitle: TextStyle get() = VazirmatnTypography.screenTitle
val Typography.importantDate: TextStyle get() = VazirmatnTypography.importantDate
val Typography.dialogTitle: TextStyle get() = VazirmatnTypography.dialogTitle
val Typography.cardTitle: TextStyle get() = VazirmatnTypography.cardTitle
val Typography.activeLabel: TextStyle get() = VazirmatnTypography.activeLabel
val Typography.importantNumber: TextStyle get() = VazirmatnTypography.importantNumber

val Typography.mainScreenTitle: TextStyle get() = VazirmatnTypography.mainScreenTitle
val Typography.highlightedInfo: TextStyle get() = VazirmatnTypography.highlightedInfo
val Typography.majorHeading: TextStyle get() = VazirmatnTypography.majorHeading
val Typography.specialEmphasis: TextStyle get() = VazirmatnTypography.specialEmphasis
val Typography.alertErrorText: TextStyle get() = VazirmatnTypography.alertErrorText
