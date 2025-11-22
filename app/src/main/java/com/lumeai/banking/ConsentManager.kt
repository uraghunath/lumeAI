package com.lumeai.banking

import com.lumeai.banking.models.*

/**
 * ConsentManager - Manages customer consent for AI data usage
 * Provides transparency and control over what data banks can use
 */
object ConsentManager {
    
    // In-memory storage for demo (in production, this would be a database)
    private val consentPreferences = mutableMapOf<String, MutableList<ConsentPreference>>()
    private val consentAuditLogs = mutableListOf<ConsentAuditLog>()
    
    /**
     * Create a consent request for customer
     */
    fun createConsentRequest(
        customerId: String,
        bankName: String,
        purpose: String
    ): ConsentRequest {
        val dataItems = when (purpose) {
            "loan_evaluation" -> listOf(
                DataItem(
                    category = "transaction_history",
                    friendlyName = "Transaction History",
                    description = "Your past 12 months of bank transactions",
                    sensitivity = "HIGH"
                ),
                DataItem(
                    category = "salary_info",
                    friendlyName = "Salary Information",
                    description = "Your monthly income credits",
                    sensitivity = "HIGH"
                ),
                DataItem(
                    category = "credit_history",
                    friendlyName = "Credit History",
                    description = "Your loans and credit card history",
                    sensitivity = "HIGH"
                ),
                DataItem(
                    category = "location",
                    friendlyName = "Location",
                    description = "Your city and state for regional assessment",
                    sensitivity = "MEDIUM"
                )
            )
            "fraud_detection" -> listOf(
                DataItem(
                    category = "transaction_patterns",
                    friendlyName = "Transaction Patterns",
                    description = "Your typical spending habits and locations",
                    sensitivity = "MEDIUM"
                ),
                DataItem(
                    category = "device_info",
                    friendlyName = "Device Information",
                    description = "Your phone/computer used for banking",
                    sensitivity = "LOW"
                ),
                DataItem(
                    category = "location",
                    friendlyName = "Real-time Location",
                    description = "Where you are when making transactions",
                    sensitivity = "HIGH"
                )
            )
            "credit_scoring" -> listOf(
                DataItem(
                    category = "payment_history",
                    friendlyName = "Payment History",
                    description = "Your bill payment track record",
                    sensitivity = "HIGH"
                ),
                DataItem(
                    category = "account_balances",
                    friendlyName = "Account Balances",
                    description = "Your average account balance",
                    sensitivity = "MEDIUM"
                ),
                DataItem(
                    category = "digital_behavior",
                    friendlyName = "Digital Activity",
                    description = "Your online banking usage patterns",
                    sensitivity = "LOW"
                )
            )
            else -> emptyList()
        }
        
        val consequences = getConsequences(purpose)
        
        return ConsentRequest(
            requestId = "REQ_${System.currentTimeMillis()}",
            bankName = bankName,
            dataRequested = dataItems,
            purpose = purpose,
            purposeExplanation = getPurposeExplanation(purpose),
            consequences = consequences,
            validUntil = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), // 1 year
            isRequired = purpose == "fraud_detection" // Fraud detection is mandatory
        )
    }
    
    /**
     * Get consequences of granting/denying consent
     */
    private fun getConsequences(purpose: String): ConsentConsequences {
        return when (purpose) {
            "loan_evaluation" -> ConsentConsequences(
                ifYes = "✓ Instant AI evaluation (2 hours) | Lower interest rates with full data",
                ifNo = "⏱ Manual review (5-7 days) | May miss best rate offers"
            )
            "fraud_detection" -> ConsentConsequences(
                ifYes = "✓ Real-time fraud protection | Auto-block suspicious transactions",
                ifNo = "⚠️ Required for account security (cannot be disabled)"
            )
            "credit_scoring" -> ConsentConsequences(
                ifYes = "✓ Personalized credit offers | Automatic limit increases",
                ifNo = "📊 Standard credit limits | Manual review for increases"
            )
            else -> ConsentConsequences(
                ifYes = "Your data will be used for the specified purpose",
                ifNo = "Service may be limited or unavailable"
            )
        }
    }
    
    /**
     * Get explanation of why data is needed
     */
    private fun getPurposeExplanation(purpose: String): String {
        return when (purpose) {
            "loan_evaluation" -> "We use AI to evaluate your loan application. The AI needs your financial data to assess your ability to repay. More data = faster, more accurate decisions."
            "fraud_detection" -> "AI monitors your transactions 24/7 to protect you from fraud. It learns your normal behavior and flags anything suspicious."
            "credit_scoring" -> "AI calculates your credit score based on your financial behavior. This helps you get better rates and higher limits."
            else -> "AI helps provide better, faster banking services."
        }
    }
    
    /**
     * Process customer's consent decision
     */
    fun processConsent(
        customerId: String,
        requestId: String,
        consentGiven: Boolean,
        dataCategories: List<String>,
        purpose: String
    ): ConsentProcessingResult {
        val timestamp = System.currentTimeMillis()
        
        // Store consent preferences
        dataCategories.forEach { category ->
            val preference = ConsentPreference(
                dataCategory = category,
                purpose = purpose,
                consentGiven = consentGiven,
                consequences = getConsequences(purpose),
                timestamp = timestamp,
                expiryDate = timestamp + (365L * 24 * 60 * 60 * 1000) // 1 year
            )
            
            consentPreferences.getOrPut(customerId) { mutableListOf() }.add(preference)
        }
        
        // Log consent action for audit
        val auditLog = ConsentAuditLog(
            consentId = requestId,
            customerId = customerId,
            action = if (consentGiven) "GRANTED" else "DENIED",
            dataCategory = dataCategories.joinToString(", "),
            purpose = purpose,
            timestamp = timestamp,
            ipAddress = "192.168.1.1" // Mock IP
        )
        consentAuditLogs.add(auditLog)
        
        return ConsentProcessingResult(
            success = true,
            message = if (consentGiven) 
                "Consent granted successfully. Your data will be used only for $purpose."
                else "Consent denied. Your data will not be used for $purpose.",
            nextSteps = getNextSteps(purpose, consentGiven)
        )
    }
    
    /**
     * Get customer's consent preferences
     */
    fun getConsents(customerId: String): List<ConsentPreference> {
        return consentPreferences[customerId] ?: emptyList()
    }
    
    /**
     * Revoke consent for a specific purpose
     */
    fun revokeConsent(
        customerId: String,
        purpose: String
    ): Boolean {
        val preferences = consentPreferences[customerId] ?: return false
        
        preferences.removeAll { it.purpose == purpose }
        
        // Log revocation
        consentAuditLogs.add(
            ConsentAuditLog(
                consentId = "REVOKE_${System.currentTimeMillis()}",
                customerId = customerId,
                action = "REVOKED",
                dataCategory = "all",
                purpose = purpose,
                timestamp = System.currentTimeMillis(),
                ipAddress = "192.168.1.1"
            )
        )
        
        return true
    }
    
    /**
     * Get audit trail for customer
     */
    fun getAuditTrail(customerId: String): List<ConsentAuditLog> {
        return consentAuditLogs.filter { it.customerId == customerId }
            .sortedByDescending { it.timestamp }
    }
    
    /**
     * Check if customer has given consent for specific purpose
     */
    fun hasConsent(customerId: String, purpose: String, dataCategory: String): Boolean {
        val preferences = consentPreferences[customerId] ?: return false
        
        return preferences.any { 
            it.purpose == purpose && 
            it.dataCategory == dataCategory && 
            it.consentGiven &&
            (it.expiryDate == null || it.expiryDate > System.currentTimeMillis())
        }
    }
    
    /**
     * Get next steps after consent decision
     */
    private fun getNextSteps(purpose: String, consentGiven: Boolean): List<String> {
        if (consentGiven) {
            return when (purpose) {
                "loan_evaluation" -> listOf(
                    "Complete your loan application",
                    "AI will process in 2 hours",
                    "You'll get instant notification"
                )
                "fraud_detection" -> listOf(
                    "Fraud protection is now active",
                    "You'll get alerts for suspicious activity",
                    "Your transactions are monitored 24/7"
                )
                "credit_scoring" -> listOf(
                    "Your credit score is being calculated",
                    "Check back in 24 hours",
                    "You may get personalized offers"
                )
                else -> listOf("Your consent has been recorded")
            }
        } else {
            return when (purpose) {
                "loan_evaluation" -> listOf(
                    "Your application will go through manual review",
                    "Expect 5-7 business days processing time",
                    "You may need to submit physical documents"
                )
                "fraud_detection" -> listOf(
                    "⚠️ This consent is required for account security",
                    "Your account may have limited functionality",
                    "Please contact support if you have concerns"
                )
                "credit_scoring" -> listOf(
                    "You'll receive standard credit terms",
                    "Limit increases require manual review",
                    "You can change this anytime"
                )
                else -> listOf("Service may be limited")
            }
        }
    }
    
    /**
     * Generate mock consent scenarios for demo
     */
    fun getMockConsentScenarios(): List<ConsentRequest> {
        return listOf(
            createConsentRequest("CUST_12345", "HDFC Bank", "loan_evaluation"),
            createConsentRequest("CUST_12345", "SBI", "fraud_detection"),
            createConsentRequest("CUST_12345", "ICICI Bank", "credit_scoring")
        )
    }
}

/**
 * Result of consent processing
 */
data class ConsentProcessingResult(
    val success: Boolean,
    val message: String,
    val nextSteps: List<String>
)

