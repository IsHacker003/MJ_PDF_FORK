package com.gitlab.mudlej.MjPdfReader.ui.link

import com.gitlab.mudlej.MjPdfReader.data.Link

interface LinkFunctions {

    fun onLinkClicked(link: Link)

    fun onPageNumberClicked(link: Link)

    fun onCopyLinkClicked(link: Link)

}