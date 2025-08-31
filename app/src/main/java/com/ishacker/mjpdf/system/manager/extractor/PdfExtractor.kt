package com.ishacker.mjpdf.system.manager.extractor

import com.ishacker.mjpdf.system.data.Bookmark
import com.ishacker.mjpdf.system.data.Link
import com.shockwave.pdfium.PdfDocument

interface PdfExtractor {

    fun getPageText(pageNumber: Int): String

    fun getPageCount(): Int

    fun getPageLinks(pageNumber: Int): List<PdfDocument.Link>

    fun getAllBookmarks(): List<Bookmark>

    fun getAllLinks(): List<Link>
}