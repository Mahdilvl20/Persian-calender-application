package com.aistudio.lumacalendar.vtxk.ui.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.lumacalendar.vtxk.ui.theme.CategorySpecial
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhitePrimary
import com.aistudio.lumacalendar.vtxk.ui.theme.TextWhiteSecondary
import com.aistudio.lumacalendar.vtxk.util.LocalAppStrings
import com.aistudio.lumacalendar.vtxk.ui.theme.LocalLumaAppearance

/**
 * Centered Liquid Glass confirmation dialog for destructive actions (FR-008).
 *
 * Mirrors the delete-confirmation dialog in [EventDetailSheet]: dismiss on back or
 * tap-outside performs no action, Confirm invokes [onConfirm]. Labels are supplied
 * by the caller so every string stays localized through AppStrings.
 *
 * Layout direction and strings are captured before the Dialog opens so they are
 * re-applied inside the dialog window regardless of how locals propagate.
 */
@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    icon: ImageVector,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val layoutDirection = LocalLayoutDirection.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection,
            LocalAppStrings provides strings
        ) {
            Box(
                modifier = modifier
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
                        .testTag("dialog_destructive_confirmation")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CategorySpecial.copy(alpha = 0.15f))
                                .border(1.dp, CategorySpecial.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = CategorySpecial,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                color = TextWhitePrimary,
                                letterSpacing = 0.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = message,
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassButton(
                                text = strings.cancel,
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_cancel_confirmation"
                            )
                            GlassButton(
                                text = confirmLabel,
                                onClick = onConfirm,
                                modifier = Modifier.weight(1f),
                                isPrimary = true,
                                testTag = "btn_confirm_action"
                            )
                        }
                    }
                }
            }
        }
    }
}
