package com.gitlab.mudlej.MjPdfReader.ui.home

import androidx.recyclerview.widget.DiffUtil
import com.gitlab.mudlej.MjPdfReader.repository.PdfRecord

class RecordComparator : DiffUtil.ItemCallback<PdfRecord>() {

    override fun areItemsTheSame(oldItem: PdfRecord, newItem: PdfRecord): Boolean
            = oldItem.hashCode() == newItem.hashCode()

    override fun areContentsTheSame(oldItem: PdfRecord, newItem: PdfRecord): Boolean
            = oldItem == newItem
}