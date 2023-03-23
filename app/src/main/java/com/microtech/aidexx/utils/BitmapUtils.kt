package com.microtech.aidexx.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import com.microtech.aidexx.common.getContext
import kotlin.math.roundToInt

object BitmapUtils {

    fun dp2px(dp: Float): Int {
        return (getContext().resources.displayMetrics.density * dp).roundToInt()
    }

    fun getDrawableFromResource(id: Int): Drawable? {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getContext().resources.getDrawable(id, null)
        } else {
            null
        }
    }

    fun getBitmapFromResource(id: Int, width: Float, height: Float): BitmapDrawable {
        val originalBitmap = getBitmapFromResource(id)
        val matrix = Matrix()
        matrix.postScale(width / originalBitmap.width, height / originalBitmap.height)
        val changedBitmap = Bitmap.createBitmap(
            originalBitmap, 0, 0,
            originalBitmap.width, originalBitmap.height, matrix, true
        )
        return BitmapDrawable(getContext().resources, changedBitmap)
    }

    fun getBitmapFromResource(id: Int): Bitmap {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable = getContext().resources.getDrawable(id, null)
            val bitmap = Bitmap.createBitmap(
                vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            bitmap
        } else {
            BitmapFactory.decodeResource(getContext().resources, id)
        }
    }

}