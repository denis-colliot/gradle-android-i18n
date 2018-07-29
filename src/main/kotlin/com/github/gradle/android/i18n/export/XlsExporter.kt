package com.github.gradle.android.i18n.export

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.api.Project
import java.io.OutputStream

class XlsExporter(project: Project) : AbstractExporter(project) {

    override fun export(outputStream: OutputStream, defaultLocale: String) {

        // Workbook creation.
        val workbook = XSSFWorkbook()
        val createHelper = workbook.creationHelper
        val sheet = workbook.createSheet("android-i18n")

        // Header row.
        val headerRow = sheet.createRow(0)
        loadProjectResources(defaultLocale).forEachIndexed { index, stringResources ->
            if (index == 0) {
                val headerKeyCell = headerRow.createCell(index)
                headerKeyCell.setCellValue("key")
            } else {
                val headerLocaleCell = headerRow.createCell(index)
                headerLocaleCell.setCellValue(stringResources.locale)
            }
        }
    }

    private fun mapToRows(defaultLocale: String) {

        val rows = mutableMapOf<String, List<String>>()

        loadProjectResources(defaultLocale).forEach { stringResources ->
            stringResources.strings.forEach {
                rows[it.name ?: ""] = mutableListOf(it.text ?: "")
            }
            stringResources.plurals.forEach {
                // TODO rows[it.name] = it.items.ma
            }
        }
    }
}