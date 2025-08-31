package com.ishacker.mjpdf.system.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ishacker.mjpdf.system.databinding.PdfRecordItemBinding
import com.ishacker.mjpdf.system.repository.PdfRecord


class RecordAdapter(
    private val recordFunctions: RecordFunctions,
    val activity: HomeActivity
) : ListAdapter<PdfRecord, RecordViewHolder>(RecordComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        return RecordViewHolder(
            PdfRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            recordFunctions
        )
    }

    override fun onBindViewHolder(holder: RecordViewHolder, i: Int) {
        getItem(i)?.let { holder.bind(it) }
    }
}