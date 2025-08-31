package com.ishacker.mjpdf.system.ui.bookmark

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ishacker.mjpdf.system.data.Bookmark
import com.ishacker.mjpdf.system.databinding.BookmarksListItemBinding


class BookmarkAdapter(
    private val bookmarkFunctions: BookmarkFunctions,
    val activity: BookmarksActivity
) : ListAdapter<Bookmark, BookmarkViewHolder>(BookmarkComparator()) {

    var shouldExpand = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        return BookmarkViewHolder(
            BookmarksListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            bookmarkFunctions,
            activity
        )
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, i: Int) {
        getItem(i)?.let { holder.bind(it) }
    }

}