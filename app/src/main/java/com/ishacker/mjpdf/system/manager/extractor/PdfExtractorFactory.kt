package com.ishacker.mjpdf.system.manager.extractor

import android.app.Activity
import android.net.Uri
import com.shockwave.pdfium.PdfiumCore
import java.io.IOException

object PdfExtractorFactory {

    @Throws(IOException::class)
    fun create(activity: Activity, uri: Uri, password: String? = null): PdfExtractor {
        val pdfium = PdfiumCore(activity)
        val parcelFileDescriptor = activity.contentResolver.openFileDescriptor(uri, "r")
        val pdfDocument = if (password.isNullOrEmpty()) {
            pdfium.newDocument(parcelFileDescriptor)
        } else {
            pdfium.newDocument(parcelFileDescriptor, password)
        }
        return PdfExtractorImpl(pdfium, pdfDocument)
    }

    @Throws(IOException::class)
    fun create(activity: Activity, pdfBytes: ByteArray, password: String? = null): PdfExtractor {
        val pdfium = PdfiumCore(activity)
        val pdfDocument =  if (password.isNullOrEmpty()) {
            pdfium.newDocument(pdfBytes)
        } else {
            pdfium.newDocument(pdfBytes, password)
        }
        return PdfExtractorImpl(pdfium, pdfDocument)
    }

}