package com.aistudio.lumacalendar.vtxk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.ui.components.CategoryChip
import com.aistudio.lumacalendar.vtxk.ui.components.EventCard
import com.aistudio.lumacalendar.vtxk.ui.components.GlassCard
import com.aistudio.lumacalendar.vtxk.ui.components.GlassIconButton
import com.aistudio.lumacalendar.vtxk.ui.components.GlassInput
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentElectricBlue
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfaceUltraLight
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteMuted
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhitePrimary
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteSecondary
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.AvailableCategories
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.util.LocalAppStrings
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager

@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    searchResults: List<CalendarEvent>,
    calendarType: CalendarType = CalendarType.GREGORIAN,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val groupedResults = remember(searchResults) {
        searchResults.groupBy { it.date }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Title
        Text(
            text = strings.searchEvents,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextWhitePrimary,
                fontSize = 32.sp,
                letterSpacing = 0.sp
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Translucent search input
        GlassInput(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = strings.searchPlaceholder,
            leadingIcon = Icons.Default.Search,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    GlassIconButton(
                        icon = Icons.Default.Close,
                        onClick = { onSearchQueryChange("") },
                        contentDescription = "Clear search",
                        size = 28.dp,
                        testTag = "btn_clear_search"
                    )
                }
            },
            testTag = "input_search_events"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                CategoryChip(
                    name = strings.all,
                    color = AccentElectricBlue,
                    isSelected = selectedCategory == "All",
                    onSelect = { onCategoryChange("All") },
                    testTag = "chip_search_all"
                )
            }
            items(AvailableCategories) { cat ->
                CategoryChip(
                    name = LocalizationManager.getCategoryName(cat.name, strings),
                    color = cat.color,
                    isSelected = selectedCategory == cat.name,
                    onSelect = { onCategoryChange(cat.name) },
                    testTag = "chip_search_${cat.name.lowercase()}"
                )
            }
        }

        // Search Results grouped by date
        if (groupedResults.isEmpty()) {
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
                        .padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (searchQuery.isEmpty()) Icons.Default.Search else Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = TextWhiteMuted,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) strings.findAnyEvent else strings.noMatchingEvents,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhiteSecondary,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) strings.typeKeywordsPrompt else strings.tryAnotherKeywordPrompt,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                groupedResults.forEach { (dateStr, dayEvents) ->
                    item(key = "search_header_$dateStr") {
                        Text(
                            text = DateUtils.formatMonthDay(dateStr, calendarType = calendarType),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AccentElectricBlue
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
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
