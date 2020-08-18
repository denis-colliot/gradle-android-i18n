package com.github.gradle.android.i18n.export

import com.github.gradle.android.i18n.model.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.api.Project
import java.io.OutputStream

/**
 * An Exporter that reads Android string resources in a Project and writes an Excel spreadsheet.
 *
 * This class defines how to export the resources ⇒ to and Excel workbook.
 * The parent class [AbstractExporter] defines which resources to export.
 */
class XlsExporter(project: Project) : AbstractExporter(project) {

    override fun export(outputStream: OutputStream, defaultLocale: String) {
        loadProjectResources(defaultLocale)
            .toWorkbook()
            .write(outputStream)
    }
}

private fun ProjectData.toWorkbook(): XSSFWorkbook {
    val workbook = XSSFWorkbook()
    modules.forEach { moduleData ->
        val sheet = workbook.createSheet(moduleData.name)

        // Header row: "key" | lang1 | lang2
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("key")
        moduleData.translations.forEachIndexed { translationDataIndex, translationData ->
            val colIndex = translationDataIndex + 1
            headerRow.createCell(colIndex).setCellValue(translationData.locale)
        }

        // Other rows:
        // name1 | lang1-value1 | lang2-value1
        // name2 | lang1-value2 | lang2-value2
        val firstTranslation = moduleData.translations.first()
        firstTranslation.stringDataList.forEachIndexed { firstTranslationStringDataIndex, firstTranslationStringData ->
            val rowIndex = firstTranslationStringDataIndex + 1
            val row = sheet.createRow(rowIndex)

            // First cell: name1
            row.createCell(0).setCellValue(firstTranslationStringData.name)

            // Next cells: lang1-value1 | lang2-value1
            moduleData.translations.forEachIndexed { translationDataIndex, translationData ->
                val colIndex = translationDataIndex + 1
                val stringData = translationData.stringDataList[firstTranslationStringDataIndex]
                row.createCell(colIndex).setCellValue(stringData.text)
            }
        }
    }
    return workbook
}