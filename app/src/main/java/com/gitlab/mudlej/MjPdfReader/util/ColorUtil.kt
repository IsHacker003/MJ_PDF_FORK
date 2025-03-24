package com.gitlab.mudlej.MjPdfReader.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.ActionBar
import com.google.android.material.elevation.SurfaceColors


object ColorUtil {

    fun colorize(context: Context, window: Window, actionBar: ActionBar?) {
        val color = SurfaceColors.SURFACE_2.getColor(context)
        // status bar color
        window.statusBarColor = color
        window.navigationBarColor = color

        // App Bar background color
        val colorDrawable = ColorDrawable(color)
        actionBar?.setBackgroundDrawable(colorDrawable)
    }
}