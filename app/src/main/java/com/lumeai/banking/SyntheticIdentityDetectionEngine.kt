package com.lumeai.banking

import com.lumeai.banking.models.FraudAlert
import java.util.*

/**
 * 🤖 Synthetic Identity Detection Engine
 * 
 * Detects synthetic identity fraud (fake identities created by combining real/fake info)
 * with ETHICAL AI principles:
 * - Transparent scoring
 * - Bias mitigation (don't discriminate against legitimate thin-file customers)
 * - Explainable decisions
 * - Path to approval for real customers
 */
object SyntheticIdentityDetectionEngine {
    
    /**
     * Analyze a loan application for synthetic identity fraud
     */
    fun analyzeApplication(
        customerId: String,
        applicationData: ApplicationData
    ): SyntheticIdentityAnalysis {
        val riskFactors = mutableListOf<RiskFactor>()
        var totalRiskScore = 0.0
        
        // FACTOR 1: Credit File Age (New/Thin File)
        val fileAge = analyzeCreditFileAge(applicationData)
        riskFactors.add(fileAge)
        totalRiskScore += fileAge.scoreContribution
        
        // FACTOR 2: Credit Activity Pattern
        val activityPattern = analyzeCreditActivity(applicationData)
        riskFactors.add(activityPattern)
        totalRiskScore += activityPattern.scoreContribution
        
        // FACTOR 3: Identity Verification
        val identityCheck = analyzeIdentityVerification(applicationData)
        riskFactors.add(identityCheck)
        totalRiskScore += identityCheck.scoreContribution
        
        // FACTOR 4: Application Velocity
        val velocityCheck = analyzeApplicationVelocity(applicationData)
        riskFactors.add(velocityCheck)
        totalRiskScore += velocityCheck.scoreContribution
        
        // FACTOR 5: Address & Contact Verification
        val addressCheck = analyzeAddressVerification(applicationData)
        riskFactors.add(addressCheck)
        totalRiskScore += addressCheck.scoreContribution
        
        // FACTOR 6: Employment Verification
        val employmentCheck = analyzeEmploymentVerification(applicationData)
        riskFactors.add(employmentCheck)
        totalRiskScore += employmentCheck.scoreContribution
        
        // Calculate final risk level
        val riskLevel = calculateRiskLevel(totalRiskScore, riskFactors)
        
        // BIAS CHECK: Detect if legitimate thin-file customer
        val biasCheck = checkForLegitimateCustomer(applicationData, riskFactors)
        
        // Generate explanation
        val explanation = generateExplanation(riskFactors, biasCheck, applicationData)
        
        // Generate recommendation
        val recommendation = generateRecommendation(riskLevel, biasCheck, riskFactors)
        
        return SyntheticIdentityAnalysis(
            riskScore = totalRiskScore.toInt().coerceIn(0, 100),
            riskLevel = riskLevel,
            riskFactors = riskFactors,
            isLikelyLegitimate = biasCheck.isLegitimate,
            legitimacyReason = biasCheck.reason,
            explanationEnglish = explanation.english,
            explanationHindi = explanation.hindi,
            explanationTelugu = explanation.telugu,
            recommendationEnglish = recommendation.english,
            recommendationHindi = recommendation.hindi,
            recommendationTelugu = recommendation.telugu,
            pathToApproval = generatePathToApproval(riskFactors, biasCheck)
        )
    }
    
    /**
     * Analyze credit file age (new/thin file indicator)
     */
    private fun analyzeCreditFileAge(data: ApplicationData): RiskFactor {
        val ageMonths = data.creditFileAgeMonths
        val accountCount = data.numberOfCreditAccounts
        
        return when {
            ageMonths < 6 && accountCount <= 1 -> RiskFactor(
                name = "Credit File Age",
                value = "New file ($ageMonths months, $accountCount account)",
                riskLevel = "HIGH",
                scoreContribution = 35.0,
                explanation = "Credit file recently created with minimal history",
                isSuspicious = true
            )
            ageMonths < 12 && accountCount <= 2 -> RiskFactor(
                name = "Credit File Age",
                value = "Thin file ($ageMonths months, $accountCount accounts)",
                riskLevel = "MEDIUM",
                scoreContribution = 20.0,
                explanation = "Limited credit history",
                isSuspicious = true
            )
            ageMonths < 24 && accountCount <= 3 -> RiskFactor(
                name = "Credit File Age",
                value = "Young file ($ageMonths months, $accountCount accounts)",
                riskLevel = "LOW",
                scoreContribution = 10.0,
                explanation = "Building credit history",
                isSuspicious = false
            )
            else -> RiskFactor(
                name = "Credit File Age",
                value = "Established file ($ageMonths months, $accountCount accounts)",
                riskLevel = "SAFE",
                scoreContribution = 0.0,
                explanation = "Sufficient credit history",
                isSuspicious = false
            )
        }
    }
    
    /**
     * Analyze credit activity pattern
     */
    private fun analyzeCreditActivity(data: ApplicationData): RiskFactor {
        val rapidBuildUp = data.recentAccountsOpened >= 5 && data.creditFileAgeMonths < 12
        val highUtilization = data.creditUtilization > 90
        val loanAmount = data.requestedLoanAmount
        
        return when {
            rapidBuildUp && loanAmount > 500000 -> RiskFactor(
                name = "Credit Activity",
                value = "${data.recentAccountsOpened} accounts opened in ${data.creditFileAgeMonths} months, requesting ₹${loanAmount}",
                riskLevel = "CRITICAL",
                scoreContribution = 40.0,
                explanation = "Rapid credit buildup followed by large loan request (classic synthetic identity pattern)",
                isSuspicious = true
            )
            rapidBuildUp -> RiskFactor(
                name = "Credit Activity",
                value = "${data.recentAccountsOpened} accounts opened recently",
                riskLevel = "HIGH",
                scoreContribution = 25.0,
                explanation = "Multiple accounts opened in short timeframe",
                isSuspicious = true
            )
            highUtilization && data.creditFileAgeMonths < 6 -> RiskFactor(
                name = "Credit Activity",
                value = "${data.creditUtilization}% utilization on new file",
                riskLevel = "MEDIUM",
                scoreContribution = 15.0,
                explanation = "High credit utilization on new credit file",
                isSuspicious = true
            )
            else -> RiskFactor(
                name = "Credit Activity",
                value = "Normal credit patterns",
                riskLevel = "SAFE",
                scoreContribution = 0.0,
                explanation = "Credit behavior within normal parameters",
                isSuspicious = false
            )
        }
    }
    
    /**
     * Analyze identity verification
     */
    private fun analyzeIdentityVerification(data: ApplicationData): RiskFactor {
        val panMismatch = !data.panVerified
        val aadhaarMismatch = !data.aadhaarVerified
        val addressMismatch = !data.addressMatches
        
        val mismatchCount = listOf(panMismatch, aadhaarMismatch, addressMismatch).count { it }
        
        return when (mismatchCount) {
            3 -> RiskFactor(
                name = "Identity Verification",
                value = "Multiple document mismatches",
                riskLevel = "CRITICAL",
                scoreContribution = 50.0,
                explanation = "PAN, Aadhaar, and address verification all failed",
                isSuspicious = true
            )
            2 -> RiskFactor(
                name = "Identity Verification",
                value = "Some documents don't match",
                riskLevel = "HIGH",
                scoreContribution = 30.0,
                explanation = "Two identity documents failed verification",
                isSuspicious = true
            )
            1 -> RiskFactor(
                name = "Identity Verification",
                value = "Minor verification issue",
                riskLevel = "MEDIUM",
                scoreContribution = 15.0,
                explanation = "One document failed verification (may be data entry error)",
                isSuspicious = false
            )
            else -> RiskFactor(
                name = "Identity Verification",
                value = "All documents verified",
                riskLevel = "SAFE",
                scoreContribution = 0.0,
                explanation = "Identity successfully verified",
                isSuspicious = false
            )
        }
    }
    
    /**
     * Analyze application velocity (multiple applications in short time)
     */
    private fun analyzeApplicationVelocity(data: ApplicationData): RiskFactor {
        val recentApplications = data.loanApplicationsLast30Days
        
        return when {
            recentApplications >= 5 -> RiskFactor(
                name = "Application Velocity",
                value = "$recentApplications applications in 30 days",
                riskLevel = "CRITICAL",
                scoreContribution = 35.0,
                explanation = "Extremely high application velocity suggests automated fraud",
                isSuspicious = true
            )
            recentApplications >= 3 -> RiskFactor(
                name = "Application Velocity",
                value = "$recentApplications applications in 30 days",
                riskLevel = "HIGH",
                scoreContribution = 20.0,
                explanation = "High application velocity is concerning",
                isSuspicious = true
            )
            recentApplications == 2 -> RiskFactor(
                name = "Application Velocity",
                value = "$recentApplications applications in 30 days",
                riskLevel = "LOW",
                scoreContribution = 5.0,
                explanation = "Multiple applications may indicate shopping for best rate",
                isSuspicious = false
            )
            else -> RiskFactor(
                name = "Application Velocity",
                value = "First application in 30 days",
                riskLevel = "SAFE",
                scoreContribution = 0.0,
                explanation = "Normal application pattern",
                isSuspicious = false
            )
        }
    }
    
    /**
     * Analyze address verification
     */
    private fun analyzeAddressVerification(data: ApplicationData): RiskFactor {
        val addressAge = data.addressAgeMonths
        val addressType = data.addressType
        
        return when {
            addressAge < 3 && addressType == "PO_BOX" -> RiskFactor(
                name = "Address Verification",
                value = "PO Box address, recent",
                riskLevel = "HIGH",
                scoreContribution = 25.0,
                explanation = "PO Box addresses recently established are higher risk",
                isSuspicious = true
            )
            addressAge < 6 -> RiskFactor(
                name = "Address Verification",
                value = "Recent address ($addressAge months)",
                riskLevel = "MEDIUM",
                scoreContribution = 10.0,
                explanation = "Recently changed address",
                isSuspicious = false
            )
            !data.addressMatches -> RiskFactor(
                name = "Address Verification",
                value = "Address doesn't match records",
                riskLevel = "MEDIUM",
                scoreContribution = 15.0,
                explanation = "Address mismatch across documents",
                isSuspicious = true
            )
            else -> RiskFactor(
                name = "Address Verification",
                value = "Stable address ($addressAge months)",
                riskLevel = "SAFE",
                scoreContribution = 0.0,
                explanation = "Address verified and stable",
                isSuspicious = false
            )
        }
    }
    
    /**
     * Analyze employment verification
     */
    private fun analyzeEmploymentVerification(data: ApplicationData): RiskFactor {
        val employmentVerified = data.employmentVerified
        val employmentMonths = data.employmentTenureMonths
        val salaryVerified = data.salaryVerified
        
        return when {
            !employmentVerified && !salaryVerified -> RiskFactor(
                name = "Employment Verification",
                value = "Cannot verify employment or salary",
                riskLevel = "HIGH",
                scoreContribution = 30.0,
                explanation = "Unable to confirm employment and income",
                isSuspicious = true
            )
            employmentMonths < 3 -> RiskFactor(
                name = "Employment Verification",
                value = "Very new job ($employmentMonths months)",
                riskLevel = "MEDIUM",
                scoreContribution = 15.0,
                explanation = "Very recent employment",
                isSuspicious = false
            )
            !salaryVerified -> RiskFactor(
                name = "Employment Verification",
                value = "Salary not verified",
                riskLevel = "MEDIUM",
                scoreContribution = 10.0,
                explanation = "Cannot confirm stated income",
                isSuspicious = false
            )
            else -> RiskFactor(
                name = "Employment Verification",
                value = "Employment and salary verified",
                riskLevel = "SAFE",
                scoreContribution = 0.0,
                explanation = "Employment successfully verified",
                isSuspicious = false
            )
        }
    }
    
    /**
     * Calculate overall risk level
     */
    private fun calculateRiskLevel(totalScore: Double, factors: List<RiskFactor>): String {
        val criticalFactorCount = factors.count { it.riskLevel == "CRITICAL" }
        val highFactorCount = factors.count { it.riskLevel == "HIGH" }
        
        return when {
            criticalFactorCount >= 2 || totalScore >= 80 -> "CRITICAL"
            criticalFactorCount >= 1 || totalScore >= 60 -> "HIGH"
            highFactorCount >= 2 || totalScore >= 40 -> "MEDIUM"
            else -> "LOW"
        }
    }
    
    /**
     * ETHICAL AI: Check if customer is legitimate despite thin file
     */
    private fun checkForLegitimateCustomer(data: ApplicationData, factors: List<RiskFactor>): BiasCheck {
        // Young adults (18-25) legitimately have thin files
        if (data.age in 18..25 && data.isStudent) {
            return BiasCheck(
                isLegitimate = true,
                reason = "Young adult/student with naturally thin credit file",
                mitigationSteps = listOf(
                    "Consider alternative data (rent payments, utility bills)",
                    "Accept education documents as proof of stability",
                    "Lower loan amount to build credit history"
                )
            )
        }
        
        // New to country (immigrants) legitimately have thin files
        if (data.isRecentImmigrant && data.creditFileAgeMonths < 12) {
            return BiasCheck(
                isLegitimate = true,
                reason = "Recent immigrant with no prior Indian credit history",
                mitigationSteps = listOf(
                    "Request international credit report",
                    "Verify employment and visa status",
                    "Consider secured credit products first"
                )
            )
        }
        
        // First-time formal credit users (previously unbanked)
        if (data.hasVerifiableIncomeSource && data.creditFileAgeMonths < 6 && data.numberOfCreditAccounts <= 1) {
            return BiasCheck(
                isLegitimate = true,
                reason = "First-time formal credit user (previously unbanked population)",
                mitigationSteps = listOf(
                    "Verify alternate data: UPI transaction history, mobile wallet usage",
                    "Consider microfinance or small credit line first",
                    "Manual review with local branch"
                )
            )
        }
        
        // Housewives/homemakers entering credit market
        if (data.isHomemaker && data.hasCoApplicant) {
            return BiasCheck(
                isLegitimate = true,
                reason = "Homemaker with co-applicant (legitimate thin file)",
                mitigationSteps = listOf(
                    "Verify co-applicant's credit",
                    "Consider household income, not just individual",
                    "Accept alternate verification (property ownership, family ties)"
                )
            )
        }
        
        // Rural customers with limited digital footprint
        if (data.isRuralCustomer && !data.hasDigitalFootprint) {
            return BiasCheck(
                isLegitimate = true,
                reason = "Rural customer with limited digital/credit history (not fraud)",
                mitigationSteps = listOf(
                    "Manual verification through branch network",
                    "Accept agricultural income proof, land records",
                    "Village head/sarpanch recommendation"
                )
            )
        }
        
        // Default: Not obviously legitimate
        return BiasCheck(
            isLegitimate = false,
            reason = "Risk profile matches synthetic identity patterns",
            mitigationSteps = listOf(
                "Enhanced identity verification required",
                "In-person verification at branch",
                "Video KYC with live identity check"
            )
        )
    }
    
    /**
     * Generate human-readable explanation
     */
    private fun generateExplanation(
        factors: List<RiskFactor>,
        biasCheck: BiasCheck,
        data: ApplicationData
    ): MultilingualText {
        val suspiciousFactors = factors.filter { it.isSuspicious }
        
        val english = if (biasCheck.isLegitimate) {
            buildString {
                append("⚠️ This application was flagged for review, but shows signs of a legitimate customer.\n\n")
                append("🎯 Reason: ${biasCheck.reason}\n\n")
                append("📊 Risk Factors Detected:\n")
                suspiciousFactors.forEach {
                    append("• ${it.name}: ${it.explanation}\n")
                }
                append("\n💡 This customer may qualify through alternate verification.\n")
            }
        } else {
            buildString {
                append("🚨 This application shows patterns consistent with synthetic identity fraud.\n\n")
                append("📊 Risk Factors:\n")
                suspiciousFactors.forEach {
                    append("• ${it.name}: ${it.explanation}\n")
                }
                append("\n⚠️ Enhanced verification recommended before approval.\n")
            }
        }
        
        val hindi = if (biasCheck.isLegitimate) {
            "⚠️ इस आवेदन को समीक्षा के लिए चिह्नित किया गया था, लेकिन वैध ग्राहक के संकेत दिखाता है।\n\n" +
            "🎯 कारण: ${biasCheck.reason}\n\n" +
            "💡 यह ग्राहक वैकल्पिक सत्यापन के माध्यम से योग्य हो सकता है।"
        } else {
            "🚨 यह आवेदन नकली पहचान धोखाधड़ी के पैटर्न दिखाता है।\n\n" +
            "⚠️ अनुमोदन से पहले बेहतर सत्यापन की सिफारिश की जाती है।"
        }
        
        val telugu = if (biasCheck.isLegitimate) {
            "⚠️ ఈ దరఖాస్తు సమీక్ష కోసం గుర్తించబడింది, కానీ చట్టబద్ధ కస్టమర్ సంకేతాలను చూపిస్తుంది।\n\n" +
            "🎯 కారణం: ${biasCheck.reason}\n\n" +
            "💡 ఈ కస్టమర్ ప్రత్యామ్నాయ ధృవీకరణ ద్వారా అర్హత పొందవచ్చు।"
        } else {
            "🚨 ఈ దరఖాస్తు సింథటిక్ గుర్తింపు మోసం నమూనాలను చూపిస్తుంది।\n\n" +
            "⚠️ ఆమోదానికి ముందు మెరుగైన ధృవీకరణ సిఫార్సు చేయబడింది।"
        }
        
        return MultilingualText(english, hindi, telugu)
    }
    
    /**
     * Generate recommendation for bank
     */
    private fun generateRecommendation(
        riskLevel: String,
        biasCheck: BiasCheck,
        factors: List<RiskFactor>
    ): MultilingualText {
        val english = if (biasCheck.isLegitimate) {
            buildString {
                append("✅ Recommended Action: APPROVE with alternate verification\n\n")
                append("📝 Steps:\n")
                biasCheck.mitigationSteps.forEachIndexed { index, step ->
                    append("${index + 1}. $step\n")
                }
                append("\n💡 This helps include underserved populations without compromising security.")
            }
        } else {
            when (riskLevel) {
                "CRITICAL" -> "🚫 Recommended Action: DENY\n\nThis application shows multiple high-risk synthetic identity patterns. Do not approve."
                "HIGH" -> "⚠️ Recommended Action: MANUAL REVIEW\n\nRequires enhanced verification:\n• In-person branch visit\n• Video KYC\n• Additional documentation"
                "MEDIUM" -> "⚠️ Recommended Action: ENHANCED VERIFICATION\n\nApprove only after:\n• Document re-verification\n• Employment confirmation\n• Lower loan amount"
                else -> "✅ Recommended Action: APPROVE\n\nLow risk profile. Standard verification sufficient."
            }
        }
        
        val hindi = if (biasCheck.isLegitimate) {
            "✅ अनुशंसित कार्रवाई: वैकल्पिक सत्यापन के साथ स्वीकृत करें\n\n" +
            "💡 यह सुरक्षा से समझौता किए बिना वंचित आबादी को शामिल करने में मदद करता है।"
        } else {
            when (riskLevel) {
                "CRITICAL" -> "🚫 अनुशंसित कार्रवाई: अस्वीकार करें"
                "HIGH" -> "⚠️ अनुशंसित कार्रवाई: मैनुअल समीक्षा आवश्यक"
                "MEDIUM" -> "⚠️ अनुशंसित कार्रवाई: बेहतर सत्यापन"
                else -> "✅ अनुशंसित कार्रवाई: स्वीकृत करें"
            }
        }
        
        val telugu = if (biasCheck.isLegitimate) {
            "✅ సిఫార్సు చేయబడిన చర్య: ప్రత్యామ్నాయ ధృవీకరణతో ఆమోదించండి\n\n" +
            "💡 ఇది భద్రతను రాజీ చేయకుండా సేవలు పొందని జనాభాను చేర్చడానికి సహాయపడుతుంది।"
        } else {
            when (riskLevel) {
                "CRITICAL" -> "🚫 సిఫార్సు చేయబడిన చర్య: తిరస్కరించండి"
                "HIGH" -> "⚠️ సిఫార్సు చేయబడిన చర్య: మాన్యువల్ సమీక్ష అవసరం"
                "MEDIUM" -> "⚠️ సిఫార్సు చేయబడిన చర్య: మెరుగైన ధృవీకరణ"
                else -> "✅ సిఫార్సు చేయబడిన చర్య: ఆమోదించండి"
            }
        }
        
        return MultilingualText(english, hindi, telugu)
    }
    
    /**
     * Generate path to approval for legitimate thin-file customers
     */
    private fun generatePathToApproval(
        factors: List<RiskFactor>,
        biasCheck: BiasCheck
    ): List<ApprovalStep> {
        if (!biasCheck.isLegitimate) {
            return listOf(
                ApprovalStep(
                    stepNumber = 1,
                    title = "Not Available",
                    description = "This application does not qualify for alternate approval path.",
                    timeframe = "N/A",
                    difficulty = "N/A"
                )
            )
        }
        
        return biasCheck.mitigationSteps.mapIndexed { index, step ->
            ApprovalStep(
                stepNumber = index + 1,
                title = step,
                description = "Complete this verification step",
                timeframe = when (index) {
                    0 -> "1-2 days"
                    1 -> "3-5 days"
                    else -> "1 week"
                },
                difficulty = when (index) {
                    0 -> "Easy"
                    1 -> "Medium"
                    else -> "Medium"
                }
            )
        }
    }
    
    // =============== DATA MODELS ===============
    
    data class ApplicationData(
        // Credit File Info
        val creditFileAgeMonths: Int,
        val numberOfCreditAccounts: Int,
        val recentAccountsOpened: Int,
        val creditUtilization: Int, // percentage
        val loanApplicationsLast30Days: Int,
        
        // Identity Verification
        val panVerified: Boolean,
        val aadhaarVerified: Boolean,
        val addressMatches: Boolean,
        
        // Address Info
        val addressAgeMonths: Int,
        val addressType: String, // HOME, PO_BOX, OFFICE
        
        // Employment Info
        val employmentVerified: Boolean,
        val employmentTenureMonths: Int,
        val salaryVerified: Boolean,
        
        // Loan Info
        val requestedLoanAmount: Int,
        
        // Customer Profile (for bias checks)
        val age: Int,
        val isStudent: Boolean,
        val isRecentImmigrant: Boolean,
        val hasVerifiableIncomeSource: Boolean,
        val isHomemaker: Boolean,
        val hasCoApplicant: Boolean,
        val isRuralCustomer: Boolean,
        val hasDigitalFootprint: Boolean
    )
    
    data class RiskFactor(
        val name: String,
        val value: String,
        val riskLevel: String, // SAFE, LOW, MEDIUM, HIGH, CRITICAL
        val scoreContribution: Double, // 0-100
        val explanation: String,
        val isSuspicious: Boolean
    )
    
    data class BiasCheck(
        val isLegitimate: Boolean,
        val reason: String,
        val mitigationSteps: List<String>
    )
    
    data class MultilingualText(
        val english: String,
        val hindi: String,
        val telugu: String
    )
    
    data class ApprovalStep(
        val stepNumber: Int,
        val title: String,
        val description: String,
        val timeframe: String,
        val difficulty: String
    )
    
    data class SyntheticIdentityAnalysis(
        val riskScore: Int, // 0-100
        val riskLevel: String, // LOW, MEDIUM, HIGH, CRITICAL
        val riskFactors: List<RiskFactor>,
        val isLikelyLegitimate: Boolean,
        val legitimacyReason: String,
        val explanationEnglish: String,
        val explanationHindi: String,
        val explanationTelugu: String,
        val recommendationEnglish: String,
        val recommendationHindi: String,
        val recommendationTelugu: String,
        val pathToApproval: List<ApprovalStep>
    )
    
    /**
     * Convert analysis to FraudAlert for Firebase storage
     */
    fun analysisToFraudAlert(
        customerId: String,
        bankName: String,
        loanAmount: Int,
        analysis: SyntheticIdentityAnalysis
    ): FraudAlert {
        return FraudAlert(
            id = UUID.randomUUID().toString(),
            customerId = customerId,
            transactionType = "LOAN_APPLICATION",
            amount = loanAmount.toDouble(),
            merchantName = bankName,
            location = "Online Application",
            riskScore = analysis.riskScore.toDouble(),
            riskLevel = analysis.riskLevel,
            status = if (analysis.riskLevel == "CRITICAL") "BLOCKED" else "FLAGGED",
            timestamp = System.currentTimeMillis(),
            reasonEnglish = "Synthetic Identity Detection: ${analysis.riskFactors.filter { it.isSuspicious }.joinToString(", ") { it.name }}",
            reasonHindi = "कृत्रिम पहचान का पता लगाना",
            reasonTelugu = "సింథటిక్ గుర్తింపు గుర్తింపు",
            recommendationEnglish = analysis.recommendationEnglish,
            recommendationHindi = analysis.recommendationHindi,
            recommendationTelugu = analysis.recommendationTelugu,
            unusualAmount = analysis.riskFactors.any { it.name == "Credit Activity" && it.isSuspicious },
            unusualLocation = false,
            unusualTime = false,
            newMerchant = analysis.riskFactors.any { it.name == "Credit File Age" && it.riskLevel in listOf("HIGH", "CRITICAL") },
            multipleAttempts = analysis.riskFactors.any { it.name == "Application Velocity" && it.isSuspicious },
            deviceMismatch = false,
            userConfirmed = false,
            userReportedFraud = false
        )
    }
}

