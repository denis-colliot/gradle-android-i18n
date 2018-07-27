package testutil

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.gradle.android.i18n.conf.Configuration
import org.junit.Before
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

abstract class AbstractUnitTest {

    protected lateinit var mapper: XmlMapper

    @Before
    fun before() {
        mapper = Configuration.xmlMapper()
    }

    protected fun resource(file: String): URL {
        return javaClass.getResource(file) ?: error("Failed to load resource '$file'")
    }

    protected inline fun <reified T : Any> readFile(file: String): T {
        BufferedReader(InputStreamReader(resource(file).openStream())).use { reader ->
            return mapper.readValue(reader)
        }
    }
}