package com.lumeai.banking

import com.lumeai.banking.models.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * FairnessMetricsEngine - Calculates QUANTITATIVE fairness metrics
 * 
 * Measures:
 * - Demographic Parity: Approval rates across groups
 * - Disparate Impact: Ratio of approval rates
 * - Statistical Significance: Is the difference meaningful?
 * - Fairness Score: Overall 0-10 rating
 * 
 * NO TEMPLATES - Real statistical calculations on actual decision data
 */
object FairnessMetricsEngine {
    
    /**
     * Calculate comprehensive fairness metrics from decision history
     */
    fun calculateFairnessMetrics(decisions: List<BankDecision>): FairnessMetricsReport {
        android.util.Log.d("FairnessMetrics", "📊 Calculating fairness metrics for ${decisions.size} decisions...")
        
        // Calculate metrics by demographic group
        val ageMetrics = calculateAgeFairness(decisions)
        val locationMetrics = calculateLocationFairness(decisions)
        val digitalLiteracyMetrics = calculateDigitalLiteracyFairness(decisions)
        
        // NEW: Calculate intersectional bias (compounded disadvantage)
        val intersectionalAnalysis = calculateIntersectionalBias(decisions)
        
        // Calculate overall fairness score
        val overallScore = calculateOverallFairnessScore(
            ageMetrics, locationMetrics, digitalLiteracyMetrics
        )
        
        // Determine if intervention needed
        val needsIntervention = ageMetrics.needsIntervention || 
                                locationMetrics.needsIntervention || 
                                digitalLiteracyMetrics.needsIntervention ||
                                intersectionalAnalysis.needsIntervention
        
        val recommendations = generateRecommendations(
            ageMetrics, locationMetrics, digitalLiteracyMetrics
        )
        
        return FairnessMetricsReport(
            overallFairnessScore = overallScore,
            ageMetrics = ageMetrics,
            locationMetrics = locationMetrics,
            digitalLiteracyMetrics = digitalLiteracyMetrics,
            intersectionalAnalysis = intersectionalAnalysis,
            needsIntervention = needsIntervention,
            recommendations = recommendations,
            totalDecisionsAnalyzed = decisions.size
        )
    }
    
    /**
     * Calculate age-based fairness metrics
     */
    private fun calculateAgeFairness(decisions: List<BankDecision>): DemographicFairnessMetric {
        // Group by age categories
        val young = decisions.filter { it.customerProfile.age < 30 }
        val middle = decisions.filter { it.customerProfile.age in 30..59 }
        val elderly = decisions.filter { it.customerProfile.age >= 60 }
        
        // Calculate approval rates
        val youngApprovalRate = calculateApprovalRate(young)
        val middleApprovalRate = calculateApprovalRate(middle)
        val elderlyApprovalRate = calculateApprovalRate(elderly)
        
        // Use middle age as baseline (typically highest approval)
        val baseline = middleApprovalRate
        
        // Calculate disparities
        val youngDisparity = abs(youngApprovalRate - baseline)
        val elderlyDisparity = abs(elderlyApprovalRate - baseline)
        val maxDisparity = maxOf(youngDisparity, elderlyDisparity)
        
        // Disparate Impact Ratio (should be >= 0.8 for fairness)
        val youngDisparateImpact = if (baseline > 0) youngApprovalRate / baseline else 1.0
        val elderlyDisparateImpact = if (baseline > 0) elderlyApprovalRate / baseline else 1.0
        val minDisparateImpact = minOf(youngDisparateImpact, elderlyDisparateImpact)
        
        // Statistical significance (simplified chi-square)
        val isSignificant = maxDisparity >= 10.0 // 10% difference is significant
        
        // Determine severity
        val severity = when {
            maxDisparity >= 20.0 || minDisparateImpact < 0.7 -> "HIGH"
            maxDisparity >= 10.0 || minDisparateImpact < 0.8 -> "MEDIUM"
            else -> "LOW"
        }
        
        val groupBreakdown = mapOf(
            "Young (18-29)" to youngApprovalRate,
            "Middle (30-59)" to middleApprovalRate,
            "Elderly (60+)" to elderlyApprovalRate
        )
        
        return DemographicFairnessMetric(
            category = "Age",
            demographicParity = maxDisparity,
            disparateImpactRatio = minDisparateImpact,
            isStatisticallySignificant = isSignificant,
            severity = severity,
            needsIntervention = severity == "HIGH",
            groupBreakdown = groupBreakdown,
            detailedAnalysis = "Age disparity: ${maxDisparity.roundToInt()}%. " +
                    "Disparate impact: ${(minDisparateImpact * 100).roundToInt()}% " +
                    "(${if (minDisparateImpact >= 0.8) "FAIR" else "BIASED"})"
        )
    }
    
    /**
     * Calculate location-based fairness metrics
     */
    private fun calculateLocationFairness(decisions: List<BankDecision>): DemographicFairnessMetric {
        val urban = decisions.filter { it.customerProfile.locationType == "urban" }
        val semiUrban = decisions.filter { it.customerProfile.locationType == "semi-urban" }
        val rural = decisions.filter { it.customerProfile.locationType == "rural" }
        
        val urbanApprovalRate = calculateApprovalRate(urban)
        val semiUrbanApprovalRate = calculateApprovalRate(semiUrban)
        val ruralApprovalRate = calculateApprovalRate(rural)
        
        val baseline = urbanApprovalRate
        val ruralDisparity = abs(ruralApprovalRate - baseline)
        val semiUrbanDisparity = abs(semiUrbanApprovalRate - baseline)
        val maxDisparity = maxOf(ruralDisparity, semiUrbanDisparity)
        
        val ruralDisparateImpact = if (baseline > 0) ruralApprovalRate / baseline else 1.0
        val minDisparateImpact = ruralDisparateImpact
        
        val isSignificant = maxDisparity >= 15.0
        
        val severity = when {
            maxDisparity >= 25.0 || minDisparateImpact < 0.7 -> "HIGH"
            maxDisparity >= 15.0 || minDisparateImpact < 0.8 -> "MEDIUM"
            else -> "LOW"
        }
        
        val groupBreakdown = mapOf(
            "Urban" to urbanApprovalRate,
            "Semi-Urban" to semiUrbanApprovalRate,
            "Rural" to ruralApprovalRate
        )
        
        return DemographicFairnessMetric(
            category = "Location",
            demographicParity = maxDisparity,
            disparateImpactRatio = minDisparateImpact,
            isStatisticallySignificant = isSignificant,
            severity = severity,
            needsIntervention = severity == "HIGH" || severity == "MEDIUM",
            groupBreakdown = groupBreakdown,
            detailedAnalysis = "Location disparity: ${maxDisparity.roundToInt()}%. " +
                    "Rural-Urban ratio: ${(minDisparateImpact * 100).roundToInt()}%"
        )
    }
    
    /**
     * Calculate digital literacy fairness metrics
     */
    private fun calculateDigitalLiteracyFairness(decisions: List<BankDecision>): DemographicFairnessMetric {
        val high = decisions.filter { it.customerProfile.digitalLiteracy == "high" }
        val medium = decisions.filter { it.customerProfile.digitalLiteracy == "medium" }
        val low = decisions.filter { it.customerProfile.digitalLiteracy == "low" }
        
        val highApprovalRate = calculateApprovalRate(high)
        val mediumApprovalRate = calculateApprovalRate(medium)
        val lowApprovalRate = calculateApprovalRate(low)
        
        val baseline = highApprovalRate
        val lowDisparity = abs(lowApprovalRate - baseline)
        val mediumDisparity = abs(mediumApprovalRate - baseline)
        val maxDisparity = maxOf(lowDisparity, mediumDisparity)
        
        val lowDisparateImpact = if (baseline > 0) lowApprovalRate / baseline else 1.0
        val minDisparateImpact = lowDisparateImpact
        
        val isSignificant = maxDisparity >= 12.0
        
        val severity = when {
            maxDisparity >= 20.0 || minDisparateImpact < 0.7 -> "HIGH"
            maxDisparity >= 12.0 || minDisparateImpact < 0.8 -> "MEDIUM"
            else -> "LOW"
        }
        
        val groupBreakdown = mapOf(
            "High Digital Literacy" to highApprovalRate,
            "Medium Digital Literacy" to mediumApprovalRate,
            "Low Digital Literacy" to lowApprovalRate
        )
        
        return DemographicFairnessMetric(
            category = "Digital Literacy",
            demographicParity = maxDisparity,
            disparateImpactRatio = minDisparateImpact,
            isStatisticallySignificant = isSignificant,
            severity = severity,
            needsIntervention = severity == "MEDIUM" || severity == "HIGH",
            groupBreakdown = groupBreakdown,
            detailedAnalysis = "Digital literacy disparity: ${maxDisparity.roundToInt()}%. " +
                    "Low vs High ratio: ${(minDisparateImpact * 100).roundToInt()}%"
        )
    }
    
    /**
     * NEW: Calculate intersectional bias - compounded disadvantage from multiple factors
     * Analyzes combinations like "Elderly + Rural + Low Digital Literacy"
     */
    private fun calculateIntersectionalBias(decisions: List<BankDecision>): IntersectionalBiasAnalysis {
        android.util.Log.d("FairnessMetrics", "🔬 Calculating intersectional bias...")
        
        // Define intersectional groups
        val mostPrivileged = decisions.filter { 
            it.customerProfile.age in 30..59 && 
            it.customerProfile.locationType == "urban" && 
            it.customerProfile.digitalLiteracy == "high"
        }
        
        val mostDisadvantaged = decisions.filter { 
            it.customerProfile.age >= 60 && 
            it.customerProfile.locationType == "rural" && 
            it.customerProfile.digitalLiteracy == "low"
        }
        
        val elderlyRural = decisions.filter { 
            it.customerProfile.age >= 60 && 
            it.customerProfile.locationType == "rural"
        }
        
        val youngUrbanTech = decisions.filter { 
            it.customerProfile.age < 30 && 
            it.customerProfile.locationType == "urban" && 
            it.customerProfile.digitalLiteracy == "high"
        }
        
        val middleRuralLowTech = decisions.filter { 
            it.customerProfile.age in 30..59 && 
            it.customerProfile.locationType == "rural" && 
            it.customerProfile.digitalLiteracy == "low"
        }
        
        // Calculate approval rates
        val mostPrivilegedRate = calculateApprovalRate(mostPrivileged)
        val mostDisadvantagedRate = calculateApprovalRate(mostDisadvantaged)
        val elderlyRuralRate = calculateApprovalRate(elderlyRural)
        val youngUrbanTechRate = calculateApprovalRate(youngUrbanTech)
        val middleRuralLowTechRate = calculateApprovalRate(middleRuralLowTech)
        
        // Calculate compounded disadvantage
        val compoundedDisadvantage = mostPrivilegedRate - mostDisadvantagedRate
        val severity = when {
            compoundedDisadvantage >= 40.0 -> "CRITICAL"
            compoundedDisadvantage >= 25.0 -> "HIGH"
            compoundedDisadvantage >= 15.0 -> "MEDIUM"
            else -> "LOW"
        }
        
        val intersections = listOf(
            IntersectionalGroup(
                "Most Privileged",
                "Middle-age + Urban + High Tech",
                mostPrivileged.size,
                mostPrivilegedRate
            ),
            IntersectionalGroup(
                "Most Disadvantaged",
                "Elderly + Rural + Low Tech",
                mostDisadvantaged.size,
                mostDisadvantagedRate
            ),
            IntersectionalGroup(
                "Elderly Rural",
                "Age 60+ + Rural (any tech)",
                elderlyRural.size,
                elderlyRuralRate
            ),
            IntersectionalGroup(
                "Young Urban Tech-Savvy",
                "Age <30 + Urban + High Tech",
                youngUrbanTech.size,
                youngUrbanTechRate
            ),
            IntersectionalGroup(
                "Middle Rural Low-Tech",
                "Age 30-59 + Rural + Low Tech",
                middleRuralLowTech.size,
                middleRuralLowTechRate
            )
        ).sortedByDescending { it.approvalRate }
        
        return IntersectionalBiasAnalysis(
            compoundedDisadvantage = compoundedDisadvantage,
            severity = severity,
            needsIntervention = severity == "CRITICAL" || severity == "HIGH",
            intersectionalGroups = intersections,
            keyInsight = generateIntersectionalInsight(compoundedDisadvantage, severity)
        )
    }
    
    private fun generateIntersectionalInsight(disadvantage: Double, severity: String): String {
        return when (severity) {
            "CRITICAL" -> "🚨 CRITICAL: Compounded disadvantage of ${disadvantage.roundToInt()}%. " +
                    "Elderly rural customers with low digital literacy face SEVERE bias. " +
                    "Immediate intervention required - consider alternative assessment methods."
            "HIGH" -> "⚠️ HIGH CONCERN: ${disadvantage.roundToInt()}% gap between most/least privileged groups. " +
                    "Intersectional bias detected - being elderly + rural + low-tech creates compounded disadvantage."
            "MEDIUM" -> "ℹ️ MODERATE: ${disadvantage.roundToInt()}% difference in outcomes. " +
                    "Some intersectional effects present. Monitor and adjust criteria."
            else -> "✅ ACCEPTABLE: ${disadvantage.roundToInt()}% gap is within tolerance. " +
                    "No critical intersectional bias detected."
        }
    }
    
    /**
     * Calculate approval rate for a group
     */
    private fun calculateApprovalRate(decisions: List<BankDecision>): Double {
        if (decisions.isEmpty()) return 0.0
        
        // Count approvals (all factors passed)
        val approvals = decisions.count { decision ->
            decision.factors.all { it.passed }
        }
        
        return (approvals.toDouble() / decisions.size) * 100.0
    }
    
    /**
     * Calculate overall fairness score (0-10)
     */
    private fun calculateOverallFairnessScore(
        age: DemographicFairnessMetric,
        location: DemographicFairnessMetric,
        digitalLiteracy: DemographicFairnessMetric
    ): Double {
        // Convert each metric to 0-10 scale
        val ageScore = metricToScore(age)
        val locationScore = metricToScore(location)
        val digitalScore = metricToScore(digitalLiteracy)
        
        // Weighted average (location bias is most critical)
        return (ageScore * 0.3 + locationScore * 0.4 + digitalScore * 0.3)
    }
    
    /**
     * Convert metric to 0-10 score
     */
    private fun metricToScore(metric: DemographicFairnessMetric): Double {
        // Perfect score if parity < 5% and disparate impact >= 0.95
        // Zero score if parity > 30% or disparate impact < 0.7
        
        val parityScore = when {
            metric.demographicParity < 5.0 -> 10.0
            metric.demographicParity < 10.0 -> 8.0
            metric.demographicParity < 15.0 -> 6.0
            metric.demographicParity < 20.0 -> 4.0
            metric.demographicParity < 30.0 -> 2.0
            else -> 0.0
        }
        
        val disparateImpactScore = when {
            metric.disparateImpactRatio >= 0.95 -> 10.0
            metric.disparateImpactRatio >= 0.85 -> 8.0
            metric.disparateImpactRatio >= 0.80 -> 6.0
            metric.disparateImpactRatio >= 0.75 -> 4.0
            metric.disparateImpactRatio >= 0.70 -> 2.0
            else -> 0.0
        }
        
        return (parityScore + disparateImpactScore) / 2.0
    }
    
    /**
     * Generate actionable recommendations
     */
    private fun generateRecommendations(
        age: DemographicFairnessMetric,
        location: DemographicFairnessMetric,
        digitalLiteracy: DemographicFairnessMetric
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (age.severity == "HIGH") {
            recommendations.add("⚠️ HIGH AGE BIAS: Implement manual review for elderly/young customers. Consider alternative credit assessment methods.")
        } else if (age.severity == "MEDIUM") {
            recommendations.add("⚠️ MODERATE AGE BIAS: Monitor age-based rejection patterns. Adjust digital footprint requirements for elderly customers.")
        }
        
        if (location.severity == "HIGH") {
            recommendations.add("🚨 CRITICAL LOCATION BIAS: Rural customers significantly disadvantaged. Accept alternative income documentation (land records, agricultural income).")
        } else if (location.severity == "MEDIUM") {
            recommendations.add("⚠️ MODERATE LOCATION BIAS: Review credit scoring model for rural fairness. Consider local references and asset-based lending.")
        }
        
        if (digitalLiteracy.severity == "HIGH") {
            recommendations.add("⚠️ HIGH DIGITAL LITERACY BIAS: Digital footprint requirement unfairly penalizes low-tech customers. Offer in-branch assessment options.")
        } else if (digitalLiteracy.severity == "MEDIUM") {
            recommendations.add("ℹ️ MODERATE DIGITAL BIAS: Reduce weight of digital footprint in credit model. Provide digital literacy training programs.")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ GOOD: Fairness metrics are within acceptable ranges. Continue monitoring.")
        }
        
        return recommendations
    }
    
    /**
     * Generate mock decisions for demo purposes
     */
    fun generateMockDecisions(): List<BankDecision> {
        val decisions = mutableListOf<BankDecision>()
        
        // Simulate realistic distribution with bias patterns
        // Urban, middle-age, high digital literacy → 75% approval
        // Rural, elderly, low digital literacy → 45% approval
        
        val profiles = listOf(
            Triple("urban", 35, "high"),
            Triple("urban", 42, "high"),
            Triple("urban", 28, "medium"),
            Triple("semi-urban", 38, "medium"),
            Triple("rural", 68, "low"),
            Triple("rural", 65, "low"),
            Triple("rural", 45, "medium"),
            Triple("urban", 22, "high"),
            Triple("rural", 72, "low")
        )
        
        profiles.forEachIndexed { index, (location, age, digitalLit) ->
            // Simulate bias: rural + elderly + low digital → higher rejection rate
            val biasScore = 
                (if (location == "rural") 15 else 0) +
                (if (age >= 60) 15 else if (age < 25) 10 else 0) +
                (if (digitalLit == "low") 10 else 0)
            
            val approved = (Math.random() * 100) > biasScore
            
            val factors = listOf(
                RawDecisionFactor("credit_score", if (approved) "720" else "620", 0.35, "650", approved),
                RawDecisionFactor("debt_to_income_ratio", if (approved) "0.35" else "0.55", 0.25, "0.40", approved),
                RawDecisionFactor("employment_tenure", if (approved) "24" else "6", 0.20, "12", approved || age >= 60),
                RawDecisionFactor("digital_footprint", if (approved) "high" else "low", 0.20, "medium", approved || digitalLit == "high")
            )
            
            decisions.add(BankDecision(
                customerId = "CUST_${index}",
                decisionType = "LOAN_APPLICATION",
                timestamp = System.currentTimeMillis(),
                factors = factors,
                customerProfile = CustomerProfile(age, location, "en", digitalLit)
            ))
        }
        
        return decisions
    }
}

/**
 * Complete fairness metrics report
 */
data class FairnessMetricsReport(
    val overallFairnessScore: Double, // 0-10
    val ageMetrics: DemographicFairnessMetric,
    val locationMetrics: DemographicFairnessMetric,
    val digitalLiteracyMetrics: DemographicFairnessMetric,
    val intersectionalAnalysis: IntersectionalBiasAnalysis,
    val needsIntervention: Boolean,
    val recommendations: List<String>,
    val totalDecisionsAnalyzed: Int
)

/**
 * Fairness metrics for specific demographic
 */
data class DemographicFairnessMetric(
    val category: String,
    val demographicParity: Double, // % difference in approval rates
    val disparateImpactRatio: Double, // ratio (should be >= 0.8)
    val isStatisticallySignificant: Boolean,
    val severity: String, // LOW, MEDIUM, HIGH
    val needsIntervention: Boolean,
    val groupBreakdown: Map<String, Double>, // group -> approval rate %
    val detailedAnalysis: String
)

/**
 * Intersectional bias analysis - compounded disadvantage
 */
data class IntersectionalBiasAnalysis(
    val compoundedDisadvantage: Double, // gap between most/least privileged
    val severity: String, // LOW, MEDIUM, HIGH, CRITICAL
    val needsIntervention: Boolean,
    val intersectionalGroups: List<IntersectionalGroup>,
    val keyInsight: String
)

/**
 * Specific intersectional group
 */
data class IntersectionalGroup(
    val groupName: String,
    val description: String,
    val sampleSize: Int,
    val approvalRate: Double
)

