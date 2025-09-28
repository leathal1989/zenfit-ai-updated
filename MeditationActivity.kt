package com.zenfit.ai.meditation

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.zenfit.ai.databinding.ActivityMeditationBinding

class MeditationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMeditationBinding
    private var mediaPlayer: MediaPlayer? = null
    private var meditationTimer: CountDownTimer? = null
    private var isMeditationActive = false
    private var selectedDuration = 5L // minutes
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeditationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupDurationSelector()
        setupClickListeners()
    }
    
    private fun setupDurationSelector() {
        binding.durationGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDuration = when (checkedId) {
                R.id.radio5min -> 5L
                R.id.radio10min -> 10L
                R.id.radio15min -> 15L
                R.id.radio30min -> 30L
                else -> 5L
            }
            updateDurationDisplay()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnStartMeditation.setOnClickListener {
            if (!isMeditationActive) {
                startMeditation()
            }
        }
        
        binding.btnStopMeditation.setOnClickListener {
            stopMeditation()
        }
        
        binding.btnGenerateScript.setOnClickListener {
            generatePersonalizedScript()
        }
    }
    
    private fun updateDurationDisplay() {
        binding.tvSelectedDuration.text = "$selectedDuration minutes"
    }
    
    private fun startMeditation() {
        isMeditationActive = true
        binding.apply {
            btnStartMeditation.isEnabled = false
            btnStopMeditation.isEnabled = true
            durationGroup.isEnabled = false
            tvMeditationStatus.text = "Meditation in progress..."
        }
        
        // Generate AI meditation script
        val script = generateMeditationScript()
        binding.tvMeditationScript.text = script
        
        // Start timer
        startMeditationTimer(selectedDuration * 60 * 1000) // Convert to milliseconds
        
        // Play ambient sound (if available)
        playAmbientSound()
    }
    
    private fun stopMeditation() {
        isMeditationActive = false
        meditationTimer?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        
        binding.apply {
            btnStartMeditation.isEnabled = true
            btnStopMeditation.isEnabled = false
            durationGroup.isEnabled = true
            tvMeditationStatus.text = "Meditation completed!"
            progressBar.progress = 0
        }
    }
    
    private fun startMeditationTimer(durationMillis: Long) {
        meditationTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((durationMillis - millisUntilFinished) * 100 / durationMillis).toInt()
                binding.progressBar.progress = progress
                
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }
            
            override fun onFinish() {
                stopMeditation()
                binding.tvMeditationStatus.text = "Meditation session completed!"
            }
        }.start()
    }
    
    private fun playAmbientSound() {
        // In a real app, you would load actual ambient sounds
        // For now, we'll just simulate this
        binding.tvAmbientSound.text = "Playing: Forest Sounds"
    }
    
    private fun generateMeditationScript(): String {
        return """
            Welcome to your personalized meditation session.
            
            Find a comfortable position and close your eyes.
            
            Take a deep breath in... and slowly exhale.
            
            Focus on your breath. Notice the sensation of breathing.
            
            If your mind wanders, gently bring it back to your breath.
            
            Continue breathing naturally and mindfully.
            
            You are calm, relaxed, and present in this moment.
        """.trimIndent()
    }
    
    private fun generatePersonalizedScript() {
        binding.apply {
            progressBarGenerate.visibility = android.view.View.VISIBLE
            btnGenerateScript.isEnabled = false
        }
        
        // Simulate AI script generation
        android.os.Handler(mainLooper).postDelayed({
            val personalizedScript = """
                Your personalized meditation script based on your current mood and preferences:
                
                Today, focus on gratitude and self-compassion.
                
                Begin by acknowledging three things you're grateful for.
                
                Breathe in positivity, breathe out tension.
                
                You are worthy of love and happiness.
                
                Continue with this positive mindset throughout your day.
            """.trimIndent()
            
            binding.apply {
                tvMeditationScript.text = personalizedScript
                progressBarGenerate.visibility = android.view.View.GONE
                btnGenerateScript.isEnabled = true
            }
        }, 2000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        meditationTimer?.cancel()
        mediaPlayer?.release()
    }
}