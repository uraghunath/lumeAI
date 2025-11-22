package com.lumeai.banking.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lumeai.banking.BankSummary
import com.lumeai.banking.DecisionManager
import com.lumeai.banking.UserStats
import com.lumeai.banking.models.FirebaseDecision
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * ⭐ DashboardActivity - World-Class Analytics Dashboard
 * Real Firebase data with beautiful grouping, stats, and filters
 */
class DashboardActivity : AppCompatActivity() {
    
    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var statsCard: LinearLayout
    
    private var allDecisions: List<FirebaseDecision> = emptyList()
    private var userStats: UserStats? = null
    private var bankSummaries: List<BankSummary> = emptyList()
    private var currentLanguage = "en"
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    private fun getString(key: String): String {
        val translations = mapOf(
            "total_decisions" to mapOf("en" to "Total\nDecisions", "hi" to "कुल\nनिर्णय", "te" to "మొత్తం\nనిర్ణయాలు"),
            "banks_connected" to mapOf("en" to "Banks\nConnected", "hi" to "बैंक\nकनेक्टेड", "te" to "బ్యాంకులు\nకనెక్ట్"),
            "approved" to mapOf("en" to "Approved", "hi" to "स्वीकृत", "te" to "ఆమోదించబడింది"),
            "denied" to mapOf("en" to "Denied", "hi" to "अस्वीकृत", "te" to "తిరస్కరించబడింది"),
            "pending" to mapOf("en" to "Pending", "hi" to "लंबित", "te" to "పెండింగ్"),
            "bias_detected" to mapOf("en" to "Bias\nDetected", "hi" to "पूर्वाग्रह\nपाया गया", "te" to "పక్షపాతం\nకనుగొనబడింది"),
            "approval_rate" to mapOf("en" to "Approval\nRate", "hi" to "अनुमोदन\nदर", "te" to "ఆమోద\nరేటు"),
            "decisions_by_bank" to mapOf("en" to "Decisions by Bank", "hi" to "बैंक के अनुसार निर्णय", "te" to "బ్యాంక్ ద్వారా నిర్ణయాలు"),
            "overview" to mapOf("en" to "📈 Overview", "hi" to "📈 सारांश", "te" to "📈 అవలోకనం"),
            "more_decisions" to mapOf("en" to "+ %d more decisions", "hi" to "+ %d और निर्णय", "te" to "+ %d మరిన్ని నిర్ణయాలు"),
            "decisions_count" to mapOf("en" to "%d decisions", "hi" to "%d निर्णय", "te" to "%d నిర్ణయాలు")
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
        loadDashboardData()
    }
    
    override fun onResume() {
        super.onResume()
        loadDashboardData()
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
            // Compact top padding (same as other pages)
            setPadding(0, dpToPx(120), 0, 0)
        }
        
        // Loading Indicator
        loadingIndicator = ProgressBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                topMargin = dpToPx(20)  // Reduced
                bottomMargin = dpToPx(20)  // Reduced
            }
        }
        rootLayout.addView(loadingIndicator)
        
        // Content Container
        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(12), dpToPx(20), dpToPx(20))  // Reduced top padding
        }
        
        // Stats Card (Overview)
        statsCard = createStatsCard()
        contentContainer.addView(statsCard)
        
        contentContainer.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(12))  // Reduced spacing
        })
        
        // Decisions Content
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        contentContainer.addView(contentLayout)
        
        rootLayout.addView(contentContainer)
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
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))  // Compact
            elevation = dpToPx(2).toFloat()
            
            // Header row with back button and title inline
            addView(LinearLayout(this@DashboardActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                
                // Modern back button - compact
                addView(TextView(this@DashboardActivity).apply {
                    text = "←"
                    textSize = 24f  // Smaller
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                    layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40))  // Smaller
                    gravity = android.view.Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    
                    // Add ripple effect on touch
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    
                    setOnClickListener { finish() }
                })
                
                // Title - compact
                addView(TextView(this@DashboardActivity).apply {
                    text = "Dashboard"
                    textSize = 18f  // Same as other pages
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
            })
        }
    }
    
    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            gravity = Gravity.END
            elevation = dpToPx(2).toFloat()
            
            addView(createLanguageButton("English", "en"))
            addView(android.widget.Space(this@DashboardActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(8), 0)
            })
            addView(createLanguageButton("हिंदी", "hi"))
            addView(android.widget.Space(this@DashboardActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(8), 0)
            })
            addView(createLanguageButton("తెలుగు", "te"))
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
                    setColor(android.graphics.Color.WHITE)
                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)
                }
            }
            background = shape
            setTextColor(if (isSelected) android.graphics.Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    com.lumeai.banking.utils.LanguageHelper.setLanguage(this@DashboardActivity, code)
                    recreate()
                }
            }
        }
    }
    
    private fun createStatsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(6).toFloat()  // ENHANCED: Increased elevation
            
            // ENHANCED: Add border radius
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(16).toFloat()
            shape.setColor(0xFFFFFFFF.toInt())
            background = shape
            
            addView(TextView(this@DashboardActivity).apply {
                text = getString("overview")
                textSize = 22f  // ENHANCED: Larger title
                setTextColor(0xFF0A0A0A.toInt())  // Darker
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(20))  // More spacing
            })
            
            // Stats Grid
            addView(LinearLayout(this@DashboardActivity).apply {
                orientation = LinearLayout.VERTICAL
                
                // Row 1
                addView(LinearLayout(this@DashboardActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 2f
                    
                    addView(createStatBox("0", getString("total_decisions"), 0xFF2196F3.toInt(), 1f))
                    addView(View(this@DashboardActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dpToPx(12), 0)
                    })
                    addView(createStatBox("0", getString("banks_connected"), AppTheme.Primary.Blue, 1f))
                })
                
                addView(View(this@DashboardActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dpToPx(12))
                })
                
                // Row 2
                addView(LinearLayout(this@DashboardActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 3f
                    
                    addView(createStatBox("0", getString("approved"), AppTheme.Primary.Blue, 1f))
                    addView(View(this@DashboardActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dpToPx(12), 0)
                    })
                    addView(createStatBox("0", getString("denied"), AppTheme.Primary.Blue, 1f))
                    addView(View(this@DashboardActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dpToPx(12), 0)
                    })
                    addView(createStatBox("0", getString("pending"), AppTheme.Primary.Blue, 1f))
                })
                
                addView(View(this@DashboardActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dpToPx(12))
                })
                
                // Row 3
                addView(LinearLayout(this@DashboardActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 2f
                    
                    addView(createStatBox("0", getString("bias_detected"), AppTheme.Primary.Blue, 1f))
                    addView(View(this@DashboardActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dpToPx(12), 0)
                    })
                    addView(createStatBox("0%", getString("approval_rate"), 0xFF6366F1.toInt(), 1f))
                })
            })
        }
    }
    
    private fun createStatBox(value: String, label: String, color: Int, weight: Float): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(20), dpToPx(16), dpToPx(20))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            
            // Clean white card (no colored background)
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(12).toFloat()
            shape.setColor(android.graphics.Color.WHITE)
            shape.setStroke(dpToPx(1), AppTheme.Text.Tertiary)  // Subtle border
            background = shape
            elevation = dpToPx(1).toFloat()
            
            addView(TextView(this@DashboardActivity).apply {
                text = value
                textSize = 32f
                setTextColor(color)  // Only the number is colored
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@DashboardActivity).apply {
                text = label
                textSize = 12f
                setTextColor(AppTheme.Text.OnCard)  // Dark blue text
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(6), 0, 0)
            })
        }
    }
    
    private fun updateStatsCard() {
        // Update stats dynamically
        val container = statsCard.getChildAt(1) as LinearLayout
        
        val row1 = container.getChildAt(0) as LinearLayout
        ((row1.getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text = "${userStats?.totalDecisions ?: 0}"
        ((row1.getChildAt(2) as LinearLayout).getChildAt(0) as TextView).text = "${userStats?.banksCount ?: 0}"
        
        val row2 = container.getChildAt(2) as LinearLayout
        ((row2.getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text = "${userStats?.approvedCount ?: 0}"
        ((row2.getChildAt(2) as LinearLayout).getChildAt(0) as TextView).text = "${userStats?.deniedCount ?: 0}"
        ((row2.getChildAt(4) as LinearLayout).getChildAt(0) as TextView).text = "${userStats?.pendingCount ?: 0}"
        
        val row3 = container.getChildAt(4) as LinearLayout
        ((row3.getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text = "${userStats?.biasDetectedCount ?: 0}"
        ((row3.getChildAt(2) as LinearLayout).getChildAt(0) as TextView).text = "${userStats?.approvalRate?.toInt() ?: 0}%"
    }
    
    private fun loadDashboardData() {
        android.util.Log.d("DashboardActivity", "🔄 Loading dashboard data...")
        loadingIndicator.visibility = View.VISIBLE
        contentLayout.removeAllViews()
        
        lifecycleScope.launch {
            try {
                // Load all data
                allDecisions = DecisionManager.getAllDecisions(this@DashboardActivity, forceRefresh = true)
                android.util.Log.d("DashboardActivity", "📊 Loaded ${allDecisions.size} decisions")
                
                userStats = DecisionManager.getUserStats(this@DashboardActivity, forceRefresh = true)
                android.util.Log.d("DashboardActivity", "📈 Stats - Total: ${userStats?.totalDecisions}, Banks: ${userStats?.banksCount}")
                
                bankSummaries = DecisionManager.getDecisionsByBankGrouped(this@DashboardActivity, forceRefresh = true)
                android.util.Log.d("DashboardActivity", "🏦 Bank summaries: ${bankSummaries.size} banks")
                
                runOnUiThread {
                    android.util.Log.d("DashboardActivity", "🎨 Updating UI...")
                    loadingIndicator.visibility = View.GONE
                    updateStatsCard()
                    displayGroupedDecisions()
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardActivity", "❌ Failed to load dashboard: ${e.message}", e)
                e.printStackTrace()
                runOnUiThread {
                    loadingIndicator.visibility = View.GONE
                    showEmptyState("Failed to load dashboard data")
                }
            }
        }
    }
    
    private fun displayGroupedDecisions() {
        contentLayout.removeAllViews()
        
        if (allDecisions.isEmpty()) {
            showEmptyState("No decisions yet.\nYour dashboard will populate when banks make decisions.")
            return
        }
        
        // Always display by bank (removed toggle)
        displayByBank()
    }
    
    private fun displayByBank() {
        contentLayout.addView(TextView(this).apply {
            text = getString("decisions_by_bank")
            textSize = 18f
            setTextColor(0xFF212121.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dpToPx(4), 0, 0, dpToPx(16))
        })
        
        bankSummaries.forEach { bank ->
            contentLayout.addView(createBankGroup(bank))
            contentLayout.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(16))
            })
        }
    }
    
    private fun createBankGroup(bank: BankSummary): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            elevation = dpToPx(3).toFloat()
            
            // Header
            addView(LinearLayout(this@DashboardActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, dpToPx(12))
                
                addView(TextView(this@DashboardActivity).apply {
                    text = "🏦 ${bank.bankName}"
                    textSize = 20f
                    setTextColor(0xFF1976D2.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
                
                addView(TextView(this@DashboardActivity).apply {
                    text = String.format(getString("decisions_count"), bank.decisions.size)
                    textSize = 13f
                    setTextColor(0xFF666666.toInt())
                })
            })
            
            // Mini Stats
            addView(LinearLayout(this@DashboardActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(12))
                setBackgroundColor(0xFFF5F7FA.toInt())
                
                addView(TextView(this@DashboardActivity).apply {
                    text = "✅ ${bank.approvedCount}"
                    textSize = 12f
                    setTextColor(AppTheme.Primary.Blue)
                    setPadding(dpToPx(8), 0, dpToPx(16), 0)
                })
                
                addView(TextView(this@DashboardActivity).apply {
                    text = "❌ ${bank.deniedCount}"
                    textSize = 12f
                    setTextColor(AppTheme.Primary.Blue)
                    setPadding(0, 0, dpToPx(16), 0)
                })
                
                addView(TextView(this@DashboardActivity).apply {
                    text = "⏳ ${bank.pendingCount}"
                    textSize = 12f
                    setTextColor(AppTheme.Primary.Blue)
                })
            })
            
            // Decision List
            bank.decisions.take(3).forEach { decision ->
                addView(createMiniDecisionCard(decision))
            }
            
            if (bank.decisions.size > 3) {
                val moreDecisionsText = TextView(this@DashboardActivity).apply {
                    text = String.format(getString("more_decisions"), bank.decisions.size - 3)
                    textSize = 12f
                    setTextColor(0xFF1976D2.toInt())
                    gravity = Gravity.CENTER
                    setPadding(0, dpToPx(12), 0, 0)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    isClickable = true
                    isFocusable = true
                }
                
                moreDecisionsText.setOnClickListener {
                    // Get the parent layout (the bank group container)
                    val bankGroupLayout = moreDecisionsText.parent as? LinearLayout
                    if (bankGroupLayout != null) {
                        // Find the index of the "more decisions" text
                        val clickedIndex = bankGroupLayout.indexOfChild(moreDecisionsText)
                        
                        // Remove the "+ X more decisions" text
                        bankGroupLayout.removeView(moreDecisionsText)
                        
                        // Add all remaining decisions at the same position
                        bank.decisions.drop(3).forEachIndexed { index, decision ->
                            bankGroupLayout.addView(createMiniDecisionCard(decision), clickedIndex + index)
                        }
                    }
                }
                
                addView(moreDecisionsText)
            }
        }
    }
    
    private fun displayByDate() {
        contentLayout.addView(TextView(this).apply {
            text = "Recent Decisions"
            textSize = 18f
            setTextColor(0xFF212121.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dpToPx(4), 0, 0, dpToPx(16))
        })
        
        allDecisions.sortedByDescending { it.timestamp }.forEach { decision ->
            contentLayout.addView(createMiniDecisionCard(decision))
            contentLayout.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(12))
            })
        }
    }
    
    private fun createMiniDecisionCard(decision: FirebaseDecision): LinearLayout {
        val statusIcon = when {
            decision.outcome.equals("APPROVED", ignoreCase = true) -> "✅"
            decision.outcome.equals("DENIED", ignoreCase = true) -> "❌"
            decision.outcome.equals("PENDING", ignoreCase = true) -> "⏳"
            else -> "📋"
        }
        
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            setBackgroundColor(0xFFF5F7FA.toInt())
            
            setOnClickListener {
                // Navigate to AI Explainability Hub (unified explanation page)
                val intent = Intent(this@DashboardActivity, AIExplainabilityHubActivity::class.java)
                startActivity(intent)
            }
            
            addView(TextView(this@DashboardActivity).apply {
                text = statusIcon
                textSize = 20f
                setPadding(0, 0, dpToPx(12), 0)
            })
            
            addView(LinearLayout(this@DashboardActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                
                addView(TextView(this@DashboardActivity).apply {
                    val loanText = if (decision.loanType.isNotEmpty()) {
                        formatLoanType(decision.loanType)
                    } else {
                        "Loan"
                    }
                    text = "$loanText - ${decision.bankName}"
                    textSize = 14f
                    setTextColor(0xFF212121.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })
                
                if (decision.loanAmount > 0) {
                    addView(TextView(this@DashboardActivity).apply {
                        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                        text = formatter.format(decision.loanAmount)
                        textSize = 12f
                        setTextColor(0xFF666666.toInt())
                    })
                }
                
                addView(TextView(this@DashboardActivity).apply {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    text = sdf.format(Date(decision.timestamp))
                    textSize = 11f
                    setTextColor(0xFF999999.toInt())
                })
            })
            
            if (decision.biasDetected) {
                addView(TextView(this@DashboardActivity).apply {
                    text = "⚠️"
                    textSize = 18f
                    setPadding(dpToPx(8), 0, 0, 0)
                })
            }
        }
    }
    
    private fun showEmptyState(message: String) {
        contentLayout.removeAllViews()
        
        contentLayout.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(40), dpToPx(60), dpToPx(40), dpToPx(60))
            
            addView(TextView(this@DashboardActivity).apply {
                text = "📊"
                textSize = 64f
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@DashboardActivity).apply {
                text = message
                textSize = 16f
                setTextColor(0xFF666666.toInt())
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(20), 0, 0)
            })
        })
    }
    
    private fun formatLoanType(loanType: String): String {
        return when (loanType.uppercase()) {
            "PERSONAL_LOAN", "PERSONAL" -> "Personal Loan"
            "HOME_LOAN", "HOME" -> "Home Loan"
            "CAR_LOAN", "CAR" -> "Car Loan"
            "EDUCATION_LOAN", "EDUCATION" -> "Education Loan"
            "BUSINESS_LOAN", "BUSINESS" -> "Business Loan"
            "CREDIT_CARD" -> "Credit Card"
            else -> loanType.replace("_", " ")
        }
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

