package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.generator.Xls2XmlGenerator
import jcifs.smb.SmbFile
import java.io.File
import java.io.InputStream

open class AndroidI18nPluginExtension(private val xls2XmlGenerator: Xls2XmlGenerator) {

    var sourceFile: String = ""

    fun importI18nResources() {
        if (sourceFile.endsWith(".xls")) {
            xls2XmlGenerator.generate(sourceInputStream())
        } else {
            throw UnsupportedOperationException("Source file '$sourceFile' is not supported")
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