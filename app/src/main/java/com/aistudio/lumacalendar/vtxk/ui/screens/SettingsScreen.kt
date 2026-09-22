package com.aistudio.lumacalendar.vtxk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.lumacalendar.vtxk.ui.components.ConfirmActionDialog
import com.aistudio.lumacalendar.vtxk.ui.components.GlassButton
import com.aistudio.lumacalendar.vtxk.ui.components.GlassCard
import com.aistudio.lumacalendar.vtxk.ui.components.GlassToggle
import com.aistudio.lumacalendar.vtxk.ui.components.SectionHeader
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
import com.aistudio.lumacalendar.vtxk.ui.theme.CategoryHealth
import com.aistudio.lumacalendar.vtxk.ui.theme.CategoryPersonal
import com.aistudio.lumacalendar.vtxk.ui.theme.CategorySpecial
import com.aistudio.lumacalendar.vtxk.ui.theme.CategoryWork
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteMuted
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhitePrimary
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteSecondary
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.AccentPresets
import com.aistudio.lumacalendar.vtxk.util.LocalAppStrings
import com.aistudio.lumacalendar.vtxk.ui.theme.LocalLumaAppearance

@Composable
fun SettingsScreen(
    accentColorIndex: Int,
    onAccentColorSelect: (Int) -> Unit,
    firstDayMonday: Boolean,
    onFirstDayMondayChange: (Boolean) -> Unit,
    showWeekNumbers: Boolean,
    onShowWeekNumbersChange: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    snoozeMinutes: Int = 60,
    onSnoozeMinutesChange: (Int) -> Unit = {},
    personalVisible: Boolean,
    onTogglePersonal: () -> Unit,
    workVisible: Boolean,
    onToggleWork: () -> Unit,
    holidaysVisible: Boolean,
    onToggleHolidays: () -> Unit,
    themeName: String,
    onThemeSelect: (String) -> Unit,
    onResetSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showResetConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large Title
        item {
            Text(
                text = strings.settings,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhitePrimary,
                    fontSize = 34.sp,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // APPEARANCE SECTION
        item {
            SectionHeader(title = strings.appearance)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Theme Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = LocalLumaAppearance.current.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.themeSurface,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.sp
                                )
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Liquid Glass", "OLED Deep").forEach { th ->
                                val isSel = themeName.contains(th.substringBefore(" "))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSel) LocalLumaAppearance.current.accentPrimary.copy(alpha = 0.25f)
                                             else Color.Transparent
                                        )
                                        .border(
                                            0.8.dp,
                                            if (isSel) LocalLumaAppearance.current.accentPrimary else LocalLumaAppearance.current.borderSubtle,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onThemeSelect(th) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = th,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSel) Color.White else TextWhiteSecondary,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(LocalLumaAppearance.current.borderSubtle))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Accent Color Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.accentPalette,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextWhitePrimary,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.sp
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AccentPresets.forEachIndexed { idx, preset ->
                                val isSelected = accentColorIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        // Halo is drawn outside the 26dp bounds, so the row
                                        // layout, labels, order and positions do not move (FR-018)
                                        .then(
                                            if (isSelected) {
                                                Modifier.shadow(
                                                    elevation = 8.dp,
                                                    shape = CircleShape,
                                                    spotColor = Color.White,
                                                    ambientColor = Color.White
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clip(CircleShape)
                                        .background(preset.primary)
                                        .border(
                                            if (isSelected) 3.dp else 1.dp,
                                            if (isSelected) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { onAccentColorSelect(idx) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // CALENDAR CONFIGURATION SECTION
        item {
            SectionHeader(title = strings.calendarSection)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // First Day of Week
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = LocalLumaAppearance.current.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.startWeekMonday,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.sp
                                )
                            )
                        }

                        GlassToggle(
                            checked = firstDayMonday,
                            onCheckedChange = onFirstDayMondayChange,
                            testTag = "toggle_first_day_monday"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(LocalLumaAppearance.current.borderSubtle))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Show Week Numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ViewWeek,
                                contentDescription = null,
                                tint = LocalLumaAppearance.current.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.showWeekNumbers,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.sp
                                )
                            )
                        }

                        GlassToggle(
                            checked = showWeekNumbers,
                            onCheckedChange = onShowWeekNumbersChange,
                            testTag = "toggle_week_numbers"
                        )
                    }
                }
            }
        }

        // NOTIFICATIONS SECTION
        item {
            SectionHeader(title = strings.notifications)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = LocalLumaAppearance.current.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.eventReminders,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = TextWhitePrimary,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.sp
                                    )
                                )
                                Text(
                                    text = strings.eventRemindersDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextWhiteSecondary,
                                        letterSpacing = 0.sp
                                    )
                                )
                            }
                        }

                        GlassToggle(
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationsChange,
                            testTag = "toggle_notifications"
                        )
                    }

                    // Snooze Duration Setting Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LocalLumaAppearance.current.borderSubtle)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = LocalLumaAppearance.current.accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.snoozeDurationTitle,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                    Text(
                                        text = strings.snoozeDurationDesc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhiteSecondary,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                            }

                            val currentPresetLabel = when (snoozeMinutes) {
                                15 -> "15m"
                                30 -> "30m"
                                45 -> "45m"
                                60 -> "1h"
                                90 -> "1.5h"
                                120 -> "2h"
                                180 -> "3h"
                                240 -> "4h"
                                480 -> "8h"
                                else -> "${snoozeMinutes}m"
                            }
                            Text(
                                text = currentPresetLabel,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = LocalLumaAppearance.current.accentPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preset chips selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val displayPresets = listOf(15, 30, 60, 120, 240, 480)
                            displayPresets.forEach { minutes ->
                                val isSelected = snoozeMinutes == minutes
                                val label = when (minutes) {
                                    15 -> "15m"
                                    30 -> "30m"
                                    60 -> "1h"
                                    120 -> "2h"
                                    240 -> "4h"
                                    480 -> "8h"
                                    else -> "${minutes}m"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) LocalLumaAppearance.current.accentPrimary.copy(alpha = 0.25f)
                                            else LocalLumaAppearance.current.surfaceDefault
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) LocalLumaAppearance.current.accentPrimary else LocalLumaAppearance.current.borderSubtle,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSnoozeMinutesChange(minutes) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) LocalLumaAppearance.current.accentPrimary else TextWhiteSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CALENDARS VISIBILITY
        item {
            SectionHeader(title = strings.calendarsVisibility)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Personal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CategoryPersonal))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.categoryPersonal,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                        GlassToggle(checked = personalVisible, onCheckedChange = { onTogglePersonal() })
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(LocalLumaAppearance.current.borderSubtle))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Work
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CategoryWork))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.categoryWork,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                        GlassToggle(checked = workVisible, onCheckedChange = { onToggleWork() })
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(LocalLumaAppearance.current.borderSubtle))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Holidays
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(CategoryHealth))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.categoryHolidays,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                        GlassToggle(checked = holidaysVisible, onCheckedChange = { onToggleHolidays() })
                    }
                }
            }
        }

        // DYNAMIC LAUNCHER ICON & HOME WIDGET
        item {
            val context = LocalContext.current
            val realDay = remember { DynamicIconManager.getRealDeviceDay() }
            var previewDay by remember { mutableIntStateOf(realDay) }

            SectionHeader(title = if (strings.tabCalendar == "تقویم") "ابزارک زنده و تاریخ پویا" else "Dynamic Date & Widget")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = LocalLumaAppearance.current.accentPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (strings.tabCalendar == "تقویم") "میانبر تاریخ سه‌گانه" else "Triple Calendar Shortcut",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = TextWhitePrimary,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.sp
                                    )
                                )
                                Text(
                                    text = if (strings.tabCalendar == "تقویم") "به‌روزرسانی خودکار بر اساس تاریخ واقعی دستگاه" else "Updates from the device's real date",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextWhiteSecondary,
                                        letterSpacing = 0.sp
                                    )
                                )
                            }
                        }

                        // Active real device day badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LocalLumaAppearance.current.accentPrimary.copy(alpha = 0.15f))
                                .border(0.8.dp, LocalLumaAppearance.current.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (strings.tabCalendar == "تقویم") "امروز: $realDay" else "Today: $realDay",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = LocalLumaAppearance.current.accentPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Visual Rendering Previews: Single-Digit vs Double-Digit
                    Text(
                        text = if (strings.tabCalendar == "تقویم") "پیش‌نمایش ارقام تقویم (تک‌رقمی و دورقمی):" else "Date Typography Preview (Single & Double Digit):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhiteMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Single Digit Example (Day 7)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF13172B))
                                .border(
                                    1.dp,
                                    if (previewDay <= 9) LocalLumaAppearance.current.accentPrimary else LocalLumaAppearance.current.borderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { previewDay = 7 }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.verticalGradient(listOf(Color(0xFF242A4A), Color(0xFF13172B))))
                                        .border(0.8.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "7",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (strings.tabCalendar == "تقویم") "تک‌رقمی (۷)" else "Single (7)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextWhiteSecondary)
                                )
                            }
                        }

                        // Double Digit Example (Day 24)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF13172B))
                                .border(
                                    1.dp,
                                    if (previewDay > 9) LocalLumaAppearance.current.accentPrimary else LocalLumaAppearance.current.borderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { previewDay = 24 }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.verticalGradient(listOf(Color(0xFF242A4A), Color(0xFF13172B))))
                                        .border(0.8.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "24",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (strings.tabCalendar == "تقویم") "دورقمی (۲۴)" else "Double (24)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextWhiteSecondary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sync & Home Screen Widget Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = if (strings.tabCalendar == "تقویم")
                                    "میانبر زنده: شمسی وسط، میلادی پایین راست، قمری پایین چپ"
                                else
                                    "Live shortcut: Jalali center, Gregorian bottom-right, Hijri bottom-left",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextWhiteSecondary,
                                    lineHeight = 16.sp
                                )
                            )
                        }

                        GlassButton(
                            text = if (strings.tabCalendar == "تقویم") "افزودن به صفحه اصلی" else "Add to Home",
                            icon = Icons.Outlined.CheckCircle,
                            onClick = {
                                if (!DynamicIconManager.requestLiveCalendarShortcut(context)) {
                                    Toast.makeText(
                                        context,
                                        if (strings.tabCalendar == "تقویم") "لانچر این قابلیت را پشتیبانی نمی‌کند" else "Your launcher does not support pinned shortcuts",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            testTag = "btn_sync_dynamic_icon"
                        )
                    }
                }
            }
        }

        // DATA & SAMPLE SEEDING
        item {
            SectionHeader(title = strings.sampleDataTitle)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.resetSampleTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        )
                    )
                    Text(
                        text = strings.resetSampleDesc,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhiteSecondary,
                            letterSpacing = 0.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassButton(
                            text = strings.resetSampleButton,
                            icon = Icons.Outlined.Refresh,
                            onClick = { showResetConfirm = true },
                            testTag = "btn_reset_sample"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(LocalLumaAppearance.current.borderSubtle))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = strings.clearAllTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        )
                    )
                    Text(
                        text = strings.clearAllDesc,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhiteSecondary,
                            letterSpacing = 0.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassButton(
                            text = strings.clearAllTitle,
                            icon = Icons.Outlined.DeleteSweep,
                            onClick = { showClearConfirm = true },
                            testTag = "btn_clear_all_data"
                        )
                    }
                }
            }
        }

        // ABOUT LUMA CALENDAR
        item {
            SectionHeader(title = strings.about)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = LocalLumaAppearance.current.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.lumaCalendar,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.sp
                                )
                            )
                            Text(
                                text = strings.lumaVersion,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextWhiteMuted,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.lumaDescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhiteSecondary,
                            lineHeight = 18.sp,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Destructive actions require confirmation (FR-007); cancel or dismiss does nothing.
    if (showResetConfirm) {
        ConfirmActionDialog(
            title = strings.resetConfirmTitle,
            message = strings.resetConfirmMessage,
            confirmLabel = strings.resetConfirmAction,
            icon = Icons.Outlined.Refresh,
            onConfirm = {
                showResetConfirm = false
                onResetSampleData()
            },
            onDismiss = { showResetConfirm = false },
            modifier = Modifier.testTag("dialog_reset_sample_confirmation")
        )
    }

    if (showClearConfirm) {
        ConfirmActionDialog(
            title = strings.clearConfirmTitle,
            message = strings.clearConfirmMessage,
            confirmLabel = strings.clearConfirmAction,
            icon = Icons.Outlined.DeleteSweep,
            onConfirm = {
                showClearConfirm = false
                onClearAllData()
            },
            onDismiss = { showClearConfirm = false },
            modifier = Modifier.testTag("dialog_clear_all_confirmation")
        )
    }
}
