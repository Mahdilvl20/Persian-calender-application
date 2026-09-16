package com.aistudio.lumacalendar.vtxk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentElectricBlue
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentRoyalViolet
import com.aistudio.lumacalendar.vtxk.ui.theme.CanvasNavy
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassBorderBright
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassBorderDefault
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassBorderSubtle
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfaceDefault
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfaceHighlight
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteMuted
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhitePrimary
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteSecondary
import com.aistudio.lumacalendar.vtxk.util.AppStrings
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager
import com.aistudio.lumacalendar.vtxk.util.ParsedTime
import com.aistudio.lumacalendar.vtxk.util.TimeValidator
import java.util.Locale

enum class TimePickerType {
    EVENT,
    START,
    END
}

/**
 * Native Jetpack Compose Liquid Glass Time Picker Dialog.
 * Completely replaces legacy android.app.TimePickerDialog to eliminate window token crashes,
 * context mismatches, and theme failures.
 *
 * Provides:
 * - 12h (AM/PM) and 24h mode toggling
 * - Interactive stepper and direct interval controls
 * - Full Persian/Arabic numerals and RTL layout support
 * - Defensive state boundaries (hours in 0..23, minutes in 0..59)
 */
@Composable
fun LiquidGlassTimePickerDialog(
    initialTime: String,
    type: TimePickerType = TimePickerType.EVENT,
    isRtl: Boolean,
    strings: AppStrings,
    startTimeReference: String? = null,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val initialParsed = remember(initialTime) {
        val defHour = when (type) {
            TimePickerType.EVENT -> 11
            TimePickerType.START -> 9
            TimePickerType.END -> 10
        }
        TimeValidator.parseTime(initialTime, defHour, 0)
    }

    var is24HourMode by remember { mutableStateOf(true) }
    var selectedHour by remember { mutableIntStateOf(initialParsed.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialParsed.minute) }

    val currentParsed = remember(selectedHour, selectedMinute) {
        ParsedTime.ofSafe(selectedHour, selectedMinute)
    }

    val dialogTitle = when (type) {
        TimePickerType.EVENT -> strings.time
        TimePickerType.START -> strings.startLabel
        TimePickerType.END -> strings.endLabel
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.70f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 440.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Intercept background clicks
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    CanvasNavy.copy(alpha = 0.98f),
                                    Color(0xFF0D1426).copy(alpha = 0.98f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    GlassBorderBright.copy(alpha = 0.40f),
                                    GlassBorderDefault.copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(AccentElectricBlue.copy(alpha = 0.15f))
                                        .border(0.8.dp, AccentElectricBlue.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = AccentElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = dialogTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                    Text(
                                        text = if (is24HourMode) currentParsed.format24Hour(isRtl) else currentParsed.format12Hour(isRtl),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = AccentElectricBlue,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                            }

                            // 12h / 24h Toggle Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassSurfaceHighlight)
                                    .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                                    .clickable { is24HourMode = !is24HourMode }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("btn_toggle_12_24_hour")
                            ) {
                                Text(
                                    text = if (is24HourMode) "24H" else "12H",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentElectricBlue,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Digital Display with Liquid Glass Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour Box
                            val displayHourVal = if (is24HourMode) {
                                String.format(Locale.US, "%02d", selectedHour)
                            } else {
                                String.format(Locale.US, "%d", currentParsed.hour12)
                            }
                            val displayHourText = if (isRtl) LocalizationManager.formatDigits(displayHourVal) else displayHourVal

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        selectedHour = if (selectedHour >= 23) 0 else selectedHour + 1
                                    },
                                    modifier = Modifier.size(36.dp).testTag("btn_hour_plus")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase Hour",
                                        tint = AccentElectricBlue
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(width = 84.dp, height = 72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(GlassSurfaceHighlight)
                                        .border(1.2.dp, AccentElectricBlue.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayHourText,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        selectedHour = if (selectedHour <= 0) 23 else selectedHour - 1
                                    },
                                    modifier = Modifier.size(36.dp).testTag("btn_hour_minus")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease Hour",
                                        tint = TextWhiteSecondary
                                    )
                                }
                            }

                            // Colon separator
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = TextWhiteMuted,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )

                            // Minute Box
                            val displayMinVal = String.format(Locale.US, "%02d", selectedMinute)
                            val displayMinText = if (isRtl) LocalizationManager.formatDigits(displayMinVal) else displayMinVal

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = {
                                        val next = (selectedMinute / 5 * 5 + 5)
                                        selectedMinute = if (next >= 60) 0 else next
                                    },
                                    modifier = Modifier.size(36.dp).testTag("btn_minute_plus")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase Minute",
                                        tint = AccentElectricBlue
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(width = 84.dp, height = 72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(GlassSurfaceHighlight)
                                        .border(1.2.dp, AccentRoyalViolet.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayMinText,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val prev = (selectedMinute / 5 * 5 - 5)
                                        selectedMinute = if (prev < 0) 55 else prev
                                    },
                                    modifier = Modifier.size(36.dp).testTag("btn_minute_minus")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease Minute",
                                        tint = TextWhiteSecondary
                                    )
                                }
                            }

                            // 12-hour AM / PM toggle buttons (only visible in 12h mode)
                            if (!is24HourMode) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val amLabel = if (isRtl) "ق.ظ" else "AM"
                                    val pmLabel = if (isRtl) "ب.ظ" else "PM"
                                    val isAm = !currentParsed.isPm

                                    // AM Pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isAm) AccentElectricBlue else GlassSurfaceHighlight)
                                            .border(
                                                width = 0.8.dp,
                                                color = if (isAm) AccentElectricBlue else GlassBorderSubtle,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (selectedHour >= 12) selectedHour -= 12
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("btn_am")
                                    ) {
                                        Text(
                                            text = amLabel,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = if (isAm) Color.White else TextWhiteSecondary,
                                                fontWeight = if (isAm) FontWeight.SemiBold else FontWeight.Medium,
                                                letterSpacing = 0.sp
                                            )
                                        )
                                    }

                                    // PM Pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (!isAm) AccentElectricBlue else GlassSurfaceHighlight)
                                            .border(
                                                width = 0.8.dp,
                                                color = if (!isAm) AccentElectricBlue else GlassBorderSubtle,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (selectedHour < 12) selectedHour += 12
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("btn_pm")
                                    ) {
                                        Text(
                                            text = pmLabel,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = if (!isAm) Color.White else TextWhiteSecondary,
                                                fontWeight = if (!isAm) FontWeight.SemiBold else FontWeight.Medium,
                                                letterSpacing = 0.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Minute Quick-Pick Row (00, 15, 30, 45)
                        Text(
                            text = if (isRtl) "دقایق متداول" else "Quick Minutes",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextWhiteMuted,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val minuteOptions = listOf(0, 15, 30, 45)
                            for (m in minuteOptions) {
                                val isSelected = selectedMinute == m
                                val mStr = String.format(Locale.US, "%02d", m)
                                val label = if (isRtl) LocalizationManager.formatDigits(mStr) else mStr
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AccentElectricBlue else GlassSurfaceHighlight)
                                        .border(
                                            width = 0.8.dp,
                                            color = if (isSelected) AccentElectricBlue else GlassBorderSubtle,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedMinute = m }
                                        .padding(vertical = 8.dp)
                                        .testTag("quick_minute_$m"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) Color.White else TextWhiteSecondary,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            letterSpacing = 0.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Duration shortcuts if selecting End Time
                        if (type == TimePickerType.END && !startTimeReference.isNullOrBlank()) {
                            val startParsed = remember(startTimeReference) {
                                TimeValidator.parseTime(startTimeReference)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isRtl) "مدت زمان از زمان شروع" else "Duration from Start",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextWhiteMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf(
                                    Pair(15, strings.duration15m),
                                    Pair(30, strings.duration30m),
                                    Pair(60, strings.duration1h),
                                    Pair(120, strings.duration2h)
                                )
                                for ((minutes, label) in presets) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GlassSurfaceHighlight)
                                            .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(10.dp))
                                            .clickable {
                                                val newTotal = (startParsed.minutesOfDay + minutes).coerceAtMost(23 * 60 + 59)
                                                selectedHour = newTotal / 60
                                                selectedMinute = newTotal % 60
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = AccentElectricBlue,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 0.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons: Cancel and Confirm
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Cancel
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GlassSurfaceHighlight)
                                    .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                                    .clickable(onClick = onDismiss)
                                    .padding(vertical = 12.dp)
                                    .testTag("btn_time_picker_cancel"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.cancel,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextWhiteSecondary,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.sp
                                    )
                                )
                            }

                            // Confirm
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(AccentElectricBlue, AccentRoyalViolet)
                                        )
                                    )
                                    .clickable {
                                        val canonical = currentParsed.canonicalTime
                                        onTimeSelected(canonical)
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("btn_time_picker_confirm"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.save,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
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
}
