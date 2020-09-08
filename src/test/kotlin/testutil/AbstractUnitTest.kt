package testutil

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.gradle.android.i18n.conf.Configuration
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL

/**
 * Unit tests base class providing utility methods.
 */
abstract class AbstractUnitTest {

    protected val mapper = Configuration.xmlMapper

    protected fun resource(file: String): URL {
        return javaClass.getResource(file) ?: error("Failed to load resource '$file'")
    }

    protected inline fun <reified T : Any> readFile(file: String): T {
        BufferedReader(InputStreamReader(resource(file).openStream())).use { reader ->
            return mapper.readValue(reader)
        }
    }

    /**
     * Attach a new child project
     */
    protected fun Project.attachNewChildProject(moduleDir: File): Project {
        return ProjectBuilder.builder()
            .withProjectDir(moduleDir)
            .withParent(this)
            .withName(moduleDir.name)
            .build()
    }
}