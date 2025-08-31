package com.ishacker.mjpdf.system.ui.search

import com.ishacker.mjpdf.system.data.SearchResult

interface SearchResultFunctions {

    fun onSearchResultClicked(searchResult: SearchResult)

    fun onShowMoreResultTextClicked(searchResult: SearchResult, index: Int): SearchResult

}