package com.ishacker.mjpdf.system.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.isVisible
import com.ishacker.mjpdf.system.databinding.PropertiesDialogBinding
import com.ishacker.mjpdf.system.util.convertDateString
import com.ishacker.mjpdf.system.util.sizeInMb
import com.shockwave.pdfium.PdfDocument.Meta
import java.io.File
import java.util.Locale

class PropertiesDialog(private val context: Context, private val meta: Meta, private val file: File?)
    : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PropertiesDialogBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setText(fileNameRow, fileNameText, file?.name)
            setText(titleRow, titleText, meta.title)
            setText(authorRow, authorText, meta.author)
            setText(pagesRow, pagesText, String.format(Locale.getDefault(), "%d", meta.totalPages))
            setText(subjectRow, subjectText, meta.subject)
            setText(keywordsRow, keywordsText, meta.keywords)
            setText(createdRow, createdText, convertDateString(meta.creationDate) ?: meta.creationDate)
            setText(modifiedRow, modifiedText, convertDateString(meta.modDate?: meta.modDate))
            setText(creatorRow, creatorText, meta.creator)
            setText(producedByRow, producedByText, meta.producer)
            setText(fileSizeRow, fileSizeText, if (file?.sizeInMb == null) "--" else String.format(Locale.US, "%.2f MB", file.sizeInMb))
            //setText(locationText, file?.path)
            okButton.setOnClickListener { dismiss() }
        }
    }

    override fun onStart() {
        super.onStart()
        val window = window
        if (window != null) {
            // Without this this width of the dialog will be smaller than needed
            window.setLayout((context.resources.displayMetrics.widthPixels), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    private fun setText(row: TableRow, textView: TextView, text: String?) {
        if (text.isNullOrBlank()) {
            row.isVisible = false
        }
        else {
            textView.text = text
        }
    }
}