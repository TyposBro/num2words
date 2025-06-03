// =============== Main.kt ===================

package io.github.typosbro

import java.math.BigDecimal



// --- Main function (from your main.kt) for testing ---
fun main() {
    val name = "Kotlin Num2Words Demo"
    println("Hello, $name!")

    val converter = En()
    converter.isTitle = false

    println("\n--- Cardinals ---")
    println("0: " + converter.toCardinal(0))
    println("1: " + converter.toCardinal(1))
    println("19: " + converter.toCardinal(19))
    println("20: " + converter.toCardinal(20))
    println("21: " + converter.toCardinal(21))
    println("99: " + converter.toCardinal(99))
    println("100: " + converter.toCardinal(100))
    println("101: " + converter.toCardinal(101))
    println("105: " + converter.toCardinal(105))
    println("123: " + converter.toCardinal(123))
    println("999: " + converter.toCardinal(999))
    println("1000: " + converter.toCardinal(1000))
    println("1001: " + converter.toCardinal(1001))
    println("12345: " + converter.toCardinal(12345)) // twelve thousand three hundred and forty-five
    println("1234567: " + converter.toCardinal(1234567L)) // one million, two hundred and thirty-four thousand five hundred and sixty-seven
    println("1000000000: " + converter.toCardinal(1_000_000_000L))
    println("-42: " + converter.toCardinal(-42))

    println("\n--- Cardinal Floats ---")
    converter.precision = 2
    println("15.42 (prec 2): " + converter.toCardinalFloat(15.42))
    converter.precision = 3
    println("123.456 (prec 3): " + converter.toCardinalFloat(123.456))
    converter.precision = 4
    println("987.654321 (prec 4, BigDecimal): " + converter.toCardinalFloat(BigDecimal("987.654321")))
    converter.precision = 2
    println("0.5 (prec 2): " + converter.toCardinalFloat(0.5))
    println("-0.75 (prec 2): " + converter.toCardinalFloat(-0.75))
    println("-25.01 (prec 2): " + converter.toCardinalFloat(-25.01))
    println("0.0 (prec 2): " + converter.toCardinalFloat(0.0))
    println("1.000 (prec 2): " + converter.toCardinalFloat(1.000))


    println("\n--- Ordinals ---")
    println("1: " + converter.toOrdinal(1))
    println("2: " + converter.toOrdinal(2))
    println("3: " + converter.toOrdinal(3))
    println("4: " + converter.toOrdinal(4))
    println("11: " + converter.toOrdinal(11))
    println("12: " + converter.toOrdinal(12))
    println("20: " + converter.toOrdinal(20))
    println("21: " + converter.toOrdinal(21))
    println("30: " + converter.toOrdinal(30))
    println("101: " + converter.toOrdinal(101))

    println("\n--- Ordinal Numbers ---")
    println("1: " + converter.toOrdinalNum(1))
    println("22: " + converter.toOrdinalNum(22))
    println("113: " + converter.toOrdinalNum(113))

    println("\n--- Years (using specific Num2WordEn method) ---")
    println("1995: " + converter.toYear(1995))
    println("2000: " + converter.toYear(2000))
    println("2007: " + converter.toYear(2007))
    println("805: " + converter.toYear(805))
    println("100: " + converter.toYear(100))
    println("2100: " + converter.toYear(2100))
    println("-500 (BC): " + converter.toYear(-500)) // Suffix handled by toYear specific
    println("1900: " + converter.toYear(1900)) // nineteen hundred

    println("\n--- Years (using overridden base method with options) ---")
    println("1995 (opts): " + converter.toYear(1995, mapOf("longVal" to true)))
    println("2000 (opts, AD): " + converter.toYear(2000, mapOf("suffix" to "AD")))
    println("-44 (opts, default BC): " + converter.toYear(-44, mapOf("longVal" to true)))


    println("\n--- Currency ---")
    println("123.45 USD: " + converter.toCurrency(123.45, currency = "USD"))
    println("1 EUR: " + converter.toCurrency(1.0, currency = "EUR", separator = " and"))
    println("1 GBP (integer): " + converter.toCurrency(1, currency = "GBP"))
    println("200 GBP (integer, verbose cents=false): " + converter.toCurrency(20000L, currency = "GBP", centsVerbose = false)) // 20000 cents = 200 pounds
    println("50.15 GBP (BigDecimal): " + converter.toCurrency(BigDecimal("50.15"), currency = "GBP"))
    println("-2.50 EUR: " + converter.toCurrency(BigDecimal("-2.50"), currency = "EUR"))

}