package com.github.gradle.android.i18n.export

import com.github.gradle.android.i18n.import.ImportConfig
import com.github.gradle.android.i18n.import.XlsImporter
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import java.io.File
import java.io.FileInputStream
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

    @Test
    fun `should export then import`() {

        val exportProjectDir = File("src/test/resources/export_then_import/export")
        val exportProject = ProjectBuilder.builder().withProjectDir(exportProjectDir).build()
        val exporter = XlsExporter(exportProject)
        val exportOutputFile = File.createTempFile("i18n", ".xlsx")
        val outputStream = FileOutputStream(exportOutputFile)
        exporter.export(outputStream, "en")
        outputStream.close()
        println("Exported file:\n${exportOutputFile.path}")

        val importProjectDir = File("build/output/import")
        importProjectDir.mkdirs()
        val importProject = ProjectBuilder.builder().withProjectDir(importProjectDir).build()
        val importer = XlsImporter(importProject)
        val inputStream = FileInputStream(exportOutputFile)
        val config = ImportConfig("en", false, Regex(".*"))
        importer.generate(inputStream, config)
    }
}