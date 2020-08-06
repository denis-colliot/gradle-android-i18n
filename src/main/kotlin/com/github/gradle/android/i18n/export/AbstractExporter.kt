package com.github.gradle.android.i18n.export

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.gradle.android.i18n.conf.Configuration.xmlMapper
import com.github.gradle.android.i18n.model.StringResources
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
}

// TODO: Make private
fun moduleResources(
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

internal val String?.unescapeQuotes: String? get() = this?.replace("\\'", "'")