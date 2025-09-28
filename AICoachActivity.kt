package com.zenfit.ai.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zenfit.ai.databinding.ActivityAiCoachBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AICoachActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAiCoachBinding
    private lateinit var firestore: FirebaseFirestore
    private val conversationHistory = mutableListOf<ChatMessage>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiCoachBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        firestore = FirebaseFirestore.getInstance()
        setupClickListeners()
        setupAIInsights()
    }
    
    private fun setupClickListeners() {
        binding.btnSendQuestion.setOnClickListener {
            sendQuestion()
        }
        
        binding.btnHealthInsights.setOnClickListener {
            generateHealthInsights()
        }
        
        binding.btnWorkoutPlan.setOnClickListener {
            generateWorkoutPlan()
        }
        
        binding.btnNutritionPlan.setOnClickListener {
            generateNutritionPlan()
        }
    }
    
    private fun sendQuestion() {
        val question = binding.etQuestion.text.toString().trim()
        if (question.isEmpty()) return
        
        // Add user message to conversation
        val userMessage = ChatMessage(question, "user", Date())
        conversationHistory.add(userMessage)
        updateChatDisplay()
        
        // Clear input
        binding.etQuestion.text?.clear()
        
        // Show loading
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Simulate AI response (in a real app, this would call an AI API)
        android.os.Handler(mainLooper).postDelayed({
            val response = generateAIResponse(question)
            val aiMessage = ChatMessage(response, "ai", Date())
            conversationHistory.add(aiMessage)
            updateChatDisplay()
            binding.progressBar.visibility = android.view.View.GONE
        }, 1500)
    }
    
    private fun generateAIResponse(question: String): String {
        return when {
            question.contains("workout", ignoreCase = true) -> 
                "Based on your fitness level and goals, I recommend a balanced workout routine combining strength training and cardio. Would you like me to create a specific plan for you?"
            
            question.contains("nutrition", ignoreCase = true) || question.contains("diet", ignoreCase = true) -> 
                "For optimal health, focus on whole foods, lean proteins, and plenty of vegetables. I can create a personalized meal plan based on your dietary preferences and goals."
            
            question.contains("meditation", ignoreCase = true) || question.contains("stress", ignoreCase = true) -> 
                "Meditation is excellent for stress management. I can guide you through personalized sessions and help you establish a consistent practice."
            
            question.contains("sleep", ignoreCase = true) -> 
                "Quality sleep is crucial for recovery and overall health. Try to maintain a consistent sleep schedule and create a relaxing bedtime routine."
            
            else -> 
                "I'm here to help you achieve your health and fitness goals. Feel free to ask me about workouts, nutrition, meditation, or any wellness topics!"
        }
    }
    
    private fun updateChatDisplay() {
        val chatText = conversationHistory.joinToString("\n\n") { message ->
            val sender = if (message.sender == "user") "You" else "AI Coach"
            "$sender: ${message.content}"
        }
        binding.tvChatHistory.text = chatText
        
        // Scroll to bottom
        binding.scrollView.post {
            binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
    
    private fun setupAIInsights() {
        // Load initial insights
        binding.apply {
            tvDailyInsight.text = "Today's Insight: Consistency is key to achieving your fitness goals. Even 15 minutes of exercise daily can make a significant difference."
            tvWeeklyProgress.text = "Weekly Progress: You've completed 4 workouts this week and meditated for 45 minutes total. Great job!"
            tvHealthTip.text = "Health Tip: Remember to stay hydrated throughout the day. Aim for at least 8 glasses of water daily."
        }
    }
    
    private fun generateHealthInsights() {
        binding.progressBarInsights.visibility = android.view.View.VISIBLE
        
        // Simulate AI insight generation
        android.os.Handler(mainLooper).postDelayed({
            val insights = listOf(
                "Based on your recent activity patterns, you're most productive with morning workouts. Consider scheduling your main exercise sessions before 10 AM.",
                "Your heart rate variability suggests you might benefit from more recovery time. Consider adding an extra rest day this week.",
                "Your nutrition tracking shows you're consistently low on protein. Try adding more lean meats, fish, or plant-based proteins to your meals.",
                "Your meditation consistency has improved by 40% this month. Keep up the great work!",
                "Analysis of your sleep patterns suggests you might benefit from going to bed 30 minutes earlier for optimal recovery."
            )
            
            val randomInsight = insights.random()
            binding.tvDailyInsight.text = "AI Insight: $randomInsight"
            binding.progressBarInsights.visibility = android.view.View.GONE
        }, 2000)
    }
    
    private fun generateWorkoutPlan() {
        binding.progressBarPlan.visibility = android.view.View.VISIBLE
        
        android.os.Handler(mainLooper).postDelayed({
            val workoutPlan = """
                Personalized Workout Plan:
                
                Monday: Upper Body Strength (45 min)
                - Push-ups: 3 sets x 15 reps
                - Dumbbell rows: 3 sets x 12 reps
                - Shoulder press: 3 sets x 10 reps
                
                Wednesday: Cardio & Core (30 min)
                - High-intensity interval training
                - Plank variations: 3 sets
                - Mountain climbers: 3 sets x 20 reps
                
                Friday: Lower Body Strength (45 min)
                - Squats: 3 sets x 15 reps
                - Lunges: 3 sets x 12 reps each leg
                - Calf raises: 3 sets x 20 reps
                
                Daily: 10-minute meditation and stretching
            """.trimIndent()
            
            binding.tvWorkoutPlan.text = workoutPlan
            binding.progressBarPlan.visibility = android.view.View.GONE
        }, 3000)
    }
    
    private fun generateNutritionPlan() {
        binding.progressBarNutrition.visibility = android.view.View.VISIBLE
        
        android.os.Handler(mainLooper).postDelayed({
            val nutritionPlan = """
                Personalized Nutrition Plan:
                
                Daily Calorie Target: 2,200 calories
                Macronutrient Ratio: 40% Carbs, 30% Protein, 30% Fat
                
                Breakfast (500 cal):
                - Oatmeal with berries and nuts
                - Green tea or black coffee
                
                Lunch (700 cal):
                - Grilled chicken salad
                - Whole grain bread
                - Fresh vegetables
                
                Dinner (800 cal):
                - Baked salmon or tofu
                - Quinoa or brown rice
                - Steamed vegetables
                
                Snacks (200 cal):
                - Greek yogurt with honey
                - Mixed nuts or fruit
                
                Hydration: 8-10 glasses of water daily
            """.trimIndent()
            
            binding.tvNutritionPlan.text = nutritionPlan
            binding.progressBarNutrition.visibility = android.view.View.GONE
        }, 3000)
    }
}

data class ChatMessage(
    val content: String,
    val sender: String,
    val timestamp: Date
)