
// =============== BaseTest.kt ===================
package io.github.typosbro

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

private class StubNum2WordBaseConcrete : Base() {
    override fun setup() {
        cards[0L] = "zero_stub"
        for (i in 1L..19L) { cards[i] = "val_${i}_stub" }
        cards[20L] = "twenty_stub"
        cards[30L] = "thirty_stub"; cards[40L] = "forty_stub"; cards[50L] = "fifty_stub"
        cards[60L] = "sixty_stub"; cards[70L] = "seventy_stub"; cards[80L] = "eighty_stub"
        cards[90L] = "ninety_stub"
        cards[100L] = "hundred_stub"
        MAXVAL = 1000L // MAXVAL is exclusive, so 1000 is too big, 999 is okay.
    }

    override fun merge(leftPair: Pair<String, Long>, rightPair: Pair<String, Long>): Pair<String, Long> {
        // Basic merge for stub, actual logic is language-specific
        return Pair("${leftPair.first}_${rightPair.first}", leftPair.second + rightPair.second)
    }

    override fun pluralize(n: Long, forms: Pair<String, String>): String {
        return if (n == 1L) forms.first else forms.second // Basic stub
    }

    override fun getCurrencyForms(currencyCode: String): Pair<Pair<String, String>, Pair<String, String>> {
        throw NotImplementedError("Stubbed getCurrencyForms was called for $currencyCode.")
    }

    override fun getCurrencyAdjective(currencyCode: String): String? {
        throw NotImplementedError("Stubbed getCurrencyAdjective was called for $currencyCode.")
    }
}

class Num2WordBaseTest {

    private val baseConverter = StubNum2WordBaseConcrete()

    @Test
    fun testToCurrencyNotImplementedByStub() {
        assertThrows<NotImplementedError> {
            baseConverter.toCurrency(BigDecimal("1.00"), currency = "EUR")
        }
    }

    @Test
    fun testErrorToCardinalWithUnsupportedNumberType() {
        class BadNumber : Number() {
            override fun toByte(): Byte = throw UnsupportedOperationException()
            override fun toChar(): Char = throw UnsupportedOperationException()
            override fun toDouble(): Double = throw UnsupportedOperationException()
            override fun toFloat(): Float = throw UnsupportedOperationException()
            override fun toInt(): Int = throw UnsupportedOperationException()
            override fun toLong(): Long = throw UnsupportedOperationException()
            override fun toShort(): Short = throw UnsupportedOperationException()
            override fun toString(): String = "unsupported number"
        }
        assertThrows<IllegalArgumentException> {
            baseConverter.toCardinal(BadNumber())
        }
    }

    @Test
    fun testIsTitle() {
        val freshBase = StubNum2WordBaseConcrete()
        freshBase.isTitle = false
        assertEquals("one test", freshBase.title("one test"))

        freshBase.isTitle = true
        assertEquals("One Test", freshBase.title("one test"))

        freshBase.excludeTitle = listOf("test")
        assertEquals("One test", freshBase.title("one test"))

        freshBase.excludeTitle = listOf("and", "the")
        assertEquals("One and the Other", freshBase.title("one and the other")) // Corrected expectation
    }

    @Test
    fun testToOrdinalNumBaseImplementation() {
        assertEquals("1", baseConverter.toOrdinalNum(1))
        assertEquals("100", baseConverter.toOrdinalNum(100))
        assertThrows<IllegalArgumentException> { baseConverter.toOrdinalNum(-5) }
        assertThrows<IllegalArgumentException> { baseConverter.toOrdinalNum(1.5) }
    }

    @Test
    fun testBaseToOrdinal() {
        // Relies on stub's cards and basic merge for testing base toOrdinal -> toCardinal path
        assertEquals("val_1_stub", baseConverter.toOrdinal(1L))
        assertEquals("zero_stub", baseConverter.toOrdinal(0L))
    }

    @Test
    fun testBaseToYear() {
        assertEquals("ten_stub", baseConverter.toYear(10L))
    }

    @Test
    fun testMaxvalErrorInBaseCardinal() {
        // Stub's MAXVAL is 1000L. toCardinal should throw for values >= MAXVAL.
        assertThrows<IllegalArgumentException>("Value 1000 (MAXVAL) should be too big") {
            baseConverter.toCardinal(1000L)
        }
        assertThrows<IllegalArgumentException>("Value 1001 should be too big") {
            baseConverter.toCardinal(1001L)
        }
        assertDoesNotThrow({
            baseConverter.toCardinal(999L) // This was causing StackOverflow, should be fixed by stub cards.
        }, "Value 999 should be representable")
    }

    @Test
    fun testSplitnumWithStubCards() {
        // Test splitnum directly with the stub's simple card setup to ensure no recursion.
        // This relies on internal visibility or making splitnum temporarily public for testing.
        // For this example, we assume it can be called if it were protected internal.
        // Or, test via toCardinal which uses it.
        // toCardinal(9L) with stub: cards has "val_9_stub"
        assertEquals("val_9_stub", baseConverter.toCardinal(9L))
        // toCardinal(19L) with stub: cards has "val_19_stub"
        assertEquals("val_19_stub", baseConverter.toCardinal(19L))
        // toCardinal(20L) with stub:
        assertEquals("twenty_stub", baseConverter.toCardinal(20L))
        // toCardinal(21L) -> split(21) -> [split(1), ("twenty_stub",20)] or [split(20), split(1)]
        // -> [ [("val_1_stub",1)], ("twenty_stub",20) ]  -- if split(20) is base
        // -> clean -> merge(("val_1_stub",1), ("twenty_stub",20)) -> "val_1_stub_twenty_stub" (by stub merge)
        // This depends heavily on the stub's merge. The default `splitnum` aims for generic structure.
        // The test `assertDoesNotThrow { baseConverter.toCardinal(999L) }` above is a good indirect test.
    }
}