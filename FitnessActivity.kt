package com.zenfit.ai.fitness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.zenfit.ai.databinding.ActivityFitnessBinding

class FitnessActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFitnessBinding
    private lateinit var poseDetector: com.google.mlkit.vision.pose.PoseDetector
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitnessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupPoseDetector()
        setupClickListeners()
        
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun setupPoseDetector() {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)
    }
    
    private fun setupClickListeners() {
        binding.btnStartWorkout.setOnClickListener {
            startWorkout()
        }
        
        binding.btnStopWorkout.setOnClickListener {
            stopWorkout()
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
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun startWorkout() {
        binding.apply {
            btnStartWorkout.isEnabled = false
            btnStopWorkout.isEnabled = true
            tvWorkoutStatus.text = "Workout in progress..."
        }
        
        // Start workout tracking
        WorkoutTracker.startWorkout()
    }
    
    private fun stopWorkout() {
        binding.apply {
            btnStartWorkout.isEnabled = true
            btnStopWorkout.isEnabled = false
            tvWorkoutStatus.text = "Workout completed!"
        }
        
        // Stop workout tracking and get results
        val workoutResult = WorkoutTracker.stopWorkout()
        updateWorkoutStats(workoutResult)
    }
    
    private fun updateWorkoutStats(result: WorkoutResult) {
        binding.apply {
            tvDuration.text = "Duration: ${result.duration} minutes"
            tvCalories.text = "Calories: ${result.calories}"
            tvExercises.text = "Exercises: ${result.exerciseCount}"
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        poseDetector.close()
    }
}