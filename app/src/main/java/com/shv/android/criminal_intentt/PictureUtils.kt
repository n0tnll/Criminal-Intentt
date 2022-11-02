package com.shv.android.criminal_intentt

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.DisplayMetrics
import kotlin.math.roundToInt

fun getScaleBitmap(path: String, activity: Activity): Bitmap {
    val outMetrics = DisplayMetrics()
    val size = Point()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val display = activity.display
        display?.getRealMetrics(outMetrics)
        return getScaledBitmap(path, outMetrics.widthPixels, outMetrics.heightPixels)
    } else {
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getSize(size)
        return getScaledBitmap(path, size.x, size.y)
    }
}

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    //Reading size image on disk
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWith = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    //Выясняем, на сколько нужно уменьшить
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWith > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWith / destWidth

        val sampleScale = if (heightScale > widthScale) {
            heightScale
        } else {
            widthScale
        }
        inSampleSize = sampleScale.roundToInt()
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    //чтение и создание окончательного растрового изображения
    return BitmapFactory.decodeFile(path, options)
}