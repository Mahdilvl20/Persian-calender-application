package com.aistudio.lumacalendar.vtxk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Today
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.util.DateValidationResult
import com.aistudio.lumacalendar.vtxk.util.DateValidator
import com.aistudio.lumacalendar.vtxk.util.LocalAppStrings
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager

/**
 * Liquid Glass Modal Dialog for safe manual date input.
 * Allows entering Gregorian, Jalali, or Hijri dates with live validation,
 * error presentation, and seamless conversion into the canonical application date.
 * Guaranteed never to crash on any malformed, empty, or out-of-range user input.
 */
@Composable
fun ManualDateInputDialog(
    isOpen: Boolean,
    initialDate: String,
    activeCalendarType: CalendarType,
    onDismiss: () -> Unit,
    onDateSelected: (canonicalDate: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val strings = LocalAppStrings.current
    var selectedCalendarType by remember(isOpen, activeCalendarType) {
        mutableStateOf(activeCalendarType)
    }

    var inputText by remember(isOpen, initialDate, selectedCalendarType) {
        mutableStateOf(DateValidator.formatForManualInput(initialDate, selectedCalendarType))
    }

    var errorMessage by remember(isOpen) { mutableStateOf<String?>(null) }
    var validPreview by remember(isOpen, initialDate, selectedCalendarType) {
        val res = DateValidator.validateAndParse(inputText, selectedCalendarType, strings)
        mutableStateOf(if (res is DateValidationResult.Valid) res.formattedDisplayDate else null)
    }

    fun validateCurrentInput(text: String, calType: CalendarType): DateValidationResult {
        return DateValidator.validateAndParse(text, calType, strings)
    }

    fun onInputChanged(newText: String) {
        inputText = newText
        errorMessage = null
        val result = validateCurrentInput(newText, selectedCalendarType)
        validPreview = if (result is DateValidationResult.Valid) result.formattedDisplayDate else null
    }

    fun submitDate() {
        when (val result = validateCurrentInput(inputText, selectedCalendarType)) {
            is DateValidationResult.Valid -> {
                errorMessage = null
                onDateSelected(result.canonicalGregorianDate)
                onDismiss()
            }
            is DateValidationResult.Invalid -> {
                errorMessage = result.errorMessage
                validPreview = null
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            val dialogShape = RoundedCornerShape(26.dp)

            Box(
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .clip(dialogShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CanvasNavy.copy(alpha = 0.95f),
                                Color(0xFF090D18).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(
                                GlassBorderBright.copy(alpha = 0.45f),
                                AccentElectricBlue.copy(alpha = 0.25f),
                                GlassBorderSubtle
                            )
                        ),
                        dialogShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks */ }
                    .padding(22.dp)
                    .testTag("dialog_manual_date_picker")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with Icon, Title, and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentElectricBlue.copy(alpha = 0.18f))
                                    .border(1.dp, AccentElectricBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = AccentElectricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.selectDate,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.sp
                                )
                            )
                        }

                        GlassIconButton(
                            icon = Icons.Default.Close,
                            onClick = onDismiss,
                            contentDescription = strings.close,
                            size = 32.dp,
                            testTag = "btn_close_manual_date_dialog"
                        )
                    }

                    // Calendar System Selector Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassSurfaceDefault)
                            .border(0.8.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CalendarType.values().forEach { type ->
                            val isSelected = selectedCalendarType == type
                            val tabLabel = when (type) {
                                CalendarType.GREGORIAN -> strings.calGregorian
                                CalendarType.JALALI -> strings.calJalali
                                CalendarType.HIJRI -> strings.calHijri
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) AccentElectricBlue.copy(alpha = 0.28f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        if (isSelected) 1.dp else 0.dp,
                                        if (isSelected) AccentElectricBlue.copy(alpha = 0.6f)
                                        else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        if (selectedCalendarType != type) {
                                            // Preserve valid date across switching systems
                                            val valid = validateCurrentInput(inputText, selectedCalendarType)
                                            val canonical = if (valid is DateValidationResult.Valid) {
                                                valid.canonicalGregorianDate
                                            } else {
                                                initialDate
                                            }
                                            selectedCalendarType = type
                                            inputText = DateValidator.formatForManualInput(canonical, type)
                                            errorMessage = null
                                            val newValid = validateCurrentInput(inputText, type)
                                            validPreview = if (newValid is DateValidationResult.Valid) newValid.formattedDisplayDate else null
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("tab_calendar_${type.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) TextWhitePrimary else TextWhiteMuted,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    // Format hint & prompt
                    val formatPlaceholder = when (selectedCalendarType) {
                        CalendarType.GREGORIAN -> "2026-09-11 (YYYY-MM-DD)"
                        CalendarType.JALALI -> if (strings.tabCalendar == "تقویم") "۱۴۰۵/۰۶/۲۱ (سال/ماه/روز)" else "1405/06/21 (YYYY/MM/DD)"
                        CalendarType.HIJRI -> if (strings.tabCalendar == "تقویم") "۱۴۴۸/۰۳/۲۸ (سال/ماه/روز)" else "1448/03/28 (YYYY/MM/DD)"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = strings.manualDatePrompt,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextWhiteSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        // Input TextField with quick clear button
                        GlassInput(
                            value = inputText,
                            onValueChange = { onInputChanged(it) },
                            placeholder = formatPlaceholder,
                            leadingIcon = Icons.Outlined.CalendarMonth,
                            trailingIcon = {
                                if (inputText.isNotEmpty()) {
                                    GlassIconButton(
                                        icon = Icons.Default.Close,
                                        onClick = { onInputChanged("") },
                                        contentDescription = "Clear input",
                                        size = 26.dp,
                                        testTag = "btn_clear_date_input"
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { submitDate() }
                            ),
                            testTag = "input_manual_date"
                        )
                    }

                    // Validation Error presentation banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        errorMessage?.let { error ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF3B1218).copy(alpha = 0.88f))
                                    .border(1.dp, Color(0xFFFF453A).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("box_date_validation_error")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFFD1D1),
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.testTag("text_date_validation_error")
                                    )
                                }
                            }
                        }
                    }

                    // Valid Date Preview presentation
                    AnimatedVisibility(
                        visible = validPreview != null && errorMessage == null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        validPreview?.let { preview ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentElectricBlue.copy(alpha = 0.12f))
                                    .border(0.8.dp, AccentElectricBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AccentElectricBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = preview,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.testTag("text_date_preview")
                                    )
                                }
                            }
                        }
                    }

                    // Quick Jump to Today Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GlassSurfaceHighlight)
                                .border(0.8.dp, GlassBorderDefault, RoundedCornerShape(10.dp))
                                .clickable {
                                    val today = DateUtils.getRealDeviceDate()
                                    inputText = DateValidator.formatForManualInput(today, selectedCalendarType)
                                    errorMessage = null
                                    val valid = validateCurrentInput(inputText, selectedCalendarType)
                                    validPreview = if (valid is DateValidationResult.Valid) valid.formattedDisplayDate else null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("btn_quick_today_manual_date")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Today,
                                    contentDescription = null,
                                    tint = AccentElectricBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = strings.today,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccentElectricBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dialog Actions (Cancel & Confirm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(GlassSurfaceDefault)
                                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable { onDismiss() }
                                .testTag("btn_cancel_manual_date"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.cancel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextWhiteSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Confirm Button
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AccentRoyalViolet, AccentElectricBlue)
                                    )
                                )
                                .border(1.dp, GlassBorderBright.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { submitDate() }
                                .testTag("btn_confirm_manual_date"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.confirm,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
