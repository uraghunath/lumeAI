package com.lumeai.banking.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lumeai.banking.DecisionManager
import com.lumeai.banking.FirebaseListenerService
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch

/**
 * ProfileActivity - Modern User Profile Management
 * World-class UX inspired by Revolut, N26, PayPal
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView
    private lateinit var contentLayout: LinearLayout
    private var currentLanguage = "en"
    private lateinit var headerNameTextView: TextView // Reference to update header name
    
    // SharedPreferences keys
    private val PREFS_NAME = "LumeAIPrefs"
    private val KEY_NAME = "user_name"
    private val KEY_MOBILE = "user_mobile"
    private val KEY_EMAIL = "user_email"
    private val KEY_AADHAR = "user_aadhar"
    private val KEY_PAN = "user_pan"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        
        // Navy blue status bar matching app theme
        window.statusBarColor = AppTheme.Background.Secondary
        
        supportActionBar?.hide()
        
        setContentView(createUI())
        
        // Initialize default KYC data if not already set
        initializeDefaultKYCData()
        
        // Sync existing profile to Firebase (if name exists)
        syncProfileToFirebase()
    }
    
    /**
     * Initialize default KYC data for demo purposes
     * Prepopulates Aadhaar and PAN if not already set
     */
    private fun initializeDefaultKYCData() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        // Set default Aadhaar number if not present (12 digits in XXXX XXXX XXXX format)
        if (prefs.getString(KEY_AADHAR, "").isNullOrEmpty()) {
            editor.putString(KEY_AADHAR, "2345 6789 0123")
        }
        
        // Set default PAN number if not present (Format: ABCDE1234F)
        // Format: First 3 letters + P (Person) + First letter of surname + 4 digits + Check letter
        if (prefs.getString(KEY_PAN, "").isNullOrEmpty()) {
            editor.putString(KEY_PAN, "AVMPK7890L")
        }
        
        // Set default mobile if not present
        if (prefs.getString(KEY_MOBILE, "").isNullOrEmpty()) {
            editor.putString(KEY_MOBILE, "+91 98765 43210")
        }
        
        // Set default email if not present
        if (prefs.getString(KEY_EMAIL, "").isNullOrEmpty()) {
            editor.putString(KEY_EMAIL, "user@example.com")
        }
        
        editor.apply()
        android.util.Log.d("ProfileActivity", "✅ Default KYC data initialized")
    }
    
    /**
     * Sync user profile to Firebase so bank portal can access it
     */
    private fun syncProfileToFirebase() {
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val name = prefs.getString(KEY_NAME, "") ?: ""
                val email = prefs.getString(KEY_EMAIL, "") ?: ""
                val mobile = prefs.getString(KEY_MOBILE, "") ?: ""
                
                if (name.isNotEmpty()) {
                    val customerId = FirebaseListenerService.getCustomerId(this@ProfileActivity)
                    com.lumeai.banking.FirebaseSyncManager.saveUserProfile(
                        customerId,
                        name,
                        email,
                        mobile
                    )
                    android.util.Log.d("ProfileActivity", "✅ Profile synced to Firebase")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileActivity", "Failed to sync profile: ${e.message}")
            }
        }
    }
    
    override fun onBackPressed() {
        finish()
    }
    
    private fun createUI(): FrameLayout {
        // Main container with header on top
        val mainContainer = FrameLayout(this)
        mainContainer.setBackgroundColor(AppTheme.Background.Primary)
        
        // Scrollable content
        scrollView = ScrollView(this)
        scrollView.setBackgroundColor(AppTheme.Background.Primary)
        
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // Add top padding to prevent content from going under header (increased)
        contentLayout.setPadding(0, dp(240), 0, 0)
        
        // Customer ID Section
        contentLayout.addView(createCustomerIdSection())
        
        // Personal Details
        contentLayout.addView(createPersonalDetailsSection())
        
        // KYC Details
        contentLayout.addView(createKYCSection())
        
        // Quick Actions
        contentLayout.addView(createQuickActionsSection())
        
        // Bottom Spacing
        addSpace(40)
        
        scrollView.addView(contentLayout)
        
        // Add scrollView first (background layer) - MATCH_PARENT for both dimensions
        val scrollParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollParams
        mainContainer.addView(scrollView)
        
        // Add sticky header on top (foreground layer) - WRAP_CONTENT height
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
     * Modern gradient header with avatar and profile info
     */
    private fun createModernHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(24), dp(40), dp(24), dp(24))
            
            // Back button + Title + Language Row
            addView(LinearLayout(this@ProfileActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@ProfileActivity).apply {
                    text = "←"
                    textSize = 24f
                    setTextColor(Color.WHITE)
                    setPadding(0, 0, dp(16), 0)
                    setOnClickListener { finish() }
                })
                
                addView(TextView(this@ProfileActivity).apply {
                    text = getString(currentLanguage, "my_profile")
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
            
            addSpace(this, 24)
            
            // Profile Card
            addView(LinearLayout(this@ProfileActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                val cardShape = GradientDrawable().apply {
                    cornerRadius = dp(20).toFloat()
                    setColor(0x30FFFFFF.toInt())
                }
                background = cardShape
                setPadding(dp(20), dp(20), dp(20), dp(20))
                
                // Avatar
                addView(LinearLayout(this@ProfileActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    
                    val avatarShape = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(0xFFFFFFFF.toInt())
                    }
                    background = avatarShape
                    layoutParams = LinearLayout.LayoutParams(dp(70), dp(70))
                    
                    addView(TextView(this@ProfileActivity).apply {
                        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val userName = prefs.getString(KEY_NAME, "User") ?: "User"
                        text = userName.firstOrNull()?.toString()?.uppercase() ?: "U"
                        textSize = 32f
                        setTextColor(AppTheme.Text.OnCard)  // Blue theme
                        setTypeface(null, Typeface.BOLD)
                        gravity = Gravity.CENTER
                    })
                })
                
                addSpace(this, 16)
                
                // Info Column
                addView(LinearLayout(this@ProfileActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val userName = prefs.getString(KEY_NAME, "") ?: ""
                    
                    // Store reference to update later
                    headerNameTextView = TextView(this@ProfileActivity).apply {
                        text = if (userName.isEmpty()) getString(currentLanguage, "guest_user") else userName
                        textSize = 20f
                        setTextColor(Color.WHITE)
                        setTypeface(null, Typeface.BOLD)
                    }
                    addView(headerNameTextView)
                    
                    addView(TextView(this@ProfileActivity).apply {
                        text = getString(currentLanguage, "lumeai_customer")
                        textSize = 13f
                        setTextColor(0xCCFFFFFF.toInt())
                        setPadding(0, dp(4), 0, 0)
                    })
                    
                    addSpace(this, 8)
                    
                    // Verification Badge
                    addView(LinearLayout(this@ProfileActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        
                        val badgeShape = GradientDrawable().apply {
                            cornerRadius = dp(12).toFloat()
                            setColor(0x40FFFFFF.toInt())
                        }
                        background = badgeShape
                        setPadding(dp(8), dp(4), dp(8), dp(4))
                        
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        
                        addView(TextView(this@ProfileActivity).apply {
                            text = "✓"
                            textSize = 12f
                            setTextColor(0xFF4ADE80.toInt())
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, 0, dp(4), 0)
                        })
                        
                        addView(TextView(this@ProfileActivity).apply {
                            text = getString(currentLanguage, "verified")
                            textSize = 11f
                            setTextColor(Color.WHITE)
                            setTypeface(null, Typeface.BOLD)
                        })
                    })
                })
            })
        }
    }
    
    /**
     * Language button for header
     */
    private fun createHeaderLanguageButton(name: String, code: String): TextView {
        return TextView(this).apply {
            text = name.split(" ").first() // Short name
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
                    LanguageHelper.setLanguage(this@ProfileActivity, code)
                    recreate()
                }
            }
        }
    }
    
    /**
     * Customer ID section
     */
    private fun createCustomerIdSection(): LinearLayout {
        return createModernCard {
            addView(LinearLayout(this@ProfileActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@ProfileActivity).apply {
                    text = "🔑"
                    textSize = 20f
                    setPadding(0, 0, dp(12), 0)
                })
                
                addView(TextView(this@ProfileActivity).apply {
                    text = getString(currentLanguage, "customer_id")
                    textSize = 15f
                    setTextColor(0xFF1E293B.toInt())
                    setTypeface(null, Typeface.BOLD)
                })
            })
            
            addSpace(this, 12)
            
            // ID Display
            val customerId = FirebaseListenerService.getCustomerId(this@ProfileActivity)
            addView(LinearLayout(this@ProfileActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                val cardShape = GradientDrawable().apply {
                    cornerRadius = dp(12).toFloat()
                    setColor(AppTheme.Cards.SurfaceLight)
                    setStroke(dp(1), 0xFFE2E8F0.toInt())
                }
                background = cardShape
                setPadding(dp(16), dp(14), dp(16), dp(14))
                
                addView(TextView(this@ProfileActivity).apply {
                    text = customerId
                    textSize = 13f
                    setTextColor(AppTheme.Text.OnCard)
                    setTypeface(typeface, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                
                addView(TextView(this@ProfileActivity).apply {
                    text = "📋"
                    textSize = 18f
                    setPadding(dp(8), 0, 0, 0)
                    setOnClickListener {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Customer ID", customerId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this@ProfileActivity, getString(currentLanguage, "id_copied"), Toast.LENGTH_SHORT).show()
                    }
                })
            })
            
            addSpace(this, 8)
            
            addView(TextView(this@ProfileActivity).apply {
                text = "💡 " + getString(currentLanguage, "id_info")
                textSize = 11f
                setTextColor(0xFF64748B.toInt())
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    /**
     * Personal details section
     */
    private fun createPersonalDetailsSection(): LinearLayout {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        return createModernCard {
            addView(TextView(this@ProfileActivity).apply {
                text = "👤 " + getString(currentLanguage, "personal_details")
                textSize = 16f
                setTextColor(AppTheme.Text.OnCard)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            addView(createModernField(
                icon = "👤",
                label = getString(currentLanguage, "full_name"),
                value = prefs.getString(KEY_NAME, "") ?: "",
                hint = getString(currentLanguage, "enter_name"),
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                key = KEY_NAME
            ))
            
            addSpace(this, 12)
            
            addView(createModernField(
                icon = "📱",
                label = getString(currentLanguage, "mobile"),
                value = prefs.getString(KEY_MOBILE, "") ?: "",
                hint = getString(currentLanguage, "enter_mobile"),
                inputType = InputType.TYPE_CLASS_PHONE,
                key = KEY_MOBILE
            ))
            
            addSpace(this, 12)
            
            addView(createModernField(
                icon = "📧",
                label = getString(currentLanguage, "email"),
                value = prefs.getString(KEY_EMAIL, "") ?: "",
                hint = getString(currentLanguage, "enter_email"),
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                key = KEY_EMAIL
            ))
        }
    }
    
    /**
     * KYC section
     */
    private fun createKYCSection(): LinearLayout {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        return createModernCard {
            addView(LinearLayout(this@ProfileActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@ProfileActivity).apply {
                    text = "🔐 " + getString(currentLanguage, "kyc_details")
                    textSize = 16f
                    setTextColor(AppTheme.Text.OnCard)
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                
                // Verification badge
                addView(TextView(this@ProfileActivity).apply {
                    text = "✓ " + getString(currentLanguage, "verified")
                    textSize = 11f
                    setTextColor(0xFF10B981.toInt())
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    
                    val badgeShape = GradientDrawable().apply {
                        cornerRadius = dp(8).toFloat()
                        setColor(0x2010B981.toInt())
                    }
                    background = badgeShape
                    setPadding(dp(8), dp(4), dp(8), dp(4))
                })
            })
            
            addSpace(this, 16)
            
            addView(createModernField(
                icon = "🆔",
                label = getString(currentLanguage, "aadhar"),
                value = prefs.getString(KEY_AADHAR, "") ?: "",
                hint = "XXXX XXXX XXXX",
                inputType = InputType.TYPE_CLASS_NUMBER,
                key = KEY_AADHAR
            ))
            
            addSpace(this, 12)
            
            addView(createModernField(
                icon = "💳",
                label = getString(currentLanguage, "pan"),
                value = prefs.getString(KEY_PAN, "") ?: "",
                hint = "ABCDE1234F",
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
                key = KEY_PAN
            ))
        }
    }
    
    /**
     * Quick actions section
     */
    private fun createQuickActionsSection(): LinearLayout {
        return createModernCard {
            addView(TextView(this@ProfileActivity).apply {
                text = "⚡ " + getString(currentLanguage, "quick_actions")
                textSize = 16f
                setTextColor(AppTheme.Text.OnCard)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            val actions = listOf(
                Triple("🔔", getString(currentLanguage, "notifications"), ""),
                Triple("🛡️", getString(currentLanguage, "security"), ""),
                Triple("❓", getString(currentLanguage, "help_support"), "")
            )
            
            actions.forEachIndexed { index, (icon, label, _) ->
                if (index > 0) {
                    addView(View(this@ProfileActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(1)
                        )
                        setBackgroundColor(0xFFE2E8F0.toInt())
                        val params = layoutParams as LinearLayout.LayoutParams
                        params.topMargin = dp(12)
                        params.bottomMargin = dp(12)
                    })
                }
                
                addView(LinearLayout(this@ProfileActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(8), 0, dp(8))
                    
                    addView(TextView(this@ProfileActivity).apply {
                        text = icon
                        textSize = 20f
                        setPadding(0, 0, dp(12), 0)
                    })
                    
                    addView(TextView(this@ProfileActivity).apply {
                        text = label
                        textSize = 14f
                        setTextColor(AppTheme.Text.OnCard)
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    
                    addView(TextView(this@ProfileActivity).apply {
                        text = "›"
                        textSize = 20f
                        setTextColor(0xFF94A3B8.toInt())
                    })
                    
                    setOnClickListener {
                        when (label) {
                            getString(currentLanguage, "notifications") -> {
                                // Open Settings activity (where we can manage notifications)
                                startActivity(Intent(this@ProfileActivity, SettingsActivity::class.java))
                            }
                            getString(currentLanguage, "security") -> {
                                // Open Security & Privacy activity
                                startActivity(Intent(this@ProfileActivity, SecurityPrivacyActivity::class.java))
                            }
                            getString(currentLanguage, "help_support") -> {
                                // Open AI Assistant Chatbot for help
                                startActivity(Intent(this@ProfileActivity, ChatbotActivity::class.java))
                            }
                        }
                    }
                })
            }
        }
    }
    
    /**
     * Modern field with direct inline editing (no dialog)
     */
    private fun createModernField(
        icon: String,
        label: String,
        value: String,
        hint: String,
        inputType: Int,
        key: String
    ): FrameLayout {
        return FrameLayout(this).apply {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Container with modern styling
            val container = LinearLayout(this@ProfileActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
            val shape = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(AppTheme.Cards.Surface)
                setStroke(dp(1), 0xFFE2E8F0.toInt())
            }
                background = shape
                setPadding(dp(16), dp(14), dp(16), dp(14))
                
                // Icon
                addView(TextView(this@ProfileActivity).apply {
                    text = icon
                    textSize = 20f
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        rightMargin = dp(12)
                    }
                })
                
                // Value Column
                addView(LinearLayout(this@ProfileActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    
                    addView(TextView(this@ProfileActivity).apply {
                        text = label
                        textSize = 11f
                        setTextColor(0xFF64748B.toInt())
                    })
                    
                    // EditText for direct editing
                    val editText = EditText(this@ProfileActivity).apply {
                        this.inputType = inputType
                        this.hint = hint
                        setText(value)
                        textSize = 15f
                        setTextColor(AppTheme.Text.OnCard)
                        setHintTextColor(0xFF94A3B8.toInt())
                        setPadding(0, dp(4), 0, 0)
                        setBackgroundColor(Color.TRANSPARENT)
                        
                        // Save on text change
                        addTextChangedListener(object : android.text.TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: android.text.Editable?) {
                                val newValue = s?.toString()?.trim() ?: ""
                                prefs.edit().putString(key, newValue).apply()
                                
                                // Sync to Firebase if name, email, or mobile changed
                                if (key == KEY_NAME || key == KEY_EMAIL || key == KEY_MOBILE) {
                                    lifecycleScope.launch {
                                        try {
                                            val customerId = FirebaseListenerService.getCustomerId(this@ProfileActivity)
                                            val name = prefs.getString(KEY_NAME, "") ?: ""
                                            val email = prefs.getString(KEY_EMAIL, "") ?: ""
                                            val mobile = prefs.getString(KEY_MOBILE, "") ?: ""
                                            
                                            if (name.isNotEmpty()) {
                                                com.lumeai.banking.FirebaseSyncManager.saveUserProfile(
                                                    customerId,
                                                    name,
                                                    email,
                                                    mobile
                                                )
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("ProfileActivity", "Failed to sync profile: ${e.message}")
                                        }
                                    }
                                }
                            }
                        })
                        
                        // Update header name when focus is lost (cursor moves out)
                        if (key == KEY_NAME) {
                            setOnFocusChangeListener { _, hasFocus ->
                                if (!hasFocus) {
                                    val newName = text.toString().trim()
                                    if (::headerNameTextView.isInitialized) {
                                        headerNameTextView.text = if (newName.isEmpty()) {
                                            getString(currentLanguage, "guest_user")
                                        } else {
                                            newName
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
            setBackgroundColor(AppTheme.Cards.Surface)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(4).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(AppTheme.CornerRadius).toFloat()
                setColor(AppTheme.Cards.Surface)
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
                "my_profile" to "My Profile",
                "guest_user" to "Guest User",
                "lumeai_customer" to "LumeAI Customer",
                "verified" to "Verified",
                "profile_strength" to "Profile Strength",
                "complete" to "Complete",
                "decisions" to "Decisions",
                "pending" to "Pending",
                "approved" to "Approved",
                "app_language" to "App Language",
                "customer_id" to "Lume ID",
                "id_copied" to "ID copied to clipboard",
                "id_info" to "Use this ID in the bank portal to receive real-time decision alerts",
                "personal_details" to "Personal Details",
                "full_name" to "Full Name",
                "enter_name" to "Enter your full name",
                "mobile" to "Mobile Number",
                "enter_mobile" to "Enter your mobile number",
                "email" to "Email Address",
                "enter_email" to "Enter your email",
                "kyc_details" to "KYC Details",
                "aadhar" to "Aadhar Number",
                "pan" to "PAN Number",
                "quick_actions" to "Quick Actions",
                "notifications" to "Settings",
                "security" to "Security & Privacy",
                "help_support" to "Help & Support",
                "coming_soon" to "coming soon!",
                "edit" to "Edit",
                "save" to "Save",
                "cancel" to "Cancel",
                "updated" to "Updated successfully!"
            ),
            "hi" to mapOf(
                "my_profile" to "मेरी प्रोफ़ाइल",
                "guest_user" to "अतिथि उपयोगकर्ता",
                "lumeai_customer" to "LumeAI ग्राहक",
                "verified" to "सत्यापित",
                "profile_strength" to "प्रोफ़ाइल शक्ति",
                "complete" to "पूर्ण",
                "decisions" to "निर्णय",
                "pending" to "लंबित",
                "approved" to "स्वीकृत",
                "app_language" to "ऐप भाषा",
                "customer_id" to "Lume ID",
                "id_copied" to "आईडी कॉपी हो गई",
                "id_info" to "रीयल-टाइम निर्णय अलर्ट प्राप्त करने के लिए बैंक पोर्टल में इस आईडी का उपयोग करें",
                "personal_details" to "व्यक्तिगत विवरण",
                "full_name" to "पूरा नाम",
                "enter_name" to "अपना पूरा नाम दर्ज करें",
                "mobile" to "मोबाइल नंबर",
                "enter_mobile" to "अपना मोबाइल नंबर दर्ज करें",
                "email" to "ईमेल पता",
                "enter_email" to "अपना ईमेल दर्ज करें",
                "kyc_details" to "KYC विवरण",
                "aadhar" to "आधार नंबर",
                "pan" to "पैन नंबर",
                "quick_actions" to "त्वरित क्रियाएं",
                "notifications" to "सेटिंग्स",
                "security" to "सुरक्षा और गोपनीयता",
                "help_support" to "सहायता और समर्थन",
                "coming_soon" to "जल्द आ रहा है!",
                "edit" to "संपादित करें",
                "save" to "सहेजें",
                "cancel" to "रद्द करें",
                "updated" to "सफलतापूर्वक अपडेट किया गया!"
            ),
            "te" to mapOf(
                "my_profile" to "నా ప్రొఫైల్",
                "guest_user" to "అతిథి వినియోగదారు",
                "lumeai_customer" to "LumeAI కస్టమర్",
                "verified" to "ధృవీకరించబడింది",
                "profile_strength" to "ప్రొఫైల్ బలం",
                "complete" to "పూర్తి",
                "decisions" to "నిర్ణయాలు",
                "pending" to "పెండింగ్",
                "approved" to "ఆమోదించబడింది",
                "app_language" to "యాప్ భాష",
                "customer_id" to "Lume ID",
                "id_copied" to "ID కాపీ చేయబడింది",
                "id_info" to "రియల్-టైమ్ నిర్ణయ హెచ్చరికలను స్వీకరించడానికి బ్యాంక్ పోర్టల్‌లో ఈ IDని ఉపయోగించండి",
                "personal_details" to "వ్యక్తిగత వివరాలు",
                "full_name" to "పూర్తి పేరు",
                "enter_name" to "మీ పూర్తి పేరు నమోదు చేయండి",
                "mobile" to "మొబైల్ నంబర్",
                "enter_mobile" to "మీ మొబైల్ నంబర్ నమోదు చేయండి",
                "email" to "ఇమెయిల్ చిరునామా",
                "enter_email" to "మీ ఇమెయిల్ నమోదు చేయండి",
                "kyc_details" to "KYC వివరాలు",
                "aadhar" to "ఆధార్ నంబర్",
                "pan" to "పాన్ నంబర్",
                "quick_actions" to "త్వరిత చర్యలు",
                "notifications" to "సెట్టింగ్‌లు",
                "security" to "భద్రత మరియు గోప్యత",
                "help_support" to "సహాయం మరియు మద్దతు",
                "coming_soon" to "త్వరలో వస్తోంది!",
                "edit" to "సవరించు",
                "save" to "సేవ్ చేయండి",
                "cancel" to "రద్దు చేయండి",
                "updated" to "విజయవంతంగా నవీకరించబడింది!"
            )
        )
        
        return strings[lang]?.get(key) ?: strings["en"]?.get(key) ?: key
    }
}

