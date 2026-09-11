package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarEvent
import com.example.data.holiday.Holiday
import com.example.util.CalendarType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentDeepViolet
import com.example.ui.theme.AccentElectricBlue
import com.example.ui.theme.AccentRoyalViolet
import com.example.ui.theme.AmbientGlowCyan
import com.example.ui.theme.AmbientGlowIndigo
import com.example.ui.theme.AmbientGlowPurple
import com.example.ui.theme.AmbientGlowRose
import com.example.ui.theme.CanvasBlack
import com.example.ui.theme.CanvasNavy
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderDefault
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceDefault
import com.example.ui.theme.GlassSurfaceHighlight
import com.example.ui.theme.GlassSurfaceUltraLight
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.util.CalendarDay
import com.example.util.LocalAppStrings
import com.example.util.LocalCalendarType
import com.example.util.LocalizationManager

/**
 * Atmospheric deep-space background with layered ambient glow orbs behind translucent glass.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    accentGlow: Color = AccentRoyalViolet,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBlack)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Base deep navy-black atmospheric gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CanvasBlack,
                        CanvasNavy.copy(alpha = 0.85f),
                        CanvasBlack
                    )
                )
            )

            // Top-right ethereal violet light source
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentGlow.copy(alpha = 0.35f),
                        AmbientGlowPurple.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.85f, canvasH * 0.15f),
                    radius = canvasW * 0.7f
                )
            )

            // Mid-left subtle cyan neon ambient pool
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmbientGlowCyan.copy(alpha = 0.28f),
                        AmbientGlowCyan.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.1f, canvasH * 0.48f),
                    radius = canvasW * 0.65f
                )
            )

            // Bottom-right deep indigo anchor glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmbientGlowIndigo.copy(alpha = 0.30f),
                        AmbientGlowRose.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.75f, canvasH * 0.82f),
                    radius = canvasW * 0.75f
                )
            )
        }

        content()
    }
}

/**
 * Premium translucent glass card container with specular highlight gradient rim.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    surfaceColor: Color = GlassSurfaceDefault,
    borderColor: Color = GlassBorderDefault,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "glass_card",
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = tween(150),
        label = "glass_press"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            GlassBorderBright.copy(alpha = 0.45f),
            borderColor.copy(alpha = 0.25f),
            Color(0x0DFFFFFF)
        ),
        start = Offset(0f, 0f),
        end = Offset(300f, 600f)
    )

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            surfaceColor.copy(alpha = if (isPressed) 0.22f else 0.14f),
            surfaceColor.copy(alpha = if (isPressed) 0.14f else 0.07f)
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .testTag(testTag)
            .clip(shape)
            .background(brush = bgBrush)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * Tactical Glass Button with iOS styling.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    icon: ImageVector? = null,
    testTag: String = "glass_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    val shape = RoundedCornerShape(24.dp)
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                AccentRoyalViolet,
                AccentElectricBlue
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                GlassSurfaceHighlight.copy(alpha = if (isPressed) 0.35f else 0.20f),
                GlassSurfaceDefault.copy(alpha = if (isPressed) 0.20f else 0.10f)
            )
        )
    }

    val borderBrush = if (isPrimary) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.6f),
                AccentElectricBlue.copy(alpha = 0.3f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                GlassBorderBright.copy(alpha = 0.4f),
                GlassBorderSubtle
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .testTag(testTag)
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderBrush, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isPrimary) Color.White else TextWhitePrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isPrimary) Color.White else TextWhitePrimary,
                    fontSize = 14.sp
                )
            )
        }
    }
}

/**
 * Frosted Glass Icon Button.
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 40.dp,
    tint: Color = TextWhitePrimary,
    testTag: String = "glass_icon_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .testTag(testTag)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = if (isPressed) 0.25f else 0.15f),
                        Color.White.copy(alpha = if (isPressed) 0.12f else 0.06f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * Floating Glass Orb Action Button ("+") with radial blur and soft electric glow.
 */
@Composable
fun FloatingGlassActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "floating_action_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(120),
        label = "fab_scale"
    )

    Box(
        modifier = modifier
            .size(60.dp)
            .scale(scale)
            .testTag(testTag)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = AccentRoyalViolet.copy(alpha = 0.6f),
                spotColor = AccentElectricBlue.copy(alpha = 0.8f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AccentRoyalViolet.copy(alpha = 0.95f),
                        AccentElectricBlue.copy(alpha = 0.95f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.2f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner specular reflection ring
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(
                    0.8.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New Event",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Modern iOS Calendar Cell.
 */
@Composable
fun CalendarCell(
    day: CalendarDay,
    hasEvents: Boolean,
    eventColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "calendar_cell_${day.dateString}"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "cell_scale"
    )

    val holidayRed = Color(0xFFFF453A)
    val holidayRedSelected = Color(0xFFFF6B6B)

    Box(
        modifier = modifier
            .height(50.dp)
            .scale(scale)
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(
                when {
                    day.isSelected -> Modifier
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AccentRoyalViolet.copy(alpha = 0.45f),
                                    AccentElectricBlue.copy(alpha = 0.25f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.7f),
                                    AccentElectricBlue.copy(alpha = 0.5f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                    day.isToday -> Modifier
                        .border(
                            1.dp,
                            AccentElectricBlue.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp)
                        )

                    else -> Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day number container
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (day.isToday && !day.isSelected) {
                            Modifier
                                .clip(CircleShape)
                                .background(AccentRoyalViolet)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.displayNumber.ifEmpty { day.dayOfMonth.toString() },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (day.isSelected || day.isToday || day.isHoliday) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            day.isHoliday && day.isSelected -> holidayRedSelected
                            day.isSelected -> Color.White
                            day.isHoliday && day.isToday -> holidayRedSelected
                            day.isToday -> Color.White
                            day.isHoliday && day.isCurrentMonth -> holidayRed
                            day.isHoliday && !day.isCurrentMonth -> holidayRed.copy(alpha = 0.5f)
                            day.isCurrentMonth -> TextWhitePrimary
                            else -> TextWhiteMuted.copy(alpha = 0.4f)
                        },
                        fontSize = 15.sp,
                        letterSpacing = 0.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Event and holiday indicator dots beneath date
            val displayDots = remember(day.isHoliday, hasEvents, eventColors) {
                when {
                    day.isHoliday && hasEvents -> listOf(holidayRed) + eventColors.take(2)
                    day.isHoliday -> listOf(holidayRed)
                    hasEvents -> eventColors.take(3).ifEmpty { listOf(AccentElectricBlue) }
                    else -> emptyList()
                }
            }

            if (displayDots.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(4.dp)
                ) {
                    displayDots.forEach { dotColor ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Compact translucent glass card representing an event with accent stripe, time, category, and location.
 */
@Composable
fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "event_card_${event.id}"
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(event.colorHex))
    } catch (e: Exception) {
        AccentRoyalViolet
    }

    val strings = LocalAppStrings.current
    val calendarType = LocalCalendarType.current
    val categoryName = LocalizationManager.getCategoryName(event.category, strings)
    val timeFormatted = LocalizationManager.formatTimeRange(event.startTime, event.endTime, calendarType)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        surfaceColor = GlassSurfaceDefault,
        borderColor = categoryColor.copy(alpha = 0.35f),
        onClick = onClick,
        testTag = testTag
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Vertical Accent Capsule
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                categoryColor,
                                categoryColor.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main event details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhitePrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Category Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .border(0.8.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = categoryColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Time
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = TextWhiteSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextWhiteSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    // Optional Location
                    if (event.location.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = TextWhiteMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextWhiteMuted
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Elegant Liquid Glass card displaying an official Holiday.
 */
@Composable
fun HolidayCard(
    holiday: Holiday,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val holidayRed = Color(0xFFFF453A)
    val badgeText = if (holiday.calendarType == CalendarType.GREGORIAN) "Federal Holiday" else "تعطیل رسمی"

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        surfaceColor = GlassSurfaceDefault,
        borderColor = holidayRed.copy(alpha = 0.45f),
        onClick = onClick,
        testTag = "holiday_card_${holiday.id}"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Holiday Red Accent Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                holidayRed,
                                holidayRed.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = holiday.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhitePrimary,
                            letterSpacing = 0.sp
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Red Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(holidayRed.copy(alpha = 0.18f))
                            .border(0.8.dp, holidayRed.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = holidayRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = holiday.description.ifEmpty {
                        if (holiday.calendarType == CalendarType.GREGORIAN) "Official Public Holiday" else "مناسبت تقویم رسمی کشور"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextWhiteSecondary.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        letterSpacing = 0.sp
                    )
                )
            }
        }
    }
}

/**
 * Frosted Glass Input Field.
 */
@Composable
fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = "glass_input"
) {
    val shape = RoundedCornerShape(16.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clip(shape)
            .background(GlassSurfaceDefault)
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextWhitePrimary,
            fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(AccentElectricBlue),
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = TextWhiteSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextWhiteMuted)
                        )
                    }
                    innerTextField()
                }

                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    )
}

/**
 * Category Chip.
 */
@Composable
fun CategoryChip(
    name: String,
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "category_chip_$name"
) {
    val shape = RoundedCornerShape(14.dp)
    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0.2f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                GlassSurfaceDefault,
                GlassSurfaceUltraLight
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                color,
                color.copy(alpha = 0.5f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                GlassBorderSubtle,
                Color.Transparent
            )
        )
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderBrush, shape)
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) TextWhitePrimary else TextWhiteSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}

/**
 * iOS-like Liquid Glass Tab Bar item.
 */
@Composable
fun GlassTabBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    items: List<Pair<String, ImageVector>>,
    testTag: String = "glass_tab_bar"
) {
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .testTag(testTag)
            .shadow(
                elevation = 20.dp,
                shape = shape,
                ambientColor = CanvasBlack.copy(alpha = 0.8f),
                spotColor = AccentRoyalViolet.copy(alpha = 0.4f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xE6141A2E), // 90% opacity deep navy glass
                        Color(0xF00A0D18)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedTabIndex == index
                val itemShape = RoundedCornerShape(24.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_${item.first.lowercase()}")
                        .clip(itemShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                AccentRoyalViolet.copy(alpha = 0.35f),
                                                AccentElectricBlue.copy(alpha = 0.15f)
                                            )
                                        )
                                    )
                                    .border(
                                        0.8.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.4f),
                                                AccentElectricBlue.copy(alpha = 0.2f)
                                            )
                                        ),
                                        itemShape
                                    )
                            } else Modifier
                        )
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.second,
                            contentDescription = item.first,
                            tint = if (isSelected) AccentElectricBlue else TextWhiteMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.first,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) TextWhitePrimary else TextWhiteMuted,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 11.sp,
                                letterSpacing = 0.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * iOS-style Toggle Switch.
 */
@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = AccentElectricBlue,
    testTag: String = "glass_toggle"
) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(180),
        label = "toggle_thumb"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .width(50.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        listOf(
                            activeColor,
                            AccentRoyalViolet
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                }
            )
            .border(
                1.dp,
                if (checked) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(15.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Section Header with iOS styling.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextWhitePrimary
            )
        )

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = AccentElectricBlue,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Realistic iPhone Chrome: Dynamic Island, Status Bar & Home Indicator
 */
@Composable
fun IPhoneStatusBar(
    modifier: Modifier = Modifier,
    timeText: String = "9:41"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Time
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                color = TextWhitePrimary,
                fontSize = 15.sp
            )
        )

        // Center: Dynamic Island Pill
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Camera sensor dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF181B26))
                )
            }
        }

        // Right: Status icons (Cellular, Wifi, Battery)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Signal bars indicator
            Canvas(modifier = Modifier.size(14.dp, 10.dp)) {
                val barW = 2.5.dp.toPx()
                val gap = 1.5.dp.toPx()
                for (i in 0 until 4) {
                    val h = (size.height * (i + 1) / 4f)
                    drawRect(
                        color = TextWhitePrimary,
                        topLeft = Offset(i * (barW + gap), size.height - h),
                        size = androidx.compose.ui.geometry.Size(barW, h)
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Battery Capsule
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.dp, TextWhitePrimary, RoundedCornerShape(3.dp))
                    .padding(1.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(TextWhitePrimary)
                )
            }
        }
    }
}

@Composable
fun IPhoneHomeIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(135.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.5f))
        )
    }
}
