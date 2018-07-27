package com.github.gradle.android.i18n.generator

import org.junit.Assert.assertEquals
import org.junit.Test
import testutil.AbstractUnitTest

/**
 * Unit tests of component [GeneratorHelper].
 */
class GeneratorHelperTest : AbstractUnitTest() {

    @Test
    fun `should return string resource indexed parameter`() {
        assertEquals("%1\\\$s", GeneratorHelper.getXmlIndexedArg(1))
        assertEquals("%2\\\$s", GeneratorHelper.getXmlIndexedArg(2))
        assertEquals("%20\\\$s", GeneratorHelper.getXmlIndexedArg(20))
        assertEquals("%123\\\$s", GeneratorHelper.getXmlIndexedArg(123))
    }
}