package com.gitlab.mudlej.MjPdfReader.manager.fullscreen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.FullScreenOptionsManager.VisibilityState
import com.google.android.material.button.MaterialButton
import kotlin.reflect.KFunction1


class FullScreenOptionsManagerImpl(
    private val binding: ActivityMainBinding,
    private val pdf: PDF,
    private val delay: Long,
) : FullScreenOptionsManager {

    private val delayHandler = Handler(Looper.getMainLooper())
    
    private var visibility: VisibilityState = VisibilityState.INVISIBLE
    private var labelVisibility: VisibilityState = VisibilityState.VISIBLE

    private val viewsList: List<View> = listOf(
        binding.fullScreenButtonsLayout,
        binding.exitFullScreenButton,
        binding.rotateScreenButton,

        binding.brightnessLayout,
        binding.brightnessButton,
        binding.brightnessSeekBar,
        binding.brightnessPercentage,

        binding.autoScrollLayout,
        binding.autoScrollButton,
        binding.decScrollSpeedButton,
        binding.toggleAutoScrollButton,
        binding.reverseScrollDirectionButton,
        binding.incScrollSpeedButton,

        binding.toggleHorizontalSwipeButton,
        binding.toggleZoomLockButton,
        binding.screenshotButton,
        binding.toggleLabelButton,
    )

    init {
        setOnTouchListenerForAll()
    }

    override fun isVisible() = visibility == VisibilityState.VISIBLE

    override fun showAll() {
        if (pdf.isFullScreenToggled) {
            showFullScreenButtons()
        }
        showPageHandle()
        showAutoScrollLayout()
        showBrightnessLayout()
        visibility = VisibilityState.VISIBLE
    }

    override fun hideAll() {
        hideFullScreenButtons()
        hidePageHandle()
        hideAutoScrollLayout()
        hideBrightnessLayout()
        visibility = VisibilityState.INVISIBLE
    }

    override fun toggleAll() {
        if (isVisible()) hideAll() else showAll()
    }

    override fun showAllDelayed() {
        delayAction(::showAll)
    }

    override fun hideAllDelayed() {
        delayAction(::hideAll)
    }

    override fun toggleAllDelayed() {
        delayAction(::toggleAll)
    }

    override fun showAllTemporarily() {
        doTemporarily(::showAll, ::hideAll)
    }

    override fun hideAllTemporarily() {
        doTemporarily(::hideAll, ::showAll)
    }

    override fun toggleAllTemporarily() {
        doTemporarily(::toggleAll, ::toggleAll)
    }

    override fun showAllTemporarilyOrHide() {
        if (!isVisible()) {
            showAllTemporarily()
        }
        else {
            hideAll()
        }
    }

    override fun permanentlyHidePageHandle() {
        binding.pdfView.scrollHandle?.permanentHide()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun getOnTouchListener(): View.OnTouchListener {
        val isEventFullyConsumed = false    // false so clickOnListener will be triggered
        return View.OnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> delayHandler.reset()
                MotionEvent.ACTION_UP -> hideAllDelayed()
            }
            isEventFullyConsumed
        }
    }

    override fun isLabelsVisible(): Boolean {
        return labelVisibility == VisibilityState.VISIBLE
    }

    override fun toggleLabelVisibility(context: Context, drawableOf: KFunction1<Int, Drawable?>, getLabel: KFunction1<Int, String?>) {
        binding.apply {
            val buttons = mapOf(
                exitFullScreenButton to getLabel(R.string.exit),
                rotateScreenButton to getLabel(R.string.rotate),
                brightnessButton to getLabel(R.string.brightness),
                autoScrollButton to getLabel(R.string.auto_scroll),
                toggleHorizontalSwipeButton to getLabel(R.string.horizontal_lock),
                toggleZoomLockButton to getLabel(R.string.zoom_lock),
                screenshotButton to getLabel(R.string.screenshot),
                toggleLabelButton to getLabel(R.string.hide_labels)
            )
            if (labelVisibility == VisibilityState.VISIBLE) {
                buttons.keys.forEach { button ->
                    button.text = ""
                    makeButtonCircular(context, button)
                }
                toggleLabelButton.icon = drawableOf(R.drawable.ic_double_arrow_right)
            }
            else {
                buttons.forEach { (button, text) ->
                    button.text = text
                    resetButtonShape(button)
                }
                toggleLabelButton.icon = drawableOf(R.drawable.ic_double_arrow_left)
            }
        }
        labelVisibility = inverseVisibility(labelVisibility)
    }

    private fun makeButtonCircular(context: Context, button: MaterialButton) {
        val scale = context.resources.displayMetrics.density
        //val iconSizeDp = context.resources.getDimension(R.dimen.fs_button_size) / scale
        val iconSizeDp = 24
        val iconSizePx = (iconSizeDp * scale).toInt()

        val circleFactor = 1.9  // 1.5
        val buttonWidthPx = iconSizePx * circleFactor

        val params = button.layoutParams
        params.width = buttonWidthPx.toInt()
        button.layoutParams = params
    }

    private fun resetButtonShape(button: MaterialButton) {
        button.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
    }

    // -------------
    private fun delayAction(action: Runnable) {
        delayHandler.reset()
        delayHandler.postDelayed(action, delay)
    }

    private fun doTemporarily(action: Runnable, undoAction: Runnable) {
        delayHandler.reset()
        action.run()
        delayHandler.postDelayed(undoAction, delay)
    }

    private fun showFullScreenButtons() = changeFullScreenButtonsVisibility(true)

    private fun hideFullScreenButtons() = changeFullScreenButtonsVisibility(false)

    private fun changeFullScreenButtonsVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.fullScreenButtonsLayout.visibility = visibility
    }

    private fun showPageHandle() {
        binding.pdfView.scrollHandle?.customShow()
    }

    private fun hidePageHandle() {
        binding.pdfView.scrollHandle?.customHide()
    }

    private fun showAutoScrollLayout() {
        if (pdf.isFullScreenToggled && pdf.isAutoScrollClicked) {
            binding.autoScrollLayout.visibility = View.VISIBLE
            binding.autoScrollSpeedText.visibility = View.VISIBLE
        }
    }

    private fun hideAutoScrollLayout() {
        if (pdf.isFullScreenToggled && pdf.isAutoScrollClicked) {
            binding.autoScrollLayout.visibility = View.GONE
            binding.autoScrollSpeedText.visibility = View.GONE
        }
    }

    private fun showBrightnessLayout() {
        if (pdf.isFullScreenToggled && pdf.isBrightnessClicked) {
            binding.brightnessLayout.visibility = View.VISIBLE
        }
    }

    private fun hideBrightnessLayout() {
        if (pdf.isFullScreenToggled && pdf.isBrightnessClicked) {
            binding.brightnessLayout.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListenerForAll() {
        viewsList.forEach { it.setOnTouchListener(getOnTouchListener()) }
    }

    private fun Handler.reset() {
        this.removeCallbacksAndMessages(null)
    }

    private fun inverseVisibility(visibility: VisibilityState): VisibilityState {
        return if (visibility == VisibilityState.VISIBLE) VisibilityState.INVISIBLE
        else VisibilityState.VISIBLE
    }

}