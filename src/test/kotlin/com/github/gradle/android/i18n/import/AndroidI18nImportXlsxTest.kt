package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.contentOf
import org.junit.Test
import java.io.FileInputStream

/**
 * Plugin XLSX import task tests.
 */
class AndroidI18nImportXlsxTest : AbstractAndroidI18nImportTest() {

    private val xlsxResource = resource("/input.xlsx")

    @Test
    fun `should use 'FileInputStream' when importing i18n resources from xlsx source`() {
        with(mock<XlsImporter>()) {
            AndroidI18nPluginExtension(mock(), this).apply {
                sourceFile = xlsxResource.path
                importI18nResources()
            }

            verify(this, times(1)).generate(
                    isA<FileInputStream>(),
                    eq("en"))
        }
    }

    @Test
    fun `should import i18n resources from local xlsx source`() {
        extension().apply {
            sourceFile = xlsxResource.path
            importI18nResources()
        }

        assertThat(contentOf(actualEnFile)).isEqualTo(contentOf(expectedEnFile))
        assertThat(contentOf(actualFrFile)).isEqualTo(contentOf(expectedFrFile))
        assertThat(contentOf(actualEsFile)).isEqualTo(contentOf(expectedEsFile))
    }

    @Test
    fun `should overwrite existing i18n resources when importing from local xlsx source`() {

        val extension = extension().apply {
            sourceFile = xlsxResource.path
        }

        // Running import a first time.
        extension.importI18nResources()

        assertThat(contentOf(actualEnFile)).isEqualTo(contentOf(expectedEnFile))
        assertThat(contentOf(actualFrFile)).isEqualTo(contentOf(expectedFrFile))
        assertThat(contentOf(actualEsFile)).isEqualTo(contentOf(expectedEsFile))

        // Running import a second time.
        extension.importI18nResources()

        assertThat(contentOf(actualEnFile)).isEqualTo(contentOf(expectedEnFile))
        assertThat(contentOf(actualFrFile)).isEqualTo(contentOf(expectedFrFile))
        assertThat(contentOf(actualEsFile)).isEqualTo(contentOf(expectedEsFile))
    }
}