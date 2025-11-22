package com.lumeai.banking

import kotlin.math.roundToInt

/**
 * Transparency Metrics Engine
 * Calculates real-time AI transparency metrics based on user interactions
 */
object TransparencyMetricsEngine {
    
    // Activity counters (in production, these would be from database/analytics)
    private var explanationsViewed = 0
    private var messagesDecoded = 0
    private var applicationsTracked = 0
    private var biasChecksPerformed = 0
    private var consentPreferencesSet = 0
    
    /**
     * Calculate overall AI Transparency Score (0-100)
     * Based on multiple factors with different weights
     */
    fun calculateTransparencyScore(): Int {
        // Factor 1: Explanation Engagement (30%)
        val explanationScore = calculateExplanationEngagement() * 0.30
        
        // Factor 2: Consent Control Activity (25%)
        val consentScore = calculateConsentControlScore() * 0.25
        
        // Factor 3: Bias Awareness (20%)
        val biasScore = calculateBiasAwarenessScore() * 0.20
        
        // Factor 4: Appeal Process Awareness (15%)
        val appealScore = calculateAppealAwarenessScore() * 0.15
        
        // Factor 5: Data Clarity (10%)
        val dataScore = calculateDataClarityScore() * 0.10
        
        val totalScore = explanationScore + consentScore + biasScore + appealScore + dataScore
        
        // Ensure score is between 0-100
        return totalScore.roundToInt().coerceIn(0, 100)
    }
    
    /**
     * Explanation Engagement: How much user interacts with AI explanations
     */
    private fun calculateExplanationEngagement(): Double {
        // Perfect score if user has viewed multiple explanations
        return when {
            explanationsViewed >= 10 -> 100.0
            explanationsViewed >= 5 -> 80.0
            explanationsViewed >= 2 -> 60.0
            explanationsViewed >= 1 -> 40.0
            else -> 20.0 // Base score for having the feature
        }
    }
    
    /**
     * Consent Control Score: How actively user manages their data
     */
    private fun calculateConsentControlScore(): Double {
        // Perfect score if user has set multiple consent preferences
        return when {
            consentPreferencesSet >= 8 -> 100.0
            consentPreferencesSet >= 5 -> 80.0
            consentPreferencesSet >= 3 -> 60.0
            consentPreferencesSet >= 1 -> 40.0
            else -> 30.0 // Base score for having consent controls
        }
    }
    
    /**
     * Bias Awareness Score: User's awareness of bias detection
     */
    private fun calculateBiasAwarenessScore(): Double {
        val biasAlertsDetected = getBiasAlertsCount()
        
        // Lower alerts = higher transparency (no bias detected)
        return when {
            biasAlertsDetected == 0 && biasChecksPerformed > 0 -> 100.0
            biasAlertsDetected == 0 -> 90.0 // Default good score
            biasAlertsDetected <= 2 -> 70.0
            biasAlertsDetected <= 5 -> 50.0
            else -> 30.0
        }
    }
    
    /**
     * Appeal Awareness Score: User knows their appeal rights
     */
    private fun calculateAppealAwarenessScore(): Double {
        // Based on whether user has seen appeal information
        return when {
            explanationsViewed >= 3 -> 100.0 // Seen multiple explanations with appeal info
            explanationsViewed >= 1 -> 80.0
            else -> 60.0 // Base score for having appeal rights
        }
    }
    
    /**
     * Data Clarity Score: How clear is data usage
     */
    private fun calculateDataClarityScore(): Double {
        // Based on transparency features used
        val featuresUsed = listOf(
            explanationsViewed > 0,
            messagesDecoded > 0,
            consentPreferencesSet > 0,
            applicationsTracked > 0
        ).count { it }
        
        return when (featuresUsed) {
            4 -> 100.0
            3 -> 85.0
            2 -> 70.0
            1 -> 55.0
            else -> 40.0
        }
    }
    
    /**
     * Get total AI decisions explained
     */
    fun getAIDecisionsExplained(): Int {
        return explanationsViewed + applicationsTracked
    }
    
    /**
     * Get bias alerts count
     */
    fun getBiasAlertsCount(): Int {
        // In production, query from BiasDetector logs
        // For demo, we show 0 to indicate excellent transparency
        return 0
    }
    
    /**
     * Get active consent controls count
     */
    fun getConsentControlsCount(): Int {
        return consentPreferencesSet
    }
    
    /**
     * Get messages decoded count
     */
    fun getMessagesDecodedCount(): Int {
        return messagesDecoded
    }
    
    /**
     * Get transparency status label
     */
    fun getTransparencyStatusLabel(score: Int): String {
        return when {
            score >= 90 -> "Excellent AI Transparency"
            score >= 75 -> "Good AI Transparency"
            score >= 60 -> "Moderate AI Transparency"
            score >= 40 -> "Fair AI Transparency"
            else -> "Limited AI Transparency"
        }
    }
    
    /**
     * Get transparency description
     */
    fun getTransparencyDescription(score: Int): String {
        return when {
            score >= 90 -> "Your bank decisions are highly transparent with full AI explainability and zero detected bias."
            score >= 75 -> "Your bank provides good transparency with comprehensive AI explanations."
            score >= 60 -> "Your bank offers moderate transparency. Engage more with explanations to improve understanding."
            score >= 40 -> "Basic transparency available. Use more features to increase your control and understanding."
            else -> "Limited transparency detected. Explore AI explainability features to understand decisions better."
        }
    }
    
    /**
     * Get recent activity (last 5 activities)
     */
    fun getRecentActivity(): List<Triple<String, String, String>> {
        // In production, query from activity logs database
        // For demo, generate based on actual usage
        val activities = mutableListOf<Triple<String, String, String>>()
        
        if (applicationsTracked > 0) {
            activities.add(Triple(
                "Loan Application",
                "AI explained rejection reasons with actionable steps",
                "${applicationsTracked} days ago"
            ))
        }
        
        if (messagesDecoded > 0) {
            activities.add(Triple(
                "Transaction Alert",
                "Decoded fraud prevention message with AI",
                "${messagesDecoded} days ago"
            ))
        }
        
        if (explanationsViewed > 0) {
            activities.add(Triple(
                "Credit Limit",
                "AI explained limit reduction with improvement tips",
                "${explanationsViewed} days ago"
            ))
        }
        
        if (consentPreferencesSet > 0) {
            activities.add(Triple(
                "Consent Update",
                "Updated $consentPreferencesSet data sharing preferences",
                "1 week ago"
            ))
        }
        
        activities.add(Triple(
            "Bias Check",
            "No bias detected in recent decisions - all decisions were fair",
            "1 week ago"
        ))
        
        return activities.take(5)
    }
    
    // ============ Activity Tracking Methods (Production would use analytics SDK) ============
    
    /**
     * Track when user views an AI explanation
     */
    fun trackExplanationViewed() {
        explanationsViewed++
        android.util.Log.d("TransparencyMetrics", "Explanation viewed. Total: $explanationsViewed")
    }
    
    /**
     * Track when user decodes a message
     */
    fun trackMessageDecoded() {
        messagesDecoded++
        android.util.Log.d("TransparencyMetrics", "Message decoded. Total: $messagesDecoded")
    }
    
    /**
     * Track when user tracks an application
     */
    fun trackApplicationTracked() {
        applicationsTracked++
        android.util.Log.d("TransparencyMetrics", "Application tracked. Total: $applicationsTracked")
    }
    
    /**
     * Track when user sets consent preferences
     */
    fun trackConsentPreferenceSet(count: Int = 1) {
        consentPreferencesSet += count
        android.util.Log.d("TransparencyMetrics", "Consent set. Total: $consentPreferencesSet")
    }
    
    /**
     * Track bias check performed
     */
    fun trackBiasCheckPerformed() {
        biasChecksPerformed++
        android.util.Log.d("TransparencyMetrics", "Bias check performed. Total: $biasChecksPerformed")
    }
    
    /**
     * Initialize with demo data for hackathon
     */
    fun initializeDemoData() {
        explanationsViewed = 12
        messagesDecoded = 5
        applicationsTracked = 4
        biasChecksPerformed = 8
        consentPreferencesSet = 8
        
        android.util.Log.d("TransparencyMetrics", "Demo data initialized")
    }
    
    /**
     * Reset all metrics (for testing)
     */
    fun resetMetrics() {
        explanationsViewed = 0
        messagesDecoded = 0
        applicationsTracked = 0
        biasChecksPerformed = 0
        consentPreferencesSet = 0
    }
    
    // ============ Setter Methods (for demo controller) ============
    
    fun setExplanationsViewed(count: Int) {
        explanationsViewed = count
        android.util.Log.d("TransparencyMetrics", "Explanations set to: $count")
    }
    
    fun setMessagesDecoded(count: Int) {
        messagesDecoded = count
        android.util.Log.d("TransparencyMetrics", "Messages set to: $count")
    }
    
    fun setApplicationsTracked(count: Int) {
        applicationsTracked = count
        android.util.Log.d("TransparencyMetrics", "Applications set to: $count")
    }
    
    fun setConsentPreferences(count: Int) {
        consentPreferencesSet = count
        android.util.Log.d("TransparencyMetrics", "Consent set to: $count")
    }
    
    fun setBiasChecks(count: Int) {
        biasChecksPerformed = count
        android.util.Log.d("TransparencyMetrics", "Bias checks set to: $count")
    }
}

