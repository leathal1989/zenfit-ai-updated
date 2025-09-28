package com.zenfit.ai.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingManager(private val context: Context) : PurchasesUpdatedListener {
    
    private lateinit var billingClient: BillingClient
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState
    
    companion object {
        const val PREMIUM_MONTHLY = "zenfit_premium_monthly"
        const val PREMIUM_YEARLY = "zenfit_premium_yearly"
        const val PREMIUM_PLUS_MONTHLY = "zenfit_premium_plus_monthly"
        const val PREMIUM_PLUS_YEARLY = "zenfit_premium_plus_yearly"
        
        const val WORKOUT_PLAN_1 = "workout_plan_beginner"
        const val WORKOUT_PLAN_2 = "workout_plan_intermediate"
        const val MEDITATION_PACK_1 = "meditation_pack_stress"
        const val MEDITATION_PACK_2 = "meditation_pack_sleep"
    }
    
    init {
        setupBillingClient()
    }
    
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAvailableProducts()
                    queryPurchases()
                } else {
                    _purchaseState.value = PurchaseState.Error("Billing setup failed")
                }
            }
            
            override fun onBillingServiceDisconnected() {
                _purchaseState.value = PurchaseState.Error("Billing service disconnected")
            }
        })
    }
    
    private fun queryAvailableProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PLUS_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PLUS_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchaseState.value = PurchaseState.ProductsAvailable(productDetailsList)
            }
        }
    }
    
    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            }
        }
    }
    
    private fun processPurchases(purchases: List<Purchase>) {
        val premiumPurchases = purchases.filter { purchase ->
            purchase.products.any { productId ->
                productId in listOf(PREMIUM_MONTHLY, PREMIUM_YEARLY, PREMIUM_PLUS_MONTHLY, PREMIUM_PLUS_YEARLY)
            } && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        
        if (premiumPurchases.isNotEmpty()) {
            _purchaseState.value = PurchaseState.PremiumActive
        } else {
            _purchaseState.value = PurchaseState.FreeUser
        }
    }
    
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseState.value = PurchaseState.Error("Purchase cancelled")
        } else {
            _purchaseState.value = PurchaseState.Error("Purchase failed")
        }
    }
    
    fun isPremiumUser(): Boolean {
        return when (purchaseState.value) {
            is PurchaseState.PremiumActive -> true
            else -> false
        }
    }
    
    fun disconnect() {
        billingClient.endConnection()
    }
}

sealed class PurchaseState {
    object Loading : PurchaseState()
    object FreeUser : PurchaseState()
    object PremiumActive : PurchaseState()
    data class ProductsAvailable(val products: List<ProductDetails>) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}