package com.zenfit.ai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zenfit.ai.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.apply {
            cardFitness.setOnClickListener {
                Toast.makeText(this@MainActivity, "AI Fitness Coach - Coming Soon!", Toast.LENGTH_SHORT).show()
            }
            cardMeditation.setOnClickListener {
                Toast.makeText(this@MainActivity, "AI Meditation - Coming Soon!", Toast.LENGTH_SHORT).show()
            }
            cardNutrition.setOnClickListener {
                Toast.makeText(this@MainActivity, "Smart Nutrition - Coming Soon!", Toast.LENGTH_SHORT).show()
            }
            cardAICoach.setOnClickListener {
                Toast.makeText(this@MainActivity, "AI Health Coach - Coming Soon!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}