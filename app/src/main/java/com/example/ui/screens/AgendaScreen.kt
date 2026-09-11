package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventNote
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarEvent
import com.example.ui.components.CategoryChip
import com.example.ui.components.EventCard
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.GlassSurfaceUltraLight
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.viewmodel.AvailableCategories
import com.example.util.CalendarType
import com.example.util.DateUtils
import com.example.util.LocalAppStrings
import com.example.util.LocalizationManager

@Composable
fun AgendaScreen(
    events: List<CalendarEvent>,
    calendarType: CalendarType = CalendarType.GREGORIAN,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredEvents = remember(events, selectedCategory) {
        if (selectedCategory == "All") events
        else events.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val groupedEvents = remember(filteredEvents) {
        filteredEvents.groupBy { it.date }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Large Title
        Text(
            text = strings.agenda,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary,
                fontSize = 34.sp,
                letterSpacing = 0.sp
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Category filter pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 14.dp)
        ) {
            item {
                CategoryChip(
                    name = strings.all,
                    color = AccentElectricBlue,
                    isSelected = selectedCategory == "All",
                    onSelect = { selectedCategory = "All" },
                    testTag = "chip_agenda_all"
                )
            }
            items(AvailableCategories) { cat ->
                CategoryChip(
                    name = LocalizationManager.getCategoryName(cat.name, strings),
                    color = cat.color,
                    isSelected = selectedCategory == cat.name,
                    onSelect = { selectedCategory = cat.name },
                    testTag = "chip_agenda_${cat.name.lowercase()}"
                )
            }
        }

        // Agenda Grouped List
        if (groupedEvents.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                cornerRadius = 20.dp,
                surfaceColor = GlassSurfaceUltraLight
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventNote,
                        contentDescription = null,
                        tint = TextWhiteMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.noEventsFound,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhiteSecondary,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.addEventPrompt,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhiteMuted,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedEvents.forEach { (dateStr, dayEvents) ->
                    item(key = "header_$dateStr") {
                        Text(
                            text = DateUtils.formatSelectedHeader(dateStr, calendarType = calendarType),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AccentElectricBlue
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                        )
                    }

                    items(dayEvents, key = { it.id }) { ev ->
                        EventCard(
                            event = ev,
                            onClick = { onEventClick(ev) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
