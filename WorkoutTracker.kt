package com.zenfit.ai.fitness

import android.os.SystemClock
import java.util.concurrent.TimeUnit

object WorkoutTracker {
    
    private var startTime: Long = 0
    private var isWorkoutActive = false
    private var exerciseCount = 0
    private var caloriesBurned = 0
    
    fun startWorkout() {
        startTime = SystemClock.elapsedRealtime()
        isWorkoutActive = true
        exerciseCount = 0
        caloriesBurned = 0
    }
    
    fun stopWorkout(): WorkoutResult {
        isWorkoutActive = false
        val duration = getWorkoutDuration()
        
        return WorkoutResult(
            duration = duration,
            calories = caloriesBurned,
            exerciseCount = exerciseCount
        )
    }
    
    fun addExercise(exerciseType: String) {
        if (isWorkoutActive) {
            exerciseCount++
            // Simple calorie calculation (can be improved with actual exercise data)
            caloriesBurned += when (exerciseType) {
                "push_up" -> 5
                "squat" -> 8
                "jumping_jack" -> 10
                "plank" -> 3
                else -> 5
            }
        }
    }
    
    fun getCurrentDuration(): Long {
        return if (isWorkoutActive) {
            TimeUnit.MILLISECONDS.toMinutes(SystemClock.elapsedRealtime() - startTime)
        } else {
            0
        }
    }
    
    private fun getWorkoutDuration(): Long {
        return TimeUnit.MILLISECONDS.toMinutes(SystemClock.elapsedRealtime() - startTime)
    }
    
    fun isActive(): Boolean = isWorkoutActive
}

data class WorkoutResult(
    val duration: Long,
    val calories: Int,
    val exerciseCount: Int
)