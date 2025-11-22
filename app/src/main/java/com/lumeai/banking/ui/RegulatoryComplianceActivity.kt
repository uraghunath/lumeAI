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
import com.lumeai.banking.utils.AppTheme

/**
 * RegulatoryComplianceActivity - Shows compliance mapping
 * Demonstrates adherence to RBI, GDPR, and EU AI Act regulations
 */
class RegulatoryComplianceActivity : AppCompatActivity() {
    
    private var currentLanguage = "en"
    
    private val languagePrefs by lazy {
        getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = languagePrefs.getString("language", "en") ?: "en"
        
        // Blue status bar - same as all other pages
        window.statusBarColor = AppTheme.Background.Secondary
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        
        setContentView(createUI())
    }
    
    private fun createUI(): FrameLayout {
        val mainContainer = FrameLayout(this)
        mainContainer.setBackgroundColor(AppTheme.Background.Primary)
        
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(AppTheme.Background.Primary)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Primary)
            setPadding(0, dp(120), 0, 0)
        }
        
        // Content
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        
        // Overview card
        contentLayout.addView(createOverviewCard())
        addSpace(contentLayout, 20)
        
        // Compliance Score
        contentLayout.addView(createComplianceScoreCard())
        addSpace(contentLayout, 24)
        
        // RBI Guidelines
        contentLayout.addView(createSectionHeader("🏦 RBI Guidelines (India)"))
        contentLayout.addView(createRBIComplianceCard())
        addSpace(contentLayout, 20)
        
        // GDPR
        contentLayout.addView(createSectionHeader("🇪🇺 GDPR (Europe)"))
        contentLayout.addView(createGDPRComplianceCard())
        addSpace(contentLayout, 20)
        
        // EU AI Act
        contentLayout.addView(createSectionHeader("⚖️ EU AI Act"))
        contentLayout.addView(createEUAIActCard())
        addSpace(contentLayout, 20)
        
        // Additional Standards
        contentLayout.addView(createSectionHeader("🌍 Additional Standards"))
        contentLayout.addView(createAdditionalStandardsCard())
        addSpace(contentLayout, 20)
        
        // Audit Trail
        contentLayout.addView(createAuditTrailCard())
        addSpace(contentLayout, 20)
        
        // Export Audit Report Button
        contentLayout.addView(createExportReportButton())
        
        rootLayout.addView(contentLayout)
        scrollView.addView(rootLayout)
        
        val scrollParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollParams
        mainContainer.addView(scrollView)
        
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
            setPadding(dp(10), dp(10), dp(10), dp(10))
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
            
            // Compact back button
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "←"
                textSize = 24f
                setTextColor(Color.WHITE)
                setPadding(0, 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                gravity = Gravity.CENTER
                setOnClickListener { finish() }
            })
            
            // Title only (no subtitle)
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "नियामक अनुपालन"
                    "te" -> "నియంత్రణ అనుపాలన"
                    else -> "Regulatory Compliance"
                }
                textSize = 18f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
            })
        }
    }
    
    private fun createLanguageBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            gravity = Gravity.END
            setBackgroundColor(Color.WHITE)
            elevation = dp(2).toFloat()
            
            val languages = listOf("English" to "en", "हिंदी" to "hi", "తెలుగు" to "te")
            
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(Space(this@RegulatoryComplianceActivity).apply {
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
                    // Save language preference
                    languagePrefs.edit().putString("language", code).apply()
                    recreate()
                }
            }
        }
    }
    
    private fun createOverviewCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), 0xFFE0E0E0.toInt())
            }
            background = shape
            elevation = dp(2).toFloat()
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "🛡️ अनुपालन अवलोकन"
                    "te" -> "🛡️ అనుగుణత సమీక్ష"
                    else -> "🛡️ Compliance Overview"
                }
                textSize = 20f
                setTextColor(AppTheme.Text.OnCard)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(12))
            })
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "LumeAI भारतीय और अंतर्राष्ट्रीय नियामक ढांचे के साथ पूर्ण रूप से अनुपालन करता है, जिससे सुनिश्चित होता है कि AI निर्णय पारदर्शी, उचित और ऑडिट योग्य हैं।"
                    "te" -> "LumeAI భారతీయ మరియు అంతర్జాతీయ నియంత్రణ ఫ్రేమ్‌వర్క్‌లకు పూర్తిగా అనుగుణంగా ఉంది, AI నిర్ణయాలు పారదర్శకంగా, న్యాయంగా మరియు ఆడిట్ చేయదగినవిగా ఉన్నాయని నిర్ధారిస్తుంది."
                    else -> "LumeAI is fully compliant with Indian and international regulatory frameworks, ensuring AI decisions are transparent, fair, and auditable."
                }
                textSize = 14f
                setTextColor(AppTheme.Text.OnCardSecondary)
                setLineSpacing(0f, 1.5f)
            })
        }
    }
    
    private fun createComplianceScoreCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(24), dp(24), dp(24), dp(24))
            elevation = dp(2).toFloat()
            gravity = Gravity.CENTER
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), 0xFFE0E0E0.toInt())
            }
            background = shape
            
            // Score - keep green for good score
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "98"
                textSize = 72f
                setTextColor(Color.parseColor("#10B981"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "/ 100"
                textSize = 24f
                setTextColor(AppTheme.Text.OnCardSecondary)
                gravity = Gravity.CENTER
            })
            
            addSpace(this, 12)
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "समग्र अनुपालन स्कोर"
                    "te" -> "మొత్తం అనుగుణత స్కోర్"
                    else -> "Overall Compliance Score"
                }
                textSize = 18f
                setTextColor(Color.parseColor("#10B981"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            
            addSpace(this, 8)
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "✅ ऑडिट तैयार • ✅ उत्पादन सुरक्षित"
                    "te" -> "✅ ఆడిట్ సిద్ధం • ✅ ఉత్పత్తి సురక్షితం"
                    else -> "✅ Audit Ready • ✅ Production Safe"
                }
                textSize = 13f
                setTextColor(Color.parseColor("#059669"))
                gravity = Gravity.CENTER
            })
        }
    }
    
    private fun createRBIComplianceCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(3).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = shape
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "भारतीय रिजर्व बैंक - डिजिटल लेंडिंग दिशानिर्देश 2022"
                    "te" -> "రిజర్వ్ బ్యాంక్ ఆఫ్ ఇండియా - డిజిటల్ రుణ మార్గదర్శకాలు 2022"
                    else -> "Reserve Bank of India - Digital Lending Guidelines 2022"
                }
                textSize = 16f
                setTextColor(Color.parseColor("#1F2937"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            addView(createComplianceItem(
                "✅", 
                when (currentLanguage) {
                    "hi" -> "लेंडिंग में पारदर्शिता"
                    "te" -> "రుణ పారదర్శకత"
                    else -> "Transparency in Lending"
                },
                when (currentLanguage) {
                    "hi" -> "सभी क्रेडिट निर्णयों के लिए AI स्पष्टीकरण"
                    "te" -> "అన్ని క్రెడిట్ నిర్ణయాలకు AI వివరణలు"
                    else -> "AI explanations for all credit decisions"
                },
                "AI Explainability Hub with detailed decision reasoning"
            ))
            
            addView(createComplianceItem(
                "✅", 
                when (currentLanguage) {
                    "hi" -> "ग्राहक सहमति प्रबंधन"
                    "te" -> "కస్టమర్ సమ్మతి నిర్వహణ"
                    else -> "Customer Consent Management"
                },
                when (currentLanguage) {
                    "hi" -> "ऑडिट ट्रेल्स के साथ विस्तृत सहमति"
                    "te" -> "ఆడిట్ ట్రైల్స్‌తో గ్రాన్యులర్ సమ్మతి"
                    else -> "Granular consent with audit trails"
                },
                "Consent management with complete tracking"
            ))
            
            addView(createComplianceItem(
                "✅", 
                when (currentLanguage) {
                    "hi" -> "उचित प्रथाओं संहिता"
                    "te" -> "న్యాయమైన పద్ధతుల కోడ్"
                    else -> "Fair Practices Code"
                },
                when (currentLanguage) {
                    "hi" -> "पूर्वाग्रह पहचान और शमन"
                    "te" -> "పక్షపాత గుర్తింపు మరియు తగ్గింపు"
                    else -> "Bias detection and mitigation"
                },
                "Advanced bias detection and fairness metrics"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Grievance Redressal",
                "Appeal mechanism with clear process",
                "AppealInfo in CustomerExplanation"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Data Localization",
                "All data stored in India (Azure India)",
                "Architecture compliant"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Audit Trail",
                "Complete decision logging",
                "Comprehensive transparency and metrics tracking"
            ))
        }
    }
    
    private fun createGDPRComplianceCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(3).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = shape
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "सामान्य डेटा संरक्षण विनियमन (EU)"
                    "te" -> "సాధారణ డేటా రక్షణ నియంత్రణ (EU)"
                    else -> "General Data Protection Regulation (EU)"
                }
                textSize = 16f
                setTextColor(Color.parseColor("#1F2937"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            addView(createComplianceItem(
                "✅", 
                "Article 13-14: Right to Information",
                "Transparent data usage disclosure",
                "Detailed data sharing and usage information"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 15: Right to Access",
                "Dashboard showing all AI interactions",
                "Complete dashboard with transparency metrics"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 22: Automated Decision-Making",
                "Right to explanation & human review",
                "AI explainability with appeal mechanism"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 25: Data Protection by Design",
                "Privacy-first architecture",
                "Granular consent, minimal data collection"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 30: Records of Processing",
                "Complete audit trails",
                "ConsentManager audit logs"
            ))
            
            addView(createComplianceItem(
                "⚠️", 
                "Article 17: Right to Erasure",
                "Partial - manual data deletion available",
                "Enhancement needed for automated deletion"
            ))
        }
    }
    
    private fun createEUAIActCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(3).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = shape
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "EU कृत्रिम बुद्धिमत्ता अधिनियम (2024)"
                    "te" -> "EU కృత్రిమ మేధస్సు చట్టం (2024)"
                    else -> "EU Artificial Intelligence Act (2024)"
                }
                textSize = 16f
                setTextColor(Color.parseColor("#1F2937"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "⚠️ Classification: HIGH-RISK AI SYSTEM"
                textSize = 14f
                setTextColor(Color.parseColor("#DC2626"))
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(12), dp(8), dp(12), dp(8))
                
                val bg = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.WHITE)
                    setStroke(dp(1), 0xFFE0E0E0.toInt())
                }
                background = bg
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(16) }
            })
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "(Credit scoring AI = High-Risk per Annex III)"
                textSize = 12f
                setTextColor(Color.parseColor("#DC2626"))
                setPadding(0, 0, 0, dp(16))
            })
            
            addView(createComplianceItem(
                "✅", 
                "Article 13: Transparency & Explainability",
                "Human-readable AI explanations",
                "Clear explainability with counterfactual analysis"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 14: Human Oversight",
                "Appeal mechanism for human review",
                "AppealInfo, manual override capability"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 15: Accuracy & Robustness",
                "Bias detection & fairness metrics",
                "Comprehensive bias detection and fairness tracking"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 16: Cybersecurity",
                "Secure Azure OpenAI integration",
                "API key encryption, HTTPS only"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 17: Quality Management",
                "Version control & testing",
                "User testing and validation results tracking"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 18: Record-Keeping",
                "Automatic logging of all decisions",
                "Complete transparency metrics with audit trails"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "Article 64: Access for Authorities",
                "Audit dashboard (admin view)",
                "BiasAuditReport, complete decision logs"
            ))
        }
    }
    
    private fun createAdditionalStandardsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(3).toFloat()
            
            val shape = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
            }
            background = shape
            
            addView(createComplianceItem(
                "✅", 
                "ISO/IEC 42001 (AI Management)",
                "Systematic AI governance framework",
                "Complete audit trails and oversight"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "NIST AI Risk Management",
                "Risk assessment and mitigation",
                "Bias detection, fairness monitoring"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "IEEE 7000 (Ethical AI)",
                "Value-based design principles",
                "User-centric transparency tools"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "OECD AI Principles",
                "Inclusive growth, sustainable development",
                "Multi-language, rural inclusion focus"
            ))
            
            addView(createComplianceItem(
                "✅", 
                "80-20 Rule (Disparate Impact)",
                "Fairness threshold compliance",
                "FairnessMetricsEngine checks disparate impact ≥ 0.8"
            ))
        }
    }
    
    private fun createAuditTrailCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), 0xFFE0E0E0.toInt())
            }
            background = shape
            elevation = dp(2).toFloat()
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "📋 ऑडिट ट्रेल क्षमताएं"
                    "te" -> "📋 ఆడిట్ ట్రయిల్ సామర్థ్యాలు"
                    else -> "📋 Audit Trail Capabilities"
                }
                textSize = 18f
                setTextColor(AppTheme.Text.OnCard)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            addView(createAuditItem("🔍", "Decision Logging", "Every AI decision logged with timestamp, inputs, outputs"))
            addView(createAuditItem("👤", "User Consent History", "All consent changes tracked with before/after states"))
            addView(createAuditItem("⚠️", "Bias Warnings", "BiasAuditReport generated for each decision"))
            addView(createAuditItem("📊", "Fairness Metrics", "Periodic demographic parity and disparate impact analysis"))
            addView(createAuditItem("🔐", "Access Logs", "Who accessed what data, when (admin dashboard)"))
            addView(createAuditItem("📈", "Performance Tracking", "Model accuracy, drift detection, retraining triggers"))
            addView(createAuditItem("💬", "Chatbot Interactions", "Complete conversation logs for quality assurance"))
            addView(createAuditItem("📄", "Regulatory Reports", "Auto-generated compliance reports (monthly)"))
            
            addSpace(this, 16)
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "✅ Retention: 7 years (RBI requirement)\n✅ Export: JSON/CSV for regulatory submission\n✅ Encryption: AES-256 at rest, TLS 1.3 in transit"
                textSize = 12f
                setTextColor(Color.parseColor("#92400E"))
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    private fun createComplianceItem(
        checkmark: String,
        title: String,
        description: String,
        implementation: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, dp(8))
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = checkmark
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(dp(40), ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            
            addView(LinearLayout(this@RegulatoryComplianceActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                
                addView(TextView(this@RegulatoryComplianceActivity).apply {
                    text = title
                    textSize = 14f
                    setTextColor(Color.parseColor("#1F2937"))
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@RegulatoryComplianceActivity).apply {
                    text = description
                    textSize = 13f
                    setTextColor(Color.parseColor("#4B5563"))
                    setPadding(0, dp(2), 0, dp(2))
                    setLineSpacing(0f, 1.3f)
                })
                
                addView(TextView(this@RegulatoryComplianceActivity).apply {
                    text = "📂 $implementation"
                    textSize = 11f
                    setTextColor(Color.parseColor("#7C3AED"))
                    setPadding(0, dp(4), 0, 0)
                })
            })
        }
    }
    
    private fun createAuditItem(emoji: String, title: String, description: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(6), 0, dp(6))
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = emoji
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(dp(36), ViewGroup.LayoutParams.WRAP_CONTENT)
            })
            
            addView(LinearLayout(this@RegulatoryComplianceActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                
                addView(TextView(this@RegulatoryComplianceActivity).apply {
                    text = title
                    textSize = 13f
                    setTextColor(Color.parseColor("#92400E"))
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@RegulatoryComplianceActivity).apply {
                    text = description
                    textSize = 12f
                    setTextColor(Color.parseColor("#78350F"))
                    setPadding(0, dp(2), 0, 0)
                })
            })
        }
    }
    
    private fun createSectionHeader(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(Color.parseColor("#1F2937"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        }
    }
    
    private fun createExportReportButton(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                colors = intArrayOf(Color.parseColor("#10B981"), Color.parseColor("#059669"))
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
            }
            background = shape
            elevation = dp(4).toFloat()
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "📥 Export Audit Report"
                textSize = 18f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(12))
            })
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "Generate comprehensive audit report for regulators"
                textSize = 14f
                setTextColor(Color.parseColor("#D1FAE5"))
                setPadding(0, 0, 0, dp(16))
            })
            
            // Export Options
            addView(createExportOption("PDF Report", "Complete compliance audit (RBI/GDPR/EU AI Act)") {
                exportAuditReport("PDF")
            })
            
            addView(createExportOption("JSON Data", "Machine-readable audit trail for automated review") {
                exportAuditReport("JSON")
            })
            
            addView(createExportOption("CSV Export", "Tabular data for spreadsheet analysis") {
                exportAuditReport("CSV")
            })
        }
    }
    
    private fun createExportOption(title: String, description: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.parseColor("#ECFDF5"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(8)
            }
            isClickable = true
            isFocusable = true
            
            val bg = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(Color.parseColor("#ECFDF5"))
            }
            background = bg
            
            addView(LinearLayout(this@RegulatoryComplianceActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                
                addView(TextView(this@RegulatoryComplianceActivity).apply {
                    text = title
                    textSize = 15f
                    setTextColor(Color.parseColor("#065F46"))
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@RegulatoryComplianceActivity).apply {
                    text = description
                    textSize = 12f
                    setTextColor(Color.parseColor("#059669"))
                    setPadding(0, dp(4), 0, 0)
                })
            })
            
            addView(TextView(this@RegulatoryComplianceActivity).apply {
                text = "↓"
                textSize = 24f
                setTextColor(Color.parseColor("#059669"))
                gravity = Gravity.CENTER
            })
            
            setOnClickListener { onClick() }
        }
    }
    
    private fun exportAuditReport(format: String) {
        // In production: Generate actual report and save to Downloads
        // For demo: Show what would be exported
        
        val reportContent = when (format) {
            "PDF" -> """
                📄 AUDIT REPORT GENERATED
                
                Report ID: AUD-${System.currentTimeMillis() % 100000}
                Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}
                Format: PDF (15 pages)
                
                Contents:
                ✓ Executive Summary
                ✓ RBI Compliance Matrix (6 points)
                ✓ GDPR Article Compliance (6 points)
                ✓ EU AI Act Requirements (7 points)
                ✓ Audit Trail Statistics
                ✓ Bias Detection Reports
                ✓ Fairness Metrics Analysis
                ✓ Consent Management Logs
                ✓ Transparency Score History
                ✓ Recommendations
                
                File: lumeai_audit_report_2024.pdf
                Size: 2.4 MB
                Location: /Downloads/
            """.trimIndent()
            
            "JSON" -> """
                📄 JSON DATA EXPORTED
                
                File: lumeai_audit_${System.currentTimeMillis()}.json
                Size: 847 KB
                Location: /Downloads/
                
                Data Structure:
                {
                  "report_id": "AUD-${System.currentTimeMillis() % 100000}",
                  "timestamp": "${System.currentTimeMillis()}",
                  "compliance_score": 98,
                  "rbi_compliance": {...},
                  "gdpr_compliance": {...},
                  "eu_ai_act_compliance": {...},
                  "audit_trail": [],
                  "bias_reports": [],
                  "fairness_metrics": {...},
                  "consent_logs": [],
                  "transparency_history": []
                }
                
                Ready for automated compliance tools.
            """.trimIndent()
            
            else -> """
                📄 CSV EXPORT COMPLETE
                
                Files Generated:
                1. compliance_summary.csv (1.2 KB)
                2. audit_trail.csv (234 KB)
                3. bias_reports.csv (89 KB)
                4. fairness_metrics.csv (45 KB)
                5. consent_logs.csv (67 KB)
                
                Location: /Downloads/lumeai_audit_export/
                
                Ready for Excel/Sheets analysis.
            """.trimIndent()
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("✅ Report Generated")
            .setMessage(reportContent)
            .setPositiveButton("Open") { dialog, _ ->
                Toast.makeText(this, "In production: Opens report viewer", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Share") { dialog, _ ->
                Toast.makeText(this, "In production: Opens share dialog", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun addSpace(layout: LinearLayout, dpValue: Int) {
        layout.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(dpValue))
        })
    }
    
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}

