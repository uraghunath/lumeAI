package com.lumeai.banking

import kotlin.math.roundToInt

/**
 * GenerativeAIService - Lightweight, local "generative" helper that crafts
 * human-friendly insights, nudges, and document feedback without external calls.
 *
 * In production, replace the compose* methods with a call to your LLM endpoint.
 */
object GenerativeAIService {
    
    fun composeDashboardSummary(
        creditScore: Int,
        debtToIncomeRatio: Double,
        eligibilityScore: Int
    ): String {
        val creditTone = when {
            creditScore >= 760 -> "excellent"
            creditScore >= 700 -> "strong"
            creditScore >= 650 -> "fair"
            else -> "needs improvement"
        }
        val dtiPct = (debtToIncomeRatio * 100).roundToInt()
        val dtiTone = when {
            debtToIncomeRatio <= 0.25 -> "healthy"
            debtToIncomeRatio <= 0.4 -> "acceptable"
            else -> "high"
        }
        val eligibilityTone = when {
            eligibilityScore >= 80 -> "high"
            eligibilityScore >= 60 -> "moderate"
            else -> "low"
        }
        
        return "Your credit looks $creditTone and your DTI is $dtiPct%, which is $dtiTone. " +
               "Overall eligibility appears $eligibilityTone. Tap below to see quick wins tailored for you."
    }
    
    fun composeImprovementNudges(
        creditScore: Int,
        debtToIncomeRatio: Double
    ): List<String> {
        val nudges = mutableListOf<String>()
        if (creditScore < 700) {
            nudges += "Set up auto-pay for bills to build 6 months of on‑time payments."
            nudges += "Lower credit card utilization below 50% of the limit this month."
        }
        if (debtToIncomeRatio > 0.4) {
            nudges += "Prepay a small loan or EMI to bring DTI closer to 35%."
            nudges += "Consider consolidating high‑interest debt to reduce monthly payments."
        }
        if (nudges.isEmpty()) {
            nudges += "Maintain current habits—you're on track. Review your credit report quarterly."
        }
        return nudges.take(4)
    }
    
    data class DocumentAnalysis(
        val width: Int,
        val height: Int,
        val avgBrightness: Double,
        val contrast: Double,
        val sizeBytes: Long
    )
    
    data class DocumentFeedback(
        val passed: Boolean,
        val issues: List<String>,
        val suggestions: List<String>,
        val summary: String
    )
    
    fun composeDocumentFeedback(analysis: DocumentAnalysis, requiredType: String): DocumentFeedback {
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        
        if (analysis.width < 900 || analysis.height < 600) {
            issues += "Low resolution"
            suggestions += "Retake closer to fill the frame; ensure at least 1080p."
        }
        if (analysis.avgBrightness < 0.25) {
            issues += "Underexposed"
            suggestions += "Increase lighting or move near a bright surface."
        }
        if (analysis.contrast < 0.12) {
            issues += "Low contrast"
            suggestions += "Use a darker background for light documents (or vice‑versa)."
        }
        val sizeMB = analysis.sizeBytes / (1024.0 * 1024.0)
        if (sizeMB > 10) {
            issues += "File too large"
            suggestions += "Use JPEG with medium quality or crop borders."
        }
        
        val kycHints = when (requiredType.lowercase()) {
            "pan" -> listOf("Ensure PAN number and name are fully visible", "Avoid glare on the hologram area")
            "aadhaar" -> listOf("Mask first 8 digits if required; ensure DOB is readable", "Capture front and back")
            "passport" -> listOf("Ensure MRZ lines are sharp and fully captured", "No clipping on corners")
            else -> listOf("Ensure name, DOB, and ID number are clearly readable")
        }
        suggestions.addAll(kycHints)
        
        val passed = issues.isEmpty()
        val summary = if (passed) {
            "Looks good. All key KYC checks appear readable—ready to submit."
        } else {
            "Some improvements needed before submission."
        }
        
        return DocumentFeedback(
            passed = passed,
            issues = issues,
            suggestions = suggestions,
            summary = summary
        )
    }
    
    fun estimateEligibility(creditScore: Int, debtToIncomeRatio: Double): Int {
        val scoreComponent = (creditScore.coerceIn(300, 900) - 300) / 6.0  // maps 300-900 to 0-100
        val dtiPenalty = when {
            debtToIncomeRatio <= 0.2 -> 0
            debtToIncomeRatio <= 0.35 -> 10
            debtToIncomeRatio <= 0.5 -> 25
            else -> 40
        }
        val result = (scoreComponent - dtiPenalty).coerceIn(0.0, 100.0)
        return result.roundToInt()
    }
}


