package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.FileNotFoundException

/**
 * Plugin import task common tests.
 */
class AndroidI18nImportCommonTest : AbstractAndroidI18nImportTest() {

    @Test
    fun `should do nothing when importing i18n resources without source file`() {
        mock<XlsImporter>().apply {
            AndroidI18nPluginExtension(mock(), this).apply {
                importI18nResources()
            }
            verify(this, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with empty source file`() {
        mock<XlsImporter>().apply {
            AndroidI18nPluginExtension(mock(), this).apply {
                sourceFile = ""
                importI18nResources()
            }
            verify(this, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should do nothing when importing i18n resources with blank source file`() {
        mock<XlsImporter>().apply {
            AndroidI18nPluginExtension(mock(), this).apply {
                sourceFile = " "
                importI18nResources()
            }
            verify(this, times(0)).generate(any(), any())
        }
    }

    @Test
    fun `should fail when importing i18n resources with an unexisting file`() {
        Assertions.assertThatExceptionOfType(FileNotFoundException::class.java).isThrownBy {
            extension().apply {
                sourceFile = "unexisting_file.xls"
                importI18nResources()
            }
        }
    }

    @Test
    fun `should fail when importing i18n resources with an unsupported file type`() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException::class.java).isThrownBy {
            extension().apply {
                sourceFile = resource("/input.unsupported").path
                importI18nResources()
            }
        }
    }

    @Test
    fun `should fail when importing i18n resources with duplicated key`() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            extension().apply {
                sourceFile = resource("/input_with_duplicated_key.xls").path
                importAllSheets = true // Duplicated key is in second sheet
                importI18nResources()
            }
        }
    }

    @Test
    fun `should fail when importing i18n resources with missing key`() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            extension().apply {
                sourceFile = resource("/input_with_missing_key.xls").path
                importI18nResources()
            }
        }
    }
}