package com.aistudio.lumacalendar.vtxk.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.ui.theme.CategorySpecial
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteMuted
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhitePrimary
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteSecondary
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.util.LocalAppStrings
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager
import com.aistudio.lumacalendar.vtxk.ui.theme.LocalLumaAppearance

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
        LocalLumaAppearance.current.accentSecondary
    }

    Dialog(
        onDismissRequest = { if (!showDeleteConfirm) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (showDeleteConfirm) 0.82f else 0.70f))
                .clickable(enabled = !showDeleteConfirm, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Layered Glass Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .blur(if (showDeleteConfirm) 16.dp else 0.dp)
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                LocalLumaAppearance.current.canvasMid.copy(alpha = 0.95f),
                                LocalLumaAppearance.current.canvasBase.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                LocalLumaAppearance.current.borderBright.copy(alpha = 0.5f),
                                LocalLumaAppearance.current.borderSubtle
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
                                tint = LocalLumaAppearance.current.accentPrimary,
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
                                        fontWeight = FontWeight.Medium,
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
                                fontWeight = FontWeight.SemiBold,
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
                            surfaceColor = LocalLumaAppearance.current.surfaceDefault
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = LocalLumaAppearance.current.accentPrimary,
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
                                        .background(LocalLumaAppearance.current.borderSubtle)
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = LocalLumaAppearance.current.accentPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = LocalizationManager.formatSingleTime(event.time, isRtl),
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
                                        tint = LocalLumaAppearance.current.accentPrimary,
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
                                    tint = LocalLumaAppearance.current.accentPrimary,
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
                                            tint = LocalLumaAppearance.current.accentPrimary,
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
                    }
                }
            }
        }
    }

    // Centered Modal Liquid Glass Delete Confirmation Dialog
    if (showDeleteConfirm) {
        val layoutDir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        Dialog(
            onDismissRequest = { showDeleteConfirm = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDir,
                LocalAppStrings provides strings
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.72f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showDeleteConfirm = false }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val dialogShape = RoundedCornerShape(26.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp)
                            .widthIn(max = 380.dp)
                            .shadow(
                                elevation = 36.dp,
                                shape = dialogShape,
                                spotColor = CategorySpecial.copy(alpha = 0.40f),
                                ambientColor = Color.Black
                            )
                            .clip(dialogShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        LocalLumaAppearance.current.canvasMid.copy(alpha = 0.96f),
                                        LocalLumaAppearance.current.canvasBase.copy(alpha = 0.98f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        LocalLumaAppearance.current.borderBright.copy(alpha = 0.55f),
                                        LocalLumaAppearance.current.borderSubtle,
                                        CategorySpecial.copy(alpha = 0.25f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(300f, 500f)
                                ),
                                shape = dialogShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = false
                            ) {}
                            .testTag("dialog_delete_confirmation")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular glowing glass badge with Delete icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(CategorySpecial.copy(alpha = 0.15f))
                                    .border(1.dp, CategorySpecial.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = CategorySpecial,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Persian Title: "حذف این رویداد؟"
                            Text(
                                text = if (isRtl) "حذف این رویداد؟" else strings.deleteDialogTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    color = TextWhitePrimary,
                                    letterSpacing = 0.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Persian Description: "این رویداد به صورت دائمی حذف خواهد شد و قابلبازگشت نیست."
                            Text(
                                text = if (isRtl) "این رویداد به صورت دائمی حذف خواهد شد و قابلبازگشت نیست." else strings.deleteDialogMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = TextWhiteSecondary,
                                    lineHeight = 22.sp,
                                    letterSpacing = 0.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Two buttons: "حذف" and "انصراف"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // "انصراف" Button
                                DeleteDialogActionButton(
                                    text = if (isRtl) "انصراف" else strings.deleteDialogCancel,
                                    isDestructive = false,
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_cancel_delete",
                                    onClick = { showDeleteConfirm = false }
                                )

                                // "حذف" Button
                                DeleteDialogActionButton(
                                    text = if (isRtl) "حذف" else strings.deleteDialogConfirm,
                                    isDestructive = true,
                                    modifier = Modifier.weight(1f),
                                    testTag = "btn_confirm_delete",
                                    onClick = {
                                        showDeleteConfirm = false
                                        onDelete(event)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom action button designed specifically for the Liquid Glass Confirmation Dialog.
 */
@Composable
private fun DeleteDialogActionButton(
    text: String,
    isDestructive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(100),
        label = "dialog_btn_scale"
    )

    val shape = RoundedCornerShape(16.dp)

    val bgBrush = if (isDestructive) {
        Brush.horizontalGradient(
            colors = listOf(
                CategorySpecial.copy(alpha = if (isPressed) 0.85f else 1.0f),
                Color(0xFFE11D48).copy(alpha = if (isPressed) 0.85f else 1.0f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                LocalLumaAppearance.current.surfaceHighlight.copy(alpha = if (isPressed) 0.35f else 0.20f),
                LocalLumaAppearance.current.surfaceDefault.copy(alpha = if (isPressed) 0.20f else 0.10f)
            )
        )
    }

    val borderBrush = if (isDestructive) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.5f),
                CategorySpecial.copy(alpha = 0.3f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                LocalLumaAppearance.current.borderBright.copy(alpha = 0.40f),
                LocalLumaAppearance.current.borderSubtle
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .testTag(testTag)
            .height(48.dp)
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderBrush, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isDestructive) FontWeight.SemiBold else FontWeight.Medium,
                color = Color.White,
                fontSize = 15.sp,
                letterSpacing = 0.sp
            )
        )
    }
}
