// =============== EnTest.kt ===================

package io.github.typosbro

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class EnTest {

    private val converter = En()

    @Test
    fun testAndJoin199() {
        assertEquals("one hundred and ninety-nine", converter.toCardinal(199))
    }

    @Test
    fun testCardinalBasics() {
        assertEquals("zero", converter.toCardinal(0))
        assertEquals("one", converter.toCardinal(1))
        assertEquals("nineteen", converter.toCardinal(19))
        assertEquals("twenty", converter.toCardinal(20))
        assertEquals("twenty-one", converter.toCardinal(21))
        assertEquals("ninety-nine", converter.toCardinal(99))
        assertEquals("one hundred", converter.toCardinal(100))
        assertEquals("one hundred and one", converter.toCardinal(101))
        assertEquals("nine hundred and ninety-nine", converter.toCardinal(999))
        assertEquals("one thousand", converter.toCardinal(1000)) // Corrected: was "thousand"
        assertEquals("one thousand and one", converter.toCardinal(1001))
        assertEquals("twelve thousand, three hundred and forty-five", converter.toCardinal(12345))
        assertEquals(
            "one million, two hundred and thirty-four thousand, five hundred and sixty-seven",
            converter.toCardinal(1234567L)
        )
        assertEquals("one quintillion", converter.toCardinal(1_000_000_000_000_000_000L)) // Corrected: "quintillion"
        assertEquals("minus forty-two", converter.toCardinal(-42))
    }

    @Test
    fun testOrdinal() {
        assertEquals("zeroth", converter.toOrdinal(0))
        assertEquals("first", converter.toOrdinal(1))
        assertEquals("second", converter.toOrdinal(2))
        assertEquals("third", converter.toOrdinal(3))
        assertEquals("fourth", converter.toOrdinal(4))
        assertEquals("eleventh", converter.toOrdinal(11))
        assertEquals("twelfth", converter.toOrdinal(12))
        assertEquals("thirteenth", converter.toOrdinal(13))
        assertEquals("twentieth", converter.toOrdinal(20))
        assertEquals("twenty-first", converter.toOrdinal(21))
        assertEquals("twenty-second", converter.toOrdinal(22))
        assertEquals("one hundred and thirtieth", converter.toOrdinal(130))
        assertEquals("one thousand and third", converter.toOrdinal(1003))
        assertEquals("one hundred and first", converter.toOrdinal(101))
    }

    @Test
    fun testOrdinalNum() {
        assertEquals("0th", converter.toOrdinalNum(0))
        assertEquals("1st", converter.toOrdinalNum(1))
        assertEquals("2nd", converter.toOrdinalNum(2))
        assertEquals("3rd", converter.toOrdinalNum(3))
        assertEquals("4th", converter.toOrdinalNum(4))
        assertEquals("10th", converter.toOrdinalNum(10))
        assertEquals("11th", converter.toOrdinalNum(11))
        assertEquals("21st", converter.toOrdinalNum(21))
        assertEquals("102nd", converter.toOrdinalNum(102))
        assertEquals("73rd", converter.toOrdinalNum(73))
    }

    @Test
    fun testCardinalForFloatNumber() {
        assertEquals("zero point one two", converter.toCardinalFloat(0.12))
        assertEquals("minus zero point one two", converter.toCardinalFloat(-0.12))

        // For Double 12.50, (12.50).toString() is "12.5", so scale is 1 for BigDecimal("12.5")
        assertEquals("twelve point five", converter.toCardinalFloat(12.50))
        // For Double 12.5, (12.5).toString() is "12.5", scale 1
        assertEquals("twelve point five", converter.toCardinalFloat(12.5))
        // For Double 12.51, (12.51).toString() is "12.51", scale 2
        assertEquals("twelve point five one", converter.toCardinalFloat(12.51))


        // For precise scale control, use BigDecimal input
        assertEquals("twelve point five zero", converter.toCardinalFloat(BigDecimal("12.50"))) // Explicit scale 2
        assertEquals("twelve point five three", converter.toCardinalFloat(BigDecimal("12.53")))
        assertEquals("twelve point five nine", converter.toCardinalFloat(BigDecimal("12.59")))
        assertEquals("zero point five", converter.toCardinalFloat(BigDecimal("0.5")))
        assertEquals("minus zero point seven five", converter.toCardinalFloat(BigDecimal("-0.75")))
        assertEquals("one point zero", converter.toCardinalFloat(BigDecimal("1.0"))) // Test trailing zero for BigDecimal
        assertEquals("one point zero zero", converter.toCardinalFloat(BigDecimal("1.00")))
    }

    @Test
    fun testOverflow() {
        // Test 1: Inputting the calculated MAXVAL should throw. MAXVAL is exclusive upper bound.
        if (converter.MAXVAL > 0 && converter.MAXVAL <= Long.MAX_VALUE) {
            val justAtMax = converter.MAXVAL
            assertThrows<IllegalArgumentException>("Value $justAtMax (converter.MAXVAL) should be too big (exclusive bound)") {
                converter.toCardinal(justAtMax)
            }
        }

        // Test 2: Extremely large number string (should exceed MAXVAL for BigDecimal or internal Long limits)
        val extremelyLargeNumberString = "100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" // approx 10^102
        assertThrows<IllegalArgumentException>("Extremely large number string should cause an error") {
            converter.toCardinal(BigDecimal(extremelyLargeNumberString))
        }

        // Test 3: If MAXVAL happens to be Long.MAX_VALUE
        if (converter.MAXVAL == Long.MAX_VALUE) {
            assertThrows<IllegalArgumentException>("Value ${Long.MAX_VALUE} (when MAXVAL is Long.MAX_VALUE) should be too big") {
                converter.toCardinal(Long.MAX_VALUE)
            }
        }
    }

    @Test
    fun testToCurrency() {
        // Input "38.4" -> BigDecimal("38.4"). parseDecimalStyleCurrency rounds to "38.40". Cents part = 40.
        assertEquals(
            "thirty-eight dollars and forty cents", // centsVerbose=true
            converter.toCurrency(BigDecimal("38.4"), currency = "USD", separator = " and", centsVerbose = true)
        )
        assertEquals( // centsVerbose=false means numeric cents, no unit word
            "thirty-eight dollars and 40",
            converter.toCurrency(BigDecimal("38.4"), currency = "USD", separator = " and", centsVerbose = false)
        )

        // Input 0 (Int). parseCurrencyParts with isIntWithCentsProvided=false -> 0 units, 0 cents. hasDecimalInInput=false.
        assertEquals(
            "zero dollars",
            converter.toCurrency(0, currency = "USD", separator = " and", centsVerbose = false)
        )
        // Input 0 (Int) with verbose cents. Still no "and zero cents" because original input was whole int.
        assertEquals(
            "zero dollars",
            converter.toCurrency(0, currency = "USD", separator = " and", centsVerbose = true)
        )
        // For BigDecimal("0.00"), should include "zero cents" because hasDecimalInInput is true.
        assertEquals(
            "zero dollars and zero cents",
            converter.toCurrency(BigDecimal("0.00"), currency = "USD", separator = " and", centsVerbose = true)
        )
        assertEquals( // Test BigDecimal("0") for currency
            "zero dollars",
            converter.toCurrency(BigDecimal("0"), currency = "USD", separator = " and", centsVerbose = true)
        )


        assertEquals(
            "one dollar and one cent",
            converter.toCurrency(BigDecimal("1.01"), currency = "USD", separator = " and", centsVerbose = true)
        )

        // Assuming CURRENCY_ADJECTIVES_EN_DATA is empty for "USD"
        assertEquals(
            "four thousand, seven hundred and seventy-eight dollars and zero cents",
            converter.toCurrency(BigDecimal("4778.00"), currency = "USD", separator = " and", centsVerbose = true, adjective = true)
        )
        assertEquals(
            "four thousand, seven hundred and seventy-eight dollars and zero cents",
            converter.toCurrency(BigDecimal("4778.00"), currency = "USD", separator = " and", centsVerbose = true)
        )

        assertEquals(
            "one peso and ten centavos",
            converter.toCurrency(BigDecimal("1.1"), currency = "MXN", separator = " and", centsVerbose = true)
        )
        assertEquals(
            "one hundred and fifty-eight pesos and thirty centavos",
            converter.toCurrency(BigDecimal("158.3"), currency = "MXN", separator = " and", centsVerbose = true)
        )
        assertEquals(
            "two thousand pesos and zero centavos",
            converter.toCurrency(BigDecimal("2000.00"), currency = "MXN", separator = " and", centsVerbose = true)
        )
        assertEquals(
            "four pesos and one centavo",
            converter.toCurrency(BigDecimal("4.01"), currency = "MXN", separator = " and", centsVerbose = true)
        )
        assertEquals(
            "two thousand sums and zero tiyins",
            converter.toCurrency(BigDecimal("2000.00"), currency = "UZS", separator = " and", centsVerbose = true)
        )
        assertEquals(
            "two thousand yen and zero sen",
            converter.toCurrency(BigDecimal("2000.00"), currency = "JPY", separator = " and", centsVerbose = true)
        )
        assertEquals(
            "two thousand won and zero jeon",
            converter.toCurrency(BigDecimal("2000.00"), currency = "KRW", separator = " and", centsVerbose = true)
        )
    }

    @Test
    fun testToYear() {
        assertEquals("nineteen ninety", converter.toYear(1990))
        assertEquals("fifty-five fifty-five", converter.toYear(5555))
        assertEquals("twenty seventeen", converter.toYear(2017))
        assertEquals("ten sixty-six", converter.toYear(1066))
        assertEquals("eighteen sixty-five", converter.toYear(1865))

        assertEquals("three thousand", converter.toYear(3000))
        assertEquals("two thousand and one", converter.toYear(2001))
        assertEquals("nineteen oh-one", converter.toYear(1901))
        assertEquals("two thousand", converter.toYear(2000))
        assertEquals("nine oh-five", converter.toYear(905)) // Corrected expectation

        assertEquals("sixty-six hundred", converter.toYear(6600))
        assertEquals("nineteen hundred", converter.toYear(1900))
        assertEquals("six hundred", converter.toYear(600))
        assertEquals("fifty", converter.toYear(50))
        assertEquals("zero", converter.toYear(0))

        assertEquals("forty-four BC", converter.toYear(-44))
        assertEquals("forty-four BCE", converter.toYear(-44, suffix = "BCE"))
        assertEquals("one AD", converter.toYear(1, suffix = "AD"))
        assertEquals("sixty-six m.y.a.", converter.toYear(66, suffix = "m.y.a."))
        assertEquals("sixty-six million BC", converter.toYear(-66000000L))
    }
}