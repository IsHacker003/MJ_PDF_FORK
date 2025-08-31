package com.ishacker.mjpdf.system.ui.link

import com.ishacker.mjpdf.system.data.Link

interface LinkFunctions {

    fun onLinkClicked(link: Link)

    fun onPageNumberClicked(link: Link)

    fun onCopyLinkClicked(link: Link)

}