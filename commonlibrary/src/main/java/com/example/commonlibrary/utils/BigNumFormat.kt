package com.example.bignum

/**
 * Controls how a [BigNum] is displayed when calling [BigNum.format].
 *
 * - [SCIENTIFIC] → e.g. "1.23e24"
 * - [SUFFIX]     → e.g. "1.23 Sp" (Cookie Clicker / idle-game style)
 */
enum class BigNumFormat {
    SCIENTIFIC,
    SUFFIX
}
