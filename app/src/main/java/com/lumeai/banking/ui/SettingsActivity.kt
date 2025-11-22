package com.lumeai.banking.ui

import android.content.Context
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
import com.lumeai.banking.repository.AppRepository
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch

/**
 * SettingsActivity - Modern Settings & Notifications UI
 * Consistent design with ProfileActivity
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var scrollView: ScrollView
    private lateinit var contentLayout: LinearLayout
    private var currentLanguage = "en"
    private lateinit var repo: AppRepository
    
    // SharedPreferences for settings
    private val PREFS_NAME = "LumeAIPrefs"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        repo = AppRepository.get(this)
        
        // Modern status bar
        window.statusBarColor = AppTheme.Primary.HeaderBlue
        
        supportActionBar?.hide()
        
        setContentView(createUI())
    }
    
    override fun onBackPressed() {
        finish()
    }
    
    private fun createUI(): FrameLayout {
        // Main container with header on top
        val mainContainer = FrameLayout(this)
        mainContainer.setBackgroundColor(0xFFF8F9FC.toInt())
        
        // Scrollable content
        scrollView = ScrollView(this)
        scrollView.setBackgroundColor(0xFFF8F9FC.toInt())
        
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // Add top padding to prevent content from going under header
        contentLayout.setPadding(0, dp(180), 0, 0)
        
        // Notification Settings Card
        contentLayout.addView(createNotificationSettingsCard())
        
        // App Preferences Card
        contentLayout.addView(createAppPreferencesCard())
        
        // Financial Settings Card
        contentLayout.addView(createFinancialSettingsCard())
        
        // About Section
        contentLayout.addView(createAboutCard())
        
        // Bottom Spacing
        addSpace(40)
        
        scrollView.addView(contentLayout)
        
        // Add scrollView first (background layer)
        val scrollParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollParams
        mainContainer.addView(scrollView)
        
        // Add sticky header on top
        val header = createModernHeader()
        val headerParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        header.layoutParams = headerParams
        mainContainer.addView(header)
        
        return mainContainer
    }
    
    /**
     * Modern gradient header
     */
    private fun createModernHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(24), dp(40), dp(24), dp(24))
            
            // Back button + Title + Language Row
            addView(LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@SettingsActivity).apply {
                    text = "←"
                    textSize = 24f
                    setTextColor(Color.WHITE)
                    setPadding(0, 0, dp(16), 0)
                    setOnClickListener { finish() }
                })
                
                addView(TextView(this@SettingsActivity).apply {
                    text = getString(currentLanguage, "settings_notifications")
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                
                // Language buttons
                val languages = listOf("English" to "en", "हिंदी" to "hi", "తెలుగు" to "te")
                languages.forEach { (name, code) ->
                    addView(createHeaderLanguageButton(name, code))
                }
            })
            
            addSpace(this, 16)
            
            // Subtitle
            addView(TextView(this@SettingsActivity).apply {
                text = getString(currentLanguage, "manage_preferences")
                textSize = 14f
                setTextColor(0xCCFFFFFF.toInt())
            })
        }
    }
    
    /**
     * Notification Settings Card
     */
    private fun createNotificationSettingsCard(): LinearLayout {
        return createModernCard {
            addView(TextView(this@SettingsActivity).apply {
                text = "🔔 " + getString(currentLanguage, "notification_settings")
                textSize = 16f
                setTextColor(0xFF1E293B.toInt())
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Decision Alerts
            addView(createToggleSetting(
                icon = "📬",
                title = getString(currentLanguage, "decision_alerts"),
                subtitle = getString(currentLanguage, "decision_alerts_desc"),
                key = "notif_decisions",
                defaultValue = true,
                prefs = prefs
            ))
            
            addDivider(this)
            
            // Bias Warnings
            addView(createToggleSetting(
                icon = "⚠️",
                title = getString(currentLanguage, "bias_warnings"),
                subtitle = getString(currentLanguage, "bias_warnings_desc"),
                key = "notif_bias",
                defaultValue = true,
                prefs = prefs
            ))
            
            addDivider(this)
            
            // Action Required
            addView(createToggleSetting(
                icon = "⚡",
                title = getString(currentLanguage, "action_required"),
                subtitle = getString(currentLanguage, "action_required_desc"),
                key = "notif_actions",
                defaultValue = true,
                prefs = prefs
            ))
            
            addDivider(this)
            
            // Marketing
            addView(createToggleSetting(
                icon = "📢",
                title = getString(currentLanguage, "marketing"),
                subtitle = getString(currentLanguage, "marketing_desc"),
                key = "notif_marketing",
                defaultValue = false,
                prefs = prefs
            ))
        }
    }
    
    /**
     * App Preferences Card
     */
    private fun createAppPreferencesCard(): LinearLayout {
        return createModernCard {
            addView(TextView(this@SettingsActivity).apply {
                text = "⚙️ " + getString(currentLanguage, "app_preferences")
                textSize = 16f
                setTextColor(0xFF1E293B.toInt())
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Auto-refresh
            addView(createToggleSetting(
                icon = "🔄",
                title = getString(currentLanguage, "auto_refresh"),
                subtitle = getString(currentLanguage, "auto_refresh_desc"),
                key = "auto_refresh",
                defaultValue = true,
                prefs = prefs
            ))
            
            addDivider(this)
            
            // Biometric Lock
            addView(createToggleSetting(
                icon = "🔒",
                title = getString(currentLanguage, "biometric_lock"),
                subtitle = getString(currentLanguage, "biometric_lock_desc"),
                key = "biometric_lock",
                defaultValue = false,
                prefs = prefs
            ))
        }
    }
    
    /**
     * Financial Settings Card
     */
    private fun createFinancialSettingsCard(): LinearLayout {
        return createModernCard {
            addView(TextView(this@SettingsActivity).apply {
                text = "💰 " + getString(currentLanguage, "financial_info")
                textSize = 16f
                setTextColor(0xFF1E293B.toInt())
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(4))
            })
            
            addView(TextView(this@SettingsActivity).apply {
                text = getString(currentLanguage, "financial_info_desc")
                textSize = 12f
                setTextColor(0xFF64748B.toInt())
                setPadding(0, 0, 0, dp(16))
            })
            
            // Credit Score
            addView(createFinancialField(
                icon = "📊",
                label = getString(currentLanguage, "credit_score"),
                hint = "300-900",
                key = "credit_score"
            ))
            
            addSpace(this, 12)
            
            // Monthly Income
            addView(createFinancialField(
                icon = "💵",
                label = getString(currentLanguage, "monthly_income"),
                hint = "₹ 0.00",
                key = "monthly_income"
            ))
            
            addSpace(this, 12)
            
            // Monthly Debt
            addView(createFinancialField(
                icon = "💳",
                label = getString(currentLanguage, "monthly_debt"),
                hint = "₹ 0.00",
                key = "monthly_debt"
            ))
        }
    }
    
    /**
     * About Card
     */
    private fun createAboutCard(): LinearLayout {
        return createModernCard {
            addView(TextView(this@SettingsActivity).apply {
                text = "ℹ️ " + getString(currentLanguage, "about")
                textSize = 16f
                setTextColor(0xFF1E293B.toInt())
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(12))
            })
            
            addView(TextView(this@SettingsActivity).apply {
                text = "LumeAI v1.0.0"
                textSize = 13f
                setTextColor(0xFF334155.toInt())
                setPadding(0, 0, 0, dp(4))
            })
            
            addView(TextView(this@SettingsActivity).apply {
                text = getString(currentLanguage, "about_desc")
                textSize = 12f
                setTextColor(0xFF64748B.toInt())
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    /**
     * Create toggle setting
     */
    private fun createToggleSetting(
        icon: String,
        title: String,
        subtitle: String,
        key: String,
        defaultValue: Boolean,
        prefs: android.content.SharedPreferences
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            
            // Icon
            addView(TextView(this@SettingsActivity).apply {
                text = icon
                textSize = 24f
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = dp(16)
                }
            })
            
            // Text Column
            addView(LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                
                addView(TextView(this@SettingsActivity).apply {
                    text = title
                    textSize = 14f
                    setTextColor(0xFF1E293B.toInt())
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@SettingsActivity).apply {
                    text = subtitle
                    textSize = 12f
                    setTextColor(0xFF64748B.toInt())
                    setPadding(0, dp(2), 0, 0)
                })
            })
            
            // Switch
            addView(Switch(this@SettingsActivity).apply {
                isChecked = prefs.getBoolean(key, defaultValue)
                setOnCheckedChangeListener { _, isChecked ->
                    prefs.edit().putBoolean(key, isChecked).apply()
                }
            })
        }
    }
    
    /**
     * Create financial input field
     */
    private fun createFinancialField(icon: String, label: String, hint: String, key: String): FrameLayout {
        return FrameLayout(this).apply {
            val container = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                val shape = GradientDrawable().apply {
                    cornerRadius = dp(12).toFloat()
                    setColor(Color.WHITE)
                    setStroke(dp(1), 0xFFE2E8F0.toInt())
                }
                background = shape
                setPadding(dp(16), dp(14), dp(16), dp(14))
                
                // Icon
                addView(TextView(this@SettingsActivity).apply {
                    text = icon
                    textSize = 20f
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        rightMargin = dp(12)
                    }
                })
                
                // Label and Input Column
                addView(LinearLayout(this@SettingsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    
                    addView(TextView(this@SettingsActivity).apply {
                        text = label
                        textSize = 11f
                        setTextColor(0xFF64748B.toInt())
                    })
                    
                    // EditText
                    val editText = EditText(this@SettingsActivity).apply {
                        this.hint = hint
                        textSize = 15f
                        setTextColor(0xFF1E293B.toInt())
                        setHintTextColor(0xFF94A3B8.toInt())
                        setPadding(0, dp(4), 0, 0)
                        setBackgroundColor(Color.TRANSPARENT)
                        
                        // Load saved value
                        lifecycleScope.launch {
                            when (key) {
                                "credit_score" -> repo.creditScore.collect { setText(it.toString()) }
                                "monthly_income" -> repo.monthlyIncome.collect { setText("%.2f".format(it)) }
                                "monthly_debt" -> repo.monthlyDebt.collect { setText("%.2f".format(it)) }
                            }
                        }
                        
                        // Save on text change (with debouncing would be better in production)
                        setOnFocusChangeListener { _, hasFocus ->
                            if (!hasFocus) {
                                val value = text.toString()
                                lifecycleScope.launch {
                                    when (key) {
                                        "credit_score" -> {
                                            val cs = value.toIntOrNull()?.coerceIn(300, 900)
                                            if (cs != null) repo.updateProfile(cs, null, null)
                                        }
                                        "monthly_income" -> {
                                            val income = value.toDoubleOrNull()?.coerceAtLeast(0.0)
                                            if (income != null) repo.updateProfile(null, income, null)
                                        }
                                        "monthly_debt" -> {
                                            val debt = value.toDoubleOrNull()?.coerceAtLeast(0.0)
                                            if (debt != null) repo.updateProfile(null, null, debt)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    addView(editText)
                })
            }
            
            addView(container)
        }
    }
    
    /**
     * Create modern card
     */
    private fun createModernCard(content: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(2).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(dp(20), dp(16), dp(20), 0)
            layoutParams = params
            
            content()
        }
    }
    
    /**
     * Add divider
     */
    private fun addDivider(parent: LinearLayout) {
        parent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
            setBackgroundColor(0xFFE2E8F0.toInt())
            val params = layoutParams as LinearLayout.LayoutParams
            params.topMargin = dp(8)
            params.bottomMargin = dp(8)
        })
    }
    
    /**
     * Language button for header
     */
    private fun createHeaderLanguageButton(name: String, code: String): TextView {
        return TextView(this).apply {
            text = name.split(" ").first()
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(4), dp(8), dp(4))
            
            val isSelected = currentLanguage == code
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                if (isSelected) {
                    setColor(0x50FFFFFF.toInt())
                } else {
                    setColor(0x20FFFFFF.toInt())
                }
            }
            background = shape
            
            setTextColor(if (isSelected) Color.WHITE else 0xCCFFFFFF.toInt())
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.leftMargin = dp(6)
            layoutParams = params
            
            setOnClickListener {
                if (currentLanguage != code) {
                    LanguageHelper.setLanguage(this@SettingsActivity, code)
                    recreate()
                }
            }
        }
    }
    
    private fun addSpace(dp: Int) {
        contentLayout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(dp)
            )
        })
    }
    
    private fun addSpace(parent: LinearLayout, dp: Int) {
        parent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (parent.orientation == LinearLayout.HORIZONTAL) dp(dp) else LinearLayout.LayoutParams.MATCH_PARENT,
                if (parent.orientation == LinearLayout.HORIZONTAL) 0 else dp(dp)
            )
        })
    }
    
    private fun dp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    /**
     * Multilingual strings
     */
    private fun getString(lang: String, key: String): String {
        val strings = mapOf(
            "en" to mapOf(
                "settings_notifications" to "Settings",
                "manage_preferences" to "Manage your app preferences and notifications",
                "notification_settings" to "Notification Settings",
                "decision_alerts" to "Decision Alerts",
                "decision_alerts_desc" to "Get notified about loan decisions and updates",
                "bias_warnings" to "Bias Warnings",
                "bias_warnings_desc" to "Alerts when potential bias is detected",
                "action_required" to "Action Required",
                "action_required_desc" to "Important actions needing your attention",
                "marketing" to "Promotional Updates",
                "marketing_desc" to "News, offers, and product updates",
                "app_preferences" to "App Preferences",
                "auto_refresh" to "Auto-refresh Data",
                "auto_refresh_desc" to "Automatically sync latest decisions",
                "biometric_lock" to "Biometric Lock",
                "biometric_lock_desc" to "Use fingerprint/face to unlock app",
                "financial_info" to "Financial Information",
                "financial_info_desc" to "Help us personalize your credit improvement insights",
                "credit_score" to "Credit Score",
                "monthly_income" to "Monthly Income",
                "monthly_debt" to "Monthly Debt Payments",
                "about" to "About",
                "about_desc" to "Making banking AI transparent, fair, and trustworthy. LumeAI explains AI decisions in simple language and detects bias."
            ),
            "hi" to mapOf(
                "settings_notifications" to "सेटिंग्स",
                "manage_preferences" to "अपनी ऐप प्राथमिकताएं और सूचनाएं प्रबंधित करें",
                "notification_settings" to "सूचना सेटिंग्स",
                "decision_alerts" to "निर्णय अलर्ट",
                "decision_alerts_desc" to "ऋण निर्णयों और अपडेट के बारे में सूचित रहें",
                "bias_warnings" to "पूर्वाग्रह चेतावनी",
                "bias_warnings_desc" to "संभावित पूर्वाग्रह का पता चलने पर अलर्ट",
                "action_required" to "कार्रवाई आवश्यक",
                "action_required_desc" to "आपके ध्यान की जरूरत वाली महत्वपूर्ण क्रियाएं",
                "marketing" to "प्रचार अपडेट",
                "marketing_desc" to "समाचार, ऑफ़र और उत्पाद अपडेट",
                "app_preferences" to "ऐप प्राथमिकताएं",
                "auto_refresh" to "ऑटो-रिफ्रेश डेटा",
                "auto_refresh_desc" to "स्वचालित रूप से नवीनतम निर्णय सिंक करें",
                "biometric_lock" to "बायोमेट्रिक लॉक",
                "biometric_lock_desc" to "ऐप अनलॉक करने के लिए फिंगरप्रिंट/फेस का उपयोग करें",
                "financial_info" to "वित्तीय जानकारी",
                "financial_info_desc" to "अपने क्रेडिट सुधार अंतर्दृष्टि को वैयक्तिकृत करने में हमारी मदद करें",
                "credit_score" to "क्रेडिट स्कोर",
                "monthly_income" to "मासिक आय",
                "monthly_debt" to "मासिक ऋण भुगतान",
                "about" to "के बारे में",
                "about_desc" to "बैंकिंग AI को पारदर्शी, निष्पक्ष और भरोसेमंद बनाना। LumeAI सरल भाषा में AI निर्णयों को समझाता है और पूर्वाग्रह का पता लगाता है।"
            ),
            "te" to mapOf(
                "settings_notifications" to "సెట్టింగ్‌లు",
                "manage_preferences" to "మీ యాప్ ప్రాధాన్యతలు మరియు నోటిఫికేషన్‌లను నిర్వహించండి",
                "notification_settings" to "నోటిఫికేషన్ సెట్టింగ్‌లు",
                "decision_alerts" to "నిర్ణయ హెచ్చరికలు",
                "decision_alerts_desc" to "రుణ నిర్ణయాలు మరియు నవీకరణల గురించి తెలియజేయబడండి",
                "bias_warnings" to "పక్షపాత హెచ్చరికలు",
                "bias_warnings_desc" to "సంభావ్య పక్షపాతం గుర్తించబడినప్పుడు హెచ్చరికలు",
                "action_required" to "చర్య అవసరం",
                "action_required_desc" to "మీ శ్రద్ధ అవసరమైన ముఖ్యమైన చర్యలు",
                "marketing" to "ప్రచార నవీకరణలు",
                "marketing_desc" to "వార్తలు, ఆఫర్లు మరియు ఉత్పత్తి నవీకరణలు",
                "app_preferences" to "యాప్ ప్రాధాన్యతలు",
                "auto_refresh" to "ఆటో-రిఫ్రెష్ డేటా",
                "auto_refresh_desc" to "స్వయంచాలకంగా తాజా నిర్ణయాలను సింక్ చేయండి",
                "biometric_lock" to "బయోమెట్రిక్ లాక్",
                "biometric_lock_desc" to "యాప్‌ను అన్‌లాక్ చేయడానికి వేలిముద్ర/ముఖాన్ని ఉపయోగించండి",
                "financial_info" to "ఆర్థిక సమాచారం",
                "financial_info_desc" to "మీ క్రెడిట్ మెరుగుదల అంతర్దృష్టులను వ్యక్తిగతీకరించడంలో మాకు సహాయపడండి",
                "credit_score" to "క్రెడిట్ స్కోర్",
                "monthly_income" to "నెలవారీ ఆదాయం",
                "monthly_debt" to "నెలవారీ రుణ చెల్లింపులు",
                "about" to "గురించి",
                "about_desc" to "బ్యాంకింగ్ AIని పారదర్శకంగా, న్యాయంగా మరియు విశ్వసనీయంగా చేయడం. LumeAI సరళమైన భాషలో AI నిర్ణయాలను వివరిస్తుంది మరియు పక్షపాతాన్ని గుర్తిస్తుంది."
            )
        )
        
        return strings[lang]?.get(key) ?: strings["en"]?.get(key) ?: key
    }
}
