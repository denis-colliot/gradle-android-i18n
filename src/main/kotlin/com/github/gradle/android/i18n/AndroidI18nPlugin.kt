package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.import.XlsImporter
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin entry point referenced in `META-INF` directory.
 */
class AndroidI18nPlugin : Plugin<Project> {

    override fun apply(project: Project?) {
        project?.let {

            val extension = project.extensions.create("androidI18n",
                    AndroidI18nPluginExtension::class.java,
                    XlsImporter(project))

            project.tasks.let {
                it.create("androidI18nImport").apply {
                    doLast {
                        project.logger.info("Importing android i18n resources")
                        extension.importI18nResources()
                    }
                }
            }

            project.tasks.let {
                it.create("androidI18nExport").apply {
                    doLast {
                        project.logger.info("Exporting android i18n resources")
                        extension.exportI18nResources()
                    }
                }
            }
        }
    }
}