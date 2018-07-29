package com.github.gradle.android.i18n.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * Android resources structure.
 */
@JacksonXmlRootElement(localName = "resources")
data class StringResources(

        /**
         * Resource locale (`en`, `it`, `ja`, `b+es+ES`, etc.).
         */
        @JsonIgnore
        var locale: String = "",

        /**
         * Is this resource locale the default locale?
         */
        @JsonIgnore
        var defaultLocale: Boolean = false,

        /**
         * The resource `string` items.
         */
        @JacksonXmlProperty(localName = "string")
        @JacksonXmlElementWrapper(useWrapping = false)
        val strings: MutableList<XmlResource> = mutableListOf(),

        /**
         * The resource `plurals` item.
         */
        @JacksonXmlProperty(localName = "plurals")
        @JacksonXmlElementWrapper(useWrapping = false)
        val plurals: MutableList<XmlResources> = mutableListOf()
)