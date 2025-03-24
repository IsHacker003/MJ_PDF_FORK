package com.gitlab.mudlej.MjPdfReader.ui.home

import com.gitlab.mudlej.MjPdfReader.repository.PdfRecord

interface RecordFunctions {

    fun onCardClicked(record: PdfRecord)

    fun onAboutClicked(record: PdfRecord)

}