package com.lumeai.banking.ui

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lumeai.banking.DecisionManager
import com.lumeai.banking.FirebaseListenerService
import com.lumeai.banking.FirebaseSyncManager
import com.lumeai.banking.UserStats
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * MyAIProfileActivity - Shows how AI sees the customer
 * Part of "Explain My Profile" feature for hackathon
 * FULLY AI-GENERATED using Azure OpenAI (GPT-4o-mini)
 * Integrates ALL user data from entire app
 */
class MyAIProfileActivity : AppCompatActivity() {

    private var currentLanguage = "en"
    private var profileScore = 0
    private var riskLevel = "Medium"
    private var creditWorthiness = "Good"
    
    // AI-GENERATED content (NO static text!)
    private var aiIntroMessage = ""
    private var aiScoreExplanation = ""
    private var strengths = mutableListOf<String>()
    private var improvements = mutableListOf<String>()
    private var dataPoints = mutableListOf<Triple<String, String, Boolean>>()
    private var impactPredictions = mutableListOf<Pair<String, String>>()
    
    // Comprehensive user data
    private var allDecisions: List<com.lumeai.banking.models.FirebaseDecision> = emptyList()
    private var userStats: UserStats? = null
    
    // Azure OpenAI Configuration
    private val OPENAI_API_KEY = "zzzzzzzz"
    private val OPENAI_ENDPOINT = "https://api.hack.lume.services.io/openai/v1"
    private val OPENAI_MODEL = "gpt-4o-mini"
    private val AGENT_ID = "zzzzzzzz"
    private val API_VERSION = "2024-08-01-preview"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        
        // Blue status bar - same as all other pages
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        setContentView(createLoadingUI())
        
        // Load profile data and generate AI content
        loadAndGenerateProfile()
    }

    private fun createLoadingUI(): FrameLayout {
        return FrameLayout(this).apply {
            setBackgroundColor(0xFFF5F7FA.toInt())
            
            addView(LinearLayout(this@MyAIProfileActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                
                addView(ProgressBar(this@MyAIProfileActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(50), dp(50))
                })
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "🤖 AI आपकी प्रोफ़ाइल का विश्लेषण कर रहा है..."
                        "te" -> "🤖 AI మీ ప్రొఫైల్ విశ్లేషిస్తోంది..."
                        else -> "🤖 AI is analyzing your profile..."
                    }
                    textSize = 16f
                    setTextColor(0xFF64748B.toInt())
                    setPadding(0, dp(20), 0, 0)
                    gravity = Gravity.CENTER
                })
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "कृपया प्रतीक्षा करें..."
                        "te" -> "దయచేసి వేచి ఉండండి..."
                        else -> "Please wait..."
                    }
                    textSize = 14f
                    setTextColor(0xFF94A3B8.toInt())
                    setPadding(0, dp(8), 0, 0)
                    gravity = Gravity.CENTER
                })
            })
        }
    }

    private fun loadAndGenerateProfile() {
        lifecycleScope.launch {
            try {
                // Load ALL user data
                val decisions = withContext(Dispatchers.IO) {
                    DecisionManager.getAllDecisions(this@MyAIProfileActivity)
                }
                
                allDecisions = decisions
                
                // Calculate user stats from decisions
                userStats = calculateUserStats(decisions)
                
                // Calculate basic metrics
                calculateProfileScore(decisions)
                
                // 🤖 GENERATE ALL CONTENT USING AI with COMPREHENSIVE data
                android.util.Log.d("MyAIProfile", "🚀 Calling AI with comprehensive user data...")
                generateAIProfileContent(decisions)
                
                // Update UI with AI-generated content
                withContext(Dispatchers.Main) {
                    setContentView(createUI())
                }
            } catch (e: Exception) {
                android.util.Log.e("MyAIProfile", "❌ Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showErrorUI(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    private fun calculateUserStats(decisions: List<com.lumeai.banking.models.FirebaseDecision>): UserStats {
        return UserStats(
            totalDecisions = decisions.size,
            approvedCount = decisions.count { it.outcome.lowercase() == "approved" },
            deniedCount = decisions.count { it.outcome.lowercase() == "denied" },
            pendingCount = decisions.count { it.outcome.lowercase() == "pending" },
            biasDetectedCount = decisions.count { it.biasDetected },
            highRiskBiasCount = decisions.count { it.biasDetected && it.biasSeverity == "HIGH" },
            banksCount = decisions.map { it.bankName }.distinct().size,
            loanTypesCount = decisions.map { it.loanType }.filter { it.isNotEmpty() }.distinct().size
        )
    }

    /**
     * 🤖 GENERATE ALL PROFILE CONTENT USING AI (NO STATIC TEXT!)
     */
    private suspend fun generateAIProfileContent(decisions: List<com.lumeai.banking.models.FirebaseDecision>) = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("MyAIProfile", "🤖 Generating AI profile content...")
            
            // Build comprehensive prompt for AI
            val prompt = buildProfilePrompt(decisions)
            
            // Call Azure OpenAI
            val aiResponse = callAzureOpenAI(prompt)
            
            // Parse AI response
            parseAIResponse(aiResponse)
            
            android.util.Log.d("MyAIProfile", "✅ AI profile generated successfully!")
        } catch (e: Exception) {
            android.util.Log.e("MyAIProfile", "❌ AI generation failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Build comprehensive prompt for AI to analyze user profile
     * Integrates ALL available data from the app
     */
    private fun buildProfilePrompt(decisions: List<com.lumeai.banking.models.FirebaseDecision>): String {
        val languageName = when (currentLanguage) {
            "hi" -> "Hindi"
            "te" -> "Telugu"
            else -> "English"
        }
        
        // Build comprehensive decision summary with ALL details
        val decisionSummary = decisions.mapIndexed { index, d ->
            val biasInfo = if (d.biasDetected) {
                "\n  ⚠️ BIAS DETECTED: ${d.biasSeverity} severity - ${d.biasMessage}"
            } else ""
            
            val summaryInfo = if (d.summaryEnglish.isNotEmpty()) {
                "\n  AI Explanation: ${d.summaryEnglish.take(200)}..."
            } else ""
            
            """
Decision #${index + 1}:
  Outcome: ${d.outcome}
  Type: ${d.loanType} - Amount: ₹${d.loanAmount}
  Bank: ${d.bankName}
  Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(d.timestamp))}
  
  Factors:
  - Credit Score: ${d.creditScore} (required: ${d.creditScoreRequired}) ${if (d.creditScorePassed) "✓ PASSED" else "✗ FAILED"}
  - Income: ₹${d.income}/month (required: ₹${d.incomeRequired}) ${if (d.incomePassed) "✓ PASSED" else "✗ FAILED"}
  - Debt Ratio: ${String.format("%.1f%%", d.debtRatio * 100)} (max: ${String.format("%.1f%%", d.debtRatioRequired * 100)}) ${if (d.debtRatioPassed) "✓ PASSED" else "✗ FAILED"}
  - Employment: ${d.employmentMonths} months (required: ${d.employmentMonthsRequired}) ${if (d.employmentPassed) "✓ PASSED" else "✗ FAILED"}
  - Digital Footprint: ${d.digitalFootprint} (required: ${d.digitalFootprintRequired}) ${if (d.digitalFootprintPassed) "✓ PASSED" else "✗ FAILED"}
  
  Customer Profile:
  - Age: ${d.age}
  - Location: ${d.locationType}
  - Digital Literacy: ${d.digitalLiteracy}$biasInfo$summaryInfo
            """.trimIndent()
        }.joinToString("\n\n")
        
        // User stats summary
        val statsInfo = userStats?.let { stats ->
            """
            
**User Statistics:**
- Total Decisions: ${stats.totalDecisions}
- Approvals: ${stats.approvedCount}
- Denials: ${stats.deniedCount}
- Pending: ${stats.pendingCount}
- Approval Rate: ${String.format("%.1f%%", stats.approvalRate)}
- Bias Incidents: ${stats.biasDetectedCount}
- Banks Interacted With: ${stats.banksCount}
            """.trimIndent()
        } ?: ""
        
        // Calculate comprehensive metrics
        val approvedCount = decisions.count { it.outcome.lowercase() == "approved" }
        val deniedCount = decisions.count { it.outcome.lowercase() == "denied" }
        val biasDetectedCount = decisions.count { it.biasDetected }
        val avgCreditScore = decisions.filter { it.creditScore > 0 }.map { it.creditScore }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgIncome = decisions.filter { it.income > 0 }.map { it.income }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgDebtRatio = decisions.filter { it.debtRatio > 0 }.map { it.debtRatio }.average().takeIf { !it.isNaN() } ?: 0.0
        
        return """
You are an expert AI banking analyst. Analyze this customer's COMPLETE banking profile using ALL available data and provide personalized insights in $languageName.

**COMPREHENSIVE CUSTOMER PROFILE:**
- Total Loan Applications: ${decisions.size}
- Approved: $approvedCount
- Denied: $deniedCount
- AI Trust Score: $profileScore/100
- Risk Level: $riskLevel
- Credit Rating: $creditWorthiness

**AGGREGATED FINANCIAL METRICS:**
- Average Credit Score: ${String.format("%.0f", avgCreditScore)}
- Average Monthly Income: ₹${String.format("%.0f", avgIncome)}
- Average Debt-to-Income Ratio: ${String.format("%.1f%%", avgDebtRatio * 100)}
- Bias Incidents Detected: $biasDetectedCount

$statsInfo

**DETAILED DECISION HISTORY:**
$decisionSummary

**TASK:**
Generate a comprehensive, personalized AI profile analysis in $languageName based on ALL the data above. Respond ONLY with valid JSON in this exact format:

{
  "intro_message": "A warm, personalized introduction explaining what this profile means (2-3 sentences in $languageName). Reference specific numbers and trends from their actual history.",
  "score_explanation": "Brief explanation of why the score is $profileScore/100 (1-2 sentences in $languageName). Mention specific factors from their history.",
  "strengths": [
    "Specific strength with actual numbers (e.g., 'Credit score of XXX, which is YY points above the minimum requirement') in $languageName",
    "Another strength with concrete data from their history in $languageName",
    "3-4 total strengths, all data-driven in $languageName"
  ],
  "improvements": [
    "Specific improvement area with actionable advice (e.g., 'Reduce debt ratio from X% to Y% to qualify for better rates') in $languageName",
    "Another concrete improvement with numbers in $languageName",
    "2-3 improvements if needed, leave empty array if profile is excellent in $languageName"
  ],
  "data_points": [
    {"name": "Label in $languageName", "value": "Actual value from data", "is_positive": true/false},
    {"name": "Total Applications in $languageName", "value": "${decisions.size}", "is_positive": ${decisions.size > 0}},
    {"name": "Approval Rate in $languageName", "value": "${if (decisions.isNotEmpty()) String.format("%.0f%%", (approvedCount.toFloat() / decisions.size * 100)) else "0%"}", "is_positive": ${approvedCount > deniedCount}},
    {"name": "Average Credit Score in $languageName", "value": "${String.format("%.0f", avgCreditScore)}", "is_positive": ${avgCreditScore >= 650}},
    {"name": "Bias Detection Status in $languageName", "value": "${if (biasDetectedCount == 0) "Clean - No bias detected" else "$biasDetectedCount incident(s) detected"} in $languageName", "is_positive": ${biasDetectedCount == 0}}
  ],
  "impact_predictions": [
    {"label": "Loan Approval Likelihood in $languageName", "value": "High/Medium/Low with specific reasoning based on their history in $languageName"},
    {"label": "Expected Interest Rate Range in $languageName", "value": "X.X% - Y.Y% with explanation why in $languageName"},
    {"label": "Maximum Credit Limit in $languageName", "value": "₹X,XX,XXX with reasoning based on income/debt ratio in $languageName"}
  ]
}

**CRITICAL GUIDELINES:**
1. Use ACTUAL NUMBERS from the decision history - be specific!
2. Reference real trends (e.g., "improved from decision #1 to #3")
3. If bias was detected, mention it in improvements
4. Consider ALL factors: credit, income, debt, employment, location, digital literacy
5. Be empathetic but honest about both strengths and weaknesses
6. Provide actionable, specific advice (not generic)
7. If approved > denied, focus on maintaining momentum
8. If denied > approved, focus on concrete steps to improve
9. ALL text must be in $languageName (not translated, but originally generated)
10. Make predictions realistic based on their ACTUAL performance history

Respond ONLY with the JSON object, nothing else.
        """.trimIndent()
    }

    /**
     * Call Azure OpenAI API
     */
    private suspend fun callAzureOpenAI(prompt: String): String = withContext(Dispatchers.IO) {
        // Azure OpenAI format: /deployments/{model}/chat/completions?api-version=...
        val azureUrl = "$OPENAI_ENDPOINT/deployments/$OPENAI_MODEL/chat/completions?api-version=$API_VERSION"
        val url = URL(azureUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        android.util.Log.d("MyAIProfile", "📡 Calling Azure OpenAI at: $azureUrl")
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("api-key", OPENAI_API_KEY)
            connection.setRequestProperty("x-agent-id", AGENT_ID)
            connection.doOutput = true
            connection.connectTimeout = 120000
            connection.readTimeout = 120000
            
            val requestBody = JSONObject().apply {
                put("messages", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are an expert AI banking analyst providing personalized profile insights. Always respond with valid JSON.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 2500)
                put("response_format", JSONObject().put("type", "json_object"))
            }
            
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("API Error: $responseCode - $errorStream")
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Parse AI response and populate fields
     */
    private fun parseAIResponse(aiContent: String) {
        try {
            val json = JSONObject(aiContent)
            
            // Extract AI-generated content
            aiIntroMessage = json.optString("intro_message", "")
            aiScoreExplanation = json.optString("score_explanation", "")
            
            // Extract strengths
            strengths.clear()
            val strengthsArray = json.optJSONArray("strengths")
            if (strengthsArray != null) {
                for (i in 0 until strengthsArray.length()) {
                    strengths.add(strengthsArray.getString(i))
                }
            }
            
            // Extract improvements
            improvements.clear()
            val improvementsArray = json.optJSONArray("improvements")
            if (improvementsArray != null) {
                for (i in 0 until improvementsArray.length()) {
                    improvements.add(improvementsArray.getString(i))
                }
            }
            
            // Extract data points
            dataPoints.clear()
            val dataPointsArray = json.optJSONArray("data_points")
            if (dataPointsArray != null) {
                for (i in 0 until dataPointsArray.length()) {
                    val dp = dataPointsArray.getJSONObject(i)
                    dataPoints.add(Triple(
                        dp.getString("name"),
                        dp.getString("value"),
                        dp.getBoolean("is_positive")
                    ))
                }
            }
            
            // Extract impact predictions
            impactPredictions.clear()
            val predictionsArray = json.optJSONArray("impact_predictions")
            if (predictionsArray != null) {
                for (i in 0 until predictionsArray.length()) {
                    val pred = predictionsArray.getJSONObject(i)
                    impactPredictions.add(Pair(
                        pred.getString("label"),
                        pred.getString("value")
                    ))
                }
            }
            
            android.util.Log.d("MyAIProfile", "✅ Parsed AI content: intro=${aiIntroMessage.take(50)}...")
        } catch (e: Exception) {
            android.util.Log.e("MyAIProfile", "❌ Failed to parse AI response: ${e.message}", e)
            throw Exception("Failed to parse AI response: ${e.message}")
        }
    }
    
    /**
     * Calculate basic profile metrics (score calculation only, content generated by AI)
     */
    private fun calculateProfileScore(decisions: List<com.lumeai.banking.models.FirebaseDecision>) {
        var score = 50 // Base score
        var approvedCount = 0
        var deniedCount = 0
        
        decisions.forEach { decision ->
            when (decision.outcome.lowercase()) {
                "approved" -> {
                    approvedCount++
                    score += 10
                }
                "denied" -> {
                    deniedCount++
                    score -= 5
                }
            }
        }
        
        // Cap score between 0-100
        profileScore = score.coerceIn(0, 100)
        
        // Determine risk level
        riskLevel = when {
            profileScore >= 75 -> "Low"
            profileScore >= 50 -> "Medium"
            else -> "High"
        }
        
        // Determine credit worthiness
        creditWorthiness = when {
            profileScore >= 80 -> "Excellent"
            profileScore >= 65 -> "Good"
            profileScore >= 50 -> "Fair"
            else -> "Needs Improvement"
        }
        
        // NOTE: strengths, improvements, data points, and predictions are ALL generated by AI
        // No static content here!
    }
    
    private fun showErrorUI(error: String) {
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(0xFFF5F7FA.toInt())
            
            addView(LinearLayout(this@MyAIProfileActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(40), dp(40), dp(40), dp(40))
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = "⚠️"
                    textSize = 48f
                    gravity = Gravity.CENTER
                })
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "AI प्रोफ़ाइल लोड करने में विफल"
                        "te" -> "AI ప్రొఫైల్ లోడ్ చేయడంలో విఫలమైంది"
                        else -> "Failed to load AI profile"
                    }
                    textSize = 18f
                    setTextColor(0xFF0A0A0A.toInt())
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    setPadding(0, dp(20), 0, dp(8))
                })
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = error
                    textSize = 14f
                    setTextColor(0xFF64748B.toInt())
                    gravity = Gravity.CENTER
                })
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "पुनः प्रयास करें"
                        "te" -> "మళ్లీ ప్రయత్నించండి"
                        else -> "Retry"
                    }
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    setPadding(dp(24), dp(12), dp(24), dp(12))
                    isClickable = true
                    isFocusable = true
                    
                    val shape = GradientDrawable().apply {
                        cornerRadius = dp(12).toFloat()
                        setColor(AppTheme.Primary.Blue)
                    }
                    background = shape
                    
                    setOnClickListener {
                        finish()
                    }
                    
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = dp(20)
                    layoutParams = params
                })
            })
        })
    }

    private fun createUI(): FrameLayout {
        // CONSISTENT UI: FrameLayout with sticky header like other pages
        val mainContainer = FrameLayout(this)
        mainContainer.setBackgroundColor(AppTheme.Background.Primary)
        
        // Scrollable content
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(AppTheme.Background.Primary)
        
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
            // Compact top padding (same as other pages)
            setPadding(0, dp(120), 0, 0)
        }
        
        // Main Content with padding
        val mainContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(16))  // Reduced top padding
        }
        
        // Intro Message
        mainContent.addView(createIntroCard())
        addSpace(mainContent, 12)  // Reduced spacing
        
        // Profile Score Card
        mainContent.addView(createProfileScoreCard())
        addSpace(mainContent, 12)
        
        // Strengths Section
        mainContent.addView(createStrengthsCard())
        addSpace(mainContent, 12)
        
        // Improvements Section (only if there are improvements)
        if (improvements.isNotEmpty()) {
            mainContent.addView(createImprovementsCard())
            addSpace(mainContent, 12)
        }
        
        // Data Points Section
        mainContent.addView(createDataPointsCard())
        addSpace(mainContent, 12)
        
        // Impact Prediction
        mainContent.addView(createImpactCard())
        addSpace(mainContent, 12)
        
        // Action Buttons
        mainContent.addView(createActionButtons())
        addSpace(mainContent, 30)
        
        contentLayout.addView(mainContent)
        scrollView.addView(contentLayout)
        
        // Add scrollView first (background) - MATCH_PARENT
        val scrollParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollParams
        mainContainer.addView(scrollView)
        
        // Create sticky header combining header + language bar
        val stickyHeader = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            
            addView(createHeader())
            addView(createLanguageBar())
        }
        
        // Add sticky header on top (foreground) - WRAP_CONTENT height
        val headerParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        stickyHeader.layoutParams = headerParams
        mainContainer.addView(stickyHeader)
        
        return mainContainer
    }

    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(16), dp(10), dp(16), dp(10))  // Compact like other pages
            gravity = Gravity.CENTER_VERTICAL
            
            // Modern back button - compact
            addView(TextView(this@MyAIProfileActivity).apply {
                text = "←"
                textSize = 24f  // Smaller
                setTextColor(Color.WHITE)
                setPadding(dp(4), dp(4), dp(4), dp(4))
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply {  // Smaller
                    rightMargin = dp(8)
                }
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                
                // Add ripple effect on touch
                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                setBackgroundResource(outValue.resourceId)
                
                setOnClickListener { finish() }
            })
            
            // Title - compact
            addView(TextView(this@MyAIProfileActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "मेरी प्रोफ़ाइल समझाएं"
                    "te" -> "నా ప్రొఫైల్‌ను వివరించండి"
                    else -> "Explain My Profile"
                }
                textSize = 18f  // Same as other pages
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
        }
    }

    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.WHITE)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            gravity = Gravity.END  // Align to right side
            elevation = dp(2).toFloat()
            
            addView(createLanguageButton("English", "en"))
            addView(Space(this@MyAIProfileActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 0)
            })
            addView(createLanguageButton("हिंदी", "hi"))
            addView(Space(this@MyAIProfileActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 0)
            })
            addView(createLanguageButton("తెలుగు", "te"))
        }
    }

    private fun createLanguageButton(name: String, code: String): TextView {
        return TextView(this).apply {
            text = name
            textSize = 13f
            setPadding(dp(16), dp(8), dp(16), dp(8))
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            
            val isSelected = currentLanguage == code
            val shape = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)  // Same as other pages
                } else {
                    setColor(Color.WHITE)
                    setStroke(dp(1), AppTheme.Text.OnCardSecondary)  // Same border
                }
            }
            background = shape
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
                        .edit()
                        .putString("language", code)
                        .apply()
                    recreate()
                }
            }
        }
    }

    private fun createIntroCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#E3F2FD"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), Color.parseColor("#2196F3"))
            }
            background = shape
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "💡 AI सारांश"
                    "te" -> "💡 AI సారాంశం"
                    else -> "💡 AI Summary"
                }
                textSize = 16f
                setTextColor(Color.parseColor("#1976D2"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(8))
            })
            
            // AI-GENERATED intro message
            if (aiIntroMessage.isNotEmpty()) {
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = aiIntroMessage
                    textSize = 14f
                    setTextColor(Color.parseColor("#424242"))
                    setLineSpacing(0f, 1.4f)
                })
            }
        }
    }

    private fun createProfileScoreCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            // Title
            addView(TextView(this@MyAIProfileActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "📊 आपका AI ट्रस्ट स्कोर"
                    "te" -> "📊 మీ AI విశ్వాస స్కోర్"
                    else -> "📊 Your AI Trust Score"
                }
                textSize = 19f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            // Score Circle
            addView(createScoreVisual())
            
            // Score Details
            addView(LinearLayout(this@MyAIProfileActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(0, dp(16), 0, 0)
                
                addView(createScoreDetailItem(
                    when (currentLanguage) {
                        "hi" -> "जोखिम स्तर"
                        "te" -> "రిస్క్ స్థాయి"
                        else -> "Risk Level"
                    },
                    riskLevel,
                    getRiskColor()
                ))
                
                addView(Space(this@MyAIProfileActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(20), 0)
                })
                
                addView(createScoreDetailItem(
                    when (currentLanguage) {
                        "hi" -> "क्रेडिट योग्यता"
                        "te" -> "క్రెడిట్ యోగ్యత"
                        else -> "Credit Rating"
                    },
                    creditWorthiness,
                    getScoreColor()
                ))
            })
        }
    }

    private fun createScoreVisual(): FrameLayout {
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(150)  // Reduced height to prevent overlap
            )
            
            // Score text (centered, above the bar)
            addView(LinearLayout(this@MyAIProfileActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                ).apply {
                    bottomMargin = dp(40)  // Push it up from center
                }
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = "$profileScore"
                    textSize = 48f  // Slightly smaller
                    setTextColor(getScoreColor())
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                })
                
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "100 में से"
                        "te" -> "100లో"
                        else -> "out of 100"
                    }
                    textSize = 13f
                    setTextColor(Color.parseColor("#64748B"))
                    gravity = Gravity.CENTER
                })
            })
            
            // Progress bar at bottom
            addView(ProgressBar(this@MyAIProfileActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    dp(20),
                    Gravity.BOTTOM
                ).apply {
                    marginStart = dp(40)
                    marginEnd = dp(40)
                    bottomMargin = dp(20)
                }
                max = 100
                progress = profileScore
                progressDrawable = GradientDrawable().apply {
                    cornerRadius = dp(10).toFloat()
                    setColor(getScoreColor())
                }
            })
        }
    }

    private fun createScoreDetailItem(label: String, value: String, color: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = value
                textSize = 18f
                setTextColor(color)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = label
                textSize = 12f
                setTextColor(0xFF64748B.toInt())
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, 0)
            })
        }
    }

    private fun createStrengthsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "💪 आपकी शक्तियाँ"
                    "te" -> "💪 మీ బలాలు"
                    else -> "💪 Your Strengths"
                }
                textSize = 19f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            if (strengths.isEmpty()) {
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "अधिक निर्णय डेटा की प्रतीक्षा में..."
                        "te" -> "మరిన్ని నిర్ణయ డేటా కోసం వేచి ఉంది..."
                        else -> "Building your profile with more decision data..."
                    }
                    textSize = 14f
                    setTextColor(Color.parseColor("#64748B"))
                    setLineSpacing(0f, 1.5f)
                })
            } else {
                strengths.forEach { strength ->
                    addView(createBulletItem(strength, Color.parseColor("#10B981")))
                }
            }
        }
    }

    private fun createImprovementsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "⚠️ सुधार के क्षेत्र"
                    "te" -> "⚠️ మెరుగుదల ప్రాంతాలు"
                    else -> "⚠️ Areas to Improve"
                }
                textSize = 19f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            if (improvements.isEmpty()) {
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "बढ़िया! कोई प्रमुख सुधार क्षेत्र नहीं मिला।"
                        "te" -> "గొప్పగా! ప్రధాన మెరుగుదల ప్రాంతాలు కనుగొనబడలేదు।"
                        else -> "Great! No major improvement areas found."
                    }
                    textSize = 14f
                    setTextColor(Color.parseColor("#10B981"))
                    setTypeface(null, Typeface.BOLD)
                })
            } else {
                improvements.forEach { improvement ->
                    addView(createBulletItem(improvement, Color.parseColor("#F59E0B")))
                }
            }
        }
    }

    private fun createDataPointsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "📋 आपकी प्रोफ़ाइल को प्रभावित करने वाले डेटा"
                    "te" -> "📋 మీ ప్రొఫైల్‌ను ప్రభావితం చేసే డేటా"
                    else -> "📋 Data Influencing Your Profile"
                }
                textSize = 19f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            dataPoints.forEach { (name, value, isPositive) ->
                addView(createDataPointItem(name, value, isPositive))
            }
        }
    }

    private fun createDataPointItem(name: String, value: String, isPositive: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = if (isPositive) "✓" else "⚠"
                textSize = 18f
                setTextColor(if (isPositive) 0xFF10B981.toInt() else 0xFFF59E0B.toInt())
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(dp(30), ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = name
                textSize = 14f
                setTextColor(0xFF64748B.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = value
                textSize = 14f
                setTextColor(0xFF0A0A0A.toInt())
                setTypeface(null, Typeface.BOLD)
            })
        }
    }

    private fun createImpactCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#FFF3E0"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), Color.parseColor("#FF9800"))
            }
            background = shape
            
            addView(TextView(this@MyAIProfileActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "🎯 यह आपको कैसे प्रभावित करता है"
                    "te" -> "🎯 ఇది మిమ్మల్ని ఎలా ప్రభావితం చేస్తుంది"
                    else -> "🎯 How This Affects You"
                }
                textSize = 16f
                setTextColor(Color.parseColor("#E65100"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(12))
            })
            
            // AI-GENERATED predictions (NO static content!)
            if (impactPredictions.isNotEmpty()) {
                impactPredictions.forEach { (label, value) ->
                    addView(LinearLayout(this@MyAIProfileActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, dp(6), 0, dp(6))
                        
                        addView(TextView(this@MyAIProfileActivity).apply {
                            text = "• $label: "
                            textSize = 14f
                            setTextColor(Color.parseColor("#424242"))
                        })
                        
                        addView(TextView(this@MyAIProfileActivity).apply {
                            text = value
                            textSize = 14f
                            setTextColor(Color.parseColor("#424242"))
                            setTypeface(null, Typeface.BOLD)
                        })
                    })
                }
            } else {
                addView(TextView(this@MyAIProfileActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "AI आपके प्रभाव का विश्लेषण कर रहा है..."
                        "te" -> "AI మీ ప్రభావాన్ని విశ్లేషిస్తోంది..."
                        else -> "AI is analyzing your impact..."
                    }
                    textSize = 14f
                    setTextColor(Color.parseColor("#E65100"))
                    gravity = Gravity.CENTER
                })
            }
        }
    }

    private fun createActionButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            
            // Request Review Button
            addView(createActionButton(
                when (currentLanguage) {
                    "hi" -> "📝 प्रोफ़ाइल समीक्षा का अनुरोध करें"
                    "te" -> "📝 ప్రొఫైల్ సమీక్ష కోసం అభ్యర్థించండి"
                    else -> "📝 Request Profile Review"
                },
                AppTheme.Primary.Blue
            ) {
                showRequestReviewDialog()
            })
            
            addView(Space(this@MyAIProfileActivity).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(12))
            })
            
            // View Data Sources Button
            addView(createActionButton(
                when (currentLanguage) {
                    "hi" -> "🔍 पूर्ण डेटा स्रोत देखें"
                    "te" -> "🔍 పూర్తి డేటా మూలాలు చూడండి"
                    else -> "🔍 View Full Data Sources"
                },
                0xFF6B7280.toInt()
            ) {
                showDataSourcesDialog()
            })
        }
    }

    private fun createActionButton(text: String, color: Int, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(16), dp(20), dp(16))
            isClickable = true
            isFocusable = true
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(color)
            }
            background = shape
            elevation = dp(2).toFloat()
            
            setOnClickListener { onClick() }
        }
    }

    private fun createBulletItem(text: String, color: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(6), 0, dp(6))
            
            addView(TextView(this@MyAIProfileActivity).apply {
                this.text = "✓"
                textSize = 16f
                setTextColor(color)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(dp(30), ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            
            addView(TextView(this@MyAIProfileActivity).apply {
                this.text = text
                textSize = 14f
                setTextColor(0xFF334155.toInt())
                setLineSpacing(0f, 1.5f)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
        }
    }

    private fun showRequestReviewDialog() {
        AlertDialog.Builder(this)
            .setTitle(when (currentLanguage) {
                "hi" -> "प्रोफ़ाइल समीक्षा अनुरोध"
                "te" -> "ప్రొఫైల్ సమీక్ష అభ్యర్థన"
                else -> "Request Profile Review"
            })
            .setMessage(when (currentLanguage) {
                "hi" -> "यदि आपको लगता है कि आपकी AI प्रोफ़ाइल में कोई त्रुटि है, तो आप मानव समीक्षा का अनुरोध कर सकते हैं। बैंक 2-3 कार्य दिवसों में प्रतिक्रिया देगा।"
                "te" -> "మీ AI ప్రొఫైల్‌లో ఏదైనా లోపం ఉందని మీరు భావిస్తే, మీరు మానవ సమీక్ష కోసం అభ్యర్థించవచ్చు. బ్యాంక్ 2-3 పని దినాల్లో స్పందిస్తుంది।"
                else -> "If you believe there's an error in your AI profile, you can request a human review. The bank will respond within 2-3 business days."
            })
            .setPositiveButton(when (currentLanguage) {
                "hi" -> "अनुरोध भेजें"
                "te" -> "అభ్యర్థన పంపండి"
                else -> "Send Request"
            }) { _, _ ->
                Toast.makeText(
                    this,
                    when (currentLanguage) {
                        "hi" -> "समीक्षा अनुरोध भेजा गया!"
                        "te" -> "సమీక్ష అభ్యర్థన పంపబడింది!"
                        else -> "Review request sent!"
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(when (currentLanguage) {
                "hi" -> "रद्द करें"
                "te" -> "రద్దు చేయండి"
                else -> "Cancel"
            }, null)
            .show()
    }

    private fun showDataSourcesDialog() {
        val message = """
            ${when (currentLanguage) {
                "hi" -> "डेटा स्रोत:"
                "te" -> "డేటా మూలాలు:"
                else -> "Data Sources:"
            }}
            
            • ${when (currentLanguage) {
                "hi" -> "ऋण आवेदन इतिहास"
                "te" -> "రుణ దరఖాస్తు చరిత్ర"
                else -> "Loan application history"
            }}
            • ${when (currentLanguage) {
                "hi" -> "लेनदेन पैटर्न"
                "te" -> "లావాదేవీ నమూనాలు"
                else -> "Transaction patterns"
            }}
            • ${when (currentLanguage) {
                "hi" -> "पुनर्भुगतान व्यवहार"
                "te" -> "తిరిగి చెల్లింపు ప్రవర్తన"
                else -> "Repayment behavior"
            }}
            • ${when (currentLanguage) {
                "hi" -> "खाता आयु और गतिविधि"
                "te" -> "ఖాతా వయస్సు మరియు కార్యాచరణ"
                else -> "Account age and activity"
            }}
            • ${when (currentLanguage) {
                "hi" -> "AI निर्णय विश्वास"
                "te" -> "AI నిర్ణయ విశ్వాసం"
                else -> "AI decision confidence"
            }}
            
            ${when (currentLanguage) {
                "hi" -> "सभी डेटा एन्क्रिप्टेड और सुरक्षित है।"
                "te" -> "అన్ని డేటా ఎన్క్రిప్ట్ చేయబడింది మరియు సురక్షితం."
                else -> "All data is encrypted and secure."
            }}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle(when (currentLanguage) {
                "hi" -> "डेटा स्रोत"
                "te" -> "డేటా మూలాలు"
                else -> "Data Sources"
            })
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getScoreColor(): Int {
        return when {
            profileScore >= 75 -> 0xFF10B981.toInt() // Green
            profileScore >= 50 -> 0xFFF59E0B.toInt() // Amber
            else -> 0xFFEF4444.toInt() // Red
        }
    }

    private fun getRiskColor(): Int {
        return when (riskLevel) {
            "Low" -> 0xFF10B981.toInt() // Green
            "Medium" -> 0xFFF59E0B.toInt() // Amber
            else -> 0xFFEF4444.toInt() // Red
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun addSpace(parent: LinearLayout, dp: Int) {
        parent.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(dp)
            )
        })
    }
}

