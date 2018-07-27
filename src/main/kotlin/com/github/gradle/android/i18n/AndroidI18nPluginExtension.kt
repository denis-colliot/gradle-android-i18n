package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.generator.Xls2XmlGenerator
import jcifs.smb.SmbFile
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

open class AndroidI18nPluginExtension(private val xls2XmlGenerator: Xls2XmlGenerator) {

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
     * Imports
     * @throws FileNotFoundException If the [sourceFile] does not exist.
     * @throws UnsupportedOperationException If the [sourceFile] type is not supported.
     */
    @Throws(FileNotFoundException::class, UnsupportedOperationException::class)
    fun importI18nResources() {
        sourceInputStream().use { inputStream ->
            if (sourceFile.endsWith(".xls")) {
                xls2XmlGenerator.generate(inputStream)
            } else {
                throw UnsupportedOperationException("Source file '$sourceFile' is not supported")
            }
        }
    }

    fun exportI18nResources() {
        TODO()
    }

    private fun sourceInputStream(): InputStream {
        return when {
            sourceFile.startsWith("smb://") ->
                // Samba URI.
                SmbFile(sourceFile).inputStream

            else ->
                // Default case.
                File(sourceFile).inputStream()
        }
    }
}