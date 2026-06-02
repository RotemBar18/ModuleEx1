package com.example.eggclicker

import com.example.bignum.BigNum
import com.example.commonlibrary.ActivityBase
import com.example.commonlibrary.utils.VisualMilestone

class EggActivity : ActivityBase() {

    override var sound: Int = R.raw.egg_sound
    override fun getAppTitle(): String = "Egg Clicker"
    override fun getUpgradeLabel(): String = "Upgrade Coop"
    override fun getScoreIncrement(): BigNum = BigNum.from(1L)
    override fun getStartingUpgradePrice(): BigNum = BigNum.from(10L)

    override fun getMilestones(): List<VisualMilestone> {
        return listOf(
            VisualMilestone(BigNum.ZERO, R.drawable.egg_1_plain),
            VisualMilestone(BigNum.from(9_000_000L), R.drawable.egg_2_cracked),
            VisualMilestone(BigNum.from(9_000_000_000L), R.drawable.egg_3_chick),
            VisualMilestone(BigNum.from(9_000_000_000_000L), R.drawable.egg_4_rooster),
            VisualMilestone(BigNum.from(9_000_000_000_000_000L), R.drawable.egg_5_phoenix),
            VisualMilestone(BigNum.from(9_000_000_000_000_000_000L), R.drawable.egg_6_dragon)
        )
    }
}
