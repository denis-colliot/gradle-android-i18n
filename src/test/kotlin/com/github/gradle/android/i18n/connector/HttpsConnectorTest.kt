package com.github.gradle.android.i18n.connector

import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.charset.Charset

class HttpsConnectorTest {

    @Test
    fun `should download i18n file`() {

        val protocol = "https"
        val path = "usinedigitaleratp.atlassian.net/wiki/download/attachments/303202427/i18n.xls?api=v2"
        val user = "christophe.pele@ratpsmartsystems.com"
        val encodedUser = URLEncoder.encode(user, Charset.defaultCharset().name())
        val pass = "2NL9i2ImweAABL75P0Ar687C"
        val url = "$protocol://$encodedUser:$pass@$path"
        println("URL: $url")

        val connector = HttpsConnector(url)

        val content = connector.inputStream.readBytes()

        File("i18n.xls").delete()
        println("File has been deleted")

        val outputStream = FileOutputStream("i18n.xls")
        outputStream.write(content)
        outputStream.close()

        println("File has been written")
    }
}