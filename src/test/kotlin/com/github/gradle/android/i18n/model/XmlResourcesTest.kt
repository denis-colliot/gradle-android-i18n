package com.github.gradle.android.i18n.model

import org.junit.Assert.*
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [XmlResources].
 */
class XmlResourcesTest : AbstractUnitTest() {

    @Test
    fun `should read plurals`() {
        val res = readFile<XmlResources>("/plurals.xml")

        assertEquals("number_of_songs", res.name)

        assertNotNull(res.items)
        assertEquals(3, res.items.size)

        assertNotNull(res.items[0])
        assertEquals("zero", res.items[0].quantity)
        assertEquals("0 songs", res.items[0].text)
        assertNull(res.items[0].name)

        assertNotNull(res.items[1])
        assertEquals("one", res.items[1].quantity)
        assertEquals("1 song", res.items[1].text)
        assertNull(res.items[1].name)

        assertNotNull(res.items[2])
        assertEquals("two", res.items[2].quantity)
        assertEquals("2 songs", res.items[2].text)
        assertNull(res.items[2].name)
    }

    @Test
    fun `should read string array`() {
        val res = readFile<XmlResources>("/string-array.xml")

        assertNotNull(res.items)
        assertEquals(4, res.items.size)

        assertNotNull(res.items[0])
        assertEquals("Mercury", res.items[0].text)
        assertNull(res.items[0].quantity)
        assertNull(res.items[0].name)

        assertNotNull(res.items[1])
        assertEquals("Venus", res.items[1].text)
        assertNull(res.items[1].quantity)
        assertNull(res.items[1].name)

        assertNotNull(res.items[2])
        assertEquals("Earth", res.items[2].text)
        assertNull(res.items[2].quantity)
        assertNull(res.items[2].name)

        assertNotNull(res.items[3])
        assertEquals("Mars", res.items[3].text)
        assertNull(res.items[3].quantity)
        assertNull(res.items[3].name)
    }
}