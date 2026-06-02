package com.example.commonlibrary

import android.content.Context
import android.widget.Toast
import com.example.bignum.BigNum

class GameManager(
    private val context: Context,
    private val startingIncrement: BigNum,
    private val startingUpgradePrice: BigNum
) {

    private var score: BigNum = BigNum.ZERO
    private var scoreIncrement: BigNum = startingIncrement
    private var upgradePrice: BigNum = startingUpgradePrice

    private val msg = "Not enough points"

    fun startGame() {
        score = BigNum.ZERO
        scoreIncrement = startingIncrement
        upgradePrice = startingUpgradePrice
    }

    fun getScore(): BigNum = score

    fun updateScore() {
        score += scoreIncrement
    }

    private fun upgradeMultiplier() {
        scoreIncrement *= BigNum.from(2L)
    }

    private fun upgradePriceIncrease() {
        upgradePrice *= BigNum.from(2L)
    }

    fun buyUpgrade(): Boolean {
        return if (score >= upgradePrice) {
            score -= upgradePrice
            upgradePriceIncrease()
            upgradeMultiplier()
            true
        } else {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            false
        }
    }

    fun getUpgradePrice(): BigNum = upgradePrice


    fun devMultiplyScore(factor: Long) {
        score *= factor
    }
}
