package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.conf.Configuration.xmlMapper
import com.github.gradle.android.i18n.model.StringResources
import com.github.gradle.android.i18n.model.XmlResource
import com.github.gradle.android.i18n.model.XmlResources
import org.gradle.api.Project
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

/**
 * Abstract android i18n resources importer.
 *
 * Provides methods to generate `values[-XX]/strings.xml` files from input data.
 */
abstract class AbstractImporter(private val project: Project) {

    private companion object {

        private const val XML_QUOTE = "\\\\'"

        private const val SINGLE_QUOTE = "'"

        private const val QUANTITY_SEPARATOR = ":"

        private const val XML_SINGLE_ARG = "%s"

        private const val ARG_PLACEHOLDER = '#'

        private val XML_KEY_ILLEGAL_CHARS = ".*[\\s${Regex.escape("+-*/\\;,'()[]{}!?=@|#~&\"^%<>")}].*".toRegex()

        private fun getXmlIndexedArg(index: Int): String {
            return "%$index\\\$s"
        }
    }

    /**
     * Generates the `xml` android string resources file from given source input stream.
     */
    abstract fun generate(inputStream: InputStream, defaultLocale: String)

    /**
     * Adds the given translation to the given string resources.
     *
     * This methods automatically alters the given `translation` if necessary:
     * - escapes `translation`'s quotes (`l'avion -> l\'avion`).
     * - replaces `translation`'s parameters characters (`# -> %1$s`).
     *
     * @param stringResources The resources.
     * @param key The key.
     * @param translation The corresponding translation value.
     * @throws IllegalArgumentException If one of the arguments is empty, or if `key` contains illegal char(s).
     */
    @Throws(IllegalArgumentException::class)
    protected fun add(stringResources: StringResources, key: String?, translation: String?) {

        // Validating arguments.
        if (key.isNullOrBlank() || translation.isNullOrBlank()) {
            throw IllegalArgumentException("Invalid translation key `$key` or corresponding translation value `$translation`")
        }

        key?.trim()?.let { cleanKey ->
            if (XML_KEY_ILLEGAL_CHARS.containsMatchIn(cleanKey)) {
                throw IllegalArgumentException("Invalid translation key `$cleanKey`")
            }

            translation?.let {
                addNullSafe(stringResources, cleanKey, it)
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
            val (realKey, quantity) = key.split(QUANTITY_SEPARATOR.toRegex())
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

    /**
     * Writes the given translation resources to the corresponding output file.
     *
     * @param translations The translation resources.
     */
    protected fun writeOutput(translations: StringResources) {

        val outputFile = androidStringsResFile(translations)

        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        translations.strings.sortBy { it.name }
        translations.plurals.sortBy { it.name }

        xmlMapper.writeValue(outputFile, translations)
    }

    private fun androidStringsResFile(stringResources: StringResources): File {
        val localeSuffix = if (stringResources.defaultLocale) {
            ""
        } else {
            "-${stringResources.locale}"
        }
        val projectDir = project.projectDir.absolutePath
        return Paths.get(projectDir, "src", "main", "res", "values$localeSuffix", "strings.xml").toFile()
    }
}