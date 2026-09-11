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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CalendarEvent
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.AccentRoyalViolet
import com.example.ui.theme.CanvasBlack
import com.example.ui.theme.CanvasNavy
import com.example.ui.theme.CategorySpecial
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderDefault
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceDefault
import com.example.ui.theme.GlassSurfaceHighlight
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.util.CalendarType
import com.example.util.DateUtils
import com.example.util.LocalAppStrings
import com.example.util.LocalizationManager

@Composable
fun EventDetailSheet(
    isOpen: Boolean,
    event: CalendarEvent?,
    calendarType: CalendarType = CalendarType.GREGORIAN,
    onDismiss: () -> Unit,
    onEdit: (CalendarEvent) -> Unit,
    onDelete: (CalendarEvent) -> Unit
) {
    if (!isOpen || event == null) return

    val strings = LocalAppStrings.current
    val isRtl = LocalizationManager.isRtl(calendarType)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categoryColor = try {
        Color(android.graphics.Color.parseColor(event.colorHex))
    } catch (e: Exception) {
        AccentRoyalViolet
    }

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
            // Layered Glass Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
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
                        .padding(horizontal = 24.dp)
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

                    // Navigation bar with Close and Edit icon button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            icon = Icons.Default.Close,
                            onClick = onDismiss,
                            contentDescription = "Close",
                            size = 36.dp,
                            testTag = "btn_close_detail"
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassIconButton(
                                icon = Icons.Default.Edit,
                                onClick = { onEdit(event) },
                                contentDescription = "Edit Event",
                                size = 36.dp,
                                tint = AccentElectricBlue,
                                testTag = "btn_edit_detail"
                            )

                            GlassIconButton(
                                icon = Icons.Default.Delete,
                                onClick = { showDeleteConfirm = true },
                                contentDescription = "Delete Event",
                                size = 36.dp,
                                tint = CategorySpecial,
                                testTag = "btn_delete_detail"
                            )
                        }
                    }

                    // Content Scroll
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                    ) {
                        // Title & Category badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(categoryColor.copy(alpha = 0.20f))
                                    .border(1.dp, categoryColor.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = LocalizationManager.getCategoryName(event.category, strings),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = categoryColor,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.sp
                                    )
                                )
                            }

                            Text(
                                text = LocalizationManager.getCategoryName(event.calendarType, strings),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextWhiteMuted,
                                    letterSpacing = 0.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = TextWhitePrimary,
                                letterSpacing = 0.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Date & Time Glass Panel
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            surfaceColor = GlassSurfaceDefault
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = AccentElectricBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = DateUtils.getDayOfWeek(event.date, calendarType),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = TextWhitePrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.sp
                                            )
                                        )
                                        Text(
                                            text = DateUtils.formatDisplayDate(event.date, calendarType),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TextWhiteSecondary,
                                                letterSpacing = 0.sp
                                            )
                                        )
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

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = AccentElectricBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = LocalizationManager.formatTimeRange(event.startTime, event.endTime, isRtl),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Location Panel (if provided)
                        if (event.location.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 20.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = AccentElectricBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = strings.location,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextWhiteMuted,
                                                letterSpacing = 0.sp
                                            )
                                        )
                                        Text(
                                            text = event.location,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = TextWhitePrimary,
                                                fontWeight = FontWeight.Normal,
                                                letterSpacing = 0.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Reminder info panel
                        Spacer(modifier = Modifier.height(14.dp))
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = AccentElectricBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.reminder,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextWhiteMuted,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                    Text(
                                        text = LocalizationManager.formatReminder(event.reminderMinutes, strings),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Notes Panel (if provided)
                        if (event.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 20.dp
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = null,
                                            tint = AccentElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = strings.notes,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextWhiteMuted,
                                                letterSpacing = 0.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = event.notes,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = TextWhitePrimary,
                                            lineHeight = 22.sp,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Delete Confirmation Banner
                        if (showDeleteConfirm) {
                            Spacer(modifier = Modifier.height(20.dp))
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 18.dp,
                                surfaceColor = CategorySpecial.copy(alpha = 0.15f),
                                borderColor = CategorySpecial.copy(alpha = 0.4f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = strings.deleteEventConfirm,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = strings.actionCannotBeUndone,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhiteSecondary,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GlassButton(
                                            text = strings.cancel,
                                            onClick = { showDeleteConfirm = false },
                                            testTag = "btn_cancel_delete"
                                        )
                                        GlassButton(
                                            text = strings.delete,
                                            isPrimary = false,
                                            onClick = { onDelete(event) },
                                            testTag = "btn_confirm_delete"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
