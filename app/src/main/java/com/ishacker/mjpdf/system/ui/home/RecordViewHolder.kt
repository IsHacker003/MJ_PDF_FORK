package com.ishacker.mjpdf.system.ui.home

import androidx.recyclerview.widget.RecyclerView
import com.ishacker.mjpdf.system.databinding.PdfRecordItemBinding
import com.ishacker.mjpdf.system.repository.PdfRecord

class RecordViewHolder(
    private val binding: PdfRecordItemBinding,
    private val recordFunctions: RecordFunctions
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(record: PdfRecord) {
        binding.title.text = record.fileName

        binding.root.setOnClickListener {
            recordFunctions.onCardClicked(record)
        }

        binding.about.setOnClickListener {
            recordFunctions.onAboutClicked(record)
        }
    }
}