package com.github.gradle.android.i18n.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [StringResources].
 */
class StringResourcesTest : AbstractUnitTest() {

    @Test
    fun `should read strings resources`() {
        with(readFile<StringResources>("/strings.xml")) {

            // Default values.
            assertThat(locale).isEqualTo("")
            assertThat(defaultLocale).isFalse()

            // Strings.
            assertThat(strings).isNotNull
            assertThat(strings.size).isEqualTo(3)

            assertThat(strings[0]).isNotNull
            assertThat(strings[0].name).isEqualTo("string_1")
            assertThat(strings[0].text).isEqualTo("Lorem ipsum dolor sit amet")
            assertThat(strings[0].quantity).isNull()

            assertThat(strings[1]).isNotNull
            assertThat(strings[1].name).isEqualTo("string_2")
            assertThat(strings[1].text).isEqualTo("Mauris molestie mi felis")
            assertThat(strings[1].quantity).isNull()

            assertThat(strings[2]).isNotNull
            assertThat(strings[2].text).isEqualTo("Donec cursus nisi eu semper")
            assertThat(strings[2].name).isEqualTo("string_3")
            assertThat(strings[2].quantity).isNull()

            // Plurals.
            assertThat(plurals).isNotNull
            assertThat(plurals.size).isEqualTo(2)

            assertThat(plurals[0]).isNotNull
            assertThat(plurals[0].name).isEqualTo("plural_a")
            assertThat(plurals[0].items).isNotNull
            assertThat(plurals[0].items.size).isEqualTo(1)
            assertThat(plurals[0].items[0].name).isNull()
            assertThat(plurals[0].items[0].quantity).isEqualTo("other")
            assertThat(plurals[0].items[0].text).isEqualTo("Other of A")

            assertThat(plurals[1]).isNotNull
            assertThat(plurals[1].name).isEqualTo("plural_b")
            assertThat(plurals[1].items).isNotNull
            assertThat(plurals[1].items.size).isEqualTo(1)
            assertThat(plurals[1].items[0].name).isNull()
            assertThat(plurals[1].items[0].quantity).isEqualTo("other")
            assertThat(plurals[1].items[0].text).isEqualTo("Other of B")
        }
    }
}