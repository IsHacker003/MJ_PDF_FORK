package com.ishacker.mjpdf.system.ui.home

import com.ishacker.mjpdf.system.repository.PdfRecord

interface RecordFunctions {

    fun onCardClicked(record: PdfRecord)

    fun onAboutClicked(record: PdfRecord)

}