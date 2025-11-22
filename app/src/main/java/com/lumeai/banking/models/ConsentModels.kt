package com.lumeai.banking.models

/**
 * Customer's consent preference for AI data usage
 */
data class ConsentPreference(
    val dataCategory: String,     // "transaction_history", "salary_info", "location"
    val purpose: String,           // "loan_evaluation", "fraud_detection", "credit_scoring"
    val consentGiven: Boolean,
    val consequences: ConsentConsequences,
    val timestamp: Long,
    val expiryDate: Long?
)

/**
 * Clear explanation of what happens if consent is given or denied
 */
data class ConsentConsequences(
    val ifYes: String,  // "Loan processed in 2 hours"
    val ifNo: String    // "Manual review takes 5-7 days"
)

/**
 * Consent request from bank to customer
 */
data class ConsentRequest(
    val requestId: String,
    val bankName: String,
    val dataRequested: List<DataItem>,
    val purpose: String,
    val purposeExplanation: String,
    val consequences: ConsentConsequences,
    val validUntil: Long,
    val isRequired: Boolean  // Is this consent mandatory?
)

/**
 * Individual data item being requested
 */
data class DataItem(
    val category: String,
    val friendlyName: String,
    val description: String,
    val sensitivity: String  // "HIGH", "MEDIUM", "LOW"
)

/**
 * Consent audit log entry
 */
data class ConsentAuditLog(
    val consentId: String,
    val customerId: String,
    val action: String,  // "GRANTED", "DENIED", "REVOKED"
    val dataCategory: String,
    val purpose: String,
    val timestamp: Long,
    val ipAddress: String?
)

