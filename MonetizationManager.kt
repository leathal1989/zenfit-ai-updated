package com.zenfit.ai.monetization

import android.content.Context
import android.content.SharedPreferences

class MonetizationManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("zenfit_monetization", Context.MODE_PRIVATE)
    
    companion object {
        const val PREMIUM_MONTHLY_PRICE = "$9.99"
        const val PREMIUM_YEARLY_PRICE = "$59.99"
        const val PREMIUM_PLUS_MONTHLY_PRICE = "$19.99"
        const val PREMIUM_PLUS_YEARLY_PRICE = "$119.99"
        
        const val KEY_SUBSCRIPTION_STATUS = "subscription_status"
        const val KEY_SUBSCRIPTION_TYPE = "subscription_type"
        const val KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry"
    }
    
    enum class SubscriptionType {
        FREE, PREMIUM_MONTHLY, PREMIUM_YEARLY, PREMIUM_PLUS_MONTHLY, PREMIUM_PLUS_YEARLY
    }
    
    fun isPremiumUser(): Boolean {
        val status = prefs.getString(KEY_SUBSCRIPTION_STATUS, "free")
        return status != "free"
    }
    
    fun isPremiumPlusUser(): Boolean {
        val type = prefs.getString(KEY_SUBSCRIPTION_TYPE, "")
        return type.contains("premium_plus")
    }
    
    fun getSubscriptionType(): SubscriptionType {
        val type = prefs.getString(KEY_SUBSCRIPTION_TYPE, "free")
        return when (type) {
            "premium_monthly" -> SubscriptionType.PREMIUM_MONTHLY
            "premium_yearly" -> SubscriptionType.PREMIUM_YEARLY
            "premium_plus_monthly" -> SubscriptionType.PREMIUM_PLUS_MONTHLY
            "premium_plus_yearly" -> SubscriptionType.PREMIUM_PLUS_YEARLY
            else -> SubscriptionType.FREE
        }
    }
    
    fun simulatePurchase(subscriptionType: SubscriptionType) {
        prefs.edit().apply {
            putString(KEY_SUBSCRIPTION_STATUS, "premium")
            putString(KEY_SUBSCRIPTION_TYPE, subscriptionType.name.lowercase())
            putLong(KEY_SUBSCRIPTION_EXPIRY, System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // 30 days
            apply()
        }
    }
    
    fun getFeatureAccessLevel(): FeatureAccessLevel {
        return when (getSubscriptionType()) {
            SubscriptionType.FREE -> FeatureAccessLevel.BASIC
            SubscriptionType.PREMIUM_MONTHLY, SubscriptionType.PREMIUM_YEARLY -> FeatureAccessLevel.PREMIUM
            SubscriptionType.PREMIUM_PLUS_MONTHLY, SubscriptionType.PREMIUM_PLUS_YEARLY -> FeatureAccessLevel.PREMIUM_PLUS
        }
    }
    
    fun getDailyWorkoutLimit(): Int {
        return when (getFeatureAccessLevel()) {
            FeatureAccessLevel.BASIC -> 2
            FeatureAccessLevel.PREMIUM -> 10
            FeatureAccessLevel.PREMIUM_PLUS -> Int.MAX_VALUE
        }
    }
    
    fun getDailyMeditationLimit(): Int {
        return when (getFeatureAccessLevel()) {
            FeatureAccessLevel.BASIC -> 1
            FeatureAccessLevel.PREMIUM -> 5
            FeatureAccessLevel.PREMIUM_PLUS -> Int.MAX_VALUE
        }
    }
    
    fun hasAICoachAccess(): Boolean {
        return getFeatureAccessLevel() != FeatureAccessLevel.BASIC
    }
    
    fun hasAdvancedAIInsights(): Boolean {
        return getFeatureAccessLevel() == FeatureAccessLevel.PREMIUM_PLUS
    }
    
    fun resetDailyLimits() {
        // Called daily to reset usage counters
        prefs.edit().apply {
            putInt("workouts_today", 0)
            putInt("meditations_today", 0)
            apply()
        }
    }
    
    fun incrementWorkoutCount() {
        val current = prefs.getInt("workouts_today", 0)
        prefs.edit().putInt("workouts_today", current + 1).apply()
    }
    
    fun incrementMeditationCount() {
        val current = prefs.getInt("meditations_today", 0)
        prefs.edit().putInt("meditations_today", current + 1).apply()
    }
    
    fun canAccessWorkout(): Boolean {
        val current = prefs.getInt("workouts_today", 0)
        return current < getDailyWorkoutLimit()
    }
    
    fun canAccessMeditation(): Boolean {
        val current = prefs.getInt("meditations_today", 0)
        return current < getDailyMeditationLimit()
    }
}

enum class FeatureAccessLevel {
    BASIC,      // Free tier
    PREMIUM,    // $9.99/month
    PREMIUM_PLUS // $19.99/month
}