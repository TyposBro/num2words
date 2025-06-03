// =============== ErrorTest.kt ===================

package io.github.typosbro

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class Num2WordsErrorTest {

    @Test
    fun testInvalidToTypeStyleError() {
        val converter = En()

        assertThrows<IllegalArgumentException>("Ordinal methods should not accept non-integer numbers") {
            converter.toOrdinal(10.5)
        }
        assertThrows<IllegalArgumentException>("Ordinal methods should not accept negative numbers") {
            converter.toOrdinal(-5)
        }
    }
}