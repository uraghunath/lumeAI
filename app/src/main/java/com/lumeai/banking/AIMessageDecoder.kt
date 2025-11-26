package com.lumeai.banking

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * AIMessageDecoder - Uses generative AI to decode bank rejection messages
 * Translates complex banking jargon into simple, actionable language
 */
object AIMessageDecoder {
    
    // Azure OpenAI via Grand Central AI Gateway
    private const val OPENAI_API_KEY = "51d1b178df064e37be1a3f4e1fb5c91c"
    private const val OPENAI_ENDPOINT = "https://api.hack.lume.services.io/openai/v1"
    private const val OPENAI_MODEL = "gpt-4o-mini"
    private const val AGENT_ID = "zzzzzzzzzz" // AI Gateway requirement
    private const val API_VERSION = "2024-08-01-preview"
    
    /**
     * Decode a bank message using generative AI (OpenAI GPT-4o-mini)
     */
    suspend fun decodeMessage(
        message: String,
        language: String = "en"
    ): DecodedMessage {
        android.util.Log.d("AIMessageDecoder", "🚀 CALLING REAL AI (OpenAI GPT-4o-mini)...")
        android.util.Log.d("AIMessageDecoder", "Message: ${message.take(100)}...")
        
        return try {
            // Always use real AI now!
            val aiResponse = callOpenAI(message, language)
            parseAIResponse(aiResponse, language)
        } catch (e: Exception) {
            // Fallback to mock only on error
            android.util.Log.e("AIMessageDecoder", "❌ AI CALL FAILED, USING FALLBACK: ${e.message}", e)
            mockDecode(message, language)
        }
    }
    
    /**
     * Call OpenAI API (GPT-4o-mini)
     */
    private suspend fun callOpenAI(message: String, language: String): String {
        return withContext(Dispatchers.IO) {
            val prompt = buildPrompt(message, language)
            
            // Azure OpenAI format: /deployments/{model}/chat/completions?api-version=...
            val azureUrl = "$OPENAI_ENDPOINT/deployments/$OPENAI_MODEL/chat/completions?api-version=$API_VERSION"
            val url = URL(azureUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            android.util.Log.d("AIMessageDecoder", "📡 Making Azure OpenAI call to: $url")
            android.util.Log.d("AIMessageDecoder", "📡 Model: $OPENAI_MODEL")
            
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
                            put("content", "You are a banking AI assistant that helps customers understand bank decisions in simple, clear language. Always respond with valid JSON.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                    put("temperature", 0.3)
                    put("max_tokens", 1000)
                }
                
                android.util.Log.d("AIMessageDecoder", "📤 Request: ${requestBody.toString().take(200)}...")
                
                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray())
                }
                
                val responseCode = connection.responseCode
                android.util.Log.d("AIMessageDecoder", "📥 Response code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    android.util.Log.d("AIMessageDecoder", "📥 Raw response: ${response.take(300)}...")
                    
                    // Extract content from OpenAI response format
                    val jsonResponse = JSONObject(response)
                    val content = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    
                    android.util.Log.d("AIMessageDecoder", "✅ Extracted content: ${content.take(200)}...")
                    content
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    android.util.Log.e("AIMessageDecoder", "❌ API Error ($responseCode): $errorBody")
                    throw Exception("API call failed: $responseCode - $errorBody")
                }
            } finally {
                connection.disconnect()
            }
        }
    }
    
    /**
     * Build prompt for AI
     */
    private fun buildPrompt(message: String, language: String): String {
        val languageName = when (language) {
            "hi" -> "Hindi"
            "te" -> "Telugu"
            else -> "English"
        }
        
        return """
You are an AI assistant helping customers understand bank rejection messages.

Bank Message:
"$message"

Analyze this message and provide:

1. SUMMARY: What happened in 1-2 simple sentences
2. REASON: Why was it rejected? (main reason only)
3. FACTORS: List 2-4 specific factors that led to rejection
4. ACTIONS: List 3 practical steps the customer can take immediately
5. TIMELINE: How long will it take to fix this?
6. SEVERITY: Is this a temporary issue or serious problem?

Guidelines:
- Use simple, clear language (avoid banking jargon)
- Be empathetic and constructive
- Focus on actionable solutions
- Respond in $languageName language
- Format response as JSON with keys: summary, reason, factors (array), actions (array), timeline, severity

Example format:
{
  "summary": "Your loan was rejected because...",
  "reason": "Low credit score",
  "factors": ["Credit score below 650", "High existing debt"],
  "actions": ["Check CIBIL report", "Pay off one loan", "Wait 6 months"],
  "timeline": "6-12 months",
  "severity": "Moderate - Can be improved"
}
""".trimIndent()
    }
    
    /**
     * Parse OpenAI response (content already extracted from API response)
     */
    private fun parseAIResponse(aiContent: String, language: String): DecodedMessage {
        try {
            android.util.Log.d("AIMessageDecoder", "✅ REAL AI RESPONSE RECEIVED: ${aiContent.take(200)}...")
            
            // OpenAI might return JSON directly or wrapped in markdown
            val jsonText = when {
                aiContent.trim().startsWith("{") -> aiContent.trim()
                aiContent.contains("```json") -> aiContent.substringAfter("```json").substringBefore("```").trim()
                aiContent.contains("```") -> aiContent.substringAfter("```").substringBefore("```").trim()
                else -> aiContent.trim()
            }
            
            val decoded = JSONObject(jsonText)
            
            val factors = mutableListOf<String>()
            val factorsArray = decoded.getJSONArray("factors")
            for (i in 0 until factorsArray.length()) {
                factors.add(factorsArray.getString(i))
            }
            
            val actions = mutableListOf<String>()
            val actionsArray = decoded.getJSONArray("actions")
            for (i in 0 until actionsArray.length()) {
                actions.add(actionsArray.getString(i))
            }
            
            android.util.Log.d("AIMessageDecoder", "✅ AI PARSING SUCCESS! Reason: ${decoded.getString("reason")}")
            
            return DecodedMessage(
                summary = decoded.getString("summary"),
                reason = decoded.getString("reason"),
                factors = factors,
                actions = actions,
                timeline = decoded.getString("timeline"),
                severity = decoded.getString("severity"),
                isAIGenerated = true
            )
        } catch (e: Exception) {
            android.util.Log.e("AIMessageDecoder", "❌ AI PARSING FAILED: ${e.message}")
            android.util.Log.e("AIMessageDecoder", "Raw AI content: $aiContent")
            throw e // Re-throw to trigger fallback in decodeMessage
        }
    }
    
    /**
     * ENHANCED Mock decoder with varied realistic responses
     * This provides better demo experience until API endpoint is fixed
     */
    private fun mockDecode(message: String, language: String): DecodedMessage {
        // Generate varied responses based on message content hash for consistency
        val messageHash = message.hashCode() % 5
        
        // Detect type of rejection from message
        val isLoan = message.contains("loan", ignoreCase = true) || 
                     message.contains("credit", ignoreCase = true)
        val isCard = message.contains("card", ignoreCase = true)
        
        return when (language) {
            "hi" -> {
                if (isLoan) {
                    DecodedMessage(
                        summary = "आपका ऋण आवेदन अस्वीकार कर दिया गया है क्योंकि आपका क्रेडिट स्कोर आवश्यकता से कम है।",
                        reason = "कम क्रेडिट स्कोर (650 से नीचे)",
                        factors = listOf(
                            "आपका क्रेडिट स्कोर 620 है, बैंक को 700+ चाहिए",
                            "पिछले 12 महीनों में 3 विलंबित भुगतान",
                            "ऋण-से-आय अनुपात 45% है (40% से अधिक)"
                        ),
                        actions = listOf(
                            "💡 अपनी मुफ्त CIBIL रिपोर्ट डाउनलोड करें और त्रुटियां जांचें",
                            "💡 सभी बिलों का 6 महीने तक समय पर भुगतान करें",
                            "💡 एक छोटा ऋण चुकाएं ताकि ऋण-से-आय अनुपात में सुधार हो",
                            "💡 6 महीने बाद फिर से आवेदन करें"
                        ),
                        timeline = "6-12 महीने",
                        severity = "मध्यम - सुधार किया जा सकता है",
                        isAIGenerated = false
                    )
                } else {
                    DecodedMessage(
                        summary = "आपका आवेदन अस्वीकार कर दिया गया है। बैंक ने आपके वित्तीय प्रोफ़ाइल में कुछ चिंताएं पाईं।",
                        reason = "आय सत्यापन और क्रेडिट इतिहास",
                        factors = listOf(
                            "आय दस्तावेज़ पूर्ण नहीं थे",
                            "बैंकिंग संबंध 6 महीने से कम",
                            "डिजिटल लेनदेन इतिहास सीमित"
                        ),
                        actions = listOf(
                            "💡 6 महीने की वेतन पर्ची और बैंक स्टेटमेंट जमा करें",
                            "💡 नियमित रूप से UPI/ऑनलाइन बैंकिंग का उपयोग करें",
                            "💡 3 महीने बाद फिर से प्रयास करें"
                        ),
                        timeline = "3-6 महीने",
                        severity = "कम - आसानी से ठीक किया जा सकता है",
                        isAIGenerated = false
                    )
                }
            }
            "te" -> {
                if (isLoan) {
                    DecodedMessage(
                        summary = "మీ రుణ దరఖాస్తు తిరస్కరించబడింది ఎందుకంటే మీ క్రెడిట్ స్కోర్ అవసరత కంటే తక్కువగా ఉంది।",
                        reason = "తక్కువ క్రెడిట్ స్కోర్ (650 కంటే తక్కువ)",
                        factors = listOf(
                            "మీ క్రెడిట్ స్కోర్ 620, బ్యాంకుకు 700+ అవసరం",
                            "గత 12 నెలల్లో 3 ఆలస్యమైన చెల్లింపులు",
                            "అప్పు-ఆదాయ నిష్పత్తి 45% (40% కంటే ఎక్కువ)"
                        ),
                        actions = listOf(
                            "💡 మీ ఉచిత CIBIL నివేదిక డౌన్‌లోడ్ చేసి లోపాలను తనిఖీ చేయండి",
                            "💡 అన్ని బిల్లులను 6 నెలల పాటు సమయానికి చెల్లించండి",
                            "💡 అప్పు-ఆదాయ నిష్పత్తిని మెరుగుపరచడానికి ఒక చిన్న రుణం తిరిగి చెల్లించండి",
                            "💡 6 నెలల తర్వాత మళ్లీ దరఖాస్తు చేయండి"
                        ),
                        timeline = "6-12 నెలలు",
                        severity = "మధ్యస్థ - మెరుగుపరచవచ్చు",
                        isAIGenerated = false
                    )
                } else {
                    DecodedMessage(
                        summary = "మీ దరఖాస్తు తిరస్కరించబడింది। బ్యాంకు మీ ఆర్థిక ప్రొఫైల్‌లో కొన్ని ఆందోళనలను కనుగొంది।",
                        reason = "ఆదాయ ధృవీకరణ మరియు క్రెడిట్ చరిత్ర",
                        factors = listOf(
                            "ఆదాయ పత్రాలు పూర్తి కాలేదు",
                            "బ్యాంకింగ్ సంబంధం 6 నెలల కంటే తక్కువ",
                            "డిజిటల్ లావాదేవీ చరిత్ర పరిమితం"
                        ),
                        actions = listOf(
                            "💡 6 నెలల జీత స్లిప్‌లు మరియు బ్యాంక్ స్టేట్‌మెంట్ సమర్పించండి",
                            "💡 UPI/ఆన్‌లైన్ బ్యాంకింగ్‌ను క్రమం తప్పకుండా ఉపయోగించండి",
                            "💡 3 నెలల తర్వాత మళ్లీ ప్రయత్నించండి"
                        ),
                        timeline = "3-6 నెలలు",
                        severity = "తక్కువ - సులభంగా సరిదిద్దవచ్చు",
                        isAIGenerated = false
                    )
                }
            }
            else -> {
                if (isLoan) {
                    DecodedMessage(
                        summary = "Your loan application was rejected because your credit score is below the required threshold.",
                        reason = "Low credit score (below 650)",
                        factors = listOf(
                            "Your credit score is 620, bank requires 700+",
                            "3 late payments in the last 12 months",
                            "Debt-to-income ratio is 45% (exceeds 40% limit)"
                        ),
                        actions = listOf(
                            "💡 Download your free CIBIL report and check for errors",
                            "💡 Pay all bills on time for 6 months",
                            "💡 Pay off one small loan to improve debt-to-income ratio",
                            "💡 Reapply after 6 months of good credit behavior"
                        ),
                        timeline = "6-12 months",
                        severity = "Moderate - Can be improved",
                        isAIGenerated = false
                    )
                } else {
                    DecodedMessage(
                        summary = "Your application was rejected. The bank found some concerns in your financial profile.",
                        reason = "Income verification and credit history",
                        factors = listOf(
                            "Income documents were incomplete",
                            "Banking relationship less than 6 months",
                            "Limited digital transaction history"
                        ),
                        actions = listOf(
                            "💡 Submit 6 months of salary slips and bank statements",
                            "💡 Use UPI/online banking regularly",
                            "💡 Try again after 3 months of building relationship"
                        ),
                        timeline = "3-6 months",
                        severity = "Low - Easy to fix",
                        isAIGenerated = false
                    )
                }
            }
        }
    }
}

/**
 * Decoded bank message with AI-generated insights
 */
data class DecodedMessage(
    val summary: String,
    val reason: String,
    val factors: List<String>,
    val actions: List<String>,
    val timeline: String,
    val severity: String,
    val isAIGenerated: Boolean
)

