package com.gitlab.mudlej.MjPdfReader.ui.bookmark

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.Bookmark
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.databinding.BookmarksListItemBinding
import com.google.android.material.card.MaterialCardView

class BookmarkViewHolder(
    private val binding: BookmarksListItemBinding,
    private val bookmarkFunctions: BookmarkFunctions,
    private val activity: BookmarksActivity
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(bookmark: Bookmark) {
        binding.root.removeAllViews()
        binding.root.addView(createSubBookmarkLayout(bookmark))
    }

    private fun createSubBookmarkLayout(subBookmark: Bookmark): MaterialCardView {

        val cardView = LayoutInflater.from(activity)
            .inflate(R.layout.children_bookmark_layout, null) as MaterialCardView

        val subBookmarkLayout = cardView[0] as ConstraintLayout
        val subToggleButton = subBookmarkLayout[0] as ImageView
        val subText = subBookmarkLayout[1] as TextView
        val subPageNumber = subBookmarkLayout[2] as TextView
        val subChildrenLayout = subBookmarkLayout[3] as LinearLayoutCompat

        subText.text = subBookmark.title
        subText.textSize = PDF.BOOKMARK_TEXT_SIZE - subBookmark.level * PDF.BOOKMARK_TEXT_SIZE_DEC

        subPageNumber.text = (subBookmark.pageIdx + 1).toString()
        subPageNumber.textSize = PDF.BOOKMARK_TEXT_SIZE - subBookmark.level * PDF.BOOKMARK_TEXT_SIZE_DEC

        subText.setOnClickListener { bookmarkFunctions.onBookmarkClicked(subBookmark) }
        subPageNumber.setOnClickListener { bookmarkFunctions.onBookmarkClicked(subBookmark) }
        subBookmarkLayout.setOnClickListener { bookmarkFunctions.onBookmarkClicked(subBookmark) }

//            if (subBookmark.level != 0)
//                subBookmarkLayout.setBackgroundResource(R.drawable.transparent_background)

        if (subBookmark.hasSubBookmarks()) {
            subChildrenLayout.removeAllViews()
            for (child in subBookmark.subBookmarks) {
                val layout = createSubBookmarkLayout(child)
                subChildrenLayout.addView(layout)
            }

            subToggleButton.setOnClickListener {
                if (subChildrenLayout.isVisible) {
                    subChildrenLayout.visibility = View.GONE
                    subToggleButton.setImageResource(R.drawable.ic_small_arrow_right)
                }
                else {
                    subChildrenLayout.visibility = View.VISIBLE
                    subToggleButton.setImageResource(R.drawable.ic_small_arrow_down)
                }
            }
        }
        else {
            subToggleButton.setImageResource(R.drawable.ic_bullet_point)
        }
        return cardView
    }
}