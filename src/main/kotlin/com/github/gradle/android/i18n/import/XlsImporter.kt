package com.github.gradle.android.i18n.import

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.api.Project
import java.io.InputStream

/**
 * Android i18n resources importer from `.xls` and `.xlsx` sources.
 */
class XlsImporter(project: Project) : AbstractImporter(project) {

    override fun generate(inputStream: InputStream, handler: ImportHandler) {

        /**
         * Map storing each locale to its corresponding column index.
         */
        val localizedColumns = mutableMapOf<Int, String>()

        readInput(inputStream) { row ->
            if (row.rowNum == 0) {
                // Reading locales row and initializing `StringResources`.
                row.cellIterator()
                        .asSequence()
                        .filter { it.columnIndex > 0 }
                        .filter { it.stringCellValue.isNotBlank() }
                        .forEach { cell ->
                            val locale = cell.stringCellValue
                            handler.addLocale(locale)
                            localizedColumns[cell.columnIndex] = locale
                        }
            } else {
                // Reading other data row.
                val key = row.getCell(0)?.stringCellValue

                val translations = localizedColumns
                        .map { Pair(it.value, row.getCell(it.key)?.stringCellValue) }
                        .toMap()

                handler.addTranslations(key, translations)
            }
        }

        handler.writeOutput()
    }

    private fun readInput(inputStream: InputStream, consumer: (Row) -> Unit) {
        WorkbookFactory.create(inputStream).use {
            // We assume there is only one sheet.
            it.getSheetAt(0).forEach(consumer)
        }
    }
}