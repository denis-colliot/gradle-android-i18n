package com.github.gradle.android.i18n.export

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class XlsExporterTest {

    @Test
    fun `should export xlsx`() {

        val projectDir = File("src/test/resources/export")
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val exporter = XlsExporter(project)
        val outputFile = File.createTempFile("i18n", ".xlsx")
        val outputStream = FileOutputStream(outputFile)

        exporter.export(outputStream, "en")

        outputStream.close()
        println("Exported file:\n${outputFile.path}")
        Runtime.getRuntime().exec("open ${outputFile.path}")
    }
}