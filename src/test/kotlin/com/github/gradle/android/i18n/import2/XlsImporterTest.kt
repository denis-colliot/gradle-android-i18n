package com.github.gradle.android.i18n.import2

import com.github.gradle.android.i18n.import.ImportConfig
import com.github.gradle.android.i18n.import.XlsImporter
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.File
import java.nio.file.Paths

class XlsImporterTest : AbstractUnitTest() {

    @get:Rule
    val tmpDirRule = TemporaryFolder()

    @Test
    fun `should generate string resources for multi module project`() {

        // Given
        resource("/xls-import-multi/input.xlsx").openStream().use { inputStream ->
            // Given
            val projectDirMaster = File("src/test/resources/import_multi")
            val projectDir = tmpDirRule.newFolder("import_multi")
            projectDirMaster.copyRecursively(projectDir)
            val rootProject = ProjectBuilder.builder().withProjectDir(projectDir).build()
            val appModuleDir = File(projectDir, "app")
            rootProject.attachNewChildProject(appModuleDir)
            val featuresDir = File(projectDir, "features")
            val featuresProject = rootProject.attachNewChildProject(featuresDir)
            val featureOneDir = File(featuresDir, "feature-one")
            featuresProject.attachNewChildProject(featureOneDir)
            val libDir = File(projectDir, "lib")
            rootProject.attachNewChildProject(libDir)
            val importer = XlsImporter(rootProject)
            val config = ImportConfig("fr", false, "input.*".toRegex())

            // When
            importer.generate(inputStream, config)

            // Then
            // Check that generated files are in project dir
            // And that their content is as expected
            listOf(
                projectDir.resolve("app").path to "app_strings.xml",
                projectDir.resolve("features/feature-one").path to "feature_one_strings.xml",
                projectDir.resolve("lib").path to "lib_strings.xml"
            ).forEach { (modulePath, expectedStringsFileName) ->
                listOf("values", "values-en", "values-es").forEach { valuesDirName: String ->

                    // Check generated file path
                    val actualStringsFile = Paths.get(
                        modulePath, "src", "main", "res", valuesDirName, expectedStringsFileName
                    ).toFile()
                    assertTrue("${actualStringsFile.path} should exist", actualStringsFile.exists())
                    assertTrue("${actualStringsFile.path} should be a file", actualStringsFile.isFile)

                    // Check generated file content
                    val referenceStringsFileName = "expected-$valuesDirName-strings.xml"
                    val referenceStringsFile = Paths.get(modulePath, referenceStringsFileName).toFile()
                    val referenceStringsFileContent = referenceStringsFile.readText()
                    val actualStringsFileContent = actualStringsFile.readText()
                    assertEquals(
                        "${referenceStringsFile.path} and ${actualStringsFile.path} should be the same",
                        referenceStringsFileContent,
                        actualStringsFileContent
                    )
                }
            }
        }
    }
}