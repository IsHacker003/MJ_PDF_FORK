package com.gitlab.mudlej.MjPdfReader.data

import com.shockwave.pdfium.PdfDocument

class Bookmark(bookmark:PdfDocument.Bookmark, val level: Int) : PdfDocument.Bookmark() {
    val subBookmarks: MutableList<Bookmark> = mutableListOf()

    init {
        title = bookmark.title
        pageIdx = bookmark.pageIdx
        mNativePtr = bookmark.mNativePtr
        children = bookmark.children

        // add all children recursively
        if (hasChildren())
            for (child in children)
                subBookmarks.add(Bookmark(child, level + 1))
    }

    fun hasSubBookmarks() = subBookmarks.isNotEmpty()
}