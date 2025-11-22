package com.lumeai.banking.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lumeai.banking.models.PersonalizedOffer
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🎁 PersonalizedOffersActivity - AI-Powered Product Recommendations
 * Transparent, fair, and consent-driven personalized banking offers
 */
class PersonalizedOffersActivity : AppCompatActivity() {
    
    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var statsCard: LinearLayout
    private lateinit var resultCountText: TextView  // To show result count
    private var allOffers: List<PersonalizedOffer> = emptyList()
    private var currentLanguage: String = "en"
    private var selectedFilter: String = "All"  // Track selected filter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Blue status bar - same as all other pages
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        // Get language preference (check both keys for consistency)
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        
        setContentView(createUI())
        loadPersonalizedOffers()
    }
    
    override fun onResume() {
        super.onResume()
        loadPersonalizedOffers()
    }
    
    private fun createUI(): ScrollView {
        val scrollView = ScrollView(this)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
        }
        
        // Header
        rootLayout.addView(createHeader())
        
        // Language Bar
        rootLayout.addView(createLanguageBar())
        
        // Info banner about AI transparency
        rootLayout.addView(createTransparencyBanner())
        
        // Stats Card
        statsCard = createStatsCard()
        rootLayout.addView(statsCard)
        
        // Filter Section
        rootLayout.addView(createFilterSection())
        
        // Result Count Display
        resultCountText = TextView(this).apply {
            textSize = 13f
            setTextColor(AppTheme.Text.OnCard)
            setPadding(dpToPx(20), dpToPx(12), dpToPx(20), dpToPx(8))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        rootLayout.addView(resultCountText)
        
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
        
        // Content Layout for offers
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(12), dpToPx(20), dpToPx(20))  // Reduced top padding
        }
        rootLayout.addView(contentLayout)
        
        scrollView.addView(rootLayout)
        return scrollView
    }
    
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))  // Compact
            
            // Header row with back button and title inline
            addView(LinearLayout(this@PersonalizedOffersActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                // Modern back button - compact
                addView(TextView(this@PersonalizedOffersActivity).apply {
                    text = "←"
                    textSize = 24f  // Smaller
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                    layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40))  // Smaller
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    
                    setOnClickListener { finish() }
                })
                
                // Title - compact
                addView(TextView(this@PersonalizedOffersActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "व्यक्तिगत ऑफर"
                        "te" -> "వ్యక్తిగత ఆఫర్లు"
                        else -> "Personalized Offers"
                    }
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
            setBackgroundColor(Color.WHITE)
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            gravity = Gravity.END
            elevation = dpToPx(2).toFloat()
            
            addView(createLanguageButton("English", "en"))
            addView(android.widget.Space(this@PersonalizedOffersActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(8), 0)
            })
            addView(createLanguageButton("हिंदी", "hi"))
            addView(android.widget.Space(this@PersonalizedOffersActivity).apply {
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
                    setColor(AppTheme.Text.OnCard)  // Same as other pages
                } else {
                    setColor(Color.WHITE)
                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)
                }
            }
            background = shape
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    // Save to BOTH shared preferences keys for consistency
                    getSharedPreferences("LumeAI", MODE_PRIVATE)
                        .edit()
                        .putString("language", code)
                        .apply()
                    getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
                        .edit()
                        .putString("language", code)
                        .apply()
                    LanguageHelper.setLanguage(this@PersonalizedOffersActivity, code)
                    recreate()  // Recreate to update all UI including header
                }
            }
        }
    }
    
    private fun createTransparencyBanner(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
            gravity = Gravity.CENTER_VERTICAL
            
            // Modern card with blue border (same as other pages)
            val cardShape = android.graphics.drawable.GradientDrawable().apply {
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
            
            addView(TextView(this@PersonalizedOffersActivity).apply {
                text = "✨"
                textSize = 20f
                setPadding(0, 0, dpToPx(10), 0)
            })
            
            addView(TextView(this@PersonalizedOffersActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "आपकी प्रोफाइल के आधार पर, हमारी AI ने आपके लिए विशेष रूप से ये ऑफर चुने हैं। प्रत्येक ऑफर आपकी वित्तीय जरूरतों के अनुरूप है।"
                    "te" -> "మీ ప్రొఫైల్ ఆధారంగా, మా AI మీ కోసం ప్రత్యేకంగా ఈ ఆఫర్లను ఎంచుకుంది. ప్రతి ఆఫర్ మీ ఆర్థిక అవసరాలకు అనుగుణంగా ఉంటుంది."
                    else -> "Based on your profile, our AI has handpicked these offers just for you. Each offer is tailored to match your financial needs and goals."
                }
                textSize = 12f
                setTextColor(AppTheme.Text.OnCard)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setLineSpacing(dpToPx(2).toFloat(), 1.2f)
            })
        }
    }
    
    private fun createStatsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16))
            elevation = dpToPx(2).toFloat()
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dpToPx(2)
            layoutParams = params
            
            addView(TextView(this@PersonalizedOffersActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "📊 ऑफर सारांश"
                    "te" -> "📊 ఆఫర్ సారాంశం"
                    else -> "📊 Offers Summary"
                }
                textSize = 16f
                setTextColor(0xFF0A0A0A.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            // Stats will be populated dynamically
            addView(LinearLayout(this@PersonalizedOffersActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(12)
                }
                
                addView(TextView(this@PersonalizedOffersActivity).apply {
                    text = "Loading..."
                    textSize = 13f
                    setTextColor(0xFF666666.toInt())
                })
            })
        }
    }
    
    private fun createFilterSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(8))
            elevation = dpToPx(2).toFloat()
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dpToPx(2)
            layoutParams = params
            
            // Label
            addView(TextView(this@PersonalizedOffersActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "फ़िल्टर:"
                    "te" -> "ఫిల్టర్:"
                    else -> "Filter:"
                }
                textSize = 12f
                setTextColor(AppTheme.Text.OnCard)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(6))
            })
            
            // Chip container
            addView(createFilterChips())
        }
    }
    
    private fun createFilterChips(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            
            val filters = listOf(
                Triple("All", "सभी", "అన్ని"),
                Triple("Credit Cards", "क्रेडिट कार्ड", "క్రెడిట్ కార్డ్"),
                Triple("Loans", "ऋण", "రుణాలు"),
                Triple("Investments", "निवेश", "పెట్టుబడులు"),
                Triple("Insurance", "बीमा", "బీమా")
            )
            
            filters.forEachIndexed { index, (enText, hiText, teText) ->
                addView(createFilterChip(enText, hiText, teText))
                
                if (index < filters.size - 1) {
                    addView(android.widget.Space(this@PersonalizedOffersActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dpToPx(8), 0)
                    })
                }
            }
        }
    }
    
    private fun createFilterChip(enText: String, hiText: String, teText: String): TextView {
        return TextView(this).apply {
            val displayText = when (currentLanguage) {
                "hi" -> hiText
                "te" -> teText
                else -> enText
            }
            text = displayText  // No count
            textSize = 12f
            setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            
            val isSelected = selectedFilter == enText
            val chipShape = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = dpToPx(20).toFloat()
                if (isSelected) {
                    setColor(AppTheme.Text.OnCard)
                } else {
                    setColor(Color.WHITE)
                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)
                }
            }
            background = chipShape
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            
            setOnClickListener {
                if (selectedFilter != enText) {
                    selectedFilter = enText
                    // Update chip styles without recreating
                    (parent as? LinearLayout)?.let { container ->
                        for (i in 0 until container.childCount) {
                            val child = container.getChildAt(i)
                            if (child is TextView && child != this) {
                                // Unselect other chips
                                val unselectedShape = android.graphics.drawable.GradientDrawable().apply {
                                    cornerRadius = dpToPx(20).toFloat()
                                    setColor(Color.WHITE)
                                    setStroke(dpToPx(1), AppTheme.Text.OnCardSecondary)
                                }
                                child.background = unselectedShape
                                child.setTextColor(AppTheme.Text.OnCard)
                                child.setTypeface(null, android.graphics.Typeface.NORMAL)
                            }
                        }
                    }
                    // Select this chip
                    val selectedShape = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = dpToPx(20).toFloat()
                        setColor(AppTheme.Text.OnCard)
                    }
                    background = selectedShape
                    setTextColor(Color.WHITE)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    
                    // Apply filter
                    applyChipFilter(enText)
                }
            }
        }
    }
    
    private fun applyChipFilter(filterType: String) {
        val filteredOffers = when (filterType) {
            "All" -> allOffers
            "Credit Cards" -> allOffers.filter { it.offerType.contains("CREDIT", ignoreCase = true) }
            "Loans" -> allOffers.filter { it.offerType.contains("LOAN", ignoreCase = true) }
            "Investments" -> allOffers.filter { it.offerType.contains("INVEST", ignoreCase = true) || it.offerType.contains("SAVINGS", ignoreCase = true) }
            "Insurance" -> allOffers.filter { it.offerType.contains("INSURANCE", ignoreCase = true) }
            else -> allOffers
        }
        
        // Update result count display
        updateResultCountText(filterType, filteredOffers.size)
        
        displayOffers(filteredOffers)
    }
    
    private fun updateResultCountText(filterType: String, count: Int) {
        val filterName = when (currentLanguage) {
            "hi" -> when (filterType) {
                "All" -> "सभी ऑफर"
                "Credit Cards" -> "क्रेडिट कार्ड"
                "Loans" -> "ऋण"
                "Investments" -> "निवेश"
                "Insurance" -> "बीमा"
                else -> "ऑफर"
            }
            "te" -> when (filterType) {
                "All" -> "అన్ని ఆఫర్లు"
                "Credit Cards" -> "క్రెడిట్ కార్డ్‌లు"
                "Loans" -> "రుణాలు"
                "Investments" -> "పెట్టుబడులు"
                "Insurance" -> "బీమా"
                else -> "ఆఫర్లు"
            }
            else -> when (filterType) {
                "All" -> "offers"
                "Credit Cards" -> "credit card offers"
                "Loans" -> "loan offers"
                "Investments" -> "investment offers"
                "Insurance" -> "insurance offers"
                else -> "offers"
            }
        }
        
        val resultsText = when (currentLanguage) {
            "hi" -> "📊 $count $filterName मिले"
            "te" -> "📊 $count $filterName దొరికాయి"
            else -> "📊 Showing $count $filterName"
        }
        
        resultCountText.text = resultsText
    }
    
    /**
     * Generate intelligent pre-approved offers based on user criteria
     */
    private fun generateIntelligentOffers(customerId: String): List<PersonalizedOffer> {
        val offers = mutableListOf<PersonalizedOffer>()
        
        // Get user profile data
        val prefs = getSharedPreferences("LumeAIPrefs", MODE_PRIVATE)
        val creditScore = prefs.getInt("user_credit_score", 650) // Default 650
        val monthlyIncome = prefs.getFloat("user_monthly_income", 30000f) // Default ₹30k
        val age = prefs.getInt("user_age", 30) // Default 30
        
        val now = System.currentTimeMillis()
        val thirtyDaysLater = now + (30L * 24 * 60 * 60 * 1000) // 30 days expiry
        
        // 1. Credit Card Offers (if credit score > 680)
        if (creditScore >= 680) {
            offers.add(PersonalizedOffer(
                id = "gen_cc_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "ICICI Bank",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "CREDIT_CARD",
                productName = "ICICI Platinum Credit Card",
                productDescription = "Lifetime free credit card with amazing benefits",
                offerTitle = "Pre-Approved Credit Card",
                offerSubtitle = "Get ₹50,000 credit limit instantly",
                preApproved = true,
                instantApproval = true,
                eligibleAmount = 50000.0,
                cashback = 5000.0,
                rewardPoints = 10000,
                interestRate = 3.49,
                processingFee = 0.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "Based on your credit score of $creditScore, you're pre-approved for this premium credit card with zero processing fee and instant approval!",
                aiReasonHindi = "आपके $creditScore क्रेडिट स्कोर के आधार पर, आप इस प्रीमियम क्रेडिट कार्ड के लिए पूर्व-स्वीकृत हैं!",
                aiReasonTelugu = "మీ $creditScore క్రెడిట్ స్కోర్ ఆధారంగా, మీరు ఈ ప్రీమియం క్రెడిట్ కార్డ్ కోసం ముందస్తుగా ఆమోదించబడ్డారు!",
                personalizationFactors = listOf("credit_score", "payment_history")
            ))
        }
        
        // 2. Car Loan (if income > ₹40k and credit score > 700)
        if (monthlyIncome >= 40000f && creditScore >= 700) {
            offers.add(PersonalizedOffer(
                id = "gen_car_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "HDFC Bank",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "CAR_LOAN",
                productName = "HDFC Car Loan",
                productDescription = "Pre-approved car loan with lowest interest rates",
                offerTitle = "Pre-Approved Car Loan",
                offerSubtitle = "Get up to ₹15 Lakhs for your dream car",
                preApproved = true,
                instantApproval = false,
                eligibleAmount = 1500000.0,
                interestRate = 8.75,
                processingFee = 2500.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "With your monthly income of ₹${monthlyIncome.toInt()} and excellent credit score of $creditScore, you're eligible for this attractive car loan offer!",
                aiReasonHindi = "आपकी मासिक आय ₹${monthlyIncome.toInt()} और बेहतरीन क्रेडिट स्कोर $creditScore के साथ, आप इस आकर्षक कार लोन के लिए पात्र हैं!",
                aiReasonTelugu = "మీ నెలవారీ ఆదాయం ₹${monthlyIncome.toInt()} మరియు అద్భుతమైన క్రెడిట్ స్కోర్ $creditScore తో, మీరు ఈ ఆకర్షణీయమైన కార్ లోన్‌కు అర్హులు!",
                personalizationFactors = listOf("monthly_income", "credit_score")
            ))
        }
        
        // 3. Home Loan (if income > ₹50k, credit score > 750, age < 50)
        if (monthlyIncome >= 50000f && creditScore >= 750 && age < 50) {
            offers.add(PersonalizedOffer(
                id = "gen_home_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "SBI",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "HOME_LOAN",
                productName = "SBI Home Loan",
                productDescription = "Pre-approved home loan with special rates",
                offerTitle = "Pre-Approved Home Loan",
                offerSubtitle = "Get up to ₹50 Lakhs for your dream home",
                preApproved = true,
                instantApproval = false,
                eligibleAmount = 5000000.0,
                interestRate = 8.40,
                processingFee = 5000.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "Congratulations! Your excellent profile (income: ₹${monthlyIncome.toInt()}, credit score: $creditScore) makes you eligible for this special home loan offer!",
                aiReasonHindi = "बधाई हो! आपकी उत्कृष्ट प्रोफ़ाइल (आय: ₹${monthlyIncome.toInt()}, क्रेडिट स्कोर: $creditScore) आपको इस विशेष होम लोन ऑफर के लिए पात्र बनाती है!",
                aiReasonTelugu = "అభినందనలు! మీ అద్భుతమైన ప్రొఫైల్ (ఆదాయం: ₹${monthlyIncome.toInt()}, క్రెడిట్ స్కోర్: $creditScore) మిమ్మల్ని ఈ ప్రత్యేక హోమ్ లోన్ ఆఫర్‌కు అర్హులను చేస్తుంది!",
                personalizationFactors = listOf("monthly_income", "credit_score", "age", "employment_stability")
            ))
        }
        
        // 4. Personal Loan (if credit score > 700)
        if (creditScore >= 700 && monthlyIncome >= 30000f) {
            offers.add(PersonalizedOffer(
                id = "gen_personal_${customerId}_${now}",
                customerId = customerId,
                customerName = "Customer",
                bankName = "Axis Bank",
                timestamp = now,
                expiryTimestamp = thirtyDaysLater,
                offerType = "PERSONAL_LOAN",
                productName = "Axis Personal Loan",
                productDescription = "Instant personal loan with minimal documentation",
                offerTitle = "Pre-Approved Personal Loan",
                offerSubtitle = "Get up to ₹5 Lakhs instantly",
                preApproved = true,
                instantApproval = true,
                eligibleAmount = 500000.0,
                interestRate = 10.99,
                processingFee = 1999.0,
                status = "ACTIVE",
                userHidden = false,
                aiReasonEnglish = "You're pre-approved for instant personal loan based on your creditworthiness and income stability!",
                aiReasonHindi = "आप अपनी साख और आय स्थिरता के आधार पर तत्काल व्यक्तिगत ऋण के लिए पूर्व-स्वीकृत हैं!",
                aiReasonTelugu = "మీ క్రెడిట్ విలువ మరియు ఆదాయ స్థిరత్వం ఆధారంగా మీరు తక్షణ వ్యక్తిగత లోన్‌కు ముందస్తుగా ఆమోదించబడ్డారు!",
                personalizationFactors = listOf("credit_score", "income_stability")
            ))
        }
        
        return offers
    }
    
    private fun loadPersonalizedOffers() {
        loadingIndicator.visibility = View.VISIBLE
        contentLayout.removeAllViews()
        
        lifecycleScope.launch {
            try {
                // Use FirebaseListenerService to get customerId (consistent with rest of app)
                val customerId = com.lumeai.banking.FirebaseListenerService.getCustomerId(this@PersonalizedOffersActivity)
                
                android.util.Log.d("PersonalizedOffers", "🔍 Loading offers for customerId: $customerId")
                
                if (customerId.isEmpty()) {
                    showEmptyState("No customer ID found")
                    return@launch
                }
                
                // Fetch offers from Firebase
                val database = FirebaseDatabase.getInstance()
                val offersRef = database.getReference("personalizedOffers")
                    .orderByChild("customerId").equalTo(customerId)
                
                android.util.Log.d("PersonalizedOffers", "📡 Querying Firebase: personalizedOffers where customerId = $customerId")
                
                offersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val offers = mutableListOf<PersonalizedOffer>()
                        
                        android.util.Log.d("PersonalizedOffers", "📦 Firebase returned ${snapshot.childrenCount} offers")
                        
                        for (child in snapshot.children) {
                            val offer = child.getValue(PersonalizedOffer::class.java)
                            android.util.Log.d("PersonalizedOffers", "📋 Offer: ${offer?.productName} - Status: ${offer?.status}, Expiry: ${offer?.expiryTimestamp}")
                            if (offer != null && offer.status == "ACTIVE" && !offer.userHidden) {
                                // Filter out any premium/subscription offers that shouldn't be here
                                if (offer.offerType !in listOf("PREMIUM", "SUBSCRIPTION", "APP_UPGRADE")) {
                                    // Only show active offers that haven't expired
                                    if (offer.expiryTimestamp > System.currentTimeMillis()) {
                                        offers.add(offer)
                                    }
                                }
                            }
                        }
                        
                        // Generate intelligent pre-approved offers based on user criteria
                        val generatedOffers = generateIntelligentOffers(customerId)
                        offers.addAll(generatedOffers)
                        
                        allOffers = offers.sortedByDescending { it.timestamp }
                        loadingIndicator.visibility = View.GONE
                        
                        if (allOffers.isEmpty()) {
                            showEmptyState(when (currentLanguage) {
                                "hi" -> "🎁 कोई सक्रिय ऑफर नहीं\n\nहम जल्द ही आपके लिए व्यक्तिगत ऑफर तैयार करेंगे!"
                                "te" -> "🎁 చురుకైన ఆఫర్లు లేవు\n\nమేము త్వరలో మీ కోసం వ్యక్తిగత ఆఫర్లను రూపొందిస్తాము!"
                                else -> "🎁 No active offers\n\nWe'll create personalized offers for you soon!"
                            })
                        } else {
                            updateStats()
                            // Set initial result count for "All" filter
                            updateResultCountText("All", allOffers.size)
                            displayOffers(allOffers)
                        }
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        loadingIndicator.visibility = View.GONE
                        showEmptyState("Error: ${error.message}")
                    }
                })
                
            } catch (e: Exception) {
                loadingIndicator.visibility = View.GONE
                showEmptyState("Error loading offers: ${e.message}")
            }
        }
    }
    
    private fun updateStats() {
        val totalOffers = allOffers.size
        val preApprovedCount = allOffers.count { it.preApproved }
        val creditCardCount = allOffers.count { it.offerType == "CREDIT_CARD" }
        val loanCount = allOffers.count { it.offerType.contains("LOAN") }
        
        // Update stats card
        statsCard.removeAllViews()
        statsCard.addView(TextView(this).apply {
            text = when (currentLanguage) {
                "hi" -> "📊 ऑफर सारांश"
                "te" -> "📊 ఆఫర్ సారాంశం"
                else -> "📊 Offers Summary"
            }
            textSize = 16f
            setTextColor(0xFF0A0A0A.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        
        statsCard.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            
            addView(createStatPill(
                totalOffers.toString(),
                when (currentLanguage) {
                    "hi" -> "कुल ऑफर"
                    "te" -> "మొత్తం ఆఫర్లు"
                    else -> "Total Offers"
                },
                1f
            ))
            
            addView(createStatPill(
                preApprovedCount.toString(),
                when (currentLanguage) {
                    "hi" -> "पूर्व-स्वीकृत"
                    "te" -> "ముందస్తు-ఆమోదం"
                    else -> "Pre-Approved"
                },
                1f
            ))
            
            addView(createStatPill(
                (creditCardCount + loanCount).toString(),
                when (currentLanguage) {
                    "hi" -> "क्रेडिट उत्पाद"
                    "te" -> "క్రెడిట్ ఉత్పత్తులు"
                    else -> "Credit Products"
                },
                1f
            ))
        })
    }
    
    private fun createStatPill(number: String, label: String, weight: Float): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFFF0F4FF.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight).apply {
                marginStart = dpToPx(4)
                marginEnd = dpToPx(4)
            }
            
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(8).toFloat()
            shape.setColor(0xFFF0F4FF.toInt())
            background = shape
            
            addView(TextView(this@PersonalizedOffersActivity).apply {
                text = number
                textSize = 20f
                setTextColor(0xFF1E40AF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            addView(TextView(this@PersonalizedOffersActivity).apply {
                text = label
                textSize = 11f
                setTextColor(0xFF64748B.toInt())
            })
        }
    }
    
    private fun displayOffers(offers: List<PersonalizedOffer>) {
        contentLayout.removeAllViews()
        
        if (offers.isEmpty()) {
            showEmptyState(when (currentLanguage) {
                "hi" -> "🎁 कोई ऑफर नहीं मिला"
                "te" -> "🎁 ఆఫర్లు కనుగొనబడలేదు"
                else -> "🎁 No offers found"
            })
            return
        }
        
        offers.forEach { offer ->
            contentLayout.addView(createOfferCard(offer))
        }
    }
    
    private fun createOfferCard(offer: PersonalizedOffer): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            
            // Gradient background based on offer type (RESTORED ORIGINAL)
            val gradientColors = getOfferGradient(offer.offerType)
            val gradient = android.graphics.drawable.GradientDrawable()
            gradient.cornerRadius = dpToPx(16).toFloat()
            gradient.colors = gradientColors
            gradient.orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
            background = gradient
            
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(6).toFloat()
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dpToPx(16)
            layoutParams = params
            
            // Badges row (Pre-approved, Instant, etc.)
            if (offer.preApproved || offer.instantApproval) {
                addView(LinearLayout(this@PersonalizedOffersActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    
                    if (offer.preApproved) {
                        val preApprovedText = when (currentLanguage) {
                            "hi" -> "✅ पूर्व-स्वीकृत"
                            "te" -> "✅ ముందుగా-ఆమోదించబడింది"
                            else -> "✅ Pre-Approved"
                        }
                        addView(createBadge(preApprovedText, 0xFF16A34A.toInt(), 0xFFDCFCE7.toInt()))
                    }
                    
                    if (offer.instantApproval) {
                        val instantText = when (currentLanguage) {
                            "hi" -> "⚡ तत्काल"
                            "te" -> "⚡ తక్షణం"
                            else -> "⚡ Instant"
                        }
                        addView(createBadge(instantText, 0xFFEA580C.toInt(), 0xFFFFEDD5.toInt()))
                    }
                })
            }
            
            // Offer icon and title
            addView(TextView(this@PersonalizedOffersActivity).apply {
                val title = when (currentLanguage) {
                    "hi" -> offer.offerTitleHindi.ifEmpty { offer.offerTitle }
                    "te" -> offer.offerTitleTelugu.ifEmpty { offer.offerTitle }
                    else -> offer.offerTitle
                }
                text = "${getOfferIcon(offer.offerType)} $title"
                textSize = 22f
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dpToPx(12), 0, dpToPx(8))
                setShadowLayer(4f, 0f, 2f, 0x40000000)
            })
            
            // Product name
            addView(TextView(this@PersonalizedOffersActivity).apply {
                val productName = when (currentLanguage) {
                    "hi" -> offer.productNameHindi.ifEmpty { offer.productName }
                    "te" -> offer.productNameTelugu.ifEmpty { offer.productName }
                    else -> offer.productName
                }
                text = productName
                textSize = 16f
                setTextColor(0xFFF3F4F6.toInt())
                setPadding(0, 0, 0, dpToPx(12))
            })
            
            // Key benefits (white card - RESTORED ORIGINAL)
            addView(LinearLayout(this@PersonalizedOffersActivity).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xFFFFFFFF.toInt())
                setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
                
                val shape = android.graphics.drawable.GradientDrawable()
                shape.cornerRadius = dpToPx(12).toFloat()
                shape.setColor(0xFFFFFFFF.toInt())
                background = shape
                
                // Interest rate / Cashback
                if (offer.interestRate > 0) {
                    addView(TextView(this@PersonalizedOffersActivity).apply {
                        val label = when (currentLanguage) {
                            "hi" -> "💰 ब्याज दर:"
                            "te" -> "💰 వడ్డీ రేటు:"
                            else -> "💰 Interest Rate:"
                        }
                        text = "$label ${offer.interestRate}% p.a."
                        textSize = 14f
                        setTextColor(0xFF047857.toInt())
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(0, 0, 0, dpToPx(6))
                    })
                }
                
                if (offer.cashback > 0) {
                    addView(TextView(this@PersonalizedOffersActivity).apply {
                        val label = when (currentLanguage) {
                            "hi" -> "💸 कैशबैक:"
                            "te" -> "💸 క్యాష్‌బ్యాక్:"
                            else -> "💸 Cashback:"
                        }
                        text = "$label ₹${NumberFormat.getInstance().format(offer.cashback)}"
                        textSize = 14f
                        setTextColor(0xFF047857.toInt())
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(0, 0, 0, dpToPx(6))
                    })
                }
                
                // Eligible amount
                if (offer.eligibleAmount > 0) {
                    addView(TextView(this@PersonalizedOffersActivity).apply {
                        val label = when (currentLanguage) {
                            "hi" -> "🎯 पात्र राशि:"
                            "te" -> "🎯 అర్హత మొత్తం:"
                            else -> "🎯 Eligible Amount:"
                        }
                        text = "$label ₹${NumberFormat.getInstance().format(offer.eligibleAmount)}"
                        textSize = 14f
                        setTextColor(0xFF1E40AF.toInt())
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(0, 0, 0, dpToPx(6))
                    })
                }
                
                // Reward points
                if (offer.rewardPoints > 0) {
                    addView(TextView(this@PersonalizedOffersActivity).apply {
                        val label = when (currentLanguage) {
                            "hi" -> "⭐ स्वागत बोनस:"
                            "te" -> "⭐ స్వాగత బోనస్:"
                            else -> "⭐ Welcome Bonus:"
                        }
                        text = "$label ${NumberFormat.getInstance().format(offer.rewardPoints)} points"
                        textSize = 14f
                        setTextColor(0xFF7C3AED.toInt())
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    })
                }
            })
            
            // AI Transparency section (RESTORED ORIGINAL)
            addView(LinearLayout(this@PersonalizedOffersActivity).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0x20FFFFFF.toInt())
                setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12))
                
                val shape = android.graphics.drawable.GradientDrawable()
                shape.cornerRadius = dpToPx(10).toFloat()
                shape.setColor(0x20FFFFFF.toInt())
                background = shape
                
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.topMargin = dpToPx(12)
                layoutParams = params
                
                addView(TextView(this@PersonalizedOffersActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "🤖 एआई स्पष्टीकरण: हम यह क्यों सुझाते हैं?"
                        "te" -> "🤖 AI వివరణ: మేము దీన్ని ఎందుకు సూచిస్తున్నాము?"
                        else -> "🤖 AI Explanation: Why We Recommend This"
                    }
                    textSize = 13f
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, 0, 0, dpToPx(6))
                })
                
                addView(TextView(this@PersonalizedOffersActivity).apply {
                    val reason = when (currentLanguage) {
                        "hi" -> offer.aiReasonHindi
                        "te" -> offer.aiReasonTelugu
                        else -> offer.aiReasonEnglish
                    }
                    text = reason
                    textSize = 12f
                    setTextColor(0xFFFEFEFE.toInt())
                })
            })
            
            // Expiry date
            addView(TextView(this@PersonalizedOffersActivity).apply {
                val daysLeft = ((offer.expiryTimestamp - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                text = when (currentLanguage) {
                    "hi" -> "⏰ $daysLeft दिन बचे"
                    "te" -> "⏰ $daysLeft రోజులు మిగిలాయి"
                    else -> "⏰ $daysLeft days left"
                }
                textSize = 12f
                setTextColor(0xFFFEFEFE.toInt())
                setPadding(0, dpToPx(8), 0, dpToPx(12))
            })
            
            // Action buttons
            addView(LinearLayout(this@PersonalizedOffersActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
                
                // View Details
                addView(createActionButton(
                    when (currentLanguage) {
                        "hi" -> "📖 विवरण"
                        "te" -> "📖 వివరాలు"
                        else -> "📖 Details"
                    },
                    0xFFFFFFFF.toInt(),
                    getOfferAccentColor(offer.offerType)
                ) {
                    showOfferDetails(offer)
                })
                
                // Accept Offer
                addView(createActionButton(
                    when (currentLanguage) {
                        "hi" -> "✅ स्वीकार करें"
                        "te" -> "✅ అంగీకరించండి"
                        else -> "✅ Accept"
                    },
                    getOfferAccentColor(offer.offerType),
                    0xFFFFFFFF.toInt()
                ) {
                    acceptOffer(offer)
                })
            })
            
            // Clickable for full details
            isClickable = true
            isFocusable = true
            setOnClickListener {
                showOfferDetails(offer)
            }
        }
    }
    
    private fun createBadge(text: String, textColor: Int, bgColor: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(textColor)
            setBackgroundColor(bgColor)
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(6).toFloat()
            shape.setColor(bgColor)
            background = shape
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(8)
            }
        }
    }
    
    private fun createActionButton(text: String, bgColor: Int, textColor: Int, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textColor)
            setBackgroundColor(bgColor)
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(8).toFloat()
            shape.setColor(bgColor)
            background = shape
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dpToPx(8)
            }
            
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = resources.getDrawable(outValue.resourceId, null)
            
            setOnClickListener { onClick() }
        }
    }
    
    private fun getOfferIcon(offerType: String): String {
        return when (offerType) {
            "CREDIT_CARD" -> "💳"
            "PERSONAL_LOAN" -> "💰"
            "HOME_LOAN" -> "🏠"
            "INVESTMENT" -> "📈"
            "INSURANCE" -> "🛡️"
            "SAVINGS_ACCOUNT" -> "🏦"
            else -> "🎁"
        }
    }
    
    private fun getOfferGradient(offerType: String): IntArray {
        return AppTheme.Gradients.PrimaryHeader
    }
    
    private fun getOfferAccentColor(offerType: String): Int {
        return AppTheme.Primary.Blue
    }
    
    private fun showOfferDetails(offer: PersonalizedOffer) {
        val reason = when (currentLanguage) {
            "hi" -> offer.aiReasonHindi
            "te" -> offer.aiReasonTelugu
            else -> offer.aiReasonEnglish
        }
        
        val message = buildString {
            append("${getOfferIcon(offer.offerType)} ${offer.productName}\n\n")
            append("${offer.productDescription}\n\n")
            
            append("💰 Key Benefits:\n")
            if (offer.interestRate > 0) append("• Interest Rate: ${offer.interestRate}% p.a.\n")
            if (offer.cashback > 0) append("• Cashback: ₹${NumberFormat.getInstance().format(offer.cashback)}\n")
            if (offer.rewardPoints > 0) append("• Reward Points: ${NumberFormat.getInstance().format(offer.rewardPoints)}\n")
            if (offer.eligibleAmount > 0) append("• Eligible Amount: ₹${NumberFormat.getInstance().format(offer.eligibleAmount)}\n")
            if (offer.processingFee > 0) append("• Processing Fee: ₹${NumberFormat.getInstance().format(offer.processingFee)}\n")
            
            append("\n🤖 Why This Offer?\n$reason\n\n")
            
            if (offer.personalizationFactors.isNotEmpty()) {
                append("📊 Based on:\n")
                offer.personalizationFactors.forEach {
                    append("• ${it.replace("_", " ").capitalize()}\n")
                }
                append("\n")
            }
            
            if (offer.dataUsed.isNotEmpty()) {
                append("🔒 Data Used:\n")
                offer.dataUsed.forEach {
                    append("• ${it.replace("_", " ").capitalize()}\n")
                }
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle(offer.offerTitle)
            .setMessage(message)
            .setPositiveButton(when (currentLanguage) {
                "hi" -> "✅ इस ऑफर को स्वीकार करें"
                "te" -> "✅ ఆఫర్‌ను అంగీకరించండి"
                else -> "✅ Accept Offer"
            }) { dialog, _ ->
                acceptOffer(offer)
                dialog.dismiss()
            }
            .setNegativeButton(when (currentLanguage) {
                "hi" -> "❌ रुचि नहीं है"
                "te" -> "❌ ఆసక్తి లేదు"
                else -> "❌ Not Interested"
            }) { dialog, _ ->
                rejectOffer(offer)
                dialog.dismiss()
            }
            .setNeutralButton(when (currentLanguage) {
                "hi" -> "बंद करें"
                "te" -> "మూసివేయండి"
                else -> "Close"
            }, null)
            .show()
    }
    
    private fun acceptOffer(offer: PersonalizedOffer) {
        // Update Firebase
        val database = FirebaseDatabase.getInstance()
        val offerRef = database.getReference("personalizedOffers/${offer.id}")
        offerRef.child("userAccepted").setValue(true)
        offerRef.child("userViewed").setValue(true)
        offerRef.child("status").setValue("ACCEPTED")
        
        Toast.makeText(this, when (currentLanguage) {
            "hi" -> "✅ ऑफर स्वीकार किया गया! बैंक जल्द ही संपर्क करेगा।"
            "te" -> "✅ ఆఫర్ ఆమోదించబడింది! బ్యాంక్ త్వరలో సంప్రదిస్తుంది."
            else -> "✅ Offer accepted! Bank will contact you soon."
        }, Toast.LENGTH_LONG).show()
        
        loadPersonalizedOffers() // Refresh
    }
    
    private fun rejectOffer(offer: PersonalizedOffer) {
        // Update Firebase
        val database = FirebaseDatabase.getInstance()
        val offerRef = database.getReference("personalizedOffers/${offer.id}")
        offerRef.child("userRejected").setValue(true)
        offerRef.child("userHidden").setValue(true)
        offerRef.child("status").setValue("REJECTED")
        
        Toast.makeText(this, when (currentLanguage) {
            "hi" -> "❌ ऑफर अस्वीकार किया गया"
            "te" -> "❌ ఆఫర్ తిరస్కరించబడింది"
            else -> "❌ Offer rejected"
        }, Toast.LENGTH_SHORT).show()
        
        loadPersonalizedOffers() // Refresh
    }
    
    private fun applyFilter(filterPosition: Int) {
        val filtered = when (filterPosition) {
            0 -> allOffers // All
            1 -> allOffers.filter { it.offerType == "CREDIT_CARD" }
            2 -> allOffers.filter { it.offerType.contains("LOAN") }
            3 -> allOffers.filter { it.offerType == "INVESTMENT" }
            4 -> allOffers.filter { it.offerType == "INSURANCE" }
            else -> allOffers
        }
        
        displayOffers(filtered)
    }
    
    private fun showEmptyState(message: String) {
        contentLayout.removeAllViews()
        contentLayout.addView(TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
            setPadding(dpToPx(40), dpToPx(60), dpToPx(40), dpToPx(60))
        })
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

