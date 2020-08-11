package com.github.gradle.android.i18n.export

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.gradle.android.i18n.conf.Configuration.xmlMapper
import com.github.gradle.android.i18n.model.*
import org.gradle.api.Project
import java.io.File
import java.io.OutputStream
import java.nio.file.Paths

/**
 * Android `xml` string resources exporter.
 *
 * This class implements loading resources from the `strings.xml` files of the project.
 *
 * The implementation of writing to an output file is left to concrete classes.
 */
abstract class AbstractExporter(private val project: Project) {

    /**
     * Exports the `xml` android string resources to the given output stream.
     */
    abstract fun export(outputStream: OutputStream, defaultLocale: String)

    protected fun loadProjectResources(defaultLocale: String) =
        project.deserializeResources(defaultLocale).toProjectData()
}

private fun Project.deserializeResources(defaultLocale: String): Map<Project, List<StringResources>> {

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
        this.childProjects.forEach {
            it.value.forEachModule(callback)
        }
    }
}

private fun Map<Project, List<StringResources>>.toProjectData(): ProjectData {

    val modules = map { entry ->
        val (moduleProj, resources) = entry
        val moduleDataName = moduleProj.path
            .replace("^:".toRegex(), "")
            .replace(':', '-')
            .let { name ->
                if (name.isNotEmpty()) name
                else "android-i18n"
            }
        val translations = resources.map { it.toTranslationData() }
        ModuleData(moduleDataName, translations)
    }
    return ProjectData(modules)
}

private fun StringResources.toTranslationData(): TranslationData {
    val fromStrings = strings.map { it.toStringData() }
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

private fun moduleResources(
    modulePath: String,
    defaultLocale: String
): List<StringResources> {

    val resFolderPattern = "values(:?-(.*))?".toRegex()

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
        .firstOrNull { file -> file.name == "strings.xml" }
        ?.let { stringsFile -> xmlMapper.readValue<StringResources>(stringsFile.inputStream()) }
        ?.let { stringResources ->
            if (locale.isBlank()) {
                stringResources.copy(locale = defaultLocale, defaultLocale = true)
            } else {
                stringResources.copy(locale = locale, defaultLocale = false)
            }
        } ?: StringResources()
}

internal val String.unescapeQuotes: String get() = this.replace("\\'", "'")