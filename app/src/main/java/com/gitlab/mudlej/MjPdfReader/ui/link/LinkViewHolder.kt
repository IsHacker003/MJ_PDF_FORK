package com.gitlab.mudlej.MjPdfReader.ui.link

import androidx.recyclerview.widget.RecyclerView
import com.gitlab.mudlej.MjPdfReader.data.Link
import com.gitlab.mudlej.MjPdfReader.databinding.LinkItemBinding

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