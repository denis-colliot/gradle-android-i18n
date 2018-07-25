package com.github.gradle.android.i18n

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Plugin tests.
 */
class AndroidI18nPluginTest {

    private lateinit var project: Project

    @Before
    fun `set up test`() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    fun `should initialize plugin`() {
        project.pluginManager.apply("com.github.gradle.android-i18n")

        assertTrue(project.extensions.getByName("i18n") is AndroidI18nPluginExtension)
        assertNotNull(project.tasks.getByName("i18n"))
    }
}