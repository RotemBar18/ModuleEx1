package com.example.commonlibrary.utils

import com.example.bignum.BigNum

/**
 * Maps a score threshold to a specific drawable resource.
 * The clickable object's image will change as the player reaches these milestones.
 */
data class VisualMilestone(
    val threshold: BigNum,
    val drawableResId: Int
) : Comparable<VisualMilestone> {
    override fun compareTo(other: VisualMilestone): Int {
        return this.threshold.compareTo(other.threshold)
    }
}
