package com.gitlab.mudlej.MjPdfReader.manager.extractor

import com.gitlab.mudlej.MjPdfReader.data.Bookmark
import com.gitlab.mudlej.MjPdfReader.data.Link
import com.shockwave.pdfium.PdfDocument

interface PdfExtractor {

    fun getPageText(pageNumber: Int): String

    fun getPageCount(): Int

    fun getPageLinks(pageNumber: Int): List<PdfDocument.Link>

    fun getAllBookmarks(): List<Bookmark>

    fun getAllLinks(): List<Link>
}