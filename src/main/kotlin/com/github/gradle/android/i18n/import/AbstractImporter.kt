package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.conf.Configuration.xmlMapper
import com.github.gradle.android.i18n.model.StringResources
import com.github.gradle.android.i18n.model.XmlResource
import com.github.gradle.android.i18n.model.XmlResources
import org.gradle.api.Project
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

private val XML_KEY_ILLEGAL_CHARS = ".*[\\s${Regex.escape("+-*/\\;,'()[]{}!?=@|#~&\"^%<>")}].*".toRegex()

private const val XML_QUOTE = "\\\\'"

private const val SINGLE_QUOTE = "'"

const val QUANTITY_SEPARATOR = ":"

private const val XML_SINGLE_ARG = "%s"

private const val ARG_PLACEHOLDER = '#'

/**
 * Abstract android i18n resources importer.
 *
 * Provides methods to generate `values[-XX]/strings.xml` files from input data.
 */
abstract class AbstractImporter(private val project: Project) {

    /**
     * Generates the `xml` android string resources file(s) from given source input stream.
     *
     * @param inputStream The source input stream.
     * @param config The import task configuration.
     */
    abstract fun generate(inputStream: InputStream, config: ImportConfig)

}

/**
 * @return `true` if the given data corresponds to an *empty* row.
 */
internal fun isEmptyRow(key: String?, translations: Collection<String?>): Boolean {
    return key.isNullOrBlank() && translations.none { !it.isNullOrBlank() }
}

/**
 * @return `true` if the given key matches one of the following:
 * - is `null` or `blank`.
 * - contains invalid characters (see [XML_KEY_ILLEGAL_CHARS]).
 */
internal fun isInvalidKey(key: String?): Boolean {
    return key.isNullOrBlank() || XML_KEY_ILLEGAL_CHARS.containsMatchIn(key.trim())
}

internal fun String.cleanUpTranslatedText(): String {

    // Escaping single quotes.
    var mutableTranslation = this.replace(SINGLE_QUOTE.toRegex(), XML_QUOTE)

    // Handling translation parameters.
    val sharpCount = mutableTranslation.toCharArray().filter { it == ARG_PLACEHOLDER }.count()

    if (sharpCount == 1) {
        mutableTranslation = mutableTranslation.replace("${ARG_PLACEHOLDER}".toRegex(),
            XML_SINGLE_ARG
        )
    } else if (sharpCount > 1) {
        for (index in 1..sharpCount) {
            mutableTranslation =
                mutableTranslation.replaceFirst("$ARG_PLACEHOLDER".toRegex(),
                    getXmlIndexedArg(index)
                )
        }
    }
    return mutableTranslation
}

private fun getXmlIndexedArg(index: Int): String {
    return "%$index\\\$s"
}
