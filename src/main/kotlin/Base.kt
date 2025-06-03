
// =============== Base.kt ===================
package io.github.typosbro

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.floor

abstract class Base {
    var isTitle: Boolean = false
    var precision: Int = 2
    var excludeTitle: List<String> = emptyList()
    var negWord: String = "(-) "
    var pointWord: String = "(.)"
    open val errMsgNonNum: String = "Type %s is not a supported number type."
    open val errMsgFloatOrd: String = "Cannot treat float %s as ordinal."
    open val errMsgNegOrd: String = "Cannot treat negative number %s as ordinal."
    open val errMsgTooBig: String = "Absolute value of %s must be less than %s."

    protected val cards = LinkedHashMap<Long, String>() // Language-specific words
    var MAXVAL: Long = Long.MAX_VALUE // Set by setup()

    init {
        setup() // Call to populate cards and set MAXVAL
    }

    abstract fun setup() // To be implemented by language-specific subclasses

    protected open fun splitnum(value: Long): List<Any> {
        if (cards.containsKey(value)) {
            val cardWord = cards[value]!!
            if (value >= 100L && (cardWord == "hundred" || cardWord == "thousand" || cardWord.endsWith("illion"))) {
                return listOf(
                    splitnum(1L),
                    Pair(cardWord, value)
                )
            }
            return listOf(Pair(cardWord, value))
        }

        // Find largest power of 10 (hundred, thousand, illion) that is <= value
        val powerElemValue = cards.keys
            .filter { it > 0 && value >= it && (it == 100L || it == 1000L || cards[it]?.endsWith("illion") == true) }
            .maxOrNull()

        // If value is < 100 and not in cards (e.g. 21), use the largest tens key <= value
        val tensElemValue = if (value < 100L && powerElemValue == null) {
            cards.keys
                .filter { it > 0 && value >= it && it < 100L && it % 10L == 0L} // 20, 30 .. 90
                .maxOrNull()
        } else {
            null
        }

        val elemValue = powerElemValue ?: tensElemValue
        ?: throw IllegalStateException(
            "Cannot split number $value: No suitable base (power or tens) key <= $value found. Ensure 'cards' includes units, tens, and powers."
        )

        val multiplier = value / elemValue
        val remainder = value % elemValue

        val out = mutableListOf<Any>()
        out.add(splitnum(multiplier))
        out.add(Pair(cards[elemValue]!!, elemValue))

        if (remainder > 0) {
            out.add(splitnum(remainder))
        }
        return out
    }

    abstract fun merge(leftPair: Pair<String, Long>, rightPair: Pair<String, Long>): Pair<String, Long>

    protected fun clean(items: List<Any>): Pair<String, Long> {
        if (items.isEmpty()) return Pair("", 0L)
        val processedPairs = mutableListOf<Pair<String, Long>>()
        items.forEach { item ->
            when (item) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val cleanedSub = clean(item as List<Any>)
                    if (cleanedSub.first.isNotEmpty()) processedPairs.add(cleanedSub)
                }
                is Pair<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val pair = item as Pair<String, Long>
                    if (pair.first.isNotEmpty()) processedPairs.add(pair)
                }
            }
        }
        if (processedPairs.isEmpty()) return Pair("", 0L)
        return processedPairs.reduce { acc, pair -> merge(acc, pair) }
    }

    open fun toCardinal(value: Number): String {
        return when (value) {
            is Int -> toCardinal(value.toLong())
            is Long -> {
                var num = value
                var prefix = ""
                if (num < 0) {
                    num = abs(num)
                    prefix = negWord.trim() + " "
                }

                if (this.MAXVAL > 0 && num >= this.MAXVAL) {
                    throw IllegalArgumentException(errMsgTooBig.format(num, this.MAXVAL))
                }

                if (num == 0L && cards.containsKey(0L)) {
                    return title(prefix + cards[0L]!!)
                }
                if (num == 0L && !cards.containsKey(0L)) { // Should ideally not happen if setup is good
                    return title(prefix + "zero") // Fallback if 0 not in cards
                }


                val structuredNum = splitnum(num)
                val (words, _) = clean(structuredNum)
                return title(prefix + words)
            }
            is Float -> toCardinalFloat(value.toDouble())
            is Double -> toCardinalFloat(value)
            is BigDecimal -> {
                val maxValBdFromCards = if (this.MAXVAL > 0 && this.MAXVAL != Long.MAX_VALUE) BigDecimal(this.MAXVAL) else null
                if (maxValBdFromCards != null && value.abs() >= maxValBdFromCards) {
                    throw IllegalArgumentException(errMsgTooBig.format(value.toPlainString(), maxValBdFromCards.toPlainString()))
                }
                try {
                    value.abs().toBigInteger().longValueExact()
                } catch (e: ArithmeticException) {
                    throw IllegalArgumentException(errMsgTooBig.format(value.toPlainString(), "Long range for integer part. Max representable integer part magnitude is ${Long.MAX_VALUE}."))
                }
                return toCardinalFloat(value)
            }
            else -> throw IllegalArgumentException(errMsgNonNum.format(value::class.simpleName))
        }
    }

    open fun toCardinalFloat(value: Double): String {
        val bdValue = BigDecimal(value.toString())
        return toCardinalFloatShared(bdValue, value < 0)
    }

    open fun toCardinalFloat(value: BigDecimal): String {
        return toCardinalFloatShared(value, value < BigDecimal.ZERO)
    }

    private fun toCardinalFloatShared(bdValue: BigDecimal, isNegative: Boolean): String {
        val absBdValue = bdValue.abs()
        val pre = absBdValue.toBigInteger().longValueExact()

        val scale = bdValue.scale()
        val effectivePrecision = if (scale < 0) 0 else scale

        var postStr = ""
        if (effectivePrecision > 0) {
            val fractionalDigits = absBdValue.subtract(BigDecimal(absBdValue.toBigInteger()))
                .movePointRight(effectivePrecision)
                .setScale(0, RoundingMode.HALF_UP)
                .toBigInteger().toString()
            postStr = fractionalDigits.padStart(effectivePrecision, '0')
        }


        var cardinalPre = toCardinal(pre)
        // Handle "minus zero" for negative numbers where integer part is 0
        if (isNegative && pre == 0L && !cardinalPre.startsWith(negWord.trim()) && cardinalPre == cards[0L]) {
            // If toCardinal(0) is just "zero", prefix it with negWord
            cardinalPre = negWord.trim() + " " + cardinalPre
        }
        var fullStr = if (isNegative && pre != 0L) negWord.trim() + " " + cardinalPre else cardinalPre
        if (isNegative && pre == 0L) { // Ensures "minus zero point five" starts correctly
            fullStr = cardinalPre // cardinalPre should already be "minus zero"
        }


        if (postStr.isNotEmpty() && (postStr.any { it != '0'} || bdValue.scale() > 0 )) {
            fullStr += " " + title(pointWord)
            for (charDigit in postStr) {
                val digit = charDigit.toString().toInt()
                fullStr += " " + toCardinal(digit)
            }
        }

        return fullStr.trim()
    }

    fun title(value: String): String {
        if (!isTitle) return value
        return value.split(" ").joinToString(" ") { word ->
            if (excludeTitle.any { it.equals(word, ignoreCase = true) } || word.isBlank()) word
            else word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun verifyOrdinal(value: Long) {
        if (value < 0) throw IllegalArgumentException(errMsgNegOrd.format(value))
    }

    fun verifyOrdinal(value: Number) {
        when (value) {
            is Double -> if (value != floor(value)) throw IllegalArgumentException(errMsgFloatOrd.format(value))
            is Float -> if (value != floor(value)) throw IllegalArgumentException(errMsgFloatOrd.format(value))
            is BigDecimal -> if (value.stripTrailingZeros().scale() > 0) throw IllegalArgumentException(errMsgFloatOrd.format(value))
        }
        if (value.toDouble() < 0) throw IllegalArgumentException(errMsgNegOrd.format(value))
    }

    open fun toOrdinal(value: Number): String {
        verifyOrdinal(value)
        return toCardinal(value.toLong())
    }

    open fun toOrdinalNum(value: Number): String {
        verifyOrdinal(value)
        return value.toLong().toString()
    }

    abstract fun pluralize(n: Long, forms: Pair<String, String>): String
    abstract fun getCurrencyForms(currencyCode: String): Pair<Pair<String, String>, Pair<String, String>>
    abstract fun getCurrencyAdjective(currencyCode: String): String?

    open fun toCurrency(
        value: Number,
        currency: String = "EUR",
        centsVerbose: Boolean = true,
        separator: String = ",",
        adjective: Boolean = false
    ): String {
        val (integerPart, centsPart, isNegative) = parseCurrencyParts(value, isIntWithCentsProvided = false)

        val (mainForms, subForms) = getCurrencyForms(currency.uppercase())
        var (cr1Singular, cr1Plural) = mainForms
        val (cr2Singular, cr2Plural) = subForms

        if (adjective) {
            getCurrencyAdjective(currency.uppercase())?.let { adj ->
                cr1Singular = "$adj $cr1Singular"
                cr1Plural = "$adj $cr1Plural"
            }
        }

        val minusStr = if (isNegative && integerPart == 0L && centsPart == 0) "" // Avoid "minus zero dollars" if value is truly 0
        else if (isNegative) "${negWord.trim()} " else ""

        var moneyStr = toCardinal(integerPart)
        // if "minus zero" from toCardinal(0) and isNegative, strip "minus" as it's handled by minusStr
        if (isNegative && integerPart == 0L && moneyStr.startsWith(negWord.trim())) {
            moneyStr = moneyStr.substringAfter(negWord.trim()).trimStart()
        }


        val mainUnitText = pluralize(integerPart, Pair(cr1Singular, cr1Plural))

        // Construct integer part string, handling "minus zero dollars" for negative zero.
        val moneyAndMainUnit = if (isNegative && integerPart == 0L && centsPart == 0) { // True zero like -0.00
            // If toCardinal(0) yields "zero", then use it. If it yields "minus zero", adapt.
            var zeroCardinal = toCardinal(0L)
            if (zeroCardinal.startsWith(negWord.trim())) zeroCardinal = zeroCardinal.substringAfter(negWord.trim()).trimStart()
            "$minusStr$zeroCardinal $mainUnitText"
        } else {
            "$minusStr$moneyStr $mainUnitText"
        }


        val hasDecimalInInputOrNonZeroCents = when(value) {
            is Float, is Double, is BigDecimal -> true
            is String -> value.contains('.')
            else -> false
        } || centsPart > 0

        if (hasDecimalInInputOrNonZeroCents) {
            // For "zero dollars and zero cents", ensure "zero cents" is appended if centsVerbose.
            if (integerPart == 0L && centsPart == 0 && !centsVerbose) { // e.g. 0.00 with centsVerbose=false -> "zero dollars, 00"
                val centsStr = "%02d".format(centsPart)
                return "$moneyAndMainUnit$separator $centsStr"
            }

            val centsStr = if (centsVerbose) toCardinal(centsPart.toLong()) else "%02d".format(centsPart)
            return if (centsVerbose) {
                val subUnitText = pluralize(centsPart.toLong(), Pair(cr2Singular, cr2Plural))
                "$moneyAndMainUnit$separator $centsStr $subUnitText"
            } else {
                "$moneyAndMainUnit$separator $centsStr"
            }
        } else {
            return moneyAndMainUnit
        }
    }

    open fun toYear(value: Number, options: Map<String, Any> = emptyMap()): String {
        val numValue = value.toLong() // Convert to Long for consistent handling
        var textInput = numValue
        var suffix: String? = options["suffix"] as? String

        if (numValue < 0) {
            textInput = abs(numValue)
            if (suffix == null) suffix = "BC" // Default suffix for negative years if not provided
        }

        var text = toCardinal(textInput)

        // If original was negative, and toCardinal prepended negWord, remove it if suffix implies negativity (like BC)
        if (numValue < 0 && text.startsWith(negWord.trim()) && suffix == "BC") {
            text = text.substringAfter(negWord.trim()).trimStart()
        }

        return if (suffix != null) "$text $suffix" else text
    }
}