package com.github.gradle.android.i18n.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * Android XML values. Can represent either:
 *
 * - A `resources` element in a `strings.xml` file eg.
 *
 *     ```
 *     <resources>
 *         <string name="name1">value1</string>
 *         <string name="name2">value2</string>
 *     </resources>
 *     ```
 *
 *  - A `plurals` element in a `resources` element in a `strings.xml` file eg.
 *
 *     ```
 *     <resources>
 *         <plurals name="plurals1">
 *             <item quantity="one">value1</item>
 *             <item quantity="other">value2</item>
 *         </plurals>
 *     </resources>
 *     ```
 */
data class XmlResources(

        @JacksonXmlProperty(isAttribute = true)
        val name: String,

        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        val items: MutableList<XmlResource>
)