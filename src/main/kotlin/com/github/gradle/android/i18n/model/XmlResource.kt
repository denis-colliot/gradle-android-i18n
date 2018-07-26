package com.github.gradle.android.i18n.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

// Cannot be a `data class` because fields are optional.
class XmlResource {

    @JacksonXmlProperty(isAttribute = true)
    val name: String? = null

    @JacksonXmlProperty(isAttribute = true)
    val quantity: String? = null

    @JacksonXmlText
    val text: String? = null

}