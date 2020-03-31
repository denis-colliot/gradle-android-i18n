package com.github.gradle.android.i18n.connector

import java.io.InputStream
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * This connector allows to read a file via HTTPS with basic auth.
 *
 * Here is how to use it:
 *
 * ```
 * val connector = HttpsConnector("https://foo:bar@example.com/file.dat")
 * val bytes = connector.intputStream.readBytes()
 * ```
 *
 * @param url The URL of the file to read. It has to respect this pattern:
 * `https://user:password@path`.
 */
class HttpsConnector(url: String) {

    private val connection: HttpsURLConnection = openConnection(url)

    val inputStream: InputStream = connection.inputStream
}

private fun openConnection(url: String): HttpsURLConnection {
    val urlObj = URL(url)
    val basicAuth = extractBasicAuth(urlObj)
    val connection = urlObj.openConnection() as HttpsURLConnection
    connection.addRequestProperty("Authorization", "Basic $basicAuth")
    return connection
}

private fun extractBasicAuth(urlObj: URL): String {
    val urlEncodedCredentials = urlObj.userInfo
    val credentials = URLDecoder.decode(urlEncodedCredentials, Charset.defaultCharset().name())
    val credentialsBytes = credentials.toByteArray()
    val encodedCredentials = Base64.getEncoder().encode(credentialsBytes)
    return String(encodedCredentials)
}