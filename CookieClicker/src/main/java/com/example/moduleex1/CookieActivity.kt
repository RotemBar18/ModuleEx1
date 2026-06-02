package com.example.moduleex1

import com.example.bignum.BigNum
import com.example.commonlibrary.ActivityBase
import com.example.commonlibrary.utils.VisualMilestone

class CookieActivity : ActivityBase() {

    override var sound: Int = R.raw.cookie_sound
    override fun getAppTitle(): String = "Cookie Clicker"
    override fun getUpgradeLabel(): String = "Upgrade Oven"
    override fun getScoreIncrement(): BigNum = BigNum.from(10L)
    override fun getStartingUpgradePrice(): BigNum = BigNum.from(100L)

    override fun getMilestones(): List<VisualMilestone> {
        return listOf(
            VisualMilestone(BigNum.ZERO, R.drawable.cookie_1_dough),
            VisualMilestone(BigNum.from(9_000_000L), R.drawable.cookie_2_simple),
            VisualMilestone(BigNum.from(9_000_000_000L), R.drawable.cookie_3_choc),
            VisualMilestone(BigNum.from(9_000_000_000_000L), R.drawable.cookie_4_cake),
            VisualMilestone(BigNum.from(9_000_000_000_000_000L), R.drawable.cookie_5_factory),
            VisualMilestone(BigNum.from(9_000_000_000_000_000_000L), R.drawable.cookie_6_cosmic)
        )
    }
}
