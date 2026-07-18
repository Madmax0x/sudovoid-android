package com.aryan.sudovoid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.pow

/**
 * Draws the same "perspective dot grid" look used in the SudoVoid desktop app,
 * adapted to a static (non-animated, cheap to redraw) Android canvas.
 */
class GridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 1.2f }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint()
    private val glowPaint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // background vertical gradient
        bgPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            Color.parseColor("#050D1A"), Color.parseColor("#020810"),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val vpX = w * 0.5f
        val vpY = h * 0.42f
        val farY = h * 0.98f
        val farHalfW = w * 0.85f
        val vLines = 22
        val hLines = 16

        // vanishing-point converging lines
        for (i in 0..vLines) {
            val t = i / vLines.toFloat()
            val farX = (vpX - farHalfW) + t * farHalfW * 2
            val dc = abs(t - 0.5f) * 2
            linePaint.color = Color.argb((70 * (1 - dc * 0.7f)).toInt().coerceIn(0, 255), 30, 160, 220)
            canvas.drawLine(vpX, vpY, farX, farY, linePaint)
        }

        // horizontal depth lines + dots at intersections
        for (r in 1..hLines) {
            val t = r / hLines.toFloat()
            val tp = t.toDouble().pow(2.3).toFloat()
            val y = vpY + (farY - vpY) * tp
            val hw = farHalfW * tp

            linePaint.color = Color.argb((35 + 130 * tp).toInt().coerceIn(0, 255), 30, 160, 220)
            canvas.drawLine(vpX - hw, y, vpX + hw, y, linePaint)

            for (c in 0..vLines) {
                val ct = c / vLines.toFloat()
                val farX = (vpX - farHalfW) + ct * farHalfW * 2
                val x = vpX + (farX - vpX) * tp
                val dc = abs(ct - 0.5f) * 2
                val alpha = ((30 + 180 * tp) * (1 - dc * 0.5f)).toInt().coerceIn(0, 255)
                val size = 1.2f + 4.5f * tp * (1 - dc * 0.35f)
                dotPaint.color = Color.argb(alpha, 50, 190, 240)
                canvas.drawCircle(x, y, size, dotPaint)
            }
        }

        // soft radial glow near vanishing point
        glowPaint.shader = RadialGradient(
            vpX, vpY, w * 0.5f,
            intArrayOf(
                Color.argb(30, 10, 100, 180),
                Color.argb(10, 5, 60, 120),
                Color.argb(0, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, glowPaint)
    }
}
