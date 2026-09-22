package com.aistudio.lumacalendar.vtxk.ui.screens

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.data.holiday.Holiday
import com.aistudio.lumacalendar.vtxk.data.holiday.HolidayService
import com.aistudio.lumacalendar.vtxk.data.model.PersianCalendarDay
import com.aistudio.lumacalendar.vtxk.ui.components.CalendarCell
import com.aistudio.lumacalendar.vtxk.ui.components.EventCard
import com.aistudio.lumacalendar.vtxk.ui.components.GlassButton
import com.aistudio.lumacalendar.vtxk.ui.components.GlassCard
import com.aistudio.lumacalendar.vtxk.ui.components.GlassIconButton
import com.aistudio.lumacalendar.vtxk.ui.components.HolidayCard
import com.aistudio.lumacalendar.vtxk.ui.components.ManualDateInputDialog
import com.aistudio.lumacalendar.vtxk.util.TimeValidator
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteMuted
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhitePrimary
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteSecondary
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.util.LocalAppStrings
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager
import com.aistudio.lumacalendar.vtxk.ui.theme.LocalLumaAppearance

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
    showWeekNumbers: Boolean = false,
    onDateSelect: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onViewModeChange: (String) -> Unit,
    onCalendarTypeChange: (CalendarType) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    persianDaysMap: Map<String, PersianCalendarDay> = emptyMap()
) {
    val strings = LocalAppStrings.current
    val monthName = DateUtils.getMonthName(year, month, calendarType)
    val yearStr = DateUtils.getYear(year, month, calendarType)
    var showManualDatePicker by remember { mutableStateOf(false) }

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
            // Month + Year title: Tap to open safe manual date picker
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showManualDatePicker = true }
                    .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .testTag("btn_header_date_picker"),
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

            // Controls: [<] [Today] [Pick Date] [>]
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
                        .background(LocalLumaAppearance.current.surfaceDefault)
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    LocalLumaAppearance.current.borderBright.copy(alpha = 0.4f),
                                    LocalLumaAppearance.current.borderSubtle
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
                            color = LocalLumaAppearance.current.accentPrimary,
                            letterSpacing = 0.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Quick Date Jump Button
                GlassIconButton(
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = { showManualDatePicker = true },
                    contentDescription = strings.selectDate,
                    size = 36.dp,
                    testTag = "btn_jump_to_date"
                )

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
            val selectedHoliday = remember(selectedDate, activeType, persianDaysMap) {
                if (activeType == CalendarType.JALALI) {
                    val pDay = persianDaysMap[selectedDate]
                    if (pDay != null) {
                        if (pDay.isHoliday) {
                            Holiday(
                                id = "jalali_${pDay.date}",
                                dateString = pDay.date,
                                name = pDay.holidayDescription ?: "تعطیل رسمی",
                                isOfficialHoliday = true,
                                calendarType = CalendarType.JALALI,
                                description = pDay.holidayDescription ?: "تعطیل رسمی"
                            )
                        } else {
                            null
                        }
                    } else {
                        HolidayService.default.getHoliday(selectedDate, activeType)
                    }
                } else {
                    HolidayService.default.getHoliday(selectedDate, activeType)
                }
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
                        showWeekNumbers = showWeekNumbers,
                        onDateSelect = onDateSelect,
                        onEventClick = onEventClick,
                        onAddEventClick = onAddEventClick,
                        persianDaysMap = persianDaysMap
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
                        onAddEventClick = onAddEventClick,
                        persianDaysMap = persianDaysMap
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

        // Safe Manual Date Input Dialog
        ManualDateInputDialog(
            isOpen = showManualDatePicker,
            initialDate = selectedDate,
            activeCalendarType = calendarType,
            onDismiss = { showManualDatePicker = false },
            onDateSelected = { canonicalDate ->
                onDateSelect(canonicalDate)
                showManualDatePicker = false
            }
        )
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
            .background(LocalLumaAppearance.current.surfaceUltraLight)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        LocalLumaAppearance.current.borderBright.copy(alpha = 0.35f),
                        LocalLumaAppearance.current.borderSubtle
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

                val (persianLabel, englishLabel) = when (type) {
                    CalendarType.JALALI -> "شمسی" to "(Jalali)"
                    CalendarType.GREGORIAN -> "میلادی" to "(Gregorian)"
                    CalendarType.HIJRI -> "قمری" to "(Hijri)"
                }

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
                                                LocalLumaAppearance.current.surfaceHighlight,
                                                LocalLumaAppearance.current.surfaceDefault
                                            )
                                        )
                                    )
                                    .border(
                                        0.8.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                LocalLumaAppearance.current.borderBright.copy(alpha = 0.6f),
                                                LocalLumaAppearance.current.borderSubtle
                                            )
                                        ),
                                        itemShape
                                    )
                            } else Modifier
                        )
                        .clickable { onTypeSelected(type) }
                        .padding(vertical = 5.dp, horizontal = 2.dp)
                        .testTag("btn_cal_type_${type.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = persianLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) TextWhitePrimary else TextWhiteMuted,
                                letterSpacing = 0.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = englishLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp,
                                color = if (isSelected) TextWhiteSecondary.copy(alpha = 0.85f) else TextWhiteMuted.copy(alpha = 0.60f),
                                letterSpacing = 0.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
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
            .background(LocalLumaAppearance.current.surfaceUltraLight)
            .border(1.dp, LocalLumaAppearance.current.borderSubtle, shape)
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
                                                LocalLumaAppearance.current.surfaceHighlight,
                                                LocalLumaAppearance.current.surfaceDefault
                                            )
                                        )
                                    )
                                    .border(
                                        0.8.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                LocalLumaAppearance.current.borderBright.copy(alpha = 0.5f),
                                                LocalLumaAppearance.current.borderSubtle
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
    showWeekNumbers: Boolean = false,
    onDateSelect: (String) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: (String) -> Unit,
    persianDaysMap: Map<String, PersianCalendarDay> = emptyMap()
) {
    val weekdayLabels = remember(calendarType, firstDayMonday) {
        DateUtils.getWeekdayLabels(calendarType, firstDayMonday)
    }

    val days = remember(year, month, selectedDate, calendarType, firstDayMonday, persianDaysMap) {
        DateUtils.getMonthDays(
            year = year,
            month = month,
            selectedDate = selectedDate,
            calendarType = calendarType,
            firstDaySunday = !firstDayMonday,
            persianDaysMap = persianDaysMap
        )
    }

    // Map of dateString to event colors. The accent fallback is read in composition and keyed
    // so an accent change re-derives this map instead of serving a stale cached color (FR-012, T026).
    val fallbackAccent = LocalLumaAppearance.current.accentSecondary
    val eventsByDate = remember(events, fallbackAccent) {
        events.groupBy { it.date }.mapValues { entry ->
            entry.value.map { ev ->
                try {
                    Color(android.graphics.Color.parseColor(ev.colorHex))
                } catch (e: Exception) {
                    fallbackAccent
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
                surfaceColor = LocalLumaAppearance.current.surfaceDefault
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 14.dp)
                ) {
                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showWeekNumbers) {
                            Text(
                                text = "W#",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextWhiteMuted.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.sp
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(24.dp)
                            )
                        }
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
                        val firstDayInRowIndex = row * 7
                        val rowWeekNumber = if (showWeekNumbers && firstDayInRowIndex < days.size) {
                            DateUtils.getWeekOfYear(days[firstDayInRowIndex].dateString, firstDayMonday)
                        } else null

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showWeekNumbers) {
                                Text(
                                    text = rowWeekNumber?.toString() ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextWhiteMuted.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(24.dp)
                                )
                            }
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

        // SELECTED DAY SECTION HEADER
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
                        color = LocalLumaAppearance.current.accentPrimary,
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

        // EVENT LIST FOR SELECTED DAY
        if (selectedDayEvents.isEmpty() && holiday == null) {
            item {
                val strings = LocalAppStrings.current
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    cornerRadius = 16.dp,
                    surfaceColor = LocalLumaAppearance.current.surfaceUltraLight
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
    onAddEventClick: (String) -> Unit,
    persianDaysMap: Map<String, PersianCalendarDay> = emptyMap()
) {
    val weekDays = remember(selectedDate, calendarType, firstDayMonday, persianDaysMap) {
        DateUtils.getWeekDays(
            targetDateStr = selectedDate,
            selectedDate = selectedDate,
            calendarType = calendarType,
            firstDaySunday = !firstDayMonday,
            persianDaysMap = persianDaysMap
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
                                                    LocalLumaAppearance.current.accentSecondary.copy(alpha = 0.5f),
                                                    LocalLumaAppearance.current.accentPrimary.copy(alpha = 0.2f)
                                                )
                                            )
                                        )
                                        .border(1.dp, LocalLumaAppearance.current.accentPrimary, RoundedCornerShape(14.dp))
                                } else Modifier
                            )
                            .clickable { onDateSelect(wDay.dateString) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = wDay.dayOfWeekName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) LocalLumaAppearance.current.accentPrimary else TextWhiteMuted,
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
            val hours = (0..23).toList()

            items(hours) { hourInt ->
                val hourStr = String.format("%02d:00", hourInt)
                val displayHour = if (LocalizationManager.isRtl(calendarType)) LocalizationManager.formatDigits(hourStr) else hourStr
                val eventsAtHour = selectedDayEvents.filter {
                    val evHour = TimeValidator.parseTime(it.startTime).hour
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
                                    .border(0.5.dp, LocalLumaAppearance.current.borderSubtle.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
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
                            color = LocalLumaAppearance.current.accentPrimary,
                            letterSpacing = 0.sp
                        )
                    )
                }

                val strings = LocalAppStrings.current
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalLumaAppearance.current.accentSecondary.copy(alpha = 0.35f))
                        .border(0.8.dp, LocalLumaAppearance.current.accentPrimary, RoundedCornerShape(12.dp))
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
            val hours = (0..23).toList()

            items(hours) { hourInt ->
                val hourStr = String.format("%02d:00", hourInt)
                val displayHour = if (LocalizationManager.isRtl(calendarType)) LocalizationManager.formatDigits(hourStr) else hourStr
                val eventsInHour = events.filter {
                    val evH = TimeValidator.parseTime(it.startTime).hour
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
                                    .border(0.5.dp, LocalLumaAppearance.current.borderSubtle.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
