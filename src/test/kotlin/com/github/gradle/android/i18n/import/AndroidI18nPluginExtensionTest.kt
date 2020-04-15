package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.github.gradle.android.i18n.export.XlsExporter
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.FileInputStream

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
            val name = file.name
            name.startsWith("i18n_") && name.endsWith(".xlsx")
        }
    }
}