package com.gitlab.mudlej.MjPdfReader.ui.search

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.SearchResult
import com.gitlab.mudlej.MjPdfReader.databinding.SearchResultItemBinding
import com.gitlab.mudlej.MjPdfReader.util.indexesOf

class SearchResultViewHolder(
    private val context: Context,
    private val binding: SearchResultItemBinding,
    private val searchResultFunctions: SearchResultFunctions,
    private val searchResultAdapter: SearchResultAdapter
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(searchResult: SearchResult) {
        val text = stylizeText(searchResult)
        binding.apply {
            resultText.setText(text, TextView.BufferType.SPANNABLE)
            resultPageNumber.text = "PAGE\n${searchResult.pageNumber}"

            // show more text
            if (!searchResult.expanded) {
                showMoreButton.visibility = View.VISIBLE
                showMoreButton.setOnClickListener {
                    searchResultFunctions.onShowMoreResultTextClicked(
                        searchResult, searchResultAdapter.currentList.indexOf(searchResult)
                    )
                }
            }
            else {
                resultText.maxLines = Int.MAX_VALUE
                showMoreButton.visibility = View.GONE
            }

            // got to page
            root.setOnClickListener {
                searchResultFunctions.onSearchResultClicked(searchResult)
            }
        }
    }

    private fun stylizeText(searchResult: SearchResult): Spannable {
        val color = ContextCompat.getColor(context, R.color.search)
        val spannable = SpannableString(searchResult.text)

        // stylize nested query result
        searchResultAdapter.nestedQuery?.let { query ->
            if (query.isEmpty() || query.isBlank() || query.length < 3) {
                return@let
            } else {
                spannable.removeSpan(StyleSpan(Typeface.BOLD))
                spannable.removeSpan(UnderlineSpan())
            }

            val indexes = searchResult.text.indexesOf(query, ignoreCase = true)
            for (index in indexes) {
                if (index == searchResult.inputStart) {
                    continue // skip the main query string
                }

                spannable.setSpan(
                    UnderlineSpan(),
                    index,
                    index + query.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    index,
                    index + query.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // stylize the main query input
        spannable.setSpan(
            ForegroundColorSpan(color),
            searchResult.inputStart,
            searchResult.inputEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            searchResult.inputStart,
            searchResult.inputEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }
}