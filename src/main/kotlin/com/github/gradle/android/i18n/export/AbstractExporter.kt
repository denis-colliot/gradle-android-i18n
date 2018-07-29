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

    private val resFolderPattern = Regex("values(:?-(.*))?")

    /**
     * Exports the `xml` android string resources to the given output stream.
     */
    abstract fun export(outputStream: OutputStream, defaultLocale: String)

    protected fun loadProjectResources(defaultLocale: String): List<StringResources> {

        val resources = mutableListOf<StringResources>()

        Paths.get(project.projectDir.absolutePath, "src", "main", "res").toFile()
                .walkTopDown()
                .maxDepth(1)
                .filter { it.isDirectory && resFolderPattern.matches(it.name) }
                .forEach {
                    val locale = resFolderPattern.matchEntire(it.name)!!.groupValues[1]
                    val resourcesFile = it.walkTopDown().maxDepth(1).first { it.name == "strings.xml" }
                    val stringResources = xmlMapper.readValue<StringResources>(resourcesFile.inputStream())

                    if (locale.isBlank()) {
                        stringResources.locale = defaultLocale
                        stringResources.defaultLocale = true
                    } else {
                        stringResources.locale = locale
                        stringResources.defaultLocale = false
                    }

                    resources.add(stringResources)
                }

        return resources
    }
}