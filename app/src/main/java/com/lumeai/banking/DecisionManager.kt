package com.lumeai.banking

import android.content.Context
import android.util.Log
import com.lumeai.banking.models.FirebaseDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * 🎯 DecisionManager - World-Class Decision Data Manager
 * 
 * Features:
 * - Smart caching with 30-second TTL
 * - Multiple loan/bank support
 * - Statistics calculation
 * - Grouping and filtering
 * - Thread-safe operations
 * - Error resilience
 */
object DecisionManager {
    
    private const val TAG = "DecisionManager"
    private const val CACHE_TTL_MS = 30_000L // 30 seconds
    
    // Cache
    private var cachedDecisions: List<FirebaseDecision> = emptyList()
    private var lastFetchTime: Long = 0L
    private val loadingLock = Any()
    
    /**
     * Get all decisions for current user with smart caching
     */
    suspend fun getAllDecisions(context: Context, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return withContext(Dispatchers.IO) {
            // Check cache outside synchronized block
            val now = System.currentTimeMillis()
            val isCacheValid = (now - lastFetchTime) < CACHE_TTL_MS
            
            if (!forceRefresh && isCacheValid && cachedDecisions.isNotEmpty()) {
                Log.d(TAG, "📦 Using cached decisions (${cachedDecisions.size} items)")
                return@withContext cachedDecisions
            }
            
            try {
                val customerId = FirebaseListenerService.getCustomerId(context)
                Log.d(TAG, "🔄 Fetching fresh decisions for customer: $customerId")
                
                val decisions = FirebaseSyncManager.getCustomerDecisions(customerId)
                
                // Update cache in synchronized block
                synchronized(loadingLock) {
                    cachedDecisions = decisions.sortedByDescending { it.timestamp }
                    lastFetchTime = now
                }
                
                Log.d(TAG, "✅ Loaded ${decisions.size} decisions")
                cachedDecisions
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to fetch decisions: ${e.message}", e)
                // Return cached data if available, even if stale
                cachedDecisions
            }
        }
    }
    
    /**
     * Get the latest decision
     */
    suspend fun getLatestDecision(context: Context): FirebaseDecision? {
        return getAllDecisions(context).firstOrNull()
    }
    
    /**
     * Get decisions by bank name
     */
    suspend fun getDecisionsByBank(context: Context, bankName: String, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return getAllDecisions(context, forceRefresh).filter { 
            it.bankName.equals(bankName, ignoreCase = true) 
        }
    }
    
    /**
     * Get decisions by outcome
     */
    suspend fun getDecisionsByOutcome(context: Context, outcome: String, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return getAllDecisions(context, forceRefresh).filter { 
            it.outcome.equals(outcome, ignoreCase = true) 
        }
    }
    
    /**
     * Get decisions by loan type
     */
    suspend fun getDecisionsByLoanType(context: Context, loanType: String, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return getAllDecisions(context, forceRefresh).filter { 
            it.loanType.equals(loanType, ignoreCase = true) 
        }
    }
    
    /**
     * Get all denied decisions (for improvement suggestions)
     */
    suspend fun getDeniedDecisions(context: Context, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return getDecisionsByOutcome(context, "DENIED", forceRefresh)
    }
    
    /**
     * Get all approved decisions
     */
    suspend fun getApprovedDecisions(context: Context, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return getDecisionsByOutcome(context, "APPROVED", forceRefresh)
    }
    
    /**
     * Get all pending decisions
     */
    suspend fun getPendingDecisions(context: Context, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return getDecisionsByOutcome(context, "PENDING", forceRefresh)
    }
    
    /**
     * Get decisions with bias detected
     */
    suspend fun getBiasedDecisions(context: Context, forceRefresh: Boolean = false): List<FirebaseDecision> {
        return getAllDecisions(context, forceRefresh).filter { it.biasDetected }
    }
    
    /**
     * Calculate comprehensive user statistics
     */
    suspend fun getUserStats(context: Context, forceRefresh: Boolean = false): UserStats {
        val decisions = getAllDecisions(context, forceRefresh)
        
        val approvedCount = decisions.count { it.outcome.equals("APPROVED", ignoreCase = true) }
        val deniedCount = decisions.count { it.outcome.equals("DENIED", ignoreCase = true) }
        val pendingCount = decisions.count { it.outcome.equals("PENDING", ignoreCase = true) }
        val biasedDecisions = decisions.filter { it.biasDetected }
        val highRiskBias = biasedDecisions.count { it.biasSeverity.equals("HIGH", ignoreCase = true) }
        val uniqueBanks = decisions.map { it.bankName }.distinct().size
        val uniqueLoanTypes = decisions.map { it.loanType }.distinct().filter { it.isNotEmpty() }.size
        
        return UserStats(
            totalDecisions = decisions.size,
            approvedCount = approvedCount,
            deniedCount = deniedCount,
            pendingCount = pendingCount,
            biasDetectedCount = biasedDecisions.size,
            highRiskBiasCount = highRiskBias,
            banksCount = uniqueBanks,
            loanTypesCount = uniqueLoanTypes
        )
    }
    
    /**
     * Group decisions by bank
     */
    suspend fun getDecisionsByBankGrouped(context: Context, forceRefresh: Boolean = false): List<BankSummary> {
        val decisions = getAllDecisions(context, forceRefresh)
        
        return decisions
            .groupBy { it.bankName }
            .filter { it.key.isNotEmpty() }
            .map { (bankName, bankDecisions) ->
                BankSummary(
                    bankName = bankName,
                    decisions = bankDecisions.sortedByDescending { it.timestamp },
                    approvedCount = bankDecisions.count { it.outcome.equals("APPROVED", ignoreCase = true) },
                    deniedCount = bankDecisions.count { it.outcome.equals("DENIED", ignoreCase = true) },
                    pendingCount = bankDecisions.count { it.outcome.equals("PENDING", ignoreCase = true) },
                    latestDecision = bankDecisions.maxByOrNull { it.timestamp }
                )
            }
            .sortedByDescending { it.latestDecision?.timestamp ?: 0 }
    }
    
    /**
     * Group decisions by loan type
     */
    suspend fun getDecisionsByLoanTypeGrouped(context: Context, forceRefresh: Boolean = false): List<LoanTypeSummary> {
        val decisions = getAllDecisions(context, forceRefresh)
        
        return decisions
            .filter { it.loanType.isNotEmpty() }
            .groupBy { it.loanType }
            .map { (loanType, loanDecisions) ->
                LoanTypeSummary(
                    loanType = loanType,
                    displayName = formatLoanType(loanType),
                    icon = getLoanTypeIcon(loanType),
                    decisions = loanDecisions.sortedByDescending { it.timestamp },
                    totalAmount = loanDecisions.sumOf { it.loanAmount }
                )
            }
            .sortedByDescending { it.decisions.size }
    }
    
    /**
     * Search decisions by any text
     */
    suspend fun searchDecisions(context: Context, query: String, forceRefresh: Boolean = false): List<FirebaseDecision> {
        if (query.isBlank()) return getAllDecisions(context, forceRefresh)
        
        val lowerQuery = query.lowercase()
        return getAllDecisions(context, forceRefresh).filter { decision ->
            decision.bankName.lowercase().contains(lowerQuery) ||
            decision.loanType.lowercase().contains(lowerQuery) ||
            decision.outcome.lowercase().contains(lowerQuery) ||
            decision.id.lowercase().contains(lowerQuery) ||
            decision.decisionType.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * Clear cache (useful for force refresh)
     */
    fun clearCache() {
        synchronized(loadingLock) {
            cachedDecisions = emptyList()
            lastFetchTime = 0L
            Log.d(TAG, "🗑️ Cache cleared")
        }
    }
    
    // Helper functions
    private fun formatLoanType(loanType: String): String {
        return when (loanType.uppercase()) {
            "PERSONAL_LOAN", "PERSONAL" -> "Personal Loan"
            "HOME_LOAN", "HOME" -> "Home Loan"
            "CAR_LOAN", "CAR", "AUTO_LOAN" -> "Car Loan"
            "EDUCATION_LOAN", "EDUCATION" -> "Education Loan"
            "BUSINESS_LOAN", "BUSINESS" -> "Business Loan"
            "CREDIT_CARD" -> "Credit Card"
            else -> loanType.replace("_", " ").split(" ")
                .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
        }
    }
    
    private fun getLoanTypeIcon(loanType: String): String {
        return when (loanType.uppercase()) {
            "PERSONAL_LOAN", "PERSONAL" -> "💰"
            "HOME_LOAN", "HOME" -> "🏠"
            "CAR_LOAN", "CAR", "AUTO_LOAN" -> "🚗"
            "EDUCATION_LOAN", "EDUCATION" -> "🎓"
            "BUSINESS_LOAN", "BUSINESS" -> "💼"
            "CREDIT_CARD" -> "💳"
            else -> "📄"
        }
    }
}

/**
 * User Statistics Data Class
 */
data class UserStats(
    val totalDecisions: Int = 0,
    val approvedCount: Int = 0,
    val deniedCount: Int = 0,
    val pendingCount: Int = 0,
    val biasDetectedCount: Int = 0,
    val highRiskBiasCount: Int = 0,
    val banksCount: Int = 0,
    val loanTypesCount: Int = 0
) {
    val approvalRate: Float
        get() = if (totalDecisions > 0) (approvedCount.toFloat() / totalDecisions) * 100 else 0f
    
    val biasRate: Float
        get() = if (totalDecisions > 0) (biasDetectedCount.toFloat() / totalDecisions) * 100 else 0f
}

/**
 * Bank Summary Data Class
 */
data class BankSummary(
    val bankName: String,
    val decisions: List<FirebaseDecision>,
    val approvedCount: Int,
    val deniedCount: Int,
    val pendingCount: Int,
    val latestDecision: FirebaseDecision?
)

/**
 * Loan Type Summary Data Class
 */
data class LoanTypeSummary(
    val loanType: String,
    val displayName: String,
    val icon: String,
    val decisions: List<FirebaseDecision>,
    val totalAmount: Double
)

