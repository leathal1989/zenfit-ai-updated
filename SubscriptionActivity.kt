package com.zenfit.ai.billing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.zenfit.ai.databinding.ActivitySubscriptionBinding
import kotlinx.coroutines.launch

class SubscriptionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var billingManager: BillingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        billingManager = BillingManager(this)
        setupSubscriptionOptions()
        observePurchaseState()
    }
    
    private fun setupSubscriptionOptions() {
        binding.apply {
            // Premium Monthly
            cardPremiumMonthly.setOnClickListener {
                launchPurchase(PREMIUM_MONTHLY)
            }
            
            // Premium Yearly (with discount)
            cardPremiumYearly.setOnClickListener {
                launchPurchase(PREMIUM_YEARLY)
            }
            
            // Premium+ Monthly
            cardPremiumPlusMonthly.setOnClickListener {
                launchPurchase(PREMIUM_PLUS_MONTHLY)
            }
            
            // Premium+ Yearly (with discount)
            cardPremiumPlusYearly.setOnClickListener {
                launchPurchase(PREMIUM_PLUS_YEARLY)
            }
        }
    }
    
    private fun launchPurchase(productId: String) {
        lifecycleScope.launch {
            val currentState = billingManager.purchaseState.value
            if (currentState is PurchaseState.ProductsAvailable) {
                val productDetails = currentState.products.find { it.productId == productId }
                productDetails?.let {
                    billingManager.launchPurchaseFlow(this@SubscriptionActivity, it)
                }
            }
        }
    }
    
    private fun observePurchaseState() {
        lifecycleScope.launch {
            billingManager.purchaseState.collect { state ->
                when (state) {
                    is PurchaseState.PremiumActive -> {
                        showPremiumActive()
                    }
                    is PurchaseState.ProductsAvailable -> {
                        updateProductPricing(state.products)
                    }
                    is PurchaseState.Error -> {
                        showError(state.message)
                    }
                    else -> {
                        // Handle other states
                    }
                }
            }
        }
    }
    
    private fun updateProductPricing(products: List<ProductDetails>) {
        products.forEach { product ->
            val price = product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
            when (product.productId) {
                PREMIUM_MONTHLY -> binding.tvPremiumMonthlyPrice.text = price
                PREMIUM_YEARLY -> binding.tvPremiumYearlyPrice.text = price
                PREMIUM_PLUS_MONTHLY -> binding.tvPremiumPlusMonthlyPrice.text = price
                PREMIUM_PLUS_YEARLY -> binding.tvPremiumPlusYearlyPrice.text = price
            }
        }
    }
    
    private fun showPremiumActive() {
        binding.apply {
            tvSubscriptionStatus.text = "Premium Active"
            tvSubscriptionStatus.setTextColor(android.graphics.Color.GREEN)
            // Hide purchase buttons or show different UI
        }
    }
    
    private fun showError(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        billingManager.disconnect()
    }
}