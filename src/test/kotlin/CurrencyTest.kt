// =============== CurrencyTest.kt ===================

package io.github.typosbro

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class CurrencyUtilsTest { // Was CurrencyTest.kt in logs

    @Test
    fun testParseCurrencyParts() {
        // Test Integers with isIntWithCentsProvided = true (default)
        assertEquals(CurrencyParts(1, 1, false), parseCurrencyParts(101)) // 101 cents -> 1 unit, 1 cent
        assertEquals(CurrencyParts(1, 23, true), parseCurrencyParts(-123)) // -123 cents -> 1 unit, 23 cents, negative

        // Test Integers with isIntWithCentsProvided = false
        assertEquals(CurrencyParts(101, 0, false), parseCurrencyParts(101, isIntWithCentsProvided = false))
        assertEquals(CurrencyParts(123, 0, true), parseCurrencyParts(-123, isIntWithCentsProvided = false))

        // Test Floats (always parsed as decimal style, isIntWithCentsProvided ignored)
        assertEquals(CurrencyParts(1, 1, false), parseCurrencyParts(1.01f))
        assertEquals(CurrencyParts(1, 23, true), parseCurrencyParts(-1.23f))
        assertEquals(CurrencyParts(1, 20, true), parseCurrencyParts(-1.2f))  // -1.20
        assertEquals(CurrencyParts(0, 0, false), parseCurrencyParts(0.004f)) // rounds to 0.00
        assertEquals(CurrencyParts(0, 1, false), parseCurrencyParts(0.005f)) // rounds to 0.01
        assertEquals(CurrencyParts(0, 99, false), parseCurrencyParts(0.985f))// rounds to 0.99 (HALF_UP)
        assertEquals(CurrencyParts(1, 0, false), parseCurrencyParts(0.995f)) // rounds to 1.00 (HALF_UP)

        // Test Doubles (always parsed as decimal style)
        assertEquals(CurrencyParts(0, 99, false), parseCurrencyParts(0.989)) // rounds to 0.99
        assertEquals(CurrencyParts(1, 0, false), parseCurrencyParts(0.999))   // rounds to 1.00

        // Test BigDecimals (always parsed as decimal style)
        assertEquals(CurrencyParts(1, 1, false), parseCurrencyParts(BigDecimal("1.01")))
        assertEquals(CurrencyParts(1, 23, true), parseCurrencyParts(BigDecimal("-1.23")))
        assertEquals(CurrencyParts(1, 23, true), parseCurrencyParts(BigDecimal("-1.233"))) // rounds to -1.23
        assertEquals(CurrencyParts(1, 99, true), parseCurrencyParts(BigDecimal("-1.989"))) // rounds to -1.99

        // Test Strings (always parsed as decimal style)
        assertEquals(CurrencyParts(1, 1, false), parseCurrencyParts("1.01"))
        assertEquals(CurrencyParts(1, 23, true), parseCurrencyParts("-1.23"))
        assertEquals(CurrencyParts(1, 20, true), parseCurrencyParts("-1.2"))
        // String "1" -> BigDecimal("1") -> 1 unit, 0 cents
        assertEquals(CurrencyParts(1, 0, false), parseCurrencyParts("1")) // Corrected expectation
        assertEquals(CurrencyParts(100, 0, false), parseCurrencyParts("100"))

        // Test String that looks like integer but should be total cents if isIntWithCentsProvided=true (via old logic)
        // New logic: String always parsed as BigDecimal. "101" -> BigDecimal("101") -> 101 units, 0 cents.
        assertEquals(CurrencyParts(101, 0, false), parseCurrencyParts("101"))
        // If you need to parse "101" (string) as 1 unit and 1 cent, the input must be an actual Int/Long.
        // Or the string must be "1.01".

        // Test invalid string
        assertThrows<IllegalArgumentException> { parseCurrencyParts("not-a-number") }
    }
}