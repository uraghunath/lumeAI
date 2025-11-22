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
import com.lumeai.banking.*
import com.lumeai.banking.models.FirebaseDecision
import com.lumeai.banking.models.BankDecision
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

/**
 * PathToApprovalActivity - Shows AI-generated "What If" scenarios
 * Uses REAL GENERATIVE AI (GPT-4o-mini) to generate paths to approval
 * Now works with REAL Firebase decisions!
 */
class PathToApprovalActivity : AppCompatActivity() {
    
    private var currentLanguage = "en"
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var resultsContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var decisionSelectionContainer: LinearLayout
    
    private var deniedDecisions: List<FirebaseDecision> = emptyList()
    private var selectedDecision: FirebaseDecision? = null
    private var realtimeListenerJob: kotlinx.coroutines.Job? = null
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = languagePrefs.getString("language", "en") ?: "en"
        
        window.statusBarColor = AppTheme.Background.Secondary  // Same as AI Explainability Hub
        supportActionBar?.hide()
        
        setContentView(createUI())
        
        // Check if a specific decision ID was passed from AI Explainability Hub
        val targetDecisionId = intent.getStringExtra("DECISION_ID")
        val autoExpandFirst = intent.getBooleanExtra("AUTO_EXPAND_FIRST", false)
        
        if (targetDecisionId != null) {
            android.util.Log.d("PathToApprovalActivity", "🎯 Direct navigation to decision: $targetDecisionId")
            loadSpecificDecision(targetDecisionId, autoExpandFirst)
        } else {
            // Load all denied decisions for selection
            loadDeniedDecisions()
        }
        
        // Start real-time listener for new decisions
        startRealtimeDecisionListener()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        realtimeListenerJob?.cancel()
        android.util.Log.d("PathToApprovalActivity", "🛑 Real-time listener stopped")
    }
    
    private fun createUI(): FrameLayout {
        // Main container with sticky header
        val mainContainer = FrameLayout(this@PathToApprovalActivity)
        mainContainer.setBackgroundColor(AppTheme.Background.Primary)
        
        // Scrollable content
        scrollView = ScrollView(this@PathToApprovalActivity)
        scrollView.setBackgroundColor(AppTheme.Background.Primary)
        
        val rootLayout = LinearLayout(this@PathToApprovalActivity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
            // Compact top padding (same as AI Explainability Hub)
            setPadding(0, dp(120), 0, 0)
        }
        
        // Info card - same style as AI Explainability Hub
        rootLayout.addView(createInfoBanner())
        
        // Decision selection container (for multiple denied decisions)
        decisionSelectionContainer = LinearLayout(this@PathToApprovalActivity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            visibility = View.GONE
        }
        rootLayout.addView(decisionSelectionContainer)
        
        // Loading spinner
        loadingSpinner = ProgressBar(this).apply {
            visibility = View.VISIBLE
            layoutParams = LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            ).apply {
                gravity = Gravity.CENTER
                setMargins(0, dp(32), 0, dp(32))
            }
        }
        rootLayout.addView(loadingSpinner)
        
        // Results container
        resultsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, dp(16), dp(16))
            visibility = View.GONE
        }
        rootLayout.addView(resultsContainer)
        
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
            
            // Header row with back button and title inline
            addView(LinearLayout(this@PathToApprovalActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                // Modern back button - icon only
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = "←"
                    textSize = 24f  // Smaller
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(dp(4), dp(4), dp(4), dp(4))
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))  // Smaller
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    
                    // Add ripple effect on touch
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    
                    setOnClickListener { finish() }
                })
                
                // Title - compact and clean
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "अनुमोदन का मार्ग"
                        "te" -> "ఆమోద మార్గం"
                        else -> "Path to Approval"
                    }
                    textSize = 18f  // Same as AI Explainability Hub
                    setTextColor(0xFFFFFFFF.toInt())
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
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(20), dp(15), dp(20), dp(10))
            gravity = Gravity.END
            setBackgroundColor(Color.WHITE)
            
            val languages = listOf(
                "English" to "en",
                "हिंदी" to "hi",
                "తెలుగు" to "te"
            )
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(Space(this@PathToApprovalActivity).apply {
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
            val shape = GradientDrawable().apply {
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
    
    private fun createHeroSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(16), dp(16), dp(16), dp(8))
            }
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.parseColor("#E3F2FD"))
            }
            background = shape
            
            // Header row with title and regenerate button
            addView(LinearLayout(this@PathToApprovalActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "AI-जनित परिदृश्य"
                        "te" -> "AI-ఉత్పన్న దృశ్యాలు"
                        else -> "AI-Generated Scenarios"
                    }
                    textSize = 18f
                    setTextColor(AppTheme.Primary.Blue)
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
                
                // Regenerate button
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = "🔄 Regenerate"
                    textSize = 13f
                    setTextColor(AppTheme.Primary.Blue)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(dp(12), dp(8), dp(12), dp(8))
                    
                    val btnShape = GradientDrawable().apply {
                        cornerRadius = dp(8).toFloat()
                        setColor(Color.WHITE)
                        setStroke(dp(2), AppTheme.Primary.Blue)
                    }
                    background = btnShape
                    
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        regenerateCounterfactuals()
                    }
                })
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "AI आपके लिए विभिन्न परिदृश्यों का विश्लेषण कर रहा है। प्रत्येक परिदृश्य दिखाता है कि यदि आप विशिष्ट कारकों में सुधार करते हैं तो क्या होगा।"
                    "te" -> "AI మీ కోసం వివిధ దృశ్యాలను విశ్లేషిస్తోంది। ప్రతి దృశ్యం మీరు నిర్దిష్ట కారకాలను మెరుగుపరచినట్లయితే ఏమి జరుగుతుందో చూపిస్తుంది।"
                    else -> "AI is analyzing different paths to approval for you. Each scenario shows what would happen if you improve specific factors."
                }
                textSize = 14f
                setTextColor(AppTheme.Primary.Blue)
                setLineSpacing(0f, 1.4f)
                setPadding(0, dp(12), 0, 0)
            })
            
            // Get CIBIL Report Button
            addView(TextView(this@PathToApprovalActivity).apply {
                text = "📊 Get Your CIBIL Report →"
                textSize = 15f
                setTextColor(Color.parseColor("#FFFFFF"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(14), dp(16), dp(14))
                
                val btnShape = GradientDrawable().apply {
                    cornerRadius = dp(10).toFloat()
                    setColor(AppTheme.Primary.Blue)
                }
                background = btnShape
                
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.topMargin = dp(16)
                layoutParams = params
                
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    openCIBILWebsite()
                }
            })
        }
    }
    
    /**
     * Open CIBIL website to get credit report
     */
    private fun openCIBILWebsite() {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse("https://www.cibil.com/freecibilscore")
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                "Unable to open browser. Please visit www.cibil.com",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Regenerate counterfactual scenarios with AI
     */
    private fun regenerateCounterfactuals() {
        if (selectedDecision == null) {
            Toast.makeText(this, "No decision selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔄 Regenerate AI Scenarios?")
            .setMessage("This will generate new improvement paths using AI.\n\nNote: This may take up to 2 minutes.")
            .setPositiveButton("Regenerate") { dialog, _ ->
                dialog.dismiss()
                
                Toast.makeText(
                    this,
                    "🤖 Generating new AI scenarios...",
                    Toast.LENGTH_LONG
                ).show()
                
                // Reload counterfactuals
                loadCounterfactualsForDecision(selectedDecision!!)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Load a specific decision by ID (direct navigation from AI Explainability Hub)
     */
    private fun loadSpecificDecision(decisionId: String, autoExpand: Boolean) {
        android.util.Log.d("PathToApprovalActivity", "🎯 Loading specific decision: $decisionId")
        lifecycleScope.launch {
            try {
                // Load all denied decisions to find the specific one
                deniedDecisions = DecisionManager.getDeniedDecisions(this@PathToApprovalActivity, forceRefresh = true)
                val targetDecision = deniedDecisions.find { it.id == decisionId }
                
                runOnUiThread {
                    if (targetDecision != null) {
                        android.util.Log.d("PathToApprovalActivity", "✅ Found target decision: ${targetDecision.bankName}")
                        selectedDecision = targetDecision
                        loadCounterfactualsForDecision(targetDecision, autoExpand)
                    } else {
                        android.util.Log.w("PathToApprovalActivity", "⚠️ Decision $decisionId not found in denied decisions")
                        // Fall back to showing all denied decisions
                        loadingSpinner.visibility = View.GONE
                        if (deniedDecisions.isEmpty()) {
                            showNoDeniedDecisionsState()
                        } else {
                            showDecisionSelection()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PathToApprovalActivity", "❌ Failed to load specific decision: ${e.message}", e)
                runOnUiThread {
                    loadingSpinner.visibility = View.GONE
                    Toast.makeText(
                        this@PathToApprovalActivity,
                        "Error loading decision: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Load real denied decisions from Firebase
     */
    private fun loadDeniedDecisions() {
        android.util.Log.d("PathToApprovalActivity", "🔄 Loading denied decisions...")
        lifecycleScope.launch {
            try {
                deniedDecisions = DecisionManager.getDeniedDecisions(this@PathToApprovalActivity, forceRefresh = true)
                android.util.Log.d("PathToApprovalActivity", "❌ Found ${deniedDecisions.size} denied decisions")
                
                runOnUiThread {
                    when {
                        deniedDecisions.isEmpty() -> {
                            // No denied decisions - show empty state
                            android.util.Log.d("PathToApprovalActivity", "✅ No denied decisions - showing success state")
                            loadingSpinner.visibility = View.GONE
                            showNoDeniedDecisionsState()
                        }
                        deniedDecisions.size == 1 -> {
                            // Only one denial - analyze it directly
                            android.util.Log.d("PathToApprovalActivity", "📋 One denial - analyzing: ${deniedDecisions[0].bankName}")
                            selectedDecision = deniedDecisions[0]
                            loadCounterfactualsForDecision(selectedDecision!!)
                        }
                        else -> {
                            // Multiple denials - show selection
                            android.util.Log.d("PathToApprovalActivity", "📋 Multiple denials - showing selection")
                            loadingSpinner.visibility = View.GONE
                            showDecisionSelection()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PathToApprovalActivity", "❌ Failed to load: ${e.message}", e)
                e.printStackTrace()
                runOnUiThread {
                    loadingSpinner.visibility = View.GONE
                    Toast.makeText(
                        this@PathToApprovalActivity,
                        "Error loading decisions: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Show decision selection if multiple denials exist
     */
    private fun showDecisionSelection() {
        decisionSelectionContainer.removeAllViews()
        decisionSelectionContainer.visibility = View.VISIBLE
        
        decisionSelectionContainer.addView(TextView(this).apply {
            text = "Select a denied application to improve:"
            textSize = 16f
            setTextColor(Color.parseColor("#212121"))
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(4), 0, 0, dp(16))
        })
        
        deniedDecisions.forEach { decision ->
            decisionSelectionContainer.addView(createDecisionSelectionCard(decision))
            addSpace(decisionSelectionContainer, 12)
        }
    }
    
    /**
     * Create a selectable decision card
     */
    private fun createDecisionSelectionCard(decision: FirebaseDecision): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            elevation = dp(3).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = shape
            
            setOnClickListener {
                selectedDecision = decision
                decisionSelectionContainer.visibility = View.GONE
                loadingSpinner.visibility = View.VISIBLE
                loadCounterfactualsForDecision(decision)
            }
            
            addView(LinearLayout(this@PathToApprovalActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = "🏦 ${decision.bankName}"
                    textSize = 16f
                    setTextColor(Color.parseColor("#1976D2"))
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = "❌ DENIED"
                    textSize = 12f
                    setTextColor(Color.parseColor("#EF4444"))
                    setTypeface(null, Typeface.BOLD)
                })
            })
            
            if (decision.loanType.isNotEmpty()) {
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = formatLoanType(decision.loanType)
                    textSize = 13f
                    setTextColor(Color.parseColor("#666666"))
                    setPadding(0, dp(4), 0, 0)
                })
            }
            
            addView(TextView(this@PathToApprovalActivity).apply {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                text = sdf.format(Date(decision.timestamp))
                textSize = 11f
                setTextColor(Color.parseColor("#999999"))
                setPadding(0, dp(8), 0, 0)
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = "👆 Tap to see improvement paths"
                textSize = 12f
                setTextColor(AppTheme.Primary.Blue)
                gravity = Gravity.CENTER
                setPadding(0, dp(12), 0, 0)
            })
        }
    }
    
    /**
     * Show empty state when no denied decisions exist
     */
    private fun showNoDeniedDecisionsState() {
        resultsContainer.visibility = View.VISIBLE
        resultsContainer.removeAllViews()
        
        resultsContainer.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(40), dp(60), dp(40), dp(60))
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = "✅"
                textSize = 64f
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = "No Denied Applications!"
                textSize = 20f
                setTextColor(Color.parseColor("#212121"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, dp(20), 0, dp(12))
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = "Great news! You don't have any rejections to improve.\n\nThis screen will show AI-powered improvement paths when you have a denied application."
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                gravity = Gravity.CENTER
                setPadding(dp(20), 0, dp(20), 0)
                setLineSpacing(0f, 1.5f)
            })
        })
    }
    
    /**
     * Load counterfactuals for specific decision
     */
    private fun loadCounterfactualsForDecision(firebaseDecision: FirebaseDecision, autoExpandFirst: Boolean = false) {
        lifecycleScope.launch {
            try {
                // Convert Firebase decision to BankDecision format
                val bankDecision = firebaseDecision.toBankDecision()
                
                // Call REAL AI to generate counterfactuals
                val analysis = withContext(Dispatchers.IO) {
                    CounterfactualEngine.generateCounterfactuals(bankDecision)
                }
                
                runOnUiThread {
                    loadingSpinner.visibility = View.GONE
                    resultsContainer.visibility = View.VISIBLE
                    displayCounterfactuals(analysis, autoExpandFirst)
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    loadingSpinner.visibility = View.GONE
                    
                    Toast.makeText(
                        this@PathToApprovalActivity,
                        "Error generating scenarios: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
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
    
    private fun displayCounterfactuals(analysis: com.lumeai.banking.CounterfactualAnalysis, autoExpandFirst: Boolean = false) {
        resultsContainer.removeAllViews()
        
        // AI Badge
        if (analysis.isAIGenerated) {
            resultsContainer.addView(createAIBadge())
            addSpace(resultsContainer, 16)
        }
        
        // Overall message
        resultsContainer.addView(createOverallMessageCard(analysis.overallMessage))
        addSpace(resultsContainer, 16)
        
        // Quick paths summary
        resultsContainer.addView(createQuickPathsCard(analysis))
        addSpace(resultsContainer, 20)
        
        // Scenarios
        resultsContainer.addView(TextView(this).apply {
            text = when (currentLanguage) {
                "hi" -> "📊 विस्तृत परिदृश्य"
                "te" -> "📊 వివరమైన దృశ్యాలు"
                else -> "📊 Detailed Scenarios"
            }
            textSize = 20f
            setTextColor(Color.parseColor("#1F2937"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        })
        
        analysis.scenarios.forEachIndexed { index, scenario ->
            val shouldExpand = autoExpandFirst && index == 0
            resultsContainer.addView(createScenarioCard(scenario, index + 1, shouldExpand))
            addSpace(resultsContainer, 16)
        }
        
        // If auto-expand is enabled, scroll to the first scenario after a short delay
        if (autoExpandFirst && analysis.scenarios.isNotEmpty()) {
            scrollView.postDelayed({
                scrollView.smoothScrollTo(0, resultsContainer.top)
            }, 300)
        }
    }
    
    private fun createAIBadge(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                AppTheme.Gradients.PrimaryHeader
            )
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(AppTheme.Primary.Blue)
            }
            background = shape
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "✨ AI-संचालित विश्लेषण"
                    "te" -> "✨ AI-ఆధారిత విశ్లేషణ"
                    else -> "✨ AI-Powered Analysis"
                }
                textSize = 12f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
            })
        }
    }
    
    private fun createOverallMessageCard(message: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E3F2FD"))
            setPadding(dp(16), dp(16), dp(16), dp(16))
            
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#E3F2FD"))
                cornerRadius = dp(12).toFloat()
                setStroke(dp(2), Color.parseColor("#2196F3"))
            }
            background = shape
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "💡 AI सारांश"
                    "te" -> "💡 AI సారాంశం"
                    else -> "💡 AI Summary"
                }
                textSize = 16f
                setTextColor(Color.parseColor("#1976D2"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(8))
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = message
                textSize = 14f
                setTextColor(Color.parseColor("#424242"))
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    private fun createQuickPathsCard(analysis: com.lumeai.banking.CounterfactualAnalysis): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))  // ENHANCED: More padding
            elevation = dp(6).toFloat()  // ENHANCED: Increased elevation
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()  // ENHANCED: Larger radius
            }
            background = shape
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "🎯 त्वरित पथ"
                    "te" -> "🎯 శీఘ్ర మార్గాలు"
                    else -> "🎯 Quick Paths"
                }
                textSize = 20f  // ENHANCED: Larger title
                setTextColor(Color.parseColor("#0A0A0A"))  // Darker
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))  // More spacing
            })
            
            addView(createPathItem(
                when (currentLanguage) {
                    "hi" -> "⚡ सबसे आसान पथ"
                    "te" -> "⚡ సులభమైన మార్గం"
                    else -> "⚡ Easiest Path"
                },
                analysis.easiestPath, "#10B981"
            ))
            addView(createPathItem(
                when (currentLanguage) {
                    "hi" -> "🚀 सबसे तेज़ पथ"
                    "te" -> "🚀 వేగవంతమైన మార్గం"
                    else -> "🚀 Fastest Path"
                },
                analysis.fastestPath, "#3B82F6"
            ))
            addView(createPathItem(
                when (currentLanguage) {
                    "hi" -> "💪 सबसे प्रभावशाली"
                    "te" -> "💪 అత్యంత ప్రభావం"
                    else -> "💪 Most Impactful"
                },
                analysis.mostImpactful, "#7C3AED"
            ))
        }
    }
    
    private fun createPathItem(label: String, value: String, color: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))  // ENHANCED: More padding
            
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor(color + "20"))
                cornerRadius = dp(12).toFloat()  // ENHANCED: Larger radius
                setStroke(dp(2), Color.parseColor(color))  // ENHANCED: Thicker border
            }
            background = shape
            elevation = dp(1).toFloat()  // ENHANCED: Add subtle shadow
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))  // More spacing between items
            }
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = label
                textSize = 13f  // ENHANCED: Slightly larger
                setTextColor(Color.parseColor(color))
                setTypeface(null, Typeface.BOLD)
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = value
                textSize = 15f  // ENHANCED: Larger value text
                setTextColor(Color.parseColor("#0A0A0A"))  // Darker
                setTypeface(null, Typeface.BOLD)  // ENHANCED: Bolder
                setPadding(0, dp(6), 0, 0)  // More spacing
            })
        }
    }
    
    private fun createScenarioCard(scenario: CounterfactualScenario, index: Int, initiallyExpanded: Boolean = false): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(6).toFloat()  // ENHANCED: Increased elevation
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()  // ENHANCED: Larger radius
            }
            background = shape
            
            // Header with impact
            addView(LinearLayout(this@PathToApprovalActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = "$index. ${scenario.scenarioName}"
                    textSize = 19f  // ENHANCED: Larger
                    setTextColor(Color.parseColor("#0A0A0A"))  // Darker
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
                
                // ENHANCED: Larger impact badge
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = "+${scenario.impactOnApproval}%"
                    textSize = 18f  // Larger
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(dp(14), dp(8), dp(14), dp(8))  // More padding
                    gravity = Gravity.CENTER
                    
                    val badgeShape = GradientDrawable().apply {
                        cornerRadius = dp(24).toFloat()  // More rounded
                        setColor(when {
                            scenario.impactOnApproval >= 50 -> Color.parseColor("#10B981")
                            scenario.impactOnApproval >= 30 -> Color.parseColor("#F59E0B")
                            else -> Color.parseColor("#3B82F6")
                        })
                    }
                    background = badgeShape
                    elevation = dp(2).toFloat()  // Add shadow
                })
            })
            
            addSpace(this, 16)  // ENHANCED: More spacing
            
            // Change description
            addView(createInfoRow(
                when (currentLanguage) {
                    "hi" -> "🔄 परिवर्तन"
                    "te" -> "🔄 మార్పు"
                    else -> "🔄 Change"
                },
                scenario.changeRequired
            ))
            addView(createInfoRow(
                when (currentLanguage) {
                    "hi" -> "📊 से → तक"
                    "te" -> "📊 నుండి → వరకు"
                    else -> "📊 From → To"
                },
                "${scenario.fromValue} → ${scenario.toValue}"
            ))
            addView(createInfoRow(
                when (currentLanguage) {
                    "hi" -> "⚙️ कठिनाई"
                    "te" -> "⚙️ కష్టత"
                    else -> "⚙️ Difficulty"
                },
                scenario.difficulty
            ))
            addView(createInfoRow(
                when (currentLanguage) {
                    "hi" -> "⏱️ समय सीमा"
                    "te" -> "⏱️ కాల వ్యవధి"
                    else -> "⏱️ Timeframe"
                },
                scenario.timeframe
            ))
            addView(createInfoRow(
                when (currentLanguage) {
                    "hi" -> "💰 लागत"
                    "te" -> "💰 ఖర్చు"
                    else -> "💰 Cost"
                },
                scenario.costEstimate
            ))
            addView(createInfoRow(
                when (currentLanguage) {
                    "hi" -> "✨ लाभ"
                    "te" -> "✨ ప్రయోజనం"
                    else -> "✨ Benefit"
                },
                scenario.keyBenefit
            ))
            
            addSpace(this, 12)
            
            // Action steps
            addView(TextView(this@PathToApprovalActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "📋 कार्रवाई के चरण:"
                    "te" -> "📋 చర్య దశలు:"
                    else -> "📋 Action Steps:"
                }
                textSize = 15f
                setTextColor(AppTheme.Primary.Blue)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(8), 0, dp(8))
            })
            
            scenario.actionSteps.forEachIndexed { stepIndex, step ->
                addView(TextView(this@PathToApprovalActivity).apply {
                    text = "${stepIndex + 1}. $step"
                    textSize = 14f
                    setTextColor(Color.parseColor("#374151"))
                    setPadding(dp(8), dp(4), 0, dp(4))
                    setLineSpacing(0f, 1.3f)
                })
            }
        }
    }
    
    private fun createInfoRow(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = label
                textSize = 13f
                setTextColor(Color.parseColor("#6B7280"))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(dp(100), ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = value
                textSize = 13f
                setTextColor(Color.parseColor("#1F2937"))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
        }
    }
    
    private fun addSpace(layout: LinearLayout, dpValue: Int) {
        layout.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(dpValue))
        })
    }
    
    /**
     * Real-time listener for new denied decisions
     */
    private fun startRealtimeDecisionListener() {
        realtimeListenerJob?.cancel()
        realtimeListenerJob = lifecycleScope.launch {
            try {
                FirebaseSyncManager.listenForCustomerDecisions("CUST-2024-001").collect { newDecision ->
                    android.util.Log.d("PathToApprovalActivity", "🔔 New decision received: ${newDecision.id}")
                    
                    // Only refresh if it's a denied decision
                    if (newDecision.outcome == "DENIED") {
                        android.util.Log.d("PathToApprovalActivity", "♻️ Refreshing denied decisions list")
                        withContext(Dispatchers.Main) {
                            // Reload the decision list
                            val targetDecisionId = intent.getStringExtra("DECISION_ID")
                            if (targetDecisionId != null) {
                                loadSpecificDecision(targetDecisionId, false)
                            } else {
                                loadDeniedDecisions()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PathToApprovalActivity", "❌ Error in real-time listener", e)
            }
        }
        android.util.Log.d("PathToApprovalActivity", "🎧 Real-time listener started")
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
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = "💡"
                textSize = 20f
                setPadding(0, 0, dp(10), 0)
            })
            
            addView(TextView(this@PathToApprovalActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "अपने आवेदन को स्वीकृत करने के लिए AI-जनित सुधार सुझाव देखें"
                    "te" -> "మీ అప్లికేషన్‌ను ఆమోదించడానికి AI-ఉత్పన్న మెరుగుదల సూచనలను చూడండి"
                    else -> "See AI-generated improvement suggestions to increase your chances of approval"
                }
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
}

