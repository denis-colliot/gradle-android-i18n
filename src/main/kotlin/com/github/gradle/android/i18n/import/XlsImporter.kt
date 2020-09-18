package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.model.*
import com.github.gradle.android.i18n.toStringResourcesByPath
import com.github.gradle.android.i18n.write
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.api.Project
import java.io.InputStream

/**
 * Android i18n resources importer from `.xls` and `.xlsx` sources.
 */
class XlsImporter(private val project: Project) : AbstractImporter(project) {

    override fun generate(inputStream: InputStream, config: ImportConfig) {

        val projectData = WorkbookFactory.create(inputStream).use { workbook ->
            if (project.isSingleModule()) {
                workbook.toSingleModuleProjectData(config)
            } else {
                workbook.toMultiModuleProjectData()
            }
        }

        val stringResourcesByPath = projectData.toStringResourcesByPath(
            project.projectDir,
            config.defaultLocale
        )
        stringResourcesByPath.write()
    }
}

private fun Project.isSingleModule(): Boolean {
    return this.childProjects.size <= 1
}

private fun Workbook.toMultiModuleProjectData(): ProjectData {
    val moduleDataList = this.map { sheet: Sheet ->
        val stringDataByLocale = sheet.toList().toStringDataByLocale()
        val translationDataList = stringDataByLocale.keys.map { locale ->
            TranslationData(locale, stringDataByLocale[locale] as List<StringData>)
        }
        ModuleData(sheet.sheetName, translationDataList)
    }
    return ProjectData(moduleDataList)
}

private fun Workbook.toSingleModuleProjectData(config: ImportConfig): ProjectData {
    val rows = readWorkbookRows(config, this)
    val stringDataByLocale = rows.toStringDataByLocale()
    val translationDataList = stringDataByLocale.keys.map { locale ->
        TranslationData(locale, stringDataByLocale[locale] as List<StringData>)
    }
    val moduleData = ModuleData(ModuleData.DEFAULT_NAME, translationDataList)
    return ProjectData(listOf(moduleData))
}

private fun List<Row>.toStringDataByLocale(): Map<String, List<StringData>> {

    // Reading locales row.
    val headerRow = this.first()
    val localizedColumns = readLocaleByColIndex(headerRow)

    // Reading other data row.
    val stringKeySet = mutableSetOf<String?>() // Keys set facilitating duplicated key control.
    val stringDataByLocale = mutableMapOf<String, MutableList<StringData>>()
    this.drop(1)
        .filterNot { row -> isEmptyWorkbookRow(row) }
        .map { row -> pairCheckedStringKeyWithRow(row) }
        .forEach { (stringKey, row) -> // Record translations (key, text) by locale
            if (!stringKeySet.add(stringKey)) {
                throw IllegalArgumentException(
                    "Duplicated key `$stringKey` " +
                            "at line ${row.rowNum + 1} " +
                            "in sheet `${row.sheet.sheetName}`"
                )
            }
            row.drop(1).forEach { cell ->
                localizedColumns[cell.columnIndex]?.let { locale ->
                    val translatedText = cell.stringCellValue?.cleanUpTranslatedText()
                    val stringData = StringData(stringKey, translatedText)
                    val stringDataList = stringDataByLocale[locale] ?: mutableListOf()
                    stringDataList.add(stringData)
                    stringDataByLocale[locale] = stringDataList
                }
            }
        }

    return stringDataByLocale
}

private fun pairCheckedStringKeyWithRow(row: Row): Pair<String?, Row> {
    val stringKey = row.getCell(0)?.stringCellValue?.trim()
    if (isInvalidKey(stringKey)) {
        throw IllegalArgumentException(
            "Invalid translation key `$stringKey` " +
                    "at row ${row.rowNum + 1} " +
                    "in sheet `${row.sheet.sheetName}`"
        )
    }
    return Pair(stringKey, row)
}

private fun isEmptyWorkbookRow(row: Row): Boolean {
    return isEmptyRow(
        row.getCell(0)?.stringCellValue,
        row.drop(1).map { cell -> cell?.stringCellValue }
    )
}

private fun readLocaleByColIndex(headerRow: Row): Map<Int, String> {
    // Map storing each locale to its corresponding column index.
    val localizedColumns = mutableMapOf<Int, String>()
    headerRow.drop(1)
        .filter { it.stringCellValue.isNotBlank() }
        .forEach { cell ->
            val locale = cell.stringCellValue.trim()
            localizedColumns[cell.columnIndex] = locale
        }
    return localizedColumns
}

private fun readWorkbookRows(config: ImportConfig, workbook: Workbook): List<Row> {

    val result = mutableListOf<Row>()
    workbook.filterIndexed { sheetIndex, sheet: Sheet ->
        // All sheets matching name regex.
        config.allSheets && config.sheetNameRegex.matches(sheet.sheetName) || sheetIndex == 0
    }.forEachIndexed { sheetIndex, sheet: Sheet ->
        sheet.forEachIndexed { rowIndex, row ->
            // Read first row (locales) only for first sheet
            if (sheetIndex == 0 || rowIndex > 0) {
                result.add(row)
            }
        }
    }
    return result
}