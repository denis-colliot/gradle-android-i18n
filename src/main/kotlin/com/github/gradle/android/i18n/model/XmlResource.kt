package com.github.gradle.android.i18n.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

/**
 * Android XML value entry. Can represent either:
 * - A `string` element in a `strings.xml` file eg. `<string name="name1">Value 1</string>`
 * - An `item` element in a `plurals` element in a `strings.xml` file eg. `<item name="item-name1">Item value 1</item>`
 */
// Cannot be a `data class` because fields are optional.
class XmlResource() {

    @JacksonXmlProperty(isAttribute = true)
    var name: String? = null

    @JacksonXmlProperty(isAttribute = true)
    var quantity: String? = null

    @JacksonXmlText
    var text: String? = null

    @JacksonXmlProperty(isAttribute = true)
    var translatable: Boolean? = null

    constructor(name: String? = null, quantity: String? = null, text: String? = null, translatable: Boolean? = null) : this() {
        this.name = name
        this.quantity = quantity
        this.text = text
        this.translatable = translatable
    }
}