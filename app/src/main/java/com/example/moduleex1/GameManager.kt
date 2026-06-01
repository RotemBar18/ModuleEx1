package com.example.moduleex1

import android.content.Context
import android.widget.Toast

class GameManager(private val context: Context) {

    private var score: Long = 0
    private var scoreIncrement:Long = 10

    private var upgradePrice : Long = 100

    private val msg = "Not enough points"

    fun startGame() {
        //reset all parameters
        score = 0L
        scoreIncrement = 10L
        upgradePrice = 100L
    }

    fun getScore(): Long {
        return score
    }

    fun updateScore() {
        score += scoreIncrement
    }

    private fun upgradeMultiplier() {
        scoreIncrement *= 2
    }

    private fun upgradePriceIncrease() {
        upgradePrice *= 4
    }

    fun buyUpgrade(): Boolean {
        if (score >= upgradePrice){
            score -= upgradePrice
            upgradePriceIncrease()
            upgradeMultiplier()
            return true
        } else {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            return false
        }
    }

    fun getUpgradePrice(): Long {
        return upgradePrice
    }
}



