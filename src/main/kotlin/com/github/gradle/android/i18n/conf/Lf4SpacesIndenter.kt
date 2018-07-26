package com.github.gradle.android.i18n.conf

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import org.codehaus.stax2.XMLStreamWriter2
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Default linefeed-based indenter uses system-specific linefeeds and 4 spaces for indentation per level.
 *
 * [com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter.Lf2SpacesIndenter]
 */
internal class Lf4SpacesIndenter : DefaultXmlPrettyPrinter.Indenter {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Lf4SpacesIndenter::class.java)

        private const val SPACE_COUNT = 64

        private val SPACES = CharArray(SPACE_COUNT) { ' ' }

        private val SYSTEM_LINE_SEPARATOR = getLineSeparator()

        private fun getLineSeparator(): String {
            var lf: String? = null
            try {
                lf = System.getProperty("line.separator")
            } catch (t: Throwable) {
                LOGGER.error("Error while reading system property 'line.separator'", t)
            }
            return lf ?: "\n"
        }
    }

    private fun spacesLevel(level: Int): Int {
        var mutableLevel = level
        mutableLevel += mutableLevel
        mutableLevel += mutableLevel
        return mutableLevel
    }

    override fun isInline(): Boolean {
        return false
    }

    override fun writeIndentation(sw: XMLStreamWriter2?, level: Int) {
        sw?.let {
            sw.writeRaw(SYSTEM_LINE_SEPARATOR)
            var mutableLevel = spacesLevel(level)
            while (mutableLevel > SPACE_COUNT) { // should never happen but...
                sw.writeRaw(SPACES, 0, SPACE_COUNT)
                mutableLevel -= SPACES.size
            }
            sw.writeRaw(SPACES, 0, mutableLevel)
        }
    }

    override fun writeIndentation(jg: JsonGenerator?, level: Int) {
        jg?.let {
            jg.writeRaw(SYSTEM_LINE_SEPARATOR)
            var mutableLevel = spacesLevel(level)
            while (mutableLevel > SPACE_COUNT) { // should never happen but...
                jg.writeRaw(SPACES, 0, SPACE_COUNT)
                mutableLevel -= SPACES.size
            }
            jg.writeRaw(SPACES, 0, mutableLevel)
        }
    }
}