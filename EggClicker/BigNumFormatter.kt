package com.example.bignum

import kotlin.math.abs

/**
 * Handles all display formatting for [BigNum].
 *
 * Two modes, switchable via [BigNumFormat]:
 *
 *  • [BigNumFormat.SCIENTIFIC] → "1.23e24"
 *  • [BigNumFormat.SUFFIX]     → "1.23 Sp"
 *
 * Suffix table (every 3 exponents = one tier):
 *
 * | Exponent range | Suffix | Name        |
 * |---------------|--------|-------------|
 * | 0–2           | (none) | units       |
 * | 3–5           | K      | Thousand    |
 * | 6–8           | M      | Million     |
 * | 9–11          | B      | Billion     |
 * | 12–14         | T      | Trillion    |
 * | 15–17         | Qa     | Quadrillion |
 * | 18–20         | Qi     | Quintillion |
 * | 21–23         | Sx     | Sextillion  |
 * | 24–26         | Sp     | Septillion  |
 * | 27–29         | Oc     | Octillion   |
 * | 30–32         | No     | Nonillion   |
 * | 33–35         | Dc     | Decillion   |
 * | 36–38         | UDc    | Undecillion |
 * | ...beyond...  | scientific fallback |
 *
 * Beyond the suffix table, the formatter automatically falls back to
 * scientific notation so you never get an ugly crash or "???" display.
 */
object BigNumFormatter {

    // ── Suffix table ─────────────────────────────────────────────────────────
    //  Index i → exponent range [i*3, i*3+2].  Index 0 = no suffix (< 1 000).

    private val SUFFIXES = arrayOf(
        "",       // 0  — units            (1 – 999)
        "K",      // 1  — Thousand         (1K)
        "M",      // 2  — Million          (1M)
        "B",      // 3  — Billion          (1B)
        "T",      // 4  — Trillion         (1T)
        "Qa",     // 5  — Quadrillion
        "Qi",     // 6  — Quintillion
        "Sx",     // 7  — Sextillion
        "Sp",     // 8  — Septillion
        "Oc",     // 9  — Octillion
        "No",     // 10 — Nonillion
        "Dc",     // 11 — Decillion
        "UDc",    // 12 — Undecillion
        "DDc",    // 13 — Duodecillion
        "TDc",    // 14 — Tredecillion
        "QaDc",   // 15 — Quattuordecillion
        "QiDc",   // 16 — Quindecillion
        "SxDc",   // 17 — Sexdecillion
        "SpDc",   // 18 — Septendecillion
        "OcDc",   // 19 — Octodecillion
        "NoDc",   // 20 — Novemdecillion
        "Vg",     // 21 — Vigintillion
        "UVg",    // 22 — Unvigintillion
        "DVg",    // 23 — Duovigintillion
        "TVg",    // 24 — Trevigintillion
        "QaVg",   // 25 — Quattuorvigintillion
        "QiVg",   // 26 — Quinvigintillion
        "SxVg",   // 27 — Sexvigintillion
        "SpVg",   // 28 — Septenvigintillion
        "OcVg",   // 29 — Octovigintillion
        "NoVg",   // 30 — Novemvigintillion
        "Tg",     // 31 — Trigintillion
        "UTg",    // 32 — Untrigintillion
        "DTg",    // 33 — Duotrigintillion (= Googol range)
    )

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Format [num] according to [format] with [decimals] decimal places.
     */
    fun format(num: BigNum, format: BigNumFormat, decimals: Int = 2): String {
        if (num.isZero) return "0"
        return when (format) {
            BigNumFormat.SCIENTIFIC -> formatScientific(num, decimals)
            BigNumFormat.SUFFIX     -> formatSuffix(num, decimals)
        }
    }

    // ── Scientific ────────────────────────────────────────────────────────────

    /**
     * Returns a string like "1.23e24" or "-4.56e100".
     */
    fun formatScientific(num: BigNum, decimals: Int = 2): String {
        val sign  = if (num.isNegative) "-" else ""
        val mant  = abs(num.mantissa)
        return "${sign}${mant.fmt(decimals)}e${num.exponent}"
    }

    // ── Suffix ────────────────────────────────────────────────────────────────

    /**
     * Returns a string like "1.23 Sp" or "999.99" (no suffix for small numbers).
     * Falls back to scientific notation when beyond the suffix table.
     */
    fun formatSuffix(num: BigNum, decimals: Int = 2): String {
        val sign   = if (num.isNegative) "-" else ""
        val exp    = num.exponent
        val mant   = abs(num.mantissa)

        // Negative exponents (0.001, etc.) — show as plain decimal
        if (exp < 0) {
            return sign + num.toDouble().fmt(decimals)
        }

        // Which tier?  tier 0 = units (exp 0-2), tier 1 = K (exp 3-5), …
        val tier = (exp / 3).toInt()

        // Beyond our suffix table → fall back to scientific
        if (tier >= SUFFIXES.size) {
            return formatScientific(num, decimals)
        }

        // Shift mantissa so the display value is in [1, 1000)
        val remainder  = (exp % 3).toInt()           // 0, 1, or 2
        val displayVal = mant * Math.pow(10.0, remainder.toDouble())

        val suffix = SUFFIXES[tier]
        return if (suffix.isEmpty()) {
            sign + displayVal.fmt(decimals)
        } else {
            "${sign}${displayVal.fmt(decimals)} $suffix"
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Format a Double with exactly [decimals] decimal places. */
    private fun Double.fmt(decimals: Int): String = "%.${decimals}f".format(this)
}
