package com.lumeai.banking

import com.lumeai.banking.models.*

/**
 * BiasDetector - Analyzes AI decisions for potential bias
 * Checks for patterns that may disadvantage protected groups
 */
object BiasDetector {
    
    /**
     * Analyze a bank decision for potential bias
     * CRITICAL: Only flag bias if the decision was DENIED due to potentially unfair factors
     */
    fun analyze(decision: BankDecision): BiasWarning? {
        // ✅ APPROVED DECISIONS: No bias check needed - customer qualified fairly
        val isDenied = decision.decisionType.contains("DENIED", ignoreCase = true) ||
                       decision.decisionType.contains("BLOCKED", ignoreCase = true) ||
                       decision.decisionType.contains("REDUCED", ignoreCase = true)
        
        if (!isDenied) {
            // Decision was approved - no bias to report
            return null
        }
        
        // ❌ DENIED DECISIONS: Check if denial was due to biased factors
        val biasFactors = mutableListOf<String>()
        val affectedGroups = mutableListOf<String>()
        var maxSeverity = "LOW"
        
        // Check for age-based bias (only if decision was denied)
        if (decision.customerProfile.age > 60 || decision.customerProfile.age < 25) {
            val ageBias = checkAgeBias(decision)
            if (ageBias != null) {
                biasFactors.add(ageBias)
                if (decision.customerProfile.age > 60) {
                    affectedGroups.add("Elderly customers (60+)")
                } else {
                    affectedGroups.add("Young customers (<25)")
                }
                maxSeverity = "MEDIUM"
            }
        }
        
        // Check for location-based bias
        if (decision.customerProfile.locationType == "rural") {
            val locationBias = checkLocationBias(decision)
            if (locationBias != null) {
                biasFactors.add(locationBias)
                affectedGroups.add("Rural customers")
                maxSeverity = "HIGH"
            }
        }
        
        // Check for digital literacy bias
        if (decision.customerProfile.digitalLiteracy == "low") {
            val digitalBias = checkDigitalLiteracyBias(decision)
            if (digitalBias != null) {
                biasFactors.add(digitalBias)
                affectedGroups.add("Customers with low digital literacy")
                if (maxSeverity == "LOW") maxSeverity = "MEDIUM"
            }
        }
        
        // If no bias detected, return null
        if (biasFactors.isEmpty()) {
            return null
        }
        
        // Generate bias warning
        val message = when (maxSeverity) {
            "HIGH" -> "⚠️ HIGH RISK: This decision may significantly disadvantage certain customer groups."
            "MEDIUM" -> "⚠️ CAUTION: This decision may disadvantage certain customer groups."
            else -> "ℹ️ NOTICE: This decision involves factors that may affect certain groups differently."
        }
        
        return BiasWarning(
            severity = maxSeverity,
            message = message,
            affectedGroups = affectedGroups,
            mitigationSteps = generateMitigationSteps(decision, biasFactors)
        )
    }
    
    /**
     * Check for age-based bias patterns
     */
    private fun checkAgeBias(decision: BankDecision): String? {
        val profile = decision.customerProfile
        
        // Check if digital footprint or employment tenure factors disadvantage elderly
        if (profile.age > 60) {
            val digitalFactor = decision.factors.find { it.technicalName == "digital_footprint" }
            if (digitalFactor != null && !digitalFactor.passed) {
                return "Digital footprint requirements may disadvantage elderly customers who have less online presence despite being financially stable."
            }
            
            val employmentFactor = decision.factors.find { it.technicalName == "employment_tenure" }
            if (employmentFactor != null && !employmentFactor.passed) {
                return "Employment tenure requirements may disadvantage elderly customers nearing retirement or already retired."
            }
        }
        
        // Check if factors disadvantage young customers
        if (profile.age < 25) {
            val creditHistoryFactor = decision.factors.find { it.technicalName == "credit_score" }
            if (creditHistoryFactor != null && !creditHistoryFactor.passed) {
                return "Credit history requirements may disadvantage young customers who haven't had time to build credit."
            }
        }
        
        return null
    }
    
    /**
     * Check for location-based bias patterns
     */
    private fun checkLocationBias(decision: BankDecision): String? {
        // Rural customers often have:
        // - Limited credit history (fewer formal banking relationships)
        // - Lower digital footprint
        // - Different employment patterns (agriculture, seasonal work)
        
        val problematicFactors = decision.factors.filter { !it.passed }.count { factor ->
            factor.technicalName in listOf(
                "digital_footprint",
                "credit_score",
                "employment_tenure",
                "formal_income_proof"
            )
        }
        
        if (problematicFactors >= 2) {
            return "Multiple factors (credit history, digital footprint, employment patterns) are assessed in ways that systematically disadvantage rural customers, despite their ability to repay."
        }
        
        return null
    }
    
    /**
     * Check for digital literacy bias
     */
    private fun checkDigitalLiteracyBias(decision: BankDecision): String? {
        val digitalFactor = decision.factors.find { it.technicalName == "digital_footprint" }
        if (digitalFactor != null && !digitalFactor.passed) {
            return "Digital footprint requirements assume all creditworthy customers have strong online presence, which disadvantages those with low digital literacy."
        }
        return null
    }
    
    /**
     * Generate mitigation steps based on detected bias
     */
    private fun generateMitigationSteps(decision: BankDecision, biasFactors: List<String>): List<String> {
        val steps = mutableListOf<String>()
        
        // Always offer manual review
        steps.add("Request manual review with alternative documentation (income proof, references, asset verification)")
        
        if (decision.customerProfile.locationType == "rural") {
            steps.add("Provide rural-specific documentation: land ownership records, agricultural income proof, or community references")
        }
        
        if (decision.customerProfile.age > 60) {
            steps.add("Submit proof of retirement income, pension documents, or savings account statements")
        }
        
        if (decision.customerProfile.digitalLiteracy == "low") {
            steps.add("Visit branch for in-person assessment (no digital requirements)")
        }
        
        steps.add("File a formal complaint if you believe the decision is unfair")
        
        return steps
    }
    
    /**
     * Generate detailed bias report for audit purposes
     */
    fun generateAuditReport(decision: BankDecision): BiasAuditReport {
        val biasWarning = analyze(decision)
        
        return BiasAuditReport(
            decisionId = "${decision.customerId}_${decision.timestamp}",
            biasDetected = biasWarning != null,
            biasWarning = biasWarning,
            customerDemographics = CustomerDemographics(
                ageGroup = categorizeAge(decision.customerProfile.age),
                location = decision.customerProfile.locationType,
                digitalLiteracy = decision.customerProfile.digitalLiteracy
            ),
            factorAnalysis = analyzeFactors(decision),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun categorizeAge(age: Int): String {
        return when {
            age < 25 -> "18-24"
            age < 35 -> "25-34"
            age < 45 -> "35-44"
            age < 55 -> "45-54"
            age < 65 -> "55-64"
            else -> "65+"
        }
    }
    
    private fun analyzeFactors(decision: BankDecision): List<FactorBiasAnalysis> {
        return decision.factors.map { factor ->
            FactorBiasAnalysis(
                factorName = factor.technicalName,
                passed = factor.passed,
                weight = factor.weight,
                potentialBias = assessFactorBias(factor, decision.customerProfile)
            )
        }
    }
    
    private fun assessFactorBias(factor: RawDecisionFactor, profile: CustomerProfile): String? {
        return when (factor.technicalName) {
            "digital_footprint" -> if (profile.digitalLiteracy == "low" || profile.locationType == "rural") {
                "May disadvantage customers with limited digital access"
            } else null
            
            "credit_score" -> if (profile.locationType == "rural" || profile.age < 25) {
                "May disadvantage customers with limited formal banking history"
            } else null
            
            "employment_tenure" -> if (profile.age > 60 || profile.locationType == "rural") {
                "May disadvantage elderly or rural customers with non-traditional employment"
            } else null
            
            else -> null
        }
    }
}

/**
 * Detailed bias audit report
 */
data class BiasAuditReport(
    val decisionId: String,
    val biasDetected: Boolean,
    val biasWarning: BiasWarning?,
    val customerDemographics: CustomerDemographics,
    val factorAnalysis: List<FactorBiasAnalysis>,
    val timestamp: Long
)

data class CustomerDemographics(
    val ageGroup: String,
    val location: String,
    val digitalLiteracy: String
)

data class FactorBiasAnalysis(
    val factorName: String,
    val passed: Boolean,
    val weight: Double,
    val potentialBias: String?
)

