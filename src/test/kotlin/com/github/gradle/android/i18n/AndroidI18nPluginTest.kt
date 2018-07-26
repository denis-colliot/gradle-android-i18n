package com.github.gradle.android.i18n

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Plugin tests.
 */
class AndroidI18nPluginTest : AbstractUnitTest() {

    private lateinit var project: Project

    @Before
    fun `set up test`() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    fun `should initialize plugin extension & tasks`() {
        project.pluginManager.apply("com.github.gradle.android-i18n")

        val extension = project.extensions.getByName("androidI18n")
        assertTrue(extension is AndroidI18nPluginExtension)
        assertNotNull((extension as AndroidI18nPluginExtension).project)

        assertNotNull(project.tasks.getByName("androidI18nImport"))
        assertNotNull(project.tasks.getByName("androidI18nExport"))
    }
}