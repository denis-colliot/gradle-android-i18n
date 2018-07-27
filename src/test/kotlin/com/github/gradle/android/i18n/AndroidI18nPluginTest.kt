package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.generator.Xls2XmlGenerator
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import testutil.AbstractUnitTest
import java.io.FileInputStream

/**
 * Plugin tests.
 */
class AndroidI18nPluginTest : AbstractUnitTest() {

    private lateinit var project: Project

    @Before
    fun `set up test`() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.gradle.android-i18n")
    }

    @Test
    fun `should initialize plugin extension & tasks`() {
        assertTrue(project.extensions.getByName("androidI18n") is AndroidI18nPluginExtension)
        assertNotNull(project.tasks.getByName("androidI18nImport"))
        assertNotNull(project.tasks.getByName("androidI18nExport"))
    }

    @Test
    fun `should import i18n resources for an xls source file`() {
        val xls2XmlGenerator = mock<Xls2XmlGenerator>()
        val extension = AndroidI18nPluginExtension(xls2XmlGenerator)
        extension.sourceFile = resource("/input.xls").path

        extension.importI18nResources()

        verify(xls2XmlGenerator, times(1)).generate(check { assertTrue(it is FileInputStream) })
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `should fail when importing i18n resources for an unsupported file type`() {
        val xls2XmlGenerator = mock<Xls2XmlGenerator>()
        val extension = AndroidI18nPluginExtension(xls2XmlGenerator)
        extension.sourceFile = "myfile.xlsx"
        extension.importI18nResources()
    }
}