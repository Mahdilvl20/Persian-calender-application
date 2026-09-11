package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassToggle
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategorySpecial
import com.example.ui.theme.CategoryWork
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceDefault
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.viewmodel.AccentPresets

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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large Title
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary,
                    fontSize = 34.sp
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // APPEARANCE SECTION
        item {
            SectionHeader(title = "APPEARANCE")
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
                                tint = AccentElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Theme Surface",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Medium
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
                                            if (isSel) AccentElectricBlue.copy(alpha = 0.25f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            0.8.dp,
                                            if (isSel) AccentElectricBlue else GlassBorderSubtle,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onThemeSelect(th) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = th,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSel) Color.White else TextWhiteSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(GlassBorderSubtle))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Accent Color Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Accent Palette",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextWhitePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AccentPresets.forEachIndexed { idx, preset ->
                                val isSelected = accentColorIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(preset.primary)
                                        .border(
                                            if (isSelected) 2.dp else 1.dp,
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
            SectionHeader(title = "CALENDAR")
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
                                tint = AccentElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Start Week on Monday",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Medium
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
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(GlassBorderSubtle))
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
                                tint = AccentElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Show Week Numbers",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Medium
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
            SectionHeader(title = "NOTIFICATIONS")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                            tint = AccentElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Event Reminders",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Play notification chime before events",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextWhiteSecondary)
                            )
                        }
                    }

                    GlassToggle(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChange,
                        testTag = "toggle_notifications"
                    )
                }
            }
        }

        // CALENDARS VISIBILITY
        item {
            SectionHeader(title = "CALENDARS")
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
                                text = "Personal",
                                style = MaterialTheme.typography.bodyLarge.copy(color = TextWhitePrimary)
                            )
                        }
                        GlassToggle(checked = personalVisible, onCheckedChange = { onTogglePersonal() })
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(GlassBorderSubtle))
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
                                text = "Work",
                                style = MaterialTheme.typography.bodyLarge.copy(color = TextWhitePrimary)
                            )
                        }
                        GlassToggle(checked = workVisible, onCheckedChange = { onToggleWork() })
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(GlassBorderSubtle))
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
                                text = "Holidays",
                                style = MaterialTheme.typography.bodyLarge.copy(color = TextWhitePrimary)
                            )
                        }
                        GlassToggle(checked = holidaysVisible, onCheckedChange = { onToggleHolidays() })
                    }
                }
            }
        }

        // DATA & SAMPLE SEEDING
        item {
            SectionHeader(title = "SAMPLE DATA & RESTORE")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reset September 2026 Showcase",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Restores prompt demo events (Design Review, Lunch with Sarah, Gym, etc.)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextWhiteSecondary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassButton(
                            text = "Reset Sample Events",
                            icon = Icons.Outlined.Refresh,
                            onClick = onResetSampleData,
                            testTag = "btn_reset_sample"
                        )
                    }
                }
            }
        }

        // ABOUT LUMA CALENDAR
        item {
            SectionHeader(title = "ABOUT")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AccentElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Luma Calendar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Version 2.6.0 (iOS Glass Concept)",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextWhiteMuted)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Crafted with Liquid Glass translucency, fluid timelines, and Apple-like typography for modern mobile productivity.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhiteSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
