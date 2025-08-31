package com.ishacker.mjpdf.system.ui.link

import androidx.recyclerview.widget.RecyclerView
import com.ishacker.mjpdf.system.data.Link
import com.ishacker.mjpdf.system.databinding.LinkItemBinding

class LinkViewHolder(
    private val binding: LinkItemBinding,
    private val linkFunctions: LinkFunctions,
    private val activity: LinksActivity
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(link: Link) {
        //binding.linkText.text = link.text     // not extracted yet
        binding.linkUri.text = link.url
        binding.linkPageNumber.text = link.pageNumber.toString()

        binding.linkTextsLayout.setOnClickListener {
            linkFunctions.onLinkClicked(link)
        }
        binding.linkPageNumber.setOnClickListener {
            linkFunctions.onPageNumberClicked(link)
        }
        binding.copyButton.setOnClickListener {
            linkFunctions.onCopyLinkClicked(link)
        }
    }
}