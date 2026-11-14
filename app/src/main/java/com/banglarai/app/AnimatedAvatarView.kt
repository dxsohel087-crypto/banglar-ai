package com.banglarai.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Lightweight, locally drawn animated companion avatar.
 * The mouth animates while TextToSpeech is active; no network image is required.
 */
class AnimatedAvatarView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var speaking = false
    private var phase = 0f
    private val motion = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1100L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        contentDescription = "বাংলার AI অ্যানিমেটেড অবতার"
        motion.start()
    }

    fun setSpeaking(active: Boolean) {
        if (speaking != active) {
            speaking = active
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val cx = w / 2f
        val cy = h / 2f
        val unit = minOf(w, h) / 150f
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(unit, unit)

        // Soft cyan halo and breathing ring.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.argb(65, 75, 205, 255)
        canvas.drawCircle(0f, 0f, 66f + kotlin.math.sin(phase * Math.PI * 2).toFloat() * 2f, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(35, 62, 176, 255)
        canvas.drawCircle(0f, 0f, 61f, paint)

        // Hair silhouette.
        paint.color = Color.rgb(32, 24, 48)
        val hair = Path().apply {
            moveTo(-43f, 18f)
            cubicTo(-55f, -45f, -30f, -63f, 0f, -61f)
            cubicTo(40f, -64f, 56f, -32f, 43f, 23f)
            lineTo(34f, 49f)
            lineTo(-35f, 49f)
            close()
        }
        canvas.drawPath(hair, paint)

        // Neck and shoulders.
        paint.color = Color.rgb(232, 166, 145)
        canvas.drawRoundRect(RectF(-11f, 31f, 11f, 53f), 7f, 7f, paint)
        paint.color = Color.rgb(48, 105, 168)
        canvas.drawOval(RectF(-43f, 43f, 43f, 91f), paint)
        paint.color = Color.rgb(84, 190, 235)
        canvas.drawArc(RectF(-43f, 45f, 43f, 92f), 190f, 160f, false, paint)

        // Face.
        paint.color = Color.rgb(250, 205, 181)
        canvas.drawOval(RectF(-35f, -43f, 35f, 45f), paint)

        // Side hair and fringe.
        paint.color = Color.rgb(37, 27, 50)
        canvas.drawOval(RectF(-43f, -43f, -29f, 43f), paint)
        canvas.drawOval(RectF(29f, -43f, 43f, 43f), paint)
        val fringe = Path().apply {
            moveTo(-36f, -23f)
            cubicTo(-27f, -65f, 28f, -62f, 38f, -22f)
            cubicTo(18f, -35f, 2f, -18f, -12f, -25f)
            cubicTo(-24f, -18f, -29f, -18f, -36f, -23f)
            close()
        }
        canvas.drawPath(fringe, paint)

        // Eyes.
        paint.color = Color.rgb(62, 42, 48)
        canvas.drawCircle(-13f, -4f, 3.2f, paint)
        canvas.drawCircle(13f, -4f, 3.2f, paint)
        paint.color = Color.argb(110, 245, 116, 128)
        canvas.drawCircle(-23f, 15f, 5f, paint)
        canvas.drawCircle(23f, 15f, 5f, paint)

        // Animated mouth: closed at rest, gently opens/closes while speaking.
        paint.color = Color.rgb(143, 55, 73)
        if (speaking) {
            val opening = 3f + (kotlin.math.sin(phase * Math.PI * 4).toFloat() + 1f) * 4f
            canvas.drawOval(RectF(-5f, 22f, 5f, 22f + opening * 1.8f), paint)
        } else {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2f
            val smile = Path().apply {
                moveTo(-7f, 24f)
                quadTo(0f, 31f, 7f, 24f)
            }
            canvas.drawPath(smile, paint)
            paint.style = Paint.Style.FILL
        }

        // Tiny cyan sparkle.
        paint.color = Color.rgb(115, 224, 255)
        canvas.drawCircle(48f, -42f, 3f, paint)
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        motion.cancel()
        super.onDetachedFromWindow()
    }
}
