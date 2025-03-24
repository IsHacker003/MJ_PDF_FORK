package com.gitlab.mudlej.MjPdfReader.ui.search

import com.gitlab.mudlej.MjPdfReader.data.SearchResult

interface SearchResultFunctions {

    fun onSearchResultClicked(searchResult: SearchResult)

    fun onShowMoreResultTextClicked(searchResult: SearchResult, index: Int): SearchResult

}