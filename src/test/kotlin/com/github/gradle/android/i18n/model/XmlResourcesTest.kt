package com.github.gradle.android.i18n.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [XmlResources].
 */
class XmlResourcesTest : AbstractUnitTest() {

    @Test
    fun `should read plurals`() {
        with(readFile<XmlResources>("/plurals.xml")) {

            assertThat(name).isEqualTo("number_of_songs")

            assertThat(items).isNotNull
            assertThat(items.size).isEqualTo(3)

            assertThat(items[0]).isNotNull
            assertThat(items[0].quantity).isEqualTo("zero")
            assertThat(items[0].text).isEqualTo("0 songs")
            assertThat(items[0].name).isNull()

            assertThat(items[1]).isNotNull
            assertThat(items[1].quantity).isEqualTo("one")
            assertThat(items[1].text).isEqualTo("1 song")
            assertThat(items[1].name).isNull()

            assertThat(items[2]).isNotNull
            assertThat(items[2].quantity).isEqualTo("two")
            assertThat(items[2].text).isEqualTo("2 songs")
            assertThat(items[2].name).isNull()
        }
    }

    @Test
    fun `should read string array`() {
        with(readFile<XmlResources>("/string-array.xml")) {

            assertThat(items).isNotNull
            assertThat(items.size).isEqualTo(4)

            assertThat(items[0]).isNotNull
            assertThat(items[0].text).isEqualTo("Mercury")
            assertThat(items[0].quantity).isNull()
            assertThat(items[0].name).isNull()

            assertThat(items[1]).isNotNull
            assertThat(items[1].text).isEqualTo("Venus")
            assertThat(items[1].quantity).isNull()
            assertThat(items[1].name).isNull()

            assertThat(items[2]).isNotNull
            assertThat(items[2].text).isEqualTo("Earth")
            assertThat(items[2].quantity).isNull()
            assertThat(items[2].name).isNull()

            assertThat(items[3]).isNotNull
            assertThat(items[3].text).isEqualTo("Mars")
            assertThat(items[3].quantity).isNull()
            assertThat(items[3].name).isNull()
        }
    }
}