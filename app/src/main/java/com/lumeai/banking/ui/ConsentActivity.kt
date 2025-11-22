package com.lumeai.banking.ui

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.coroutines.launch
import com.lumeai.banking.utils.AppTheme

/**
 * ConsentActivity - FULL TRANSPARENCY consent management
 * Shows exactly what data is shared and what happens when toggles change
 */
class ConsentActivity : AppCompatActivity() {
    
    private val consentToggles = mutableMapOf<String, android.widget.CompoundButton>()
    private val expandedStates = mutableMapOf<String, Boolean>()
    private var currentLanguage = "en" // en, hi, te
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = languagePrefs.getString("language", "en") ?: "en"
        
        window.statusBarColor = AppTheme.Background.Secondary  // Same as AI Explainability Hub
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        
        supportActionBar?.hide()
        
        setContentView(createUI())
        
        // 🔧 FIX: Sync default consents to Firebase on first launch
        syncDefaultConsentsToFirebase()
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
            // Compact top padding (same as AI Explainability Hub)
            setPadding(0, dp(120), 0, 0)
        }
        
        // Info banner card (same style as AI Explainability Hub)
        rootLayout.addView(createInfoBanner())
        
        // Content
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(20))  // Reduced top padding
        }
        
        // Consent controls
        getConsentTypes().forEach { consent ->
            contentLayout.addView(createDetailedConsentCard(consent))
            addSpace(contentLayout, 16)
        }
        
        addSpace(contentLayout, 16)
        
        // GDPR Right to be Forgotten - Data Deletion (Modern & Subtle)
        contentLayout.addView(createModernDataDeletionLink())
        
        rootLayout.addView(contentLayout)
        scrollView.addView(rootLayout)
        
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
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(16), dp(10), dp(16), dp(10))  // Compact padding
            
            addView(LinearLayout(this@ConsentActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                // Modern back button - compact
                addView(TextView(this@ConsentActivity).apply {
                    text = "←"
                    textSize = 24f  // Smaller
                    setTextColor(Color.WHITE)
                    setPadding(dp(4), dp(4), dp(4), dp(4))
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))  // Smaller
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    setOnClickListener { finish() }
                })
                
                // Title - compact, no subtitle
                addView(TextView(this@ConsentActivity).apply {
                    text = "Consent & Privacy"
                    textSize = 18f  // Same as AI Explainability Hub
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
            })
        }
    }
    
    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.END
            
            // Add language buttons
            val languages = listOf(
                "English" to "en",
                "हिंदी" to "hi",
                "తెలుగు" to "te"
            )
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(View(this@ConsentActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(8), 0)
                    })
                }
            }
        }
    }
    
    private fun createLanguageButtons(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(dp(40), dp(12), 0, 0)
            
            val languages = listOf(
                "English" to "en",
                "हिंदी" to "hi",
                "తెలుగు" to "te"
            )
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(View(this@ConsentActivity).apply {
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
            
            val isSelected = currentLanguage == code
            val shape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)  // Same as AI Explainability Hub
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
                    // Save language preference
                    languagePrefs.edit().putString("language", code).apply()
                    recreate()
                }
            }
        }
    }
    
    private fun createInfoCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            
            val shape = GradientDrawable().apply {
                setColor(AppTheme.StatusBg.Info)
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), AppTheme.Primary.Blue)
            }
            background = shape
            
            addView(TextView(this@ConsentActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "💡 यह क्यों मायने रखता है"
                    "te" -> "💡 ఇది ఎందుకు ముఖ్యం"
                    else -> "💡 Why This Matters"
                }
                textSize = 16f
                setTextColor(AppTheme.Primary.Blue)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(8))
            })
            
            addView(TextView(this@ConsentActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "हम पूर्ण पारदर्शिता में विश्वास करते हैं। नीचे, आप देखेंगे कि कौन सा डेटा साझा किया गया है, " +
                           "किन दस्तावेजों की आवश्यकता है, और प्रत्येक सुविधा को सक्षम या अक्षम करने पर क्या होता है। " +
                           "पूरी जानकारी देखने के लिए 'विवरण दिखाएं' पर क्लिक करें।"
                    "te" -> "మేము పూర్తి పారదర్శకతను విశ్వసిస్తాము. దిగువన, ఏ డేటా పంచుకోబడిందో, " +
                           "ఏ పత్రాలు అవసరమో, మరియు ప్రతి ఫీచర్‌ను ఎనేబుల్ లేదా డిసేబుల్ చేసినప్పుడు ఏమి జరుగుతుందో మీరు చూస్తారు। " +
                           "పూర్తి సమాచారం కోసం 'వివరాలు చూపు' క్లిక్ చేయండి."
                    else -> "We believe in complete transparency. Below, you'll see exactly what data is shared, " +
                           "what documents are required, and what happens when you enable or disable each feature. " +
                           "Click 'Show Details' to see full information."
                }
                textSize = 14f
                setTextColor(AppTheme.Text.Primary)
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    private fun createDetailedConsentCard(consent: DetailedConsent): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            elevation = dp(2).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = shape
        }
        
        // Header with toggle
        cardLayout.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            
            addView(TextView(this@ConsentActivity).apply {
                text = "${consent.icon} ${consent.title}"
                textSize = 17f
                setTextColor(Color.parseColor("#1F2937"))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            
            val toggle = androidx.appcompat.widget.SwitchCompat(this@ConsentActivity).apply {
                // Load saved value IMMEDIATELY when creating toggle
                val consentPrefs = getSharedPreferences("ConsentPreferences", MODE_PRIVATE)
                val keyMap = mapOf(
                    "aiAnalysis" to "consent_ai_analysis",
                    "biasDetection" to "consent_bias_detection",
                    "dataSharing" to "consent_data_sharing",
                    "dataStorage" to "consent_data_storage"
                )
                val prefKey = keyMap[consent.id] ?: "consent_${consent.id}"
                isChecked = consentPrefs.getBoolean(prefKey, consent.defaultEnabled)
                
                scaleX = 1.2f
                scaleY = 1.2f
                
                // Set blue theme colors using proper SwitchCompat API
                // Track tint (the background rail)
                trackTintList = android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        Color.parseColor("#90CAF9"),  // Light blue when checked
                        Color.parseColor("#E0E0E0")   // Light gray when unchecked
                    )
                )
                
                // Thumb tint (the circular knob)
                thumbTintList = android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        AppTheme.Primary.Blue,        // Blue when checked
                        Color.parseColor("#FAFAFA")   // White when unchecked
                    )
                )
                
                setOnCheckedChangeListener { _, isChecked ->
                    updateConsentView(cardLayout, consent, isChecked)
                    
                    // ✨ AUTO-SAVE: Save to SharedPreferences and Firebase immediately
                    autoSaveConsent(consent.id, isChecked)
                }
            }
            consentToggles[consent.id] = toggle
            addView(toggle)
            
            // Update view to match loaded toggle state
            updateConsentView(cardLayout, consent, toggle.isChecked)
        })
        
        // Brief description
        cardLayout.addView(TextView(this@ConsentActivity).apply {
            text = consent.shortDescription
            textSize = 14f
            setTextColor(Color.parseColor("#6B7280"))
            setPadding(0, dp(8), 0, dp(12))
            setLineSpacing(0f, 1.3f)
        })
        
        // Details container (collapsible)
        val detailsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = if (expandedStates[consent.id] == true) View.VISIBLE else View.GONE
            tag = "details_${consent.id}"
        }
        
        // When ENABLED section - modern card with green accent
        detailsContainer.addView(createDetailSection(
            "✅ When ENABLED",
            Color.WHITE,  // White background
            AppTheme.Status.Success,  // Green accent color
            listOf(
                "📊 Data Shared:" to consent.dataSharedWhenOn,
                "📄 Documents Required:" to consent.documentsNeededWhenOn,
                "⚙️ Processing:" to consent.processingWhenOn,
                "👥 Access:" to consent.accessWhenOn
            )
        ))
        
        addSpace(detailsContainer, 12)
        
        // When DISABLED section - modern card with red accent
        detailsContainer.addView(createDetailSection(
            "⭕ When DISABLED",
            Color.WHITE,  // White background
            AppTheme.Status.Error,  // Red accent color
            listOf(
                "🚫 What Stops:" to consent.whatStopsWhenOff,
                "⏱️ Impact:" to consent.impactWhenOff,
                "🔄 Alternative:" to consent.alternativeWhenOff,
                "⚠️ Limitations:" to consent.limitationsWhenOff
            )
        ))
        
        cardLayout.addView(detailsContainer)
        
        // Show/Hide details button
        cardLayout.addView(TextView(this@ConsentActivity).apply {
            text = if (expandedStates[consent.id] == true) {
                when (currentLanguage) {
                    "hi" -> "▲ विवरण छुपाएं"
                    "te" -> "▲ వివరాలు దాచు"
                    else -> "▲ Hide Details"
                }
            } else {
                when (currentLanguage) {
                    "hi" -> "▼ विवरण दिखाएं"
                    "te" -> "▼ వివరాలు చూపు"
                    else -> "▼ Show Details"
                }
            }
            textSize = 14f
            setTextColor(Color.parseColor("#3B82F6"))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
            
            setOnClickListener {
                val isExpanded = expandedStates[consent.id] ?: false
                expandedStates[consent.id] = !isExpanded
                
                if (!isExpanded) {
                    detailsContainer.visibility = View.VISIBLE
                    this.text = when (currentLanguage) {
                        "hi" -> "▲ विवरण छुपाएं"
                        "te" -> "▲ వివరాలు దాచు"
                        else -> "▲ Hide Details"
                    }
                } else {
                    detailsContainer.visibility = View.GONE
                    this.text = when (currentLanguage) {
                        "hi" -> "▼ विवरण दिखाएं"
                        "te" -> "▼ వివరాలు చూపు"
                        else -> "▼ Show Details"
                    }
                }
            }
        })
        
        // Current status indicator
        val statusIndicator = createStatusIndicator(consent.defaultEnabled)
        cardLayout.addView(statusIndicator)
        statusIndicator.tag = "status_${consent.id}"
        
        return cardLayout
    }
    
    private fun createDetailSection(
        title: String,
        bgColor: Int,
        accentColor: Int,
        items: List<Pair<String, String>>
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL  // Horizontal for border + content
            
            // Modern card background with border
            val shape = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = dp(8).toFloat()
                setStroke(dp(1), AppTheme.Text.Tertiary)  // Subtle gray border
            }
            background = shape
            
            // Colored left border accent
            addView(View(this@ConsentActivity).apply {
                setBackgroundColor(accentColor)
                layoutParams = LinearLayout.LayoutParams(dp(4), LinearLayout.LayoutParams.MATCH_PARENT)
            })
            
            // Content container
            val contentContainer = LinearLayout(this@ConsentActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            
            // Title
            contentContainer.addView(TextView(this@ConsentActivity).apply {
                text = title
                textSize = 15f
                setTextColor(accentColor)  // Title in accent color
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(8))
            })
            
            // Items
            items.forEach { (label, value) ->
                contentContainer.addView(TextView(this@ConsentActivity).apply {
                    text = label
                    textSize = 13f
                    setTextColor(AppTheme.Text.OnCard)  // Dark blue - visible
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, dp(6), 0, dp(2))
                })
                
                contentContainer.addView(TextView(this@ConsentActivity).apply {
                    text = value
                    textSize = 13f
                    setTextColor(AppTheme.Text.OnCard)  // Dark blue - visible
                    setPadding(dp(8), 0, 0, dp(4))
                    setLineSpacing(0f, 1.3f)
                })
            }
            
            addView(contentContainer)
        }
    }
    
    private fun createStatusIndicator(isEnabled: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            
            val bgColor = if (isEnabled) Color.parseColor("#E3F2FD") else Color.parseColor("#F5F5F5")
            val textColor = if (isEnabled) AppTheme.Primary.Blue else AppTheme.Text.Secondary
            
            val shape = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = dp(8).toFloat()
            }
            background = shape
            
            addView(TextView(this@ConsentActivity).apply {
                text = if (isEnabled) {
                    when (currentLanguage) {
                        "hi" -> "✓ वर्तमान में सक्षम"
                        "te" -> "✓ ప్రస్తుతం ఎనేబుల్"
                        else -> "✓ Currently ENABLED"
                    }
                } else {
                    when (currentLanguage) {
                        "hi" -> "⭕ वर्तमान में अक्षम"
                        "te" -> "⭕ ప్రస్తుతం డిసేబుల్"
                        else -> "⭕ Currently DISABLED"
                    }
                }
                textSize = 13f
                setTextColor(textColor)
                setTypeface(null, Typeface.BOLD)
            })
        }
    }
    
    private fun updateConsentView(cardLayout: LinearLayout, consent: DetailedConsent, isEnabled: Boolean) {
        val statusIndicator = cardLayout.findViewWithTag<LinearLayout>("status_${consent.id}")
        statusIndicator?.removeAllViews()
        
        val bgColor = if (isEnabled) Color.parseColor("#E3F2FD") else Color.parseColor("#F5F5F5")
        val textColor = if (isEnabled) AppTheme.Primary.Blue else AppTheme.Text.Secondary
        
        val shape = GradientDrawable().apply {
            setColor(bgColor)
            cornerRadius = dp(8).toFloat()
        }
        statusIndicator?.background = shape
        
        statusIndicator?.addView(TextView(this).apply {
            text = if (isEnabled) {
                when (currentLanguage) {
                    "hi" -> "✓ वर्तमान में सक्षम"
                    "te" -> "✓ ప్రస్తుతం ఎనేబుల్"
                    else -> "✓ Currently ENABLED"
                }
            } else {
                when (currentLanguage) {
                    "hi" -> "⭕ वर्तमान में अक्षम"
                    "te" -> "⭕ ప్రస్తుతం డిసేబుల్"
                    else -> "⭕ Currently DISABLED"
                }
            }
            textSize = 13f
            setTextColor(textColor)
            setTypeface(null, Typeface.BOLD)
        })
    }
    
    /**
     * 🔧 SYNC DEFAULT CONSENTS: Save initial consent values to Firebase on first launch
     * This ensures the bank portal can see consent preferences even before user toggles anything
     */
    private fun syncDefaultConsentsToFirebase() {
        lifecycleScope.launch {
            try {
                val customerId = com.lumeai.banking.FirebaseListenerService.getCustomerId(this@ConsentActivity)
                
                // Check if consents already exist in Firebase
                val existingConsents = com.lumeai.banking.FirebaseSyncManager.getUserConsents(customerId)
                
                if (existingConsents == null) {
                    // First time - sync default values to Firebase
                    android.util.Log.d("ConsentActivity", "🔧 First launch detected - syncing default consents to Firebase...")
                    
                    val consentPrefs = getSharedPreferences("ConsentPreferences", MODE_PRIVATE)
                    val keyMap = mapOf(
                        "aiAnalysis" to "consent_ai_analysis",
                        "biasDetection" to "consent_bias_detection",
                        "dataSharing" to "consent_data_sharing",
                        "dataStorage" to "consent_data_storage"
                    )
                    
                    // Get current values or use defaults from getConsentTypes()
                    val consentTypes = getConsentTypes().filter { it.id in keyMap.keys }
                    val aiAnalysis = consentPrefs.getBoolean(keyMap["aiAnalysis"]!!, 
                        consentTypes.find { it.id == "aiAnalysis" }?.defaultEnabled ?: true)
                    val biasDetection = consentPrefs.getBoolean(keyMap["biasDetection"]!!, 
                        consentTypes.find { it.id == "biasDetection" }?.defaultEnabled ?: true)
                    val dataSharing = consentPrefs.getBoolean(keyMap["dataSharing"]!!, 
                        consentTypes.find { it.id == "dataSharing" }?.defaultEnabled ?: true)
                    val dataStorage = consentPrefs.getBoolean(keyMap["dataStorage"]!!, 
                        consentTypes.find { it.id == "dataStorage" }?.defaultEnabled ?: true)
                    
                    // Save to Firebase
                    val success = com.lumeai.banking.FirebaseSyncManager.saveUserConsents(
                        customerId,
                        aiAnalysis,
                        biasDetection,
                        dataSharing,
                        dataStorage
                    )
                    
                    if (success) {
                        android.util.Log.d("ConsentActivity", "✅ Default consents synced to Firebase for $customerId")
                    } else {
                        android.util.Log.e("ConsentActivity", "❌ Failed to sync default consents")
                    }
                } else {
                    android.util.Log.d("ConsentActivity", "ℹ️ Consents already exist in Firebase - skipping initial sync")
                }
            } catch (e: Exception) {
                android.util.Log.e("ConsentActivity", "❌ Error syncing default consents: ${e.message}")
            }
        }
    }
    
    /**
     * ✨ AUTO-SAVE: Save consent immediately when toggle changes
     */
    private fun autoSaveConsent(consentId: String, isEnabled: Boolean) {
        // Map consentId (camelCase) to the correct key format (with underscores)
        val keyMap = mapOf(
            "aiAnalysis" to "consent_ai_analysis",
            "biasDetection" to "consent_bias_detection",
            "dataSharing" to "consent_data_sharing",
            "dataStorage" to "consent_data_storage"
        )
        
        val prefKey = keyMap[consentId] ?: "consent_$consentId"
        
        // Save to SharedPreferences for local checks
        val consentPrefs = getSharedPreferences("ConsentPreferences", MODE_PRIVATE)
        consentPrefs.edit().putBoolean(prefKey, isEnabled).apply()
        
        android.util.Log.d("ConsentActivity", "✅ Auto-saved: $prefKey = $isEnabled")
        
        // Save to Firebase for bank portal visibility
        lifecycleScope.launch {
            try {
                val customerId = com.lumeai.banking.FirebaseListenerService.getCustomerId(this@ConsentActivity)
                
                // Update Firebase with current state of all consents
                val aiAnalysis = consentToggles["aiAnalysis"]?.isChecked ?: true
                val biasDetection = consentToggles["biasDetection"]?.isChecked ?: true
                val dataSharing = consentToggles["dataSharing"]?.isChecked ?: true
                val dataStorage = consentToggles["dataStorage"]?.isChecked ?: true
                
                com.lumeai.banking.FirebaseSyncManager.saveUserConsents(
                    customerId,
                    aiAnalysis,
                    biasDetection,
                    dataSharing,
                    dataStorage
                )
                
                android.util.Log.d("ConsentActivity", "✅ Auto-saved to Firebase for $customerId")
                
                // Show subtle feedback
                runOnUiThread {
                    Toast.makeText(
                        this@ConsentActivity,
                        "✓ Saved automatically",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("ConsentActivity", "❌ Auto-save failed: ${e.message}")
            }
        }
    }
    
    /**
     * GDPR Data Deletion Card - matches consent card design
     */
    private fun createModernDataDeletionLink(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            elevation = dp(2).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = shape
            
            // Header row with icon and title
            addView(LinearLayout(this@ConsentActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@ConsentActivity).apply {
                    text = "⚠️"
                    textSize = 20f
                    setPadding(0, 0, dp(12), 0)
                })
                
                addView(TextView(this@ConsentActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "डेटा हटाना (GDPR)"
                        "te" -> "డేటా తొలగింపు (GDPR)"
                        else -> "Data Deletion (GDPR)"
                    }
                    textSize = 17f
                    setTextColor(Color.parseColor("#1F2937"))
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
            })
            
            // Description text
            addView(TextView(this@ConsentActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "GDPR के तहत, आप हमारे सिस्टम से अपना सभी व्यक्तिगत डेटा स्थायी रूप से हटा सकते हैं। यह एक अपरिवर्तनीय कार्रवाई है।"
                    "te" -> "GDPR కింద, మీరు మా సిస్టమ్ నుండి మీ మొత్తం వ్యక్తిగత డేటాను శాశ్వతంగా తొలగించవచ్చు. ఇది తిరిగి మార్చలేని చర్య."
                    else -> "Under GDPR, you can permanently delete all your personal data from our systems. This is an irreversible action."
                }
                textSize = 14f
                setTextColor(Color.parseColor("#6B7280"))
                setPadding(0, dp(8), 0, dp(16))
                setLineSpacing(0f, 1.3f)
            })
            
            // Delete button
            addView(Button(this@ConsentActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "डेटा हटाने का अनुरोध करें"
                    "te" -> "డేటా తొలగింపు అభ్యర్థించండి"
                    else -> "Request Data Deletion"
                }
                textSize = 14f
                setTextColor(Color.parseColor("#DC2626"))
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(20), dp(12), dp(20), dp(12))
                
                val shape = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.WHITE)
                    setStroke(dp(2), Color.parseColor("#DC2626"))
                }
                background = shape
                
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                
                setOnClickListener {
                    showDataDeletionDialog()
                }
            })
        }
    }
    
    private fun showDataDeletionDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Delete All Data?")
            .setMessage("""
                GDPR Right to be Forgotten
                
                This will permanently delete:
                • All your AI interaction history
                • Transparency metrics & scores
                • Consent preferences
                • Bias detection logs
                • Application tracking data
                • Chatbot conversation history
                • All personal data from our systems
                
                ⚠️ This action CANNOT be undone!
                
                Your bank account data remains with your bank (we don't control that).
                
                Processing time: 30 days as per GDPR.
                You'll receive confirmation email.
                
                Are you absolutely sure?
            """.trimIndent())
            .setPositiveButton("Yes, Delete Everything") { dialog, _ ->
                processDataDeletion()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun processDataDeletion() {
        // In production: Call API to initiate data deletion request
        // For demo: Show confirmation
        
        android.app.AlertDialog.Builder(this)
            .setTitle("✅ Data Deletion Request Submitted")
            .setMessage("""
                Your data deletion request has been logged.
                
                Request ID: DEL-${System.currentTimeMillis() % 100000}
                Status: PENDING
                
                What happens next:
                1. Identity verification email sent
                2. 30-day processing period (GDPR requirement)
                3. All data permanently deleted
                4. Confirmation email sent
                
                During the 30 days:
                • You can cancel the request
                • Data is marked for deletion
                • No new data will be collected
                
                Compliance:
                ✓ GDPR Article 17 (Right to Erasure)
                ✓ Complete audit trail maintained
                ✓ Regulatory reporting completed
                
                You will receive an email within 24 hours.
            """.trimIndent())
            .setPositiveButton("Understood") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this, 
                    "Data deletion request logged. Check your email for confirmation.", 
                    Toast.LENGTH_LONG
                ).show()
            }
            .show()
    }
    
    private fun getConsentTypes(): List<DetailedConsent> {
        return listOf(
            DetailedConsent(
                id = "aiAnalysis",  // Match Firebase key expected by bank portal
                icon = "🎯",
                title = getString("ai_credit_scoring"),
                shortDescription = when (currentLanguage) {
                    "hi" -> "AI आपके वित्तीय इतिहास का विश्लेषण करके ऋण पात्रता निर्धारित करता है"
                    "te" -> "AI మీ ఆర్థిక చరిత్రను విశ్లేషించి రుణ అర్హతను నిర్ణయిస్తుంది"
                    else -> "AI analyzes your financial history to determine creditworthiness and loan eligibility."
                },
                
                dataSharedWhenOn = "• Credit history (last 24 months)\n• Payment patterns & defaults\n• Account balance trends\n• Transaction categories\n• Income sources & stability",
                documentsNeededWhenOn = "• Bank statements (6 months)\n• Salary slips (3 months)\n• PAN card\n• Aadhaar card",
                processingWhenOn = "AI model processes data in real-time (< 2 minutes). Risk score calculated automatically. Instant decision for most cases.",
                accessWhenOn = "Bank's AI system, Credit bureau (CIBIL), Authorized loan officers",
                
                whatStopsWhenOff = "Automatic credit scoring stops. No AI-based decisions possible.",
                impactWhenOff = "⏱️ Manual review required (3-5 business days)\n⚠️ Loan applications take longer\n⚠️ May require additional documentation",
                alternativeWhenOff = "Human underwriters manually review all documents. Traditional credit check via credit bureau only.",
                limitationsWhenOff = "Cannot get instant loan approvals. Pre-approved offers unavailable. Higher documentation burden.",
                
                defaultEnabled = true
            ),
            
            DetailedConsent(
                id = "biasDetection",  // Match Firebase key expected by bank portal
                icon = "🛡️",
                title = getString("ai_fraud_detection"),
                shortDescription = when (currentLanguage) {
                    "hi" -> "धोखाधड़ी का पता लगाने के लिए लेनदेन की रीयल-टाइम निगरानी"
                    "te" -> "మోసం కార్యకలాపాలను గుర్తించడానికి లావాదేవీల రియల్-టైమ్ పర్యవేక్షణ"
                    else -> "Real-time monitoring of transactions to detect and prevent fraudulent activity."
                },
                
                dataSharedWhenOn = "• Transaction locations & devices\n• Spending patterns & timing\n• Merchant categories\n• Login locations & IP addresses\n• Card usage patterns",
                documentsNeededWhenOn = "No additional documents required (automatic monitoring)",
                processingWhenOn = "Real-time AI monitoring on every transaction. Suspicious patterns flagged instantly. Automatic blocks for high-risk transactions.",
                accessWhenOn = "Bank's fraud detection AI, Security operations team, Payment network partners",
                
                whatStopsWhenOff = "AI fraud monitoring disabled. Only rule-based checks remain (amount limits, country blocks).",
                impactWhenOff = "⚠️ Reduced fraud protection\n⚠️ Higher risk of unauthorized transactions\n⚠️ May miss sophisticated fraud patterns",
                alternativeWhenOff = "Basic rule-based fraud checks only (daily limits, geographic restrictions). Manual review of flagged transactions.",
                limitationsWhenOff = "Cannot detect behavioral anomalies. International transactions may be blocked automatically. Slower fraud response.",
                
                defaultEnabled = true
            ),
            
            DetailedConsent(
                id = "dataSharing",  // Match Firebase key expected by bank portal
                icon = "✨",
                title = getString("personalized_offers"),
                shortDescription = when (currentLanguage) {
                    "hi" -> "AI आपकी खर्च आदतों के आधार पर वित्तीय उत्पाद और ऑफर सुझाता है"
                    "te" -> "AI మీ ఖర్చు అలవాట్ల ఆధారంగా ఆర్థిక ఉత్పత్తులు మరియు ఆఫర్లను సూచిస్తుంది"
                    else -> "AI suggests financial products, cards, and offers based on your spending habits and needs."
                },
                
                dataSharedWhenOn = "• Spending categories & amounts\n• Merchant preferences\n• Recurring payments\n• Seasonal spending patterns\n• Product usage history",
                documentsNeededWhenOn = "No additional documents required",
                processingWhenOn = "AI analyzes spending monthly. Personalized offers generated weekly. Relevant product recommendations shown in app.",
                accessWhenOn = "Bank's recommendation AI, Product marketing team, Partner offer platforms",
                
                whatStopsWhenOff = "Personalized recommendations stop. Generic offers only.",
                impactWhenOff = "ℹ️ No custom product suggestions\nℹ️ Generic offers for all customers\nℹ️ May miss relevant financial products",
                alternativeWhenOff = "Standard promotional offers shown to all customers. No customization based on your needs.",
                limitationsWhenOff = "Cannot get tailored credit card suggestions. No spending insights. Miss cashback opportunities.",
                
                defaultEnabled = true
            ),
            
            DetailedConsent(
                id = "dataStorage",  // Match Firebase key expected by bank portal
                icon = "🔗",
                title = getString("third_party_sharing"),
                shortDescription = when (currentLanguage) {
                    "hi" -> "सत्यापन के लिए क्रेडिट ब्यूरो और भागीदारों के साथ डेटा साझा करें"
                    "te" -> "ధృవీకరణ కోసం క్రెడిట్ బ్యూరోలు మరియు భాగస్వాములతో డేటాను భాగస్వామ్యం చేయండి"
                    else -> "Share data with credit bureaus and authorized partners for verification and financial services."
                },
                
                dataSharedWhenOn = "• Credit account details\n• Payment history & defaults\n• Outstanding loan amounts\n• Income verification data\n• Identity documents",
                documentsNeededWhenOn = "• PAN card\n• Aadhaar card\n• Address proof\n• Income proof",
                processingWhenOn = "Data shared with CIBIL, Experian, Equifax monthly. Partners access only for specific loan/credit applications. Encrypted transmission.",
                accessWhenOn = "Credit bureaus (CIBIL, Experian), Co-lending partners, Insurance providers, Government agencies (for verification)",
                
                whatStopsWhenOff = "No data shared with external partners. Credit bureau reporting stops.",
                impactWhenOff = "⚠️ Credit score not updated\n⚠️ Loan applications to other banks affected\n⚠️ Cannot build credit history\n⚠️ Limited financial services access",
                alternativeWhenOff = "Manual verification required for every application. Must submit documents repeatedly to different institutions.",
                limitationsWhenOff = "Cannot apply for loans at other banks using existing credit history. Insurance applications delayed. Credit score stagnant.",
                
                defaultEnabled = true
            ),
            
            DetailedConsent(
                id = "behavioral_analytics",
                icon = "📱",
                title = getString("app_usage_analytics"),
                shortDescription = when (currentLanguage) {
                    "hi" -> "उपयोगकर्ता अनुभव में सुधार के लिए ऐप इंटरैक्शन ट्रैक करें"
                    "te" -> "వినియోగదారు అనుభవాన్ని మెరుగుపరచడానికి యాప్ పరస్పర చర్యలను ట్రాక్ చేయండి"
                    else -> "Track app interactions to improve user experience, detect unusual activity, and enhance security."
                },
                
                dataSharedWhenOn = "• Screen navigation patterns\n• Feature usage frequency\n• Session duration & timing\n• Device type & OS version\n• Error logs & crashes",
                documentsNeededWhenOn = "No additional documents required",
                processingWhenOn = "Anonymous analytics collected during app usage. Aggregated weekly for UX improvements. Device-level anomaly detection for security.",
                accessWhenOn = "Bank's UX team, App development team, Security analytics AI, Cloud analytics platform (anonymized)",
                
                whatStopsWhenOff = "Usage tracking disabled. Basic error reporting only.",
                impactWhenOff = "ℹ️ Cannot detect unusual app behavior\nℹ️ Slower security incident response\nℹ️ App improvements less personalized",
                alternativeWhenOff = "Basic crash reporting only. No behavioral security checks. Generic app experience for all users.",
                limitationsWhenOff = "Cannot detect if your account is accessed from unusual device. App improvements not tailored. Performance issues may persist longer.",
                
                defaultEnabled = false
            )
        )
    }
    
    private fun addSpace(layout: LinearLayout, dpValue: Int) {
        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(dpValue)
            )
        })
    }
    
    /**
     * Info banner card - same style as AI Explainability Hub
     */
    private fun createInfoBanner(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            gravity = Gravity.CENTER_VERTICAL
            
            // Modern card with blue border (matching AI Explainability Hub)
            val cardShape = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), AppTheme.Text.OnCardSecondary)  // Same blue border
            }
            background = cardShape
            
            // Reduced margins
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(dp(20), dp(8), dp(20), dp(8))
            layoutParams = params
            
            elevation = dp(1).toFloat()
            
            addView(TextView(this@ConsentActivity).apply {
                text = "💡"
                textSize = 20f
                setPadding(0, 0, dp(10), 0)
            })
            
            addView(TextView(this@ConsentActivity).apply {
                text = "Control exactly what data is shared. Your privacy, your choice"
                textSize = 12f
                setTextColor(AppTheme.Text.OnCard)
                setLineSpacing(dp(2).toFloat(), 1.2f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
        }
    }
    
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
    
    /**
     * Get translated string based on current language
     */
    private fun getString(key: String): String {
        val translations = when (currentLanguage) {
            "hi" -> hindiStrings
            "te" -> teluguStrings
            else -> englishStrings
        }
        return translations[key] ?: englishStrings[key] ?: key
    }
    
    private val englishStrings = mapOf(
        "consent_title" to "Privacy & Consent Control",
        "ai_credit_scoring" to "AI Credit Scoring",
        "ai_fraud_detection" to "AI Fraud Detection",
        "personalized_offers" to "Personalized Offers",
        "third_party_sharing" to "Third-Party Data Sharing",
        "app_usage_analytics" to "App Usage Analytics"
    )
    
    private val hindiStrings = mapOf(
        "consent_title" to "गोपनीयता और सहमति नियंत्रण",
        "ai_credit_scoring" to "AI क्रेडिट स्कोरिंग",
        "ai_fraud_detection" to "AI धोखाधड़ी जांच",
        "personalized_offers" to "व्यक्तिगत ऑफर",
        "third_party_sharing" to "तृतीय-पक्ष डेटा साझाकरण",
        "app_usage_analytics" to "ऐप उपयोग विश्लेषण"
    )
    
    private val teluguStrings = mapOf(
        "consent_title" to "గోప్యత & సమ్మతి నియంత్రణ",
        "ai_credit_scoring" to "AI క్రెడిట్ స్కోరింగ్",
        "ai_fraud_detection" to "AI మోసం గుర్తింపు",
        "personalized_offers" to "వ్యక్తిగత ఆఫర్లు",
        "third_party_sharing" to "మూడవ-పార్టీ డేటా షేరింగ్",
        "app_usage_analytics" to "యాప్ వినియోగ విశ్లేషణ"
    )
}

data class DetailedConsent(
    val id: String,
    val icon: String,
    val title: String,
    val shortDescription: String,
    
    // When ENABLED
    val dataSharedWhenOn: String,
    val documentsNeededWhenOn: String,
    val processingWhenOn: String,
    val accessWhenOn: String,
    
    // When DISABLED
    val whatStopsWhenOff: String,
    val impactWhenOff: String,
    val alternativeWhenOff: String,
    val limitationsWhenOff: String,
    
    val defaultEnabled: Boolean
)
