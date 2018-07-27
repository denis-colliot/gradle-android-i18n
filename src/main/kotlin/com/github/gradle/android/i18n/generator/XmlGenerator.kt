package com.github.gradle.android.i18n.generator

import com.github.gradle.android.i18n.conf.Configuration
import com.github.gradle.android.i18n.generator.GeneratorHelper.ARG_PLACEHOLDER
import com.github.gradle.android.i18n.generator.GeneratorHelper.QUANTITY_SEPARATOR
import com.github.gradle.android.i18n.generator.GeneratorHelper.SINGLE_QUOTE
import com.github.gradle.android.i18n.generator.GeneratorHelper.XML_KEY_ILLEGAL_CHARS
import com.github.gradle.android.i18n.generator.GeneratorHelper.XML_QUOTE
import com.github.gradle.android.i18n.generator.GeneratorHelper.XML_SINGLE_ARG
import com.github.gradle.android.i18n.generator.GeneratorHelper.getXmlIndexedArg
import com.github.gradle.android.i18n.model.StringResources
import com.github.gradle.android.i18n.model.XmlResource
import com.github.gradle.android.i18n.model.XmlResources
import org.gradle.api.Project
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

abstract class XmlGenerator(private val project: Project) {

    private val mapper = Configuration.xmlMapper()
    private val logger = LoggerFactory.getLogger(XmlGenerator::class.java)

    /**
     * Generates `xml` android resources from given source input stream.
     */
    abstract fun generate(inputStream: InputStream)

    /**
     * Adds the given translation to the current XML resource.
     *
     * This methods automatically alters the given `translation` if necessary:
     * - escapes `translation`'s quotes (`l'avion -> l\'avion`).
     * - replaces `translation`'s parameters characters (`# -> %1$s`).
     *
     * @throws IllegalArgumentException If one of the arguments is empty, or if `key` contains space(s).
     */
    @Throws(IllegalArgumentException::class)
    protected fun add(stringResources: StringResources, key: String?, translation: String?) {

        // Validating arguments.
        if (key.isNullOrBlank() || translation.isNullOrBlank()) {
            throw IllegalArgumentException("Invalid translation key '" + key + "' or corresponding translation " +
                    "value '" + translation + "'")
        }

        key?.trim()?.let { cleanKey ->
            if (XML_KEY_ILLEGAL_CHARS.toRegex().containsMatchIn(cleanKey)) {
                throw IllegalArgumentException("Invalid translation key '$cleanKey'")
            }

            translation?.let {
                addNullSafe(stringResources, cleanKey, translation)
            }
        }
    }

    private fun addNullSafe(stringResources: StringResources, key: String, translation: String) {

        // Escaping simple quotes.
        var mutableTranslation = translation.replace(SINGLE_QUOTE.toRegex(), XML_QUOTE)

        // Handling translation parameters.
        val sharpCount = mutableTranslation.toCharArray().filter { it == ARG_PLACEHOLDER }.count()

        if (sharpCount == 1) {
            mutableTranslation = mutableTranslation.replace("$ARG_PLACEHOLDER".toRegex(), XML_SINGLE_ARG)
        } else if (sharpCount > 1) {
            for (index in 1..sharpCount) {
                mutableTranslation = mutableTranslation.replaceFirst("$ARG_PLACEHOLDER".toRegex(), getXmlIndexedArg(index))
            }
        }

        if (key.contains(QUANTITY_SEPARATOR)) {
            val split = key.split(QUANTITY_SEPARATOR.toRegex())
            val realKey = split[0]
            val quantity = split[1]

            val index = stringResources.plurals.indexOfFirst { it.name == realKey }

            val xmlListResource: XmlResources
            if (index == -1) {
                xmlListResource = XmlResources(realKey, mutableListOf())
                stringResources.plurals.add(xmlListResource)
            } else {
                xmlListResource = stringResources.plurals[index]
            }

            xmlListResource.items.add(XmlResource(quantity = quantity, text = mutableTranslation))

        } else {
            stringResources.strings.add(XmlResource(name = key, text = mutableTranslation))
        }
    }

    protected fun writeOutput(outputFile: File, translations: StringResources) {

        logger.info("Writing to output file '{}'", outputFile)

        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        mapper.writeValue(outputFile, translations)
    }

    protected fun androidStringsResFile(locale: String = ""): File {
        val localeSuffix = if (locale.isNotBlank()) {
            "-${locale.trim()}"
        } else {
            ""
        }
        val projectDir = project.projectDir.absolutePath
        return Paths.get(projectDir, "src", "main", "res", "values$localeSuffix", "strings.xml").toFile()
    }
}