package com.github.gradle.android.i18n.connector

import java.io.InputStream
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import javax.net.ssl.HttpsURLConnection

class HttpsConnector(url: String) {

    private val connection: HttpsURLConnection = run {
        val urlObj = URL(url)
        val basicAuth = extractBasicAuth(urlObj)
        val connection = urlObj.openConnection() as HttpsURLConnection
        connection.addRequestProperty("Authorization", "Basic $basicAuth")
        connection
    }

    private fun extractBasicAuth(urlObj: URL): String {
        val urlEncodedCredentials = urlObj.userInfo
        val credentials = URLDecoder.decode(urlEncodedCredentials, Charset.defaultCharset().name())
        val credentialsBytes = credentials.toByteArray()
        val encodedCredentials = Base64.getEncoder().encode(credentialsBytes)
        return String(encodedCredentials)
    }

    val inputStream: InputStream = connection.inputStream
}
