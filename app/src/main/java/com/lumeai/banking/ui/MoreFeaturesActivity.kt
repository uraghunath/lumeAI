package com.lumeai.banking.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lumeai.banking.utils.AppTheme

/**
 * MoreFeaturesActivity - Advanced features for power users
 */
class MoreFeaturesActivity : AppCompatActivity() {
    
    private var currentLanguage = "en"
    
    private fun getString(key: String): String {
        val translations = mapOf(
            "more_features" to mapOf("en" to "More Features", "hi" to "अधिक सुविधाएँ", "te" to "మరిన్ని ఫీచర్లు"),
            "powerful_tools" to mapOf("en" to "Explore additional capabilities", "hi" to "अतिरिक्त क्षमताओं का अन्वेषण करें", "te" to "అదనపు సామర్థ్యాలను అన్వేషించండి"),
            "advanced_analysis" to mapOf("en" to "📊 Advanced Analysis", "hi" to "📊 उन्नत विश्लेषण", "te" to "📊 అధునాతన విశ్లేషణ"),
            "decode_message" to mapOf("en" to "Decode Message", "hi" to "संदेश डिकोड करें", "te" to "సందేశాన్ని డీకోడ్ చేయండి"),
            "decode_desc" to mapOf("en" to "Decode bank messages using AI to understand what they mean", "hi" to "बैंक संदेशों को समझने के लिए AI का उपयोग करें", "te" to "బ్యాంక్ సందేశాలను అర్థం చేసుకోవడానికి AI ఉపయోగించండి"),
            "education_growth" to mapOf("en" to "📚 Education & Growth", "hi" to "📚 शिक्षा और विकास", "te" to "📚 విద్య మరియు అభివృద్ధి"),
            "financial_literacy" to mapOf("en" to "Financial Literacy", "hi" to "वित्तीय साक्षरता", "te" to "ఆర్థిక అక్షరాస్యత"),
            "financial_desc" to mapOf("en" to "Learn about credit scores, loans, and financial planning", "hi" to "क्रेडिट स्कोर, ऋण और वित्तीय योजना के बारे में जानें", "te" to "క్రెడిట్ స్కోర్లు, రుణాలు మరియు ఆర్థిక ప్రణాళిక గురించి తెలుసుకోండి"),
            "tools_utilities" to mapOf("en" to "🛠️ Tools & Utilities", "hi" to "🛠️ उपकरण और उपयोगिताएँ", "te" to "🛠️ సాధనాలు మరియు యుటిలిటీలు"),
            "ai_chatbot" to mapOf("en" to "AI Chatbot", "hi" to "AI चैटबॉट", "te" to "AI చాట్‌బాట్"),
            "chatbot_desc" to mapOf("en" to "Ask questions about banking and get instant AI-powered answers", "hi" to "बैंकिंग के बारे में प्रश्न पूछें और तुरंत AI उत्तर प्राप्त करें", "te" to "బ్యాంకింగ్ గురించి ప్రశ్నలు అడగండి మరియు తక్షణ AI సమాధానాలు పొందండి"),
            "document_validation" to mapOf("en" to "Document Validation", "hi" to "दस्तावेज़ सत्यापन", "te" to "పత్రం ధృవీకరణ"),
            "document_desc" to mapOf("en" to "Validate your documents using AI for faster loan processing", "hi" to "तेज़ ऋण प्रसंस्करण के लिए AI का उपयोग करके अपने दस्तावेज़ सत्यापित करें", "te" to "వేగవంతమైన రుణ ప్రాసెసింగ్ కోసం AI ఉపయోగించి మీ పత్రాలను ధృవీకరించండి"),
            "compliance" to mapOf("en" to "🔒 Compliance", "hi" to "🔒 अनुपालन", "te" to "🔒 సమ్మతి"),
            "regulatory_compliance" to mapOf("en" to "Regulatory Compliance", "hi" to "नियामक अनुपालन", "te" to "నియంత్రణ సమ్మతి"),
            "regulatory_desc" to mapOf("en" to "Learn how LumeAI ensures RBI, GDPR, and EU AI Act compliance", "hi" to "जानें कि LumeAI कैसे RBI, GDPR और EU AI Act अनुपालन सुनिश्चित करता है", "te" to "LumeAI RBI, GDPR మరియు EU AI చట్టం సమ్మతిని ఎలా నిర్ధారిస్తుందో తెలుసుకోండి")
        )
        return translations[key]?.get(currentLanguage) ?: translations[key]?.get("en") ?: key
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = com.lumeai.banking.utils.LanguageHelper.getCurrentLanguage(this)
        
        // Blue status bar - same as all other pages
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        setContentView(createUI())
    }
    
    private fun createUI(): FrameLayout {
        // Main container with sticky header
        val mainContainer = FrameLayout(this)
        mainContainer.setBackgroundColor(AppTheme.Background.Primary)
        
        // Scrollable content
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(AppTheme.Background.Primary)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
            // Add top padding for sticky header (compact header ~100dp)
            setPadding(0, dp(120), 0, 0)
        }
        
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        
        // Education & Growth Section
        contentLayout.addView(createSectionHeader(getString("education_growth")))
        addSpace(contentLayout, 12)
        
        contentLayout.addView(createFeatureCard(
            "📚",
            getString("financial_literacy"),
            getString("financial_desc")
        ) {
            startActivity(Intent(this, EducationalContentActivity::class.java))
        })
        addSpace(contentLayout, 16)
        
        // Tools & Utilities Section
        contentLayout.addView(createSectionHeader(getString("tools_utilities")))
        addSpace(contentLayout, 12)
        
        contentLayout.addView(createFeatureCard(
            "💬",
            getString("ai_chatbot"),
            getString("chatbot_desc")
        ) {
            startActivity(Intent(this, ChatbotActivity::class.java))
        })
        addSpace(contentLayout, 12)
        
        contentLayout.addView(createFeatureCard(
            "📄",
            getString("document_validation"),
            getString("document_desc")
        ) {
            startActivity(Intent(this, DocumentValidationActivity::class.java))
        })
        addSpace(contentLayout, 16)
        
        // Compliance Section
        contentLayout.addView(createSectionHeader(getString("compliance")))
        addSpace(contentLayout, 12)
        
        contentLayout.addView(createFeatureCard(
            "🔒",
            getString("regulatory_compliance"),
            getString("regulatory_desc")
        ) {
            startActivity(Intent(this, RegulatoryComplianceActivity::class.java))
        })
        addSpace(contentLayout, 16)
        
        // Advanced Analysis Section (moved to bottom)
        contentLayout.addView(createSectionHeader(getString("advanced_analysis")))
        addSpace(contentLayout, 12)
        
        contentLayout.addView(createFeatureCard(
            "📨",
            getString("decode_message"),
            getString("decode_desc")
        ) {
            startActivity(Intent(this, DecodeMessageActivity::class.java))
        })
        addSpace(contentLayout, 30)
        
        rootLayout.addView(contentLayout)
        scrollView.addView(rootLayout)
        
        // Add scrollView first (background) - MATCH_PARENT
        val scrollParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollParams
        mainContainer.addView(scrollView)
        
        // Create sticky header
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
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
            
            // Compact back button
            addView(TextView(this@MoreFeaturesActivity).apply {
                text = "←"
                textSize = 24f
                setTextColor(Color.WHITE)
                setPadding(0, 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                
                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                setBackgroundResource(outValue.resourceId)
                
                setOnClickListener { finish() }
            })
            
            // Title only (no subtitle)
            addView(TextView(this@MoreFeaturesActivity).apply {
                text = getString("more_features")
                textSize = 18f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
            })
        }
    }
    
    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            gravity = Gravity.END
            elevation = dp(2).toFloat()
            
            addView(createLanguageButton("English", "en"))
            addView(android.widget.Space(this@MoreFeaturesActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 0)
            })
            addView(createLanguageButton("हिंदी", "hi"))
            addView(android.widget.Space(this@MoreFeaturesActivity).apply {
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
                    com.lumeai.banking.utils.LanguageHelper.setLanguage(this@MoreFeaturesActivity, code)
                    recreate()
                }
            }
        }
    }
    
    private fun createSectionHeader(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(AppTheme.Text.OnCard)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 0)
        }
    }
    
    private fun createFeatureCard(
        icon: String,
        title: String,
        description: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(2).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), 0xFFE0E0E0.toInt())
            }
            background = shape
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
            gravity = Gravity.CENTER_VERTICAL
            
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            
            // Icon circle background
            addView(LinearLayout(this@MoreFeaturesActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, 0, dp(16), 0)
                
                val iconBg = GradientDrawable().apply {
                    setShape(GradientDrawable.OVAL)
                    setColor(0xFFF0F4FF.toInt())
                }
                
                addView(TextView(this@MoreFeaturesActivity).apply {
                    text = icon
                    textSize = 28f
                    gravity = Gravity.CENTER
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
                    background = iconBg
                })
            })
            
            addView(LinearLayout(this@MoreFeaturesActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                
                addView(TextView(this@MoreFeaturesActivity).apply {
                    text = title
                    textSize = 16f
                    setTextColor(AppTheme.Text.OnCard)
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@MoreFeaturesActivity).apply {
                    text = description
                    textSize = 13f
                    setTextColor(AppTheme.Text.OnCardSecondary)
                    setPadding(0, dp(6), 0, 0)
                    setLineSpacing(0f, 1.4f)
                })
            })
            
            addView(TextView(this@MoreFeaturesActivity).apply {
                text = "›"
                textSize = 32f
                setTextColor(AppTheme.Text.OnCardSecondary)
                setPadding(dp(12), 0, 0, 0)
            })
        }
    }
    
    private fun addSpace(parent: LinearLayout, dp: Int) {
        parent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(dp)
            )
        })
    }
    
    private fun dp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

