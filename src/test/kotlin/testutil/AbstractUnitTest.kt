package testutil

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.gradle.android.i18n.di.AppModule
import org.junit.Before
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.AutoCloseKoinTest
import java.io.BufferedReader
import java.io.InputStreamReader

abstract class AbstractUnitTest : AutoCloseKoinTest() {

    protected val mapper: XmlMapper by inject()

    protected inline fun <reified T : Any> readFile(file: String): T {
        val inputStream = this::class.java.getResourceAsStream(file)
                ?: error("Failed to load resource: $file")

        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            return mapper.readValue(reader)
        }
    }

    @Before
    fun before() {
        startKoin(listOf(AppModule.applicationModule))
    }
}