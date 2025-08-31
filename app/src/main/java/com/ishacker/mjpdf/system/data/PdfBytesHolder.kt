package com.ishacker.mjpdf.system.data

object PdfBytesHolder {
    var pdfByte: ByteArray? = null

    fun clear() { pdfByte = null }
}