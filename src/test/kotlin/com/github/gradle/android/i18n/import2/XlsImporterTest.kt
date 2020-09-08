package com.github.gradle.android.i18n.import2

import com.github.gradle.android.i18n.import.ImportConfig
import com.github.gradle.android.i18n.import.XlsImporter
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import testutil.AbstractUnitTest
import java.io.File
import java.nio.file.Paths

class XlsImporterTest : AbstractUnitTest() {

    @Test
    fun `should generate string resources for multi module project`() {

        // Given
        resource("/xls-import-multi/input.xlsx").openStream().use { inputStream ->
            // Given
            val projectDir = File("src/test/resources/import_multi")
            val rootProject = ProjectBuilder.builder().withProjectDir(projectDir).build()
            val appModuleDir = File(projectDir, "app")
            rootProject.attachNewChildProject(appModuleDir)
            val featuresDir = File(projectDir, "features")
            val featuresProject = rootProject.attachNewChildProject(featuresDir)
            val featureOneDir = File(featuresDir, "feature1")
            featuresProject.attachNewChildProject(featureOneDir)
            val libDir = File(projectDir, "lib")
            rootProject.attachNewChildProject(libDir)
            val importer = XlsImporter(rootProject)
            val config = ImportConfig("fr", false, "input.*".toRegex())

            // When
            importer.generate(inputStream, config)

            // Then
            listOf(appModuleDir, featureOneDir, libDir).forEach { moduleDir: File ->
                listOf("values", "values-en", "values-es").forEach { valuesDirName: String ->

                    val actualStringsFile = Paths.get(moduleDir.path, "src", "main", "res", valuesDirName, "strings.xml").toFile()
                    assertTrue(actualStringsFile.exists())
                    assertTrue(actualStringsFile.isFile)

                    val expectedStringsFileName = "expected-$valuesDirName-strings.xml"
                    val expectedStringsFile = Paths.get(moduleDir.path, expectedStringsFileName).toFile()
                    val expectedStringsFileContent = expectedStringsFile.readText()
                    val actualStringsFileContent = actualStringsFile.readText()
                    assertEquals(
                        "${expectedStringsFile.path} and ${actualStringsFile.path} should be the same",
                        expectedStringsFileContent,
                        actualStringsFileContent
                    )
                }
            }
        }
    }
}