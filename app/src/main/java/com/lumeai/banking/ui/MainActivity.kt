package com.lumeai.banking.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.lumeai.banking.DecisionManager
import com.lumeai.banking.FirebaseListenerService
import com.lumeai.banking.FirebaseSyncManager
import com.lumeai.banking.UserStats
import com.lumeai.banking.models.FirebaseDecision
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.lumeai.banking.R
import com.lumeai.banking.utils.AppTheme.CornerRadius
import com.lumeai.banking.utils.AppTheme.Text
import com.lumeai.banking.utils.AppTheme.Text.OnCard
import com.lumeai.banking.utils.AppTheme.Text.OnCardSecondary
import com.lumeai.banking.utils.AppTheme.Text.Tertiary

/**
 * MainActivity - Professional UI inspired by modern banking apps
 */
@SuppressLint("NewApi")
class MainActivity : AppCompatActivity() {

    private var rootLayout: LinearLayout? = null
    private var contentLayout: LinearLayout? = null
    private var latestDecision: FirebaseDecision? = null
    private var userStats: UserStats? = null
    private var greetingTextView: TextView? = null
    private var realtimeListenerJob: kotlinx.coroutines.Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // White status bar
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Hide action bar for custom header
        supportActionBar?.hide()

        // Start Firebase listener service for real-time notifications
        startFirebaseListenerService()

        setContentView(createUI())
        addChatbotFAB()

        // Load real Firebase data
        loadDecisionData()
        
        // 🔧 FIX: Sync default consents to Firebase on first app launch
        syncDefaultConsentsToFirebase()
        
        // 🔥 START REAL-TIME LISTENER: Auto-refresh UI when new decisions arrive
        startRealtimeDecisionListener()
    }

    override fun onResume() {
        super.onResume()
        // Update greeting (in case name changed in profile)
        updateGreeting()
        // Refresh data when returning to screen
        loadDecisionData()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up real-time listener
        realtimeListenerJob?.cancel()
        android.util.Log.d("MainActivity", "🛑 Real-time listener stopped")
    }

    /**
     * Update greeting message with user's name and time-based greeting
     */
    private fun updateGreeting() {
        greetingTextView?.let { textView ->
            // Get user name from profile
            val prefs = getSharedPreferences("LumeAIPrefs", MODE_PRIVATE)
            var userName = prefs.getString("user_name", null)

            // Set default username if none exists
            if (userName.isNullOrEmpty()) {
                userName = "User"
                prefs.edit().putString("user_name", userName).apply()

                // Sync to Firebase so bank portal can access it
                syncProfileToFirebase(userName)
            }

            // Time-based greeting
            val greeting =
                when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    in 0..11 -> "Good Morning"
                    in 12..16 -> "Good Afternoon"
                    else -> "Good Evening"
                }

            textView.text = "$greeting, $userName!"
        }
    }

    /**
     * Sync user profile to Firebase for bank portal access
     */
    private fun syncProfileToFirebase(name: String) {
        lifecycleScope.launch {
            try {
                val customerId = FirebaseListenerService.getCustomerId(this@MainActivity)
                FirebaseSyncManager.saveUserProfile(customerId, name)
                android.util.Log.d("MainActivity", "✅ Profile synced: $name")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to sync profile: ${e.message}")
            }
        }
    }
    
    /**
     * 🔧 SYNC DEFAULT CONSENTS: Save initial consent values to Firebase on first launch
     * This ensures the bank portal can see consent preferences immediately after app install
     */
    private fun syncDefaultConsentsToFirebase() {
        lifecycleScope.launch {
            try {
                val customerId = FirebaseListenerService.getCustomerId(this@MainActivity)
                
                // Check if consents already exist in Firebase
                val existingConsents = FirebaseSyncManager.getUserConsents(customerId)
                
                if (existingConsents == null) {
                    // First time - sync default values to Firebase
                    android.util.Log.d("MainActivity", "🔧 First launch detected - syncing default consents to Firebase...")
                    
                    val consentPrefs = getSharedPreferences("ConsentPreferences", MODE_PRIVATE)
                    
                    // Get default values (all ON by default as per ConsentActivity)
                    val aiAnalysis = consentPrefs.getBoolean("consent_ai_analysis", true)
                    val biasDetection = consentPrefs.getBoolean("consent_bias_detection", true)
                    val dataSharing = consentPrefs.getBoolean("consent_data_sharing", true)
                    val dataStorage = consentPrefs.getBoolean("consent_data_storage", true)
                    
                    // Save to Firebase
                    val success = FirebaseSyncManager.saveUserConsents(
                        customerId,
                        aiAnalysis,
                        biasDetection,
                        dataSharing,
                        dataStorage
                    )
                    
                    if (success) {
                        android.util.Log.d("MainActivity", "✅ Default consents synced to Firebase for customer: $customerId")
                        android.util.Log.d("MainActivity", "   - AI Analysis: $aiAnalysis")
                        android.util.Log.d("MainActivity", "   - Bias Detection: $biasDetection")
                        android.util.Log.d("MainActivity", "   - Data Sharing: $dataSharing")
                        android.util.Log.d("MainActivity", "   - Data Storage: $dataStorage")
                    } else {
                        android.util.Log.e("MainActivity", "❌ Failed to sync default consents")
                    }
                } else {
                    android.util.Log.d("MainActivity", "ℹ️ Consents already exist in Firebase (found ${existingConsents.size} keys) - skipping initial sync")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "❌ Error syncing default consents: ${e.message}", e)
            }
        }
    }
    
    /**
     * 🔥 REAL-TIME LISTENER: Listen for new decisions and auto-refresh UI
     * This ensures MainActivity updates immediately when bank portal sends new decisions
     */
    private fun startRealtimeDecisionListener() {
        realtimeListenerJob?.cancel() // Cancel any existing listener
        
        realtimeListenerJob = lifecycleScope.launch {
            try {
                val customerId = FirebaseListenerService.getCustomerId(this@MainActivity)
                android.util.Log.d("MainActivity", "🔥 Starting real-time listener for customer: $customerId")
                
                FirebaseSyncManager.listenForCustomerDecisions(customerId)
                    .collect { newDecision ->
                        android.util.Log.d("MainActivity", "🔔 NEW DECISION RECEIVED in MainActivity: ${newDecision.id}")
                        android.util.Log.d("MainActivity", "   Bank: ${newDecision.bankName}")
                        android.util.Log.d("MainActivity", "   Outcome: ${newDecision.outcome}")
                        android.util.Log.d("MainActivity", "   Type: ${newDecision.decisionType}")
                        
                        // Refresh data and UI immediately
                        runOnUiThread {
                            android.util.Log.d("MainActivity", "🔄 Auto-refreshing MainActivity UI...")
                            loadDecisionData()
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "❌ Real-time listener error: ${e.message}", e)
            }
        }
    }

    /**
     * Load decision data from Firebase and update UI
     */
    private fun loadDecisionData() {
        android.util.Log.d("MainActivity", "🔄 Loading decision data...")

        lifecycleScope.launch {
            try {
                // Force refresh to get latest data from Firebase
                val allDecisions =
                    DecisionManager.getAllDecisions(this@MainActivity, forceRefresh = true)
                android.util.Log.d(
                    "MainActivity",
                    "📊 Loaded ${allDecisions.size} decisions from Firebase"
                )

                // Get latest decision and stats
                latestDecision = allDecisions.firstOrNull()
                userStats = DecisionManager.getUserStats(this@MainActivity, forceRefresh = true)

                android.util.Log.d(
                    "MainActivity",
                    "📋 Latest decision: ${latestDecision?.bankName ?: "None"}"
                )
                android.util.Log.d(
                    "MainActivity",
                    "📊 Stats - Total: ${userStats?.totalDecisions}, Bias: ${userStats?.biasDetectedCount}, Banks: ${userStats?.banksCount}"
                )

                // Update UI on main thread
                runOnUiThread {
                    android.util.Log.d("MainActivity", "🎨 Updating UI with new data...")
                    updateContentWithData()
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "❌ Failed to load decisions: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Update content layout with real data
     */
    private fun updateContentWithData() {
        contentLayout?.let { layout ->
            layout.removeAllViews()

            // Add Latest Decision Card (if exists and not dismissed)
            latestDecision?.let { decision ->
                // Check if user dismissed this decision
                val prefs = getSharedPreferences("LumeAIPrefs", MODE_PRIVATE)
                val dismissedId = prefs.getString("dismissed_decision_id", null)

                // Only show if not dismissed OR if it's a new decision
                if (dismissedId != decision.id) {
                    layout.addView(createLatestDecisionCard(decision))
                    addSpace(layout, 20)
                }
            }

            // Status Card with Real Stats
            layout.addView(createStatusCard())
            addSpace(layout, 20)

            // Quick Actions
            layout.addView(createQuickActionsSection())
            addSpace(layout, 20)

            // App Statistics
            layout.addView(createStatisticsSection())
            addSpace(layout, 20)

            // New to LumeAI? - Simple navigation card
            layout.addView(createNewUserCard())
            addSpace(layout, 30)

        }
    }

    /**
     * Start Firebase listener service for real-time decision notifications
     */
    private fun startFirebaseListenerService() {
        try {
            val intent = Intent(this, FirebaseListenerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            android.util.Log.d("MainActivity", "✅ Firebase listener service started")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "❌ Failed to start Firebase service", e)
        }
    }

    private fun createUI(): ScrollView {
        val scrollView = ScrollView(this)

        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            // Subtle blue-gray gradient background (banking professional)
            val bgGradient = android.graphics.drawable.GradientDrawable()
            bgGradient.colors = intArrayOf(
                0xFFEFF6FF.toInt(),  // Light blue tint (top)
                0xFFF1F5F9.toInt(),  // Light gray-blue
                0xFFF8FAFC.toInt()   // Almost white (bottom)
            )
            bgGradient.gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
            bgGradient.orientation =
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
            background = AppTheme.Background.Primary.toDrawable()
        }

        // Header
        rootLayout!!.addView(createHeader())

        // Content with proper spacing below header
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            // Proper margin to avoid overlap with header
            val params = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dpToPx(8)  // Small spacing below header
            layoutParams = params
        }

        // Initial content - will be updated when data loads
        contentLayout!!.addView(createStatusCard())
        addSpace(contentLayout!!, 24)
        contentLayout!!.addView(createQuickActionsSection())
        addSpace(contentLayout!!, 24)
        contentLayout!!.addView(createStatisticsSection())
        addSpace(contentLayout!!, 24)
        contentLayout!!.addView(createNewUserCard())
        addSpace(contentLayout!!, 30)

        rootLayout!!.addView(contentLayout)
        scrollView.addView(rootLayout)
        return scrollView
    }

    /**
     * Create bold header (Revolut/N26/Wise style) - SEAMLESS DESIGN
     */
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(32))
            // Extended header to create seamless overlap
            val headerGradient = android.graphics.drawable.GradientDrawable()
            headerGradient.colors = intArrayOf(
                0xFFDBEAFE.toInt(),  // Light blue
                0xFFE3F2FD.toInt(),  // Very light blue (smoother transition)
                0xFFF8FAFC.toInt()   // Almost white (blends to background)
            )
            headerGradient.gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
            headerGradient.orientation =
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
            // Remove bottom rounded corners for seamless flow
            background = AppTheme.Background.Secondary.toDrawable()

            // Header row with app icon, title, and profile avatar
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                // App Logo Icon
                addView(ImageView(this@MainActivity).apply {
                    setImageResource(R.mipmap.ic_launcher)
                    layoutParams = LayoutParams(dpToPx(32), dpToPx(32)).apply {
                        rightMargin = dpToPx(12)
                    }
                })

                // Title
                addView(TextView(this@MainActivity).apply {
                    text = "LumeAI"
                    textSize = 26f
                    setTextColor(Text.Primary)  // Strong black
                    typeface = getTypefaceBold()
                    applyProfessionalText()
                    letterSpacing = -0.03f
                    layoutParams =
                        LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                })

                // Modern circular profile avatar (centered vertically)
                addView(createModernProfileAvatar())
            })

            greetingTextView = TextView(this@MainActivity).apply {
                textSize = 15f
                setTextColor(Text.Secondary)  // Strong black
                typeface = getTypefaceBold()
                applyProfessionalText()
                setPadding(0, dpToPx(10), 0, dpToPx(2))
            }
            updateGreeting()
            addView(greetingTextView)

            // Friendly subtitle
            addView(TextView(this@MainActivity).apply {
                text = "Your trusted partner for understanding banking decisions"
                textSize = 13f
                setTextColor(Text.Secondary)
                typeface = getTypefaceRegular()
                applyProfessionalText()
                setPadding(0, 0, 0, 0)
            })
        }
    }

    /**
     * Create modern circular profile avatar (Instagram/Gmail style)
     */
    private fun createModernProfileAvatar(): TextView {
        return TextView(this).apply {
            // Get user initials from profile
            val prefs = getSharedPreferences("LumeAIPrefs", MODE_PRIVATE)
            val userName = prefs.getString("user_name", null)

            // Display first letter of name, or default icon
            text = if (!userName.isNullOrEmpty()) {
                userName.first().uppercase()
            } else {
                "U" // Default for "User"
            }

            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = getTypefaceBold()
            applyProfessionalText()
            gravity = Gravity.CENTER

            // Circular avatar - 40dp standard size (Material Design)
            val size = dpToPx(40)
            layoutParams = LayoutParams(size, size).apply {
                // Push down slightly for better visual balance
                topMargin = dpToPx(4)
            }

            // Professional banking gradient
            val shape = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                colors = intArrayOf(
                    OnCard, // Trust Blue
                    OnCardSecondary  // Cyan - AI
                )
                gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
                orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
            }
            background = shape

            // Add subtle elevation for depth
            elevation = dpToPx(4).toFloat()

            // Add ripple effect on touch
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
                outValue,
                true
            )
            foreground = getDrawable(outValue.resourceId)

            setOnClickListener {
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
            }
        }
    }

    /**
     * ⭐ Create Latest Decision Card (Modern Banking Design - Theme Consistent)
     */
    private fun createLatestDecisionCard(decision: FirebaseDecision): LinearLayout {
        val (statusIcon, statusText, statusBgColor, statusTextColor) = when {
            decision.outcome.equals("APPROVED", ignoreCase = true) ->
                Quadruple("✅", "APPROVED", AppTheme.StatusBg.Success, AppTheme.Status.Success)
            decision.outcome.equals("DENIED", ignoreCase = true) ->
                Quadruple("❌", "DENIED", AppTheme.StatusBg.Error, AppTheme.Status.Error)
            decision.outcome.equals("PENDING", ignoreCase = true) ->
                Quadruple("⏳", "PENDING", AppTheme.StatusBg.Warning, AppTheme.Status.Warning)
            else ->
                Quadruple("📋", decision.outcome.uppercase(), AppTheme.StatusBg.Info, AppTheme.Status.Info)
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(4).toFloat()
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            
            // White card with rounded corners - matching theme
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(CornerRadius).toFloat()
            shape.setColor(AppTheme.Cards.Surface)
            background = shape
            
            val thisCard = this
            
            // Header: "Latest Decision" + Dismiss Button
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, dpToPx(16))
                
                addView(TextView(this@MainActivity).apply {
                    text = "🔔 Latest Decision"
                    textSize = 16f
                    setTextColor(Text.OnCard)
                    typeface = getTypefaceBold()
                    applyProfessionalText()
                    layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                })
                
                // Dismiss button
                addView(TextView(this@MainActivity).apply {
                    text = "✕"
                    textSize = 22f
                    setTextColor(Text.Secondary)
                    setPadding(dpToPx(8), dpToPx(4), dpToPx(4), dpToPx(4))
                    isClickable = true
                    isFocusable = true
                    
                    setOnClickListener {
                        thisCard.animate()
                            .alpha(0f)
                            .translationY(-thisCard.height.toFloat() / 2)
                            .setDuration(250)
                            .withEndAction {
                                thisCard.visibility = View.GONE
                                getSharedPreferences("LumeAIPrefs", MODE_PRIVATE)
                                    .edit().putString("dismissed_decision_id", decision.id).apply()
                                Toast.makeText(this@MainActivity, "Decision dismissed", Toast.LENGTH_SHORT).show()
                            }
                            .start()
                    }
                })
            })
            
            // Clickable content area
            val contentArea = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                isClickable = true
                isFocusable = true
                
                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                setBackgroundResource(outValue.resourceId)
                
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, AIExplainabilityHubActivity::class.java))
                }
                
                // Grid Layout: Status Badge + Bank Info side by side
                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 0, 0, dpToPx(16))
                    
                    // Status Badge (Left)
                    addView(LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER
                        setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
                        
                        val badge = android.graphics.drawable.GradientDrawable()
                        badge.cornerRadius = dpToPx(12).toFloat()
                        badge.setColor(statusBgColor)
                        background = badge
                        
                        addView(TextView(this@MainActivity).apply {
                            text = "$statusIcon $statusText"
                            textSize = 14f
                            setTextColor(statusTextColor)
                            typeface = getTypefaceBold()
                            applyProfessionalText()
                        })
                    })
                    
                    // Spacer
                    addView(View(this@MainActivity).apply {
                        layoutParams = LayoutParams(dpToPx(12), 0)
                    })
                    
                    // Bank Name (Right)
                    addView(TextView(this@MainActivity).apply {
                        text = decision.bankName
                        textSize = 16f
                        setTextColor(Text.OnCard)
                        typeface = getTypefaceBold()
                        applyProfessionalText()
                        layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                    })
                })
                
                // Loan Type & Amount Row
                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 0, 0, dpToPx(12))
                    
                    if (decision.loanType.isNotEmpty()) {
                        addView(TextView(this@MainActivity).apply {
                            text = formatLoanType(decision.loanType)
                            textSize = 14f
                            setTextColor(Text.OnCardSecondary)
                            typeface = getTypefaceRegular()
                            applyProfessionalText()
                            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                        })
                    }
                    
                    // Loan Amount
                    if (decision.loanAmount > 0) {
                        addView(TextView(this@MainActivity).apply {
                            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                            text = formatter.format(decision.loanAmount)
                            textSize = 18f
                            setTextColor(Text.OnCard)
                            typeface = getTypefaceBold()
                            applyProfessionalText()
                        })
                    }
                })
                
                // Bias Warning (if detected)
                if (decision.biasDetected) {
                    addView(LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
                        
                        val biasShape = android.graphics.drawable.GradientDrawable()
                        biasShape.cornerRadius = dpToPx(8).toFloat()
                        biasShape.setColor(AppTheme.StatusBg.Warning)
                        background = biasShape
                        
                        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                        params.topMargin = 0
                        params.bottomMargin = dpToPx(12)
                        layoutParams = params
                        
                        addView(TextView(this@MainActivity).apply {
                            text = "⚠️"
                            textSize = 16f
                            setPadding(0, 0, dpToPx(8), 0)
                        })
                        
                        addView(TextView(this@MainActivity).apply {
                            text = "Potential bias detected (${decision.biasSeverity} risk)"
                            textSize = 13f
                            setTextColor(AppTheme.Status.Warning)
                            typeface = getTypefaceMedium()
                            applyProfessionalText()
                        })
                    })
                }
                
                // Timestamp + Tap hint
                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    
                    addView(TextView(this@MainActivity).apply {
                        val sdf = SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault())
                        text = sdf.format(Date(decision.timestamp))
                        textSize = 12f
                        setTextColor(Text.Secondary)
                        typeface = getTypefaceRegular()
                        applyProfessionalText()
                        layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                    })
                    
                    addView(TextView(this@MainActivity).apply {
                        text = "Tap to view →"
                        textSize = 13f
                        setTextColor(AppTheme.Primary.Blue)
                        typeface = getTypefaceMedium()
                        applyProfessionalText()
                    })
                })
            }
            
            addView(contentArea)
        }
    }

    private fun formatLoanType(loanType: String): String {
        return when (loanType.uppercase()) {
            "PERSONAL_LOAN", "PERSONAL" -> "💰 Personal Loan"
            "HOME_LOAN", "HOME" -> "🏠 Home Loan"
            "CAR_LOAN", "CAR" -> "🚗 Car Loan"
            "EDUCATION_LOAN", "EDUCATION" -> "🎓 Education Loan"
            "BUSINESS_LOAN", "BUSINESS" -> "💼 Business Loan"
            "CREDIT_CARD" -> "💳 Credit Card"
            else -> loanType.replace("_", " ")
        }
    }

    /**
     * New User Card - Simple navigation to How It Works
     */
    private fun createNewUserCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val cardGradient = android.graphics.drawable.GradientDrawable()
            cardGradient.colors = intArrayOf(
                "#233c67".toColorInt(),  // Light blue
                "#233FCF".toColorInt()   // Light indigo
            )
            cardGradient.gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
            cardGradient.orientation =
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
            cardGradient.cornerRadius = dpToPx(CornerRadius).toFloat()
            background = cardGradient

            setPadding(dpToPx(24), dpToPx(20), dpToPx(24), dpToPx(20))
            elevation = dpToPx(6).toFloat()
            gravity = Gravity.CENTER_VERTICAL

            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = resources.getDrawable(outValue.resourceId, null)

            setOnClickListener {
                startActivity(Intent(this@MainActivity, HowItWorksActivity::class.java))
            }

            // Icon
            addView(TextView(this@MainActivity).apply {
                text = "✨"
                textSize = 32f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 0, dpToPx(16), 0)
            })

            // Content
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT,
                    1f
                )

                addView(TextView(this@MainActivity).apply {
                    text = "New to LumeAI?"
                    textSize = 18f
                    setTextColor(Tertiary)
                    typeface = getTypefaceBold()
                    applyProfessionalText()
                })

                addView(TextView(this@MainActivity).apply {
                    text = "Learn how we make AI decisions transparent"
                    textSize = 14f
                    setTextColor(Tertiary)
                    typeface = getTypefaceRegular()
                    applyProfessionalText()
                    setPadding(0, dpToPx(4), 0, 0)
                })
            })

            // Arrow
            addView(TextView(this@MainActivity).apply {
                text = "→"
                textSize = 28f
                setTextColor(Tertiary)
                setPadding(dpToPx(12), 0, 0, 0)
            })
        }
    }

    /**
     * How It Works Promo Card (OLD - keeping for reference but not used)
     */
    private fun createHowItWorksPromo_OLD(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFF3E5F5.toInt())
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(4).toFloat()
            gravity = Gravity.CENTER_VERTICAL

            val shape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(0xFFF3E5F5.toInt())
            }
            background = shape

            setOnClickListener {
                startActivity(Intent(this@MainActivity, HowItWorksActivity::class.java))
            }

            addView(TextView(this@MainActivity).apply {
                text = "✨"
                textSize = 40f
                setPadding(0, 0, dpToPx(16), 0)
            })

            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )

                addView(TextView(this@MainActivity).apply {
                    text = "New to LumeAI?"
                    textSize = 16f
                    setTextColor(AppTheme.Primary.Blue)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })

                addView(TextView(this@MainActivity).apply {
                    text = "Learn how we protect your rights →"
                    textSize = 13f
                    setTextColor(0xFF8E24AA.toInt())
                    setPadding(0, dpToPx(4), 0, 0)
                })
            })
        }
    }

    // Helper data class for 4 values
    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    /**
     * Create protection status card - Modern overlapping design (Revolut/N26 style)
     */
    private fun createStatusCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(20))  // Reduced top padding
            elevation = dpToPx(4).toFloat()  // Increased elevation for depth
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            // Pure white card with prominent shadow (professional banking style)
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(CornerRadius).toFloat()  // Larger radius for modern look
            shape.setColor(AppTheme.Cards.Surface)  // Pure white
            background = shape

            // Professional sparkle icon for AI transparency (modern, eye-catching)
            addView(TextView(this@MainActivity).apply {
                text = "✨"
                textSize = 42f
                setTextColor(Text.OnCard)
                typeface = getTypefaceBold()
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 0)  // Removed bottom padding
            })

            addView(TextView(this@MainActivity).apply {
                text = "AI Transparency Active"
                textSize = 22f
                setTextColor(OnCard)  // Strong black
                typeface = getTypefaceBold()
                applyProfessionalText()
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(4), 0, dpToPx(6))  // Reduced top/bottom padding
            })

            addView(TextView(this@MainActivity).apply {
                text = "Monitoring and explaining AI decisions across all your bank accounts"
                textSize = 15f
                setTextColor(OnCard)  // Very dark gray - almost black for maximum visibility
                typeface = getTypefaceMedium()
                applyProfessionalText()
                gravity = Gravity.CENTER
                setLineSpacing(dpToPx(2).toFloat(), 1.4f)
                setPadding(dpToPx(20), 0, dpToPx(20), dpToPx(14))
            })

            // Display Customer ID
            addView(TextView(this@MainActivity).apply {
                val customerId = FirebaseListenerService.getCustomerId(this@MainActivity)
                text = "📱 Your ID: $customerId"
                textSize = 13f
                setTextColor(OnCard)
                typeface = getTypefaceMedium()
                applyProfessionalText()
                gravity = Gravity.CENTER
                setPadding(dpToPx(20), 0, dpToPx(20), dpToPx(12))
            })

            // Stats row - with REAL data from Firebase
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(dpToPx(16), 0, dpToPx(16), 0)

                val stats = userStats
                val decisionsCount = stats?.totalDecisions ?: 0
                val biasCount = stats?.biasDetectedCount ?: 0
                val banksCount = stats?.banksCount ?: 0

                addView(
                    createStatPill(
                        if (decisionsCount > 0) "$decisionsCount" else "-",
                        "Decisions\nTracked",
                        0xFFFFDC78.toInt(),
                        1f
                    )
                )
                addView(View(this@MainActivity).apply {
                    layoutParams = LayoutParams(dpToPx(12), 0)
                })
                addView(
                    createStatPill(
                        if (biasCount > 0) "$biasCount" else "0",
                        "Bias\nDetected",
                        0xFF6951.toInt(),
                        1f
                    )
                )
                addView(View(this@MainActivity).apply {
                    layoutParams = LayoutParams(dpToPx(12), 0)
                })
                addView(
                    createStatPill(
                        if (banksCount > 0) "$banksCount" else "-",
                        "Banks\nConnected",
                        0xFFFFD7D0.toInt(),
                        1f
                    )
                )
            })
        }
    }

    /**
     * Create stat pill with soft pastel style
     */
    private fun createStatPill(
        number: String,
        label: String,
        bgColor: Int,
        weight: Float = 0f
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(12), dpToPx(14), dpToPx(12), dpToPx(14))
            gravity = Gravity.CENTER

            // Vibrant colored background pills
            val pillShape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(CornerRadius).toFloat()
                setColor(AppTheme.Cards.SurfaceLight)  // Light blue tint
            }
            background = pillShape

            if (weight > 0) {
                layoutParams = LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT,
                    weight
                )
            }

            addView(TextView(this@MainActivity).apply {
                text = number
                textSize = 28f
                setTextColor(OnCardSecondary)  // Trust Blue numbers
                typeface = getTypefaceMedium()
                applyProfessionalText()
                gravity = Gravity.CENTER
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                setTextColor(OnCardSecondary)  // Very dark slate for maximum visibility
                typeface = getTypefaceMedium()
                applyProfessionalText()
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(4), 0, 0)
            })
        }
    }

    /**
     * Create quick actions section - World-class card-based design
     */
    @SuppressLint("NewApi")
    private fun createQuickActionsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = "Quick Actions"
                textSize = 21f
                setTextColor(OnCard)
                typeface = getTypefaceBold()
                applyProfessionalText()
                setPadding(dpToPx(4), dpToPx(4), 0, dpToPx(16))
                letterSpacing = -0.02f  // Slightly tighter for crispness
            })
            val marginParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
            // ROW 1: CORE FEATURES (Most Important)
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL

                addView(
                    createProfessionalActionCard(
                        R.drawable.ic_ai_explainability,
                        "AI Explainability",
                        "Understand decisions",
                        AppTheme.Primary.Blue,
                        1f
                    ) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                AIExplainabilityHubActivity::class.java
                            )
                        )
                    })

                addView(View(this@MainActivity).apply {
                    layoutParams = LayoutParams(dpToPx(16), 0)
                })

                addView(
                    createProfessionalActionCard(
                        R.drawable.ic_path_to_approval,
                        "Path to Approval",
                        "Get better outcomes",
                        AppTheme.Primary.Blue,
                        1f
                    ) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                PathToApprovalActivity::class.java
                            )
                        )
                    })
            })

//        addView(View(this@MainActivity).apply {
//            layoutParams = LinearLayout.LayoutParams(0, dpToPx(16))
//        })

            // ROW 2: PRIVACY & FAIRNESS
            addView(
                LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL

                    addView(
                        createProfessionalActionCard(
                            R.drawable.ic_consent,
                            "Consent Control",
                            "Manage privacy",
                            AppTheme.Primary.Blue,
                            1f
                        ) {
                            startActivity(Intent(this@MainActivity, ConsentActivity::class.java))
                        })

                    addView(View(this@MainActivity).apply {
                        layoutParams = LayoutParams(dpToPx(16), 0)
                    })

                    addView(
                        createProfessionalActionCard(
                            R.drawable.ic_fairness_balance,
                            "Fairness Analysis",
                            "Bias detection metrics",
                            AppTheme.Primary.Blue,
                            1f
                        ) {
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    FairnessMetricsActivity::class.java
                                )
                            )
                        })
                }, marginParams
            )

            // ROW 3: AI PERSONALIZATION
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL

                addView(
                    createProfessionalActionCard(
                        R.drawable.ic_my_decisions,
                        "Explain My Profile",
                        "How AI sees you",
                        AppTheme.Primary.Blue,
                        1f
                    ) {
                        startActivity(Intent(this@MainActivity, MyAIProfileActivity::class.java))
                    })

                addView(View(this@MainActivity).apply {
                    layoutParams = LayoutParams(dpToPx(16), 0)
                })

                addView(
                    createProfessionalActionCard(
                        R.drawable.ic_personalized_offers,
                        "Personalized Offers",
                        "AI-driven deals",
                        AppTheme.Primary.Blue,
                        1f
                    ) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                PersonalizedOffersActivity::class.java
                            )
                        )
                    })
            }, marginParams)

            // ROW 4: SECURITY & ANALYTICS
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL

                addView(
                    createProfessionalActionCard(
                        R.drawable.ic_fraud_detection,
                        "Fraud Detection",
                        "Security monitoring",
                        AppTheme.Primary.Blue,
                        1f
                    ) {
                        startActivity(Intent(this@MainActivity, FraudDetectionActivity::class.java))
                    })

                addView(View(this@MainActivity).apply {
                    layoutParams = LayoutParams(dpToPx(16), 0)
                })

                addView(
                    createProfessionalActionCard(
                        R.drawable.ic_dashboard_grid,
                        "Dashboard",
                        "Track analytics",
                        AppTheme.Primary.Blue,
                        1f
                    ) {
                        startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                    })
            }, marginParams)

            // ROW 5: MORE FEATURES (CENTERED)
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER  // Center the button
                setPadding(dpToPx(20), dpToPx(8), dpToPx(20), dpToPx(8))

                addView(
                    createProfessionalActionCard(
                        R.drawable.ic_more,
                        "More Features",
                        "Explore all",
                        AppTheme.Primary.Blue,
                        0.5f  // Half width for centered look
                    ) {
                        startActivity(Intent(this@MainActivity, MoreFeaturesActivity::class.java))
                    })
            }, marginParams)
        }
    }

    /**
     * Create professional action card (inspired by Google Material Design 3)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createProfessionalActionCard(
        iconResId: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        weight: Float,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(18), dpToPx(16), dpToPx(18))
            gravity = Gravity.START or Gravity.TOP
            elevation = dpToPx(2).toFloat()
            layoutParams = LayoutParams(
                0,
                dpToPx(112),
                weight
            )

            // Pure white cards (N26/Revolut style)
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(CornerRadius).toFloat()
            shape.setColor(AppTheme.Cards.Surface)
            background = shape

            // Clickable with ripple
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = ContextCompat.getDrawable(context, outValue.resourceId)
            setOnClickListener { onClick() }

            // Icon circle with accent color
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(dpToPx(40), dpToPx(40)).apply {
                    gravity = Gravity.CENTER
                }

                val iconBg = android.graphics.drawable.GradientDrawable()
                iconBg.shape = android.graphics.drawable.GradientDrawable.OVAL
                // SOLID BOLD COLORS - not transparent!
                iconBg.setColor(OnCard)
                background = iconBg

                addView(ImageView(this@MainActivity).apply {
                    setImageResource(iconResId)
                    setColorFilter(0xFFFFFFFF.toInt())  // WHITE icons on colored backgrounds
                    layoutParams = LayoutParams(dpToPx(20), dpToPx(20))
                })
            })

            // Spacer
            addView(View(this@MainActivity).apply {
                layoutParams = LayoutParams(0, dpToPx(12))
            })

            // Title
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 15f
                setTextColor(OnCard)  // Strong black
                typeface = getTypefaceMedium()
                applyProfessionalText()
                maxLines = 1
                letterSpacing = -0.005f
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            })

            // Subtitle
            addView(TextView(this@MainActivity).apply {
                text = subtitle
                textSize = 12f
                setTextColor(0xFFFFFFFF.toInt())  // Medium gray
                typeface = getTypefaceRegular()
                applyProfessionalText()
                setPadding(0, dpToPx(3), 0, 0)
                maxLines = 1
            })
        }
    }

    /**
     * Adjust color alpha for subtle backgrounds
     */
    private fun adjustColorAlpha(color: Int, alpha: Float): Int {
        val r = android.graphics.Color.red(color)
        val g = android.graphics.Color.green(color)
        val b = android.graphics.Color.blue(color)
        return android.graphics.Color.argb((255 * alpha).toInt(), r, g, b)
    }

    /**
     * Create statistics section - REMOVED per user request
     */
    private fun createStatisticsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE  // Hidden section
        }
    }

    /**
     * Create stat card
     */
    private fun createStatCard(number: String, label: String, accentColor: Int): LinearLayout {
        return createModernCard(0xFFFFFFFF.toInt(), 0xFF212121.toInt()) {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            gravity = Gravity.CENTER

            addView(View(this@MainActivity).apply {
                setBackgroundColor(accentColor)
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    dpToPx(4)
                )
            })

            addView(TextView(this@MainActivity).apply {
                text = number
                textSize = 32f
                setTextColor(accentColor)
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(16), 0, dpToPx(8))
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 13f
                setTextColor(0xFF757575.toInt())
                gravity = Gravity.CENTER
                setPadding(dpToPx(12), 0, dpToPx(12), dpToPx(16))
            })
        }
    }

    /**
     * Create features section
     */
    /**
     * Show info dialog
     */
    private fun showInfoDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
        }

        dialogLayout.addView(TextView(this).apply {
            text = "About LumeAI"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, dpToPx(16))
        })

        dialogLayout.addView(TextView(this).apply {
            text =
                "LumeAI is a Transparency-as-a-Service platform that helps banks explain AI decisions, detect bias, and maintain customer trust.\n\n" +
                        "✓ Explain AI decisions in simple language\n" +
                        "✓ Detect algorithmic bias automatically\n" +
                        "✓ Multi-language support (En, Hi, Te)\n" +
                        "✓ Works with multiple banks\n" +
                        "✓ Complete audit trails\n\n" +
                        "Built for Fintech Hackathon 2025"
            textSize = 14f
            setLineSpacing(0f, 1.4f)
        })

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Create modern card with soft pastel gradient
     */
    private fun createModernCard(
        bgColor: Int,
        textColor: Int,
        content: LinearLayout.() -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(3).toFloat()
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            // Create soft pastel gradient background
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(16).toFloat()
            shape.colors = intArrayOf(
                0xFFFAFBFF.toInt(),  // Very light blue-white
                0xFFF5F7FB.toInt()   // Light gray-blue
            )
            shape.gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
            shape.orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
            background = shape

            content()
        }
    }

    /**
     * Add vertical space
     */
    private fun addSpace(parent: LinearLayout, dp: Int) {
        parent.addView(View(this).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                dpToPx(dp)
            )
        })
    }

    /**
     * Add professional floating action button (chatbot)
     * Navy blue theme matching Quick Actions style
     */
    private fun addChatbotFAB() {
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        val fab = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            elevation = dpToPx(6).toFloat()
            layoutParams = FrameLayout.LayoutParams(
                dpToPx(56),
                dpToPx(56)
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                rightMargin = dpToPx(20)
                bottomMargin = dpToPx(20)
            }

            // Navy blue solid background - matching Quick Actions icons (#113D6B)
            val shape = android.graphics.drawable.GradientDrawable()
            shape.shape = android.graphics.drawable.GradientDrawable.OVAL
            shape.setColor(OnCard)  // Navy blue #113D6B - same as Quick Actions
            background = shape

            // Ripple effect
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
                outValue,
                true
            )
            foreground = getDrawable(outValue.resourceId)

            setOnClickListener {
                startActivity(Intent(this@MainActivity, ChatbotActivity::class.java))
            }

            // Chat bubble icon (white bubble with navy dots)
            addView(ImageView(this@MainActivity).apply {
                setImageResource(R.drawable.ic_chat)
                layoutParams = LayoutParams(dpToPx(26), dpToPx(26))
            })
        }

        rootView.addView(fab)
    }

    /**
     * Convert DP to pixels
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Get professional typeface for world-class text rendering
     * Inspired by Google Pay, Revolut, N26, PhonePe
     */
    private fun getTypefaceBold(): android.graphics.Typeface {
        return android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
    }

    private fun getTypefaceMedium(): android.graphics.Typeface {
        return android.graphics.Typeface.create(
            "sans-serif-medium",
            android.graphics.Typeface.NORMAL
        )
    }

    private fun getTypefaceRegular(): android.graphics.Typeface {
        return android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }

    /**
     * Apply professional text rendering to TextView - AGGRESSIVE MODE
     */
    private fun TextView.applyProfessionalText() {
        // Enable anti-aliasing for crisp text
        paintFlags = paintFlags or android.graphics.Paint.ANTI_ALIAS_FLAG
        // Enable subpixel rendering
        paintFlags = paintFlags or android.graphics.Paint.SUBPIXEL_TEXT_FLAG
        // Improve text appearance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            paintFlags = paintFlags or android.graphics.Paint.LINEAR_TEXT_FLAG
        }
        // AGGRESSIVE: Force filter bitmap for sharper rendering
        paintFlags = paintFlags or android.graphics.Paint.FILTER_BITMAP_FLAG
        // AGGRESSIVE: Enable LCD text rendering
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            paintFlags = paintFlags or android.graphics.Paint.EMBEDDED_BITMAP_TEXT_FLAG
        }
        // Force include font padding for consistent rendering
        includeFontPadding = false
        // Slightly increase text contrast
        setTextColor(
            android.graphics.Color.rgb(
                android.graphics.Color.red(currentTextColor),
                android.graphics.Color.green(currentTextColor),
                android.graphics.Color.blue(currentTextColor)
            )
        )
    }
}


