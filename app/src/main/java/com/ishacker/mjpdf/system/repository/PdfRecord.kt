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

package com.ishacker.mjpdf.system.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ishacker.mjpdf.system.data.PDF
import com.ishacker.mjpdf.system.enums.ReadingStatus
import com.ishacker.mjpdf.system.util.getFileName
import java.io.File
import java.time.LocalDateTime

@Entity
data class PdfRecord(
    @PrimaryKey
    val hash: String,

    val pageNumber: Int,

    @ColumnInfo(defaultValue = UNSET_VALUE)
    var uri: Uri,

    @ColumnInfo(defaultValue = UNSET_LENGTH)
    val length: Int,

    @ColumnInfo(defaultValue = UNSET_VALUE)
    val fileName: String,

    val password: String?,

    @ColumnInfo(defaultValue = UNSET_DATE)
    var lastOpened: LocalDateTime,

    @ColumnInfo(defaultValue = UNSET_READING_STATUS)
    var reading: ReadingStatus,

    @ColumnInfo(defaultValue = UNSET_FAVORITE)
    var favorite: Boolean,
) {

    companion object {

        fun from(context: Context, entry: Map.Entry<String, File>): PdfRecord {
            return PdfRecord(
                entry.key,
                0,
                entry.value.toUri(),
                0,
                getFileName(context, entry.value.toUri()),
                null,
                LocalDateTime.now(),
                ReadingStatus.UNSET,
                false
            )
        }

        fun from(fileHash: String, pdf: PDF, password: String? = null): PdfRecord {
            return PdfRecord(
                fileHash,
                pdf.pageNumber,
                pdf.uri ?: throw RuntimeException("No fileUri while create PdfRecord"),
                pdf.length,
                pdf.name.removeSuffix(".pdf"),
                password,
                LocalDateTime.now(),
                ReadingStatus.UNSET,
                false
            )
        }

        const val UNSET_NAME = "Unknown Name"
        const val UNSET_DATE = "-999999999-01-01T00:00" // LocalDateTime.MIN
        const val UNSET_VALUE = ""
        const val UNSET_LENGTH = "-1"
        const val UNSET_PAGE_NUMBER = "0"
        const val UNSET_READING_STATUS = "UNSET"
        const val UNSET_FAVORITE = false.toString()
    }
}