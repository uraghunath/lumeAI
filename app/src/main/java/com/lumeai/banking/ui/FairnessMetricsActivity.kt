package com.lumeai.banking.ui

import android.content.Intent
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
import com.lumeai.banking.DecisionManager
import com.lumeai.banking.models.FirebaseDecision
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ⭐ FairnessMetricsActivity - Personal Bias Analysis
 * Shows bias stats from YOUR real decisions
 */
class FairnessMetricsActivity : AppCompatActivity() {
    
    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingIndicator: ProgressBar
    private var allDecisions: List<FirebaseDecision> = emptyList()
    private var biasedDecisions: List<FirebaseDecision> = emptyList()
    private var currentLanguage = "en"
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        
        window.statusBarColor = AppTheme.Background.Secondary  // Same as all other pages
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        setContentView(createUI())
        loadFairnessData()
    }
    
    override fun onResume() {
        super.onResume()
        loadFairnessData()
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
        
        // Info Banner
        rootLayout.addView(createInfoBanner())
        
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
        
        // Content Layout
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(12), dpToPx(20), dpToPx(20))  // Reduced top padding
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
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))  // Compact like other pages
            
            // Header row with back button and title inline
            addView(LinearLayout(this@FairnessMetricsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                // Modern back button - compact
                addView(TextView(this@FairnessMetricsActivity).apply {
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
                
                // Title - compact, no subtitle
                addView(TextView(this@FairnessMetricsActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "निष्पक्षता विश्लेषण"
                        "te" -> "న్యాయ విశ్లేషణ"
                        else -> "Fairness Analysis"
                    }
                    textSize = 18f  // Same as other pages
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
            })
        }
    }
    
    private fun createInfoBanner(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            gravity = Gravity.CENTER_VERTICAL
            
            // Modern card with blue border (same as other pages)
            val cardShape = GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.WHITE)
                setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)
            }
            background = cardShape
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(dpToPx(20), dpToPx(8), dpToPx(20), dpToPx(8))
            layoutParams = params
            
            elevation = dpToPx(1).toFloat()
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = "⚖️"
                textSize = 20f
                setPadding(0, 0, dpToPx(10), 0)
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "info_banner")
                textSize = 12f
                setTextColor(AppTheme.Text.OnCard)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setLineSpacing(dpToPx(2).toFloat(), 1.2f)
            })
        }
    }
    
    private fun loadFairnessData() {
        android.util.Log.d("FairnessMetrics", "🔄 Loading fairness data...")
        loadingIndicator.visibility = View.VISIBLE
        contentLayout.removeAllViews()
        
        lifecycleScope.launch {
            try {
                allDecisions = DecisionManager.getAllDecisions(this@FairnessMetricsActivity, forceRefresh = true)
                android.util.Log.d("FairnessMetrics", "📊 Loaded ${allDecisions.size} total decisions")
                
                biasedDecisions = DecisionManager.getBiasedDecisions(this@FairnessMetricsActivity, forceRefresh = true)
                android.util.Log.d("FairnessMetrics", "⚠️ Found ${biasedDecisions.size} biased decisions")
                
                runOnUiThread {
                    android.util.Log.d("FairnessMetrics", "🎨 Displaying fairness analysis...")
                    loadingIndicator.visibility = View.GONE
                    displayFairnessAnalysis()
                }
            } catch (e: Exception) {
                android.util.Log.e("FairnessMetrics", "❌ Failed to load: ${e.message}", e)
                e.printStackTrace()
                runOnUiThread {
                    loadingIndicator.visibility = View.GONE
                    showEmptyState("Failed to load fairness data")
                }
            }
        }
    }
    
    private fun displayFairnessAnalysis() {
        contentLayout.removeAllViews()
        
        if (allDecisions.isEmpty()) {
            showEmptyState(getString(currentLanguage, "no_decisions"))
            return
        }
        
        // Fairness Score Card
        contentLayout.addView(createFairnessScoreCard())
        contentLayout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(20))
        })
        
        // Bias Detection Summary
        contentLayout.addView(createBiasSummaryCard())
        contentLayout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(20))
        })
        
        // Biased Decisions Section
        if (biasedDecisions.isNotEmpty()) {
            contentLayout.addView(createBiasedDecisionsSection())
        } else {
            contentLayout.addView(createNoBiasCard())
        }
    }
    
    private fun createFairnessScoreCard(): LinearLayout {
        val fairnessScore = if (allDecisions.isNotEmpty()) {
            ((allDecisions.size - biasedDecisions.size).toFloat() / allDecisions.size * 100).toInt()
        } else 100
        
        val (scoreColor, scoreLabel) = when {
            fairnessScore >= 90 -> Color.parseColor("#10B981") to "EXCELLENT"
            fairnessScore >= 70 -> Color.parseColor("#F59E0B") to "GOOD"
            fairnessScore >= 50 -> Color.parseColor("#EF4444") to "NEEDS ATTENTION"
            else -> Color.parseColor("#991B1B") to "CRITICAL"
        }
        
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
            elevation = dpToPx(4).toFloat()
            gravity = Gravity.CENTER
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(16).toFloat()
            }
            background = shape
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "fairness_score")
                textSize = 18f
                setTextColor(Color.parseColor("#666666"))
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = "$fairnessScore"
                textSize = 64f
                setTextColor(scoreColor)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(12), 0, dpToPx(8))
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = scoreLabel
                textSize = 16f
                setTextColor(scoreColor)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "out_of_100")
                textSize = 12f
                setTextColor(Color.parseColor("#999999"))
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(4), 0, 0)
            })
        }
    }
    
    private fun createBiasSummaryCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(4).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
            }
            background = shape
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "summary")
                textSize = 20f
                setTextColor(Color.parseColor("#212121"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(16))
            })
            
            // Stats Grid
            addView(LinearLayout(this@FairnessMetricsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 3f
                
                addView(createStatBox("${allDecisions.size}", getString(currentLanguage, "total_decisions"), Color.parseColor("#2196F3"), 1f))
                addView(View(this@FairnessMetricsActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(12), 0)
                })
                addView(createStatBox("${biasedDecisions.size}", getString(currentLanguage, "bias_detected"), Color.parseColor("#EF4444"), 1f))
                addView(View(this@FairnessMetricsActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(12), 0)
                })
                addView(createStatBox("${biasedDecisions.count { it.biasSeverity == "HIGH" }}", getString(currentLanguage, "high_risk"), Color.parseColor("#991B1B"), 1f))
            })
            
            // Bias breakdown
            if (biasedDecisions.isNotEmpty()) {
                addView(View(this@FairnessMetricsActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                        setMargins(0, dpToPx(16), 0, dpToPx(16))
                    }
                    setBackgroundColor(Color.parseColor("#E5E7EB"))
                })
                
                addView(TextView(this@FairnessMetricsActivity).apply {
                    text = getString(currentLanguage, "risk_breakdown")
                    textSize = 14f
                    setTextColor(Color.parseColor("#666666"))
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 0, 0, dpToPx(8))
                })
                
                val highCount = biasedDecisions.count { it.biasSeverity == "HIGH" }
                val mediumCount = biasedDecisions.count { it.biasSeverity == "MEDIUM" }
                val lowCount = biasedDecisions.count { it.biasSeverity == "LOW" }
                
                if (highCount > 0) {
                    addView(createRiskLabel("🔴 HIGH RISK", "$highCount decision${if (highCount != 1) "s" else ""}", Color.parseColor("#DC2626")))
                }
                if (mediumCount > 0) {
                    addView(createRiskLabel("🟡 MEDIUM RISK", "$mediumCount decision${if (mediumCount != 1) "s" else ""}", Color.parseColor("#F59E0B")))
                }
                if (lowCount > 0) {
                    addView(createRiskLabel("🟢 LOW RISK", "$lowCount decision${if (lowCount != 1) "s" else ""}", Color.parseColor("#10B981")))
                }
            }
        }
    }
    
    private fun createStatBox(value: String, label: String, color: Int, weight: Float): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            setPadding(dpToPx(12), dpToPx(16), dpToPx(12), dpToPx(16))
            gravity = Gravity.CENTER
            
            // Clean white card without colored border
            val cardShape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(8).toFloat()
                setStroke(dpToPx(1), AppTheme.Text.Tertiary)  // Subtle gray border
            }
            background = cardShape
            elevation = dpToPx(1).toFloat()
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = value
                textSize = 28f
                setTextColor(color)  // Colored number
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = label
                textSize = 10f
                setTextColor(AppTheme.Text.OnCard)  // Dark blue text
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(4), 0, 0)
            })
        }
    }
    
    private fun createRiskLabel(label: String, count: String, color: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = label
                textSize = 12f
                setTextColor(color)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = count
                textSize = 12f
                setTextColor(color)
            })
        }
    }
    
    private fun createBiasedDecisionsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "biased_decisions")
                textSize = 18f
                setTextColor(Color.parseColor("#212121"))
                setTypeface(null, Typeface.BOLD)
                setPadding(dpToPx(4), 0, 0, dpToPx(16))
            })
            
            biasedDecisions.forEach { decision ->
                addView(createBiasedDecisionCard(decision))
                addView(View(this@FairnessMetricsActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dpToPx(12))
                })
            }
        }
    }
    
    private fun createBiasedDecisionCard(decision: FirebaseDecision): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            
            // Clean white card with rounded corners (like AI Explainability Hub)
            val cardShape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dpToPx(12).toFloat()
            }
            background = cardShape
            elevation = dpToPx(2).toFloat()
            
            setOnClickListener {
                // Navigate to AI Explainability Hub (unified explanation page)
                val intent = Intent(this@FairnessMetricsActivity, AIExplainabilityHubActivity::class.java)
                startActivity(intent)
            }
            
            // Header Row: Bank + Risk Badge
            addView(LinearLayout(this@FairnessMetricsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, dpToPx(8))
                
                // Bank icon + name
                addView(TextView(this@FairnessMetricsActivity).apply {
                    text = "🏦 ${decision.bankName}"
                    textSize = 16f
                    setTextColor(AppTheme.Text.OnCard)  // Dark blue
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
                
                // Risk Badge
                addView(TextView(this@FairnessMetricsActivity).apply {
                    text = "${decision.biasSeverity} RISK"
                    textSize = 11f
                    val color = when (decision.biasSeverity.uppercase()) {
                        "HIGH" -> AppTheme.Status.Error
                        "MEDIUM" -> AppTheme.Status.Warning
                        else -> AppTheme.Status.Success
                    }
                    setTextColor(color)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))
                    
                    // Badge background
                    val badgeShape = GradientDrawable().apply {
                        setColor(AppTheme.withOpacity(color, 0.1f))
                        cornerRadius = dpToPx(8).toFloat()
                    }
                    background = badgeShape
                })
            })
            
            // Loan Type
            if (decision.loanType.isNotEmpty()) {
                addView(TextView(this@FairnessMetricsActivity).apply {
                    text = "💰 ${formatLoanType(decision.loanType)}"
                    textSize = 14f
                    setTextColor(AppTheme.Text.OnCard)
                    setPadding(0, dpToPx(4), 0, dpToPx(8))
                })
            }
            
            // Bias Banner (like in AI Explainability Hub)
            addView(LinearLayout(this@FairnessMetricsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
                gravity = Gravity.CENTER_VERTICAL
                
                val biasShape = GradientDrawable().apply {
                    setColor(AppTheme.StatusBg.Warning)
                    cornerRadius = dpToPx(8).toFloat()
                    setStroke(dpToPx(1), AppTheme.Status.Warning)
                }
                background = biasShape
                
                addView(TextView(this@FairnessMetricsActivity).apply {
                    text = "⚖️"
                    textSize = 18f
                    setPadding(0, 0, dpToPx(8), 0)
                })
                
                addView(LinearLayout(this@FairnessMetricsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    
                    addView(TextView(this@FairnessMetricsActivity).apply {
                        text = "⚖️ Bias Detected"
                        textSize = 13f
                        setTextColor(AppTheme.Status.Warning)
                        setTypeface(null, Typeface.BOLD)
                    })
                    
                    addView(TextView(this@FairnessMetricsActivity).apply {
                        text = "${decision.biasSeverity} risk - Flagged for review"
                        textSize = 11f
                        setTextColor(AppTheme.Text.OnCard)
                        setPadding(0, dpToPx(2), 0, 0)
                    })
                })
            })
            
            // Bias Message
            if (decision.biasMessage.isNotEmpty()) {
                addView(TextView(this@FairnessMetricsActivity).apply {
                    text = decision.biasMessage
                    textSize = 13f
                    setTextColor(AppTheme.Text.OnCard)
                    setPadding(0, dpToPx(12), 0, 0)
                    setLineSpacing(dpToPx(2).toFloat(), 1.4f)
                })
            }
            
            // Timestamp
            addView(TextView(this@FairnessMetricsActivity).apply {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                text = sdf.format(Date(decision.timestamp))
                textSize = 11f
                setTextColor(AppTheme.Text.Secondary)
                setPadding(0, dpToPx(12), 0, dpToPx(8))
            })
            
            // CTA Button
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "tap_to_view")
                textSize = 13f
                setTextColor(AppTheme.Text.OnCardSecondary)  // Blue text
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(8), 0, 0)
                setTypeface(null, Typeface.BOLD)
            })
        }
    }
    
    private fun createNoBiasCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F0FDF4"))
            setPadding(dpToPx(24), dpToPx(32), dpToPx(24), dpToPx(32))
            gravity = Gravity.CENTER
            elevation = dpToPx(4).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#F0FDF4"))
                cornerRadius = dpToPx(12).toFloat()
            }
            background = shape
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = "✅"
                textSize = 48f
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "no_bias_title")
                textSize = 20f
                setTextColor(Color.parseColor("#10B981"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(16), 0, dpToPx(8))
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = getString(currentLanguage, "no_bias_desc")
                textSize = 14f
                setTextColor(Color.parseColor("#166534"))
                gravity = Gravity.CENTER
                setPadding(dpToPx(20), 0, dpToPx(20), 0)
                setLineSpacing(0f, 1.5f)
            })
        }
    }
    
    private fun showEmptyState(message: String) {
        contentLayout.removeAllViews()
        
        contentLayout.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(40), dpToPx(60), dpToPx(40), dpToPx(60))
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = "⚖️"
                textSize = 64f
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@FairnessMetricsActivity).apply {
                text = message
                textSize = 16f
                setTextColor(Color.parseColor("#666666"))
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(20), 0, 0)
                setLineSpacing(0f, 1.5f)
            })
        })
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
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(20), dpToPx(15), dpToPx(20), dpToPx(10))
            gravity = Gravity.END
            setBackgroundColor(Color.WHITE)
            
            val languages = listOf("English" to "en", "हिंदी" to "hi", "తెలుగు" to "te")
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(android.widget.Space(this@FairnessMetricsActivity).apply {
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
            val shape = GradientDrawable().apply {
                cornerRadius = dpToPx(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)  // Same as other pages
                } else {
                    setColor(Color.WHITE)
                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)  // Same border
                }
            }
            background = shape
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    // Save language preference
                    LanguageHelper.setLanguage(this@FairnessMetricsActivity, code)
                    recreate()
                }
            }
        }
    }
    
    private fun getString(lang: String, key: String): String {
        val strings = mapOf(
            "title" to mapOf(
                "en" to "Fairness Analysis",
                "hi" to "निष्पक्षता विश्लेषण",
                "te" to "న్యాయ విశ్లేషణ"
            ),
            "subtitle" to mapOf(
                "en" to "Personal bias detection & fairness score",
                "hi" to "व्यक्तिगत पूर्वाग्रह पहचान और निष्पक्षता स्कोर",
                "te" to "వ్యక్తిగత పక్షపాత గుర్తింపు & న్యాయ స్కోర్"
            ),
            "info_banner" to mapOf(
                "en" to "This shows bias analysis for YOUR decisions only. We detect potential discrimination based on age, location, and digital literacy.",
                "hi" to "यह केवल आपके निर्णयों के लिए पूर्वाग्रह विश्लेषण दिखाता है। हम उम्र, स्थान और डिजिटल साक्षरता के आधार पर संभावित भेदभाव का पता लगाते हैं।",
                "te" to "ఇది మీ నిర్ణయాలకు మాత్రమే పక్షపాత విశ్లేషణ చూపిస్తుంది. మేము వయస్సు, ప్రదేశం మరియు డిజిటల్ అక్షరాస్యత ఆధారంగా సంభావ్య వివక్షను గుర్తిస్తాము."
            ),
            "fairness_score" to mapOf(
                "en" to "Your Fairness Score",
                "hi" to "आपका निष्पक्षता स्कोर",
                "te" to "మీ న్యాయ స్కోర్"
            ),
            "out_of_100" to mapOf(
                "en" to "out of 100",
                "hi" to "100 में से",
                "te" to "100లో"
            ),
            "summary" to mapOf(
                "en" to "📊 Summary",
                "hi" to "📊 सारांश",
                "te" to "📊 సారాంశం"
            ),
            "total_decisions" to mapOf(
                "en" to "Total\nDecisions",
                "hi" to "कुल\nनिर्णय",
                "te" to "మొత్తం\nనిర్ణయాలు"
            ),
            "bias_detected" to mapOf(
                "en" to "Bias\nDetected",
                "hi" to "पूर्वाग्रह\nपाया गया",
                "te" to "పక్షపాతం\nగుర్తించబడింది"
            ),
            "high_risk" to mapOf(
                "en" to "High\nRisk",
                "hi" to "उच्च\nजोखिम",
                "te" to "అధిక\nప్రమాదం"
            ),
            "risk_breakdown" to mapOf(
                "en" to "Risk Breakdown:",
                "hi" to "जोखिम विवरण:",
                "te" to "ప్రమాద వివరణ:"
            ),
            "biased_decisions" to mapOf(
                "en" to "⚠️ Decisions with Bias Detected",
                "hi" to "⚠️ पूर्वाग्रह पाए गए निर्णय",
                "te" to "⚠️ పక్షపాతంతో గుర్తించిన నిర్ణయాలు"
            ),
            "no_bias_title" to mapOf(
                "en" to "No Bias Detected!",
                "hi" to "कोई पूर्वाग्रह नहीं पाया गया!",
                "te" to "పక్షపాతం కనుగొనబడలేదు!"
            ),
            "no_bias_desc" to mapOf(
                "en" to "All your decisions were made fairly without discrimination based on age, location, or digital literacy.",
                "hi" to "आपके सभी निर्णय उम्र, स्थान या डिजिटल साक्षरता के आधार पर बिना किसी भेदभाव के निष्पक्ष रूप से किए गए थे।",
                "te" to "మీ అన్ని నిర్ణయాలు వయస్సు, ప్రదేశం లేదా డిజిటల్ అక్షరాస్యత ఆధారంగా వివక్ష లేకుండా న్యాయంగా తీసుకోబడ్డాయి."
            ),
            "tap_to_view" to mapOf(
                "en" to "👆 Tap to view full analysis",
                "hi" to "👆 पूर्ण विश्लेषण देखने के लिए टैप करें",
                "te" to "👆 పూర్తి విశ్లేషణ చూడటానికి నొక్కండి"
            ),
            "no_decisions" to mapOf(
                "en" to "No decisions to analyze yet.\nFairness analysis will appear when you have banking decisions.",
                "hi" to "अभी तक विश्लेषण के लिए कोई निर्णय नहीं।\nबैंकिंग निर्णय होने पर निष्पक्षता विश्लेषण दिखाई देगा।",
                "te" to "ఇంకా విశ్లేషించడానికి నిర్ణయాలు లేవు.\nమీకు బ్యాంకింగ్ నిర్ణయాలు ఉన్నప్పుడు న్యాయ విశ్లేషణ కనిపిస్తుంది."
            )
        )
        return strings[key]?.get(lang) ?: strings[key]?.get("en") ?: key
    }
}

