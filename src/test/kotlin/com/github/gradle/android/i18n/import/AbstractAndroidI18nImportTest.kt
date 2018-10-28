package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.File
import java.nio.file.Paths

/**
 * Abstract layer for plugin import task tests.
 */
abstract class AbstractAndroidI18nImportTest : AbstractUnitTest() {

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    protected lateinit var actualEnFile: File
    protected lateinit var actualFrFile: File
    protected lateinit var actualEsFile: File

    protected lateinit var expectedEnFile: File
    protected lateinit var expectedFrFile: File
    protected lateinit var expectedEsFile: File

    private lateinit var project: Project

    @Before
    fun `set up test`() {
        project = ProjectBuilder.builder().withProjectDir(folder.root).build()
        project.pluginManager.apply("com.github.gradle.android-i18n")

        with(folder.root.absolutePath) {
            actualEnFile = Paths.get(this, "src", "main", "res", "values", "strings.xml").toFile()
            actualFrFile = Paths.get(this, "src", "main", "res", "values-fr", "strings.xml").toFile()
            actualEsFile = Paths.get(this, "src", "main", "res", "values-es", "strings.xml").toFile()
        }

        expectedEnFile = File(resource("/en_strings.xml").path)
        expectedFrFile = File(resource("/fr_strings.xml").path)
        expectedEsFile = File(resource("/es_strings.xml").path)
    }

    protected fun extension(): AndroidI18nPluginExtension {
        return project.extensions.getByType(AndroidI18nPluginExtension::class.java)
    }
}