package com.github.gradle.android.i18n

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Plugin tests.
 */
class PluginTest : AbstractUnitTest() {

    private lateinit var project: Project

    @Before
    fun `set up test`() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.gradle.android-i18n")
    }

    @Test
    fun `should initialize plugin extension & tasks`() {
        assertThat(project.extensions.getByName("androidI18n")).isInstanceOf(AndroidI18nPluginExtension::class.java)
        assertThat(project.tasks.getByName("androidI18nImport")).isNotNull()
        assertThat(project.tasks.getByName("androidI18nExport")).isNotNull()
    }
}