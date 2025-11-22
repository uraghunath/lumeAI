package com.lumeai.banking.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.lumeai.banking.AIMessageDecoder
import com.lumeai.banking.DecodedMessage
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DecodeMessageActivity - AI-powered decoder for bank rejection messages
 */
class DecodeMessageActivity : AppCompatActivity() {
    
    private lateinit var messageInput: EditText
    private lateinit var decodeButton: Button
    private lateinit var resultsContainer: LinearLayout
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var scrollView: ScrollView
    
    private var currentLanguage = "en" // en, hi, te
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        
        // Blue status bar - same as all other pages
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        
        // Hide action bar to avoid duplicate header
        supportActionBar?.hide()
        
        setContentView(createUI())
    }
    
    private fun createUI(): ScrollView {
        scrollView = ScrollView(this)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
        }
        
        // Header
        rootLayout.addView(createHeader())
        
        // Instructions
        rootLayout.addView(createInstructions())
        
        // Input area
        rootLayout.addView(createInputArea())
        
        // Loading spinner
        loadingSpinner = ProgressBar(this).apply {
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(48),
                dpToPx(48)
            ).apply {
                gravity = Gravity.CENTER
                setMargins(0, dpToPx(16), 0, dpToPx(16))
            }
        }
        rootLayout.addView(loadingSpinner)
        
        // Results container
        resultsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), 0, dpToPx(16), dpToPx(16))
        }
        rootLayout.addView(resultsContainer)
        
        scrollView.addView(rootLayout)
        return scrollView
    }
    
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            
            // Compact header row
            addView(LinearLayout(this@DecodeMessageActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(AppTheme.Background.Secondary)
                setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
                gravity = Gravity.CENTER_VERTICAL
                elevation = dpToPx(4).toFloat()
                
                // Compact back button
                addView(TextView(this@DecodeMessageActivity).apply {
                    text = "←"
                    textSize = 24f
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(0, 0, dpToPx(12), 0)
                    layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40))
                    gravity = Gravity.CENTER
                    setOnClickListener { finish() }
                })
                
                // Title only (no subtitle)
                addView(TextView(this@DecodeMessageActivity).apply {
                    text = "Decode Message"
                    textSize = 18f
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    gravity = Gravity.CENTER_VERTICAL
                })
            })
            
            // Language buttons
            addView(createLanguageButtons())
        }
    }
    
    private fun createLanguageButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            gravity = Gravity.END
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            elevation = dpToPx(2).toFloat()
            
            val languages = listOf(
                "English" to "en",
                "हिंदी" to "hi",
                "తెలుగు" to "te"
            )
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(View(this@DecodeMessageActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dpToPx(8), 0)
                    })
                }
            }
        }
    }
    
    private fun createLanguageButton(name: String, code: String): TextView {
        return TextView(this).apply {
            text = name
            textSize = 13f
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            
            val isSelected = currentLanguage == code
            val shape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)
                } else {
                    setColor(0xFFFFFFFF.toInt())
                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)
                }
            }
            background = shape
            setTextColor(if (isSelected) 0xFFFFFFFF.toInt() else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    // Save language preference
                    LanguageHelper.setLanguage(this@DecodeMessageActivity, code)
                    recreate()
                }
            }
        }
    }
    
    private fun createInstructions(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFE8EDF5.toInt())
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16))
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = getString(currentLanguage, "instructions_title")
                textSize = 16f
                setTextColor(0xFF5C7BC0.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = getString(currentLanguage, "instructions_text")
                textSize = 14f
                setTextColor(0xFF4A6BA8.toInt())
                setPadding(0, dpToPx(8), 0, 0)
                setLineSpacing(0f, 1.3f)
            })
        }
    }
    
    private fun createInputArea(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = getString(currentLanguage, "input_label")
                textSize = 14f
                setTextColor(0xFF424242.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(8))
            })
            
            messageInput = EditText(this@DecodeMessageActivity).apply {
                hint = getString(currentLanguage, "input_hint")
                setHintTextColor(0xFF9E9E9E.toInt())
                minLines = 6
                maxLines = 10
                gravity = Gravity.TOP
                setBackgroundColor(0xFFFFFFFF.toInt())
                setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
                elevation = dpToPx(2).toFloat()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dpToPx(16)
                }
            }
            addView(messageInput)
            
            decodeButton = Button(this@DecodeMessageActivity).apply {
                text = getString(currentLanguage, "decode_button")
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                
                val btnShape = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = dpToPx(10).toFloat()
                    colors = intArrayOf(0xFF5C7BC0.toInt(), 0xFF4A6BA8.toInt())
                    orientation = android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
                }
                background = btnShape
                
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(16))
                elevation = dpToPx(4).toFloat()
                setOnClickListener { decodeMessage() }
            }
            addView(decodeButton)
        }
    }
    
    private fun decodeMessage() {
        val message = messageInput.text.toString().trim()
        
        if (message.isEmpty()) {
            Toast.makeText(
                this,
                getString(currentLanguage, "error_empty"),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Show loading
        loadingSpinner.visibility = View.VISIBLE
        decodeButton.isEnabled = false
        resultsContainer.removeAllViews()
        
        // Call AI decoder
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val decoded = withContext(Dispatchers.IO) {
                    AIMessageDecoder.decodeMessage(message, currentLanguage)
                }
                displayResults(decoded)
            } catch (e: Exception) {
                Toast.makeText(
                    this@DecodeMessageActivity,
                    getString(currentLanguage, "error_decode"),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                loadingSpinner.visibility = View.GONE
                decodeButton.isEnabled = true
            }
        }
    }
    
    private fun displayResults(decoded: DecodedMessage) {
        resultsContainer.removeAllViews()
        
        // AI badge
        if (decoded.isAIGenerated) {
            resultsContainer.addView(createAIBadge())
        }
        
        // Summary card
        resultsContainer.addView(createSummaryCard(decoded))
        
        // Severity indicator
        resultsContainer.addView(createSeverityCard(decoded))
        
        // Main reason
        resultsContainer.addView(createReasonCard(decoded))
        
        // Factors
        resultsContainer.addView(createFactorsCard(decoded))
        
        // Actions
        resultsContainer.addView(createActionsCard(decoded))
        
        // Timeline
        resultsContainer.addView(createTimelineCard(decoded))
        
        // Scroll to results
        scrollView.post {
            scrollView.smoothScrollTo(0, resultsContainer.top)
        }
    }
    
    private fun createAIBadge(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF7C4DFF.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                bottomMargin = dpToPx(12)
            }
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = "✨ " + getString(currentLanguage, "ai_powered")
                textSize = 12f
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
        }
    }
    
    private fun createSummaryCard(decoded: DecodedMessage): LinearLayout {
        return createCard(0xFFE3F2FD.toInt(), 0xFF1976D2.toInt()) {
            addView(TextView(this@DecodeMessageActivity).apply {
                text = "📋 " + getString(currentLanguage, "summary_title")
                textSize = 16f
                setTextColor(0xFF1976D2.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = decoded.summary
                textSize = 15f
                setTextColor(0xFF424242.toInt())
                setPadding(0, dpToPx(8), 0, 0)
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    private fun createSeverityCard(decoded: DecodedMessage): LinearLayout {
        val severityColor = when {
            decoded.severity.contains("High", ignoreCase = true) || 
            decoded.severity.contains("Serious", ignoreCase = true) -> 0xFFE53935.toInt()
            decoded.severity.contains("Moderate", ignoreCase = true) || 
            decoded.severity.contains("Medium", ignoreCase = true) -> 0xFFFB8C00.toInt()
            else -> 0xFF43A047.toInt()
        }
        
        return createCard(0xFFFAFAFA.toInt(), severityColor) {
            addView(TextView(this@DecodeMessageActivity).apply {
                text = getString(currentLanguage, "severity_title")
                textSize = 14f
                setTextColor(0xFF757575.toInt())
            })
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = decoded.severity
                textSize = 16f
                setTextColor(severityColor)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dpToPx(4), 0, 0)
            })
        }
    }
    
    private fun createReasonCard(decoded: DecodedMessage): LinearLayout {
        return createCard(0xFFFFF3E0.toInt(), 0xFFF57C00.toInt()) {
            addView(TextView(this@DecodeMessageActivity).apply {
                text = "⚠️ " + getString(currentLanguage, "reason_title")
                textSize = 16f
                setTextColor(0xFFF57C00.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = decoded.reason
                textSize = 15f
                setTextColor(0xFF424242.toInt())
                setPadding(0, dpToPx(8), 0, 0)
            })
        }
    }
    
    private fun createFactorsCard(decoded: DecodedMessage): LinearLayout {
        return createCard(0xFFFCE4EC.toInt(), 0xFFC2185B.toInt()) {
            addView(TextView(this@DecodeMessageActivity).apply {
                text = "📊 " + getString(currentLanguage, "factors_title")
                textSize = 16f
                setTextColor(0xFFC2185B.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(8))
            })
            
            decoded.factors.forEach { factor ->
                addView(TextView(this@DecodeMessageActivity).apply {
                    text = "• $factor"
                    textSize = 14f
                    setTextColor(0xFF424242.toInt())
                    setPadding(dpToPx(8), dpToPx(4), 0, dpToPx(4))
                    setLineSpacing(0f, 1.3f)
                })
            }
        }
    }
    
    private fun createActionsCard(decoded: DecodedMessage): LinearLayout {
        return createCard(0xFFE8F5E9.toInt(), 0xFF43A047.toInt()) {
            addView(TextView(this@DecodeMessageActivity).apply {
                text = "✅ " + getString(currentLanguage, "actions_title")
                textSize = 16f
                setTextColor(0xFF2E7D32.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(12))
            })
            
            decoded.actions.forEachIndexed { index, action ->
                addView(TextView(this@DecodeMessageActivity).apply {
                    text = "${index + 1}. $action"
                    textSize = 14f
                    setTextColor(0xFF1B5E20.toInt())
                    setPadding(dpToPx(8), dpToPx(6), 0, dpToPx(6))
                    setLineSpacing(0f, 1.3f)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })
            }
        }
    }
    
    private fun createTimelineCard(decoded: DecodedMessage): LinearLayout {
        return createCard(0xFFEDE7F6.toInt(), 0xFF7C4DFF.toInt()) {
            addView(TextView(this@DecodeMessageActivity).apply {
                text = "⏱️ " + getString(currentLanguage, "timeline_title")
                textSize = 16f
                setTextColor(0xFF7C4DFF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            addView(TextView(this@DecodeMessageActivity).apply {
                text = decoded.timeline
                textSize = 15f
                setTextColor(0xFF424242.toInt())
                setPadding(0, dpToPx(8), 0, 0)
            })
        }
    }
    
    private fun createCard(bgColor: Int, accentColor: Int, content: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            elevation = dpToPx(2).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            content()
        }
    }
    
    private fun getString(lang: String, key: String): String {
        val strings = mapOf(
            "title" to mapOf(
                "en" to "🔍 Decode Bank Message",
                "hi" to "🔍 बैंक संदेश डिकोड करें",
                "te" to "🔍 బ్యాంక్ సందేశాన్ని డీకోడ్ చేయండి"
            ),
            "subtitle" to mapOf(
                "en" to "AI-powered explanation in simple language",
                "hi" to "सरल भाषा में AI-संचालित स्पष्टीकरण",
                "te" to "సాధారణ భాషలో AI-శక్తితో వివరణ"
            ),
            "instructions_title" to mapOf(
                "en" to "📱 How to use:",
                "hi" to "📱 उपयोग कैसे करें:",
                "te" to "📱 ఎలా ఉపయోగించాలి:"
            ),
            "instructions_text" to mapOf(
                "en" to "Paste your bank rejection email or SMS below. Our AI will decode the technical jargon and explain what went wrong in simple language, plus give you actionable steps to fix it!",
                "hi" to "नीचे अपना बैंक अस्वीकृति ईमेल या SMS पेस्ट करें। हमारा AI तकनीकी शब्दजाल को डिकोड करेगा और सरल भाषा में समझाएगा कि क्या गलत हुआ, साथ ही इसे ठीक करने के लिए कार्रवाई योग्य कदम देगा!",
                "te" to "క్రింద మీ బ్యాంక్ తిరస్కరణ ఇమెయిల్ లేదా SMS అతికించండి। మా AI సాంకేతిక పదజాలాన్ని డీకోడ్ చేస్తుంది మరియు సాధారణ భాషలో ఏమి తప్పు జరిగిందో వివరిస్తుంది, దానితో పాటు దాన్ని సరిచేయడానికి చర్యలు ఇస్తుంది!"
            ),
            "input_label" to mapOf(
                "en" to "📧 Bank Message:",
                "hi" to "📧 बैंक संदेश:",
                "te" to "📧 బ్యాంక్ సందేశం:"
            ),
            "input_hint" to mapOf(
                "en" to "Paste your rejection email or SMS here...\n\nExample:\n\"Dear Customer, we regret to inform you that your loan application has been declined due to insufficient credit history and high debt-to-income ratio...\"",
                "hi" to "यहां अपना अस्वीकृति ईमेल या SMS पेस्ट करें...\n\nउदाहरण:\n\"प्रिय ग्राहक, हमें आपको सूचित करते हुए खेद है कि अपर्याप्त क्रेडिट इतिहास और उच्च ऋण-से-आय अनुपात के कारण आपका ऋण आवेदन अस्वीकार कर दिया गया है...\"",
                "te" to "ఇక్కడ మీ తిరస్కరణ ఇమెయిల్ లేదా SMS అతికించండి...\n\nఉదాహరణ:\n\"ప్రియమైన కస్టమర్, తగినంత క్రెడిట్ చరిత్ర లేకపోవడం మరియు అధిక అప్పు-ఆదాయ నిష్పత్తి కారణంగా మీ రుణ దరఖాస్తు తిరస్కరించబడిందని మీకు తెలియజేయడానికి మేము చింతిస్తున్నాము...\""
            ),
            "decode_button" to mapOf(
                "en" to "🔍 Decode with AI",
                "hi" to "🔍 AI से डिकोड करें",
                "te" to "🔍 AI తో డీకోడ్ చేయండి"
            ),
            "error_empty" to mapOf(
                "en" to "Please paste a message first",
                "hi" to "कृपया पहले एक संदेश पेस्ट करें",
                "te" to "దయచేసి మొదట ఒక సందేశాన్ని అతికించండి"
            ),
            "error_decode" to mapOf(
                "en" to "Error decoding message. Please try again.",
                "hi" to "संदेश डिकोड करने में त्रुटि। कृपया पुनः प्रयास करें।",
                "te" to "సందేశాన్ని డీకోడ్ చేయడంలో లోపం। దయచేసి మళ్లీ ప్రయత్నించండి."
            ),
            "ai_powered" to mapOf(
                "en" to "AI-Powered Analysis",
                "hi" to "AI-संचालित विश्लेषण",
                "te" to "AI-శక్తితో విశ్లేషణ"
            ),
            "summary_title" to mapOf(
                "en" to "What Happened",
                "hi" to "क्या हुआ",
                "te" to "ఏమి జరిగింది"
            ),
            "severity_title" to mapOf(
                "en" to "Severity:",
                "hi" to "गंभीरता:",
                "te" to "తీవ్రత:"
            ),
            "reason_title" to mapOf(
                "en" to "Main Reason",
                "hi" to "मुख्य कारण",
                "te" to "ప్రధాన కారణం"
            ),
            "factors_title" to mapOf(
                "en" to "Specific Factors",
                "hi" to "विशिष्ट कारक",
                "te" to "నిర్దిష్ట కారకాలు"
            ),
            "actions_title" to mapOf(
                "en" to "What You Can Do",
                "hi" to "आप क्या कर सकते हैं",
                "te" to "మీరు ఏమి చేయవచ్చు"
            ),
            "timeline_title" to mapOf(
                "en" to "Expected Timeline",
                "hi" to "अपेक्षित समयरेखा",
                "te" to "ఊహించిన కాలపరిమితి"
            )
        )
        
        return strings[key]?.get(lang) ?: strings[key]?.get("en") ?: key
    }
    
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

