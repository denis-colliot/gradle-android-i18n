package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.import.ImportConfig
import com.github.gradle.android.i18n.import.XlsImporter
import jcifs.smb.SmbFile
import org.gradle.api.Project
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * [AndroidI18nPlugin] extension.
 */
open class AndroidI18nPluginExtension(
        private val project: Project,
        private val xlsImporter: XlsImporter
) {

    /**
     * The source file URI that can be configured in host project.
     *
     * Supported URIs:
     * - Samba URI : `smb://<domain>;<login>:<password>@<host>/<path_to_file>`
     * - Windows UNC : `\\<host>\<path_to_file>`
     *
     * Supported file types:
     * - `.xls` (and not `.xlsx`)
     */
    var sourceFile: String = ""

    /**
     * Default android locale.
     */
    var defaultLocale: String = "en"

    /**
     * Import all sheets when using `xls`/`xlsx` source file.
     *
     * Default is `false`.
     */
    var importAllSheets: Boolean = false

    /**
     * Sheet names filter when using `xls`/`xlsx` source file and [importAllSheets] is set to `true`.
     *
     * Default is `.*` to read all sheets.
     */
    var importSheetNameRegex: String = "^.*$"

    /**
     * Imports the i18n translation resources from the configured [sourceFile].
     *
     * If the [sourceFile] is not defined (empty or blank), the method does nothing.
     *
     * @throws FileNotFoundException If the [sourceFile] does not exist.
     * @throws UnsupportedOperationException If the [sourceFile] type is not supported.
     */
    @Throws(FileNotFoundException::class, UnsupportedOperationException::class)
    fun importI18nResources() {
        sourceFile.apply {
            if (isBlank()) {
                // Does nothing if source file is not configured.
                return
            }

            val config = ImportConfig(defaultLocale, importAllSheets, importSheetNameRegex.toRegex())

            toInputStream().use { inputStream ->
                when {
                    endsWith(".xls") || endsWith(".xlsx") -> xlsImporter.generate(inputStream, config)
                    else -> throw UnsupportedOperationException("Source file `$this` is not supported")
                }
            }
        }
    }

    /**
     * Exports the project android i18n resources to an output file.
     */
    fun exportI18nResources() {
        TODO()
    }

    /**
     * Returns the [InputStream] corresponding to [sourceFile].
     */
    private fun toInputStream(): InputStream {
        return when {
            sourceFile.startsWith("smb://") -> {
                // Samba URI.
                setJcifsProperties()
                SmbFile(sourceFile).inputStream
            }
            else -> {
                // Default case.
                File(sourceFile).inputStream()
            }
        }
    }

    /**
     * Sets the JCIFS properties based on *project* configuration properties starting with "`androidI18n.jcifs.*`".
     *
     * @see jcifs.Config
     */
    private fun setJcifsProperties() {
        project.properties
                .filter {
                    it.key.startsWith("androidI18n.jcifs.")
                            && it.value is String
                            && (it.value as String).isNotBlank()
                }
                .forEach {
                    jcifs.Config.setProperty(it.key.substringAfter('.'), it.value.toString().trim())
                }
    }
}