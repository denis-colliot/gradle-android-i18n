package com.github.gradle.android.i18n.model

import org.junit.Assert.*
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [XmlResource].
 */
class XmlResourceTest : AbstractUnitTest() {

    @Test
    fun `should initialize resource properly`() {
        XmlResource().let { xmlResource ->
            assertNotNull(xmlResource)
            assertNull(xmlResource.name)
            assertNull(xmlResource.quantity)
            assertNull(xmlResource.text)
        }

        XmlResource(name = "dumb_name").let { xmlResource ->
            assertNotNull(xmlResource)
            assertEquals("dumb_name", xmlResource.name)
            assertNull(xmlResource.quantity)
            assertNull(xmlResource.text)
        }

        XmlResource(quantity = "dumb_quantity", text = "dumb text").let { xmlResource ->
            assertNotNull(xmlResource)
            assertNull(xmlResource.name)
            assertEquals("dumb_quantity", xmlResource.quantity)
            assertEquals("dumb text", xmlResource.text)
        }

        XmlResource(name = "dumb_name", quantity = "dumb_quantity", text = "dumb text").let { xmlResource ->
            assertNotNull(xmlResource)
            assertEquals("dumb_name", xmlResource.name)
            assertEquals("dumb_quantity", xmlResource.quantity)
            assertEquals("dumb text", xmlResource.text)
        }
    }

    @Test
    fun `should read item with text`() {
        val res = readFile<XmlResource>("/item.xml")
        assertNull(res.name)
        assertNull(res.quantity)
        assertEquals("Proin dui metus", res.text)
    }

    @Test
    fun `should read string with text name`() {
        val res = readFile<XmlResource>("/string.xml")
        assertEquals("toto", res.name)
        assertNull(res.quantity)
        assertEquals("Lorem Ipsum", res.text)
    }

    @Test
    fun `should read item with text quantity`() {
        val res = readFile<XmlResource>("/item-quantity.xml")
        assertNull(res.name)
        assertEquals("one", res.quantity)
        assertEquals("Nullam a felis", res.text)
    }
}