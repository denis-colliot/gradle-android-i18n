package com.github.gradle.android.i18n.export

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.api.Project
import java.io.OutputStream

class XlsExporter(project: Project) : AbstractExporter(project) {

    override fun export(outputStream: OutputStream, defaultLocale: String) {

        // Workbook creation.
        val workbook = XSSFWorkbook()
        // val createHelper = workbook.creationHelper
        val sheet = workbook.createSheet("android-i18n")

        // Header row.
        mapToRows(defaultLocale).entries.forEachIndexed { entryIndex, entryRow ->
            val (entryKey, entryValues) = entryRow
            val sheetRow = sheet.createRow(entryIndex)
            val keyCell = sheetRow.createCell(0)
            keyCell.setCellValue(entryKey)
            entryValues.forEachIndexed { valueIndex, valueText ->
                val valueCell = sheetRow.createCell(valueIndex + 1)
                valueCell.setCellValue(valueText)
            }
        }

        workbook.write(outputStream)
    }

    private fun mapToRows(defaultLocale: String): Map<String, List<String>> {

        val rows = mutableMapOf<String, MutableList<String>>()

        val loadedResources = loadProjectResources(defaultLocale)

        rows["key"] = loadedResources.map { it.locale }.toMutableList()

        loadedResources.forEach { stringResources ->

            stringResources.strings.forEach {
                val key = it.name ?: ""
                val value = it.text ?: ""
                if (!rows.containsKey(key)) {
                    rows[key] = mutableListOf(value)
                } else {
                    rows[key]?.add(value)
                }
            }

            stringResources.plurals.forEach { plural ->
                plural.items.forEach { pluralItem ->
                    val key = "${plural.name}:${pluralItem.quantity}"
                    val value = pluralItem.text ?: ""
                    if (!rows.containsKey(key)) {
                        rows[key] = mutableListOf(value)
                    } else {
                        rows[key]?.add(value)
                    }
                }
            }
        }

        return rows
    }
}