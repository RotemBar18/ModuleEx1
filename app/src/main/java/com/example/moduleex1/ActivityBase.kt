package com.example.moduleex1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import com.example.moduleex1.databinding.ActivityMainBinding

class ActivityBase : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val gameManager = GameManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.mainLBLScoreValue.text = gameManager.getScore().toString()
        binding.mainIMGClickableObject.setOnClickListener {
            gameManager.updateScore()
            binding.mainLBLScoreValue.text = gameManager.getScore().toString()
        }
        binding.mainBTNUpgrade.text = "Upgrade: ${gameManager.getUpgradePrice()}"
        binding.mainBTNUpgrade.setOnClickListener {
            if (gameManager.buyUpgrade()){
                binding.mainLBLScoreValue.text = gameManager.getScore().toString()
                binding.mainBTNUpgrade.text = "Upgrade: ${gameManager.getUpgradePrice()}"
            }
        }

    }


}