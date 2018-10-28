package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import testutil.AbstractUnitTest
import java.io.FileInputStream

/**
 * Plugin extension tests regarding import task methods.
 */
class PluginExtensionTest : AbstractUnitTest() {

    @Test
    fun `should use 'FileInputStream' when importing i18n resources from xls source`() {
        val xls2XmlGenerator = mock<XlsImporter>()

        AndroidI18nPluginExtension(mock(), xls2XmlGenerator).apply {
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
            AndroidI18nPluginExtension(mock(), importer).importI18nResources()
            verify(importer, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with empty source file`() {
        mock<XlsImporter>().let { importer ->
            AndroidI18nPluginExtension(mock(), importer).apply {
                sourceFile = ""
                importI18nResources()
            }
            verify(importer, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with blank source file`() {
        mock<XlsImporter>().let { importer ->
            AndroidI18nPluginExtension(mock(), importer).apply {
                sourceFile = " "
                importI18nResources()
            }
            verify(importer, times(0)).generate(any(), any())
        }
    }
}