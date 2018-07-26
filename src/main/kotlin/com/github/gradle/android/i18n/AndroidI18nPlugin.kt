package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.di.AppModule
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.startKoin

/**
 * Plugin entry point referenced in `META-INF` directory.
 */
class AndroidI18nPlugin : Plugin<Project> {

    override fun apply(project: Project?) {
        try {

            startKoin(listOf(AppModule.applicationModule))

            project?.let {
                project.extensions.create("i18n", AndroidI18nPluginExtension::class.java, project)

                project.tasks.let {
                    it.create("i18n").apply {
                        doLast {
                            project.logger.info("Android i18n gradle plugin initialized")
                        }
                    }
                }
            }

        } finally {
            closeKoin()
        }
    }
}