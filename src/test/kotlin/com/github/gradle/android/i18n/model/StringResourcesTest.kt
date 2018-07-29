package com.github.gradle.android.i18n.model

import org.junit.Assert.*
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [StringResources].
 */
class StringResourcesTest : AbstractUnitTest() {

    @Test
    fun `should read strings resources`() {
        val res = readFile<StringResources>("/strings.xml")

        // Default values.
        assertEquals("", res.locale)
        assertFalse(res.defaultLocale)

        // Strings.
        assertNotNull(res.strings)
        assertEquals(3, res.strings.size)

        assertNotNull(res.strings[0])
        assertEquals("string_1", res.strings[0].name)
        assertEquals("Lorem ipsum dolor sit amet", res.strings[0].text)
        assertNull(res.strings[0].quantity)

        assertNotNull(res.strings[1])
        assertEquals("string_2", res.strings[1].name)
        assertEquals("Mauris molestie mi felis", res.strings[1].text)
        assertNull(res.strings[1].quantity)

        assertNotNull(res.strings[2])
        assertEquals("Donec cursus nisi eu semper", res.strings[2].text)
        assertEquals("string_3", res.strings[2].name)
        assertNull(res.strings[2].quantity)

        // Plurals.
        assertNotNull(res.plurals)
        assertEquals(2, res.plurals.size)

        assertNotNull(res.plurals[0])
        assertEquals("plural_a", res.plurals[0].name)
        assertNotNull(res.plurals[0].items)
        assertEquals(1, res.plurals[0].items.size)
        assertNull(res.plurals[0].items[0].name)
        assertEquals("other", res.plurals[0].items[0].quantity)
        assertEquals("Other of A", res.plurals[0].items[0].text)

        assertNotNull(res.plurals[1])
        assertEquals("plural_b", res.plurals[1].name)
        assertNotNull(res.plurals[1].items)
        assertEquals(1, res.plurals[1].items.size)
        assertNull(res.plurals[1].items[0].name)
        assertEquals("other", res.plurals[1].items[0].quantity)
        assertEquals("Other of B", res.plurals[1].items[0].text)
    }
}