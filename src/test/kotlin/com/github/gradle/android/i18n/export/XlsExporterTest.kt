package com.github.gradle.android.i18n.export

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream

class XlsExporterTest {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `should export single module strings to xlsx`() {

        // Given
        val projectDir = File("src/test/resources/export")
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val exporter = XlsExporter(project)
        val outputFile = temporaryFolder.newFile("i18n.xlsx")
        val outputStream = FileOutputStream(outputFile)

        outputStream.let {

            // When
            exporter.export(it, "en")
            it.close()
            println("Exported file:\n${outputFile.path}")
        }

        // Then
        assertEquals(
            listOf(
                listOf("key", "en", "fr"),
                listOf("name1", "Value 1", "Valeur 1"),
                listOf("name2", "Value 2", "Valeur 2"),
                listOf("name3", "Value 3", "Valeur 3"),
                listOf("name4", "Valeur 'with quotes' 4", "Valeur 'avec guillemets' 4"),
                listOf("plurals1:one", "%s singular 1", "%s singulier 1"),
                listOf("plurals1:other", "%s plural 1", "%s pluriel 1"),
                listOf("plurals2:one", "%d singular 2", "%d singulier 2"),
                listOf("plurals2:other", "%d plural 2", "%d pluriel 2"),
                listOf("plurals3:one", "%d singular 3 with 'quotes'", "%d singulier 3 avec 'guillemets'"),
                listOf("plurals3:other", "%d plural 3 with 'quotes'", "%d pluriel 3 avec 'guillemets'")
            ),
            outputFile.parsedFirstSheet()
        )
    }

    @Test
    fun `should export multi module strings to xlsx`() {

        // Given
        val projectDir = File("src/test/resources/export_multi")
        val rootProject = ProjectBuilder.builder().withProjectDir(projectDir).build()
        rootProject.attachNewChildProject(File(projectDir, "app"))
        rootProject.attachNewChildProject(File(projectDir, "lib"))
        val exporter = XlsExporter(rootProject)
        val outputFile = temporaryFolder.newFile("i18n.xlsx")
        val outputStream = FileOutputStream(outputFile)

        outputStream.let {

            // When
            exporter.export(it, "en")
            it.close()
            println("Exported file:\n${outputFile.path}")
        }

        // Then
        assertEquals(
            mapOf(
                "app" to listOf(
                    listOf("key", "en", "fr"),
                    listOf("app-name1", "App Value 1", "Valeur App 1"),
                    listOf("app-name2", "App Value 2", "Valeur App 2")
                ), "lib" to listOf(
                    listOf("key", "en", "fr"),
                    listOf("lib-name1", "Lib Value 1", "Valeur Lib 1"),
                    listOf("lib-name2", "Lib Value 2", "Valeur Lib 2")
                )
            ),
            outputFile.parsedSheetsByName()
        )
    }

    @Test
    fun `should export multi module strings to xlsx skipping modules without strings`() {

        // Given
        val projectDir = File("src/test/resources/export_multi")
        val rootProject = ProjectBuilder.builder().withProjectDir(projectDir).build()
        rootProject.attachNewChildProject(File(projectDir, "app"))
        rootProject.attachNewChildProject(File(projectDir, "lib-no-strings"))
        val exporter = XlsExporter(rootProject)
        val outputFile = temporaryFolder.newFile("i18n.xlsx")
        val outputStream = FileOutputStream(outputFile)

        outputStream.let {

            // When
            exporter.export(it, "en")
            it.close()
            println("Exported file:\n${outputFile.path}")
        }

        // Then
        assertEquals(
            mapOf(
                "app" to listOf(
                    listOf("key", "en", "fr"),
                    listOf("app-name1", "App Value 1", "Valeur App 1"),
                    listOf("app-name2", "App Value 2", "Valeur App 2")
                )
            ),
            outputFile.parsedSheetsByName()
        )
    }

    @Test
    fun `should export multi module strings with path prefix in path`() {

        // Given
        val projectDir = File("src/test/resources/export_multi")
        val rootProject = ProjectBuilder.builder().withProjectDir(projectDir).build()
        rootProject.attachNewChildProject(File(projectDir, "app"))
        val intermediateDir = File(projectDir, "features")
        val intermediateProject = rootProject.attachNewChildProject(intermediateDir)
        intermediateProject.attachNewChildProject(File(intermediateDir, "feature1"))
        val exporter = XlsExporter(rootProject)
        val outputFile = temporaryFolder.newFile("i18n.xlsx")
        val outputStream = FileOutputStream(outputFile)

        outputStream.let {

            // When
            exporter.export(it, "en")
            it.close()
            println("Exported file:\n${outputFile.path}")
        }

        // Then
        assertEquals(
            mapOf(
                "app" to listOf(
                    listOf("key", "en", "fr"),
                    listOf("app-name1", "App Value 1", "Valeur App 1"),
                    listOf("app-name2", "App Value 2", "Valeur App 2")
                ),
                "features-feature1" to listOf(
                    listOf("key", "en", "fr"),
                    listOf("feature1-name1", "Feature 1 Value 1", "Valeur 1 Fonctionnalité 1"),
                    listOf("feature1-name2", "Feature 1 Value 2", "Valeur 2 Fonctionnalité 1")
                )
            ),
            outputFile.parsedSheetsByName()
        )
    }
}

/**
 * Attach a new child project
 */
private fun Project.attachNewChildProject(moduleDir: File): Project {
    return ProjectBuilder.builder()
            .withProjectDir(moduleDir)
            .withParent(this)
            .withName(moduleDir.name)
            .build()
}

private fun File.parsedSheetsByName(): Map<String, ParsedSheet> =
    WorkbookFactory.create(this).associate { sheet ->
        sheet.sheetName to sheet.parsed()
    }

private typealias ParsedCell = String
private typealias ParsedRow = List<ParsedCell>
private typealias ParsedSheet = List<ParsedRow>

private fun File.parsedFirstSheet(): ParsedSheet {
    val sheet = WorkbookFactory.create(this).first()
    return sheet.parsed()
}

private fun Sheet.parsed(): ParsedSheet =
    rowIterator().asSequence().toList().map { row ->
        row.cellIterator().asSequence().toList().map { cell -> cell.stringCellValue }
    }
