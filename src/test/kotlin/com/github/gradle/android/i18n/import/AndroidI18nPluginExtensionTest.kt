package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.github.gradle.android.i18n.export.XlsExporter
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Condition
import org.gradle.api.Project
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
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

    @Test
    fun `should use 'FileInputStream' when importing i18n resources from xls source`() {
        val xls2XmlGenerator = mock<XlsImporter>()

        AndroidI18nPluginExtension(mock(), xls2XmlGenerator, mock()).apply {
            sourceFile = resource("/xls-import/input.xls").path
            importI18nResources()
        }

        verify(xls2XmlGenerator, times(1)).generate(
                check { assertThat(it).isInstanceOf(FileInputStream::class.java) },
                check {
                    assertThat(it.defaultLocale).isEqualTo("en")
                    assertThat(it.allSheets).isFalse()
                    assertThat(it.sheetNameRegex.pattern).isEqualTo("^.*$")
                })
    }

    @Test
    fun `should do nothing when importing i18n resources without source file`() {
        mock<XlsImporter>().let { importer ->
            AndroidI18nPluginExtension(mock(), importer, mock()).importI18nResources()
            verify(importer, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with empty source file`() {
        mock<XlsImporter>().let { importer ->
            AndroidI18nPluginExtension(mock(), importer, mock()).apply {
                sourceFile = ""
                importI18nResources()
            }
            verify(importer, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with blank source file`() {
        mock<XlsImporter>().let { importer ->
            AndroidI18nPluginExtension(mock(), importer, mock()).apply {
                sourceFile = " "
                importI18nResources()
            }
            verify(importer, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should export resources to output file`() {

        // Given
        val project: Project = mock()
        val importer: XlsImporter = mock()
        val exporter: XlsExporter = mock()
        val extension = AndroidI18nPluginExtension(project, importer, exporter)
        extension.defaultLocale = "en"
        val buildDir = temporaryFolder.newFolder()
        given(project.buildDir).willReturn(buildDir)

        // When
        extension.exportI18nResources()

        // Then
        then(exporter).should().export(any(), eq("en"))
        assertThat(buildDir).isDirectoryContaining { file ->
            file.name
            file.name.matches(EXPECTED_OUTPUT_FILE_PATTERN.toRegex())
        }
    }

    @Test
    fun `should remove previous output files`() {

        // Setup
        val project: Project = mock()
        val importer: XlsImporter = mock()
        val exporter: XlsExporter = mock()
        val extension = AndroidI18nPluginExtension(project, importer, exporter)
        extension.defaultLocale = "en"

        // Given
        val buildDir = temporaryFolder.newFolder()
        Paths.get(buildDir.path, "i18n_file1.xlsx").toFile().createNewFile()
        Paths.get(buildDir.path, "i18n_file2.xlsx").toFile().createNewFile()
        Paths.get(buildDir.path, "other_file.txt").toFile().createNewFile()
        given(project.buildDir).willReturn(buildDir)

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

    companion object {
        /**
         * Expected output file pattern: `i18n_YYYY-MM-DD_HH-MM-SS.xlsx`
         */
        private const val EXPECTED_OUTPUT_FILE_PATTERN =
            "^i18n_[\\d]{4}-[\\d]{2}-[\\d]{2}_[\\d]{2}-[\\d]{2}-[\\d]{2}\\.xlsx$"
    }
}