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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ishacker.mjpdf.system.enums.ReadingStatus
import java.time.LocalDateTime

@Dao
interface PdfRecordDao {

    @Query("SELECT * FROM PdfRecord")
    fun findAll(): List<PdfRecord>

    @Query("SELECT pageNumber FROM PdfRecord WHERE hash = :fileHash")
    fun findSavedPage(fileHash: String?): Int?

    @Query("SELECT password FROM PdfRecord WHERE hash = :fileHash")
    fun findPdfPassword(fileHash: String?): String?

    @Query("UPDATE PdfRecord SET pageNumber = :page WHERE hash = :fileHash")
    fun updatePageNumber(fileHash: String?, page: Int): Int?

    @Query("UPDATE PdfRecord SET lastOpened = :lastOpened WHERE hash = :fileHash")
    fun updateLastOpened(fileHash: String?, lastOpened: LocalDateTime)

    @Query("UPDATE PdfRecord SET favorite = :favorite WHERE hash = :fileHash")
    fun updateFavorite(fileHash: String?, favorite: Boolean)

    @Query("UPDATE PdfRecord SET reading = :readingStatus WHERE hash = :fileHash")
    fun updateReading(fileHash: String, readingStatus: ReadingStatus)

    @Query("UPDATE PdfRecord SET password = :password WHERE hash = :fileHash")
    fun updatePassword(fileHash: String, password: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(saveLocations: PdfRecord)

    @Query("SELECT EXISTS(SELECT * FROM PdfRecord WHERE hash = :fileHash)")
    fun hasRecord(fileHash: String): Boolean

    @Delete()
    fun delete(record: PdfRecord)
}