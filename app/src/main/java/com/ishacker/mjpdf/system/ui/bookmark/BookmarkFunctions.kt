package com.ishacker.mjpdf.system.ui.bookmark

import com.ishacker.mjpdf.system.data.Bookmark

interface BookmarkFunctions {
    fun onBookmarkClicked(bookmark: Bookmark)
}