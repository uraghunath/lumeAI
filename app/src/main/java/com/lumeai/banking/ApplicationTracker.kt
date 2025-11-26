package com.lumeai.banking

import com.lumeai.banking.models.CustomerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.delay

/**
 * ApplicationTracker - Tracks loan/card applications across multiple banks
 * Provides real-time status and AI-powered reason inference
 */
object ApplicationTracker {
    
    // Azure OpenAI via Grand Central AI Gateway
    private const val OPENAI_API_KEY = "zzzzzzzzzz"
    private const val OPENAI_ENDPOINT = "https://api.hack.lume.services.io/openai/v1"
    private const val OPENAI_MODEL = "gpt-4o-mini"
    private const val AGENT_ID = "zzzzzzzzzz" // AI Gateway requirement
    private const val API_VERSION = "2024-08-01-preview"
    
    /**
     * Supported banks
     */
    enum class Bank(val displayName: String, val code: String) {
        HDFC("HDFC Bank", "HDFC"),
        SBI("State Bank of India", "SBI"),
        ICICI("ICICI Bank", "ICICI"),
        AXIS("Axis Bank", "AXIS"),
        KOTAK("Kotak Mahindra Bank", "KOTAK"),
        BOB("Bank of Baroda", "BOB"),
        PNB("Punjab National Bank", "PNB"),
        IDFC("IDFC First Bank", "IDFC")
    }
    
    /**
     * Application status
     */
    enum class ApplicationStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        PENDING_DOCUMENTS,
        NOT_FOUND
    }
    
    /**
     * Track application by bank and application number
     */
    suspend fun trackApplication(
        bank: Bank,
        applicationNumber: String,
        userProfile: CustomerProfile? = null
    ): ApplicationResult {
        // Simulate API delay
        delay(1500)
        
        // Validate application number format
        if (!isValidApplicationNumber(bank, applicationNumber)) {
            return ApplicationResult(
                found = false,
                status = ApplicationStatus.NOT_FOUND,
                message = "Invalid application number format for ${bank.displayName}",
                applicationNumber = applicationNumber,
                bank = bank,
                detailedReason = null,
                aiInferredReasons = null,
                appliedDate = null,
                lastUpdated = null,
                nextSteps = null
            )
        }
        
        // Simulate fetching from bank's system
        return fetchFromBank(bank, applicationNumber, userProfile)
    }
    
    /**
     * Validate application number format
     */
    private fun isValidApplicationNumber(bank: Bank, appNum: String): Boolean {
        return when (bank) {
            Bank.HDFC -> appNum.matches(Regex("^(HDFC|LN|CC)\\d{6,10}$"))
            Bank.SBI -> appNum.matches(Regex("^(SBI|LA|CA)\\d{8,12}$"))
            Bank.ICICI -> appNum.matches(Regex("^(ICICI|APP)\\d{8,10}$"))
            Bank.AXIS -> appNum.matches(Regex("^(AXIS|AX)\\d{8,10}$"))
            Bank.KOTAK -> appNum.matches(Regex("^(KM|KOTAK)\\d{8,10}$"))
            Bank.BOB -> appNum.matches(Regex("^(BOB|BB)\\d{8,10}$"))
            Bank.PNB -> appNum.matches(Regex("^(PNB|PN)\\d{8,10}$"))
            Bank.IDFC -> appNum.matches(Regex("^(IDFC|IF)\\d{8,10}$"))
        }
    }
    
    /**
     * Fetch application details from bank
     */
    private suspend fun fetchFromBank(
        bank: Bank,
        appNum: String,
        userProfile: CustomerProfile?
    ): ApplicationResult {
        // Specific test IDs for demo purposes
        val upperAppNum = appNum.uppercase()
        
        // REJECTED WITH DETAILED REASON - Credit Score
        if (upperAppNum in listOf("HDFC123456", "LN123456", "SBI11111111", "LA11111111", "REJECTED1")) {
            return ApplicationResult(
                found = true,
                status = ApplicationStatus.REJECTED,
                message = "Application has been rejected",
                applicationNumber = appNum,
                bank = bank,
                detailedReason = RejectionDetails(
                    primaryReason = "Credit Score Below Threshold",
                    factors = listOf(
                        "Credit score: 620 (Required: 700+)",
                        "3 late payments in last 12 months",
                        "Credit utilization: 78% (High risk)"
                    ),
                    specificDetails = mapOf(
                        "Credit Score" to "620",
                        "Required Score" to "700",
                        "Late Payments" to "3 in last year"
                    )
                ),
                aiInferredReasons = null,
                appliedDate = "2024-10-15",
                lastUpdated = "2024-11-01",
                nextSteps = getNextStepsForRejection(RejectionDetails(
                    primaryReason = "Credit Score Below Threshold",
                    factors = emptyList(),
                    specificDetails = emptyMap()
                ))
            )
        }
        
        // REJECTED WITH DETAILED REASON - High Debt
        if (upperAppNum in listOf("HDFC234567", "LN234567", "SBI22222222", "LA22222222", "REJECTED2")) {
            return ApplicationResult(
                found = true,
                status = ApplicationStatus.REJECTED,
                message = "Application has been rejected",
                applicationNumber = appNum,
                bank = bank,
                detailedReason = RejectionDetails(
                    primaryReason = "High Debt-to-Income Ratio",
                    factors = listOf(
                        "Debt-to-income ratio: 52% (Limit: 40%)",
                        "Existing EMI: ₹38,000/month",
                        "Declared income: ₹75,000/month"
                    ),
                    specificDetails = mapOf(
                        "DTI Ratio" to "52%",
                        "Max Allowed" to "40%",
                        "Existing EMI" to "₹38,000"
                    )
                ),
                aiInferredReasons = null,
                appliedDate = "2024-10-16",
                lastUpdated = "2024-11-02",
                nextSteps = getNextStepsForRejection(RejectionDetails(
                    primaryReason = "High Debt-to-Income Ratio",
                    factors = emptyList(),
                    specificDetails = emptyMap()
                ))
            )
        }
        
        // REJECTED WITH DETAILED REASON - Employment
        if (upperAppNum in listOf("HDFC345678", "LN345678", "SBI33333333", "LA33333333", "REJECTED3")) {
            return ApplicationResult(
                found = true,
                status = ApplicationStatus.REJECTED,
                message = "Application has been rejected",
                applicationNumber = appNum,
                bank = bank,
                detailedReason = RejectionDetails(
                    primaryReason = "Insufficient Employment History",
                    factors = listOf(
                        "Current employment: 4 months (Required: 12+ months)",
                        "Job changes: 3 in last 2 years",
                        "Income stability concern"
                    ),
                    specificDetails = mapOf(
                        "Employment Duration" to "4 months",
                        "Required Duration" to "12 months",
                        "Job Changes" to "3 in 2 years"
                    )
                ),
                aiInferredReasons = null,
                appliedDate = "2024-10-17",
                lastUpdated = "2024-11-03",
                nextSteps = getNextStepsForRejection(RejectionDetails(
                    primaryReason = "Insufficient Employment History",
                    factors = emptyList(),
                    specificDetails = emptyMap()
                ))
            )
        }
        
        // REJECTED WITH GENERIC REASON (AI INFERENCE)
        if (upperAppNum in listOf("HDFC999999", "SBI99999999", "GENERIC1", "REJECTED4")) {
            val genericReason = GenericRejection(
                message = "Your application does not meet our current lending criteria",
                isVague = true
            )
            val inferredReasons = if (userProfile != null) {
                inferProbableCauses(bank, userProfile)
            } else {
                getDefaultInferredReasons()
            }
            
            return ApplicationResult(
                found = true,
                status = ApplicationStatus.REJECTED,
                message = "Application has been rejected",
                applicationNumber = appNum,
                bank = bank,
                detailedReason = genericReason,
                aiInferredReasons = inferredReasons,
                appliedDate = "2024-10-20",
                lastUpdated = "2024-11-03",
                nextSteps = getNextStepsForInferredReasons(inferredReasons)
            )
        }
        
        // APPROVED
        if (upperAppNum in listOf("ICICI87654321", "APP87654321", "APPROVED1", "HDFC777777")) {
            return ApplicationResult(
                found = true,
                status = ApplicationStatus.APPROVED,
                message = "Congratulations! Your application has been approved",
                applicationNumber = appNum,
                bank = bank,
                detailedReason = ApprovedDetails(
                    approvedAmount = "₹5,00,000",
                    interestRate = "10.5% p.a.",
                    tenure = "60 months",
                    processingFee = "₹2,500"
                ),
                aiInferredReasons = null,
                appliedDate = "2024-10-25",
                lastUpdated = "2024-11-05",
                nextSteps = listOf(
                    "Complete digital KYC verification",
                    "Upload required documents",
                    "Sign loan agreement digitally",
                    "Funds will be disbursed in 48 hours"
                )
            )
        }
        
        // Simulate different bank responses based on random seed
        // Some banks give detailed reasons, others are generic
        
        val randomSeed = appNum.hashCode() % 10
        
        return when {
            // 40% - Rejected with detailed reason
            randomSeed in 0..3 -> {
                val detailedReason = getDetailedRejectionReason(bank, randomSeed)
                ApplicationResult(
                    found = true,
                    status = ApplicationStatus.REJECTED,
                    message = "Application has been rejected",
                    applicationNumber = appNum,
                    bank = bank,
                    detailedReason = detailedReason,
                    aiInferredReasons = null, // No AI inference needed
                    appliedDate = "2024-10-${15 + randomSeed}",
                    lastUpdated = "2024-11-${1 + randomSeed}",
                    nextSteps = getNextStepsForRejection(detailedReason)
                )
            }
            
            // 30% - Rejected with generic reason (AI inference needed)
            randomSeed in 4..6 -> {
                val genericReason = getGenericRejectionReason(bank)
                val inferredReasons = if (userProfile != null) {
                    inferProbableCauses(bank, userProfile)
                } else {
                    getDefaultInferredReasons()
                }
                
                ApplicationResult(
                    found = true,
                    status = ApplicationStatus.REJECTED,
                    message = "Application has been rejected",
                    applicationNumber = appNum,
                    bank = bank,
                    detailedReason = genericReason,
                    aiInferredReasons = inferredReasons,
                    appliedDate = "2024-10-${20 + randomSeed}",
                    lastUpdated = "2024-11-${3 + randomSeed}",
                    nextSteps = getNextStepsForInferredReasons(inferredReasons)
                )
            }
            
            // 20% - Approved
            randomSeed == 7 -> {
                ApplicationResult(
                    found = true,
                    status = ApplicationStatus.APPROVED,
                    message = "Congratulations! Your application has been approved",
                    applicationNumber = appNum,
                    bank = bank,
                    detailedReason = ApprovedDetails(
                        approvedAmount = "₹5,00,000",
                        interestRate = "10.5% p.a.",
                        tenure = "60 months",
                        processingFee = "₹2,500"
                    ),
                    aiInferredReasons = null,
                    appliedDate = "2024-10-25",
                    lastUpdated = "2024-11-05",
                    nextSteps = listOf(
                        "Complete digital KYC verification",
                        "Upload required documents",
                        "Sign loan agreement digitally",
                        "Funds will be disbursed in 48 hours"
                    )
                )
            }
            
            // 10% - Under review/Pending
            else -> {
                ApplicationResult(
                    found = true,
                    status = ApplicationStatus.UNDER_REVIEW,
                    message = "Your application is under review",
                    applicationNumber = appNum,
                    bank = bank,
                    detailedReason = PendingDetails(
                        currentStage = "Credit Assessment",
                        estimatedCompletion = "3-5 business days",
                        pendingItems = listOf("Credit score verification", "Income assessment")
                    ),
                    aiInferredReasons = null,
                    appliedDate = "2024-11-01",
                    lastUpdated = "2024-11-06",
                    nextSteps = listOf(
                        "Keep your phone available for verification call",
                        "Ensure documents are up to date",
                        "Check status in 2-3 days"
                    )
                )
            }
        }
    }
    
    /**
     * Get detailed rejection reason (bank provides specifics)
     */
    private fun getDetailedRejectionReason(bank: Bank, seed: Int): RejectionDetails {
        return when (seed % 4) {
            0 -> RejectionDetails(
                primaryReason = "Credit Score Below Threshold",
                factors = listOf(
                    "Credit score: 620 (Required: 700+)",
                    "3 late payments in last 12 months",
                    "Credit utilization: 78% (High risk)"
                ),
                specificDetails = mapOf(
                    "Credit Score" to "620",
                    "Required Score" to "700",
                    "Late Payments" to "3 in last year"
                )
            )
            1 -> RejectionDetails(
                primaryReason = "High Debt-to-Income Ratio",
                factors = listOf(
                    "Debt-to-income ratio: 52% (Limit: 40%)",
                    "Existing EMI: ₹38,000/month",
                    "Declared income: ₹75,000/month"
                ),
                specificDetails = mapOf(
                    "DTI Ratio" to "52%",
                    "Max Allowed" to "40%",
                    "Existing EMI" to "₹38,000"
                )
            )
            2 -> RejectionDetails(
                primaryReason = "Insufficient Employment History",
                factors = listOf(
                    "Current employment: 4 months (Required: 12+ months)",
                    "Job changes: 3 in last 2 years",
                    "Income stability concern"
                ),
                specificDetails = mapOf(
                    "Employment Duration" to "4 months",
                    "Required Duration" to "12 months",
                    "Job Changes" to "3 in 2 years"
                )
            )
            else -> RejectionDetails(
                primaryReason = "Incomplete Documentation",
                factors = listOf(
                    "Salary slips missing for last 3 months",
                    "Bank statements not updated",
                    "ITR not filed for last year"
                ),
                specificDetails = mapOf(
                    "Missing Documents" to "Salary slips, ITR",
                    "Last Updated" to "6 months ago",
                    "Action Required" to "Submit latest documents"
                )
            )
        }
    }
    
    /**
     * Get generic rejection reason (vague response from bank)
     */
    private fun getGenericRejectionReason(bank: Bank): GenericRejection {
        val genericMessages = listOf(
            "Your application does not meet our current lending criteria",
            "Unable to proceed with your application at this time",
            "Application rejected as per bank's internal policy",
            "Your profile does not match our risk assessment parameters"
        )
        
        return GenericRejection(
            message = genericMessages[(bank.code.hashCode() % genericMessages.size).coerceAtLeast(0)],
            isVague = true
        )
    }
    
    /**
     * AI-powered inference of probable causes using REAL GENERATIVE AI (OpenAI GPT-4o-mini)
     */
    private suspend fun inferProbableCauses(
        bank: Bank,
        profile: CustomerProfile
    ): InferredReasons {
        android.util.Log.d("ApplicationTracker", "🚀 CALLING AI INFERENCE for ${bank.displayName}")
        android.util.Log.d("ApplicationTracker", "Profile: Age=${profile.age}, Location=${profile.locationType}, Digital=${profile.digitalLiteracy}")
        
        return try {
            // Call real AI for inference
            val result = inferProbableCausesWithAI(bank, profile)
            android.util.Log.d("ApplicationTracker", "✅ AI INFERENCE SUCCESS: ${result.causes.size} reasons inferred")
            result
        } catch (e: Exception) {
            android.util.Log.e("ApplicationTracker", "❌ AI INFERENCE FAILED, USING FALLBACK: ${e.message}", e)
            // Fallback to default reasons
            getDefaultInferredReasons()
        }
    }
    
    /**
     * Use OpenAI to infer rejection reasons based on profile analysis
     */
    private suspend fun inferProbableCausesWithAI(
        bank: Bank,
        profile: CustomerProfile
    ): InferredReasons {
        return withContext(Dispatchers.IO) {
            val prompt = """
You are a banking AI analyst. A customer's loan/credit application was rejected by ${bank.displayName} with a generic reason: "Does not meet our current lending criteria."

Customer Profile:
- Age: ${profile.age} years old
- Location Type: ${profile.locationType}
- Digital Literacy: ${profile.digitalLiteracy}
- Language Preference: ${profile.language}

Based on this profile and ${bank.displayName}'s typical lending patterns, infer the 3 most probable rejection reasons.

For each reason, provide:
1. Reason name (concise, 5-8 words)
2. Probability (0-100)
3. Explanation (1-2 sentences in simple language)
4. What it's based on (briefly mention profile factors or industry patterns)

Respond in this EXACT JSON format:
{
  "confidence": "High" or "Moderate" or "Low",
  "causes": [
    {
      "reason": "reason name",
      "probability": 85,
      "explanation": "explanation text",
      "basedOn": "what factors this is based on"
    }
  ]
}

Important:
- Be specific and actionable
- Consider age, location, and digital literacy factors
- Mention ${bank.displayName}'s typical criteria
- Order by probability (highest first)
- Keep explanations customer-friendly
""".trim()

            // Azure OpenAI format: /deployments/{model}/chat/completions?api-version=...
            val azureUrl = "$OPENAI_ENDPOINT/deployments/$OPENAI_MODEL/chat/completions?api-version=$API_VERSION"
            val url = URL(azureUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            android.util.Log.d("ApplicationTracker", "📡 Making Azure OpenAI call to: $url")
            android.util.Log.d("ApplicationTracker", "📡 Model: $OPENAI_MODEL")
            
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("api-key", OPENAI_API_KEY) // Azure auth
                connection.setRequestProperty("x-agent-id", AGENT_ID) // AI Gateway requirement
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                
                // Azure OpenAI format: NO model in body (it's in URL)
                val requestBody = JSONObject().apply {
                    put("messages", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are an expert banking analyst who helps customers understand loan rejection reasons.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                    put("temperature", 0.4)
                    put("max_tokens", 800)
                    put("response_format", JSONObject().put("type", "json_object"))
                }
                
                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray())
                }
                
                val responseCode = connection.responseCode
                android.util.Log.d("ApplicationTracker", "📥 Response code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    android.util.Log.d("ApplicationTracker", "📥 Raw response: ${response.take(300)}...")
                    
                    val jsonResponse = JSONObject(response)
                    val aiContent = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    
                    android.util.Log.d("ApplicationTracker", "✅ Extracted AI content: ${aiContent.take(200)}...")
                    
                    // Parse AI response
                    parseAIInferenceResponse(aiContent, bank)
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    android.util.Log.e("ApplicationTracker", "❌ API Error ($responseCode): $errorBody")
                    throw Exception("API call failed: $responseCode - $errorBody")
                }
            } finally {
                connection.disconnect()
            }
        }
    }
    
    /**
     * Parse OpenAI JSON response into InferredReasons
     */
    private fun parseAIInferenceResponse(aiContent: String, bank: Bank): InferredReasons {
        return try {
            val json = JSONObject(aiContent)
            val confidence = json.optString("confidence", "Moderate")
            val causesArray = json.getJSONArray("causes")
            
            val causes = mutableListOf<ProbableCause>()
            for (i in 0 until causesArray.length()) {
                val causeJson = causesArray.getJSONObject(i)
                causes.add(ProbableCause(
                    reason = causeJson.getString("reason"),
                    probability = causeJson.getInt("probability"),
                    explanation = causeJson.getString("explanation"),
                    basedOn = causeJson.getString("basedOn")
                ))
            }
            
            InferredReasons(
                isAIGenerated = true,
                confidence = confidence,
                causes = causes.sortedByDescending { it.probability },
                disclaimer = "AI-inferred probable causes based on your profile and ${bank.displayName}'s lending patterns. Actual reasons may vary. Contact the bank for specific details."
            )
        } catch (e: Exception) {
            android.util.Log.e("ApplicationTracker", "Failed to parse AI response: ${e.message}")
            throw e
        }
    }
    
    /**
     * Default inferred reasons when no profile available
     */
    private fun getDefaultInferredReasons(): InferredReasons {
        return InferredReasons(
            isAIGenerated = true,
            confidence = "Low to Moderate",
            causes = listOf(
                ProbableCause(
                    reason = "Credit Score Below Threshold",
                    probability = 75,
                    explanation = "Most common reason for rejection. Banks typically require 650-750+ credit score.",
                    basedOn = "Industry data: 40% of rejections"
                ),
                ProbableCause(
                    reason = "High Debt-to-Income Ratio",
                    probability = 60,
                    explanation = "Existing EMI burden exceeding 40% of monthly income.",
                    basedOn = "Industry data: 25% of rejections"
                ),
                ProbableCause(
                    reason = "Insufficient Income Documentation",
                    probability = 50,
                    explanation = "Missing or incomplete salary slips, ITR, or bank statements.",
                    basedOn = "Industry data: 20% of rejections"
                )
            ),
            disclaimer = "These are general probable causes based on industry patterns. Provide your profile for more accurate inference."
        )
    }
    
    /**
     * Get next steps for detailed rejection with personalized advice
     */
    private fun getNextStepsForRejection(reason: Any): List<String> {
        return when (reason) {
            is RejectionDetails -> {
                generatePersonalizedAdvice(reason.primaryReason)
            }
            else -> listOf("Contact bank for next steps")
        }
    }
    
    /**
     * Generate personalized advice with alternatives
     */
    private fun generatePersonalizedAdvice(primaryReason: String): List<String> {
        return when {
            primaryReason.contains("Credit Score", ignoreCase = true) -> listOf(
                "📊 IMMEDIATE ACTION: Download free CIBIL report (www.cibil.com) today",
                "🔍 Check for errors - dispute inaccuracies within 30 days",
                "💳 Reduce credit utilization to below 30% within 2 weeks",
                "⏱️ Pay ALL bills on time for next 6 months (set auto-pay)",
                "",
                "🎯 ALTERNATIVES WHILE YOU IMPROVE:",
                "• Apply for secured credit card (₹5K-50K FD backing)",
                "• Try co-applicant loan with better credit score",
                "• Consider peer-to-peer lending (Faircent, Lendbox)",
                "• Explore NBFC options (more flexible than banks)",
                "",
                "🏦 RECOMMENDED BANKS FOR LOW CREDIT:",
                "• IDFC First Bank (Credit Builder Program)",
                "• IndusInd Bank (accepts 650+ score)",
                "• Kotak 811 (secured card with ₹10K FD)",
                "",
                "⏱️ TIMELINE: 6-12 months to improve score by 50-100 points"
            )
            
            primaryReason.contains("Debt", ignoreCase = true) || 
            primaryReason.contains("Income", ignoreCase = true) -> listOf(
                "📊 IMMEDIATE ACTION: Calculate exact debt-to-income ratio",
                "💰 Pay off smallest loan completely (debt avalanche method)",
                "📉 Consolidate multiple loans into one (lower EMI)",
                "💼 Document all income sources (salary + freelance + rent)",
                "",
                "🎯 ALTERNATIVES WITH HIGH DTI:",
                "• Apply with co-applicant (spouse/parent with income)",
                "• Try secured loan (gold/property/FD)",
                "• Request lower loan amount (better approval odds)",
                "• Consider balance transfer to reduce EMI",
                "",
                "🏦 RECOMMENDED OPTIONS:",
                "• Gold loan (10-12% interest, no income proof needed)",
                "• Loan against FD (instant approval, 1-2% above FD rate)",
                "• Bajaj Finserv (flexible EMI options)",
                "• Tata Capital (accepts higher DTI up to 50%)",
                "",
                "⏱️ TIMELINE: 3-6 months to reduce DTI below 40%"
            )
            
            primaryReason.contains("Employment", ignoreCase = true) -> listOf(
                "📊 IMMEDIATE ACTION: Get employment verification letter",
                "📄 Collect 3-6 months salary slips + bank statements",
                "💼 Show previous employment history (total experience)",
                "🏢 Get HR to confirm job permanency in writing",
                "",
                "🎯 ALTERNATIVES FOR SHORT TENURE:",
                "• Apply with co-applicant having stable job (5+ years)",
                "• Provide Form 16 from previous employer",
                "• Show consistent career progression in same field",
                "• Try NBFC (less strict than banks on tenure)",
                "",
                "🏦 RECOMMENDED OPTIONS:",
                "• Secured loans (gold/FD) - no employment tenure needed",
                "• Bajaj Finance (accepts 6+ months tenure)",
                "• HDB Financial Services (flexible criteria)",
                "• Muthoot Finance (gold loan, instant approval)",
                "",
                "⏱️ TIMELINE: Wait 6-12 months in current job OR use co-applicant now"
            )
            
            primaryReason.contains("Documentation", ignoreCase = true) || 
            primaryReason.contains("Incomplete", ignoreCase = true) -> listOf(
                "📊 IMMEDIATE ACTION: Gather complete documents TODAY",
                "📄 Required: 6 months salary slips + bank statements",
                "🆔 ID Proof: Aadhaar + PAN (ensure address matches)",
                "🏠 Address Proof: Latest utility bill (within 3 months)",
                "💼 Income Proof: Latest Form 16 + ITR (last 2 years)",
                "",
                "🎯 FAST-TRACK REAPPLICATION:",
                "• Upload all docs on bank's mobile app",
                "• Visit branch with complete documents for instant verification",
                "• Use DigiLocker for verified government documents",
                "• Get documents notarized if self-employed",
                "",
                "🏦 DIGITAL-FIRST BANKS (FASTER PROCESSING):",
                "• HDFC Bank (InstaCash digital verification)",
                "• ICICI Bank (instant in-principle approval)",
                "• Kotak 811 (paperless loans through app)",
                "",
                "⏱️ TIMELINE: Reapply within 2-3 days with complete docs"
            )
            
            else -> listOf(
                "📊 IMMEDIATE ACTION: Call bank helpline for exact reason",
                "📧 Email bank requesting detailed rejection report",
                "🔍 Check CIBIL report for any red flags",
                "",
                "🎯 GENERAL ALTERNATIVES:",
                "• Try 3-4 different banks (each has different criteria)",
                "• Consider NBFC (Bajaj, Tata Capital, Fullerton)",
                "• Explore fintech lenders (MoneyTap, EarlySalary)",
                "• Add co-applicant for better approval odds",
                "",
                "🏦 RECOMMENDED NEXT STEPS:",
                "• Visit branch in person for manual underwriting",
                "• Provide additional documents proactively",
                "• Request relationship manager review",
                "",
                "⏱️ TIMELINE: 30 days cooling period before reapplying"
            )
        }
    }
    
    /**
     * Get next steps for AI-inferred rejection with personalized alternatives
     */
    private fun getNextStepsForInferredReasons(inferred: InferredReasons): List<String> {
        val steps = mutableListOf<String>()
        
        steps.add("📞 FIRST: Call bank helpline for exact rejection reason")
        steps.add("")
        
        // Add personalized advice based on inferred causes
        val topCause = inferred.causes.firstOrNull()
        if (topCause != null) {
            steps.add("🎯 MOST LIKELY ISSUE (${topCause.probability}% confidence):")
            steps.add("${topCause.reason}")
            steps.add("")
            
            when {
                topCause.reason.contains("Credit", ignoreCase = true) -> {
                    steps.add("📊 IMMEDIATE ACTIONS:")
                    steps.add("• Download free CIBIL report today")
                    steps.add("• Pay all bills on time for 6 months")
                    steps.add("• Reduce credit card usage to 30%")
                    steps.add("")
                    steps.add("🎯 ALTERNATIVES:")
                    steps.add("• Apply for secured credit card")
                    steps.add("• Try NBFC with lower credit requirements")
                    steps.add("• Add co-applicant with good credit")
                }
                
                topCause.reason.contains("Location", ignoreCase = true) || 
                topCause.reason.contains("Rural", ignoreCase = true) -> {
                    steps.add("🏦 RECOMMENDED ACTIONS:")
                    steps.add("• Visit nearest branch for in-person review")
                    steps.add("• Provide local references (2-3 people)")
                    steps.add("• Submit land ownership/property papers")
                    steps.add("")
                    steps.add("🎯 RURAL-FRIENDLY OPTIONS:")
                    steps.add("• Regional Rural Banks (easier approval)")
                    steps.add("• Pradhan Mantri Mudra Yojana")
                    steps.add("• Cooperative banks in your district")
                    steps.add("• Gold loan (no location bias)")
                }
                
                topCause.reason.contains("Age", ignoreCase = true) -> {
                    steps.add("🏦 RECOMMENDED ACTIONS:")
                    steps.add("• Apply with co-applicant (younger family member)")
                    steps.add("• Show stable income/pension documents")
                    steps.add("• Provide property ownership proof")
                    steps.add("")
                    steps.add("🎯 AGE-NEUTRAL OPTIONS:")
                    steps.add("• Secured loans (gold/property/FD)")
                    steps.add("• Senior citizen special schemes")
                    steps.add("• Reverse mortgage (if 60+ with property)")
                }
                
                topCause.reason.contains("Digital", ignoreCase = true) -> {
                    steps.add("💳 IMMEDIATE ACTIONS:")
                    steps.add("• Start using UPI daily (build history)")
                    steps.add("• Enable online banking and use regularly")
                    steps.add("• Make 10+ digital transactions/month")
                    steps.add("")
                    steps.add("🎯 NON-DIGITAL ALTERNATIVES:")
                    steps.add("• Visit branch for offline application")
                    steps.add("• Try public sector banks (less digital focus)")
                    steps.add("• Gold loan (minimal digital requirement)")
                }
                
                else -> {
                    steps.add("🎯 GENERAL ALTERNATIVES:")
                    steps.add("• Try 3-4 different banks")
                    steps.add("• Consider NBFC options")
                    steps.add("• Add co-applicant")
                    steps.add("• Try smaller loan amount")
                }
            }
            
            steps.add("")
            steps.add("🏦 RECOMMENDED BANKS TO TRY:")
            steps.add("• IDFC First Bank (flexible criteria)")
            steps.add("• IndusInd Bank (quick approvals)")
            steps.add("• Bajaj Finserv (NBFC, lenient)")
            steps.add("• Local cooperative banks")
        }
        
        steps.add("")
        steps.add("⏱️ Wait 30 days before reapplying to same bank")
        
        return steps
    }
}

/**
 * Application tracking result
 */
data class ApplicationResult(
    val found: Boolean,
    val status: ApplicationTracker.ApplicationStatus,
    val message: String,
    val applicationNumber: String,
    val bank: ApplicationTracker.Bank,
    val detailedReason: Any?, // Can be RejectionDetails, ApprovedDetails, PendingDetails, or GenericRejection
    val aiInferredReasons: InferredReasons?,
    val appliedDate: String?,
    val lastUpdated: String?,
    val nextSteps: List<String>?
)

/**
 * Detailed rejection with specific factors
 */
data class RejectionDetails(
    val primaryReason: String,
    val factors: List<String>,
    val specificDetails: Map<String, String>
)

/**
 * Generic rejection (vague response)
 */
data class GenericRejection(
    val message: String,
    val isVague: Boolean
)

/**
 * Approved application details
 */
data class ApprovedDetails(
    val approvedAmount: String,
    val interestRate: String,
    val tenure: String,
    val processingFee: String
)

/**
 * Pending application details
 */
data class PendingDetails(
    val currentStage: String,
    val estimatedCompletion: String,
    val pendingItems: List<String>
)

/**
 * AI-inferred probable causes
 */
data class InferredReasons(
    val isAIGenerated: Boolean,
    val confidence: String,
    val causes: List<ProbableCause>,
    val disclaimer: String
)

/**
 * Individual probable cause
 */
data class ProbableCause(
    val reason: String,
    val probability: Int, // 0-100
    val explanation: String,
    val basedOn: String // What data/pattern this is based on
)

