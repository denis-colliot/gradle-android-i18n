package com.github.gradle.android.i18n.import

import org.apache.tools.ant.util.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths

/**
 * Plugin import task tests.
 */
class ImportTaskTest : AbstractUnitTest() {

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    private val fileUtils = FileUtils.getFileUtils()

    private lateinit var actualEnFile: File
    private lateinit var actualFrFile: File
    private lateinit var actualEsFile: File

    private lateinit var expectedEnFile: File
    private lateinit var expectedFrFile: File
    private lateinit var expectedEsFile: File

    @Before
    fun `set up test`() {
        val rootDir = folder.root.absolutePath
        actualEnFile = Paths.get(rootDir, "src", "main", "res", "values", "strings.xml").toFile()
        actualFrFile = Paths.get(rootDir, "src", "main", "res", "values-fr", "strings.xml").toFile()
        actualEsFile = Paths.get(rootDir, "src", "main", "res", "values-es", "strings.xml").toFile()

        expectedEnFile = File(resource("/en_strings.xml").path)
        expectedFrFile = File(resource("/fr_strings.xml").path)
        expectedEsFile = File(resource("/es_strings.xml").path)
    }

    /**
     * Initialize the [folder] with a test gradle project.
     *
     * @param sourceFile The source file path set up in `androidI18nImport` task.
     * @param defaultLocale The default locale set up in `androidI18nImport` task.
     * @return The [GradleRunner] set up to run `androidI18nImport` task.
     */
    private fun setUpImportTask(sourceFile: String, defaultLocale: String = "en"): GradleRunner {

        // build.gradle
        folder.newFile("build.gradle").writeText("""
            plugins {
                id 'com.github.gradle.android-i18n'
            }

            androidI18n {
                sourceFile = '$sourceFile'
                defaultLocale = '$defaultLocale'
            }
        """.trimIndent())

        // gradle.properties
        folder.newFile("gradle.properties").writeText("""
            androidI18n.jcifs.smb.client.dfs.disabled=true
            androidI18n.jcifs.smb.client.responseTimeout=5000
            androidI18n.jcifs.util.loglevel=1
        """.trimIndent())

        return GradleRunner.create()
                .withProjectDir(folder.root)
                .withPluginClasspath()
                .withDebug(true)
                .withArguments("androidI18nImport")
    }

    @Test
    fun `should fail when importing i18n resources with an unexisting file`() {
        val buildResult = setUpImportTask("unexisting_file.xlsx").buildAndFail()
        assertNotNull(buildResult)
        assertNotNull(buildResult.output)
        assertTrue(buildResult.output.contains(FileNotFoundException::class.java.simpleName))
    }

    @Test
    fun `should fail when importing i18n resources with an unsupported file type`() {
        val sourceFile = resource("/input.xlsx").path
        val buildResult = setUpImportTask(sourceFile).buildAndFail()
        assertNotNull(buildResult)
        assertNotNull(buildResult.output)
        assertTrue(buildResult.output.contains("Source file '$sourceFile' is not supported"))
    }

    @Test
    fun `should import i18n resources from local xls source`() {
        setUpImportTask(resource("/input.xls").path).build()

        assertTrue(fileUtils.contentEquals(actualEnFile, expectedEnFile, true))
        assertTrue(fileUtils.contentEquals(actualFrFile, expectedFrFile, true))
        assertTrue(fileUtils.contentEquals(actualEsFile, expectedEsFile, true))
    }

    @Test
    fun `should overwrite existing i18n resources when importing from local xls source`() {

        val importTask = setUpImportTask(resource("/input.xls").path)

        // Running import a first time.
        importTask.build()

        assertTrue(fileUtils.contentEquals(actualEnFile, expectedEnFile, true))
        assertTrue(fileUtils.contentEquals(actualFrFile, expectedFrFile, true))
        assertTrue(fileUtils.contentEquals(actualEsFile, expectedEsFile, true))

        // Running import a second time.
        importTask.build()

        assertTrue(fileUtils.contentEquals(actualEnFile, expectedEnFile, true))
        assertTrue(fileUtils.contentEquals(actualFrFile, expectedFrFile, true))
        assertTrue(fileUtils.contentEquals(actualEsFile, expectedEsFile, true))
    }

    @Test
    @Ignore("Requires remote directory access")
    fun `should import i18n resources from remote samba xls source`() {
        setUpImportTask("smb://RATP;<login>:<pwd>@urbanbox.info.ratp/sit-cps-ivs/Domaine Agile/" +
                "Appli RATP/Android/Application RATP V3/Ressources/Traductions/i18n.xls").build()

        assertTrue(expectedEnFile.exists())
        assertTrue(expectedEnFile.length() > 0)

        assertTrue(expectedFrFile.exists())
        assertTrue(expectedFrFile.length() > 0)

        assertTrue(expectedEsFile.exists())
        assertTrue(expectedEsFile.length() > 0)
    }

    @Test
    @Ignore("Requires remote directory access")
    fun `should import i18n resources from remote windows xls source`() {
        setUpImportTask("\\\\urbanbox.info.ratp\\sit-cps-ivs\\Domaine Agile\\Appli RATP\\Android\\" +
                "Application RATP V3\\Ressources\\Traductions\\i18n.xls").build()

        assertTrue(expectedEnFile.exists())
        assertTrue(expectedEnFile.length() > 0)

        assertTrue(expectedFrFile.exists())
        assertTrue(expectedFrFile.length() > 0)

        assertTrue(expectedEsFile.exists())
        assertTrue(expectedEsFile.length() > 0)
    }
}