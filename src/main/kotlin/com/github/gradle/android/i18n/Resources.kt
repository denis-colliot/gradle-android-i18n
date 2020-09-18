package com.github.gradle.android.i18n

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.gradle.android.i18n.conf.Configuration
import com.github.gradle.android.i18n.import.QUANTITY_SEPARATOR
import com.github.gradle.android.i18n.model.*
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

fun Map<Path, StringResources>.write() {
    forEach { (path, stringResources) ->
        val outputResFile = path.toFile()
        outputResFile.parentFile.mkdirs()
        Configuration.xmlMapper.writeValue(outputResFile, stringResources)
    }
}

fun Project.deserializeResources(defaultLocale: String): Map<Project, List<StringResources>> {

    val result = mutableMapOf<Project, List<StringResources>>()

    forEachModule { moduleProject ->
        val resources = moduleResources(moduleProject.projectDir.absolutePath, defaultLocale)
        if (resources.isNotEmpty()) {
            result[moduleProject] = resources
        }
    }

    return result
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
        this.childProjects.forEach { (_, childProj) ->
            childProj.forEachModule(callback)
        }
    }
}

private fun moduleResources(
    modulePath: String,
    defaultLocale: String
): List<StringResources> {

    val resFolderPattern = "values(-(.*))?".toRegex()

    val resources = mutableListOf<StringResources>()
    Paths.get(modulePath, "src", "main", "res")
        .toFile()
        .walkTopDown()
        .maxDepth(1)
        .filter { it.isDirectory && resFolderPattern.matches(it.name) }
        .sortedBy { it.path }
        .map { resourcesInDirectory(it, resFolderPattern, defaultLocale) }
        .filter { it.strings.isNotEmpty() || it.plurals.isNotEmpty() }
        .forEach { resources.add(it) }
    return resources
}

private fun resourcesInDirectory(

    directory: File,
    resFolderPattern: Regex,
    defaultLocale: String

): StringResources {

    val locale = resFolderPattern.matchEntire(directory.name)!!.groupValues[2]

    return directory.walkTopDown()
        .maxDepth(1)
        .firstOrNull { file -> file.name.endsWith("strings.xml") }
        ?.let { stringsFile -> Configuration.xmlMapper.readValue<StringResources>(stringsFile.inputStream()) }
        ?.let { stringResources ->
            if (locale.isBlank()) {
                stringResources.copy(locale = defaultLocale, defaultLocale = true)
            } else {
                stringResources.copy(locale = locale, defaultLocale = false)
            }
        } ?: StringResources()
}

fun Map<Project, List<StringResources>>.toProjectData(): ProjectData {

    val modules = map { (moduleProj, resources) ->
        val moduleDataName = moduleProj.path
            .replace("^:".toRegex(), "")
            .replaceFirst(':', '.')
            .replace(':', '-')
            .let { name ->
                if (name.isNotEmpty()) name
                else ModuleData.DEFAULT_NAME
            }
        val translations = resources.map { it.toTranslationData() }
        ModuleData(moduleDataName, translations)
    }
    return ProjectData(modules)
}

private fun StringResources.toTranslationData(): TranslationData {
    val fromStrings = strings.map { it.toStringData() }.sortedBy { it.name }
    val fromPlurals = plurals.flatMap { it.toStringDataList() }
    val stringDataList = fromStrings + fromPlurals
    return TranslationData(locale, stringDataList)
}

private fun XmlResource.toStringData(): StringData = StringData(name, text?.unescapeQuotes)
private fun XmlResources.toStringDataList(): List<StringData> = items.map {
    val namePrefix =
        if (name.isBlank()) ""
        else "${name}:"
    StringData(namePrefix + it.quantity, it.text?.unescapeQuotes)
}

internal val String.unescapeQuotes: String get() = this.replace("\\'", "'")
fun ProjectData.toStringResourcesByPath(
    baseDir: File,
    defaultLocale: String
): Map<Path, StringResources> {
    val isMultiModule = this.modules.size > 1
    return this.modules.map { moduleData ->
        val moduleBaseDir = if (isMultiModule) File(baseDir, moduleData.pathRelativeToProj()).path else baseDir.path
        val moduleResPath = Paths.get(moduleBaseDir, "src", "main", "res")
        Pair(moduleResPath, moduleData)
    }.flatMap { (resDirPath, moduleData) ->
        moduleData.translations.map { translationData ->
            val valuesPath =
                if (translationData.locale == defaultLocale) "values"
                else "values-${translationData.locale}"
            val stringsFileName = if (isMultiModule) moduleData.stringsFileName() else "strings.xml"
            val stringsFileSubPath = Paths.get(valuesPath, stringsFileName)
            val stringsFileFullPath = resDirPath.resolve(stringsFileSubPath)
            val stringResources = translationData.toStringResources(defaultLocale)
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

private fun TranslationData.toStringResources(defaultLocale: String): StringResources {

    val stringDataListByPlurality = this.stringDataList.groupBy {
        it.name?.contains(QUANTITY_SEPARATOR) == true
    }

    val singularStringDataList = stringDataListByPlurality[false]
    val pluralStringDataList = stringDataListByPlurality[true]

    return StringResources(
        locale,
        locale == defaultLocale,
        singularStringDataList
            ?.sortedBy { (name, _) -> name }
            ?.toSingularXmlResourceList()
            ?.toMutableList()
            ?: mutableListOf(),
        pluralStringDataList
            ?.sortedBy { (name, _) -> name }
            ?.toPluralXmlResourcesList()
            ?.toMutableList()
            ?: mutableListOf()
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