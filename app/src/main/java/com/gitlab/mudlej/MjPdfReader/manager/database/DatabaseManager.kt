package com.gitlab.mudlej.MjPdfReader.manager.database

import com.gitlab.mudlej.MjPdfReader.enums.ReadingStatus
import com.gitlab.mudlej.MjPdfReader.repository.PdfRecord
import java.time.LocalDateTime

interface DatabaseManager {

    suspend fun findAllRecords(): List<PdfRecord>

    suspend fun saveRecordInBackground(pdfRecord: PdfRecord)

    suspend fun findPageNumber(fileHash: String): Int

    suspend fun findPdfPassword(fileHash: String): String?

    suspend fun setPageNumber(fileHash: String, page: Int)

    suspend fun hasRecord(fileHash: String): Boolean

    suspend fun setLastOpened(fileHash: String, lastOpened: LocalDateTime)

    suspend fun removeRecord(record: PdfRecord)

    suspend fun setFavorite(fileHash: String, favorite: Boolean)

    suspend fun setReading(fileHash: String, readingStatus: ReadingStatus)

    suspend fun setPassword(fileHash: String, password: String)

}