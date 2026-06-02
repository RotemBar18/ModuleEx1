package com.example.bignum

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * A number type that can represent values far beyond Long or Double,
 * ideal for idle games (cookie counts, DPS, boss HP, etc.).
 *
 * Internally stored as  mantissa × 10^exponent
 * where mantissa is always in [1.0, 10.0) unless the value is 0.
 *
 * Maximum representable value:  ~9.99 × 10^(Long.MAX_VALUE)
 *
 * Example usage:
 * ```
 * val cookies = BigNum(1.5, 24)   // 1.5 × 10^24
 * val dmg     = BigNum.from(9_999_999L)
 * val total   = cookies + dmg
 * println(total.format(BigNumFormat.SUFFIX))   // "1.50 Sp"
 * println(total.format(BigNumFormat.SCIENTIFIC))// "1.50e24"
 * ```
 */
class BigNum private constructor(
    /** Significant digits, always in [1.0, 10.0) unless value is 0. */
    val mantissa: Double,
    /** Power of 10. */
    val exponent: Long
) : Comparable<BigNum> {

    // ──────────────────────────────────────────────────────────────────────────
    // Companion — constructors & constants
    // ──────────────────────────────────────────────────────────────────────────

    companion object {
        val ZERO = BigNum(0.0, 0)
        val ONE  = BigNum(1.0, 0)

        /** Build from a plain Double (e.g. 1_500_000.0). */
        fun from(value: Double): BigNum {
            if (value == 0.0) return ZERO
            if (!value.isFinite()) return ZERO
            val isNeg   = value < 0
            val abs     = abs(value)
            val exp     = floor(log10(abs)).toLong()
            val mant    = abs / 10.0.pow(exp.toDouble())
            return normalize(if (isNeg) -mant else mant, exp)
        }

        /** Build from a Long. */
        fun from(value: Long): BigNum = from(value.toDouble())

        /** Build from an Int. */
        fun from(value: Int): BigNum = from(value.toLong())

        /**
         * Build directly from mantissa + exponent.
         * The value is automatically normalized.
         */
        operator fun invoke(mantissa: Double, exponent: Long): BigNum =
            normalize(mantissa, exponent)

        /**
         * Parse a string produced by [BigNum.toRawString] ("mantissa|exponent")
         * or a plain decimal like "1500000" / "1.5e24".
         */
        fun parse(s: String): BigNum {
            val trimmed = s.trim()
            // Internal serialization format: "1.5|24"
            if ('|' in trimmed) {
                val parts = trimmed.split('|')
                return normalize(parts[0].toDouble(), parts[1].toLong())
            }
            // Scientific notation: "1.5e24" or "1.5E24"
            val eIdx = trimmed.indexOfFirst { it == 'e' || it == 'E' }
            if (eIdx >= 0) {
                val mant = trimmed.substring(0, eIdx).toDouble()
                val exp  = trimmed.substring(eIdx + 1).toLong()
                return normalize(mant, exp)
            }
            // Plain number
            return from(trimmed.toDouble())
        }

        // ── Internal helpers ──────────────────────────────────────────────────

        /**
         * Ensures mantissa stays in [1, 10) (or (-10, -1] for negatives),
         * adjusting exponent accordingly.
         */
        internal fun normalize(mantissa: Double, exponent: Long): BigNum {
            if (mantissa == 0.0 || !mantissa.isFinite()) return ZERO
            var m = mantissa
            var e = exponent
            // Bring into [1, 10) range
            while (abs(m) >= 10.0) { m /= 10.0; e++ }
            while (abs(m) < 1.0 && m != 0.0) { m *= 10.0; e-- }
            return BigNum(m, e)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Properties
    // ──────────────────────────────────────────────────────────────────────────

    val isZero: Boolean     get() = mantissa == 0.0
    val isNegative: Boolean get() = mantissa < 0.0
    val isPositive: Boolean get() = mantissa > 0.0

    /** Absolute value */
    val abs: BigNum get() = if (isNegative) BigNum(-mantissa, exponent) else this

    /** Negated */
    operator fun unaryMinus(): BigNum = BigNum(-mantissa, exponent)

    // ──────────────────────────────────────────────────────────────────────────
    // Arithmetic
    // ──────────────────────────────────────────────────────────────────────────

    operator fun plus(other: BigNum): BigNum {
        if (isZero) return other
        if (other.isZero) return this
        // Align exponents: bring the smaller one up to match
        val expDiff = exponent - other.exponent
        return when {
            expDiff >= 17  -> this                // other is negligible
            expDiff <= -17 -> other               // this is negligible
            expDiff >= 0   -> normalize(
                mantissa + other.mantissa / 10.0.pow(expDiff.toDouble()), exponent
            )
            else           -> normalize(
                mantissa / 10.0.pow((-expDiff).toDouble()) + other.mantissa, other.exponent
            )
        }
    }

    operator fun minus(other: BigNum): BigNum = this + (-other)

    operator fun times(other: BigNum): BigNum {
        if (isZero || other.isZero) return ZERO
        return normalize(mantissa * other.mantissa, exponent + other.exponent)
    }

    operator fun div(other: BigNum): BigNum {
        if (other.isZero) throw ArithmeticException("BigNum division by zero")
        if (isZero) return ZERO
        return normalize(mantissa / other.mantissa, exponent - other.exponent)
    }

    /** Raises this number to an integer power. */
    fun pow(n: Int): BigNum {
        if (n == 0) return ONE
        if (n < 0) return ONE / pow(-n)
        var result = ONE
        var base = this
        var exp = n
        while (exp > 0) {
            if (exp % 2 == 1) result *= base
            base *= base
            exp /= 2
        }
        return result
    }

    // Convenience overloads with primitives
    operator fun plus(value: Long)   = this + from(value)
    operator fun plus(value: Double) = this + from(value)
    operator fun minus(value: Long)  = this - from(value)
    operator fun minus(value: Double)= this - from(value)
    operator fun times(value: Long)  = this * from(value)
    operator fun times(value: Double)= this * from(value)
    operator fun div(value: Long)    = this / from(value)
    operator fun div(value: Double)  = this / from(value)

    // ──────────────────────────────────────────────────────────────────────────
    // Comparison
    // ──────────────────────────────────────────────────────────────────────────

    override fun compareTo(other: BigNum): Int {
        // Handle sign differences
        val thisSign  = mantissa.compareTo(0.0)
        val otherSign = other.mantissa.compareTo(0.0)
        if (thisSign != otherSign) return thisSign.compareTo(otherSign)
        if (thisSign == 0) return 0          // both zero

        val expCmp = exponent.compareTo(other.exponent)
        return if (expCmp != 0) {
            if (isNegative) -expCmp else expCmp
        } else {
            mantissa.compareTo(other.mantissa)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BigNum) return false
        return mantissa == other.mantissa && exponent == other.exponent
    }

    override fun hashCode(): Int = 31 * mantissa.hashCode() + exponent.hashCode()

    // ──────────────────────────────────────────────────────────────────────────
    // Conversion
    // ──────────────────────────────────────────────────────────────────────────

    /** Lossy conversion — only meaningful for small numbers. */
    fun toDouble(): Double = mantissa * 10.0.pow(exponent.toDouble())

    /** Lossy conversion — only meaningful for small numbers (≤ Long.MAX_VALUE). */
    fun toLong(): Long = toDouble().toLong()

    // ──────────────────────────────────────────────────────────────────────────
    // Formatting
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Format this number using the given format and decimal count.
     *
     * @param format   [BigNumFormat.SCIENTIFIC] or [BigNumFormat.SUFFIX]
     * @param decimals number of decimal places (default 2)
     */
    fun format(
        format: BigNumFormat = BigNumFormat.SUFFIX,
        decimals: Int = 2
    ): String = BigNumFormatter.format(this, format, decimals)

    /**
     * Serialization string: "mantissa|exponent". Use [parse] to restore.
     */
    fun toRawString(): String = "$mantissa|$exponent"

    override fun toString(): String = format(BigNumFormat.SUFFIX)
}
