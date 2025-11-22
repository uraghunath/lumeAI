package com.lumeai.banking

import com.lumeai.banking.models.*

/**
 * Mock Bank API - Simulates decisions from banks like HDFC, SBI, etc.
 * In production, real banks would call LumeAI API with their decisions
 */
object MockBankAPI {
    
    /**
     * Scenario 1: Loan Denial for Rural, Elderly Customer
     * Shows potential bias against underserved demographics
     */
    fun simulateLoanDenial(): BankDecision {
        return BankDecision(
            customerId = "CUST_12345",
            decisionType = "LOAN_DENIAL",
            timestamp = System.currentTimeMillis(),
            factors = listOf(
                RawDecisionFactor(
                    technicalName = "credit_score",
                    value = "620",
                    weight = 0.35,
                    threshold = "650",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "debt_to_income_ratio",
                    value = "0.55",
                    weight = 0.25,
                    threshold = "0.40",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "employment_tenure",
                    value = "3_months",
                    weight = 0.20,
                    threshold = "12_months",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "existing_relationship",
                    value = "2_years",
                    weight = 0.10,
                    threshold = "1_year",
                    passed = true
                ),
                RawDecisionFactor(
                    technicalName = "digital_footprint",
                    value = "low",
                    weight = 0.10,
                    threshold = "medium",
                    passed = false
                )
            ),
            customerProfile = CustomerProfile(
                age = 68,
                locationType = "rural",
                language = "hi",
                digitalLiteracy = "low"
            )
        )
    }
    
    /**
     * Scenario 2: Transaction Blocked (Fraud Detection)
     * High-value international transaction flagged
     */
    fun simulateTransactionBlock(): BankDecision {
        return BankDecision(
            customerId = "CUST_67890",
            decisionType = "TRANSACTION_BLOCKED",
            timestamp = System.currentTimeMillis(),
            factors = listOf(
                RawDecisionFactor(
                    technicalName = "transaction_amount",
                    value = "₹2,50,000",
                    weight = 0.30,
                    threshold = "₹1,00,000",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "international_transaction",
                    value = "true",
                    weight = 0.25,
                    threshold = "false",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "unusual_merchant",
                    value = "first_time",
                    weight = 0.20,
                    threshold = "known",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "time_of_transaction",
                    value = "3:00 AM",
                    weight = 0.15,
                    threshold = "business_hours",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "device_match",
                    value = "new_device",
                    weight = 0.10,
                    threshold = "known_device",
                    passed = false
                )
            ),
            customerProfile = CustomerProfile(
                age = 42,
                locationType = "urban",
                language = "en",
                digitalLiteracy = "high"
            )
        )
    }
    
    /**
     * Scenario 3: Credit Limit Reduction
     * Based on spending patterns and risk assessment
     */
    fun simulateCreditLimitReduction(): BankDecision {
        return BankDecision(
            customerId = "CUST_45678",
            decisionType = "CREDIT_LIMIT_REDUCED",
            timestamp = System.currentTimeMillis(),
            factors = listOf(
                RawDecisionFactor(
                    technicalName = "utilization_rate",
                    value = "95%",
                    weight = 0.35,
                    threshold = "70%",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "payment_history",
                    value = "2_late_payments",
                    weight = 0.30,
                    threshold = "0_late_payments",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "credit_inquiries",
                    value = "8",
                    weight = 0.20,
                    threshold = "3",
                    passed = false
                ),
                RawDecisionFactor(
                    technicalName = "income_verification",
                    value = "not_updated",
                    weight = 0.15,
                    threshold = "current",
                    passed = false
                )
            ),
            customerProfile = CustomerProfile(
                age = 35,
                locationType = "urban",
                language = "en",
                digitalLiteracy = "medium"
            )
        )
    }
    
    /**
     * Scenario 4: Loan Approval with Good Profile
     * Positive scenario to show contrast
     */
    fun simulateLoanApproval(): BankDecision {
        return BankDecision(
            customerId = "CUST_99999",
            decisionType = "LOAN_APPROVED",
            timestamp = System.currentTimeMillis(),
            factors = listOf(
                RawDecisionFactor(
                    technicalName = "credit_score",
                    value = "780",
                    weight = 0.35,
                    threshold = "650",
                    passed = true
                ),
                RawDecisionFactor(
                    technicalName = "debt_to_income_ratio",
                    value = "0.25",
                    weight = 0.25,
                    threshold = "0.40",
                    passed = true
                ),
                RawDecisionFactor(
                    technicalName = "employment_tenure",
                    value = "5_years",
                    weight = 0.20,
                    threshold = "12_months",
                    passed = true
                ),
                RawDecisionFactor(
                    technicalName = "existing_relationship",
                    value = "10_years",
                    weight = 0.10,
                    threshold = "1_year",
                    passed = true
                ),
                RawDecisionFactor(
                    technicalName = "digital_footprint",
                    value = "high",
                    weight = 0.10,
                    threshold = "medium",
                    passed = true
                )
            ),
            customerProfile = CustomerProfile(
                age = 32,
                locationType = "urban",
                language = "en",
                digitalLiteracy = "high"
            )
        )
    }
    
    /**
     * Get all demo scenarios
     */
    fun getAllScenarios(): List<Pair<String, BankDecision>> {
        return listOf(
            "Loan Denial (Rural, Elderly)" to simulateLoanDenial(),
            "Transaction Blocked (Fraud)" to simulateTransactionBlock(),
            "Credit Limit Reduced" to simulateCreditLimitReduction(),
            "Loan Approved (Good Profile)" to simulateLoanApproval()
        )
    }
}

