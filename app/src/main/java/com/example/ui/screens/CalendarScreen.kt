package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarEvent
import com.example.data.holiday.Holiday
import com.example.data.holiday.HolidayService
import com.example.ui.components.CalendarCell
import com.example.ui.components.EventCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.HolidayCard
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.AccentRoyalViolet
import com.example.ui.theme.CanvasBlack
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderDefault
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceDefault
import com.example.ui.theme.GlassSurfaceHighlight
import com.example.ui.theme.GlassSurfaceUltraLight
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.util.CalendarType
import com.example.util.DateUtils
import com.example.util.LocalAppStrings
import com.example.util.LocalizationManager

@Composable
fun CalendarScreen(
    year: Int,
    month: Int,
    selectedDate: String,
    calendarViewMode: String,
    calendarType: CalendarType = CalendarType.GREGORIAN,
    events: List<CalendarEvent>,
    selectedDayEvents: List<CalendarEvent>,
    firstDayMonday: Boolean,
    onDateSelect: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onViewModeChange: (String) -> Unit,
    onCalendarTypeChange: (CalendarType) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val monthName = DateUtils.getMonthName(year, month, calendarType)
    val yearStr = DateUtils.getYear(year, month, calendarType)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // TOP HEADER: Month, Year, Controls, and "Today" Glass Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month + Year title: Never wrap, ample space, no broken words
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhitePrimary,
                        fontSize = if (monthName.length > 8) 24.sp else 28.sp,
                        letterSpacing = 0.sp
                    ),
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = yearStr,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhiteSecondary.copy(alpha = 0.85f),
                        fontSize = 22.sp,
                        letterSpacing = 0.sp
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Controls: [<] [Today] [>]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    onClick = onPrevMonth,
                    contentDescription = strings.previousMonth,
                    size = 36.dp,
                    testTag = "btn_prev_month"
                )

                // "Today" small glass button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassSurfaceDefault)
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    GlassBorderBright.copy(alpha = 0.4f),
                                    GlassBorderSubtle
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(onClick = onTodayClick)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("btn_today"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.today,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = AccentElectricBlue,
                            letterSpacing = 0.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    onClick = onNextMonth,
                    contentDescription = strings.nextMonth,
                    size = 36.dp,
                    testTag = "btn_next_month"
                )
            }
        }

        // CALENDAR TYPE SWITCHER: شمسی (Jalali) / میلادی (Gregorian) / قمری (Hijri)
        CalendarTypeSegmentedControl(
            selectedType = calendarType,
            onTypeSelected = onCalendarTypeChange,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // VIEW MODE SWITCHER: Month / Week / Day (iOS Segmented Control)
        SegmentedViewSwitcher(
            currentMode = calendarViewMode,
            onModeSelected = onViewModeChange,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // View content with smooth iOS-style transitions when changing calendar type or view mode
        AnimatedContent(
            targetState = calendarType to calendarViewMode,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) +
                 scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = LinearOutSlowInEasing)))
                .togetherWith(
                    fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)) +
                    scaleOut(targetScale = 0.98f, animationSpec = tween(160, easing = FastOutLinearInEasing))
                )
            },
            label = "calendar_content_transition"
        ) { (activeType, activeMode) ->
            val selectedHoliday = remember(selectedDate, activeType) {
                HolidayService.default.getHoliday(selectedDate, activeType)
            }

            when (activeMode) {
                "Month" -> {
                    MonthViewContent(
                        year = year,
                        month = month,
                        selectedDate = selectedDate,
                        calendarType = activeType,
                        events = events,
                        selectedDayEvents = selectedDayEvents,
                        holiday = selectedHoliday,
                        firstDayMonday = firstDayMonday,
                        onDateSelect = onDateSelect,
                        onEventClick = onEventClick,
                        onAddEventClick = onAddEventClick
                    )
                }
                "Week" -> {
                    WeekViewContent(
                        selectedDate = selectedDate,
                        calendarType = activeType,
                        events = events,
                        holiday = selectedHoliday,
                        firstDayMonday = firstDayMonday,
                        onDateSelect = onDateSelect,
                        onEventClick = onEventClick,
                        onAddEventClick = onAddEventClick
                    )
                }
                "Day" -> {
                    DayViewContent(
                        selectedDate = selectedDate,
                        calendarType = activeType,
                        events = selectedDayEvents,
                        holiday = selectedHoliday,
                        onEventClick = onEventClick,
                        onAddEventClick = onAddEventClick
                    )
                }
            }
        }
    }
}

/**
 * Compact iOS-styled Liquid Glass Segmented Control for Calendar Type:
 * شمسی (Jalali), میلادی (Gregorian), قمری (Hijri)
 */
@Composable
fun CalendarTypeSegmentedControl(
    selectedType: CalendarType,
    onTypeSelected: (CalendarType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = CalendarType.values()
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassSurfaceUltraLight)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        GlassBorderBright.copy(alpha = 0.35f),
                        GlassBorderSubtle
                    )
                ),
                shape
            )
            .padding(3.dp)
            .testTag("calendar_type_segmented_control")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            types.forEach { type ->
                val isSelected = selectedType == type
                val itemShape = RoundedCornerShape(13.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(itemShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                GlassSurfaceHighlight,
                                                GlassSurfaceDefault
                                            )
                                        )
                                    )
                                    .border(
                                        0.8.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                GlassBorderBright.copy(alpha = 0.6f),
                                                GlassBorderSubtle
                                            )
                                        ),
                                        itemShape
                                    )
                            } else Modifier
                        )
                        .clickable { onTypeSelected(type) }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .testTag("btn_cal_type_${type.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    val strings = LocalAppStrings.current
                    Text(
                        text = LocalizationManager.getCalendarTypeName(type, strings),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) TextWhitePrimary else TextWhiteMuted,
                            letterSpacing = 0.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * iOS-styled Segmented Control for Month, Week, Day
 */
@Composable
private fun SegmentedViewSwitcher(
    currentMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf("Month", "Week", "Day")
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassSurfaceUltraLight)
            .border(1.dp, GlassBorderSubtle, shape)
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            modes.forEach { mode ->
                val isSelected = currentMode == mode
                val itemShape = RoundedCornerShape(13.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(itemShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                GlassSurfaceHighlight,
                                                GlassSurfaceDefault
                                            )
                                        )
                                    )
                                    .border(
                                        0.8.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                GlassBorderBright.copy(alpha = 0.5f),
                                                GlassBorderSubtle
                                            )
                                        ),
                                        itemShape
                                    )
                            } else Modifier
                        )
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val strings = LocalAppStrings.current
                    Text(
                        text = LocalizationManager.getViewModeName(mode, strings),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) TextWhitePrimary else TextWhiteMuted,
                            letterSpacing = 0.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * MONTH VIEW: Translucent glass calendar container with day cells & agenda below
 */
@Composable
private fun MonthViewContent(
    year: Int,
    month: Int,
    selectedDate: String,
    calendarType: CalendarType = CalendarType.GREGORIAN,
    events: List<CalendarEvent>,
    selectedDayEvents: List<CalendarEvent>,
    holiday: Holiday?,
    firstDayMonday: Boolean,
    onDateSelect: (String) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: (String) -> Unit
) {
    val weekdayLabels = remember(calendarType, firstDayMonday) {
        DateUtils.getWeekdayLabels(calendarType, firstDayMonday)
    }

    val days = remember(year, month, selectedDate, calendarType, firstDayMonday) {
        DateUtils.getMonthDays(
            year = year,
            month = month,
            selectedDate = selectedDate,
            calendarType = calendarType,
            firstDaySunday = !firstDayMonday
        )
    }

    // Map of dateString to event colors
    val eventsByDate = remember(events) {
        events.groupBy { it.date }.mapValues { entry ->
            entry.value.map { ev ->
                try {
                    Color(android.graphics.Color.parseColor(ev.colorHex))
                } catch (e: Exception) {
                    AccentRoyalViolet
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Translucent Glass Calendar Matrix Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                surfaceColor = GlassSurfaceDefault
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 14.dp)
                ) {
                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        weekdayLabels.forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextWhiteMuted,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.sp
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6 rows of 7 days
                    for (row in 0 until 6) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 7) {
                                val dayIndex = row * 7 + col
                                if (dayIndex < days.size) {
                                    val day = days[dayIndex]
                                    val eventColors = eventsByDate[day.dateString] ?: emptyList()
                                    CalendarCell(
                                        day = day,
                                        hasEvents = eventColors.isNotEmpty(),
                                        eventColors = eventColors,
                                        onClick = { onDateSelect(day.dateString) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SELECTED DAY / AGENDA SECTION HEADER
        item {
            val strings = LocalAppStrings.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatSelectedHeader(selectedDate, calendarType = calendarType),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhitePrimary,
                        letterSpacing = 0.sp
                    )
                )

                Text(
                    text = "+ ${strings.addEvent}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AccentElectricBlue,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAddEventClick(selectedDate) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        // Official Holiday Card (if selected date is a holiday)
        if (holiday != null) {
            item {
                HolidayCard(
                    holiday = holiday,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // AGENDA EVENT LIST FOR SELECTED DAY
        if (selectedDayEvents.isEmpty() && holiday == null) {
            item {
                val strings = LocalAppStrings.current
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    cornerRadius = 16.dp,
                    surfaceColor = GlassSurfaceUltraLight
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            tint = TextWhiteMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.noEventsForDate,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextWhiteSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.addEventPrompt,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextWhiteMuted)
                        )
                    }
                }
            }
        } else {
            items(selectedDayEvents, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }

        // Bottom space so content doesn't get covered by floating tab bar
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * WEEK VIEW: Mon -> Sun day strip with hourly timeline
 */
@Composable
private fun WeekViewContent(
    selectedDate: String,
    calendarType: CalendarType = CalendarType.GREGORIAN,
    events: List<CalendarEvent>,
    holiday: Holiday?,
    firstDayMonday: Boolean,
    onDateSelect: (String) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: (String) -> Unit
) {
    val weekDays = remember(selectedDate, calendarType, firstDayMonday) {
        DateUtils.getWeekDays(
            targetDateStr = selectedDate,
            selectedDate = selectedDate,
            calendarType = calendarType,
            firstDaySunday = !firstDayMonday
        )
    }

    val selectedDayEvents = events.filter { it.date == selectedDate }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal week days bar
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { wDay ->
                    val isSelected = wDay.dateString == selectedDate
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(
                                            Brush.radialGradient(
                                                listOf(
                                                    AccentRoyalViolet.copy(alpha = 0.5f),
                                                    AccentElectricBlue.copy(alpha = 0.2f)
                                                )
                                            )
                                        )
                                        .border(1.dp, AccentElectricBlue, RoundedCornerShape(14.dp))
                                } else Modifier
                            )
                            .clickable { onDateSelect(wDay.dateString) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = wDay.dayOfWeekName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) AccentElectricBlue else TextWhiteMuted,
                                letterSpacing = 0.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wDay.displayNumber.ifEmpty { wDay.dayOfMonth.toString() },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected || wDay.isToday || wDay.isHoliday) FontWeight.SemiBold else FontWeight.Normal,
                                color = when {
                                    wDay.isHoliday -> Color(0xFFFF453A)
                                    isSelected -> Color.White
                                    else -> TextWhitePrimary
                                },
                                letterSpacing = 0.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Official Holiday Card in Week View
        if (holiday != null) {
            HolidayCard(
                holiday = holiday,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Hourly timeline for the selected day in week view
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val hours = (8..22).toList()

            items(hours) { hourInt ->
                val hourStr = String.format("%02d:00", hourInt)
                val displayHour = if (LocalizationManager.isRtl(calendarType)) LocalizationManager.formatDigits(hourStr) else hourStr
                val eventsAtHour = selectedDayEvents.filter {
                    val evHour = it.startTime.substringBefore(":").toIntOrNull() ?: -1
                    evHour == hourInt
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Hour label
                    Text(
                        text = displayHour,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextWhiteMuted,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        ),
                        modifier = Modifier.width(50.dp)
                    )

                    // Slot area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        if (eventsAtHour.isEmpty()) {
                            // Empty subtle slot line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Transparent)
                                    .border(0.5.dp, GlassBorderSubtle.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .clickable { onAddEventClick(selectedDate) }
                            )
                        } else {
                            eventsAtHour.forEach { ev ->
                                EventCard(
                                    event = ev,
                                    onClick = { onEventClick(ev) },
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

/**
 * DAY VIEW: Focused hour-by-hour vertical timeline
 */
@Composable
private fun DayViewContent(
    selectedDate: String,
    calendarType: CalendarType = CalendarType.GREGORIAN,
    events: List<CalendarEvent>,
    holiday: Holiday?,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Large Day Header
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.getDayOfWeek(selectedDate, calendarType),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhitePrimary,
                            letterSpacing = 0.sp
                        )
                    )
                    Text(
                        text = DateUtils.formatDisplayDate(selectedDate, calendarType),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AccentElectricBlue,
                            letterSpacing = 0.sp
                        )
                    )
                }

                val strings = LocalAppStrings.current
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentRoyalViolet.copy(alpha = 0.35f))
                        .border(0.8.dp, AccentElectricBlue, RoundedCornerShape(12.dp))
                        .clickable { onAddEventClick(selectedDate) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+ ${strings.addEvent}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }
        }

        // Official Holiday Card in Day View
        if (holiday != null) {
            HolidayCard(
                holiday = holiday,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Timeline
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val hours = (7..23).toList()

            items(hours) { hourInt ->
                val hourStr = String.format("%02d:00", hourInt)
                val displayHour = if (LocalizationManager.isRtl(calendarType)) LocalizationManager.formatDigits(hourStr) else hourStr
                val eventsInHour = events.filter {
                    val evH = it.startTime.substringBefore(":").toIntOrNull() ?: -1
                    evH == hourInt
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = displayHour,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextWhiteMuted,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        ),
                        modifier = Modifier.width(48.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    ) {
                        if (eventsInHour.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(0.5.dp, GlassBorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { onAddEventClick(selectedDate) }
                            )
                        } else {
                            eventsInHour.forEach { ev ->
                                EventCard(
                                    event = ev,
                                    onClick = { onEventClick(ev) },
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
