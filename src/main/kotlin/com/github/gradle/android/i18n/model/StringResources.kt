package com.github.gradle.android.i18n.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "resources")
data class StringResources(

        @JsonIgnore
        val locale: String? = null,

        @JacksonXmlProperty(localName = "string")
        @JacksonXmlElementWrapper(useWrapping = false)
        val strings: MutableList<XmlResource> = mutableListOf(),

        @JacksonXmlProperty(localName = "plurals")
        @JacksonXmlElementWrapper(useWrapping = false)
        val plurals: MutableList<XmlResources> = mutableListOf()
)