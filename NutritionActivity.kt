package com.zenfit.ai.nutrition

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zenfit.ai.databinding.ActivityNutritionBinding
import java.util.*

class NutritionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNutritionBinding
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var dailyCalorieGoal = 2000
    private var currentCalories = 0
    
    companion object {
        private const val CAMERA_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_CODE = 1002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNutritionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        loadNutritionData()
    }
    
    private fun setupClickListeners() {
        binding.btnTrackMeal.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        
        binding.btnGenerateMealPlan.setOnClickListener {
            generatePersonalizedMealPlan()
        }
        
        binding.btnAddWater.setOnClickListener {
            addWaterIntake()
        }
        
        binding.btnNutritionTips.setOnClickListener {
            generateNutritionTips()
        }
    }
    
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }
    
    private fun openCamera() {
        val cameraIntent = android.content.Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as Bitmap
            analyzeFoodImage(photo)
        }
    }
    
    private fun analyzeFoodImage(bitmap: Bitmap) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        val image = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Extract text from image and estimate nutrition
                val detectedText = visionText.text
                estimateNutritionFromText(detectedText)
                binding.progressBar.visibility = android.view.View.GONE
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                showError("Could not analyze image. Please try again.")
            }
    }
    
    private fun estimateNutritionFromText(text: String) {
        // Simple nutrition estimation based on detected text
        // In a real app, you would use a food database API
        val calories = when {
            text.contains("pizza", ignoreCase = true) -> 300
            text.contains("salad", ignoreCase = true) -> 150
            text.contains("burger", ignoreCase = true) -> 400
            text.contains("pasta", ignoreCase = true) -> 350
            text.contains("rice", ignoreCase = true) -> 200
            text.contains("chicken", ignoreCase = true) -> 250
            text.contains("fish", ignoreCase = true) -> 200
            text.contains("fruit", ignoreCase = true) -> 80
            else -> 250 // Default estimate
        }
        
        addMeal(calories, "Detected from image: $text")
    }
    
    private fun addMeal(calories: Int, description: String) {
        currentCalories += calories
        
        // Update UI
        updateCalorieDisplay()
        
        // Add to meal history
        val meal = Meal(
            calories = calories,
            description = description,
            timestamp = Date()
        )
        
        addMealToHistory(meal)
        saveNutritionData()
    }
    
    private fun updateCalorieDisplay() {
        binding.apply {
            tvCurrentCalories.text = "Current: $currentCalories cal"
            tvRemainingCalories.text = "Remaining: ${dailyCalorieGoal - currentCalories} cal"
            
            // Update progress
            val progress = (currentCalories * 100) / dailyCalorieGoal
            progressBarCalories.progress = progress
        }
    }
    
    private fun addMealToHistory(meal: Meal) {
        // Add meal to the history list
        val currentHistory = binding.tvMealHistory.text.toString()
        val newEntry = "${meal.description} - ${meal.calories} cal"
        binding.tvMealHistory.text = if (currentHistory.isEmpty()) {
            newEntry
        } else {
            "$newEntry\n$currentHistory"
        }
    }
    
    private fun generatePersonalizedMealPlan() {
        binding.progressBarMealPlan.visibility = android.view.View.VISIBLE
        
        // Simulate AI meal plan generation
        android.os.Handler(mainLooper).postDelayed({
            val mealPlan = when {
                currentCalories > dailyCalorieGoal -> """
                    You've reached your daily calorie goal. For your next meal, consider:
                    
                    🥗 Light Salad with mixed greens
                    🍵 Herbal tea
                    🥒 Fresh vegetables with hummus
                    
                    Focus on low-calorie, nutrient-dense foods.
                """.trimIndent()
                
                else -> """
                    Based on your remaining ${dailyCalorieGoal - currentCalories} calories:
                    
                    🍗 Grilled chicken breast (250 cal)
                    🥦 Steamed broccoli (50 cal)
                    🍚 Brown rice (150 cal)
                    🥗 Side salad with olive oil (100 cal)
                    
                    Total: ~550 calories
                """.trimIndent()
            }
            
            binding.tvMealPlan.text = mealPlan
            binding.progressBarMealPlan.visibility = android.view.View.GONE
        }, 2000)
    }
    
    private fun addWaterIntake() {
        val currentWater = binding.tvWaterIntake.text.toString().split(":")[1].trim().toIntOrNull() ?: 0
        val newWater = currentWater + 1
        binding.tvWaterIntake.text = "Water Intake: $newWater glasses"
        
        if (newWater >= 8) {
            binding.tvWaterStatus.text = "Great job! You've reached your daily water goal! 💧"
        } else {
            val remaining = 8 - newWater
            binding.tvWaterStatus.text = "Keep going! $remaining more glasses to reach your goal"
        }
    }
    
    private fun generateNutritionTips() {
        binding.progressBarTips.visibility = android.view.View.VISIBLE
        
        val tips = listOf(
            "Eat a variety of colorful fruits and vegetables to ensure you're getting a wide range of nutrients.",
            "Include protein in every meal to help maintain muscle mass and keep you feeling full longer.",
            "Choose whole grains over refined grains for better fiber and nutrient content.",
            "Stay hydrated by drinking water throughout the day, not just when you feel thirsty.",
            "Practice mindful eating by slowing down and paying attention to your hunger and fullness cues.",
            "Limit processed foods and added sugars for better overall health.",
            "Include healthy fats from sources like avocados, nuts, and olive oil in your diet."
        )
        
        android.os.Handler(mainLooper).postDelayed({
            val randomTip = tips.random()
            binding.tvNutritionTips.text = "💡 $randomTip"
            binding.progressBarTips.visibility = android.view.View.GONE
        }, 1500)
    }
    
    private fun loadNutritionData() {
        // Load saved data or use defaults
        updateCalorieDisplay()
        binding.tvWaterIntake.text = "Water Intake: 0 glasses"
        binding.tvWaterStatus.text = "Start tracking your water intake!"
    }
    
    private fun saveNutritionData() {
        // Save nutrition data to local storage or Firebase
        // Implementation would depend on your data storage strategy
    }
    
    private fun showError(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                showError("Camera permission is required to track meals with photos")
            }
        }
    }
}

data class Meal(
    val calories: Int,
    val description: String,
    val timestamp: Date
)