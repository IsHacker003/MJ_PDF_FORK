package com.ishacker.mjpdf.system.ui.link

import androidx.recyclerview.widget.DiffUtil
import com.ishacker.mjpdf.system.data.Link

class LinkComparator : DiffUtil.ItemCallback<Link>() {
    override fun areItemsTheSame(oldItem: Link, newItem: Link): Boolean
            = oldItem.hashCode() == newItem.hashCode()

    override fun areContentsTheSame(oldItem: Link, newItem: Link): Boolean
            = oldItem == newItem
}