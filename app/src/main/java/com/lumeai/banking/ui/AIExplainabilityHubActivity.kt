package com.lumeai.banking.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.lumeai.banking.DecisionManager
import com.lumeai.banking.models.FirebaseDecision
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ⭐ AIExplainabilityHubActivity - World-Class AI Explainability
 * Shows all decisions with focus on AI explanations
 */
class AIExplainabilityHubActivity : AppCompatActivity() {
    
    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var filterChipContainer: LinearLayout
    private var currentFilterIndex = 0
    private var allDecisions: List<FirebaseDecision> = emptyList()
    private var currentLanguage = "en"
    private var realtimeListenerJob: kotlinx.coroutines.Job? = null
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // CRITICAL: Always load current language preference first
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        setContentView(createUI())
        loadDecisions()
        
        // 🔥 START REAL-TIME LISTENER: Auto-refresh when new decisions arrive
        startRealtimeDecisionListener()
    }
    
    override fun onResume() {
        super.onResume()
        loadDecisions()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up real-time listener
        realtimeListenerJob?.cancel()
        android.util.Log.d("AIExplainabilityHub", "🛑 Real-time listener stopped")
    }
    
    /**
     * 🔥 REAL-TIME LISTENER: Auto-refresh when new decisions arrive
     */
    private fun startRealtimeDecisionListener() {
        realtimeListenerJob?.cancel()
        
        realtimeListenerJob = lifecycleScope.launch {
            try {
                val customerId = com.lumeai.banking.FirebaseListenerService.getCustomerId(this@AIExplainabilityHubActivity)
                android.util.Log.d("AIExplainabilityHub", "🔥 Starting real-time listener for customer: $customerId")
                
                com.lumeai.banking.FirebaseSyncManager.listenForCustomerDecisions(customerId)
                    .collect { newDecision ->
                        android.util.Log.d("AIExplainabilityHub", "🔔 NEW DECISION RECEIVED: ${newDecision.id}")
                        
                        // Auto-refresh the list
                        runOnUiThread {
                            android.util.Log.d("AIExplainabilityHub", "🔄 Auto-refreshing decision list...")
                            loadDecisions()
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("AIExplainabilityHub", "❌ Real-time listener error: ${e.message}", e)
            }
        }
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
            // Reduced top padding for compact header
            setPadding(0, dpToPx(120), 0, 0)  // Reduced from 160
        }
        
        // Info Banner
        rootLayout.addView(createInfoBanner())
        
        // Filter Section
        rootLayout.addView(createFilterSection())
        
        // Loading Indicator
        loadingIndicator = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = dpToPx(30)
                bottomMargin = dpToPx(30)
            }
        }
        rootLayout.addView(loadingIndicator)
        
        // Content Layout for decisions
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
        }
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
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))  // More compact
            
            // Header row with back button and title inline
            addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                // Modern back button - icon only
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = "←"
                    textSize = 24f  // Smaller
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                    layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40))  // Smaller
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    
                    // Add ripple effect on touch
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    
                    setOnClickListener { finish() }
                })
                
                // Title - smaller and no subtitle
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = getString(currentLanguage, "title")
                    textSize = 18f  // Reduced from 20f
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
            })
            
            // Removed subtitle - info card explains everything
        }
    }
    
    private fun createInfoBanner(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))  // Reduced padding
            gravity = Gravity.CENTER_VERTICAL
            
            // Modern card with blue border (matching language button style)
            val cardShape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.WHITE)
                setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)  // Same blue border as language buttons
            }
            background = cardShape
            
            // Reduced margins
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(dpToPx(20), dpToPx(8), dpToPx(20), dpToPx(8))  // Reduced top margin from 16 to 8
            layoutParams = params
            
            elevation = dpToPx(1).toFloat()
            
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = "💡"
                textSize = 20f  // Slightly smaller
                setPadding(0, 0, dpToPx(10), 0)
            })
            
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = "Tap any decision to see detailed AI explanation, bias analysis, and improvement suggestions"
                textSize = 12f  // Reduced from 13f
                setTextColor(AppTheme.Text.OnCard)  // Same dark blue as buttons
                setLineSpacing(dpToPx(2).toFloat(), 1.2f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
        }
    }
    
    /**
     * Modern chip-style filter section (replaces old dropdown)
     */
    private fun createFilterSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(8))  // Reduced padding
            
            // Title
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = "Show:"
                textSize = 12f  // Slightly smaller
                setTextColor(AppTheme.Text.Secondary)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(6))  // Reduced from 8
            })
            
            // Horizontal scroll for chips
            val scrollView = HorizontalScrollView(this@AIExplainabilityHubActivity).apply {
                isHorizontalScrollBarEnabled = false
            }
            
            filterChipContainer = LinearLayout(this@AIExplainabilityHubActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            
            updateFilterChips()
            
            scrollView.addView(filterChipContainer)
            addView(scrollView)
        }
    }
    
    /**
     * Update filter chips based on current selection
     */
    private fun updateFilterChips() {
        filterChipContainer.removeAllViews()
        
        val filters = listOf(
            mapOf("en" to "All", "hi" to "सभी", "te" to "అన్నీ"),
            mapOf("en" to "Bias Only", "hi" to "केवल पूर्वाग्रह", "te" to "బయాస్ మాత్రమే"),
            mapOf("en" to "Denied", "hi" to "अस्वीकृत", "te" to "తిరస్కరించబడింది"),
            mapOf("en" to "Approved", "hi" to "स्वीकृत", "te" to "ఆమోదించబడింది")
        )
        
        filters.forEachIndexed { index, labels ->
            filterChipContainer.addView(createFilterChip(
                label = labels[currentLanguage] ?: labels["en"]!!,
                isSelected = index == currentFilterIndex,
                index = index
            ))
            
            if (index < filters.size - 1) {
                filterChipContainer.addView(View(this@AIExplainabilityHubActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(8), 0)
                })
            }
        }
    }
    
    /**
     * Create modern filter chip button - uses same color as language buttons
     */
    private fun createFilterChip(label: String, isSelected: Boolean, index: Int): TextView {
        return TextView(this).apply {
            text = label
            textSize = 14f
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))
            
            val chipShape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)  // Same as language button selected
                } else {
                    setColor(Color.WHITE)
                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)  // Same border as language buttons
                }
            }
            background = chipShape
            
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            
            isClickable = true
            isFocusable = true
            setOnClickListener {
                if (currentFilterIndex != index) {
                    currentFilterIndex = index
                    filterDecisions(index)
                    updateFilterChips() // Simple refresh
                }
            }
            
            // Add ripple effect
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = ContextCompat.getDrawable(context, outValue.resourceId)
        }
    }
    
    private fun loadDecisions() {
        android.util.Log.d("AIExplainabilityHub", "🔄 Loading decisions for AI Hub...")
        loadingIndicator.visibility = View.VISIBLE
        contentLayout.removeAllViews()
        
        lifecycleScope.launch {
            try {
                allDecisions = DecisionManager.getAllDecisions(this@AIExplainabilityHubActivity, forceRefresh = true)
                android.util.Log.d("AIExplainabilityHub", "📊 Loaded ${allDecisions.size} decisions")
                
                runOnUiThread {
                    android.util.Log.d("AIExplainabilityHub", "🎨 Displaying decisions...")
                    loadingIndicator.visibility = View.GONE
                    displayDecisions(allDecisions)
                }
            } catch (e: Exception) {
                android.util.Log.e("AIExplainabilityHub", "❌ Failed to load: ${e.message}", e)
                e.printStackTrace()
                runOnUiThread {
                    loadingIndicator.visibility = View.GONE
                    showEmptyState("Failed to load decisions")
                }
            }
        }
    }
    
    private fun filterDecisions(filterIndex: Int) {
        val filtered = when (filterIndex) {
            1 -> allDecisions.filter { it.biasDetected }
            2 -> allDecisions.filter { it.outcome.equals("DENIED", ignoreCase = true) }
            3 -> allDecisions.filter { it.outcome.equals("APPROVED", ignoreCase = true) }
            else -> allDecisions
        }
        displayDecisions(filtered)
    }
    
    private fun displayDecisions(decisions: List<FirebaseDecision>) {
        contentLayout.removeAllViews()
        
        if (decisions.isEmpty()) {
            showEmptyState(getString(currentLanguage, "no_decisions"))
            return
        }
        
        // Header with count
        contentLayout.addView(TextView(this).apply {
            text = "${decisions.size} AI Decision${if (decisions.size != 1) "s" else ""} to Explain"
            textSize = 18f
            setTextColor(AppTheme.Text.OnCard)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dpToPx(4), 0, 0, dpToPx(16))
        })
        
        // Display each decision
        decisions.forEach { decision ->
            contentLayout.addView(createExplainabilityCard(decision))
            contentLayout.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(16))
            })
        }
    }
    
    private fun createExplainabilityCard(decision: FirebaseDecision): LinearLayout {
        // Debug: Log the outcome to check data integrity
        android.util.Log.d("AIExplainabilityHub", "🔍 Decision ${decision.id}: outcome='${decision.outcome}', biasDetected=${decision.biasDetected}")
        
        val (statusIcon, statusText, statusColor) = when {
            decision.outcome.equals("DENIED", ignoreCase = true) ->
                Triple("❌", "DENIED", 0xFFEF4444.toInt())
            decision.outcome.equals("APPROVED", ignoreCase = true) ->
                Triple("✅", "APPROVED", 0xFF10B981.toInt())
            else ->
                Triple("⏳", "PENDING", 0xFFF59E0B.toInt())
        }
        
        var isExpanded = false
        var expandedContent: LinearLayout? = null
        var tapToViewText: TextView? = null
        
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            
            // Modern white card with rounded corners
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(16).toFloat()
            shape.setColor(0xFFFFFFFF.toInt())
            background = shape
            
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(4).toFloat()
            
            // Header: Decision Info
            addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = "🏦 ${decision.bankName}"
                    textSize = 18f
                    setTextColor(AppTheme.Text.OnCard)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
                
                // Status badge with colored background
                addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
                    
                    val badgeShape = android.graphics.drawable.GradientDrawable()
                    badgeShape.cornerRadius = dpToPx(8).toFloat()
                    badgeShape.setColor(AppTheme.withOpacity(statusColor, 0.15f))
                    background = badgeShape
                    
                    addView(TextView(this@AIExplainabilityHubActivity).apply {
                        text = "$statusIcon $statusText"
                        textSize = 13f
                        setTextColor(statusColor)
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    })
                })
            })
            
            if (decision.loanType.isNotEmpty()) {
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = formatLoanType(decision.loanType)
                    textSize = 14f
                    setTextColor(AppTheme.Text.OnCardSecondary)
                    setPadding(0, dpToPx(4), 0, 0)
                })
            }
            
            // Bias Warning Banner - Modern Design
            if (decision.biasDetected) {
                addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
                    
                    // Modern gradient background matching theme
                    val biasShape = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = dpToPx(10).toFloat()
                        setColor(AppTheme.StatusBg.Warning) // Theme warning color
                        setStroke(dpToPx(1), AppTheme.Status.Warning)
                    }
                    background = biasShape
                    
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = dpToPx(12)
                    layoutParams = params
                    
                    addView(TextView(this@AIExplainabilityHubActivity).apply {
                        text = "⚠️"
                        textSize = 20f
                        setPadding(0, 0, dpToPx(10), 0)
                    })
                    
                    addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        
                        addView(TextView(this@AIExplainabilityHubActivity).apply {
                            text = "⚖️ Bias Detected"
                            textSize = 14f
                            setTextColor(AppTheme.Status.Warning)
                            setTypeface(null, android.graphics.Typeface.BOLD)
                        })
                        
                        addView(TextView(this@AIExplainabilityHubActivity).apply {
                            text = "${decision.biasSeverity} risk - Flagged for review"
                            textSize = 12f
                            setTextColor(AppTheme.Text.OnCard)
                            setPadding(0, dpToPx(2), 0, 0)
                        })
                    })
                })
            }
            
            // Explanation Preview - Show summary preview (bias details will be in expanded content only)
            val previewText = run {
                val summary = LanguageHelper.getSummary(decision, currentLanguage)
                if (summary.isNotEmpty()) {
                    summary.take(150) + if (summary.length > 150) "..." else ""
                } else ""
            }
            
            if (previewText.isNotEmpty()) {
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = previewText
                    textSize = 13f
                    setTextColor(0xFF374151.toInt())
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = dpToPx(12)
                    layoutParams = params
                })
            }
            
            // Timestamp
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                text = sdf.format(Date(decision.timestamp))
                textSize = 11f
                setTextColor(0xFF999999.toInt())
                setPadding(0, dpToPx(12), 0, dpToPx(8))
            })
            
            // Call to Action
            tapToViewText = TextView(this@AIExplainabilityHubActivity).apply {
                text = getString(currentLanguage, "tap_to_view")
                textSize = 13f
                setTextColor(0xFF9C27B0.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(4), 0, 0)
            }
            addView(tapToViewText)
            
            // Expandable content (initially hidden)
            expandedContent = LinearLayout(this@AIExplainabilityHubActivity).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(12))
                
                // Styled background for expanded section
                val expandedBg = android.graphics.drawable.GradientDrawable().apply {
                    setColor(0xFFFAFAFA.toInt())
                    cornerRadius = dpToPx(12).toFloat()
                    setStroke(dpToPx(1), 0xFFE0E0E0.toInt())
                }
                background = expandedBg
                
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.topMargin = dpToPx(12)
                layoutParams = params
                
                // IF BIAS DETECTED - Modern professional design
                if (decision.biasDetected) {
                    // Modern bias alert card
                    addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                        
                        // Modern gradient background for bias - subtle and professional
                        val biasCard = android.graphics.drawable.GradientDrawable().apply {
                            cornerRadius = dpToPx(12).toFloat()
                            setColor(AppTheme.StatusBg.Warning) // Light warning color
                            setStroke(dpToPx(2), AppTheme.Status.Warning) // Warning border
                        }
                        background = biasCard
                        
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.topMargin = dpToPx(16)
                        params.bottomMargin = dpToPx(16)
                        layoutParams = params
                        
                        // Header with icon and title - clean layout
                        addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(0, 0, 0, dpToPx(12))
                            
                            addView(TextView(this@AIExplainabilityHubActivity).apply {
                                text = "⚠️"
                                textSize = 24f
                                setPadding(0, 0, dpToPx(12), 0)
                            })
                            
                            addView(TextView(this@AIExplainabilityHubActivity).apply {
                                text = "⚖️ Bias Detected - High Risk"
                                textSize = 16f
                                setTextColor(AppTheme.Status.Warning)
                                setTypeface(null, android.graphics.Typeface.BOLD)
                            })
                        })
                        
                        // Bias message in clean modern format
                        addView(TextView(this@AIExplainabilityHubActivity).apply {
                            text = decision.biasMessage.ifEmpty { 
                                "CRITICAL: All eligibility criteria were met, but the loan was still denied. This suggests potential human bias or discriminatory decision-making that requires immediate review." 
                            }
                            textSize = 14f
                            setTextColor(AppTheme.Text.OnCard)
                            setLineSpacing(dpToPx(4).toFloat(), 1.4f)
                            setPadding(0, 0, 0, dpToPx(12))
                        })
                        
                        // Affected groups - modern chip-style design
                        if (decision.biasAffectedGroups.isNotEmpty()) {
                            addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.CENTER_VERTICAL
                                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                                
                                val groupsBg = android.graphics.drawable.GradientDrawable().apply {
                                    cornerRadius = dpToPx(8).toFloat()
                                    setColor(0xFFFFFFFF.toInt()) // White background
                                }
                                background = groupsBg
                                
                                addView(TextView(this@AIExplainabilityHubActivity).apply {
                                    text = "👥 "
                                    textSize = 16f
                                })
                                
                                addView(TextView(this@AIExplainabilityHubActivity).apply {
                                    text = "Potentially Affected: ${decision.biasAffectedGroups.replace(",", ", ")}"
                                    textSize = 13f
                                    setTextColor(AppTheme.Text.OnCard)
                                    setTypeface(null, android.graphics.Typeface.BOLD)
                                })
                            })
                        }
                    })
                }
                
                // Full explanation - Notification-style Card
                val fullSummary = LanguageHelper.getSummary(decision, currentLanguage)
                if (fullSummary.isNotEmpty()) {
                    // Card container (like notification cards)
                    addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                        
                        // Clean white card with subtle shadow (like decision cards)
                        val cardShape = android.graphics.drawable.GradientDrawable().apply {
                            cornerRadius = dpToPx(12).toFloat()
                            setColor(Color.WHITE)
                        }
                        background = cardShape
                        
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.topMargin = dpToPx(16)
                        params.bottomMargin = dpToPx(16)
                        layoutParams = params
                        
                        elevation = dpToPx(2).toFloat()  // Subtle shadow like notification cards
                        
                        // Header with icon
                        addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(0, 0, 0, dpToPx(12))
                            
                            addView(TextView(this@AIExplainabilityHubActivity).apply {
                                text = "🤖"
                                textSize = 20f
                                setPadding(0, 0, dpToPx(10), 0)
                            })
                            
                            addView(TextView(this@AIExplainabilityHubActivity).apply {
                                text = "AI Explanation"
                                textSize = 16f
                                setTextColor(AppTheme.Text.OnCard)  // Dark blue text
                                setTypeface(null, android.graphics.Typeface.BOLD)
                            })
                        })
                        
                        // Summary text
                        addView(TextView(this@AIExplainabilityHubActivity).apply {
                            text = fullSummary
                            textSize = 14f
                            setTextColor(AppTheme.Text.OnCard)
                            setLineSpacing(dpToPx(4).toFloat(), 1.4f)
                        })
                    })
                }
                
                // Key factors in a clean grid
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = "\n🔑 Key Decision Factors"
                    textSize = 15f
                    setTextColor(0xFF212121.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, dpToPx(16), 0, dpToPx(12))
                })
                
                // Create factor cards
                val factorsAdded = mutableListOf<Boolean>()
                
                if (decision.creditScore > 0) {
                    addView(createFactorCard(
                        passed = decision.creditScorePassed,
                        label = "Credit Score",
                        actual = decision.creditScore.toString(),
                        required = decision.creditScoreRequired.toString()
                    ))
                    factorsAdded.add(true)
                }
                
                if (decision.income > 0) {
                    addView(createFactorCard(
                        passed = decision.incomePassed,
                        label = "Monthly Income",
                        actual = "₹${String.format("%,.0f", decision.income)}",
                        required = "₹${String.format("%,.0f", decision.incomeRequired)}"
                    ))
                    factorsAdded.add(true)
                }
                
                if (decision.debtRatio > 0) {
                    addView(createFactorCard(
                        passed = decision.debtRatioPassed,
                        label = "Debt-to-Income Ratio",
                        actual = "${String.format("%.1f%%", decision.debtRatio * 100)}",
                        required = "Max ${String.format("%.1f%%", decision.debtRatioRequired * 100)}"
                    ))
                    factorsAdded.add(true)
                }
                
                if (decision.employmentMonths > 0) {
                    addView(createFactorCard(
                        passed = decision.employmentPassed,
                        label = "Employment Duration",
                        actual = "${decision.employmentMonths} months",
                        required = "${decision.employmentMonthsRequired} months"
                    ))
                    factorsAdded.add(true)
                }
                
                if (decision.digitalFootprint.isNotEmpty()) {
                    addView(createFactorCard(
                        passed = decision.digitalFootprintPassed,
                        label = "Digital Footprint",
                        actual = decision.digitalFootprint.replaceFirstChar { it.uppercase() },
                        required = decision.digitalFootprintRequired.replaceFirstChar { it.uppercase() }
                    ))
                    factorsAdded.add(true)
                }
                
                // NEXT STEPS - Show appropriate actions based on outcome
                val isDenied = decision.outcome.equals("DENIED", ignoreCase = true)
                val isApproved = decision.outcome.equals("APPROVED", ignoreCase = true)
                
                // Check if there are failed factors (for denied decisions)
                val hasFailedFactors = !listOf(
                    decision.creditScorePassed,
                    decision.incomePassed,
                    decision.debtRatioPassed,
                    decision.employmentPassed,
                    decision.digitalFootprintPassed
                ).all { it }
                
                // Show "What's Next?" section for DENIED (including bias cases) or APPROVED
                if (isDenied || isApproved) {
                    // Divider
                    addView(View(this@AIExplainabilityHubActivity).apply {
                        setBackgroundColor(0xFFE0E0E0.toInt())
                        val dividerParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                        )
                        dividerParams.topMargin = dpToPx(20)
                        dividerParams.bottomMargin = dpToPx(16)
                        layoutParams = dividerParams
                    })
                    
                    // Section Header
                    addView(TextView(this@AIExplainabilityHubActivity).apply {
                        text = when {
                            isApproved -> "🎉 Congratulations!"
                            decision.biasDetected -> "⚖️ Your Options"
                            else -> "💡 What's Next?"
                        }
                        textSize = 16f
                        setTextColor(0xFF212121.toInt())
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(0, 0, 0, dpToPx(12))
                    })
                    
                    addView(TextView(this@AIExplainabilityHubActivity).apply {
                        text = when {
                            isApproved -> "Explore additional products and benefits tailored for you:"
                            decision.biasDetected -> "This decision has been flagged for review. Meanwhile, explore these options:"
                            else -> "While this application was denied, you have options:"
                        }
                        textSize = 13f
                        setTextColor(0xFF666666.toInt())
                        setPadding(0, 0, 0, dpToPx(16))
                    })
                    
                    // 1. IMPROVEMENT PATHS (for DENIED with failed factors OR bias detected)
                    if (isDenied && (hasFailedFactors || decision.biasDetected)) {
                        addView(createActionButton(
                            icon = "🚀",
                            title = if (decision.biasDetected) "Appeal This Decision" else "View Improvement Paths",
                            subtitle = if (decision.biasDetected) {
                                "Since you met all criteria, we'll help you appeal this decision"
                            } else {
                                "See AI-generated paths to approval for this application"
                            },
                            gradientColors = intArrayOf(0xFF667EEA.toInt(), 0xFF764BA2.toInt()),
                            onClick = {
                                if (decision.biasDetected) {
                                    // Show appeal dialog for bias-detected decisions
                                    showAppealDialog(decision)
                                } else {
                                    // Navigate to improvement paths for regular denials
                                    val intent = android.content.Intent(this@AIExplainabilityHubActivity, com.lumeai.banking.ui.PathToApprovalActivity::class.java)
                                    intent.putExtra("DECISION_ID", decision.id)
                                    intent.putExtra("AUTO_EXPAND_FIRST", true)
                                    startActivity(intent)
                                }
                            }
                        ))
                    }
                    
                    // 2. PERSONALIZED OFFERS (show for BOTH denied AND approved)
                    addView(createActionButton(
                        icon = "🎁",
                        title = if (isApproved) "Explore More Products" else "View Alternative Offers",
                        subtitle = if (isApproved) {
                            "Discover additional products and upgrades personalized for you"
                        } else {
                            "See alternative products you're pre-approved for right now"
                        },
                        gradientColors = intArrayOf(0xFFF093FB.toInt(), 0xFFF5576C.toInt()),
                        onClick = {
                            val intent = android.content.Intent(this@AIExplainabilityHubActivity, com.lumeai.banking.ui.PersonalizedOffersActivity::class.java)
                            intent.putExtra("FROM_DECISION", decision.id)
                            intent.putExtra("LOAN_AMOUNT", decision.loanAmount)
                            intent.putExtra("BANK_NAME", decision.bankName)
                            intent.putExtra("LOAN_TYPE", decision.loanType)
                            intent.putExtra("OUTCOME", decision.outcome)
                            startActivity(intent)
                        }
                    ))
                }
            }
            addView(expandedContent)
            
            // Click handler to toggle expansion
            setOnClickListener {
                isExpanded = !isExpanded
                expandedContent?.visibility = if (isExpanded) View.VISIBLE else View.GONE
                tapToViewText?.text = if (isExpanded) {
                    "👆 Tap to collapse"
                } else {
                    getString(currentLanguage, "tap_to_view")
                }
            }
        }
    }
    
    private fun createFactorCard(passed: Boolean, label: String, actual: String, required: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
            
            val cardBg = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(if (passed) 0xFFEFF6FF.toInt() else 0xFFF5F3FF.toInt()) // Light blue for passed, lavender for failed
            }
            background = cardBg
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dpToPx(8)
            layoutParams = params
            
            // Status icon
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = if (passed) "✅" else "❌"
                textSize = 16f
                setPadding(0, 0, dpToPx(10), 0)
            })
            
            // Content
            addView(LinearLayout(this@AIExplainabilityHubActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = label
                    textSize = 13f
                    setTextColor(0xFF666666.toInt())
                })
                
                addView(TextView(this@AIExplainabilityHubActivity).apply {
                    text = "$actual ${if (passed) "✓" else "✗"} Required: $required"
                    textSize = 14f
                    setTextColor(0xFF212121.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, dpToPx(2), 0, 0)
                })
            })
        }
    }
    
    private fun showEmptyState(message: String) {
        contentLayout.removeAllViews()
        
        contentLayout.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(40), dpToPx(60), dpToPx(40), dpToPx(60))
            
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = "💡"
                textSize = 64f
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = message
                textSize = 16f
                setTextColor(0xFF666666.toInt())
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(20), 0, 0)
            })
        })
    }
    
    private fun createActionButton(
        icon: String,
        title: String,
        subtitle: String,
        gradientColors: IntArray,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            
            val buttonBg = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                colors = gradientColors
                orientation = android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
            }
            background = buttonBg
            isClickable = true
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dpToPx(12)
            layoutParams = params
            
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = "$icon $title"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
            })
            
            addView(TextView(this@AIExplainabilityHubActivity).apply {
                text = subtitle
                textSize = 12f
                setTextColor(0xFFE0E0E0.toInt())
                gravity = android.view.Gravity.CENTER
                setPadding(0, dpToPx(4), 0, 0)
            })
            
            setOnClickListener {
                onClick()
            }
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
     * Show modern appeal dialog for bias-detected decisions
     * Follows world-class banking app patterns
     */
    private fun showAppealDialog(decision: FirebaseDecision) {
        val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog)
            .setTitle("⚖️ Appeal This Decision")
            .setMessage("""
                ${decision.bankName} denied your application despite meeting all criteria.
                
                📋 Decision ID: ${decision.id}
                💰 Amount: ₹${decision.loanAmount.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}
                📅 Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(decision.timestamp))}
                
                🔍 Why This Matters:
                Our AI detected that all eligibility requirements were met, yet the application was denied. This suggests potential bias or discriminatory practices.
                
                ⚖️ Your Rights:
                • Right to appeal under RBI guidelines
                • Right to full explanation
                • Right to human review
                • Protection against discrimination
                
                📞 Next Steps:
                Choose how you'd like to proceed:
            """.trimIndent())
            .setPositiveButton("📝 Submit Appeal Request") { _, _ ->
                submitAppealRequest(decision)
            }
            .setNeutralButton("📞 Contact Bank") { _, _ ->
                showContactBankOptions(decision)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
        
        // Style the dialog buttons
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(AppTheme.Primary.Blue)
        dialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL)?.setTextColor(AppTheme.Text.OnCard)
    }
    
    /**
     * Submit appeal request - flags decision in Firebase
     */
    private fun submitAppealRequest(decision: FirebaseDecision) {
        lifecycleScope.launch {
            try {
                // Update Firebase to flag appeal
                com.lumeai.banking.FirebaseSyncManager.submitAppeal(decision.id, decision.customerId)
                
                // Show confirmation
                android.app.AlertDialog.Builder(this@AIExplainabilityHubActivity)
                    .setTitle("✅ Appeal Submitted")
                    .setMessage("""
                        Your appeal has been registered successfully.
                        
                        Reference ID: APPEAL-${System.currentTimeMillis() % 100000}
                        Status: Under Review
                        
                        What Happens Next:
                        ⏱️ Review within 7 business days
                        📧 Email confirmation sent
                        📞 Bank may contact you for details
                        ⚖️ Independent review conducted
                        
                        Expected Timeline:
                        • Initial review: 3-5 days
                        • Final decision: 7-14 days
                        • Appeal outcome notification via email & app
                        
                        You can track your appeal status in the app.
                    """.trimIndent())
                    .setPositiveButton("Got It") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(this@AIExplainabilityHubActivity, "Appeal tracking added to dashboard", Toast.LENGTH_LONG).show()
                    }
                    .show()
                    
            } catch (e: Exception) {
                Toast.makeText(this@AIExplainabilityHubActivity, "Failed to submit appeal. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Show contact options for the bank
     */
    private fun showContactBankOptions(decision: FirebaseDecision) {
        val options = arrayOf(
            "📞 Call ${decision.bankName}",
            "📧 Email Customer Service",
            "💬 Live Chat",
            "🏦 Visit Branch"
        )
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Contact ${decision.bankName}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Call bank
                        val phoneNumber = "1800-XXX-XXXX" // Demo number
                        Toast.makeText(this, "Calling $phoneNumber...", Toast.LENGTH_SHORT).show()
                        // In production: val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                        // startActivity(intent)
                    }
                    1 -> {
                        // Email
                        Toast.makeText(this, "Opening email client...", Toast.LENGTH_SHORT).show()
                        // In production: val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@bank.com"))
                        // startActivity(intent)
                    }
                    2 -> {
                        // Live chat
                        Toast.makeText(this, "Opening live chat...", Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        // Branch locator
                        Toast.makeText(this, "Finding nearest branch...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(20), dpToPx(15), dpToPx(10), dpToPx(10))
            gravity = Gravity.END
            setBackgroundColor(0xFFFFFFFF.toInt())
            
            val languages = listOf("English" to "en", "हिंदी" to "hi", "తెలుగు" to "te")
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(android.widget.Space(this@AIExplainabilityHubActivity).apply {
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
            
            val isSelected = currentLanguage == code
            val shape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)  // Dark blue when selected
                } else {
                    setColor(0xFFFFFFFF.toInt())
                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)  // Blue border when not selected
                }
            }
            background = shape
            setTextColor(if (isSelected) 0xFFFFFFFF.toInt() else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    // Save language preference using LanguageHelper
                    LanguageHelper.setLanguage(this@AIExplainabilityHubActivity, code)
                    recreate()
                }
            }
        }
    }
    
    private fun getString(lang: String, key: String): String {
        val strings = mapOf(
            "title" to mapOf(
                "en" to "AI Explainability Hub",
                "hi" to "AI व्याख्या केंद्र",
                "te" to "AI వివరణ కేంద్రం"
            ),
            "subtitle" to mapOf(
                "en" to "Understand every AI decision made about you",
                "hi" to "आपके बारे में लिए गए हर AI निर्णय को समझें",
                "te" to "మీ గురించి తీసుకున్న ప్रతి AI నిర్ణయాన్ని అర్థం చేసుకోండి"
            ),
            "info_banner" to mapOf(
                "en" to "Tap any decision to see detailed AI explanation, bias analysis, and improvement suggestions",
                "hi" to "विस्तृत AI व्याख्या, पूर्वाग्रह विश्लेषण और सुधार सुझाव देखने के लिए किसी भी निर्णय पर टैप करें",
                "te" to "వివరణాత్మక AI వివరణ, పక్షపాత విశ్లేషణ మరియు మెరుగుదల సూచనలను చూడటానికి ఏదైనా నిర్ణయాన్ని నొక్కండి"
            ),
            "filter_label" to mapOf(
                "en" to "Show: ",
                "hi" to "दिखाएं: ",
                "te" to "చూపించు: "
            ),
            "no_decisions" to mapOf(
                "en" to "No decisions yet.\nWhen banks make AI decisions about you, they'll appear here with full explanations.",
                "hi" to "अभी तक कोई निर्णय नहीं।\nजब बैंक आपके बारे में AI निर्णय लेते हैं, तो वे यहां पूर्ण व्याख्या के साथ दिखाई देंगे।",
                "te" to "ఇంకా నిర్ణయాలు లేవు.\nబ్యాంకులు మీ గురించి AI నిర్ణయాలు తీసుకున్నప్పుడు, అవి పూర్తి వివరణలతో ఇక్కడ కనిపిస్తాయి."
            ),
            "tap_to_view" to mapOf(
                "en" to "👆 Tap to view full explanation",
                "hi" to "👆 पूर्ण व्याख्या देखने के लिए टैप करें",
                "te" to "👆 పూర్తి వివరణ చూడటానికి నొక్కండి"
            )
        )
        return strings[key]?.get(lang) ?: strings[key]?.get("en") ?: key
    }
}

