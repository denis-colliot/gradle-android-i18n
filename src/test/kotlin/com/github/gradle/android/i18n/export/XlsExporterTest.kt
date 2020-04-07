package com.github.gradle.android.i18n.export

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class XlsExporterTest {

    @Test
    fun `should export to xlsx`() {

        // Given
        val projectDir = File("src/test/resources/export")
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val exporter = XlsExporter(project)
        val outputFile = File.createTempFile("i18n", ".xlsx")
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
            outputFile.toRows()
        )

        outputFile.delete()
    }
}

private fun File.toRows(): List<List<String>> {
    val sheet = WorkbookFactory.create(this).first()

    @Suppress("UnnecessaryVariable")
    val rows = sheet.rowIterator().asSequence().toList().map { row ->
        row.cellIterator().asSequence().toList().map { cell -> cell.stringCellValue }
    }
    return rows
}
