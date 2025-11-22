package com.lumeai.banking

import com.lumeai.banking.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * BankingAIExplainer - AI-Powered engine that translates technical AI decisions
 * into simple, actionable explanations for customers using GPT-4o-mini
 */
object BankingAIExplainer {
    
    // Azure OpenAI via Grand Central AI Gateway
    private const val OPENAI_API_KEY = "51d1b178df064e37be1a3f4e1fb5c91c"
    private const val OPENAI_ENDPOINT = "https://api.dev.agbs.gcservices.io/openai/v1"
    private const val OPENAI_MODEL = "gpt-4o-mini"
    private const val AGENT_ID = "550e8400-e29b-41d4-a716-446655440000"
    private const val API_VERSION = "2024-08-01-preview"
    
    /**
     * Main method: Explain a bank AI decision to a customer using real AI
     */
    suspend fun explain(bankDecision: BankDecision): CustomerExplanation {
        return try {
            android.util.Log.d("BankingAIExplainer", "🚀 Generating AI-powered explanation...")
            
            // Call AI to generate complete explanation
            generateAIExplanation(bankDecision)
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("BankingAIExplainer", "❌ Network error: No internet connection", e)
            generateFallbackExplanation(bankDecision, "⚠️ No internet connection")
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("BankingAIExplainer", "❌ Timeout: AI service took too long", e)
            generateFallbackExplanation(bankDecision, "⚠️ AI service timeout")
        } catch (e: java.io.IOException) {
            android.util.Log.e("BankingAIExplainer", "❌ IO Error: ${e.message}", e)
            generateFallbackExplanation(bankDecision, "⚠️ Connection error")
        } catch (e: org.json.JSONException) {
            android.util.Log.e("BankingAIExplainer", "❌ JSON parsing error: ${e.message}", e)
            generateFallbackExplanation(bankDecision, "⚠️ AI response parsing error")
        } catch (e: Exception) {
            android.util.Log.e("BankingAIExplainer", "❌ Unexpected error: ${e.message}", e)
            generateFallbackExplanation(bankDecision, "⚠️ AI service error")
        }
    }
    
    /**
     * Generate complete explanation using AI
     */
    private suspend fun generateAIExplanation(bankDecision: BankDecision): CustomerExplanation {
        val prompt = buildExplanationPrompt(bankDecision)
        val aiResponse = callAzureOpenAI(prompt)
        return parseExplanationResponse(aiResponse, bankDecision)
    }
    
    /**
     * Build comprehensive prompt for AI
     */
    private fun buildExplanationPrompt(bankDecision: BankDecision): String {
        val factorsDesc = bankDecision.factors.joinToString("\n") { 
            "- ${it.technicalName}: ${it.value} (threshold: ${it.threshold}, passed: ${it.passed}, weight: ${it.weight})"
        }
        
        return """
You are a banking transparency AI assistant. Explain this bank decision in simple, empathetic language.

DECISION DETAILS:
- Customer ID: ${bankDecision.customerId}
- Decision Type: ${bankDecision.decisionType}
- Customer Age: ${bankDecision.customerProfile.age}
- Location: ${bankDecision.customerProfile.locationType}
- Digital Literacy: ${bankDecision.customerProfile.digitalLiteracy}

DECISION FACTORS:
$factorsDesc

TASK: Create a complete customer explanation in JSON format with:
1. summaryEnglish: Brief 2-sentence summary in English
2. summaryHindi: Same summary in Hindi
3. factors: Array of explained factors, each with:
   - friendlyName: Simple name (English)
   - impact: "High", "Medium", or "Low"
   - status: "✓ Met" or "✗ Not Met"  
   - yourValue: Customer's value in simple terms
   - required: Required value in simple terms
   - explanation: Why this matters (1-2 sentences, English)
   - actionableStep: What customer can do (English)
4. biasWarning: If age/location/digital factors seem unfair, provide:
   - detected: true/false
   - type: bias type
   - severity: "Low"/"Medium"/"High"
   - explanation: Why this might be biased
   - recommendation: What customer should do
5. nextSteps: Array of 3-5 actionable steps (English)
6. appealInfo: How to appeal (English)

IMPORTANT:
- Be empathetic and constructive
- Avoid jargon
- Focus on solutions
- Detect potential bias (age, location, digital literacy discrimination)
- Keep explanations under 50 words each

Respond ONLY with valid JSON, no markdown.
""".trim()
    }
    
    /**
     * Call Azure OpenAI API
     */
    private suspend fun callAzureOpenAI(prompt: String): String {
        return withContext(Dispatchers.IO) {
            val azureUrl = "$OPENAI_ENDPOINT/deployments/$OPENAI_MODEL/chat/completions?api-version=$API_VERSION"
            val url = URL(azureUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("api-key", OPENAI_API_KEY)
                connection.setRequestProperty("x-agent-id", AGENT_ID)
                connection.doOutput = true
            connection.connectTimeout = 120000  // Increased to 120 seconds (2 minutes)
            connection.readTimeout = 120000     // Increased to 120 seconds (2 minutes)
                
                val requestBody = JSONObject().apply {
                    put("messages", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are a banking transparency AI that explains decisions clearly. Always respond with valid JSON.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                    put("temperature", 0.4)
                    put("max_tokens", 2000)
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
                    throw Exception("API Error: $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        }
    }
    
    /**
     * Parse AI response into CustomerExplanation
     */
    private fun parseExplanationResponse(aiContent: String, bankDecision: BankDecision): CustomerExplanation {
        val json = JSONObject(aiContent)
        
        // Parse explained factors
        val factorsArray = json.getJSONArray("factors")
        val explainedFactors = mutableListOf<ExplainedFactor>()
        for (i in 0 until factorsArray.length()) {
            val factorJson = factorsArray.getJSONObject(i)
            explainedFactors.add(ExplainedFactor(
                friendlyName = factorJson.getString("friendlyName"),
                impact = factorJson.getString("impact"),
                status = factorJson.getString("status"),
                yourValue = factorJson.getString("yourValue"),
                required = factorJson.getString("required"),
                explanation = factorJson.getString("explanation"),
                actionableStep = factorJson.getString("actionableStep")
            ))
        }
        
        // Parse bias warning
        val biasJson = json.getJSONObject("biasWarning")
        val biasWarning = if (biasJson.optBoolean("detected", false)) {
            BiasWarning(
                severity = biasJson.optString("severity", "Low"),
                message = biasJson.optString("explanation", "Potential bias detected"),
                affectedGroups = listOf(biasJson.optString("type", "Unknown")),
                mitigationSteps = listOf(biasJson.optString("recommendation", "Contact bank for review"))
            )
        } else {
            null
        }
        
        // Parse next steps
        val nextStepsArray = json.getJSONArray("nextSteps")
        val nextSteps = mutableListOf<ActionableStep>()
        for (i in 0 until nextStepsArray.length()) {
            val stepText = nextStepsArray.getString(i)
            nextSteps.add(ActionableStep(
                title = "Step ${i+1}",
                description = stepText,
                estimatedImpact = if (i == 0) "High" else if (i == 1) "Medium" else "Low",
                timeframe = "1-6 months"
            ))
        }
        
        return CustomerExplanation(
            summaryEnglish = json.getString("summaryEnglish"),
            summaryHindi = json.getString("summaryHindi"),
            factors = explainedFactors,
            biasWarning = biasWarning,
            nextSteps = nextSteps,
            appealInfo = AppealInfo(
                canAppeal = true,
                process = json.getString("appealInfo"),
                timeframe = "15-30 days",
                requiredDocuments = listOf("ID proof", "Address proof")
            )
        )
    }
    
    /**
     * Emergency fallback with error transparency (rarely used)
     */
    private fun generateFallbackExplanation(bankDecision: BankDecision, errorMessage: String = "⚠️ Using backup system"): CustomerExplanation {
        return CustomerExplanation(
            summaryEnglish = "$errorMessage. Using backup explanation system.\n\nYour ${bankDecision.decisionType} decision requires review. Please contact the bank for details.",
            summaryHindi = "$errorMessage। बैकअप व्याख्या प्रणाली का उपयोग कर रहे हैं।\n\nआपके ${bankDecision.decisionType} निर्णय की समीक्षा आवश्यक है। विवरण के लिए बैंक से संपर्क करें।",
            factors = emptyList(),
            biasWarning = null,
            nextSteps = listOf(
                ActionableStep("Contact Bank", "Contact bank customer service", "High", "Immediate"),
                ActionableStep("Request Review", "Request detailed explanation", "Medium", "1-2 days")
            ),
            appealInfo = AppealInfo(
                canAppeal = true,
                process = "Contact your bank within 30 days",
                timeframe = "30 days",
                requiredDocuments = listOf("ID proof")
            )
        )
    }
}
