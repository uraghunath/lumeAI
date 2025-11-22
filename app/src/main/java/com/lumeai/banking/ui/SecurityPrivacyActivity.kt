package com.lumeai.banking.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.lumeai.banking.utils.AppTheme

/**
 * SecurityPrivacyActivity - Security and privacy center tailored for Lume AI
 * 
 * Features relevant to this app:
 * - User ID display and management
 * - Data sharing controls (with banks, for AI analysis)
 * - Privacy transparency (how AI uses data)
 * - Data deletion requests
 * - Privacy policy and terms
 * 
 * Note: No login/logout system - uses auto-generated User ID
 */
class SecurityPrivacyActivity : AppCompatActivity() {
    
    private var currentLanguage = "en"
    private lateinit var customerId: String
    
    // SharedPreferences for settings
    private val PREFS_NAME = "LumeAIPrefs"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        
        // Get customer ID
        customerId = com.lumeai.banking.FirebaseListenerService.getCustomerId(this)
        
        window.statusBarColor = AppTheme.Primary.HeaderBlue
        supportActionBar?.hide()
        
        setContentView(createUI())
    }
    
    private fun createUI(): FrameLayout {
        val mainContainer = FrameLayout(this)
        mainContainer.setBackgroundColor(Color.parseColor("#F5F7FA"))
        
        // Scrollable content
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(Color.parseColor("#F5F7FA"))
        
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F7FA"))
            // Add top padding for sticky header
            setPadding(0, dp(145), 0, 0)
        }
        
        // Main content
        val mainContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        
        // Overview card
        mainContent.addView(createOverviewCard())
        addSpace(mainContent, 16)
        
        // Your Lume ID section
        mainContent.addView(createSectionHeader("🆔", getString("your_lume_id")))
        mainContent.addView(createLumeIDCard())
        addSpace(mainContent, 16)
        
        // Data & Privacy section
        mainContent.addView(createSectionHeader("🔐", getString("data_privacy")))
        mainContent.addView(createDataSharingCard())
        addSpace(mainContent, 8)
        mainContent.addView(createAIDataUsageCard())
        addSpace(mainContent, 16)
        
        // Your Rights section
        mainContent.addView(createSectionHeader("⚖️", getString("your_rights")))
        mainContent.addView(createDataRightsCard())
        addSpace(mainContent, 16)
        
        // Legal & Compliance section
        mainContent.addView(createSectionHeader("📄", getString("legal_compliance")))
        mainContent.addView(createLegalCard())
        
        addSpace(mainContent, 30)
        
        contentLayout.addView(mainContent)
        scrollView.addView(contentLayout)
        
        // Add scrollView first (background)
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
            setPadding(dp(16), dp(20), dp(16), dp(20))
            gravity = Gravity.CENTER_VERTICAL
            
            // Back button
            addView(ImageView(this@SecurityPrivacyActivity).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                    rightMargin = dp(16)
                }
                setOnClickListener { finish() }
            })
            
            // Title
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = getString("security_privacy")
                textSize = 20f
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
            gravity = Gravity.END
            elevation = dp(2).toFloat()
            
            addView(createLanguageButton("English", "en"))
            addView(Space(this@SecurityPrivacyActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), 0)
            })
            addView(createLanguageButton("हिंदी", "hi"))
            addView(Space(this@SecurityPrivacyActivity).apply {
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
                    setColor(AppTheme.Primary.Blue)
                } else {
                    setColor(Color.WHITE)
                    setStroke(dp(2), Color.parseColor("#D1D5DB"))
                }
            }
            background = shape
            setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#374151"))
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
    
    private fun createOverviewCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#E3F2FD"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), Color.parseColor("#2196F3"))
            }
            background = shape
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = "🔒 " + getString("security_status")
                textSize = 16f
                setTextColor(Color.parseColor("#1976D2"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(8))
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = getString("security_status_desc")
                textSize = 14f
                setTextColor(Color.parseColor("#424242"))
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    private fun createSectionHeader(icon: String, title: String): TextView {
        return TextView(this).apply {
            text = "$icon $title"
            textSize = 18f
            setTextColor(Color.parseColor("#0A0A0A"))
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(4), 0, 0, dp(12))
        }
    }
    
    private fun createLumeIDCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = "🆔 ${getString("lume_id_title")}"
                textSize = 16f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = getString("lume_id_desc")
                textSize = 13f
                setTextColor(Color.parseColor("#64748B"))
                setPadding(0, dp(4), 0, dp(12))
                setLineSpacing(0f, 1.3f)
            })
            
            // ID display
            addView(LinearLayout(this@SecurityPrivacyActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                
                val idShape = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.parseColor("#F0F5FF"))
                }
                background = idShape
                
                addView(TextView(this@SecurityPrivacyActivity).apply {
                    text = customerId
                    textSize = 14f
                    setTextColor(AppTheme.Primary.Blue)
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
                
                addView(TextView(this@SecurityPrivacyActivity).apply {
                    text = "📋 ${getString("copy")}"
                    textSize = 13f
                    setTextColor(AppTheme.Primary.Blue)
                    setPadding(dp(8), 0, 0, 0)
                    isClickable = true
                    setOnClickListener {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Lume ID", customerId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this@SecurityPrivacyActivity, getString("id_copied"), Toast.LENGTH_SHORT).show()
                    }
                })
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = getString("lume_id_info")
                textSize = 11f
                setTextColor(Color.parseColor("#94A3B8"))
                setPadding(0, dp(8), 0, 0)
                setLineSpacing(0f, 1.3f)
            })
        }
    }
    
    private fun createDataSharingCard(): LinearLayout {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = "🔗 ${getString("data_sharing")}"
                textSize = 16f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = getString("data_sharing_desc")
                textSize = 13f
                setTextColor(Color.parseColor("#64748B"))
                setPadding(0, dp(4), 0, dp(12))
                setLineSpacing(0f, 1.3f)
            })
            
            // Toggle list
            addView(createPrivacyToggle(
                getString("share_with_banks"),
                getString("share_banks_desc"),
                "data_sharing_banks",
                true,
                prefs
            ))
            
            addView(createDivider())
            
            addView(createPrivacyToggle(
                getString("enable_ai_analysis"),
                getString("ai_analysis_desc"),
                "data_sharing_ai",
                true,
                prefs
            ))
        }
    }
    
    private fun createAIDataUsageCard(): LinearLayout {
        return createInfoCard(
            icon = "🤖",
            title = getString("ai_data_usage"),
            description = getString("ai_data_desc"),
            actionText = getString("learn_more")
        ) {
            showAIDataUsageDialog()
        }
    }
    
    private fun createDataRightsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = "⚖️ ${getString("data_rights_title")}"
                textSize = 16f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = getString("data_rights_desc")
                textSize = 13f
                setTextColor(Color.parseColor("#64748B"))
                setPadding(0, dp(4), 0, dp(16))
                setLineSpacing(0f, 1.3f)
            })
            
            addView(createDataRightButton(getString("download_data")) {
                showDownloadDataDialog()
            })
            
            addView(Space(this@SecurityPrivacyActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8))
            })
            
            addView(createDataRightButton(getString("delete_account")) {
                showDeleteAccountDialog()
            })
        }
    }
    
    private fun createPrivacyToggle(
        title: String,
        description: String,
        key: String,
        defaultValue: Boolean,
        prefs: android.content.SharedPreferences
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
            
            addView(LinearLayout(this@SecurityPrivacyActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    rightMargin = dp(16)
                }
                
                addView(TextView(this@SecurityPrivacyActivity).apply {
                    text = title
                    textSize = 14f
                    setTextColor(Color.parseColor("#1E293B"))
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@SecurityPrivacyActivity).apply {
                    text = description
                    textSize = 12f
                    setTextColor(Color.parseColor("#64748B"))
                    setPadding(0, dp(2), 0, 0)
                })
            })
            
            addView(Switch(this@SecurityPrivacyActivity).apply {
                isChecked = prefs.getBoolean(key, defaultValue)
                setOnCheckedChangeListener { _, isChecked ->
                    prefs.edit().putBoolean(key, isChecked).apply()
                    Toast.makeText(
                        this@SecurityPrivacyActivity,
                        if (isChecked) getString("enabled") else getString("disabled"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
    
    private fun createDataRightButton(text: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(AppTheme.Primary.Blue)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(12), dp(16), dp(12))
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(Color.parseColor("#F0F5FF"))
            }
            background = shape
            
            isClickable = true
            isFocusable = true
            
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = resources.getDrawable(outValue.resourceId, null)
            
            setOnClickListener { onClick() }
        }
    }
    
    private fun createLegalCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(createLegalLink(getString("privacy_policy")) { showPrivacyPolicy() })
            addView(createDivider())
            addView(createLegalLink(getString("terms_service")) { showTermsOfService() })
            addView(createDivider())
            addView(createLegalLink(getString("data_protection")) { showDataProtection() })
        }
    }
    
    private fun createLegalLink(text: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            isClickable = true
            isFocusable = true
            
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                this.text = text  // Use this.text to avoid conflict
                textSize = 15f
                setTextColor(Color.parseColor("#0A0A0A"))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                this.text = "›"
                textSize = 24f
                setTextColor(Color.parseColor("#64748B"))
            })
            
            setOnClickListener { onClick() }
        }
    }
    
    private fun createToggleCard(
        icon: String,
        title: String,
        description: String,
        isEnabled: Boolean,
        onToggle: (Boolean) -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            // Icon + Text
            addView(LinearLayout(this@SecurityPrivacyActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    rightMargin = dp(16)
                }
                
                addView(TextView(this@SecurityPrivacyActivity).apply {
                    text = "$icon $title"
                    textSize = 16f
                    setTextColor(Color.parseColor("#0A0A0A"))
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@SecurityPrivacyActivity).apply {
                    text = description
                    textSize = 13f
                    setTextColor(Color.parseColor("#64748B"))
                    setPadding(0, dp(4), 0, 0)
                    setLineSpacing(0f, 1.3f)
                })
            })
            
            // Toggle Switch
            addView(Switch(this@SecurityPrivacyActivity).apply {
                isChecked = isEnabled
                setOnCheckedChangeListener { _, checked ->
                    onToggle(checked)
                }
            })
        }
    }
    
    private fun createActionCard(
        icon: String,
        title: String,
        description: String,
        actionText: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            elevation = dp(6).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
            }
            background = shape
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = "$icon $title"
                textSize = 16f
                setTextColor(Color.parseColor("#0A0A0A"))
                setTypeface(null, Typeface.BOLD)
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = description
                textSize = 13f
                setTextColor(Color.parseColor("#64748B"))
                setPadding(0, dp(4), 0, dp(12))
                setLineSpacing(0f, 1.3f)
            })
            
            addView(TextView(this@SecurityPrivacyActivity).apply {
                text = actionText
                textSize = 14f
                setTextColor(AppTheme.Primary.Blue)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(8), 0, 0)
                isClickable = true
                isFocusable = true
                setOnClickListener { onClick() }
            })
        }
    }
    
    private fun createInfoCard(
        icon: String,
        title: String,
        description: String,
        actionText: String,
        onClick: () -> Unit
    ): LinearLayout {
        return createActionCard(icon, title, description, actionText, onClick)
    }
    
    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            )
            setBackgroundColor(Color.parseColor("#E5E7EB"))
        }
    }
    
    // Dialog functions
    private fun showAIDataUsageDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString("ai_data_usage"))
            .setMessage(getString("ai_data_details"))
            .setPositiveButton(getString("got_it"), null)
            .show()
    }
    
    private fun showDownloadDataDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString("download_data"))
            .setMessage(getString("download_data_msg"))
            .setPositiveButton(getString("request")) { _, _ ->
                Toast.makeText(this, getString("request_submitted"), Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(getString("cancel"), null)
            .show()
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString("delete_account"))
            .setMessage(getString("delete_account_msg"))
            .setPositiveButton(getString("request")) { _, _ ->
                Toast.makeText(this, getString("request_submitted"), Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(getString("cancel"), null)
            .show()
    }
    
    private fun showPrivacyPolicy() {
        AlertDialog.Builder(this)
            .setTitle(getString("privacy_policy"))
            .setMessage(getString("privacy_policy_content"))
            .setPositiveButton(getString("close"), null)
            .show()
    }
    
    private fun showTermsOfService() {
        AlertDialog.Builder(this)
            .setTitle(getString("terms_service"))
            .setMessage(getString("terms_content"))
            .setPositiveButton(getString("close"), null)
            .show()
    }
    
    private fun showDataProtection() {
        AlertDialog.Builder(this)
            .setTitle(getString("data_protection"))
            .setMessage(getString("data_protection_content"))
            .setPositiveButton(getString("close"), null)
            .show()
    }
    
    private fun addSpace(parent: LinearLayout, dp: Int) {
        parent.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(dp)
            )
        })
    }
    
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
    
    // Localization strings
    private fun getString(key: String): String {
        val strings = mapOf(
            "en" to mapOf(
                "security_privacy" to "Security & Privacy",
                "security_status" to "Your Account is Secure",
                "security_status_desc" to "Your security settings are configured to protect your account and data. Review and update them regularly.",
                "authentication" to "Authentication",
                "biometric_login" to "Biometric Login",
                "biometric_desc" to "Use fingerprint or face recognition to sign in",
                "biometric_enabled" to "Biometric login enabled",
                "biometric_disabled" to "Biometric login disabled",
                "pin_management" to "PIN Management",
                "pin_desc" to "Change your 4-digit security PIN",
                "change_pin" to "Change PIN",
                "change_pin_msg" to "You'll need to verify your identity before changing your PIN.",
                "session_timeout" to "Auto Logout",
                "current_timeout" to "Current: %d minutes of inactivity",
                "change" to "Change",
                "timeout_updated" to "Session timeout updated to %d minutes",
                "minute" to "minute",
                "minutes" to "minutes",
                "privacy_controls" to "Privacy Controls",
                "data_sharing" to "Data Sharing Controls",
                "data_sharing_desc" to "Control how your loan decision data is shared and used",
                "data_sharing_on" to "Data sharing enabled",
                "data_sharing_off" to "Data sharing disabled",
                "your_lume_id" to "Your Lume ID",
                "lume_id_title" to "Your Unique Identifier",
                "lume_id_desc" to "This auto-generated ID links your app to the bank portal for real-time decision updates",
                "lume_id_info" to "Use this ID in the bank portal to receive decision alerts. Your ID cannot be changed",
                "copy" to "Copy",
                "id_copied" to "ID copied to clipboard",
                "data_privacy" to "Data & Privacy",
                "share_with_banks" to "Share with Partner Banks",
                "share_banks_desc" to "Allow sharing your decision history with partner banks for better services",
                "enable_ai_analysis" to "Enable AI Analysis",
                "ai_analysis_desc" to "Let AI analyze your data to provide explainability insights",
                "your_rights" to "Your Rights",
                "data_rights_title" to "Your Data Rights",
                "data_rights_desc" to "You have full control over your data. Request access, download, or delete your information anytime",
                "download_data" to "Download My Data",
                "download_data_msg" to "We'll prepare a complete copy of your data including all loan decisions, AI explanations, bias detection reports, and your profile information. You'll receive a download link via email within 48 hours",
                "delete_account" to "Delete My Account",
                "delete_account_msg" to "⚠️ Warning: This action cannot be undone.\n\nDeleting your account will remove all your data from our servers, disconnect from partner banks, and cancel all active services. Your data will be permanently deleted within 30 days",
                "request" to "Request",
                "request_submitted" to "Request submitted. We'll process it within 48 hours",
                "enabled" to "Enabled",
                "disabled" to "Disabled",
                "analytics" to "Usage Analytics",
                "analytics_desc" to "Help us improve the app by sharing usage data",
                "analytics_on" to "Analytics enabled",
                "analytics_off" to "Analytics disabled",
                "ai_data_usage" to "AI Data Usage",
                "ai_data_desc" to "How we use AI to provide transparency insights",
                "ai_data_details" to "LumeAI uses your loan decision data to provide personalized explainability insights. Your data is encrypted and never shared without your consent. AI models process data locally when possible.",
                "learn_more" to "Learn More",
                "security_alerts" to "Security & Alerts",
                "security_notifications" to "Security Notifications",
                "security_notifications_desc" to "Get alerts for important security events",
                "alerts_on" to "Security alerts enabled",
                "alerts_off" to "Security alerts disabled",
                "activity_log" to "Account Activity",
                "activity_log_desc" to "View your recent account activity",
                "view_log" to "View Activity",
                "recent_activity" to "Recent Activity",
                "activity_log_sample" to "• Login: Today at 11:12 AM\n• Profile updated: Yesterday\n• New decision received: 2 days ago\n• Settings changed: 3 days ago",
                "device_security" to "Device Security",
                "this_device" to "This Device",
                "last_login" to "Last login",
                "just_now" to "Just now",
                "manage_devices" to "Manage Devices",
                "device_management_msg" to "You are currently logged in on this device only. You can log out remotely if you lose access to your device.",
                "legal_compliance" to "Legal & Compliance",
                "privacy_policy" to "Privacy Policy",
                "privacy_policy_content" to "LumeAI Privacy Policy\n\nEffective Date: November 2024\n\nWe collect and process your data to provide AI-powered transparency for financial decisions. Your data is encrypted, stored securely, and never sold to third parties.\n\nKey Points:\n• Data encryption at rest and in transit\n• Minimal data collection\n• User control over data sharing\n• Regular security audits\n• GDPR compliant",
                "terms_service" to "Terms of Service",
                "terms_content" to "LumeAI Terms of Service\n\nBy using LumeAI, you agree to:\n• Use the app for personal, non-commercial purposes\n• Provide accurate information\n• Maintain account security\n• Comply with applicable laws\n\nWe reserve the right to modify these terms with notice.",
                "data_protection" to "Data Protection Rights",
                "data_protection_content" to "Your Rights:\n\n• Right to Access: Request a copy of your data\n• Right to Rectification: Correct inaccurate data\n• Right to Erasure: Request deletion of your data\n• Right to Portability: Transfer your data\n• Right to Object: Object to data processing\n\nContact us at privacy@lumeai.com to exercise your rights.",
                "continue" to "Continue",
                "cancel" to "Cancel",
                "ok" to "OK",
                "close" to "Close",
                "got_it" to "Got it",
                "feature_coming_soon" to "This feature will be available soon!"
            ),
            "hi" to mapOf(
                "security_privacy" to "सुरक्षा और गोपनीयता",
                "security_status" to "आपका खाता सुरक्षित है",
                "security_status_desc" to "आपकी सुरक्षा सेटिंग्स आपके खाते और डेटा की सुरक्षा के लिए कॉन्फ़िगर की गई हैं। नियमित रूप से समीक्षा और अपडेट करें।",
                "authentication" to "प्रमाणीकरण",
                "biometric_login" to "बायोमेट्रिक लॉगिन",
                "biometric_desc" to "साइन इन करने के लिए फिंगरप्रिंट या फेस रिकग्निशन का उपयोग करें",
                "biometric_enabled" to "बायोमेट्रिक लॉगिन सक्षम",
                "biometric_disabled" to "बायोमेट्रिक लॉगिन अक्षम",
                "pin_management" to "PIN प्रबंधन",
                "pin_desc" to "अपना 4 अंकों का सुरक्षा PIN बदलें",
                "change_pin" to "PIN बदलें",
                "change_pin_msg" to "अपना PIN बदलने से पहले आपको अपनी पहचान सत्यापित करनी होगी।",
                "session_timeout" to "ऑटो लॉगआउट",
                "current_timeout" to "वर्तमान: %d मिनट की निष्क्रियता",
                "change" to "बदलें",
                "timeout_updated" to "सत्र समय समाप्ति %d मिनट में अपडेट किया गया",
                "minute" to "मिनट",
                "minutes" to "मिनट",
                "privacy_controls" to "गोपनीयता नियंत्रण",
                "data_sharing" to "बैंकों के साथ डेटा साझा करना",
                "data_sharing_desc" to "साझेदार बैंकों के साथ अपना निर्णय इतिहास साझा करें",
                "data_sharing_on" to "डेटा साझा करना सक्षम",
                "data_sharing_off" to "डेटा साझा करना अक्षम",
                "analytics" to "उपयोग विश्लेषण",
                "analytics_desc" to "उपयोग डेटा साझा करके ऐप को बेहतर बनाने में मदद करें",
                "analytics_on" to "विश्लेषण सक्षम",
                "analytics_off" to "विश्लेषण अक्षम",
                "ai_data_usage" to "AI डेटा उपयोग",
                "ai_data_desc" to "पारदर्शिता अंतर्दृष्टि प्रदान करने के लिए हम AI का उपयोग कैसे करते हैं",
                "ai_data_details" to "LumeAI व्यक्तिगत स्पष्टीकरण अंतर्दृष्टि प्रदान करने के लिए आपके ऋण निर्णय डेटा का उपयोग करता है। आपका डेटा एन्क्रिप्टेड है और आपकी सहमति के बिना कभी साझा नहीं किया जाता है।",
                "learn_more" to "और जानें",
                "security_alerts" to "सुरक्षा और अलर्ट",
                "security_notifications" to "सुरक्षा सूचनाएं",
                "security_notifications_desc" to "महत्वपूर्ण सुरक्षा घटनाओं के लिए अलर्ट प्राप्त करें",
                "alerts_on" to "सुरक्षा अलर्ट सक्षम",
                "alerts_off" to "सुरक्षा अलर्ट अक्षम",
                "activity_log" to "खाता गतिविधि",
                "activity_log_desc" to "अपनी हाल की खाता गतिविधि देखें",
                "view_log" to "गतिविधि देखें",
                "recent_activity" to "हाल की गतिविधि",
                "activity_log_sample" to "• लॉगिन: आज 11:12 AM\n• प्रोफ़ाइल अपडेट: कल\n• नया निर्णय प्राप्त: 2 दिन पहले\n• सेटिंग्स बदली गईं: 3 दिन पहले",
                "device_security" to "डिवाइस सुरक्षा",
                "this_device" to "यह डिवाइस",
                "last_login" to "अंतिम लॉगिन",
                "just_now" to "अभी",
                "manage_devices" to "डिवाइस प्रबंधित करें",
                "device_management_msg" to "आप वर्तमान में केवल इस डिवाइस पर लॉग इन हैं। यदि आप अपने डिवाइस तक पहुंच खो देते हैं तो आप दूर से लॉग आउट कर सकते हैं।",
                "legal_compliance" to "कानूनी और अनुपालन",
                "privacy_policy" to "गोपनीयता नीति",
                "privacy_policy_content" to "LumeAI गोपनीयता नीति\n\nप्रभावी तिथि: नवंबर 2024\n\nहम वित्तीय निर्णयों के लिए AI-संचालित पारदर्शिता प्रदान करने के लिए आपके डेटा को एकत्र और संसाधित करते हैं।",
                "terms_service" to "सेवा की शर्तें",
                "terms_content" to "LumeAI सेवा की शर्तें\n\nLumeAI का उपयोग करके, आप सहमत हैं:\n• व्यक्तिगत, गैर-वाणिज्यिक उद्देश्यों के लिए ऐप का उपयोग करें\n• सटीक जानकारी प्रदान करें\n• खाता सुरक्षा बनाए रखें",
                "data_protection" to "डेटा संरक्षण अधिकार",
                "data_protection_content" to "आपके अधिकार:\n\n• पहुंच का अधिकार: अपने डेटा की प्रति का अनुरोध करें\n• सुधार का अधिकार: गलत डेटा को सही करें\n• मिटाने का अधिकार: अपने डेटा को हटाने का अनुरोध करें",
                "continue" to "जारी रखें",
                "cancel" to "रद्द करें",
                "ok" to "ठीक है",
                "close" to "बंद करें",
                "got_it" to "समझ गया",
                "feature_coming_soon" to "यह सुविधा जल्द ही उपलब्ध होगी!"
            ),
            "te" to mapOf(
                "security_privacy" to "భద్రత మరియు గోప్యత",
                "security_status" to "మీ ఖాతా సురక్షితంగా ఉంది",
                "security_status_desc" to "మీ భద్రతా సెట్టింగులు మీ ఖాతా మరియు డేటాను రక్షించడానికి కాన్ఫిగర్ చేయబడ్డాయి। వాటిని క్రమం��ంగా సమీక్షించండి మరియు నవీకరించండి।",
                "authentication" to "ప్రమాణీకరణ",
                "biometric_login" to "బయోమెట్రిక్ లాగిన్",
                "biometric_desc" to "సైన్ ఇన్ చేయడానికి వేలిముద్ర లేదా ముఖ గుర్తింపును ఉపయోగించండి",
                "biometric_enabled" to "బయోమెట్రిక్ లాగిన్ ప్రారంభించబడింది",
                "biometric_disabled" to "బయోమెట్రిక్ లాగిన్ నిలిపివేయబడింది",
                "pin_management" to "PIN నిర్వహణ",
                "pin_desc" to "మీ 4 అంకెల భద్రతా PINని మార్చండి",
                "change_pin" to "PIN మార్చండి",
                "change_pin_msg" to "మీ PINని మార్చే ముందు మీరు మీ గుర్తింపును ధృవీకరించాలి।",
                "session_timeout" to "ఆటో లాగౌట్",
                "current_timeout" to "ప్రస్తుతం: %d నిమిషాల నిష్క్రియత",
                "change" to "మార్చండి",
                "timeout_updated" to "సెషన్ టైమ్అవుట్ %d నిమిషాలకు నవీకరించబడింది",
                "minute" to "నిమిషం",
                "minutes" to "నిమిషాలు",
                "privacy_controls" to "గోప్యత నియంత్రణలు",
                "data_sharing" to "బ్యాంకులతో డేటా భాగస్వామ్యం",
                "data_sharing_desc" to "భాగస్వామి బ్యాంకులతో మీ నిర్ణయ చరిత్రను భాగస్వామ్యం చేయండి",
                "data_sharing_on" to "డేటా భాగస్వామ్యం ప్రారంభించబడింది",
                "data_sharing_off" to "డేటా భాగస్వామ్యం నిలిపివేయబడింది",
                "analytics" to "వినియోగ విశ్లేషణ",
                "analytics_desc" to "వినియోగ డేటాను భాగస్వామ్యం చేయడం ద్వారా యాప్‌ను మెరుగుపరచడంలో సహాయపడండి",
                "analytics_on" to "విశ్లేషణ ప్రారంభించబడింది",
                "analytics_off" to "విశ్లేషణ నిలిపివేయబడింది",
                "ai_data_usage" to "AI డేటా వినియోగం",
                "ai_data_desc" to "పారదర్శకత అంతర్దృష్టులను అందించడానికి మేము AIని ఎలా ఉపయోగిస్తాము",
                "ai_data_details" to "LumeAI వ్యక్తిగత వివరణ అంతర్దృష్టులను అందించడానికి మీ రుణ నిర్ణయ డేటాను ఉపయోగిస్తుంది। మీ డేటా ఎన్క్రిప్ట్ చేయబడింది మరియు మీ సమ్మతి లేకుండా ఎప్పుడూ భాగస్వామ్యం చేయబడదు।",
                "learn_more" to "మరింత తెలుసుకోండి",
                "security_alerts" to "భద్రత మరియు హెచ్చరికలు",
                "security_notifications" to "భద్రతా నోటిఫికేషన్లు",
                "security_notifications_desc" to "ముఖ్యమైన భద్రతా సంఘటనల కోసం హెచ్చరికలు పొందండి",
                "alerts_on" to "భద్రతా హెచ్చరికలు ప్రారంభించబడ్డాయి",
                "alerts_off" to "భద్రతా హెచ్చరికలు నిలిపివేయబడ్డాయి",
                "activity_log" to "ఖాతా కార్యకలాపం",
                "activity_log_desc" to "మీ ఇటీవలి ఖాతా కార్యకలాపాన్ని వీక్షించండి",
                "view_log" to "కార్యకలాపాన్ని వీక్షించండి",
                "recent_activity" to "ఇటీవలి కార్యకలాపం",
                "activity_log_sample" to "• లాగిన్: ఈరోజు 11:12 AM\n• ప్రొఫైల్ నవీకరించబడింది: నిన్న\n• కొత్త నిర్ణయం స్వీకరించబడింది: 2 రోజుల క్రితం\n• సెట్టింగులు మార్చబడ్డాయి: 3 రోజుల క్రితం",
                "device_security" to "పరికర భద్రత",
                "this_device" to "ఈ పరికరం",
                "last_login" to "చివరి లాగిన్",
                "just_now" to "ఇప్పుడే",
                "manage_devices" to "పరికరాలను నిర్వహించండి",
                "device_management_msg" to "మీరు ప్రస్తుతం ఈ పరికరంలో మాత్రమే లాగిన్ అయ్యారు। మీరు మీ పరికరానికి యాక్సెస్ కోల్పోతే మీరు రిమోట్‌గా లాగౌట్ చేయవచ్చు।",
                "legal_compliance" to "చట్టపరమైన మరియు సమ్మతి",
                "privacy_policy" to "గోప్యతా విధానం",
                "privacy_policy_content" to "LumeAI గోప్యతా విధానం\n\nప్రభావవంతమైన తేదీ: నవంబర్ 2024\n\nఆర్థిక నిర్ణయాల కోసం AI-శక్తితో కూడిన పారదర్శకతను అందించడానికి మేము మీ డేటాను సేకరిస్తాము మరియు ప్రాసెస్ చేస్తాము।",
                "terms_service" to "సేవా నిబంధనలు",
                "terms_content" to "LumeAI సేవా నిబంధనలు\n\nLumeAIని ఉపయోగించడం ద్వారా, మీరు అంగీకరిస్తున్నారు:\n• వ్యక్తిగత, వాణిజ్యేతర ప్రయోజనాల కోసం యాప్‌ను ఉపయోగించండి\n• ఖచ్చితమైన సమాచారాన్ని అందించండి",
                "data_protection" to "డేటా రక్షణ హక్కులు",
                "data_protection_content" to "మీ హక్కులు:\n\n• యాక్సెస్ హక్కు: మీ డేటా కాపీని అభ్యర్థించండి\n• దిద్దుబాటు హక్కు: తప్పు డేటాను సరిచేయండి\n• తొలగింపు హక్కు: మీ డేటాను తొలగించమని అభ్యర్థించండి",
                "continue" to "కొనసాగించు",
                "cancel" to "రద్దు చేయండి",
                "ok" to "సరే",
                "close" to "మూసివేయండి",
                "got_it" to "అర్థమైంది",
                "feature_coming_soon" to "ఈ ఫీచర్ త్వరలో అందుబాటులో ఉంటుంది!"
            )
        )
        
        return strings[currentLanguage]?.get(key) ?: strings["en"]?.get(key) ?: key
    }
}

