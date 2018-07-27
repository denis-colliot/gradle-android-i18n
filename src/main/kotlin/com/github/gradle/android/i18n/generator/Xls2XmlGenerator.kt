package com.github.gradle.android.i18n.generator

import com.github.gradle.android.i18n.model.StringResources
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.api.Project
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.*

class Xls2XmlGenerator(project: Project) : XmlGenerator(project) {

    private val logger = LoggerFactory.getLogger(Xls2XmlGenerator::class.java)

    override fun generate(inputStream: InputStream, defaultLocale: String) {

        val keys = HashSet<String>()
        val stringResources = mutableMapOf<Int, StringResources>()

        readInput(inputStream) { row ->
            if (row.rowNum == 0) {
                row.cellIterator()
                        .asSequence()
                        .filter { it.columnIndex > 0 }
                        .filter { it.stringCellValue.isNotBlank() }
                        .forEach {
                            val locale = when (defaultLocale) {
                                it.stringCellValue -> null
                                else -> it.stringCellValue
                            }
                            stringResources[it.columnIndex] = StringResources(locale)
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

        stringResources
                .values
                .forEach { resources ->
                    val locale = resources.locale
                    logger.debug("Translations for locale $locale: $resources")
                    writeOutput(androidStringsResFile(locale), resources)
                }
    }

    private fun readInput(inputStream: InputStream, consumer: (Row) -> Unit) {
        WorkbookFactory.create(inputStream).use {
            // We assume there is only one sheet.
            it.getSheetAt(0).forEach(consumer)
        }
    }
}