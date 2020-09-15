package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.conf.Configuration
import com.github.gradle.android.i18n.model.*
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.api.Project
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Android i18n resources importer from `.xls` and `.xlsx` sources.
 */
class XlsImporter(private val project: Project) : AbstractImporter(project) {

    override fun generate(inputStream: InputStream, config: ImportConfig) {

        val workbook = WorkbookFactory.create(inputStream) // TODO .use

        val projectData = if (project.isSingleModule()) {
            workbook.toSingleModuleProjectData(config)
        } else {
            workbook.toMultiModuleProjectData(config)
        }

        val stringResourcesByPath = projectData.toStringResourcesByPath(project.projectDir, config)
        stringResourcesByPath.write()
    }
}

private fun Project.isSingleModule(): Boolean {
    return this.childProjects.size <= 1
}

private fun Workbook.toMultiModuleProjectData(config: ImportConfig): ProjectData {
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

private fun ProjectData.toStringResourcesByPath(
    baseDir: File,
    config: ImportConfig
): Map<Path, StringResources> {
    val isMultiModule = this.modules.size > 1
    return this.modules.map { moduleData ->
        val moduleBaseDir = if (isMultiModule) File(baseDir, moduleData.pathRelativeToProj()).path else baseDir.path
        val moduleResPath = Paths.get(moduleBaseDir, "src", "main", "res")
        Pair(moduleResPath, moduleData)
    }.flatMap { (resDirPath, moduleData) ->
        moduleData.translations.map { translationData ->
            val valuesPath =
                if (translationData.locale == config.defaultLocale) "values"
                else "values-${translationData.locale}"
            val stringsFileName = if (isMultiModule) moduleData.stringsFileName() else "strings.xml"
            val stringsFileSubPath = Paths.get(valuesPath, stringsFileName)
            val stringsFileFullPath = resDirPath.resolve(stringsFileSubPath)
            val stringResources = translationData.toStringResources(config)
            Pair(stringsFileFullPath, stringResources)
        }
    }.associateBy({ it.first }) { it.second }
}

private fun ModuleData.stringsFileName(): String =
    "${this.name
        .replace("^[^.]*\\.".toRegex(), "")
        .replace('-', '_')}_strings.xml"

private fun ModuleData.pathRelativeToProj(): String =
    this.name.split(".").joinToString(File.separator)

private fun TranslationData.toStringResources(config: ImportConfig): StringResources {

    val stringDataListByPlurality = this.stringDataList.groupBy {
        it.name?.contains(QUANTITY_SEPARATOR) == true
    }

    val singularStringDataList = stringDataListByPlurality[false]
    val pluralStringDataList = stringDataListByPlurality[true]

    return StringResources(
        locale,
        locale == config.defaultLocale,
        singularStringDataList?.toSingularXmlResourceList()?.toMutableList() ?: mutableListOf(), // TODO sorted
        pluralStringDataList?.toPluralXmlResourcesList()?.toMutableList() ?: mutableListOf() // TODO sorted
    )
}

private fun List<StringData>.toPluralXmlResourcesList(): List<XmlResources> {
    val groupedByPluralKey = groupBy { stringData: StringData ->
        assert(stringData.name?.contains(QUANTITY_SEPARATOR) == true)
        stringData.name?.split(QUANTITY_SEPARATOR)!!.first()
    }
    return groupedByPluralKey.map { (pluralName, pluralStringDataList) ->
        XmlResources(
            pluralName,
            pluralStringDataList.map { stringData ->
                val quantity = stringData.name?.split(QUANTITY_SEPARATOR)?.get(1)
                XmlResource(name = null, quantity = quantity, text = stringData.text)
            }.toMutableList()
        )
    }
}

private fun List<StringData>.toSingularXmlResourceList(): List<XmlResource> =
    map { stringData ->
        XmlResource(name = stringData.name, text = stringData.text)
    }.sortedBy { it.name }

private fun Map<Path, StringResources>.write() {
    forEach { (path, stringResources) ->
        val outputResFile = path.toFile()
        outputResFile.parentFile.mkdirs()
        Configuration.xmlMapper.writeValue(outputResFile, stringResources)
    }
}