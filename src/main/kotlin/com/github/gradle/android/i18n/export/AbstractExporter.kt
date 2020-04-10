package com.github.gradle.android.i18n.export

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.gradle.android.i18n.conf.Configuration.xmlMapper
import com.github.gradle.android.i18n.model.StringResources
import org.gradle.api.Project
import java.io.OutputStream
import java.nio.file.Paths

/**
 * Android `xml` string resources exporter.
 */
abstract class AbstractExporter(private val project: Project) {

    private val resFolderPattern = "values(:?-(.*))?".toRegex()

    /**
     * Exports the `xml` android string resources to the given output stream.
     */
    abstract fun export(outputStream: OutputStream, defaultLocale: String)

    protected fun loadProjectResources(defaultLocale: String): List<StringResources> {

        val resources = mutableListOf<StringResources>()

        Paths.get(project.projectDir.absolutePath, "src", "main", "res")
            .toFile()
            .walkTopDown()
            .maxDepth(1)
            .filter { it.isDirectory && resFolderPattern.matches(it.name) }
            .sortedBy { it.path }
            .forEach { directory ->
                val locale = resFolderPattern.matchEntire(directory.name)!!.groupValues[2]
                val resourcesFile = directory.walkTopDown().maxDepth(1).first { file -> file.name == "strings.xml" }
                val stringResources = xmlMapper.readValue<StringResources>(resourcesFile.inputStream())

                val resourceToAdd = if (locale.isBlank()) {
                    stringResources.copy(locale = defaultLocale, defaultLocale = true)
                } else {
                    stringResources.copy(locale = locale, defaultLocale = false)
                }

                resources.add(resourceToAdd)
            }

        return resources
    }
}

internal val String?.unescapeQuotes: String? get() = this?.replace("\\'", "'")