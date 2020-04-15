package com.github.gradle.android.i18n

import com.github.gradle.android.i18n.export.XlsExporter
import com.github.gradle.android.i18n.import.XlsImporter
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.gradle.api.Project
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.function.Predicate

/**
 * Plugin extension tests regarding import task methods.
 */
class AndroidI18nPluginExtensionTest : AbstractUnitTest() {

    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    // region Import verifications

    @Test
    @Suppress("UsePropertyAccessSyntax")
    fun `should use 'FileInputStream' when importing i18n resources from xls source`() {
        // Given
        val mockImporter = mock<XlsImporter>()
        val extension = AndroidI18nPluginExtension(mock(), mockImporter, mock()).apply {
            sourceFile = resource("/xls-import/input.xls").path
        }

        // When
        extension.importI18nResources()

        // Then
        then(mockImporter).should().generate(
            check { assertThat(it).isInstanceOf(FileInputStream::class.java) },
            check {
                assertThat(it.defaultLocale).isEqualTo("en")
                assertThat(it.allSheets).isFalse()
                assertThat(it.sheetNameRegex.pattern).isEqualTo("^.*$")
            })
    }

    @Test
    fun `should do nothing when importing i18n resources without source file`() {
        // Given
        val mockImporter = mock<XlsImporter>()
        val extension = AndroidI18nPluginExtension(mock(), mockImporter, mock())

        // When
        extension.importI18nResources()

        // Then
        then(mockImporter).should(never()).generate(any(), any())
    }

    @Test
    fun `should do nothing when importing i18n resources with empty source file`() {
        // Given
        val mockImporter = mock<XlsImporter>()
        val extension = AndroidI18nPluginExtension(mock(), mockImporter, mock()).apply {
            sourceFile = ""
        }

        // When
        extension.importI18nResources()

        // Then
        then(mockImporter).should(never()).generate(any(), any())
    }

    @Test
    fun `should do nothing when importing i18n resources with blank source file`() {
        // Given
        val mockImporter = mock<XlsImporter>()
        val extension = AndroidI18nPluginExtension(mock(), mockImporter, mock()).apply {
            sourceFile = " "
        }

        // When
        extension.importI18nResources()

        // Then
        then(mockImporter).should(never()).generate(any(), any())
    }

    // endregion

    // region Export verifications

    @Test
    @Suppress("UsePropertyAccessSyntax")
    fun `should create output file parent folders when they don't already exist`() {
        // Given
        val mockProject = mock<Project>()
        val mockExporter = mock<XlsExporter>()
        val extension = AndroidI18nPluginExtension(mockProject, mock(), mockExporter).apply {
            defaultLocale = "en"
        }
        val buildDir = File(temporaryFolder.root, "nonExistingFolder")
        given(mockProject.buildDir).willReturn(buildDir)

        // When
        extension.exportI18nResources()

        // Then
        then(mockExporter).should().export(any(), eq("en"))
        assertThat(buildDir).isDirectory()
        assertThat(buildDir).isDirectoryContaining { file ->
            file.name
            file.name.matches(EXPECTED_OUTPUT_FILE_PATTERN.toRegex())
        }
    }

    @Test
    fun `should export resources to output file`() {
        // Given
        val mockProject = mock<Project>()
        val mockExporter = mock<XlsExporter>()
        val extension = AndroidI18nPluginExtension(mockProject, mock(), mockExporter).apply {
            defaultLocale = "en"
        }
        val buildDir = temporaryFolder.newFolder()
        given(mockProject.buildDir).willReturn(buildDir)

        // When
        extension.exportI18nResources()

        // Then
        then(mockExporter).should().export(any(), eq("en"))
        assertThat(buildDir).isDirectoryContaining { file ->
            file.name
            file.name.matches(EXPECTED_OUTPUT_FILE_PATTERN.toRegex())
        }
    }

    @Test
    fun `should remove previous output files`() {
        // Given
        val mockProject = mock<Project>()
        val mockExporter = mock<XlsExporter>()
        val extension = AndroidI18nPluginExtension(mockProject, mock(), mockExporter).apply {
            defaultLocale = "en"
        }
        val buildDir = temporaryFolder.newFolder().also {
            Paths.get(it.path, "i18n_file1.xlsx").toFile().createNewFile()
            Paths.get(it.path, "i18n_file2.xlsx").toFile().createNewFile()
            Paths.get(it.path, "other_file.txt").toFile().createNewFile()
        }
        given(mockProject.buildDir).willReturn(buildDir)

        // When
        extension.exportI18nResources()

        // Then
        assertThat(buildDir)
            .isDirectoryContaining { file ->
                file.name.matches(EXPECTED_OUTPUT_FILE_PATTERN.toRegex())
            }.isDirectoryContaining { file ->
                file.name == "other_file.txt"
            }.has(
                Condition(
                    Predicate { it?.listFiles()?.size == 2 },
                    "Only 2 elements"
                )
            )
    }

    // endregion

    companion object {
        /**
         * Expected output file pattern: `i18n_YYYY-MM-DD_HH-MM-SS.xlsx`
         */
        private const val EXPECTED_OUTPUT_FILE_PATTERN =
            "^i18n_[\\d]{4}-[\\d]{2}-[\\d]{2}_[\\d]{2}-[\\d]{2}-[\\d]{2}\\.xlsx$"
    }
}