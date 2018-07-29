package com.github.gradle.android.i18n.import

import com.github.gradle.android.i18n.AndroidI18nPluginExtension
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.apache.tools.ant.util.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.file.Paths

/**
 * Plugin import task tests.
 */
class AndroidI18nImportTest : AbstractUnitTest() {

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    private val fileUtils = FileUtils.getFileUtils()

    private lateinit var actualFrFile: File
    private lateinit var actualEnFile: File

    private lateinit var expectedFrFile: File
    private lateinit var expectedEnFile: File

    private lateinit var project: Project

    @Before
    fun `set up test`() {
        project = ProjectBuilder.builder().withProjectDir(folder.root).build()
        project.pluginManager.apply("com.github.gradle.android-i18n")

        val rootDir = folder.root.absolutePath
        actualFrFile = Paths.get(rootDir, "src", "main", "res", "values-fr", "strings.xml").toFile()
        actualEnFile = Paths.get(rootDir, "src", "main", "res", "values", "strings.xml").toFile()

        expectedFrFile = File(resource("/fr_strings.xml").path)
        expectedEnFile = File(resource("/en_strings.xml").path)
    }

    @Test
    fun `should use 'FileInputStream' when importing i18n resources from xls source`() {
        val xls2XmlGenerator = mock<XlsImporter>()

        AndroidI18nPluginExtension(xls2XmlGenerator).let {
            it.sourceFile = resource("/input.xls").path
            it.importI18nResources()
        }

        verify(xls2XmlGenerator, times(1)).generate(
                check { assertTrue(it is FileInputStream) },
                check { assertEquals("en", it) })
    }

    @Test(expected = FileNotFoundException::class)
    fun `should fail when importing i18n resources without source file`() {
        extension().importI18nResources()
    }

    @Test(expected = FileNotFoundException::class)
    fun `should fail when importing i18n resources with empty source file`() {
        extension().let {
            it.sourceFile = ""
            it.importI18nResources()
        }
    }

    @Test(expected = FileNotFoundException::class)
    fun `should fail when importing i18n resources with an unexisting file`() {
        extension().let {
            it.sourceFile = "myfile.xlsx"
            it.importI18nResources()
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `should fail when importing i18n resources with an unsupported file type`() {
        extension().let {
            it.sourceFile = resource("/input.xlsx").path
            it.importI18nResources()
        }
    }

    @Test
    fun `should import i18n resources from local xls source`() {
        extension().let {
            it.sourceFile = resource("/input.xls").path
            it.importI18nResources()
        }

        assertTrue(fileUtils.contentEquals(actualFrFile, expectedFrFile, true))
        assertTrue(fileUtils.contentEquals(actualEnFile, expectedEnFile, true))
    }

    @Test
    @Ignore("Requires remote directory access")
    fun `should import i18n resources from remote samba xls source`() {
        extension().let {
            it.sourceFile = "smb://RATP;<login>:<pwd>@urbanbox.info.ratp/sit-cps-ivs/Domaine Agile/" +
                    "Appli RATP/Android/Application RATP V3/Ressources/Traductions/i18n.xls"
            it.importI18nResources()
        }

        assertTrue(expectedFrFile.exists())
        assertTrue(expectedFrFile.length() > 0)

        assertTrue(expectedEnFile.exists())
        assertTrue(expectedEnFile.length() > 0)
    }

    @Test
    @Ignore("Requires remote directory access")
    fun `should import i18n resources from remote windows xls source`() {
        extension().let {
            it.sourceFile = "\\\\urbanbox.info.ratp\\sit-cps-ivs\\Domaine Agile\\Appli RATP\\Android\\" +
                    "Application RATP V3\\Ressources\\Traductions\\i18n.xls"
            it.importI18nResources()
        }

        assertTrue(expectedFrFile.exists())
        assertTrue(expectedFrFile.length() > 0)

        assertTrue(expectedEnFile.exists())
        assertTrue(expectedEnFile.length() > 0)
    }

    private fun extension(): AndroidI18nPluginExtension {
        return project.extensions.getByType(AndroidI18nPluginExtension::class.java)
    }
}