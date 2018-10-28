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
     * Generates the `xml` android string resources file(s) from given source input stream.
     *
     * @param inputStream The source input stream.
     * @param defaultLocale The default locale.
     */
    fun generate(inputStream: InputStream, config: ImportConfig) {
        generate(inputStream, config, ImportHandler(project, config.defaultLocale))
    }

    /**
     * Generates the `xml` android string resources file(s) from given source input stream.
     *
     * @param inputStream The source input stream.
     * @param config The import task configuration.
     * @param handler The import handler providing necessary methods to complete import task.
     */
    protected abstract fun generate(inputStream: InputStream, config: ImportConfig, handler: ImportHandler)

    /**
     * Import handler providing necessary methods to complete import task.
     *
     * @param project The project.
     * @param defaultLocale The default locale.
     */
    protected class ImportHandler(private val project: Project, private val defaultLocale: String) {

        /**
         * Keys set facilitating duplicated key control.
         */
        private val keys = mutableSetOf<String>()

        /**
         * Map storing each registered locale to its corresponding [StringResources] instance.
         */
        private val stringResources = mutableMapOf<String, StringResources>()

        /**
         * Registers a new locale with the given value.
         *
         * @param locale The new locale to register.
         * @throws IllegalArgumentException If the given locale is invalid (`null` or `blank`).
         */
        @Throws(IllegalArgumentException::class)
        fun addLocale(locale: String?) {
            if (locale.isNullOrBlank()) {
                throw IllegalArgumentException("Invalid locale value: `$locale`")
            }
            with(locale!!.trim()) {
                stringResources[this] = StringResources(this, this == defaultLocale.trim())
            }
        }

        /**
         * Adds the given translation to the given string resources.
         *
         * This methods automatically alters the given `translation` if necessary:
         * - escapes `translation`'s quotes (`l'avion -> l\'avion`).
         * - replaces `translation`'s parameters characters (`# -> %1$s`).
         *
         * @param key The key.
         * @param translations The translation values mapping locale to the new translation to add.
         * @throws IllegalArgumentException If one of the arguments is empty, or if `key` contains illegal char(s).
         */
        @Throws(IllegalArgumentException::class)
        fun addTranslations(key: String?, translations: Map<String, String?>) {

            if (isEmptyRow(key, translations.values)) {
                return
            }

            // Validating key.
            if (isInvalidKey(key)) {
                throw IllegalArgumentException("Invalid translation key `$key` for corresponding translation " +
                        "values: `${translations.values}`")
            }

            val cleanKey = key!!.trim()

            // Checking for duplicated key.
            if (isDuplicatedKey(cleanKey)) {
                throw IllegalArgumentException("Duplicated key `$cleanKey` for corresponding translation " +
                        "values: `${translations.values}`")
            }

            // Importing translations.
            translations.forEach {
                val cleanLocale = it.key.trim()
                if (!stringResources.containsKey(cleanLocale)) {
                    throw IllegalArgumentException("Unknown locale `$cleanLocale`")
                }
                val cleanValue = if (it.value == null) "" else it.value!!
                addTranslation(stringResources[cleanLocale]!!, cleanKey, cleanValue)
            }
        }

        /**
         * Writes the collected translation resource(s) to the corresponding output file(s).
         *
         * Once this operation completed, the android localized folder(s) should have been generated:
         * - `src/main/res/values` (*default locale*)
         * - `src/main/res/values-<locale>` (*one for each other supported locale*)
         */
        fun writeOutput() {
            stringResources.values.forEach {

                val outputFile = androidStringsResFile(it)

                if (!outputFile.parentFile.exists()) {
                    outputFile.parentFile.mkdirs()
                }

                it.strings.sortBy { it.name }
                it.plurals.sortBy { it.name }

                xmlMapper.writeValue(outputFile, it)
            }
        }

        /**
         * Adds the given key and corresponding translation to the [StringResources].
         *
         * Takes care of:
         * - Escaping single quotes.
         * - Replacing argument placeholder by corresponding android placeholder.
         * - Extracting plurals quantity from key (`<plurals>` case).
         */
        private fun addTranslation(stringResources: StringResources, key: String, translation: String) {

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
         * @return `true` if the given data corresponds to an *empty* row.
         */
        private fun isEmptyRow(key: String?, translations: Collection<String?>): Boolean {
            return key.isNullOrBlank() && translations.none { !it.isNullOrBlank() }
        }

        /**
         * @return `true` if the given key matches one of the following:
         * - is `null` or `blank`.
         * - contains invalid characters (see [XML_KEY_ILLEGAL_CHARS]).
         */
        private fun isInvalidKey(key: String?): Boolean {
            return key.isNullOrBlank() || XML_KEY_ILLEGAL_CHARS.containsMatchIn(key!!.trim())
        }

        /**
         * @return `true` if the given key has already been processed.
         */
        private fun isDuplicatedKey(key: String): Boolean {
            return !keys.add(key)
        }

        /**
         * @return The given localized resources corresponding android file path.
         */
        private fun androidStringsResFile(stringResources: StringResources): File {
            val localeSuffix = when {
                stringResources.defaultLocale -> ""
                else -> "-${stringResources.locale}"
            }
            val projectDir = project.projectDir.absolutePath
            return Paths.get(projectDir, "src", "main", "res", "values$localeSuffix", "strings.xml").toFile()
        }
    }
}