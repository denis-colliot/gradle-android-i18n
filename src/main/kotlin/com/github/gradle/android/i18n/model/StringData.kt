package com.github.gradle.android.i18n.model

/**
 * This class represents the translation of a label to a text in one language.
 * It corresponds to an `string` element in a `strings.xml` file.
 * @param name the name (aka key) of the label
 * @param text the value of the label
 */
data class StringData(val name: String?, val text: String?)
