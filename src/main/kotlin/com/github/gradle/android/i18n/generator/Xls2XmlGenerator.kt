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

    fun generate(inputStream: InputStream) {

        val keys = HashSet<String>()
        val frStringResources = StringResources()
        val enStringResources = StringResources()

        readInput(inputStream) { row ->
            val key = row.getCell(0).stringCellValue
            val french = row.getCell(1).stringCellValue
            val english = row.getCell(2).stringCellValue

            if (!keys.add(key)) {
                throw IllegalArgumentException("Duplicated key '$key'")
            }
            add(frStringResources, key, french)
            add(enStringResources, key, english)
        }

        logger.debug("French translations: {}", frStringResources)
        logger.debug("English translations: {}", enStringResources)

        writeOutput(androidStringsResFile("fr"), frStringResources)
        writeOutput(androidStringsResFile(), enStringResources)
    }

    private fun readInput(inputStream: InputStream, consumer: (Row) -> Unit) {
        inputStream.use {
            WorkbookFactory.create(it).use {
                // We assume there is only one sheet.
                it.getSheetAt(0).forEach(consumer)
            }
        }
    }
}