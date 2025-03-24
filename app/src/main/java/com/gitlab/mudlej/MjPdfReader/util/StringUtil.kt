package com.gitlab.mudlej.MjPdfReader.util

object StringUtil {

    fun String.toTitleCase(): String = split(" ").joinToString(" ") {
        it.lowercase().replaceFirstChar { char -> char.titlecase() }
    }


    fun String.formatEnumToTitle() = this.replace("_", " ").toTitleCase()

    fun String.formatTitleToEnum() = this.replace(" ", "_").uppercase()

}