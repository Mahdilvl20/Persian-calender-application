package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CalendarEvent
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.AccentRoyalViolet
import com.example.ui.theme.CanvasBlack
import com.example.ui.theme.CanvasNavy
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderDefault
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceDefault
import com.example.ui.theme.GlassSurfaceHighlight
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.viewmodel.AvailableCategories
import com.example.util.CalendarType
import com.example.util.DateUtils

@Composable
fun AddEditEventSheet(
    isOpen: Boolean,
    event: CalendarEvent?,
    defaultDate: String,
    activeCalendarType: CalendarType = CalendarType.GREGORIAN,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        category: String,
        colorHex: String,
        location: String,
        notes: String,
        reminderMinutes: Int,
        calendarType: String
    ) -> Unit
) {
    if (!isOpen) return

    var title by remember(event) { mutableStateOf(event?.title ?: "") }
    var date by remember(event, defaultDate) { mutableStateOf(event?.date ?: defaultDate) }
    var startTime by remember(event) { mutableStateOf(event?.startTime ?: "09:00") }
    var endTime by remember(event) { mutableStateOf(event?.endTime ?: "10:00") }
    var selectedCategoryIndex by remember(event) {
        val idx = AvailableCategories.indexOfFirst { it.name == event?.category }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }
    var location by remember(event) { mutableStateOf(event?.location ?: "") }
    var notes by remember(event) { mutableStateOf(event?.notes ?: "") }
    var reminderMinutes by remember(event) { mutableIntStateOf(event?.reminderMinutes ?: 15) }
    var calendarType by remember(event) { mutableStateOf(event?.calendarType ?: "Personal") }

    val selectedCategory = AvailableCategories[selectedCategoryIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.70f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Glass modal sheet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CanvasNavy.copy(alpha = 0.95f),
                                CanvasBlack.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                GlassBorderBright.copy(alpha = 0.5f),
                                GlassBorderSubtle
                            )
                        ),
                        RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    // Grabber notch
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp, bottom = 12.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    // Sheet Header: Cancel, Title, Save
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextWhiteSecondary,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier
                                .testTag("btn_cancel_event")
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                        )

                        Text(
                            text = if (event == null) "New Event" else "Edit Event",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhitePrimary
                            )
                        )

                        GlassButton(
                            text = "Save",
                            isPrimary = true,
                            onClick = {
                                onSave(
                                    event?.id ?: 0L,
                                    title.ifBlank { "New Event" },
                                    date,
                                    startTime,
                                    endTime,
                                    selectedCategory.name,
                                    selectedCategory.hex,
                                    location,
                                    notes,
                                    reminderMinutes,
                                    calendarType
                                )
                            },
                            testTag = "btn_save_event"
                        )
                    }

                    // Scrollable form body
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                    ) {
                        // Title Input
                        Text(
                            text = "TITLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhiteMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        GlassInput(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Event title (e.g., Design Review)",
                            testTag = "input_event_title"
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Category Selection
                        Text(
                            text = "CATEGORY & COLOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhiteMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(AvailableCategories.indices.toList()) { idx ->
                                val cat = AvailableCategories[idx]
                                CategoryChip(
                                    name = cat.name,
                                    color = cat.color,
                                    isSelected = selectedCategoryIndex == idx,
                                    onSelect = { selectedCategoryIndex = idx },
                                    testTag = "chip_${cat.name.lowercase()}"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Date & Time Grouped Glass Card
                        Text(
                            text = "DATE & TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhiteMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                // Date selector row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarMonth,
                                            contentDescription = null,
                                            tint = AccentElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Date",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = TextWhitePrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    Text(
                                        text = DateUtils.formatDisplayDate(date, activeCalendarType),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = AccentElectricBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                // Quick date buttons (Today, Tomorrow, +3 Days)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val quickDates = listOf(
                                        "Today" to DateUtils.DEFAULT_TODAY,
                                        "Tomorrow" to DateUtils.addDays(DateUtils.DEFAULT_TODAY, 1),
                                        "Sep 15" to "2026-09-15",
                                        "Sep 20" to "2026-09-20"
                                    )
                                    quickDates.forEach { (label, qDate) ->
                                        val isSel = date == qDate
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSel) AccentRoyalViolet.copy(alpha = 0.4f)
                                                    else GlassSurfaceDefault
                                                )
                                                .border(
                                                    0.8.dp,
                                                    if (isSel) AccentElectricBlue else GlassBorderSubtle,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { date = qDate }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSel) Color.White else TextWhiteSecondary,
                                                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.8.dp)
                                        .background(GlassBorderSubtle)
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                // Time selector row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = AccentElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Time",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = TextWhitePrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Quick Start Time presets
                                        TimePill(time = startTime, onSelect = { startTime = it })
                                        Text(
                                            text = "to",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextWhiteMuted)
                                        )
                                        TimePill(time = endTime, onSelect = { endTime = it })
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Location Input
                        Text(
                            text = "LOCATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhiteMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        GlassInput(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = "Add location or conference link",
                            leadingIcon = Icons.Outlined.LocationOn,
                            testTag = "input_event_location"
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Reminder & Calendar Selector
                        Text(
                            text = "REMINDERS & CALENDAR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhiteMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = null,
                                            tint = AccentElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Alert",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = TextWhitePrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    // Reminder pill cycle
                                    val reminders = listOf(0 to "None", 5 to "5 min before", 15 to "15 min before", 30 to "30 min before", 60 to "1 hour before")
                                    val curReminder = reminders.firstOrNull { it.first == reminderMinutes }?.second ?: "15 min before"

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GlassSurfaceHighlight)
                                            .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(10.dp))
                                            .clickable {
                                                val nextIdx = (reminders.indexOfFirst { it.first == reminderMinutes } + 1) % reminders.size
                                                reminderMinutes = reminders[nextIdx].first
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = curReminder,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = AccentElectricBlue,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.8.dp)
                                        .background(GlassBorderSubtle)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Calendar",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("Personal", "Work", "Holidays").forEach { calName ->
                                            val isSelected = calendarType == calName
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) AccentRoyalViolet.copy(alpha = 0.4f)
                                                        else GlassSurfaceDefault
                                                    )
                                                    .border(
                                                        0.8.dp,
                                                        if (isSelected) AccentElectricBlue else Color.Transparent,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { calendarType = calName }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = calName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (isSelected) Color.White else TextWhiteSecondary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Notes Input
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhiteMuted,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        GlassInput(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = "Add description or agenda items...",
                            leadingIcon = Icons.Outlined.Description,
                            singleLine = false,
                            maxLines = 4,
                            testTag = "input_event_notes"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePill(
    time: String,
    onSelect: (String) -> Unit
) {
    val commonTimes = listOf("09:00", "10:00", "11:00", "12:00", "13:30", "15:00", "16:30", "18:00", "19:00", "20:00")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GlassSurfaceHighlight)
            .border(0.8.dp, GlassBorderDefault, RoundedCornerShape(10.dp))
            .clickable {
                val nextIdx = (commonTimes.indexOf(time) + 1).coerceAtLeast(0) % commonTimes.size
                onSelect(commonTimes[nextIdx])
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextWhitePrimary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
