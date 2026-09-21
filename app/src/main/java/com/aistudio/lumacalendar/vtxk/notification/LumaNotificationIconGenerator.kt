package com.aistudio.lumacalendar.vtxk.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.aistudio.lumacalendar.vtxk.R

/**
 * Generates a dynamic 2026-style premium glassmorphism notification icon
 * with deep blue -> violet glass gradient, minimal white calendar outline,
 * and the actual current day number.
 */
object LumaNotificationIconGenerator {

    fun generateIcon(
        context: Context,
        dayText: String,
        sizePx: Int = 120
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val size = sizePx.toFloat()
        val cornerRadius = size * 0.28f

        // 1. Deep Blue -> Violet Glass Gradient Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, size, size,
                intArrayOf(
                    Color.rgb(30, 27, 75),   // #1E1B4B - Deep indigo / blue
                    Color.rgb(67, 56, 202),  // #4338CA - Vibrant indigo
                    Color.rgb(124, 58, 237)  // #7C3AED - Violet glow
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        val bgRect = RectF(1.5f, 1.5f, size - 1.5f, size - 1.5f)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)

        // 2. Subtle glass highlight stroke (1.5px translucent border)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            shader = LinearGradient(
                0f, 0f, 0f, size,
                intArrayOf(
                    Color.argb(120, 255, 255, 255), // Top edge highlight
                    Color.argb(50, 129, 140, 248),  // Subtle indigo
                    Color.argb(80, 167, 139, 250)   // Subtle violet bottom
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, strokePaint)

        // 3. Top subtle glass sheen (reflection effect)
        val sheenPath = Path().apply {
            addRoundRect(
                RectF(2f, 2f, size - 2f, size * 0.52f),
                floatArrayOf(
                    cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius,
                    0f, 0f,
                    0f, 0f
                ),
                Path.Direction.CW
            )
        }
        val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, size * 0.52f,
                Color.argb(45, 255, 255, 255),
                Color.argb(0, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(sheenPath, sheenPaint)

        // 4. Minimal white calendar outline & header
        val calMarginLeft = size * 0.22f
        val calMarginTop = size * 0.26f
        val calMarginRight = size * 0.78f
        val calMarginBottom = size * 0.82f
        val calRadius = size * 0.08f

        val calRect = RectF(calMarginLeft, calMarginTop, calMarginRight, calMarginBottom)
        val calOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.035f
            color = Color.argb(220, 255, 255, 255)
        }
        canvas.drawRoundRect(calRect, calRadius, calRadius, calOutlinePaint)

        // Calendar header separator bar
        val headerY = calMarginTop + (calMarginBottom - calMarginTop) * 0.28f
        canvas.drawLine(
            calMarginLeft + 2f, headerY,
            calMarginRight - 2f, headerY,
            calOutlinePaint
        )

        // Two small binder tabs on top
        val binderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(240, 255, 255, 255)
        }
        val tabWidth = size * 0.055f
        val tabHeight = size * 0.10f
        val tab1X = size * 0.35f
        val tab2X = size * 0.65f - tabWidth
        val tabY = calMarginTop - tabHeight * 0.5f

        canvas.drawRoundRect(
            RectF(tab1X, tabY, tab1X + tabWidth, tabY + tabHeight),
            tabWidth * 0.5f, tabWidth * 0.5f, binderPaint
        )
        canvas.drawRoundRect(
            RectF(tab2X, tabY, tab2X + tabWidth, tabY + tabHeight),
            tabWidth * 0.5f, tabWidth * 0.5f, binderPaint
        )

        // 5. Large Day Number inside the calendar
        var customTypeface: Typeface? = null
        try {
            customTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
        } catch (_: Exception) {}

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = if (dayText.length > 2) size * 0.26f else size * 0.32f
        }

        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(dayText, 0, dayText.length, textBounds)
        val textCenterY = headerY + (calMarginBottom - headerY) * 0.52f - textBounds.exactCenterY()
        val textCenterX = size * 0.5f

        canvas.drawText(dayText, textCenterX, textCenterY, textPaint)

        return bitmap
    }

    /**
     * Generates a monochrome notification SMALL icon: an alpha mask of a
     * minimal calendar silhouette with the current day number centered inside.
     *
     * Android renders small icons as alpha masks and tints them (white on most
     * system UIs). Therefore this bitmap is opaque white on a transparent
     * background — NO color/gradient, unlike [generateIcon]. Pass the result to
     * NotificationCompat.Builder.setSmallIcon(IconCompat.createWithBitmap(...)).
     */
    fun generateSmallIcon(
        context: Context,
        dayText: String,
        sizePx: Int = 96
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val size = sizePx.toFloat()

        // Calendar silhouette bounds (leave breathing room so the system tint
        // circle doesn't clip it)
        val left = size * 0.16f
        val top = size * 0.20f
        val right = size * 0.84f
        val bottom = size * 0.86f
        val calRadius = size * 0.10f
        val calRect = RectF(left, top, right, bottom)

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.055f
            color = Color.WHITE
        }
        canvas.drawRoundRect(calRect, calRadius, calRadius, whitePaint)

        // Header separator bar
        val headerY = top + (bottom - top) * 0.26f
        canvas.drawLine(left, headerY, right, headerY, whitePaint)

        // Two binder tabs
        val tabPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val tabW = size * 0.06f
        val tabH = size * 0.11f
        val tabY = top - tabH * 0.5f
        canvas.drawRoundRect(
            RectF(size * 0.34f, tabY, size * 0.34f + tabW, tabY + tabH),
            tabW * 0.5f, tabW * 0.5f, tabPaint
        )
        canvas.drawRoundRect(
            RectF(size * 0.66f - tabW, tabY, size * 0.66f, tabY + tabH),
            tabW * 0.5f, tabW * 0.5f, tabPaint
        )

        // Day number (opaque white), size adapts to digit count so 1–31 fit
        var typeface: Typeface? = null
        try {
            typeface = ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
        } catch (_: Exception) {}

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            this.typeface = typeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = if (dayText.length > 1) size * 0.34f else size * 0.42f
        }
        val bounds = android.graphics.Rect()
        textPaint.getTextBounds(dayText, 0, dayText.length, bounds)
        val cx = size * 0.5f
        val cy = headerY + (bottom - headerY) * 0.5f - bounds.exactCenterY()
        canvas.drawText(dayText, cx, cy, textPaint)

        return bitmap
    }
}
