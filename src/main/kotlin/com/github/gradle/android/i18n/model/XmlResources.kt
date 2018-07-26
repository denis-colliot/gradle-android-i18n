package com.github.gradle.android.i18n.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class XmlResources(

        @JacksonXmlProperty(isAttribute = true)
        val name: String,

        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        val items: MutableList<XmlResource>
)