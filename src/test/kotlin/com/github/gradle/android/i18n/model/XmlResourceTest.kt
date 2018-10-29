package com.github.gradle.android.i18n.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [XmlResource].
 */
class XmlResourceTest : AbstractUnitTest() {

    @Test
    fun `should initialize resource properly`() {
        with(XmlResource()) {
            assertThat(this).isNotNull
            assertThat(name).isNull()
            assertThat(quantity).isNull()
            assertThat(text).isNull()
        }

        with(XmlResource(name = "dumb_name")) {
            assertThat(this).isNotNull
            assertThat(name).isEqualTo("dumb_name")
            assertThat(quantity).isNull()
            assertThat(text).isNull()
        }

        with(XmlResource(quantity = "dumb_quantity", text = "dumb text")) {
            assertThat(this).isNotNull
            assertThat(name).isNull()
            assertThat(quantity).isEqualTo("dumb_quantity")
            assertThat(text).isEqualTo("dumb text")
        }

        with(XmlResource(name = "dumb_name", quantity = "dumb_quantity", text = "dumb text")) {
            assertThat(this).isNotNull
            assertThat(name).isEqualTo("dumb_name")
            assertThat(quantity).isEqualTo("dumb_quantity")
            assertThat(text).isEqualTo("dumb text")
        }
    }

    @Test
    fun `should read item with text`() {
        with(readFile<XmlResource>("/item.xml")) {
            assertThat(name).isNull()
            assertThat(quantity).isNull()
            assertThat(text).isEqualTo("Proin dui metus")
        }
    }

    @Test
    fun `should read string with text name`() {
        with(readFile<XmlResource>("/string.xml")) {
            assertThat(name).isEqualTo("toto")
            assertThat(quantity).isNull()
            assertThat(text).isEqualTo("Lorem Ipsum")
        }
    }

    @Test
    fun `should read item with text quantity`() {
        with(readFile<XmlResource>("/item-quantity.xml")) {
            assertThat(name).isNull()
            assertThat(quantity).isEqualTo("one")
            assertThat(text).isEqualTo("Nullam a felis")
        }
    }
}