package com.gitlab.mudlej.MjPdfReader.util

import android.content.res.Resources
import android.util.TypedValue

object ViewUtil {

    val Number.inPx get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

}