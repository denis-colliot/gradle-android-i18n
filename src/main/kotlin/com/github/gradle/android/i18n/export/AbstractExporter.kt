package com.github.gradle.android.i18n.export

import com.github.gradle.android.i18n.deserializeResources
import com.github.gradle.android.i18n.model.*
import com.github.gradle.android.i18n.toProjectData
import org.gradle.api.Project
import java.io.OutputStream

/**
 * Android `xml` string resources exporter.
 *
 * This class implements loading resources from the `*strings.xml` files of the project.
 *
 * The implementation of writing to an output file is left to concrete classes.
 */
abstract class AbstractExporter(private val project: Project) {

    /**
     * Exports the `xml` android string resources to the given output stream.
     */
    abstract fun export(outputStream: OutputStream, defaultLocale: String)

    protected fun loadProjectResources(defaultLocale: String): ProjectData =
        project.deserializeResources(defaultLocale).toProjectData()
}

