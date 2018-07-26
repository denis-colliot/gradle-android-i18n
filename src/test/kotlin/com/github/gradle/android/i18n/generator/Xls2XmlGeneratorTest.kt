package com.github.gradle.android.i18n.generator

import org.apache.tools.ant.util.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import testutil.AbstractUnitTest
import java.io.File
import java.nio.file.Paths

/**
 * Unit tests of component [Xls2XmlGenerator].
 */
class Xls2XmlGeneratorTest : AbstractUnitTest() {

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    private val fileUtils = FileUtils.getFileUtils()

    private lateinit var xls2XmlGenerator: Xls2XmlGenerator

    private lateinit var actualFrFile: File
    private lateinit var actualEnFile: File

    private lateinit var expectedFrFile: File
    private lateinit var expectedEnFile: File

    @Before
    fun setUp() {
        val project = ProjectBuilder.builder().withProjectDir(folder.root).build()

        val rootDir = folder.root.absolutePath
        actualFrFile = Paths.get(rootDir, "src", "main", "res", "values-fr", "strings.xml").toFile()
        actualEnFile = Paths.get(rootDir, "src", "main", "res", "values", "strings.xml").toFile()

        expectedFrFile = File(resource("/fr_strings.xml").path)
        expectedEnFile = File(resource("/en_strings.xml").path)

        xls2XmlGenerator = Xls2XmlGenerator(project)
    }

    @Test
    fun `should properly generate XML resources from XLS source`() {

        val sourceFile = File(resource("/input.xls").toURI()).absolutePath
        xls2XmlGenerator.generate(File(sourceFile).inputStream())

        assertTrue(fileUtils.contentEquals(actualFrFile, expectedFrFile, true))
        assertTrue(fileUtils.contentEquals(actualEnFile, expectedEnFile, true))
    }
}