package com.lumeai.banking.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import com.lumeai.banking.DecisionManager
import com.lumeai.banking.FirebaseListenerService
import com.lumeai.banking.models.PersonalizedOffer
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * ChatbotActivity - AI-Powered Banking Transparency Assistant
 * Features: Text input, Voice input, Multi-language, Context-aware
 */
class ChatbotActivity : AppCompatActivity() {
    
    private var currentLanguage = "en"
    private lateinit var messagesContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var micButton: Button
    private lateinit var scrollView: ScrollView
    private lateinit var quickActionsContainer: HorizontalScrollView
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    private val conversationHistory = mutableListOf<ChatMessage>()
    private var isWaitingForResponse = false
    private var userContext: UserContext? = null
    
    private val SPEECH_REQUEST_CODE = 100
    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private val languageBarId = View.generateViewId()
    
    // OpenAI Configuration (Azure OpenAI via AGBS proxy)
    private val OPENAI_API_KEY = "51d1b178df064e37be1a3f4e1fb5c91c"
    private val OPENAI_ENDPOINT = "https://api.dev.agbs.gcservices.io/openai/v1/deployments/gpt-4o-mini/chat/completions?api-version=2024-02-15-preview"
    private val X_AGENT_ID = "a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6" // Valid UUID v4
    
    data class ChatMessage(
        val text: String,
        val isUser: Boolean,
        val timestamp: Long,
        val language: String
    )
    
    data class UserContext(
        val decisions: List<com.lumeai.banking.models.FirebaseDecision>,
        val deniedDecisions: List<com.lumeai.banking.models.FirebaseDecision>,
        val approvedDecisions: List<com.lumeai.banking.models.FirebaseDecision>,
        val pendingDecisions: List<com.lumeai.banking.models.FirebaseDecision>,
        val personalizedOffers: List<PersonalizedOffer>,
        val creditScore: Int,
        val monthlyIncome: Float,
        val age: Int,
        val hasActiveOffers: Boolean,
        val offerCount: Int,
        val biasDetectedCount: Int,
        val lastDecisionBank: String?,
        val lastDecisionType: String?,
        val lastDecisionOutcome: String?,
        // App features info
        val appFeatures: AppFeaturesInfo
    )
    
    data class AppFeaturesInfo(
        val complianceScore: Int = 95,
        val rbiCompliant: Boolean = true,
        val gdprCompliant: Boolean = true,
        val euAIActCompliant: Boolean = true,
        val hasAuditTrail: Boolean = true,
        val hasFairnessMetrics: Boolean = true,
        val hasEducationalContent: Boolean = true,
        val hasFraudDetection: Boolean = true,
        val hasProgressTracker: Boolean = true,
        val hasAIExplainability: Boolean = true,
        val hasPathToApproval: Boolean = true,
        val hasConsentControl: Boolean = true
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        
        // Blue status bar - same as all other pages
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        setContentView(createUI())
        
        // Don't load user context on startup - only when user asks
        // loadUserContext()
        
        // Send simple welcome message
        addBotMessage(getWelcomeMessage())
    }
    
    private fun createUI(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
            
            // Header
            addView(createHeader())
            
            // Language bar
            addView(createLanguageBar())
            
            // Scroll view for messages
            scrollView = ScrollView(this@ChatbotActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                
                messagesContainer = LinearLayout(this@ChatbotActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(16), dp(16), dp(16), dp(16))
                }
                addView(messagesContainer)
            }
            addView(scrollView)
            
            // Quick actions
            addView(createQuickActions())
            
            // Input bar
            addView(createInputBar())
        }
    }
    
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
            
            // Compact back button
            addView(TextView(this@ChatbotActivity).apply {
                text = "←"
                textSize = 24f
                setTextColor(Color.WHITE)
                setPadding(0, 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                gravity = Gravity.CENTER
                setOnClickListener { finish() }
            })
            
            // Title only (no subtitle)
            addView(TextView(this@ChatbotActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "AI चैटबॉट"
                    "te" -> "AI చాట్‌బాట్"
                    else -> "AI Chatbot"
                }
                textSize = 18f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
            })
        }
    }
    
    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            id = languageBarId
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(12), dp(8), dp(12), dp(8))
            gravity = Gravity.END
            setBackgroundColor(Color.WHITE)
            elevation = dp(2).toFloat()
            
            val languages = listOf(
                "English" to "en",
                "हिंदी" to "hi",
                "తెలుగు" to "te"
            )
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(Space(this@ChatbotActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(8), 0)
                    })
                }
            }
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
            tag = code // Store the language code for later reference
            
            val isSelected = currentLanguage == code
            val shape = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)
                } else {
                    setColor(Color.WHITE)
                    setStroke(dp(1), AppTheme.Text.OnCardSecondary)
                }
            }
            background = shape
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    android.util.Log.d("ChatbotActivity", "Language changed to: $code")
                    
                    // Save language preference
                    languagePrefs.edit().putString("language", code).apply()
                    LanguageHelper.setLanguage(this@ChatbotActivity, code)
                    
                    // Update the UI to reflect language change
                    updateLanguageUI()
                    
                    // Refresh welcome message in new language
                    refreshWelcomeMessage()
                    
                    // Show confirmation message
                    val confirmMsg = when (code) {
                        "hi" -> "भाषा हिंदी में बदल दी गई। अब हिंदी में पूछें!"
                        "te" -> "భాష తెలుగులోకి మార్చబడింది। ఇప్పుడు తెలుగులో అడగండి!"
                        else -> "Language changed to English. Ask in English!"
                    }
                    Toast.makeText(this@ChatbotActivity, confirmMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateLanguageUI() {
        android.util.Log.d("ChatbotActivity", "🌍 Updating UI for language: $currentLanguage")
        
        try {
            // Update all language buttons
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            val languageBar = rootView.findViewById<LinearLayout>(languageBarId)
            
            if (languageBar != null) {
                for (i in 0 until languageBar.childCount) {
                    val child = languageBar.getChildAt(i)
                    if (child is TextView && child.tag is String) {
                        val code = child.tag as String
                        val isSelected = currentLanguage == code
                        
                        val shape = GradientDrawable().apply {
                            cornerRadius = dp(20).toFloat()
                            if (isSelected) {
                                setColor(AppTheme.Text.OnCard)
                            } else {
                                setColor(Color.WHITE)
                                setStroke(dp(1), AppTheme.Text.OnCardSecondary)
                            }
                        }
                        child.background = shape
                        child.setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
                        child.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
                    }
                }
            }
            
            // Update input placeholder
            if (::messageInput.isInitialized) {
                messageInput.hint = when (currentLanguage) {
                    "hi" -> "अपना सवाल टाइप करें..."
                    "te" -> "మీ ప్రశ్నను టైప్ చేయండి..."
                    else -> "Type your question..."
                }
            }
            
            // Update quick action buttons
            if (::quickActionsContainer.isInitialized) {
                val quickActionsLayout = quickActionsContainer.getChildAt(0) as? LinearLayout
                quickActionsLayout?.removeAllViews()
                
                val actions = listOf(
                "💳" to when (currentLanguage) {
                    "hi" -> "कार्ड निर्णय"
                    "te" -> "కార్డ్ నిర్ణయం"
                    else -> "Card Decision"
                },
                "🏠" to when (currentLanguage) {
                    "hi" -> "लोन सहायता"
                    "te" -> "రుణ సహాయం"
                    else -> "Loan Help"
                },
                "📊" to when (currentLanguage) {
                    "hi" -> "क्रेडिट स्कोर"
                    "te" -> "క్రెడిట్ స్కోర్"
                    else -> "Credit Score"
                },
                "⚖️" to when (currentLanguage) {
                    "hi" -> "अपील अधिकार"
                    "te" -> "అప్పీల్ హక్కులు"
                    else -> "Appeal Rights"
                },
                "🛡️" to when (currentLanguage) {
                    "hi" -> "डेटा गोपनीयता"
                    "te" -> "డేటా గోప్యత"
                    else -> "Data Privacy"
                },
            "📍" to when (currentLanguage) {
                "hi" -> "आवेदन ट्रैक करें"
                "te" -> "దరఖాస్తు ట్రాక్"
                else -> "Track App"
            }
            )
            
            actions.forEach { (emoji, label) ->
                quickActionsLayout?.addView(createQuickActionButton(emoji, label))
                quickActionsLayout?.addView(Space(this).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(8), 0)
                })
            }
        }
        } catch (e: Exception) {
            android.util.Log.e("ChatbotActivity", "Error updating language UI", e)
        }
    }
    
    private fun createQuickActions(): HorizontalScrollView {
        quickActionsContainer = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        
        val actionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        
        val actions = listOf(
            "💳" to when (currentLanguage) {
                "hi" -> "कार्ड निर्णय"
                "te" -> "కార్డ్ నిర్ణయం"
                else -> "Card Decision"
            },
            "🏠" to when (currentLanguage) {
                "hi" -> "लोन सहायता"
                "te" -> "రుణ సహాయం"
                else -> "Loan Help"
            },
            "📊" to when (currentLanguage) {
                "hi" -> "क्रेडिट स्कोर"
                "te" -> "క్రెడిట్ స్కోర్"
                else -> "Credit Score"
            },
            "⚖️" to when (currentLanguage) {
                "hi" -> "अपील अधिकार"
                "te" -> "అప్పీల్ హక్కులు"
                else -> "Appeal Rights"
            },
            "🛡️" to when (currentLanguage) {
                "hi" -> "डेटा गोपनीयता"
                "te" -> "డేటా గోప్యత"
                else -> "Data Privacy"
            },
            "📍" to when (currentLanguage) {
                "hi" -> "आवेदन ट्रैक करें"
                "te" -> "దరఖాస్తు ట్రాక్"
                else -> "Track App"
            }
        )
        
        actions.forEach { (emoji, label) ->
            actionsLayout.addView(createQuickActionButton(emoji, label))
            actionsLayout.addView(Space(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 0)
            })
        }
        
        quickActionsContainer.addView(actionsLayout)
        return quickActionsContainer
    }
    
    private fun createQuickActionButton(emoji: String, label: String): TextView {
        return TextView(this).apply {
            text = "$emoji $label"
            textSize = 13f
            setPadding(dp(16), dp(8), dp(16), dp(8))
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(AppTheme.Background.Secondary)
                setStroke(dp(1), AppTheme.Primary.HeaderBlue)
            }
            background = shape
            setTextColor(AppTheme.Primary.HeaderBlue)
            
            setOnClickListener {
                handleQuickAction(label)
            }
        }
    }
    
    private fun createInputBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(8).toFloat()
            
            // Mic button
            micButton = Button(this@ChatbotActivity).apply {
                text = "🎤"
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
                
                val micShape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#E8EAF6"))
                }
                background = micShape
                
                setOnClickListener {
                    startVoiceInput()
                }
            }
            addView(micButton)
            
            addView(Space(this@ChatbotActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 0)
            })
            
            // Text input
            messageInput = EditText(this@ChatbotActivity).apply {
                hint = when (currentLanguage) {
                    "hi" -> "अपना सवाल टाइप करें..."
                    "te" -> "మీ ప్రశ్నను టైప్ చేయండి..."
                    else -> "Type your question..."
                }
                textSize = 15f
                setPadding(dp(16), dp(12), dp(16), dp(12))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                maxLines = 1
                imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEND
                
                val inputShape = GradientDrawable().apply {
                    cornerRadius = dp(24).toFloat()
                    setColor(Color.parseColor("#F3F4F6"))
                }
                background = inputShape
                
                // Send on Enter key press
                setOnEditorActionListener { _, actionId, event ->
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND ||
                        (event?.action == android.view.KeyEvent.ACTION_DOWN && 
                         event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                        sendMessage()
                        true
                    } else {
                        false
                    }
                }
                
                addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        val hasText = s.toString().trim().isNotEmpty()
                        updateSendButtonState(hasText && !isWaitingForResponse)
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }
            addView(messageInput)
            
            addView(Space(this@ChatbotActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 0)
            })
            
            // Send button
            sendButton = Button(this@ChatbotActivity).apply {
                text = "→"
                textSize = 20f
                setTextColor(Color.WHITE)
                isEnabled = false
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
                
                // Set initial disabled state
                val sendShape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#9CA3AF"))
                }
                background = sendShape
                
                setOnClickListener {
                    android.util.Log.d("ChatbotActivity", "🔘 Send button clicked")
                    sendMessage()
                }
            }
            addView(sendButton)
        }
    }
    
    private fun updateSendButtonState(enabled: Boolean) {
        sendButton.isEnabled = enabled
        val sendShape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (enabled) {
                colors = AppTheme.Gradients.PrimaryHeader  // Blue gradient
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
            } else {
                setColor(Color.parseColor("#9CA3AF"))
            }
        }
        sendButton.background = sendShape
    }
    
    private fun addBotMessage(text: String) {
        val message = ChatMessage(text, false, System.currentTimeMillis(), currentLanguage)
        conversationHistory.add(message)
        
        messagesContainer.addView(createMessageBubble(message))
        addSpace(8)
        scrollToBottom()
    }
    
    private fun addUserMessage(text: String) {
        val message = ChatMessage(text, true, System.currentTimeMillis(), currentLanguage)
        conversationHistory.add(message)
        
        messagesContainer.addView(createMessageBubble(message))
        addSpace(8)
        scrollToBottom()
    }
    
    private fun createMessageBubble(message: ChatMessage): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = if (message.isUser) Gravity.END else Gravity.START
            setPadding(0, dp(4), 0, dp(4))
            
            // Message bubble
            addView(LinearLayout(this@ChatbotActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    // Max width 75% of screen
                    width = (resources.displayMetrics.widthPixels * 0.75).toInt()
                }
                setPadding(dp(16), dp(12), dp(16), dp(12))
                
                val shape = GradientDrawable().apply {
                    cornerRadius = dp(16).toFloat()
                    if (message.isUser) {
                        colors = AppTheme.Gradients.PrimaryHeader  // Blue gradient
                        orientation = GradientDrawable.Orientation.LEFT_RIGHT
                    } else {
                        setColor(Color.WHITE)
                    }
                }
                background = shape
                if (!message.isUser) {
                    elevation = dp(2).toFloat()
                }
                
                addView(TextView(this@ChatbotActivity).apply {
                    // Parse markdown formatting for bot messages
                    if (!message.isUser) {
                        text = parseMarkdownToSpannable(message.text)
                    } else {
                        text = message.text
                    }
                    textSize = 15f
                    setTextColor(if (message.isUser) Color.WHITE else Color.parseColor("#374151"))
                    setLineSpacing(0f, 1.4f)
                })
            })
            
            // Timestamp
            addView(TextView(this@ChatbotActivity).apply {
                val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                text = timeFormat.format(Date(message.timestamp))
                textSize = 11f
                setTextColor(Color.parseColor("#9CA3AF"))
                setPadding(dp(16), dp(4), dp(16), 0)
                gravity = if (message.isUser) Gravity.END else Gravity.START
            })
        }
    }
    
    private fun showTypingIndicator() {
        val typingView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(16), dp(12), dp(16), dp(12))
            tag = "typing_indicator"
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            elevation = dp(2).toFloat()
            
            addView(TextView(this@ChatbotActivity).apply {
                text = "🤔 " + when (currentLanguage) {
                    "hi" -> "सोच रहा हूं..."
                    "te" -> "ఆలోచిస్తున్నాను..."
                    else -> "Thinking..."
                }
                textSize = 15f
                setTextColor(Color.parseColor("#6B7280"))
            })
        }
        
        messagesContainer.addView(typingView)
        scrollToBottom()
    }
    
    private fun removeTypingIndicator() {
        for (i in 0 until messagesContainer.childCount) {
            val child = messagesContainer.getChildAt(i)
            if (child?.tag == "typing_indicator") {
                messagesContainer.removeView(child)
                break
            }
        }
    }
    
    private fun sendMessage() {
        val text = messageInput.text.toString().trim()
        if (text.isEmpty() || isWaitingForResponse) {
            android.util.Log.d("ChatbotActivity", "⚠️ Cannot send: empty=${text.isEmpty()}, waiting=$isWaitingForResponse")
            return
        }
        
        // Check network connectivity
        if (!isNetworkAvailable()) {
            val errorMsg = when (currentLanguage) {
                "hi" -> "⚠️ कोई इंटरनेट कनेक्शन नहीं। कृपया अपना कनेक्शन जांचें।"
                "te" -> "⚠️ ఇంటర్నెట్ కనెక్షన్ లేదు. దయచేసి మీ కనెక్షన్‌ను తనిఖీ చేయండి।"
                else -> "⚠️ No internet connection. Please check your connection."
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            return
        }
        
        android.util.Log.d("ChatbotActivity", "📤 Sending message: $text")
        
        // Add user message
        addUserMessage(text)
        messageInput.setText("")
        
        // Hide keyboard
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(messageInput.windowToken, 0)
        
        // Show typing indicator
        showTypingIndicator()
        isWaitingForResponse = true
        updateSendButtonState(false)
        micButton.isEnabled = false
        
        // Get AI response
        lifecycleScope.launch {
            try {
                // CRITICAL: Load user context if not already loaded (wait for it to complete)
                if (userContext == null) {
                    android.util.Log.d("ChatbotActivity", "📊 First message - loading user context SYNCHRONOUSLY...")
                    loadUserContextSynchronously()
                }
                
                android.util.Log.d("ChatbotActivity", "🤖 Getting AI response...")
                val response = withContext(Dispatchers.IO) {
                    getAIResponse(text)
                }
                
                android.util.Log.d("ChatbotActivity", "✅ Got response: ${response.take(50)}...")
                removeTypingIndicator()
                addBotMessage(response)
                
            } catch (e: Exception) {
                android.util.Log.e("ChatbotActivity", "❌ Error in sendMessage", e)
                removeTypingIndicator()
                addBotMessage(getFallbackResponse())
            } finally {
                isWaitingForResponse = false
                micButton.isEnabled = true
                val hasText = messageInput.text.toString().trim().isNotEmpty()
                updateSendButtonState(hasText)
                android.util.Log.d("ChatbotActivity", "✅ Message cycle complete")
            }
        }
    }
    
    private suspend fun getAIResponse(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ChatbotActivity", "🤖 Calling AI with message: $userMessage")
                android.util.Log.d("ChatbotActivity", "🌍 Language: $currentLanguage")
                
                val url = URL(OPENAI_ENDPOINT)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("api-key", OPENAI_API_KEY)
                connection.setRequestProperty("x-agent-id", X_AGENT_ID)
                connection.connectTimeout = 30000  // 30 seconds
                connection.readTimeout = 30000     // 30 seconds
                connection.doOutput = true
                connection.doInput = true
                
                val languageName = when (currentLanguage) {
                    "hi" -> "Hindi"
                    "te" -> "Telugu"
                    else -> "English"
                }
                
                val userContextStr = buildContextString()
                val ctx = userContext
                
                android.util.Log.d("ChatbotActivity", "📊 Context for AI: $userContextStr")
                android.util.Log.d("ChatbotActivity", "📊 Context object: decisions=${ctx?.decisions?.size}, denied=${ctx?.deniedDecisions?.size}")
                
                val systemPrompt = """
                    You are LumeAI, an AI banking transparency assistant with FULL ACCESS to customer's banking data.
                    
                    CRITICAL INSTRUCTIONS:
                    1. You MUST respond ONLY in $languageName language
                    2. You MUST use the customer's ACTUAL data provided below
                    3. NEVER say "I can't check" or "I don't have access" - YOU HAVE FULL ACCESS
                    4. When asked about loans/rejections, cite their SPECIFIC applications with bank names
                    5. Be direct and specific with their real data
                    
                    ═══════════════════════════════════════════════════
                    CUSTOMER'S ACTUAL DATA (USE THIS IN YOUR RESPONSES):
                    ═══════════════════════════════════════════════════
                    $userContextStr
                    
                    QUICK FACTS TO USE:
                    - Credit Score: ${ctx?.creditScore ?: "loading..."}
                    - Total Decisions: ${ctx?.decisions?.size ?: 0}
                    - Denied Applications: ${ctx?.deniedDecisions?.size ?: 0}
                    - Approved: ${ctx?.approvedDecisions?.size ?: 0}
                    - Pending: ${ctx?.pendingDecisions?.size ?: 0}
                    - Latest Decision: ${ctx?.lastDecisionBank ?: "none"} - ${ctx?.lastDecisionType ?: ""} (${ctx?.lastDecisionOutcome ?: ""})
                    
                    RESPONSE RULES:
                    ✓ If they ask "Do I have rejected loans?" → Answer YES/NO with count and bank names
                    ✓ If they ask "What's my credit score?" → Tell them: ${ctx?.creditScore ?: "loading..."}
                    ✓ If they ask about offers → List SPECIFIC offers with product names, banks, amounts, interest rates
                    ✓ If they ask "what offers do I have?" → Describe EACH offer by name (e.g., "HDFC FlexiCredit Card at 12% interest")
                    ✓ If they ask why loan rejected → Cite their specific bank denial (HDFC, SBI, etc)
                    ✓ If they ask about regulatory compliance → Reference RBI, GDPR, EU AI Act compliance (95% score!)
                    ✓ If they ask "is this compliant?" → YES! RBI ✅, GDPR ✅, EU AI Act ✅
                    ✓ If they ask about app features → Describe AI Explainability, Path to Approval, Fairness Metrics, etc.
                    ✓ If they ask "what can this app do?" → List ALL features from the context
                    ✗ NEVER say "I can't check" or "I don't have access to your data"
                    ✗ NEVER give generic advice when you have their real data
                    ✗ NEVER say "check your banking app" - YOU have the offer details RIGHT HERE
                    ✗ NEVER say "I don't know about compliance" - ALL compliance info is in the context
                    
                    APP FEATURES (mention when relevant):
                    - AI Explainability Hub: Detailed decision reasons
                    - Path to Approval: AI scenarios for getting approved  
                    - Track Application: Real-time status
                    - Personalized Offers: Pre-approved products
                    
                    Keep responses under 100 words, empathetic, actionable, in $languageName language.
                """.trimIndent()
                
                val requestBody = JSONObject().apply {
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                        
                        // Add conversation history (last 3 exchanges = 6 messages)
                        conversationHistory.takeLast(6).forEach { msg ->
                            put(JSONObject().apply {
                                put("role", if (msg.isUser) "user" else "assistant")
                                put("content", msg.text)
                            })
                        }
                        
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", userMessage)
                        })
                    })
                    put("max_tokens", 300)
                    put("temperature", 0.7)
                }
                
                android.util.Log.d("ChatbotActivity", "📤 Request: ${requestBody.toString(2)}")
                
                // Send request
                connection.outputStream.use { os ->
                    val requestBytes = requestBody.toString().toByteArray(Charsets.UTF_8)
                    os.write(requestBytes)
                    os.flush()
                }
                
                val responseCode = connection.responseCode
                android.util.Log.d("ChatbotActivity", "📥 Response code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    android.util.Log.d("ChatbotActivity", "✅ AI Response received: ${response.take(200)}...")
                    
                    val jsonResponse = JSONObject(response)
                    
                    // Check if response has choices
                    if (!jsonResponse.has("choices") || jsonResponse.getJSONArray("choices").length() == 0) {
                        android.util.Log.e("ChatbotActivity", "❌ No choices in response")
                        return@withContext getFallbackResponse()
                    }
                    
                    val content = jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    
                    android.util.Log.d("ChatbotActivity", "✅ Content extracted: $content")
                    content.trim()
                } else {
                    val errorResponse = try {
                        connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details available"
                    } catch (e: Exception) {
                        "Could not read error: ${e.message}"
                    }
                    android.util.Log.e("ChatbotActivity", "❌ AI API Error: $responseCode")
                    android.util.Log.e("ChatbotActivity", "❌ Error details: $errorResponse")
                    android.util.Log.e("ChatbotActivity", "📤 Request was: ${requestBody.toString(2)}")
                    
                    // Return more specific error message
                    when (responseCode) {
                        401 -> getErrorMessage("Authentication failed. Please check API key.")
                        429 -> getErrorMessage("Too many requests. Please wait a moment.")
                        500, 502, 503 -> getErrorMessage("Server error. Please try again.")
                        else -> getFallbackResponse()
                    }
                }
                
            } catch (e: java.net.SocketTimeoutException) {
                android.util.Log.e("ChatbotActivity", "⏱️ Request timeout", e)
                getErrorMessage("Request timed out. Please check your internet connection.")
            } catch (e: java.net.UnknownHostException) {
                android.util.Log.e("ChatbotActivity", "🌐 Network error", e)
                getErrorMessage("Cannot connect to server. Please check your internet connection.")
            } catch (e: Exception) {
                android.util.Log.e("ChatbotActivity", "❌ AI call failed", e)
                e.printStackTrace()
                getFallbackResponse()
            }
        }
    }
    
    private fun getErrorMessage(englishMsg: String): String {
        return when (currentLanguage) {
            "hi" -> when {
                englishMsg.contains("Authentication") -> "प्रमाणीकरण विफल। कृपया बाद में पुनः प्रयास करें।"
                englishMsg.contains("Too many") -> "बहुत सारे अनुरोध। कृपया थोड़ी देर प्रतीक्षा करें।"
                englishMsg.contains("timeout") -> "समय समाप्त। कृपया अपना इंटरनेट कनेक्शन जांचें।"
                englishMsg.contains("connect") -> "सर्वर से कनेक्ट नहीं हो सका। कृपया अपना इंटरनेट जांचें।"
                else -> "सर्वर त्रुटि। कृपया पुनः प्रयास करें।"
            }
            "te" -> when {
                englishMsg.contains("Authentication") -> "ప్రమాణీకరణ విఫలమైంది। దయచేసి తర్వాత మళ్ళీ ప్రయత్నించండి।"
                englishMsg.contains("Too many") -> "చాలా అభ్యర్థనలు। దయచేసి కొంత సమయం వేచి ఉండండి।"
                englishMsg.contains("timeout") -> "సమయం ముగిసింది। దయచేసి మీ ఇంటర్నెట్ కనెక్షన్‌ను తనిఖీ చేయండి।"
                englishMsg.contains("connect") -> "సర్వర్‌కు కనెక్ట్ చేయలేకపోయింది। దయచేసి మీ ఇంటర్నెట్‌ను తనిఖీ చేయండి।"
                else -> "సర్వర్ లోపం. దయచేసి మళ్ళీ ప్రయత్నించండి।"
            }
            else -> englishMsg
        }
    }
    
    private fun startVoiceInput() {
        // Check if we have RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
            return
        }
        
        // Check for speech recognition availability
        val packageManager = packageManager
        val activities = packageManager.queryIntentActivities(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0
        )
        
        if (activities.isEmpty()) {
            Toast.makeText(
                this,
                "Speech recognition not available. Please install Google app or enable Voice Typing.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // Show helpful toast with better instructions
        val tipMessage = when (currentLanguage) {
            "hi" -> "🎤 माइक खुलेगा - तुरंत स्पष्ट रूप से बोलें!\nउदाहरण: 'मेरा क्रेडिट स्कोर क्या है?'"
            "te" -> "🎤 మైక్ తెరుచుకుంటుంది - వెంటనే స్పష్టంగా మాట్లాడండి!\nఉదాహరణ: 'నా క్రెడిట్ స్కోర్ ఎంత?'"
            else -> "🎤 Mic will open - speak clearly right away!\nExample: 'What is my credit score?'"
        }
        Toast.makeText(this, tipMessage, Toast.LENGTH_LONG).show()
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Use web search model for better accuracy
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            
            // Set language
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, when (currentLanguage) {
                "hi" -> "hi-IN"
                "te" -> "te-IN"
                else -> "en-US"  // Changed to en-US for better recognition
            })
            
            // Friendly prompt
            putExtra(RecognizerIntent.EXTRA_PROMPT, when (currentLanguage) {
                "hi" -> "🎤 अभी बोलें..."
                "te" -> "🎤 ఇప్పుడు మాట్లాడండి..."
                else -> "🎤 Speak now..."
            })
            
            // Get multiple results for better accuracy
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            
            // CRITICAL: Force online recognition (offline is often unreliable)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            
            // More lenient timeout settings - give user more time
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L) // 3 seconds
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L) // Minimum 2 seconds
            
            // Enable partial results
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            
            // Request confidence scores
            putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
            
            // Be more aggressive about listening
            putExtra("android.speech.extra.DICTATION_MODE", true)
        }
        
        try {
            // Change mic button to red (listening)
            val micShape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#EF4444"))
            }
            micButton.background = micShape
            
            val languageCode = when (currentLanguage) {
                "hi" -> "hi-IN"
                "te" -> "te-IN"
                else -> "en-US"
            }
            android.util.Log.d("ChatbotActivity", "🎤 Starting speech recognition with language: $languageCode")
            android.util.Log.d("ChatbotActivity", "🎤 Speech recognition package: ${activities[0].activityInfo.packageName}")
            
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
            android.util.Log.e("ChatbotActivity", "Voice input not available", e)
            resetMicButton()
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting voice input: ${e.message}", Toast.LENGTH_SHORT).show()
            android.util.Log.e("ChatbotActivity", "Error starting voice input", e)
            resetMicButton()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start voice input
                    Toast.makeText(this, "Microphone permission granted. Tap mic again to speak.", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Microphone permission is required for voice input", Toast.LENGTH_LONG).show()
                    android.util.Log.w("ChatbotActivity", "RECORD_AUDIO permission denied")
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        android.util.Log.d("ChatbotActivity", "🎤 onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        
        resetMicButton()
        
        if (requestCode == SPEECH_REQUEST_CODE) {
            android.util.Log.d("ChatbotActivity", "🎤 Speech recognition result: resultCode=$resultCode")
            
            when (resultCode) {
                RESULT_OK -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    android.util.Log.d("ChatbotActivity", "🎤 Speech results: $results")
                    
                    if (results != null && results.isNotEmpty()) {
                        val spokenText = results[0]
                        android.util.Log.d("ChatbotActivity", "✅ Speech recognized: '$spokenText'")
                        
                        if (spokenText.isNotBlank()) {
                            runOnUiThread {
                                messageInput.setText(spokenText)
                                messageInput.setSelection(spokenText.length) // Move cursor to end
                                
                                // Show confirmation toast
                                val confirmMsg = when (currentLanguage) {
                                    "hi" -> "✅ सुना गया: $spokenText"
                                    "te" -> "✅ వినబడింది: $spokenText"
                                    else -> "✅ Heard: $spokenText"
                                }
                                Toast.makeText(this, confirmMsg, Toast.LENGTH_SHORT).show()
                                
                                // Auto-send after a brief delay to show the text
                                messageInput.postDelayed({
                                    if (messageInput.text.toString().trim().isNotEmpty()) {
                                        android.util.Log.d("ChatbotActivity", "🚀 Auto-sending voice message")
                                        sendMessage()
                                    }
                                }, 300)
                            }
                        } else {
                            showSpeechError("No speech detected. Please try again.")
                        }
                    } else {
                        android.util.Log.w("ChatbotActivity", "⚠️ No results in speech data")
                        showSpeechError("No speech detected. Please try again.")
                    }
                }
                
                RESULT_CANCELED -> {
                    android.util.Log.w("ChatbotActivity", "❌ Speech recognition canceled")
                    val errorMessage = when (currentLanguage) {
                        "hi" -> "❌ कोई आवाज़ नहीं सुनाई दी। फिर से कोशिश करें और तुरंत बोलें!"
                        "te" -> "❌ ఎటువంటి మాటలు వినబడలేదు. మళ్ళీ ప్రయత్నించండి మరియు వెంటనే మాట్లాడండి!"
                        else -> "❌ No speech detected. Try again and speak immediately!"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
                
                else -> {
                    android.util.Log.w("ChatbotActivity", "⚠️ Speech recognition failed with result: $resultCode")
                    val errorMessage = when (currentLanguage) {
                        "hi" -> "समझ नहीं आया। कृपया पुनः प्रयास करें।"
                        "te" -> "అర్థం కాలేదు. దయచేసి మళ్ళీ ప్రయత్నించండి।"
                        else -> "Could not understand. Please try again."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showSpeechError(englishMsg: String) {
        val errorMsg = when (currentLanguage) {
            "hi" -> "कोई आवाज़ नहीं सुनाई दी। कृपया पुनः प्रयास करें।"
            "te" -> "ఎటువంటి మాటలు వినబడలేదు. దయచేసి మళ్ళీ ప్రయత్నించండి।"
            else -> englishMsg
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
    }
    
    private fun resetMicButton() {
        val micShape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#E8EAF6"))
        }
        micButton.background = micShape
    }
    
    private fun handleQuickAction(label: String) {
        val query = when {
            label.contains("Card") || label.contains("कार्ड") || label.contains("కార్డ్") -> 
                when (currentLanguage) {
                    "hi" -> "मेरे क्रेडिट कार्ड निर्णय को समझाएं"
                    "te" -> "నా క్రెడిట్ కార్డ్ నిర్ణయాన్ని వివరించండి"
                    else -> "Explain my credit card decision"
                }
            label.contains("Loan") || label.contains("लोन") || label.contains("రుణ") -> 
                when (currentLanguage) {
                    "hi" -> "मुझे लोन कैसे मिल सकता है?"
                    "te" -> "నేను రుణం ఎలా పొందగలను?"
                    else -> "How can I get a loan approved?"
                }
            label.contains("Credit Score") || label.contains("क्रेडिट") || label.contains("క్రెడిట్") -> 
                when (currentLanguage) {
                    "hi" -> "क्रेडिट स्कोर क्या है और कैसे सुधारें?"
                    "te" -> "క్రెడిట్ స్కోర్ అంటే ఏమిటి మరియు ఎలా మెరుగుపరచాలి?"
                    else -> "What is credit score and how to improve it?"
                }
            label.contains("Appeal") || label.contains("अपील") || label.contains("అప్పీల్") -> 
                when (currentLanguage) {
                    "hi" -> "बैंक निर्णय के खिलाफ अपील कैसे करें?"
                    "te" -> "బ్యాంక్ నిర్ణయానికి వ్యతిరేకంగా ఎలా అప్పీల్ చేయాలి?"
                    else -> "How do I appeal a bank decision?"
                }
            label.contains("Privacy") || label.contains("गोपनीयता") || label.contains("గోప్యత") -> 
                when (currentLanguage) {
                    "hi" -> "मेरा डेटा कैसे उपयोग होता है?"
                    "te" -> "నా డేటా ఎలా ఉపయోగించబడుతుంది?"
                    else -> "How is my data used?"
                }
            label.contains("Track") || label.contains("ट्रैक") || label.contains("ట్రాక్") -> 
                when (currentLanguage) {
                    "hi" -> "अपने आवेदन को कैसे ट्रैक करें?"
                    "te" -> "మీ దరఖాస్తును ఎలా ట్రాక్ చేయాలి?"
                    else -> "How do I track my application?"
                }
            else -> label
        }
        
        messageInput.setText(query)
        sendMessage()
    }
    
    private fun getWelcomeMessage(): String {
        return when (currentLanguage) {
            "hi" -> """
                नमस्ते! LumeAI में आपका स्वागत है! 🤖
                
                मैं आपका AI बैंकिंग सहायक हूं। मैं आपकी मदद कर सकता हूं:
                
                📊 **मुख्य विशेषताएं:**
                • लोन/कार्ड निर्णयों की जानकारी
                • अस्वीकृति के कारण और सुधार
                • क्रेडिट स्कोर सलाह
                • व्यक्तिगत ऑफ़र
                • AI निर्णय स्पष्टीकरण
                • पूर्वाग्रह जांच
                • अनुमोदन का रास्ता
                • धोखाधड़ी का पता लगाना
                • शैक्षिक सामग्री
                
                **मुझसे पूछें:**
                "मेरा लोन क्यों अस्वीकार हुआ?"
                "मेरा क्रेडिट स्कोर क्या है?"
                "मैं अनुमोदन कैसे प्राप्त करूं?"
                "मेरे लिए कौन से ऑफ़र हैं?"
                
                टाइप करें या 🎤 बोलें!
            """.trimIndent()
            "te" -> """
                హలో! LumeAIకి స్వాగతం! 🤖
                
                నేను మీ AI బ్యాంకింగ్ సహాయకుడను। నేను మీకు సహాయం చేయగలను:
                
                📊 **ప్రధాన ఫీచర్లు:**
                • రుణ/కార్డ్ నిర్ణయాల సమాచారం
                • తిరస్కరణ కారణాలు మరియు మెరుగుదల
                • క్రెడిట్ స్కోర్ సలహా
                • వ్యక్తిగత ఆఫర్లు
                • AI నిర్ణయ వివరణ
                • పక్షపాత తనిఖీ
                • ఆమోదం మార్గం
                • మోసం గుర్తింపు
                • విద్యా కంటెంట్
                
                **నన్ను అడగండి:**
                "నా రుణం ఎందుకు తిరస్కరించబడింది?"
                "నా క్రెడిట్ స్కోర్ ఎంత?"
                "నేను ఆమోదం ఎలా పొందగలను?"
                "నాకు ఏ ఆఫర్లు ఉన్నాయి?"
                
                టైప్ చేయండి లేదా 🎤 మాట్లాడండి!
            """.trimIndent()
            else -> """
                Welcome to LumeAI! 🤖
                
                I'm your AI Banking Assistant. I can help you with:
                
                📊 **Main Features:**
                • Loan/Card Decision Information
                • Rejection Reasons & Improvements
                • Credit Score Advice
                • Personalized Offers
                • AI Decision Explanations
                • Bias Detection
                • Path to Approval
                • Fraud Detection
                • Educational Content
                
                **Ask me about:**
                "Why was my loan rejected?"
                "What's my credit score?"
                "How can I get approved?"
                "What offers do I have?"
                
                Type or tap 🎤 to speak!
            """.trimIndent()
        }
    }
    
    /**
     * Refresh welcome message when language changes
     */
    private fun refreshWelcomeMessage() {
        // Clear all messages
        messagesContainer.removeAllViews()
        conversationHistory.clear()
        
        // Add welcome message in new language
        addBotMessage(getWelcomeMessage())
        
        android.util.Log.d("ChatbotActivity", "✅ Welcome message refreshed to $currentLanguage")
    }
    
    private fun getFallbackResponse(): String {
        return when (currentLanguage) {
            "hi" -> "क्षमा करें, मुझे थोड़ी समस्या हुई। कृपया फिर से प्रयास करें या अपना प्रश्न अलग तरीके से पूछें।"
            "te" -> "క్షమించండి, నాకు కొంత సమస్య ఉంది। దయచేసి మళ్లీ ప్రయత్నించండి లేదా మీ ప్రశ్నను వేరే విధంగా అడగండి।"
            else -> "I apologize, I'm having trouble right now. Please try again or rephrase your question."
        }
    }
    
    private fun addSpace(dpValue: Int) {
        messagesContainer.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(dpValue))
        })
    }
    
    /**
     * Parse markdown formatting to Android SpannableString
     * Supports: **bold**, numbered lists
     */
    private fun parseMarkdownToSpannable(text: String): android.text.SpannableString {
        val spannable = android.text.SpannableString(text)
        
        // Replace **bold** with actual bold styling
        val boldPattern = "\\*\\*([^*]+)\\*\\*".toRegex()
        var offset = 0
        
        val cleanText = text.replace(boldPattern) { matchResult ->
            val boldText = matchResult.groupValues[1]
            val startIndex = matchResult.range.first - offset
            val endIndex = startIndex + boldText.length
            
            // Apply bold style
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                startIndex,
                endIndex,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            
            offset += 4 // Remove the 4 asterisks
            boldText
        }
        
        return android.text.SpannableString(cleanText)
    }
    
    private fun scrollToBottom() {
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
    
    /**
     * Load comprehensive user context from all app features
     */
    private fun loadUserContext() {
        android.util.Log.d("ChatbotActivity", "📊 Loading user context...")
        lifecycleScope.launch {
            loadUserContextSynchronously()
        }
    }
    
    /**
     * Load user context synchronously (waits until complete)
     */
    private suspend fun loadUserContextSynchronously() {
        try {
            // Load decisions
            val decisions = withContext(Dispatchers.IO) {
                DecisionManager.getAllDecisions(this@ChatbotActivity, forceRefresh = false)
            }
                
            // Load personalized offers from Firebase (EXACTLY like PersonalizedOffersActivity)
            val customerId = FirebaseListenerService.getCustomerId(this@ChatbotActivity)
            val prefs = getSharedPreferences("LumeAIPrefs", MODE_PRIVATE)
            val creditScore = prefs.getInt("user_credit_score", 650)
            val monthlyIncome = prefs.getFloat("user_monthly_income", 30000f)
            val age = prefs.getInt("user_age", 30)
            
            val offers = withContext(Dispatchers.IO) {
                try {
                    val database = FirebaseDatabase.getInstance()
                    val snapshot = database.getReference("personalizedOffers")
                        .orderByChild("customerId")
                        .equalTo(customerId)
                        .get()
                        .await()
                    
                    val offersList = mutableListOf<PersonalizedOffer>()
                    
                    android.util.Log.d("ChatbotActivity", "📦 Firebase returned ${snapshot.childrenCount} offers")
                    
                    // Load Firebase offers (filter exactly like PersonalizedOffersActivity)
                    for (child in snapshot.children) {
                        val offer = child.getValue(PersonalizedOffer::class.java)
                        android.util.Log.d("ChatbotActivity", "📋 Offer: ${offer?.productName} - Status: ${offer?.status}")
                        if (offer != null && offer.status == "ACTIVE" && !offer.userHidden) {
                            // Filter out premium/subscription offers (same as PersonalizedOffersActivity)
                            if (offer.offerType !in listOf("PREMIUM", "SUBSCRIPTION", "APP_UPGRADE")) {
                                // Only show active offers that haven't expired
                                if (offer.expiryTimestamp > System.currentTimeMillis()) {
                                    offersList.add(offer)
                                }
                            }
                        }
                    }
                    
                    // ALWAYS add generated offers (exactly like PersonalizedOffersActivity does)
                    val generatedOffers = generateIntelligentOffers(customerId, creditScore, monthlyIncome, age)
                    offersList.addAll(generatedOffers)
                    
                    android.util.Log.d("ChatbotActivity", "✅ Total offers: ${offersList.size} (${snapshot.childrenCount} from Firebase + ${generatedOffers.size} generated)")
                    offersList.sortedByDescending { it.timestamp }
                } catch (e: Exception) {
                    android.util.Log.e("ChatbotActivity", "❌ Failed to load offers", e)
                    // Still try to generate offers even if Firebase fails
                    generateIntelligentOffers(customerId, creditScore, monthlyIncome, age)
                }
            }
            
            val deniedDecisions = decisions.filter { it.outcome.equals("DENIED", ignoreCase = true) }
            val approvedDecisions = decisions.filter { it.outcome.equals("APPROVED", ignoreCase = true) }
            val pendingDecisions = decisions.filter { it.outcome.equals("PENDING", ignoreCase = true) }
            
            val latestDecision = decisions.firstOrNull()
            
            userContext = UserContext(
                decisions = decisions,
                deniedDecisions = deniedDecisions,
                approvedDecisions = approvedDecisions,
                pendingDecisions = pendingDecisions,
                personalizedOffers = offers,
                creditScore = creditScore,
                monthlyIncome = monthlyIncome,
                age = age,
                hasActiveOffers = offers.isNotEmpty(),
                offerCount = offers.size,
                biasDetectedCount = decisions.count { it.biasDetected },
                lastDecisionBank = latestDecision?.bankName,
                lastDecisionType = latestDecision?.loanType,
                lastDecisionOutcome = latestDecision?.outcome,
                appFeatures = AppFeaturesInfo()
            )
            
            android.util.Log.d("ChatbotActivity", "✅ User context loaded: ${decisions.size} decisions, ${offers.size} offers, Credit: $creditScore")
        } catch (e: Exception) {
            android.util.Log.e("ChatbotActivity", "❌ Failed to load user context", e)
        }
    }
    
    /**
     * Build context string for AI prompt
     */
    private fun buildContextString(): String {
        val context = userContext
        
        // Context should always be loaded now since we wait for it
        if (context == null) {
            return "⚠️ No user data available. Provide general banking assistance only."
        }
        
        val contextParts = mutableListOf<String>()
        
        // Explicit confirmation that we have data
        contextParts.add("✅ FULL ACCESS TO CUSTOMER DATA - USE THIS INFORMATION:")
        contextParts.add("")
        
        // Basic profile
        contextParts.add("USER PROFILE:")
        contextParts.add("- Age: ${context.age}")
        contextParts.add("- Credit Score: ${context.creditScore}")
        contextParts.add("- Monthly Income: ₹${context.monthlyIncome.toInt()}")
        
        // Decision summary
        contextParts.add("\nDECISION HISTORY:")
        contextParts.add("- Total Decisions: ${context.decisions.size}")
        contextParts.add("- Approved: ${context.approvedDecisions.size}")
        contextParts.add("- Denied: ${context.deniedDecisions.size}")
        contextParts.add("- Pending: ${context.pendingDecisions.size}")
        
        // Latest decision
        if (context.lastDecisionBank != null) {
            contextParts.add("\nLATEST DECISION:")
            contextParts.add("- Bank: ${context.lastDecisionBank}")
            contextParts.add("- Type: ${context.lastDecisionType}")
            contextParts.add("- Outcome: ${context.lastDecisionOutcome}")
        }
        
        // Recent denials (detailed) - THIS IS KEY FOR "DO I HAVE REJECTED LOANS"
        if (context.deniedDecisions.isNotEmpty()) {
            contextParts.add("\n❌ DENIED LOANS (Answer YES when asked 'Do I have rejected loans?'):")
            context.deniedDecisions.take(5).forEach { decision ->
                contextParts.add("  • ${decision.bankName}: ${decision.loanType}")
                contextParts.add("    Date: ${formatTimestamp(decision.timestamp)}")
                if (decision.summaryEnglish.isNotEmpty()) {
                    contextParts.add("    Reason: ${decision.summaryEnglish.take(100)}")
                }
            }
        } else {
            contextParts.add("\n✅ NO DENIED LOANS (Answer NO when asked about rejections)")
        }
        
        // Approved loans
        if (context.approvedDecisions.isNotEmpty()) {
            contextParts.add("\n✅ APPROVED LOANS:")
            context.approvedDecisions.take(3).forEach { decision ->
                contextParts.add("  • ${decision.bankName}: ${decision.loanType} (${formatTimestamp(decision.timestamp)})")
            }
        }
        
        // Bias alerts
        if (context.biasDetectedCount > 0) {
            contextParts.add("\n⚠️ BIAS DETECTED: ${context.biasDetectedCount} cases of potential discrimination")
        }
        
        // Personalized Offers (DETAILED)
        if (context.personalizedOffers.isNotEmpty()) {
            contextParts.add("\n🎁 PERSONALIZED OFFERS (${context.personalizedOffers.size} active):")
            context.personalizedOffers.take(5).forEach { offer ->
                contextParts.add("  • ${offer.productName}")
                contextParts.add("    Type: ${offer.offerType}")
                contextParts.add("    Bank: ${offer.bankName}")
                if (offer.eligibleAmount > 0) {
                    contextParts.add("    Amount: ₹${offer.eligibleAmount.toInt()}")
                }
                if (offer.interestRate > 0) {
                    contextParts.add("    Interest: ${offer.interestRate}%")
                }
                if (offer.preApproved) {
                    contextParts.add("    ✅ PRE-APPROVED!")
                }
                if (offer.aiReasonEnglish.isNotEmpty()) {
                    contextParts.add("    Why: ${offer.aiReasonEnglish.take(100)}")
                }
            }
        } else {
            contextParts.add("\n🎁 NO ACTIVE OFFERS at this time")
        }
        
        // APP FEATURES & COMPLIANCE (COMPREHENSIVE)
        contextParts.add("\n═════════════════════════════════════════")
        contextParts.add("📱 LUME AI APP FEATURES & CAPABILITIES:")
        contextParts.add("═════════════════════════════════════════")
        
        contextParts.add("\n⚖️ REGULATORY COMPLIANCE:")
        contextParts.add("  • Overall Compliance Score: ${context.appFeatures.complianceScore}%")
        contextParts.add("  • RBI Guidelines (India): ${if (context.appFeatures.rbiCompliant) "✅ FULLY COMPLIANT" else "⚠️ Partial"}")
        contextParts.add("    - Fair lending practices")
        contextParts.add("    - KYC/AML compliance")
        contextParts.add("    - Customer grievance redressal (30-day appeal)")
        contextParts.add("  • GDPR (Europe): ${if (context.appFeatures.gdprCompliant) "✅ FULLY COMPLIANT" else "⚠️ Partial"}")
        contextParts.add("    - Article 13-14: Right to Information")
        contextParts.add("    - Article 15: Right to Access (Dashboard)")
        contextParts.add("    - Article 22: Right to Explanation for AI decisions")
        contextParts.add("    - Data portability and deletion rights")
        contextParts.add("  • EU AI Act (2024): ${if (context.appFeatures.euAIActCompliant) "✅ FULLY COMPLIANT" else "⚠️ Partial"}")
        contextParts.add("    - Classification: HIGH-RISK AI SYSTEM")
        contextParts.add("    - Transparency requirements met")
        contextParts.add("    - Human oversight implemented")
        contextParts.add("    - Bias monitoring active")
        
        contextParts.add("\n🔍 AI EXPLAINABILITY:")
        contextParts.add("  • AI Explainability Hub - Shows WHY each decision was made")
        contextParts.add("  • Detailed factor breakdown with weights")
        contextParts.add("  • Bias detection and fairness warnings")
        contextParts.add("  • Human-readable summaries in English/Hindi/Telugu")
        
        contextParts.add("\n🎯 PATH TO APPROVAL:")
        contextParts.add("  • AI-generated 'What If' scenarios")
        contextParts.add("  • Shows specific steps to get approved")
        contextParts.add("  • Personalized improvement roadmaps")
        contextParts.add("  • Timeline and cost estimates")
        
        contextParts.add("\n📊 FAIRNESS METRICS:")
        contextParts.add("  • Demographic parity analysis")
        contextParts.add("  • Disparate impact calculations")
        contextParts.add("  • Group-wise approval rate tracking")
        contextParts.add("  • Bias alerts for age/location/digital literacy")
        
        contextParts.add("\n🛡️ FRAUD DETECTION:")
        contextParts.add("  • Real-time fraud scoring")
        contextParts.add("  • Synthetic identity detection")
        contextParts.add("  • Transaction pattern analysis")
        
        contextParts.add("\n📚 FINANCIAL LITERACY:")
        contextParts.add("  • Credit score education")
        contextParts.add("  • Loan types explained")
        contextParts.add("  • Interest rate comparisons")
        contextParts.add("  • Rights and responsibilities")
        
        contextParts.add("\n🔐 CONSENT & PRIVACY:")
        contextParts.add("  • Granular data sharing controls")
        contextParts.add("  • Consent history tracking")
        contextParts.add("  • Data deletion requests")
        contextParts.add("  • Export personal data (GDPR)")
        
        contextParts.add("\n📋 AUDIT TRAIL:")
        contextParts.add("  • Every AI decision logged")
        contextParts.add("  • User consent history tracked")
        contextParts.add("  • Chatbot conversations recorded")
        contextParts.add("  • Model versioning and drift detection")
        
        contextParts.add("\n💬 DECODE MESSAGE:")
        contextParts.add("  • Translates bank jargon to simple language")
        contextParts.add("  • Uses AI to explain complex rejection messages")
        
        contextParts.add("\n📍 TRACK APPLICATION:")
        contextParts.add("  • Real-time application status")
        contextParts.add("  • Firebase-powered live updates")
        
        return contextParts.joinToString("\n")
    }
    
    /**
     * Generate intelligent pre-approved offers based on user profile
     * EXACTLY matching PersonalizedOffersActivity logic
     */
    private fun generateIntelligentOffers(customerId: String, creditScore: Int, monthlyIncome: Float, age: Int): List<PersonalizedOffer> {
        val offers = mutableListOf<PersonalizedOffer>()
        val now = System.currentTimeMillis()
        val thirtyDaysLater = now + (30L * 24 * 60 * 60 * 1000) // 30 days expiry
        
        // 1. Credit Card Offers (if credit score >= 680)
        if (creditScore >= 680) {
            offers.add(PersonalizedOffer(
                id = "gen_cc_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "ICICI Bank",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "CREDIT_CARD",
                productName = "ICICI Platinum Credit Card",
                productDescription = "Lifetime free credit card with amazing benefits",
                offerTitle = "Pre-Approved Credit Card",
                offerSubtitle = "Get ₹50,000 credit limit instantly",
                preApproved = true,
                instantApproval = true,
                eligibleAmount = 50000.0,
                cashback = 5000.0,
                rewardPoints = 10000,
                interestRate = 3.49,
                processingFee = 0.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "Based on your credit score of $creditScore, you're pre-approved for this premium credit card with zero processing fee and instant approval!",
                aiReasonHindi = "आपके $creditScore क्रेडिट स्कोर के आधार पर, आप इस प्रीमियम क्रेडिट कार्ड के लिए पूर्व-स्वीकृत हैं!",
                aiReasonTelugu = "మీ $creditScore క్రెడిట్ స్కోర్ ఆధారంగా, మీరు ఈ ప్రీమియం క్రెడిట్ కార్డ్ కోసం ముందస్తుగా ఆమోదించబడ్డారు!",
                personalizationFactors = listOf("credit_score", "payment_history")
            ))
        }
        
        // 2. Car Loan (if income >= ₹40k and credit score >= 700)
        if (monthlyIncome >= 40000f && creditScore >= 700) {
            offers.add(PersonalizedOffer(
                id = "gen_car_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "HDFC Bank",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "CAR_LOAN",
                productName = "HDFC Car Loan",
                productDescription = "Pre-approved car loan with lowest interest rates",
                offerTitle = "Pre-Approved Car Loan",
                offerSubtitle = "Get up to ₹15 Lakhs for your dream car",
                preApproved = true,
                instantApproval = false,
                eligibleAmount = 1500000.0,
                interestRate = 8.75,
                processingFee = 2500.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "With your monthly income of ₹${monthlyIncome.toInt()} and excellent credit score of $creditScore, you're eligible for this attractive car loan offer!",
                aiReasonHindi = "आपकी मासिक आय ₹${monthlyIncome.toInt()} और बेहतरीन क्रेडिट स्कोर $creditScore के साथ, आप इस आकर्षक कार लोन के लिए पात्र हैं!",
                aiReasonTelugu = "మీ నెలవారీ ఆదాయం ₹${monthlyIncome.toInt()} మరియు అద్భుతమైన క్రెడిట్ స్కోర్ $creditScore తో, మీరు ఈ ఆకర్షణీయమైన కార్ లోన్‌కు అర్హులు!",
                personalizationFactors = listOf("monthly_income", "credit_score")
            ))
        }
        
        // 3. Home Loan (if income >= ₹50k, credit score >= 750, age < 50)
        if (monthlyIncome >= 50000f && creditScore >= 750 && age < 50) {
            offers.add(PersonalizedOffer(
                id = "gen_home_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "SBI",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "HOME_LOAN",
                productName = "SBI Home Loan",
                productDescription = "Pre-approved home loan with special rates",
                offerTitle = "Pre-Approved Home Loan",
                offerSubtitle = "Get up to ₹50 Lakhs for your dream home",
                preApproved = true,
                instantApproval = false,
                eligibleAmount = 5000000.0,
                interestRate = 8.40,
                processingFee = 5000.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "Congratulations! Your excellent profile (income: ₹${monthlyIncome.toInt()}, credit score: $creditScore) makes you eligible for this special home loan offer!",
                aiReasonHindi = "बधाई हो! आपकी उत्कृष्ट प्रोफ़ाइल (आय: ₹${monthlyIncome.toInt()}, क्रेडिट स्कोर: $creditScore) आपको इस विशेष होम लोन ऑफर के लिए पात्र बनाती है!",
                aiReasonTelugu = "అభినందనలు! మీ అద్భుతమైన ప్రొఫైల్ (ఆదాయం: ₹${monthlyIncome.toInt()}, క్రెడిట్ స్కోర్: $creditScore) మిమ్మల్ని ఈ ప్రత్యేక హోమ్ లోన్ ఆఫర్‌కు అర్హులను చేస్తుంది!",
                personalizationFactors = listOf("monthly_income", "credit_score", "age", "employment_stability")
            ))
        }
        
        // 4. Personal Loan (if credit score >= 700 and income >= ₹30k)
        if (creditScore >= 700 && monthlyIncome >= 30000f) {
            offers.add(PersonalizedOffer(
                id = "gen_personal_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "Axis Bank",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "PERSONAL_LOAN",
                productName = "Axis Personal Loan",
                productDescription = "Instant personal loan with minimal documentation",
                offerTitle = "Pre-Approved Personal Loan",
                offerSubtitle = "Get up to ₹5 Lakhs instantly",
                preApproved = true,
                instantApproval = true,
                eligibleAmount = 500000.0,
                interestRate = 10.99,
                processingFee = 1999.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "You're pre-approved for instant personal loan based on your creditworthiness and income stability!",
                aiReasonHindi = "आप अपनी साख और आय स्थिरता के आधार पर तत्काल व्यक्तिगत ऋण के लिए पूर्व-स्वीकृत हैं!",
                aiReasonTelugu = "మీ క్రెడిట్ విలువ మరియు ఆదాయ స్థిరత్వం ఆధారంగా మీరు తక్షణ వ్యక్తిగత లోన్‌కు ముందస్తుగా ఆమోదించబడ్డారు!",
                personalizationFactors = listOf("credit_score", "income_stability")
            ))
        }
        
        return offers
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
