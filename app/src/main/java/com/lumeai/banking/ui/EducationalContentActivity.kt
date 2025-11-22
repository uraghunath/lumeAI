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
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme

/**
 * Financial Literacy Hub - Comprehensive banking education
 * covering credit, loans, accounts, and financial products
 */
class EducationalContentActivity : AppCompatActivity() {
    
    private var currentLanguage = "en"
    private lateinit var rootLayout: LinearLayout
    private lateinit var scrollView: ScrollView
    private var activeSection = "intro"
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    // Section markers for navigation
    private val sectionPositions = mutableMapOf<String, Int>()
    private val navButtons = mutableMapOf<String, TextView>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = LanguageHelper.getCurrentLanguage(this)
        
        // Blue status bar - same as all other pages
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        setContentView(createUI())
    }
    
    private fun createUI(): LinearLayout {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(AppTheme.Background.Primary)
        }
        
        // Header (fixed at top)
        mainLayout.addView(createHeader())
        
        // Language Selector (fixed)
        mainLayout.addView(createLanguageSelector())
        
        // Quick Navigation Bar (fixed)
        mainLayout.addView(createQuickNavBar())
        
        // Scrollable Content
        scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setBackgroundColor(AppTheme.Background.Primary)
            
            rootLayout = LinearLayout(this@EducationalContentActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
            addView(rootLayout)
        }
        
        mainLayout.addView(scrollView)
        
        refreshContent()
        
        return mainLayout
    }
    
    private fun createQuickNavBar(): LinearLayout {
        navButtons.clear()
        
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.WHITE)
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(1), Color.parseColor("#E5E7EB"))
            }
            background = shape
            
            // Title
            addView(TextView(this@EducationalContentActivity).apply {
                text = if (currentLanguage == "hi") "📑 विषय" 
                       else if (currentLanguage == "te") "📑 అంశాలు"
                       else "📑 Topics"
                textSize = 15f
                setTextColor(Color.parseColor("#6B7280"))
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(4), 0, 0, dp(10))
            })
            
            val topics = if (currentLanguage == "hi") listOf(
                "परिचय" to "intro",
                "क्रेडिट स्कोर" to "credit",
                "बैंकिंग उत्पाद" to "products",
                "ऋण प्रकार" to "loans",
                "खाते" to "accounts",
                "सुझाव" to "tips"
            ) else if (currentLanguage == "te") listOf(
                "పరిచయం" to "intro",
                "క్రెడిట్ స్కోర్" to "credit",
                "బ్యాంకింగ్ ఉత్పత్తులు" to "products",
                "రుణ రకాలు" to "loans",
                "ఖాతాలు" to "accounts",
                "చిట్కాలు" to "tips"
            ) else listOf(
                "Introduction" to "intro",
                "Credit Scores" to "credit",
                "Banking Products" to "products",
                "Loan Types" to "loans",
                "Accounts" to "accounts",
                "Financial Tips" to "tips"
            )
            
            // Create rows of 3 topics each
            val rows = topics.chunked(3)
            rows.forEach { rowTopics ->
                addView(createNavRow(rowTopics))
            }
        }
    }
    
    private fun createNavRow(topics: List<Pair<String, String>>): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(10))
            }
            
            topics.forEachIndexed { index, (name, id) ->
                val button = createNavButton(name, id)
                navButtons[id] = button
                addView(button)
                
                // Add spacing between buttons (but not after last one)
                if (index < topics.size - 1) {
                    addView(Space(this@EducationalContentActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(10), 0)
                    })
                }
            }
        }
    }
    
    private fun createNavButton(name: String, sectionId: String): TextView {
        val isActive = sectionId == activeSection
        
        return TextView(this).apply {
            text = name
            textSize = 13f
            setPadding(dp(12), dp(12), dp(12), dp(12))
            gravity = Gravity.CENTER
            setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
            
            // Active vs Inactive styling - Blue theme
            if (isActive) {
                setTextColor(Color.WHITE)
                val shape = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(AppTheme.Text.OnCard)
                }
                background = shape
                elevation = dp(2).toFloat()
            } else {
                setTextColor(AppTheme.Text.OnCardSecondary)
                val shape = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.WHITE)
                    setStroke(dp(1), 0xFFE0E0E0.toInt())
                }
                background = shape
            }
            
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            
            setOnClickListener {
                scrollToSection(sectionId)
                setActiveSection(sectionId)
            }
        }
    }
    
    private fun setActiveSection(sectionId: String) {
        activeSection = sectionId
        
        // Update all button styles
        navButtons.forEach { (id, button) ->
            val isActive = id == sectionId
            
            if (isActive) {
                button.setTextColor(Color.WHITE)
                button.setTypeface(null, Typeface.BOLD)
                val shape = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(AppTheme.Text.OnCard)
                }
                button.background = shape
                button.elevation = dp(2).toFloat()
            } else {
                button.setTextColor(AppTheme.Text.OnCardSecondary)
                button.setTypeface(null, Typeface.NORMAL)
                val shape = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.WHITE)
                    setStroke(dp(1), 0xFFE0E0E0.toInt())
                }
                button.background = shape
                button.elevation = 0f
            }
        }
    }
    
    private fun scrollToSection(sectionId: String) {
        // Post to ensure layout is complete
        scrollView.post {
            val position = sectionPositions[sectionId] ?: 0
            scrollView.smoothScrollTo(0, position)
        }
    }
    
    private fun markSection(sectionId: String, view: android.view.View) {
        view.post {
            // Calculate absolute Y position
            val location = IntArray(2)
            view.getLocationInWindow(location)
            sectionPositions[sectionId] = view.top
        }
    }
    
    private fun refreshContent() {
        rootLayout.removeAllViews()
        sectionPositions.clear()
        activeSection = "intro"
        
        // Introduction
        val introSection = createIntroSection()
        rootLayout.addView(introSection)
        markSection("intro", introSection)
        
        // Credit Score Deep Dive
        val creditSection = createCreditScoreSection()
        rootLayout.addView(creditSection)
        markSection("credit", creditSection)
        
        // Banking Products
        val productsSection = createProductsSection()
        rootLayout.addView(productsSection)
        markSection("products", productsSection)
        
        // Loan Types
        val loansSection = createLoansSection()
        rootLayout.addView(loansSection)
        markSection("loans", loansSection)
        
        // Account Types
        val accountsSection = createAccountsSection()
        rootLayout.addView(accountsSection)
        markSection("accounts", accountsSection)
        
        // Financial Planning Tips (moved cards and interest into tips)
        val tipsSection = createFinancialTipsSection()
        rootLayout.addView(tipsSection)
        markSection("tips", tipsSection)
        
        // Bottom padding
        rootLayout.addView(Space(this@EducationalContentActivity).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(30))
        })
    }
    
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
            
            // Compact back button
            addView(TextView(this@EducationalContentActivity).apply {
                text = "←"
                textSize = 24f
                setTextColor(Color.WHITE)
                setPadding(0, 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                gravity = Gravity.CENTER
                setOnClickListener { finish() }
            })
            
            // Title only (no subtitle)
            addView(TextView(this@EducationalContentActivity).apply {
                text = if (currentLanguage == "hi") "वित्तीय साक्षरता" 
                       else if (currentLanguage == "te") "ఆర్థిక అక్షరాస్యత"
                       else "Financial Literacy"
                textSize = 18f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
            })
        }
    }
    
    private fun createLanguageSelector(): LinearLayout {
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
            
            val languages = listOf(
                "English" to "en",
                "हिंदी" to "hi",
                "తెలుగు" to "te"
            )
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(Space(this@EducationalContentActivity).apply {
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
                    setColor(AppTheme.Text.OnCard)
                } else {
                    setColor(Color.WHITE)
                    setStroke(dp(1), AppTheme.Text.OnCardSecondary)
                }
            }
            background = shape
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    LanguageHelper.setLanguage(this@EducationalContentActivity, code)
                    recreate()
                }
            }
        }
    }
    
    private fun createIntroSection(): LinearLayout {
        return createSection(
            if (currentLanguage == "hi") "💡 आपका वित्तीय शिक्षा केंद्र" 
            else if (currentLanguage == "te") "💡 మీ ఆర్థిక విద్యా కేంద్రం"
            else "💡 Your Financial Education Hub",
            
            if (currentLanguage == "hi") 
                "यह मंच आपको भारत में बैंकिंग, ऋण, क्रेडिट स्कोर और वित्तीय उत्पादों के बारे में व्यापक ज्ञान प्रदान करता है। सूचित वित्तीय निर्णय लेने के लिए सीखें।"
            else if (currentLanguage == "te")
                "ఈ ప్లాట్‌ఫారమ్ భారతదేశంలో బ్యాంకింగ్, రుణాలు, క్రెడిట్ స్కోర్‌లు మరియు ఆర్థిక ఉత్పత్తుల గురించి సమగ్ర పరిజ్ఞానాన్ని అందిస్తుంది."
            else 
                "This platform provides comprehensive knowledge about banking, loans, credit scores, and financial products in India. Learn to make informed financial decisions.",
            "#E0F2FE"
        )
    }
    
    private fun createCreditScoreSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            addView(createSectionTitle(
                if (currentLanguage == "hi") "📊 क्रेडिट स्कोर की पूरी जानकारी"
                else if (currentLanguage == "te") "📊 క్రెడిట్ స్కోర్ పూర్తి సమాచారం"
                else "📊 Credit Score: Complete Guide"
            ))
            
            // What is Credit Score
            addView(createInfoCard(
                if (currentLanguage == "hi") "क्रेडिट स्कोर क्या है?"
                else if (currentLanguage == "te") "క్రెడిట్ స్కోర్ అంటే ఏమిటి?"
                else "What is a Credit Score?",
                
                if (currentLanguage == "hi")
                    "300-900 के बीच की एक संख्या जो आपकी साख को दर्शाती है। यह CIBIL, Experian, Equifax और CRIF द्वारा गणना की जाती है। स्कोर आपके भुगतान इतिहास (35%), क्रेडिट उपयोग (30%), क्रेडिट इतिहास की लंबाई (15%), क्रेडिट मिश्रण (10%), और नए क्रेडिट (10%) पर आधारित है।"
                else if (currentLanguage == "te")
                    "300-900 మధ్య సంఖ్య మీ క్రెడిట్ విశ్వసనీయతను సూచిస్తుంది. CIBIL, Experian, Equifax మరియు CRIF ద్వారా లెక్కించబడుతుంది."
                else
                    "A number between 300-900 that represents your creditworthiness. Calculated by CIBIL, Experian, Equifax, and CRIF. Score is based on: Payment History (35%), Credit Utilization (30%), Length of Credit History (15%), Credit Mix (10%), and New Credit (10%).",
                "#DBEAFE"
            ))
            
            // Score Ranges
            addView(createScoreRangeCard("750-900", 
                if (currentLanguage == "hi") "उत्कृष्ट" else if (currentLanguage == "te") "అద్భుతమైన" else "Excellent",
                if (currentLanguage == "hi") "सर्वोत्तम दरें, आसान स्वीकृति" else if (currentLanguage == "te") "అత్యుత్తమ రేట్లు" else "Best rates, easy approval",
                "#10B981"))
            addView(createScoreRangeCard("700-749", 
                if (currentLanguage == "hi") "अच्छा" else if (currentLanguage == "te") "మంచి" else "Good",
                if (currentLanguage == "hi") "प्रतिस्पर्धी दरें, अच्छी संभावनाएं" else if (currentLanguage == "te") "మంచి అవకాశాలు" else "Competitive rates, good chances",
                "#3B82F6"))
            addView(createScoreRangeCard("650-699", 
                if (currentLanguage == "hi") "औसत" else if (currentLanguage == "te") "సగటు" else "Fair",
                if (currentLanguage == "hi") "उच्च दरें, सावधानीपूर्वक समीक्षा" else if (currentLanguage == "te") "అధిక రేట్లు" else "Higher rates, careful review",
                "#F59E0B"))
            addView(createScoreRangeCard("< 650", 
                if (currentLanguage == "hi") "सुधार की आवश्यकता" else if (currentLanguage == "te") "మెరుగుదల అవసరం" else "Needs Improvement",
                if (currentLanguage == "hi") "कठिन स्वीकृति, उच्च दरें या अस्वीकृति" else if (currentLanguage == "te") "కష్టమైన ఆమోదం" else "Difficult approval, high rates or rejection",
                "#EF4444"))
            
            // How to Improve
            addView(createInfoCard(
                if (currentLanguage == "hi") "स्कोर कैसे सुधारें?"
                else if (currentLanguage == "te") "స్కోర్ ఎలా మెరుగుపరచాలి?"
                else "How to Improve Your Score?",
                
                if (currentLanguage == "hi")
                    "✅ हमेशा समय पर भुगतान करें (सबसे महत्वपूर्ण!)\n✅ क्रेडिट उपयोग को 30% से कम रखें\n✅ पुराने खातों को बंद न करें\n✅ एक साथ कई आवेदन न करें\n✅ क्रेडिट मिश्रण बनाए रखें (सुरक्षित + असुरक्षित)\n✅ नियमित रूप से अपनी रिपोर्ट जांचें"
                else if (currentLanguage == "te")
                    "✅ ఎల్లప్పుడూ సమయానికి చెల్లించండి\n✅ క్రెడిట్ వినియోగాన్ని 30% కంటే తక్కువగా ఉంచండి\n✅ పాత ఖాతాలను మూసివేయవద్దు"
                else
                    "✅ Always pay on time (Most important!)\n✅ Keep credit utilization below 30%\n✅ Don't close old accounts\n✅ Avoid multiple applications at once\n✅ Maintain credit mix (secured + unsecured)\n✅ Check your report regularly",
                "#F0FDF4"
            ))
        }
    }
    
    private fun createProductsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            addView(createSectionTitle(
                if (currentLanguage == "hi") "🏦 बैंकिंग उत्पाद"
                else if (currentLanguage == "te") "🏦 బ్యాంకింగ్ ఉత్పత్తులు"
                else "🏦 Banking Products Overview"
            ))
            
            addView(createProductCard(
                "💳", 
                if (currentLanguage == "hi") "क्रेडिट कार्ड" else if (currentLanguage == "te") "క్రెడిట్ కార్డ్" else "Credit Cards",
                if (currentLanguage == "hi") 
                    "प्रकार: रिवॉर्ड, कैशबैक, ट्रैवल, फ्यूल, प्रीमियम। ब्याज दर: 36-42% वार्षिक। न्यूनतम आय: ₹2-5 लाख। फीस: ₹500-₹10,000। सर्वश्रेष्ठ: HDFC Regalia, SBI Card Elite, Axis Magnus।"
                else if (currentLanguage == "te")
                    "రకాలు: రివార్డ్స్, క్యాష్‌బ్యాక్, ట్రావెల్। వడ్డీ రేటు: 36-42% వార్షిక। కనీస ఆదాయం: ₹2-5 లక్షలు।"
                else
                    "Types: Rewards, Cashback, Travel, Fuel, Premium. Interest Rate: 36-42% p.a. Min Income: ₹2-5 lakhs. Fees: ₹500-₹10,000. Best: HDFC Regalia, SBI Card Elite, Axis Magnus.",
                "#EDE9FE"
            ))
            
            addView(createProductCard(
                "🏠", 
                if (currentLanguage == "hi") "गृह ऋण" else if (currentLanguage ==="te") "గృహ రుణం" else "Home Loans",
                if (currentLanguage == "hi")
                    "दरें: 8.5-10% वार्षिक। अवधि: 5-30 वर्ष। LTV: 75-90%। अधिकतम: ₹2-10 करोड़। प्रकार: फ्लोटिंग, फिक्स्ड, हाइब्रिड। लाभ: 80C में ₹1.5L और 24B में ₹2L कर कटौती। सर्वश्रेष्ठ बैंक: SBI, HDFC, ICICI, LIC।"
                else if (currentLanguage == "te")
                    "రేట్లు: 8.5-10% వార్షిక। వ్యవధి: 5-30 సంవత్సరాలు। LTV: 75-90%। గరిష్ఠం: ₹2-10 కోట్లు।"
                else
                    "Rates: 8.5-10% p.a. Tenure: 5-30 years. LTV: 75-90%. Max: ₹2-10 crore. Types: Floating, Fixed, Hybrid. Benefits: ₹1.5L deduction under 80C and ₹2L under 24B. Best Banks: SBI, HDFC, ICICI, LIC.",
                "#FEF3C7"
            ))
            
            addView(createProductCard(
                "🚗", 
                if (currentLanguage == "hi") "वाहन ऋण" else if (currentLanguage == "te") "వాహన రుణం" else "Vehicle Loans",
                if (currentLanguage == "hi")
                    "दरें: 8-12% वार्षिक। अवधि: 1-7 वर्ष। डाउन पेमेंट: 10-20%। नई कार: कम दर। पुरानी कार: उच्च दर। प्रोसेसिंग फीस: 0.25-2%। बीमा अनिवार्य। EMI कैलकुलेटर उपयोग करें।"
                else if (currentLanguage == "te")
                    "రేట్లు: 8-12% వార్షిక। వ్యవధి: 1-7 సంవత్సరాలు। డౌన్ పేమెంట్: 10-20%।"
                else
                    "Rates: 8-12% p.a. Tenure: 1-7 years. Down Payment: 10-20%. New Car: Lower rates. Used Car: Higher rates. Processing: 0.25-2%. Insurance mandatory. Use EMI calculator.",
                "#DBEAFE"
            ))
            
            addView(createProductCard(
                "💼", 
                if (currentLanguage == "hi") "व्यक्तिगत ऋण" else if (currentLanguage == "te") "వ్యక्తిగత రుణం" else "Personal Loans",
                if (currentLanguage == "hi")
                    "दरें: 10-20% वार्षिक। अवधि: 1-5 वर्ष। राशि: ₹50K-₹40L। असुरक्षित। तेज स्वीकृति। क्रेडिट स्कोर 750+ चाहिए। कोई सुरक्षा नहीं। उपयोग: शादी, शिक्षा, चिकित्सा, यात्रा।"
                else if (currentLanguage == "te")
                    "రేట్లు: 10-20% వార్షిక। వ్యవధి: 1-5 సంవత్సరాలు। మొత్తం: ₹50K-₹40L।"
                else
                    "Rates: 10-20% p.a. Tenure: 1-5 years. Amount: ₹50K-₹40L. Unsecured. Fast approval. Credit Score 750+ needed. No collateral. Use: Wedding, Education, Medical, Travel.",
                "#FEE2E2"
            ))
        }
    }
    
    private fun createLoansSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            addView(createSectionTitle(
                if (currentLanguage == "hi") "📋 ऋण के प्रकार विस्तार से"
                else if (currentLanguage == "te") "📋 రుణ రకాలు వివరంగా"
                else "📋 Loan Types in Detail"
            ))
            
            addView(createExpandableCard(
                if (currentLanguage == "hi") "शिक्षा ऋण" else if (currentLanguage == "te") "విద్యా రుణం" else "Education Loan",
                if (currentLanguage == "hi")
                    "राशि: ₹10L-₹1.5Cr\nदरें: 8-12% वार्षिक\nअवधि: 10-15 वर्ष\nमोरेटोरियम: पाठ्यक्रम + 1 वर्ष\nटैक्स लाभ: 80E के तहत\nसर्वश्रेष्ठ: SBI Scholar, Avanse, HDFC Credila"
                else if (currentLanguage == "te")
                    "మొత్తం: ₹10L-₹1.5Cr\nరేట్లు: 8-12% వార్షిక\nవ్యవధి: 10-15 సంవత్సరాలు"
                else
                    "Amount: ₹10L-₹1.5Cr\nRates: 8-12% p.a.\nTenure: 10-15 years\nMoratorium: Course + 1 year\nTax Benefit: Section 80E\nBest: SBI Scholar, Avanse, HDFC Credila"
            ))
            
            addView(createExpandableCard(
                if (currentLanguage == "hi") "व्यवसाय ऋण" else if (currentLanguage == "te") "వ్యాపార రుణం" else "Business Loan",
                if (currentLanguage == "hi")
                    "राशि: ₹50K-₹50Cr\nदरें: 11-20% वार्षिक\nअवधि: 1-10 वर्ष\nप्रकार: सुरक्षित, असुरक्षित, MSME\nआवश्यकता: ITR, GST, बैंक स्टेटमेंट\nसरकारी योजना: Mudra (₹10L तक), CGTMSE"
                else if (currentLanguage == "te")
                    "మొత్తం: ₹50K-₹50Cr\nరేట్లు: 11-20% వార్షిక\nవ్యవధి: 1-10 సంవత్సరాలు"
                else
                    "Amount: ₹50K-₹50Cr\nRates: 11-20% p.a.\nTenure: 1-10 years\nTypes: Secured, Unsecured, MSME\nNeeds: ITR, GST, Bank Statement\nGovt Schemes: Mudra (up to ₹10L), CGTMSE"
            ))
            
            addView(createExpandableCard(
                if (currentLanguage == "hi") "गोल्ड लोन" else if (currentLanguage == "te") "బంగారు రుణం" else "Gold Loan",
                if (currentLanguage == "hi")
                    "दरें: 7-12% वार्षिक\nLTV: 75% (RBI मानदंड)\nकोई क्रेडिट स्कोर नहीं चाहिए\nतेज स्वीकृति: 30 मिनट\nन्यूनतम कागजी कार्रवाई\nसर्वश्रेष्ठ: Muthoot, Manappuram, IIFL"
                else if (currentLanguage == "te")
                    "రేట్లు: 7-12% వార్షిక\nLTV: 75%\nక్రెడిట్ స్కోర్ అవసరం లేదు"
                else
                    "Rates: 7-12% p.a.\nLTV: 75% (RBI norms)\nNo credit score needed\nQuick approval: 30 mins\nMinimal documentation\nBest: Muthoot, Manappuram, IIFL"
            ))
        }
    }
    
    private fun createAccountsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            addView(createSectionTitle(
                if (currentLanguage == "hi") "💰 बैंक खाते के प्रकार"
                else if (currentLanguage == "te") "💰 బ్యాంక్ ఖాతా రకాలు"
                else "💰 Bank Account Types"
            ))
            
            addView(createAccountCard(
                if (currentLanguage == "hi") "बचत खाता" else if (currentLanguage == "te") "పొదుపు ఖాతా" else "Savings Account",
                if (currentLanguage == "hi")
                    "ब्याज: 2.7-4% वार्षिक\nMAB: ₹0-₹10K\nलेनदेन सीमा: 5-10/माह\nसर्वश्रेष्ठ: HDFC Bank, SBI, ICICI\nलाभ: नेटबैंकिंग, डेबिट कार्ड, UPI"
                else if (currentLanguage == "te")
                    "వడ్డీ: 2.7-4% వార్షిక\nMAB: ₹0-₹10K\nట్రాన్సాక్షన్ పరిమితి: 5-10/నెల"
                else
                    "Interest: 2.7-4% p.a.\nMAB: ₹0-₹10K\nTransaction Limit: 5-10/month\nBest: HDFC Bank, SBI, ICICI\nBenefits: Netbanking, Debit Card, UPI"
            ))
            
            addView(createAccountCard(
                if (currentLanguage == "hi") "चालू खाता" else if (currentLanguage == "te") "కరెంట్ ఖాతా" else "Current Account",
                if (currentLanguage == "hi")
                    "ब्याज: कोई नहीं\nMAB: ₹25K-₹1L\nअसीमित लेनदेन\nव्यवसाय के लिए\nओवरड्राफ्ट सुविधा\nनकद जमा सीमा अधिक"
                else if (currentLanguage == "te")
                    "వడ్డీ: ఏదీ లేదు\nMAB: ₹25K-₹1L\nఅపరిమిత లావాదేవీలు"
                else
                    "Interest: None\nMAB: ₹25K-₹1L\nUnlimited transactions\nFor businesses\nOverdraft facility\nHigher cash deposit limit"
            ))
            
            addView(createAccountCard(
                if (currentLanguage == "hi") "फिक्स्ड डिपॉजिट" else if (currentLanguage == "te") "స్థిర డిపాజిట్" else "Fixed Deposit",
                if (currentLanguage == "hi")
                    "ब्याज: 5-7.5% वार्षिक\nअवधि: 7 दिन-10 वर्ष\nसुरक्षित निवेश\nटैक्स लाभ: 80C (5 वर्ष FD)\nऋण सुविधा: 70-90%"
                else if (currentLanguage == "te")
                    "వడ్డీ: 5-7.5% వార్షిక\nవ్యవధి: 7 రోజులు-10 సంవత్సరాలు"
                else
                    "Interest: 5-7.5% p.a.\nTenure: 7 days-10 years\nSafe investment\nTax Benefit: 80C (5-year FD)\nLoan facility: 70-90%"
            ))
            
            addView(createAccountCard(
                if (currentLanguage == "hi") "आवर्ती जमा" else if (currentLanguage == "te") "రికరింగ్ డిపాజిట్" else "Recurring Deposit",
                if (currentLanguage == "hi")
                    "ब्याज: 5-7% वार्षिक\nअवधि: 6 महीने-10 वर्ष\nमासिक जमा\nअनुशासित बचत\nलचीला निवेश"
                else if (currentLanguage == "te")
                    "వడ్డీ: 5-7% వార్షిక\nవ్యవధి: 6 నెలలు-10 సంవత్సరాలు"
                else
                    "Interest: 5-7% p.a.\nTenure: 6 months-10 years\nMonthly deposits\nDisciplined savings\nFlexible investment"
            ))
        }
    }
    
    private fun createCreditCardsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            addView(createSectionTitle(
                if (currentLanguage == "hi") "💳 क्रेडिट कार्ड गाइड"
                else if (currentLanguage == "te") "💳 క్రెడిట్ కార్డ్ గైడ్"
                else "💳 Credit Card Complete Guide"
            ))
            
            addView(createInfoCard(
                if (currentLanguage == "hi") "क्रेडिट कार्ड का सही उपयोग"
                else if (currentLanguage == "te") "క్రెడిట్ కార్డ్ సరైన వినియోగం"
                else "Smart Credit Card Usage",
                if (currentLanguage == "hi")
                    "✅ समय पर पूर्ण भुगतान करें\n✅ 30% से कम उपयोग रखें\n✅ ऑटो-पे सेट करें\n✅ रिवॉर्ड्स को समझें\n✅ हिडन फीस जांचें\n❌ न्यूनतम देय से बचें\n❌ नकद निकासी न करें"
                else if (currentLanguage == "te")
                    "✅ సమయానికి పూర్తిగా చెల్లించండి\n✅ 30% కంటే తక్కువ వినియోగం\n✅ ఆటో-పే సెట్ చేయండి"
                else
                    "✅ Pay full amount on time\n✅ Keep utilization below 30%\n✅ Set up auto-pay\n✅ Understand rewards\n✅ Check hidden fees\n❌ Avoid minimum due\n❌ Don't withdraw cash",
                "#F3E8FF"
            ))
        }
    }
    
    private fun createInterestRatesSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            addView(createSectionTitle(
                if (currentLanguage == "hi") "📈 ब्याज दरें समझें"
                else if (currentLanguage == "te") "📈 వడ్డీ రేట్లు అర్థం చేసుకోండి"
                else "📈 Understanding Interest Rates"
            ))
            
            addView(createInfoCard(
                if (currentLanguage == "hi") "ब्याज दरों के प्रकार"
                else if (currentLanguage == "te") "వడ్డీ రేట్ల రకాలు"
                else "Types of Interest Rates",
                if (currentLanguage == "hi")
                    "📍 फ्लोटिंग रेट: बाजार से जुड़ी, बदलती रहती है\n📍 फिक्स्ड रेट: पूरी अवधि के लिए स्थिर\n📍 MCLR: Marginal Cost-आधारित\n📍 रेपो रेट: RBI की नीति दर\n📍 APR vs APY: वार्षिक प्रतिशत दर बनाम उपज"
                else if (currentLanguage == "te")
                    "📍 ఫ్లోటింగ్ రేట్: మార్కెట్‌కు లింక్, మారుతూ ఉంటుంది\n📍 ఫిక్స్‌డ్ రేట్: స్థిరంగా ఉంటుంది"
                else
                    "📍 Floating Rate: Market-linked, changes periodically\n📍 Fixed Rate: Constant throughout tenure\n📍 MCLR: Marginal Cost-based lending rate\n📍 Repo Rate: RBI's policy rate\n📍 APR vs APY: Annual Percentage Rate vs Yield",
                "#FEF9C3"
            ))
        }
    }
    
    private fun createFinancialTipsSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            addView(createSectionTitle(
                if (currentLanguage == "hi") "💡 वित्तीय योजना टिप्स"
                else if (currentLanguage == "te") "💡 ఆర్థిక ప్రణాళిక చిట్కాలు"
                else "💡 Financial Planning Tips"
            ))
            
            val tips = if (currentLanguage == "hi") listOf(
                "50-30-20 नियम: 50% आवश्यकताएं, 30% इच्छाएं, 20% बचत",
                "आपातकालीन फंड: 6 महीने का खर्च बचाएं",
                "निवेश जल्दी शुरू करें: चक्रवृद्धि की शक्ति",
                "जोखिम विविधता: सभी अंडे एक टोकरी में न रखें",
                "बीमा जरूरी है: जीवन और स्वास्थ्य बीमा लें"
            ) else if (currentLanguage == "te") listOf(
                "50-30-20 నియమం: 50% అవసరాలు, 30% కోరికలు, 20% పొదుపు",
                "అత్యవసర నిధి: 6 నెలల ఖర్చు ఆదా చేయండి",
                "త్వరగా పెట్టుబడి పెట్టండి: కాంపౌండింగ్ శక్తి"
            ) else listOf(
                "50-30-20 Rule: 50% needs, 30% wants, 20% savings",
                "Emergency Fund: Save 6 months of expenses",
                "Start Investing Early: Power of compounding",
                "Diversify Risks: Don't put all eggs in one basket",
                "Insurance is Must: Get life and health insurance"
            )
            
            tips.forEach { tip ->
                addView(createTipCard(tip))
            }
        }
    }
    
    private fun createSection(title: String, description: String, bgColor: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(10))
            
            val card = LinearLayout(this@EducationalContentActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(20), dp(20), dp(20))
                
                val shape = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(1), 0xFFE0E0E0.toInt())
                }
                background = shape
                elevation = dp(2).toFloat()
                
                addView(TextView(this@EducationalContentActivity).apply {
                    text = title
                    textSize = 18f
                    setTextColor(AppTheme.Text.OnCard)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 0, 0, dp(12))
                })
                
                addView(TextView(this@EducationalContentActivity).apply {
                    text = description
                    textSize = 15f
                    setTextColor(AppTheme.Text.OnCardSecondary)
                    setLineSpacing(0f, 1.5f)
                })
            }
            
            addView(card)
        }
    }
    
    private fun createSectionTitle(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 19f
            setTextColor(Color.parseColor("#1F2937"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(15))
        }
    }
    
    private fun createInfoCard(title: String, content: String, bgColor: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
            setPadding(dp(18), dp(18), dp(18), dp(18))
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), 0xFFE0E0E0.toInt())
            }
            background = shape
            elevation = dp(2).toFloat()
            
            addView(TextView(this@EducationalContentActivity).apply {
                text = title
                textSize = 16f
                setTextColor(AppTheme.Text.OnCard)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(10))
            })
            
            addView(TextView(this@EducationalContentActivity).apply {
                text = content
                textSize = 14f
                setTextColor(AppTheme.Text.OnCardSecondary)
                setLineSpacing(0f, 1.5f)
            })
        }
    }
    
    private fun createScoreRangeCard(range: String, label: String, desc: String, color: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(10))
            }
            setPadding(dp(16), dp(14), dp(16), dp(14))
            gravity = Gravity.CENTER_VERTICAL
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(8).toFloat()
                setStroke(dp(3), Color.parseColor(color))
            }
            background = shape
            
            addView(TextView(this@EducationalContentActivity).apply {
                text = range
                textSize = 16f
                setTextColor(Color.parseColor(color))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(dp(100), ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            
            addView(LinearLayout(this@EducationalContentActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                
                addView(TextView(this@EducationalContentActivity).apply {
                    text = label
                    textSize = 15f
                    setTextColor(Color.parseColor("#1F2937"))
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@EducationalContentActivity).apply {
                    text = desc
                    textSize = 13f
                    setTextColor(Color.parseColor("#6B7280"))
                })
            })
        }
    }
    
    private fun createProductCard(emoji: String, title: String, content: String, bgColor: String): LinearLayout {
        return createInfoCard("$emoji $title", content, bgColor)
    }
    
    private fun createExpandableCard(title: String, content: String): LinearLayout {
        return createInfoCard(title, content, "#FFFFFF").apply {
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.parseColor("#E5E7EB"))
            }
            background = shape
        }
    }
    
    private fun createAccountCard(title: String, content: String): LinearLayout {
        return createInfoCard(title, content, "#F9FAFB")
    }
    
    private fun createTipCard(tip: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(10))
            }
            setPadding(dp(16), dp(14), dp(16), dp(14))
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(8).toFloat()
                setStroke(dp(1), 0xFFE0E0E0.toInt())
            }
            background = shape
            elevation = dp(2).toFloat()
            
            addView(TextView(this@EducationalContentActivity).apply {
                text = "💡"
                textSize = 20f
                setPadding(0, 0, dp(12), 0)
            })
            
            addView(TextView(this@EducationalContentActivity).apply {
                text = tip
                textSize = 14f
                setTextColor(AppTheme.Text.OnCardSecondary)
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
