package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.File
import java.io.FileNotFoundException
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

    // region Common import tests

    @Test
    fun `should do nothing when importing i18n resources without source file`() {
        mock<XlsImporter>().apply {
            AndroidI18nPluginExtension(this).apply {
                importI18nResources()
            }
            verify(this, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with empty source file`() {
        mock<XlsImporter>().apply {
            AndroidI18nPluginExtension(this).apply {
                sourceFile = ""
                importI18nResources()
            }
            verify(this, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with blank source file`() {
        mock<XlsImporter>().apply {
            AndroidI18nPluginExtension(this).apply {
                sourceFile = " "
                importI18nResources()
            }
            verify(this, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should fail when importing i18n resources with an unexisting file`() {
        assertThatExceptionOfType(FileNotFoundException::class.java).isThrownBy {
            extension().apply {
                sourceFile = "unexisting_file.xls"
                importI18nResources()
            }
        }
    }

    @Test
    fun `should fail when importing i18n resources with an unsupported file type`() {
        assertThatExceptionOfType(UnsupportedOperationException::class.java).isThrownBy {
            extension().apply {
                sourceFile = resource("/input.unsupported").path
                importI18nResources()
            }
        }
    }

    // endregion
}