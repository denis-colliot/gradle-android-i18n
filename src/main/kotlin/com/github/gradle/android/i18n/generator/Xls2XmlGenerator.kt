package com.github.gradle.android.i18n.generator

import com.github.gradle.android.i18n.model.StringResources
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.api.Project
import java.io.InputStream
import java.util.*

class Xls2XmlGenerator(project: Project) : XmlGenerator(project) {

    override fun generate(inputStream: InputStream, defaultLocale: String) {

        val keys = HashSet<String>()
        val stringResources = mutableMapOf<Int, StringResources>()

        readInput(inputStream) { row ->
            if (row.rowNum == 0) {
                row.cellIterator()
                        .asSequence()
                        .filter { it.columnIndex > 0 }
                        .filter { it.stringCellValue.isNotBlank() }
                        .forEach { cell ->
                            val locale = cell.stringCellValue.trim()
                            stringResources[cell.columnIndex] = StringResources(locale, locale == defaultLocale)
                        }
            } else {
                val key = row.getCell(0).stringCellValue

                if (!keys.add(key)) {
                    throw IllegalArgumentException("Duplicated key '$key'")
                }

                row.cellIterator()
                        .asSequence()
                        .filter { it.columnIndex > 0 }
                        .forEach { cell ->
                            stringResources[cell.columnIndex]?.let {
                                add(it, key, cell.stringCellValue)
                            }
                        }
            }
        }

        stringResources.values.forEach { writeOutput(it) }
    }

    private fun readInput(inputStream: InputStream, consumer: (Row) -> Unit) {
        WorkbookFactory.create(inputStream).use {
            // We assume there is only one sheet.
            it.getSheetAt(0).forEach(consumer)
        }
    }
}