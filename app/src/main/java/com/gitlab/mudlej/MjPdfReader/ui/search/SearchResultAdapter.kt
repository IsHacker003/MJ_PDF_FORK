package com.gitlab.mudlej.MjPdfReader.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.ListAdapter
import com.gitlab.mudlej.MjPdfReader.data.SearchResult
import com.gitlab.mudlej.MjPdfReader.databinding.SearchResultItemBinding

class SearchResultAdapter(
    private val searchResultFunctions: SearchResultFunctions
) : ListAdapter<SearchResult, SearchResultViewHolder>(SearchResultComparator()) {

    var nestedQuery: String? = null
    var progressBar: ProgressBar? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(parent.context,
            SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            searchResultFunctions,
            searchResultAdapter = this
        )
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, index: Int) {
        getItem(index)?.let {
            it.searchResultIndexInList = index
            holder.bind(it)
        }
    }

    override fun onCurrentListChanged(previousList: MutableList<SearchResult>, currentList: MutableList<SearchResult>) {
        progressBar?.visibility = View.GONE
        super.onCurrentListChanged(previousList, currentList)
    }
}
