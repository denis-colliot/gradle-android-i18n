package com.github.gradle.android.i18n.generator

import java.util.regex.Pattern

object GeneratorHelper {

    const val XML_QUOTE = "\\\\'"

    const val SINGLE_QUOTE = "'"

    const val QUANTITY_SEPARATOR = ":"

    const val XML_SINGLE_ARG = "%s"

    const val ARG_PLACEHOLDER = '#'

    val XML_KEY_ILLEGAL_CHARS = ".*[\\s" + Pattern.quote("+-*/\\;,'()[]{}!?=@|#~&\"^%<>") + "].*"

    fun getXmlIndexedArg(index: Int): String {
        return "%$index\\\$s"
    }
}