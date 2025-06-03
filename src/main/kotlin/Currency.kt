// =============== Currency.kt ===================
package io.github.typosbro

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

// --- Helper for currency parsing (simulating parts of currency.py) ---
data class CurrencyParts(val integer: Long, val cents: Int, val isNegative: Boolean)



// =============== Top-level parseCurrencyParts function ===================
// (Ensure this is the version used)
fun parseCurrencyParts(value: Any, isIntWithCentsProvided: Boolean = true): CurrencyParts {
    return when (value) {
        is Int -> {
            if (isIntWithCentsProvided) {
                val negative = value < 0; val absVal = abs(value.toLong())
                CurrencyParts(absVal / 100, (absVal % 100).toInt(), negative)
            } else { CurrencyParts(abs(value.toLong()), 0, value < 0) }
        }
        is Long -> {
            if (isIntWithCentsProvided) {
                val negative = value < 0; val absVal = abs(value)
                CurrencyParts(absVal / 100, (absVal % 100).toInt(), negative)
            } else { CurrencyParts(abs(value), 0, value < 0) }
        }
        is Float -> parseDecimalStyleCurrency(BigDecimal(value.toString()))
        is Double -> parseDecimalStyleCurrency(BigDecimal(value.toString()))
        is String -> {
            try {
                // For String input, always parse as a decimal number.
                parseDecimalStyleCurrency(BigDecimal(value))
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Invalid number string for currency: $value", e)
            }
        }
        is BigDecimal -> parseDecimalStyleCurrency(value)
        else -> throw IllegalArgumentException("Unsupported type for currency parsing: ${value::class.simpleName}")
    }
}

private fun parseDecimalStyleCurrency(bdValue: BigDecimal): CurrencyParts {
    val roundedValue = bdValue.setScale(2, RoundingMode.HALF_UP)
    val negative = roundedValue < BigDecimal.ZERO
    val absValue = roundedValue.abs()
    val integerPart = absValue.toBigInteger()
    val fractionalPart = absValue.subtract(BigDecimal(integerPart))
    val cents = fractionalPart.multiply(BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toInt()
    return CurrencyParts(integerPart.toLong(), cents, negative)
}