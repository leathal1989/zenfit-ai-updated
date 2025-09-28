package com.zenfit.ai

import android.app.Application
import com.google.firebase.FirebaseApp

class ZenFitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}