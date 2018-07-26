package com.github.gradle.android.i18n.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

// Cannot be a `data class` because fields are optional.
class XmlResource() {

    @JacksonXmlProperty(isAttribute = true)
    var name: String? = null

    @JacksonXmlProperty(isAttribute = true)
    var quantity: String? = null

    @JacksonXmlText
    var text: String? = null

    constructor(name: String? = null, quantity: String? = null, text: String? = null) : this() {
        this.name = name
        this.quantity = quantity
        this.text = text
    }
}