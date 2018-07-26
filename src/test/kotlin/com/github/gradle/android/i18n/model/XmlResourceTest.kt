package com.github.gradle.android.i18n.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [XmlResource].
 */
class XmlResourceTest : AbstractUnitTest() {

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