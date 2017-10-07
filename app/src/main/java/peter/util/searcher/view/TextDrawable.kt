package peter.util.searcher.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.IntRange

import peter.util.searcher.R


@Suppress("DEPRECATION")
/**
 * 可动态改变数字的drawable
 *
 *
 * Created by peter on 2017/7/11.
 */

class TextDrawable(private val context: Context) : Drawable() {

    private var text: String? = null
    private val paint: Paint = Paint()

    fun setText(text: Int) {
        this.text = text.toString()
        paint.color = context.resources.getColor(R.color.tint_color)
        val size = sp2px(context, 10f)

        paint.textSize = size.toFloat()
        paint.isAntiAlias = true

        paint.isFakeBoldText = true
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER

        paint.getTextBounds(this.text, 0, this.text!!.length,  Rect())
        invalidateSelf()
    }

    private fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    override fun draw(canvas: Canvas) {
        val vectorDrawable = context.resources.getDrawable(R.drawable.ic_multi)
        val bitmap = getBitmap(vectorDrawable)
        val centreX = (canvas.width - bitmap.width) / 2
        val centreY = (canvas.height - bitmap.height) / 2
        canvas.drawBitmap(bitmap, centreX.toFloat(), centreY.toFloat(), paint)
        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()
        canvas.drawText(text!!, xPos.toFloat(), yPos.toFloat(), paint)
    }

    private fun getBitmap(vectorDrawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

}
