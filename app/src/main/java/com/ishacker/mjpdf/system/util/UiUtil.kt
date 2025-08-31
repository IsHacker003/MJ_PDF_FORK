package com.ishacker.mjpdf.system.util

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.Menu
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.ishacker.mjpdf.system.R
import top.defaults.colorpicker.ColorPickerPopup


@SuppressLint("RestrictedApi")
fun Menu.showOptionalIcons() {
    if (this is MenuBuilder) {
        setOptionalIconsVisible(true)
    }
}

fun newColorPicker(activity: Activity): ColorPickerPopup {
    return ColorPickerPopup.Builder(activity)
        .initialColor(Color.BLUE)
        .enableBrightness(true)
        .enableAlpha(true)
        .okTitle(activity.getString(R.string.ok))
        .cancelTitle(activity.getString(R.string.cancel))
        .build()
}


fun configureSearchIcon(menu: Menu, show: Boolean) {
    val searchItem = menu.findItem(R.id.search_in_search_activity)
    searchItem?.isVisible = show
}

fun toggleViewStartConstraint(dynamicView: LinearLayoutCompat, staticView: Int) {
    val constraintLayout = dynamicView.parent as? ConstraintLayout
    if (constraintLayout == null) {
        Log.e("UiUtil", "toggleViewStartConstraint: constraintLayout is null for the view!!")
        return
    }

    val constraintSet = ConstraintSet()
    constraintSet.clone(constraintLayout)

    val currentConstraint = constraintSet.getConstraint(dynamicView.id)
    val isCurrentlyAlignedToParent = currentConstraint.layout.startToStart == ConstraintSet.PARENT_ID

    if (isCurrentlyAlignedToParent) {
        //Log.d("UiUtil", "AlignToButtonsStart")
        constraintSet.clear(dynamicView.id, ConstraintSet.START)
        constraintSet.connect(dynamicView.id, ConstraintSet.START, staticView, ConstraintSet.END)
    } else {
        //Log.d("UiUtil", "AlignToParentStart")
        constraintSet.clear(dynamicView.id, ConstraintSet.START)
        constraintSet.connect(dynamicView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
    }

    constraintSet.applyTo(constraintLayout)
}