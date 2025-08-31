/*
 *   MJ PDF
 *   Copyright (C) 2023 Mudlej
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *  --------------------------
 *  This code was previously licensed under
 *
 *  MIT License
 *
 *  Copyright (c) 2018 Gokul Swaminathan
 *  Copyright (c) 2023 Mudlej
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.ishacker.mjpdf.system.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.github.barteksc.pdfviewer.PDFView
import com.ishacker.mjpdf.system.BuildConfig
import com.ishacker.mjpdf.system.R
import com.ishacker.mjpdf.system.data.PDF
import com.ishacker.mjpdf.system.data.Preferences
import com.ishacker.mjpdf.system.databinding.ActivityMainBinding
import com.ishacker.mjpdf.system.databinding.PasswordDialogBinding
import com.ishacker.mjpdf.system.ui.dialog.PropertiesDialog
import com.ishacker.mjpdf.system.ui.main.MainActivity
import com.ishacker.mjpdf.system.ui.search.SearchActivity
import com.ishacker.mjpdf.system.ui.text_mode.TextModeActivity
import com.ishacker.mjpdf.system.util.copyToClipboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.shockwave.pdfium.PdfDocument
import java.io.File

private const val TAG = "Dialogs"

fun showAppFeaturesDialog(context: Context) {
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle("${context.resources.getString(R.string.mj_app_name)} ${BuildConfig.VERSION_NAME}")
        .setMessage(context.resources.getString(R.string.what_is_new))
        .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        .create()

    try {
        dialog.show()
    }
    catch (e: Throwable) {
        Log.e(TAG, "showAppFeaturesDialog: Error showing the dialog.(${e.message})")
    }
}

fun showMetaDialog(context: Context, meta: PdfDocument.Meta?, file: File?) {
    if (meta == null) {
        Toast.makeText(context, "Cannot read PDF's meta data!", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val dialog = PropertiesDialog(context, meta, file)
        val dialogWindow = dialog.window

        if (dialogWindow != null) {
            val displayMetrics = context.resources.displayMetrics
            val width = displayMetrics.widthPixels
            // Set the dialog width to a certain percentage of the screen width
            dialogWindow.setLayout((width * 0.5).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            dialogWindow.setGravity(Gravity.CENTER)
        }

        dialogWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    catch (throwable: Throwable) {
        Log.e(TAG, "showMetaDialog: Failed to show File Properties Dialog", throwable)
        Toast.makeText(context, "Failed to show file properties", Toast.LENGTH_SHORT).show()
    }
}

fun showHowToExitFullscreenDialog(context: Context, pref: Preferences) {
    MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.exit_fullscreen_title))
        .setMessage(context.getString(R.string.exit_fullscreen_message))
        .setPositiveButton(context.getString(R.string.exit_fullscreen_positive)) { _, _ ->
            pref.setShowFeaturesDialog(false)
        }
        .setNegativeButton(context.getString(R.string.ok)) {
                dialog: DialogInterface, _ -> dialog.dismiss()
        }
        .create()
        .show()
}

fun showAskForPasswordDialog(
    context: Context,
    pdf: PDF,
    dialogBinding: PasswordDialogBinding,
    displayFunc: (Uri?, Boolean) -> Unit
) {
    val alert = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.protected_pdf)
        .setView(dialogBinding.root)
        .setIcon(R.drawable.lock_icon)
        .setPositiveButton(R.string.ok) { _, _ ->
            pdf.password = dialogBinding.passwordInput.text.toString()
            displayFunc(pdf.uri, dialogBinding.savePassword.isChecked)
        }
        .create()

    alert.setCanceledOnTouchOutside(false)
    alert.show()
}

fun showPartSizeDialog(activity: MainActivity, pref: Preferences) {
    // min values for the seekbars or sliders
    val minPartSize = Preferences.minPartSize
    val minMaxZoom = Preferences.minMaxZoom

    // create dialog layout
    val builder = MaterialAlertDialogBuilder(activity)
    builder.setTitle(R.string.advanced)
    val inflater = activity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout: View = inflater.inflate(R.layout.advanced_dialog,
        activity.findViewById(R.id.partSizeSeekbar))
    builder.setView(layout)

    // set partSize TextView and Seekbar
    val partSizeText = layout.findViewById(R.id.partSizeText) as TextView
    partSizeText.text = pref.getPartSize().toInt().toString()

    val partSizeBar = layout.findViewById(R.id.partSizeSeekbar) as SeekBar
    partSizeBar.max = Preferences.maxPartSize.toInt()
    partSizeBar.progress = pref.getPartSize().toInt()
    partSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            partSizeText.text = if(p1 > minPartSize) p1.toString() else minPartSize.toString()
        }
        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    })

    // set maxZoom TextView and Seekbar
    val maxZoomText = layout.findViewById(R.id.maxZoomText) as TextView
    maxZoomText.text = pref.getMaxZoom().toInt().toString()

    val maxZoomBar = layout.findViewById(R.id.maxZoomSeekbar) as SeekBar
    maxZoomBar.max = Preferences.maxMaxZoom.toInt()
    maxZoomBar.progress = pref.getMaxZoom().toInt()
    maxZoomBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            maxZoomText.text = if(p1 > minMaxZoom) p1.toString() else minMaxZoom.toString()
        }
        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    })

    builder.setPositiveButton(R.string.apply, null)
    builder.setNeutralButton(R.string.reset, null)
    builder.setNegativeButton(R.string.cancel, null)

    val dialog = builder.create()
    dialog.show()
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        pref.setPartSize(partSizeText.text.toString().toFloat())
        pref.setMaxZoom(maxZoomText.text.toString().toFloat())
        activity.recreate()
    }
    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
        pref.setPartSize(Preferences.partSizeDefault)
        pref.setMaxZoom(Preferences.maxZoomDefault)
        activity.recreate()
    }
}

fun showBookmarksDialog(activity: MainActivity, pdfView: PDFView) {
    // get bookmarks or set an appropriate message for the user
    var bookmarks = pdfView.tableOfContents.map { "${it.title} - P${it.pageIdx + 1}" }

    if (bookmarks.isEmpty()) bookmarks = listOf(activity.getString(R.string.no_bookmarks))

    // create and show the bookmarks dialog
    MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.bookmarks))
        .setItems(bookmarks.toTypedArray()) { dialog, which ->
            if (pdfView.tableOfContents.isEmpty()) return@setItems

            val page = pdfView.tableOfContents[which].pageIdx
            pdfView.jumpTo(page.toInt())
            dialog.dismiss()
        }
        .show()
}

fun showCopyPageTextDialog(
    activity: MainActivity,
    binding: ActivityMainBinding,
    pageNumber: Int,
    pageText: String,
    pref: Preferences,
    bypass: Boolean = false
) {
    if (!bypass && !pref.getCopyTextDialog()) {
        return
    }

    // create a custom view to make the text selectable
    val pageTextView = TextView(activity)
    pageTextView.setPadding(30, 20, 30, 0)
    pageTextView.setTextIsSelectable(true)
    pageTextView.textSize = 18f
    pageTextView.text = pageText

    val scrollView = ScrollView(activity)
    scrollView.addView(pageTextView)
    //scrollView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
    //scrollView.scrollBarSize = 2

    MaterialAlertDialogBuilder(activity)
        .setView(scrollView)
        .setTitle("${activity.getString(R.string.selectable_text)} #${pageNumber + 1}")
        .setNegativeButton(activity.getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
        .setPositiveButton(activity.getString(R.string.copy_all)) { dialog, _ ->
            val copyLabel = "${activity.getString(R.string.page)} #${pageNumber} Text"
            copyToClipboard(activity, copyLabel, pageText)

            // show message to user before closing
            //Toast.makeText(activity, activity.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            Snackbar.make(binding.root, activity.getString(R.string.copied_to_clipboard), Snackbar.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        .also {
            // don't show this button if the click came from the action bar
            if (!bypass) {
                it.setNeutralButton(activity.getString(R.string.dont_pop_up)) { dialog, _ ->
                    pref.setCopyTextDialog(false)
                    dialog.dismiss()
                }
            }
        }
        .show()
}

fun showUnderDevelopmentDialog(activity: TextModeActivity) {
    MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.this_is_experimental))
        .setMessage(activity.getString(R.string.this_is_experimental_message))
        .setPositiveButton(activity.getString(R.string.ok)) { dialog, _ -> dialog.dismiss()}
        .setNegativeButton(activity.getString(R.string.go_back)) { dialog, _ ->
            dialog.dismiss(); activity.finish()
        }
        .show()
}


fun showSearchDialog(activity: Activity, pdf: PDF) {
    val searchLayout = LayoutInflater.from(activity).inflate(R.layout.input_layout, null) as TextInputLayout
    MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.search))
        .setMessage(activity.getString(R.string.search_dialog_message))
        .setView(searchLayout)
        .setPositiveButton(activity.getText(R.string.search)) { searchDialog, _ ->
            val query = searchLayout.editText?.text ?: return@setPositiveButton
            fun startSearchActivity() {
                Intent(activity, SearchActivity::class.java).also { searchIntent ->
                    searchIntent.putExtra(PDF.filePathKey, pdf.uri.toString())
                    searchIntent.putExtra(PDF.searchQueryKey, query.toString())
                    activity.startActivityForResult(searchIntent, PDF.startSearchActivity)
                }
            }
            if (query.isBlank() || query.length < PDF.MIN_SEARCH_QUERY) {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.too_short_query))
                    .setMessage(activity.getString(R.string.too_short_query_message).format(query))
                    .setNeutralButton(activity.getString(R.string.proceed_anyway)) { _, _ ->
                        startSearchActivity()
                    }
                    .setPositiveButton(activity.getText(R.string.ok)) { badQueryDialog, _ ->
                        searchDialog.dismiss()
                        badQueryDialog.dismiss()
                        showSearchDialog(activity, pdf)
                    }
                    .show()
            }
            else {
                startSearchActivity()
            }
        }
        .setNegativeButton(activity.getText(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun showGoToPageDialog(
    activity: Activity,
    view: View,
    pageIndex: Int,
    pdfLength: Int,
    goToPageFunc: (Int) -> Unit
) {
    // create EditText for input
    val inputLayout = LayoutInflater
        .from(activity)
        .inflate(R.layout.only_integers_input_layout, null) as TextInputLayout

    inputLayout.hint = "Current page ${pageIndex + 1}/$pdfLength"

    MaterialAlertDialogBuilder(activity)
        .setTitle(activity.getString(R.string.go_to_page))
        .setView(inputLayout)
        .setPositiveButton(activity.getString(R.string.go_to)) { dialog, _ ->
            val query = inputLayout.editText?.text.toString().lowercase().trim()

            // check if the user provided input
            if (query.isEmpty()) {
                //Toast.makeText(activity, activity.getString(R.string.no_input), Toast.LENGTH_SHORT).show()
                Snackbar.make(view, activity.getString(R.string.no_input), Snackbar.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (query.isDigitsOnly())
                goToPageFunc(query.toInt() - 1)

            dialog.dismiss()
        }
        .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        .show()
}