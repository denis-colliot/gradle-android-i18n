package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.generator.Xls2XmlGenerator
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI

open class AndroidI18nPluginExtension(val project: Project) {

    var sourceFile: String = ""

    fun importI18nResources() {
        if (sourceFile.endsWith(".xls")) {
            Xls2XmlGenerator(project).generate(sourceInputStream())
        } else {
            throw UnsupportedOperationException("Source file '$sourceFile' is not supported")
        }
    }

    fun exportI18nResources() {
        TODO()
    }

    private fun sourceInputStream(): InputStream {
        return when {
            sourceFile.startsWith("smb:/") ->
                // Samba URI.
                SmbFileInputStream(SmbFile(sourceFile))

            sourceFile.startsWith("\\\\") ->
                // Windows network URI.
                FileInputStream(File(URI("file:$sourceFile")))

            else ->
                // Default case.
                FileInputStream(File(sourceFile))
        }
    }
}