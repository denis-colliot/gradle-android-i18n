package com.github.gradle.android.i18n.export

import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.api.Project
import java.io.OutputStream

/**
 * An Exporter that reads all Android string resources in a Project and writes an Excel spreadsheet.
 */
class XlsExporter(private val project: Project) : AbstractExporter(project) {

    override fun export(outputStream: OutputStream, defaultLocale: String) {

        // Workbook creation.
        val workbook = XSSFWorkbook()

        project.forEachModule { moduleProject ->

            val sheetName = moduleProject.path
                .replace("^:".toRegex(), "")
                .replace(':', '-')
                .let {
                    if (it.isNotEmpty()) it
                    else "android-i18n"
                }
            val rowEntries = readModuleResources(moduleProject, defaultLocale)
            if (rowEntries.isNotEmpty()) {
                val sheet = workbook.createSheet(sheetName)
                addToSheet(rowEntries, sheet)
            }
        }

        workbook.write(outputStream)
    }
}

private fun addToSheet(
    resources: Map<String, List<String>>,
    sheet: XSSFSheet
) {
    resources.entries.forEachIndexed { entryIndex, entryRow ->
        val (entryKey, entryValues) = entryRow
        val sheetRow = sheet.createRow(entryIndex)
        val keyCell = sheetRow.createCell(0)
        keyCell.setCellValue(entryKey)
        entryValues.forEachIndexed { valueIndex, valueText ->
            val valueCell = sheetRow.createCell(valueIndex + 1)
            valueCell.setCellValue(valueText)
        }
    }
}

private fun readModuleResources(project: Project, defaultLocale: String): Map<String, List<String>> {

    val rows = mutableMapOf<String, MutableList<String>>()

    val loadedResources = moduleResources(project.projectDir.absolutePath, defaultLocale)

    if (!loadedResources.all { it.strings.isEmpty() && it.plurals.isEmpty() }) {
        rows["key"] = loadedResources.map { it.locale }.toMutableList()
    }

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

/**
 * Apply a callback to each module project that is a child of the receiver.
 *
 * Given a project `rootProject` with the following structure:
 *
 * ```
 * :app
 * :features:feature1
 * :features:feature2
 * :library:library1
 * :library:library2
 * ```
 *
 * Calling `rootProject.forEachModule(callback) will apply the callback
 * to `app`, `feature1`, `feature2`, `library1` and `library2`
 * but not to `features` and `library` that are also considered by Gradle as child projects.
 */
private fun Project.forEachModule(callback: (Project) -> Unit) {
    if (this.childProjects.isEmpty()) {
        callback(this)
    } else {
        this.childProjects.forEach {
            it.value.forEachModule(callback)
        }
    }
}

private fun <K, V> MutableMap<K, MutableList<V>>.putItem(key: K, value: V) {
    if (!this.containsKey(key)) {
        this[key] = mutableListOf(value)
    } else {
        this[key]?.add(value)
    }
}