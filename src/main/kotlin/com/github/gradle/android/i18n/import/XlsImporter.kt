package com.github.gradle.android.i18n.import

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.api.Project
import java.io.InputStream

/**
 * Android i18n resources importer from `.xls` and `.xlsx` sources.
 */
class XlsImporter(project: Project) : AbstractImporter(project) {

    override fun generate(inputStream: InputStream, config: ImportConfig, handler: ImportHandler) {

        /**
         * Map storing each locale to its corresponding column index.
         */
        val localizedColumns = mutableMapOf<Int, String>()

        readInput(inputStream, config) { row, readLocales ->
            if (readLocales && row.rowNum == 0) {
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
            } else if (row.rowNum > 0) {
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

    private fun readInput(inputStream: InputStream, config: ImportConfig, consumer: (Row, Boolean) -> Unit) {
        var readLocales = true
        WorkbookFactory.create(inputStream).forEachIndexed { index, sheet ->
            when {
                config.allSheets -> if (config.sheetNameRegex.matches(sheet.sheetName)) {
                    // All sheets matching name regex.
                    sheet.forEach { consumer(it, readLocales) }
                    readLocales = false
                }

                else -> if (index == 0) {
                    // Only first sheet.
                    sheet.forEach { consumer(it, true) }
                }
            }
        }
    }
}