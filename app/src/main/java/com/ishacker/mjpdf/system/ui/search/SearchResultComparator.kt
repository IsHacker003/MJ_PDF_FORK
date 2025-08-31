package com.ishacker.mjpdf.system.ui.search

import androidx.recyclerview.widget.DiffUtil
import com.ishacker.mjpdf.system.data.SearchResult

class SearchResultComparator : DiffUtil.ItemCallback<SearchResult>() {
    override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult) =
        oldItem.hashCode() == newItem.hashCode()

    override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult) =
        oldItem == newItem
}
