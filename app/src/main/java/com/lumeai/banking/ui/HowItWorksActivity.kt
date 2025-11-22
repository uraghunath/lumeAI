package com.lumeai.banking.ui

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lumeai.banking.utils.AppTheme

/**
 * HowItWorksActivity - Simple explanation of how LumeAI works
 * NOW WITH MULTILINGUAL SUPPORT
 */
class HowItWorksActivity : AppCompatActivity() {

    private var currentLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load saved language preference
        currentLanguage = getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        
        // CONSISTENT: Blue status bar like other feature pages
        window.statusBarColor = AppTheme.Primary.HeaderBlue
        supportActionBar?.hide()
        
        setContentView(createUI())
    }
    
    private fun createUI(): ScrollView {
        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(AppTheme.Background.Primary)
        
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(20))
        }
        
        // Header with blue gradient like other pages
        contentLayout.addView(createHeader())
        contentLayout.addView(createLanguageBar())
        
        // Main content with padding
        val mainContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), 0)
        }
        
        // Main Message
        mainContent.addView(createMainMessage())
        addSpace(mainContent, 30)
        
        // Step 1
        mainContent.addView(createStepCard(
            1,
            "🏦",
            when (currentLanguage) {
                "hi" -> "बैंक निर्णय लेता है"
                "te" -> "బ్యాంక నిర్ణయం తీసుకుంటుంది"
                else -> "Bank Makes Decision"
            },
            when (currentLanguage) {
                "hi" -> "जब बैंक आपके ऋण को स्वीकृत या अस्वीकार करता है, तो वे निर्णय लेने के लिए AI का उपयोग करते हैं।"
                "te" -> "బ్యాంక మీ రుణాన్ని ఆమోదించినప్పుడు లేదా తిరస్కరించినప్పుడు, వారు నిర్ణయం తీసుకోవడానికి AIని ఉపయోగిస్తారు।"
                else -> "When a bank approves or denies your loan, they use AI to make the decision."
            }
        ))
        addSpace(mainContent, 16)
        
        // Step 2
        mainContent.addView(createStepCard(
            2,
            "🔔",
            when (currentLanguage) {
                "hi" -> "तुरंत अलर्ट मिलता है"
                "te" -> "తక్షణ హెచ్చరిక పొందండి"
                else -> "You Get Instant Alert"
            },
            when (currentLanguage) {
                "hi" -> "LumeAI तुरंत आपको बैंक के निर्णय के बारे में वास्तविक समय में सूचित करता है।"
                "te" -> "LumeAI వెంటనే బ్యాంక్ నిర్ణయం గురించి మీకు రియల్ టైమ్‌లో తెలియజేస్తుంది।"
                else -> "LumeAI immediately notifies you about the bank's decision in real-time."
            }
        ))
        addSpace(mainContent, 16)
        
        // Step 3
        mainContent.addView(createStepCard(
            3,
            "💡",
            when (currentLanguage) {
                "hi" -> "AI सरल शब्दों में समझाता है"
                "te" -> "AI సరళ పదాలలో వివరిస్తుంది"
                else -> "AI Explains in Simple Words"
            },
            when (currentLanguage) {
                "hi" -> "हमारा AI जटिल बैंकिंग निर्णयों को सरल भाषा में अनुवाद करता है जिसे आप समझते हैं।"
                "te" -> "మా AI సంక్లిష్ట బ్యాంకింగ్ నిర్ణయాలను మీరు అర్థం చేసుకునే సరళమైన భాషలోకి అనువదిస్తుంది।"
                else -> "Our AI translates complex banking decisions into simple language you understand."
            }
        ))
        addSpace(mainContent, 16)
        
        // Step 4
        mainContent.addView(createStepCard(
            4,
            "🎯",
            when (currentLanguage) {
                "hi" -> "कार्रवाई योग्य कदम प्राप्त करें"
                "te" -> "చర్య తీసుకోదగిన అంచనాలను పొందండి"
                else -> "Get Actionable Steps"
            },
            when (currentLanguage) {
                "hi" -> "हम आपको बताते हैं कि अगली बार स्वीकृत होने के लिए क्या सुधार करना है।"
                "te" -> "తదుపరిసారి ఆమోదం పొందడానికి ఏమి మెరుగుపరచాలో మేము మీకు చూపిस్తాము।"
                else -> "We show you exactly what to improve to get approved next time."
            }
        ))
        addSpace(mainContent, 16)
        
        // Step 5
        mainContent.addView(createStepCard(
            5,
            "🤖",
            when (currentLanguage) {
                "hi" -> "AI सहायक 24/7 मदद करता है"
                "te" -> "AI సహాయకుడు 24/7 సహాయం చేస్తుంది"
                else -> "AI Assistant Helps 24/7"
            },
            when (currentLanguage) {
                "hi" -> "हमारा चैटबॉट AI संदेशों को समझने, प्रश्नों के उत्तर देने और सुविधाओं के माध्यम से मार्गदर्शन करने के लिए हमेशा उपलब्ध है।"
                "te" -> "మా చాట్‌బాట్ AI సందేశాలను డీకోడ్ చేయడానికి, ప్రశ్నలకు సమాధానం ఇవ్వడానికి మరియు ఫీచర్ల ద్వారా మార్గనిర్దేశం చేయడానికి ఎల్లప్పుడూ అందుబాటులో ఉంది।"
                else -> "Our chatbot is always available to decode AI messages, answer questions, and guide you through features."
            }
        ))
        addSpace(mainContent, 16)
        
        // Step 6
        mainContent.addView(createStepCard(
            6,
            "⚖️",
            when (currentLanguage) {
                "hi" -> "निष्पक्षता निगरानी"
                "te" -> "న్యాయ పర్యవేక్షణ"
                else -> "Fairness Monitoring"
            },
            when (currentLanguage) {
                "hi" -> "हम स्वचालित रूप से हर निर्णय में पूर्वाग्रह का पता लगाते हैं ताकि यह सुनिश्चित किया जा सके कि आपके साथ निष्पक्ष और पारदर्शी व्यवहार किया जाए।"
                "te" -> "మేము ప్రతి నిర్ణయంలో పక్షపాతాన్ని స్వయంచాలకంగా గుర్తిస్తాము, మీరు న్యాయంగా మరియు పారదర్శకంగా వ్యవహరించబడుతున్నారని నిర్ధారించుకోవడానికి।"
                else -> "We automatically detect bias in every decision to ensure you're treated fairly and transparently."
            }
        ))
        addSpace(mainContent, 30)
        
        // Trust Badges
        mainContent.addView(createTrustSection())
        addSpace(mainContent, 30)
        
        contentLayout.addView(mainContent)
        scrollView.addView(contentLayout)
        return scrollView
    }
    
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dp(20), dp(16), dp(20), dp(20))
            gravity = Gravity.CENTER_VERTICAL
            
            // Back button
            addView(TextView(this@HowItWorksActivity).apply {
                text = "←"
                textSize = 28f
                setTextColor(Color.WHITE)
                setPadding(0, 0, dp(15), 0)
                setOnClickListener { finish() }
            })
            
            // Title - MULTILINGUAL
            addView(TextView(this@HowItWorksActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "LumeAI कैसे काम करता है"
                    "te" -> "LumeAI ఎలా పనిచేస్తుంది"
                    else -> "How LumeAI Works"
                }
                textSize = 22f
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
            setPadding(dp(20), dp(12), dp(20), dp(8))  // REDUCED: Smaller padding
            gravity = Gravity.END
            setBackgroundColor(Color.WHITE)
            
            val languages = listOf("English" to "en", "हिंदी" to "hi", "తెలుగు" to "te")
            languages.forEach { (name, code) ->
                addView(createLanguageButton(name, code))
                if (code != "te") {
                    addView(View(this@HowItWorksActivity).apply {
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
                    setColor(AppTheme.Text.OnCard)  // Dark blue when selected (matching theme)
                } else {
                    setColor(Color.WHITE)
                    setStroke(dp(1), AppTheme.Text.OnCardSecondary)  // Blue border (matching theme)
                }
            }
            background = shape
            setTextColor(if (isSelected) Color.WHITE else AppTheme.Text.OnCard)
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            
            setOnClickListener {
                if (currentLanguage != code) {
                    currentLanguage = code
                    // Save language preference
                    getSharedPreferences("LumeAILanguage", MODE_PRIVATE)
                        .edit()
                        .putString("language", code)
                        .apply()
                    recreate()  // Recreate activity to show new language
                }
            }
        }
    }
    
    private fun createMainMessage(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.StatusBg.Info)  // Light blue background (matches theme)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(AppTheme.CornerRadius).toFloat()
                setColor(AppTheme.StatusBg.Info)  // Light blue (matches theme)
            }
            background = shape
            elevation = dp(4).toFloat()
            
            addView(TextView(this@HowItWorksActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "🤝 हम आपके साथ हैं"
                    "te" -> "🤝 మేము మీ వైపు ఉన్నాము"
                    else -> "🤝 We're On Your Side"
                }
                textSize = 20f
                setTextColor(AppTheme.Text.OnCard)  // Dark blue text (matches theme)
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            
            addView(TextView(this@HowItWorksActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "बैंक अब आपसे निर्णय छिपा नहीं सकते। LumeAI सुनिश्चित करता है कि आप हर निर्णय को समझें और बेहतर बनने की शक्ति देता है।"
                    "te" -> "బ్యాంకులు ఇకపై మీ నుండి నిర్ణయాలను దాచలేవు. LumeAI ప్రతి నిర్ణయాన్ని మీరు అర్థం చేసుకోవడం మరియు మెరుగుపడే శక్తిని ఇస్తుంది."
                    else -> "Banks can't hide decisions from you anymore. LumeAI ensures you understand every decision and gives you the power to improve."
                }
                textSize = 15f
                setTextColor(AppTheme.Text.OnCard)
                gravity = Gravity.CENTER
                setPadding(0, dp(12), 0, 0)
                setLineSpacing(0f, 1.4f)
            })
        }
    }
    
    private fun createStepCard(
        stepNumber: Int,
        icon: String,
        title: String,
        description: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(AppTheme.Cards.Surface)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(4).toFloat()
            
            val shape = GradientDrawable().apply {
                cornerRadius = dp(AppTheme.CornerRadius).toFloat()
                setColor(AppTheme.Cards.Surface)
            }
            background = shape
            
            // Step number circle
            addView(LinearLayout(this@HowItWorksActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, 0, dp(16), 0)
                
                background = GradientDrawable().apply {
                    setShape(GradientDrawable.OVAL)
                    setColor(AppTheme.Text.OnCard)
                }
                
                layoutParams = LinearLayout.LayoutParams(dp(50), dp(50))
                
                addView(TextView(this@HowItWorksActivity).apply {
                    text = stepNumber.toString()
                    textSize = 20f
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                })
            })
            
            // Content
            addView(LinearLayout(this@HowItWorksActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                
                // Icon + Title
                addView(LinearLayout(this@HowItWorksActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    
                    addView(TextView(this@HowItWorksActivity).apply {
                        text = icon
                        textSize = 24f
                        setPadding(0, 0, dp(8), 0)
                    })
                    
                    addView(TextView(this@HowItWorksActivity).apply {
                        text = title
                        textSize = 18f
                        setTextColor(AppTheme.Text.OnCard)
                        setTypeface(null, Typeface.BOLD)
                    })
                })
                
                // Description
                addView(TextView(this@HowItWorksActivity).apply {
                    text = description
                    textSize = 14f
                    setTextColor(AppTheme.Text.OnCardSecondary)
                    setPadding(0, dp(8), 0, 0)
                    setLineSpacing(0f, 1.3f)
                })
            })
        }
    }
    
    private fun createTrustSection(): LinearLayout {
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
            
            addView(TextView(this@HowItWorksActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "🛡️ LumeAI पर भरोसा क्यों करें?"
                    "te" -> "🛡️ LumeAIని ఎందుకు విశ్వసించాలి?"
                    else -> "🛡️ Why Trust LumeAI?"
                }
                textSize = 20f
                setTextColor(AppTheme.Text.OnCard)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(16))
            })
            
            addView(createTrustBadge(
                "✓", 
                when (currentLanguage) {
                    "hi" -> "100% पारदर्शी"
                    "te" -> "100% పారదర్శక"
                    else -> "100% Transparent"
                },
                when (currentLanguage) {
                    "hi" -> "कोई छिपा एल्गोरिथ्म नहीं"
                    "te" -> "దాచిన అల్గారిథమ్‌లు లేవు"
                    else -> "No hidden algorithms"
                }
            ))
            addView(createTrustBadge(
                "✓",
                when (currentLanguage) {
                    "hi" -> "आपकी गोपनीयता प्रथम"
                    "te" -> "మీ గోప్యత మొదట"
                    else -> "Your Privacy First"
                },
                when (currentLanguage) {
                    "hi" -> "डेटा एन्क्रिप्टेड और सुरक्षित"
                    "te" -> "డేటా ఎన్క్రిప్ట్ చేయబడింది & భద్రం"
                    else -> "Data encrypted & secure"
                }
            ))
            addView(createTrustBadge(
                "✓",
                when (currentLanguage) {
                    "hi" -> "हमेशा मुफ्त"
                    "te" -> "ఎల్లప్పుడూ ఉచితం"
                    else -> "Always Free"
                },
                when (currentLanguage) {
                    "hi" -> "कोई छिपा शुल्क नहीं"
                    "te" -> "దాచిన ఛార్జీలు లేవు"
                    else -> "No hidden charges"
                }
            ))
            addView(createTrustBadge(
                "✓",
                when (currentLanguage) {
                    "hi" -> "आपके लिए बनाया गया"
                    "te" -> "మీ కోసం నిర్మించబడింది"
                    else -> "Built for You"
                },
                when (currentLanguage) {
                    "hi" -> "बैंकों के लिए नहीं"
                    "te" -> "బ్యాంకుల కోసం కాదు"
                    else -> "Not for banks"
                }
            ))
        }
    }
    
    private fun createTrustBadge(icon: String, title: String, subtitle: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            
            addView(TextView(this@HowItWorksActivity).apply {
                text = icon
                textSize = 20f
                setTextColor(AppTheme.Status.Success)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, dp(12), 0)
            })
            
            addView(LinearLayout(this@HowItWorksActivity).apply {
                orientation = LinearLayout.VERTICAL
                
                addView(TextView(this@HowItWorksActivity).apply {
                    text = title
                    textSize = 16f
                    setTextColor(AppTheme.Text.OnCard)
                    setTypeface(null, Typeface.BOLD)
                })
                
                addView(TextView(this@HowItWorksActivity).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(AppTheme.Text.OnCardSecondary)
                    setPadding(0, dp(4), 0, 0)
                })
            })
        }
    }
    
    private fun addSpace(parent: LinearLayout, dp: Int) {
        parent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(dp)
            )
        })
    }
    
    private fun dp(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

