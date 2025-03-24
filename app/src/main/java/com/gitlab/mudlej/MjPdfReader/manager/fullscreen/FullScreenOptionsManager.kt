package com.gitlab.mudlej.MjPdfReader.manager.fullscreen

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import kotlin.reflect.KFunction1

interface FullScreenOptionsManager {

    enum class VisibilityState { VISIBLE, INVISIBLE }

    fun isVisible(): Boolean

    fun showAll()

    fun hideAll()

    fun toggleAll()

    fun showAllDelayed()

    fun hideAllDelayed()

    fun toggleAllDelayed()

    fun showAllTemporarily()

    fun hideAllTemporarily()

    fun toggleAllTemporarily()

    fun showAllTemporarilyOrHide()

    fun permanentlyHidePageHandle()

    fun getOnTouchListener(): View.OnTouchListener

    fun toggleLabelVisibility(context: Context, drawableOf: KFunction1<Int, Drawable?>, getLabel: KFunction1<Int, String?>)

    fun isLabelsVisible(): Boolean
}

