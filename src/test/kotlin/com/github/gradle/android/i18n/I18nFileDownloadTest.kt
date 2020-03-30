package com.github.gradle.android.i18n

import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class I18nFileDownloadTest {

    @Test
    fun `should download i18n file`() {


        val protocol = "https"
        val path = "usinedigitaleratp.atlassian.net/wiki/download/attachments/303202427/i18n.xls?api=v2"
        val url = URL("$protocol://$path")
        println("URL: $url")

        val user = "christophe.pele@ratpsmartsystems.com"
        val pass = "2NL9i2ImweAABL75P0Ar687C"
        val credentials = "$user:$pass"
        val basicAuth = String(Base64.getEncoder().encode(credentials.toByteArray()))
        println("Basic auth: $basicAuth")

        val connection = url.openConnection() as HttpsURLConnection
        connection.addRequestProperty("Authorization", "Basic $basicAuth")
        val content = connection.inputStream.readBytes()

        File("i18n.xls").delete()
        println("File has been deleted")

        val outputStream = FileOutputStream("i18n.xls")
        outputStream.write(content)
        outputStream.close()

        println("File has been written")
    }
}