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

package com.ishacker.mjpdf.system.data

import android.net.Uri

class PDF(
    var uri: Uri? = null,
    var name: String = "",
    var password: String? = null,
    var pageNumber: Int = 0,
    var length: Int = 0,
    var sizeInMb: Double = 0.0,
    var zoom: Float = 1F,
    var isPortrait: Boolean = true,
    var isFullScreenToggled: Boolean = false,
    var isBrightnessClicked: Boolean = false,
    var isAutoScrollClicked: Boolean = false,
    var isAutoScrolling: Boolean = false,
    var fileHash: String? = null,
    val text: MutableMap<Int, String> = mutableMapOf(),
    var isExtractingTextFinished: Boolean = false,
    var lastQuery: String? = null,
) {

    companion object {


        // constants
        const val FILE_TYPE = "application/pdf"
        const val HASH_SIZE = 1024 * 1024
        const val BOOKMARK_TEXT_SIZE = 24F
        const val BOOKMARK_TEXT_SIZE_DEC = 2F
        const val BOOKMARK_RESULT_OK = 48645
        const val SEARCH_RESULT_OK = 48632
        const val LINK_RESULT_OK = 48032
        const val SCREENSHOT_IMAGE_QUALITY = 100
        const val SEARCH_RESULT_OFFSET = 40
        const val ADDITIONAL_SEARCH_RESULT_OFFSET = 100
        const val TOO_MANY_RESULTS = 3500
        const val RESET_NUMBER = -1
        const val MIN_SEARCH_QUERY = 3

        // keys
        const val nameKey = "name"
        const val passwordKey = "password"
        const val pageNumberKey = "pageNumber"
        const val lengthKey = "length"
        const val uriKey = "uri"
        const val zoomKey = "zoom"
        const val isPortraitKey = "isPortrait"
        const val isFullScreenToggledKey = "isFullScreenToggled"
        const val isExtractingTextFinishedKey = "isExtractingTextFinished"
        const val pdfBookmarksKey = "PDF_BOOKMARKS"
        const val chosenBookmarkKey = "chosenBookmarkKey"
        const val searchResultPageNumberKey = "searchResultPageNumberKey"
        const val fileHashKey = "fileHashKey"
        const val searchResultKey = "searchInput"
        const val linkResultKey = "linkResult"
        const val startBookmarksActivity = 84418
        const val startSearchActivity = 91234
        const val startTextActivity =70134
        const val startLinksActivity = 54217
        const val searchQueryKey = "searchQuery"
        const val resultPositionInListKey = "searchResultPositionKey"
        const val filePathKey = "filePathKey"
    }

    fun getTitleWithPageNumber(): String {
        return "${getPageCounterText()} ${getTitle()}";
    }

    fun getTitle(): String {
        // get .pdf start index (the dot)
        val extensionIndex: Int = if (name.lastIndexOf('.') == -1) name.length else name.lastIndexOf('.')
        return name.substring(0, extensionIndex)
    }
    fun getPageCounterText(): String {
        return String.format("[%s/%s]", pageNumber + 1, length)
    }

    fun togglePortrait() { isPortrait = !isPortrait }

    fun setPageCount(count: Int) {
        if (count == length || count < 1) return
        length = count
    }

    fun hasFile() = uri != null

    fun resetLength() {
        length = RESET_NUMBER
    }

    fun initPdfLength(pageCount: Int) {
        if (length == RESET_NUMBER) {
            length = pageCount
        }
    }
}