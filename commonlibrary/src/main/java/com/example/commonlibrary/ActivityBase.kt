package com.example.commonlibrary

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bignum.BigNum
import com.example.bignum.BigNumFormat
import com.example.commonlibrary.databinding.ActivityBaseBinding
import com.example.commonlibrary.utils.SingleSoundPlayer
import com.example.commonlibrary.utils.VisualMilestone

abstract class ActivityBase : AppCompatActivity() {

    abstract var sound: Int
    private lateinit var binding: ActivityBaseBinding
    private lateinit var gameManager: GameManager
    private val ssp: SingleSoundPlayer by lazy { SingleSoundPlayer(this) }
    private var glowAnimator: AnimatorSet? = null
    private var currentFormat: BigNumFormat = BigNumFormat.SUFFIX

    abstract fun getAppTitle(): String
    abstract fun getUpgradeLabel(): String
    abstract fun getScoreIncrement(): BigNum
    abstract fun getStartingUpgradePrice(): BigNum
    abstract fun getMilestones(): List<VisualMilestone>

    private val sortedMilestones by lazy { getMilestones().sortedDescending() }
    private var currentMilestoneResId: Int? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyGradientBackground()
        applyGlowRingTint()
        startGlowPulse()

        binding.mainTXTTitle.text = getAppTitle()
        gameManager = GameManager(this, getScoreIncrement(), getStartingUpgradePrice())
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        ssp.release()
        glowAnimator?.cancel()
    }

    //VISUAL

    private fun applyGradientBackground() {
        val start = resolveThemeColor(R.attr.gradientStartColor)
        val end   = resolveThemeColor(R.attr.gradientEndColor)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(start, end)
        )
        binding.main.background = gradient
    }

    private fun applyGlowRingTint() {
        val glowColor = resolveThemeColor(R.attr.glowColor)
        binding.mainVIEWGlowRing.backgroundTintList = ColorStateList.valueOf(glowColor)
    }


    private fun startGlowPulse() {
        val ring = binding.mainVIEWGlowRing
        val duration = 1600L
        val interp = AccelerateDecelerateInterpolator()

        fun pulsator(property: String, from: Float, to: Float) =
            ObjectAnimator.ofFloat(ring, property, from, to).apply {
                this.duration    = duration
                repeatCount      = ValueAnimator.INFINITE
                repeatMode       = ValueAnimator.REVERSE
                interpolator     = interp
            }

        glowAnimator = AnimatorSet().apply {
            playTogether(
                pulsator("scaleX", 0.88f, 1.18f),
                pulsator("scaleY", 0.88f, 1.18f),
                pulsator("alpha",  0.35f, 1.00f)
            )
            start()
        }
    }

    private fun resolveThemeColor(attrId: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(attrId, tv, true)
        return tv.data
    }

//VIEWS
    private fun initViews() {
        updateScoreDisplay()
        updateUpgradeButton()

        // Dev Controls
        binding.mainBTNFormat.setOnClickListener {
            currentFormat = if (currentFormat == BigNumFormat.SUFFIX) {
                BigNumFormat.SCIENTIFIC
            } else {
                BigNumFormat.SUFFIX
            }
            binding.mainBTNFormat.text = "FMT: ${currentFormat.name}"
            updateScoreDisplay()
            updateUpgradeButton()
        }

        binding.mainBTNDevMultiply.setOnClickListener {
            if (gameManager.getScore().isZero) {
                gameManager.updateScore() // give initial points if 0
            }
            gameManager.devMultiplyScore(100L)
            updateScoreDisplay()
            updateUpgradeButton()
        }

        binding.mainIMGClickableObject.setOnClickListener {
            //onclick animation
            binding.mainIMGClickableObject.animate()
                .scaleX(0.82f).scaleY(0.82f)
                .setDuration(55)
                .withEndAction {
                    binding.mainIMGClickableObject.animate()
                        .scaleX(1.0f).scaleY(1.0f)
                        .setDuration(140)
                        .start()
                }.start()

            gameManager.updateScore()
            ssp.playSound(sound)
            binding.mainIMGClickableObject.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

            // score animation
            binding.mainLBLScore.animate()
                .scaleX(1.20f).scaleY(1.20f)
                .setDuration(70)
                .withEndAction {
                    binding.mainLBLScore.animate()
                        .scaleX(1.0f).scaleY(1.0f)
                        .setDuration(130)
                        .start()
                }.start()

            updateScoreDisplay()
            updateUpgradeButton()
        }

        binding.mainBTNUpgrade.setOnClickListener {
            if (gameManager.buyUpgrade()) {
                updateScoreDisplay()
                updateUpgradeButton()
            }
        }
    }

//MECHANICS
    private fun updateScoreDisplay() {
        val currentScore = gameManager.getScore()
        binding.mainLBLScore.text = currentScore.format(currentFormat)


        val activeMilestone = sortedMilestones.firstOrNull { it.threshold <= currentScore }
        val targetResId = activeMilestone?.drawableResId ?: sortedMilestones.last().drawableResId
        
        val isInitialSet = currentMilestoneResId == null

        if (targetResId != currentMilestoneResId) {
            currentMilestoneResId = targetResId
            binding.mainIMGClickableObject.setImageResource(targetResId)
            
            if (!isInitialSet) {
                binding.mainIMGClickableObject.animate()
                    .scaleX(1.4f).scaleY(1.4f)
                    .setDuration(300)
                    .withEndAction {
                        binding.mainIMGClickableObject.animate()
                            .scaleX(1.0f).scaleY(1.0f)
                            .setDuration(300)
                            .start()
                    }.start()
            }
        }
    }

    private fun updateUpgradeButton() {
        val canAfford = gameManager.getScore() >= gameManager.getUpgradePrice()
        binding.mainBTNUpgrade.text =
            "${getUpgradeLabel()}  ·  ${gameManager.getUpgradePrice().format(currentFormat)}"
        binding.mainBTNUpgrade.isEnabled = canAfford
        binding.mainBTNUpgrade.animate()
            .alpha(if (canAfford) 1.0f else 0.45f)
            .setDuration(200)
            .start()
    }

}
