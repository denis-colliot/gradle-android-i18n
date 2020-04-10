package com.github.gradle.android.i18n.export

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.api.Project
import java.io.OutputStream

class XlsExporter(project: Project) : AbstractExporter(project) {

    override fun export(outputStream: OutputStream, defaultLocale: String) {

        // Workbook creation.
        val workbook = XSSFWorkbook()
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
                val value = it.text.unescapeQuotes ?: ""
                rows.putItem(key, value)
            }

            stringResources.plurals.forEach { plural ->
                plural.items.forEach { pluralItem ->
                    val key = "${plural.name}:${pluralItem.quantity}"
                    val value = pluralItem.text.unescapeQuotes ?: ""
                    rows.putItem(key, value)
                }
            }
        }

        return rows
    }
}

private fun <K, V> MutableMap<K, MutableList<V>>.putItem(key: K, value: V) {
    if (!this.containsKey(key)) {
        this[key] = mutableListOf(value)
    } else {
        this[key]?.add(value)
    }
}