package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.contentOf
import org.junit.Ignore
import org.junit.Test
import java.io.FileInputStream

/**
 * Plugin XLS import task tests.
 */
class AndroidI18nImportXlsTest : AbstractAndroidI18nImportTest() {

    private val xlsResource = resource("/xls-import/input.xls")

    @Test
    fun `should use 'FileInputStream' when importing i18n resources from xls source`() {
        with(mock<XlsImporter>()) {
            AndroidI18nPluginExtension(mock(), this).apply {
                sourceFile = xlsResource.path
                importAllSheets = true
                importSheetNameRegex = "^input\\d$"
                importI18nResources()
            }

            verify(this, times(1)).generate(
                    isA<FileInputStream>(),
                    check {
                        assertThat(it.defaultLocale).isEqualTo("en")
                        assertThat(it.allSheets).isTrue()
                        assertThat(it.sheetNameRegex.pattern).isEqualTo("^input\\d$")
                    })
        }
    }

    @Test
    fun `should import i18n resources from local xls source`() {
        extension().apply {
            sourceFile = xlsResource.path
            importAllSheets = true
            importSheetNameRegex = "^input\\d$"
            importI18nResources()
        }

        assertThat(contentOf(actualEnFile)).isEqualTo(contentOf(expectedEnFile))
        assertThat(contentOf(actualFrFile)).isEqualTo(contentOf(expectedFrFile))
        assertThat(contentOf(actualEsFile)).isEqualTo(contentOf(expectedEsFile))
    }

    @Test
    fun `should overwrite existing i18n resources when importing from local xls source`() {

        val extension = extension().apply {
            sourceFile = xlsResource.path
            importAllSheets = true
            importSheetNameRegex = "^input\\d$"
        }

        // Runnint import a first time.
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

    @Test
    @Ignore("Requires remote directory access")
    fun `should import i18n resources from remote samba xls source`() {
        extension().apply {
            sourceFile = "smb://<domain>;<login>:<pwd>@<host>/path/to/file.xls"
            importI18nResources()
        }

        assertThat(expectedEnFile.exists()).isTrue()
        assertThat(expectedEnFile.length() > 0).isTrue()

        assertThat(expectedFrFile.exists()).isTrue()
        assertThat(expectedFrFile.length() > 0).isTrue()

        assertThat(expectedEsFile.exists()).isTrue()
        assertThat(expectedEsFile.length() > 0).isTrue()
    }

    @Test
    @Ignore("Requires remote directory access")
    fun `should import i18n resources from remote windows xls source`() {
        extension().apply {
            sourceFile = "\\\\<host>\\path\\to\\file.xls"
            importI18nResources()
        }

        assertThat(expectedEnFile.exists()).isTrue()
        assertThat(expectedEnFile.length() > 0).isTrue()

        assertThat(expectedFrFile.exists()).isTrue()
        assertThat(expectedFrFile.length() > 0).isTrue()

        assertThat(expectedEsFile.exists()).isTrue()
        assertThat(expectedEsFile.length() > 0).isTrue()
    }
}