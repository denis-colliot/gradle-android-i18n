package com.github.gradle.android.i18n.conf

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jcifs.Config

/**
 * Project global configuration.
 */
object Configuration {

    /**
     * [XmlMapper] singleton.
     */
    val xmlMapper: XmlMapper

    init {
        // XmlMapper initialization.
        xmlMapper = xmlMapper()

        // JCifs configuration.
        Config.setProperty("jcifs.util.loglevel", "1")
        Config.setProperty("jcifs.smb.client.responseTimeout", "5000")
        Config.setProperty("jcifs.smb.client.dfs.disabled", "true")
    }

    private fun xmlMapper(): XmlMapper {
        return XmlMapper().apply {
            registerKotlinModule()

            val indenter = Lf4SpacesIndenter()
            val printer = DefaultXmlPrettyPrinter()
            printer.indentObjectsWith(indenter)
            printer.indentArraysWith(indenter)

            setDefaultPrettyPrinter(printer)
            configure(SerializationFeature.INDENT_OUTPUT, true)
            configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        }
    }
}