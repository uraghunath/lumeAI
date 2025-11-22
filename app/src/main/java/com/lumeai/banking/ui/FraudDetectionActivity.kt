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
import com.lumeai.banking.models.FraudAlert
import com.lumeai.banking.utils.LanguageHelper
import com.lumeai.banking.utils.AppTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🚨 FraudDetectionActivity - Real-Time Fraud Monitoring
 * AI-powered fraud detection with transparent explanations
 */
class FraudDetectionActivity : AppCompatActivity() {
    
    private lateinit var contentLayout: LinearLayout
    private lateinit var filterSpinner: Spinner
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var statsCard: LinearLayout
    private var allAlerts: List<FraudAlert> = emptyList()
    private var currentLanguage: String = "en"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Deep red for security theme
        window.statusBarColor = AppTheme.Primary.HeaderBlue
        supportActionBar?.hide()
        
        // Get language preference
        currentLanguage = getSharedPreferences("LumeAI", MODE_PRIVATE)
            .getString("language", "en") ?: "en"
        
        setContentView(createUI())
        loadFraudAlerts()
    }
    
    override fun onResume() {
        super.onResume()
        loadFraudAlerts()
    }
    
    private fun createUI(): ScrollView {
        val scrollView = ScrollView(this)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F7FA.toInt())
        }
        
        // Header
        rootLayout.addView(createHeader())
        
        // Stats Card
        statsCard = createStatsCard()
        rootLayout.addView(statsCard)
        
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
        
        // Content Layout for fraud alerts
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
        }
        rootLayout.addView(contentLayout)
        
        scrollView.addView(rootLayout)
        return scrollView
    }
    
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(20))
            
            // Header row with back button and title inline
            addView(LinearLayout(this@FraudDetectionActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                // Modern back button - icon only
                addView(TextView(this@FraudDetectionActivity).apply {
                    text = "←"
                    textSize = 28f
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                    layoutParams = LinearLayout.LayoutParams(dpToPx(48), dpToPx(48))
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    
                    setOnClickListener { finish() }
                })
                
                // Title
                addView(TextView(this@FraudDetectionActivity).apply {
                    text = when (currentLanguage) {
                        "hi" -> "धोखाधड़ी का पता लगाना"
                        "te" -> "మోసం గుర్తింపు"
                        else -> "Fraud Detection"
                    }
                    textSize = 24f
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                })
            })
            
            // Subtitle removed for cleaner UI
            /*addView(TextView(this@FraudDetectionActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "एआई-संचालित सुरक्षा निगरानी"
                    "te" -> "AI-శక్తి భద్రత పర్యవేక్షణ"
                    else -> "AI-powered security monitoring"
                }
                textSize = 13f
                setTextColor(0xFFFEE2E2.toInt())
                setPadding(dpToPx(48), dpToPx(2), 0, 0)
            })*/
        }
    }
    
    private fun createStatsCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16))
            elevation = dpToPx(2).toFloat()
            
            addView(TextView(this@FraudDetectionActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "📊 सुरक्षा अवलोकन"
                    "te" -> "📊 భద్రత అవలోకనం"
                    else -> "📊 Security Overview"
                }
                textSize = 16f
                setTextColor(0xFF0A0A0A.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            // Stats will be populated dynamically
            addView(LinearLayout(this@FraudDetectionActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(12)
                }
                
                // Placeholder for now - will be populated in loadFraudAlerts()
                addView(TextView(this@FraudDetectionActivity).apply {
                    text = "Loading..."
                    textSize = 13f
                    setTextColor(0xFF666666.toInt())
                })
            })
        }
    }
    
    private fun createFilterSection(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16))
            gravity = Gravity.CENTER_VERTICAL
            elevation = dpToPx(2).toFloat()
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dpToPx(2)
            layoutParams = params
            
            addView(TextView(this@FraudDetectionActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "जोखिम स्तर: "
                    "te" -> "ప్రమాద స్థాయి: "
                    else -> "Risk Level: "
                }
                textSize = 14f
                setTextColor(0xFF666666.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            filterSpinner = Spinner(this@FraudDetectionActivity).apply {
                val filterOptions = arrayOf(
                    when (currentLanguage) {
                        "hi" -> "सभी"
                        "te" -> "అన్ని"
                        else -> "All"
                    },
                    when (currentLanguage) {
                        "hi" -> "गंभीर"
                        "te" -> "క్రిటికల్"
                        else -> "Critical"
                    },
                    when (currentLanguage) {
                        "hi" -> "उच्च"
                        "te" -> "అధిక"
                        else -> "High"
                    },
                    when (currentLanguage) {
                        "hi" -> "मध्यम"
                        "te" -> "మధ్యస్థ"
                        else -> "Medium"
                    },
                    when (currentLanguage) {
                        "hi" -> "कम"
                        "te" -> "తక్కువ"
                        else -> "Low"
                    }
                )
                adapter = ArrayAdapter(this@FraudDetectionActivity, android.R.layout.simple_spinner_dropdown_item, filterOptions)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        applyFilter(position)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            addView(filterSpinner)
        }
    }
    
    private fun loadFraudAlerts() {
        loadingIndicator.visibility = View.VISIBLE
        contentLayout.removeAllViews()
        
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences("LumeAI", MODE_PRIVATE)
                val customerId = prefs.getString("customerId", "") ?: ""
                
                if (customerId.isEmpty()) {
                    showEmptyState("No customer ID found")
                    return@launch
                }
                
                // Fetch fraud alerts from Firebase
                val database = FirebaseDatabase.getInstance()
                val alertsRef = database.getReference("fraudAlerts").orderByChild("customerId").equalTo(customerId)
                
                alertsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val alerts = mutableListOf<FraudAlert>()
                        for (child in snapshot.children) {
                            val alert = child.getValue(FraudAlert::class.java)
                            if (alert != null) {
                                alerts.add(alert)
                            }
                        }
                        
                        allAlerts = alerts.sortedByDescending { it.timestamp }
                        loadingIndicator.visibility = View.GONE
                        
                        if (allAlerts.isEmpty()) {
                            showEmptyState(when (currentLanguage) {
                                "hi" -> "✅ कोई धोखाधड़ी अलर्ट नहीं मिला\n\nआपका खाता सुरक्षित है!"
                                "te" -> "✅ మోసం హెచ్చరికలు లేవు\n\nమీ ఖాతా సురక్షితంగా ఉంది!"
                                else -> "✅ No fraud alerts found\n\nYour account is secure!"
                            })
                        } else {
                            updateStats()
                            displayAlerts(allAlerts)
                        }
                    }
                    
                    override fun onCancelled(error: DatabaseError) {
                        loadingIndicator.visibility = View.GONE
                        showEmptyState("Error: ${error.message}")
                    }
                })
                
            } catch (e: Exception) {
                loadingIndicator.visibility = View.GONE
                showEmptyState("Error loading alerts: ${e.message}")
            }
        }
    }
    
    private fun updateStats() {
        val totalAlerts = allAlerts.size
        val criticalCount = allAlerts.count { it.riskLevel == "CRITICAL" }
        val highCount = allAlerts.count { it.riskLevel == "HIGH" }
        val blockedCount = allAlerts.count { it.status == "BLOCKED" }
        val flaggedCount = allAlerts.count { it.status == "FLAGGED" }
        
        // Update stats card
        statsCard.removeAllViews()
        statsCard.addView(TextView(this).apply {
            text = when (currentLanguage) {
                "hi" -> "📊 सुरक्षा अवलोकन"
                "te" -> "📊 భద్రత అవలోకనం"
                else -> "📊 Security Overview"
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
                totalAlerts.toString(),
                when (currentLanguage) {
                    "hi" -> "कुल अलर्ट"
                    "te" -> "మొత్తం హెచ్చరికలు"
                    else -> "Total Alerts"
                },
                1f
            ))
            
            addView(createStatPill(
                (criticalCount + highCount).toString(),
                when (currentLanguage) {
                    "hi" -> "उच्च जोखिम"
                    "te" -> "అధిక ప్రమాదం"
                    else -> "High Risk"
                },
                1f
            ))
            
            addView(createStatPill(
                blockedCount.toString(),
                when (currentLanguage) {
                    "hi" -> "अवरोधित"
                    "te" -> "బ్లాక్ చేయబడింది"
                    else -> "Blocked"
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
            
            addView(TextView(this@FraudDetectionActivity).apply {
                text = number
                textSize = 20f
                setTextColor(0xFF1E40AF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            
            addView(TextView(this@FraudDetectionActivity).apply {
                text = label
                textSize = 11f
                setTextColor(0xFF64748B.toInt())
            })
        }
    }
    
    private fun displayAlerts(alerts: List<FraudAlert>) {
        contentLayout.removeAllViews()
        
        if (alerts.isEmpty()) {
            showEmptyState(when (currentLanguage) {
                "hi" -> "✅ कोई धोखाधड़ी अलर्ट नहीं मिला"
                "te" -> "✅ మోసం హెచ్చరికలు లేవు"
                else -> "✅ No fraud alerts found"
            })
            return
        }
        
        alerts.forEach { alert ->
            contentLayout.addView(createAlertCard(alert))
        }
    }
    
    private fun createAlertCard(alert: FraudAlert): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            elevation = dpToPx(4).toFloat()
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dpToPx(12)
            layoutParams = params
            
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(12).toFloat()
            shape.setColor(0xFFFFFFFF.toInt())
            background = shape
            
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = resources.getDrawable(outValue.resourceId, null)
            
            setOnClickListener {
                showAlertDetails(alert)
            }
            
            // Risk badge and transaction type
            addView(LinearLayout(this@FraudDetectionActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                
                // Risk badge
                addView(createRiskBadge(alert.riskLevel))
                
                // Transaction type
                addView(TextView(this@FraudDetectionActivity).apply {
                    text = formatTransactionType(alert.transactionType)
                    textSize = 16f
                    setTextColor(0xFF0A0A0A.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setPadding(dpToPx(12), 0, 0, 0)
                })
            })
            
            // Amount
            addView(TextView(this@FraudDetectionActivity).apply {
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                text = "₹${formatter.format(alert.amount).replace("₹", "")}"
                textSize = 24f
                setTextColor(0xFFDC2626.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dpToPx(8), 0, dpToPx(4))
            })
            
            // Merchant/Location
            if (alert.merchantName.isNotEmpty()) {
                addView(TextView(this@FraudDetectionActivity).apply {
                    text = "🏪 ${alert.merchantName}"
                    textSize = 14f
                    setTextColor(0xFF666666.toInt())
                    setPadding(0, dpToPx(4), 0, dpToPx(2))
                })
            }
            
            if (alert.location.isNotEmpty()) {
                addView(TextView(this@FraudDetectionActivity).apply {
                    text = "📍 ${alert.location}"
                    textSize = 13f
                    setTextColor(0xFF999999.toInt())
                    setPadding(0, dpToPx(2), 0, dpToPx(8))
                })
            }
            
            // Risk reason (short)
            addView(TextView(this@FraudDetectionActivity).apply {
                val reason = when (currentLanguage) {
                    "hi" -> alert.reasonHindi
                    "te" -> alert.reasonTelugu
                    else -> alert.reasonEnglish
                }
                text = "⚠️ ${reason.take(100)}${if (reason.length > 100) "..." else ""}"
                textSize = 13f
                setTextColor(0xFFDC2626.toInt())
                setPadding(0, dpToPx(8), 0, dpToPx(8))
            })
            
            // Timestamp
            addView(TextView(this@FraudDetectionActivity).apply {
                val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                text = sdf.format(Date(alert.timestamp))
                textSize = 12f
                setTextColor(0xFF999999.toInt())
            })
            
            // Tap hint
            addView(TextView(this@FraudDetectionActivity).apply {
                text = when (currentLanguage) {
                    "hi" -> "👆 विवरण और कार्रवाई के लिए टैप करें"
                    "te" -> "👆 వివరాలు మరియు చర్య కోసం నొక్కండి"
                    else -> "👆 Tap for details and actions"
                }
                textSize = 13f
                setTextColor(0xFF1976D2.toInt())
                gravity = Gravity.CENTER
                setPadding(0, dpToPx(8), 0, 0)
            })
        }
    }
    
    private fun createRiskBadge(riskLevel: String): TextView {
        val (color, bgColor, text) = when (riskLevel) {
            "CRITICAL" -> Triple(0xFFDC2626.toInt(), 0xFFFEE2E2.toInt(), "⚠️ CRITICAL")
            "HIGH" -> Triple(0xFFEA580C.toInt(), 0xFFFFEDD5.toInt(), "🔴 HIGH")
            "MEDIUM" -> Triple(0xFFCA8A04.toInt(), 0xFFFEF3C7.toInt(), "🟡 MEDIUM")
            "LOW" -> Triple(0xFF16A34A.toInt(), 0xFFDCFCE7.toInt(), "🟢 LOW")
            else -> Triple(0xFF64748B.toInt(), 0xFFF1F5F9.toInt(), "❓ UNKNOWN")
        }
        
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(color)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(bgColor)
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            
            val shape = android.graphics.drawable.GradientDrawable()
            shape.cornerRadius = dpToPx(6).toFloat()
            shape.setColor(bgColor)
            background = shape
        }
    }
    
    private fun formatTransactionType(type: String): String {
        return when (type.uppercase()) {
            "TRANSFER" -> "💸 Bank Transfer"
            "PAYMENT" -> "💳 Payment"
            "WITHDRAWAL" -> "🏧 ATM Withdrawal"
            "PURCHASE" -> "🛒 Purchase"
            else -> type
        }
    }
    
    private fun showAlertDetails(alert: FraudAlert) {
        val reason = when (currentLanguage) {
            "hi" -> alert.reasonHindi
            "te" -> alert.reasonTelugu
            else -> alert.reasonEnglish
        }
        
        val recommendation = when (currentLanguage) {
            "hi" -> alert.recommendationHindi
            "te" -> alert.recommendationTelugu
            else -> alert.recommendationEnglish
        }
        
        val message = buildString {
            append("🚨 ${formatTransactionType(alert.transactionType)}\n\n")
            append("Amount: ₹${NumberFormat.getInstance().format(alert.amount)}\n")
            if (alert.merchantName.isNotEmpty()) append("Merchant: ${alert.merchantName}\n")
            if (alert.location.isNotEmpty()) append("Location: ${alert.location}\n")
            append("Risk Score: ${alert.riskScore.toInt()}/100\n\n")
            append("🤖 AI Analysis:\n$reason\n\n")
            append("💡 Recommendation:\n$recommendation\n\n")
            
            append("Factors Detected:\n")
            if (alert.unusualAmount) append("• Unusual amount\n")
            if (alert.unusualLocation) append("• Unusual location\n")
            if (alert.unusualTime) append("• Unusual time\n")
            if (alert.newMerchant) append("• New merchant\n")
            if (alert.multipleAttempts) append("• Multiple attempts\n")
            if (alert.deviceMismatch) append("• Device mismatch\n")
        }
        
        AlertDialog.Builder(this)
            .setTitle(when (currentLanguage) {
                "hi" -> "🚨 धोखाधड़ी अलर्ट विवरण"
                "te" -> "🚨 మోసం హెచ్చరిక వివరాలు"
                else -> "🚨 Fraud Alert Details"
            })
            .setMessage(message)
            .setPositiveButton(when (currentLanguage) {
                "hi" -> "✅ यह मैं था"
                "te" -> "✅ ఇది నేనే"
                else -> "✅ This was me"
            }) { dialog, _ ->
                confirmTransaction(alert)
                dialog.dismiss()
            }
            .setNegativeButton(when (currentLanguage) {
                "hi" -> "🚫 धोखाधड़ी की रिपोर्ट करें"
                "te" -> "🚫 మోసం నివేదించండి"
                else -> "🚫 Report Fraud"
            }) { dialog, _ ->
                reportFraud(alert)
                dialog.dismiss()
            }
            .setNeutralButton(when (currentLanguage) {
                "hi" -> "बंद करें"
                "te" -> "మూసివేయండి"
                else -> "Close"
            }, null)
            .show()
    }
    
    private fun confirmTransaction(alert: FraudAlert) {
        // Update Firebase
        val database = FirebaseDatabase.getInstance()
        val alertRef = database.getReference("fraudAlerts/${alert.id}")
        alertRef.child("userConfirmed").setValue(true)
        alertRef.child("status").setValue("APPROVED")
        
        Toast.makeText(this, when (currentLanguage) {
            "hi" -> "✅ लेनदेन की पुष्टि की गई"
            "te" -> "✅ లావాదేవీ ధృవీకరించబడింది"
            else -> "✅ Transaction confirmed"
        }, Toast.LENGTH_SHORT).show()
        
        loadFraudAlerts() // Refresh
    }
    
    private fun reportFraud(alert: FraudAlert) {
        // Update Firebase
        val database = FirebaseDatabase.getInstance()
        val alertRef = database.getReference("fraudAlerts/${alert.id}")
        alertRef.child("userReportedFraud").setValue(true)
        alertRef.child("status").setValue("BLOCKED")
        
        Toast.makeText(this, when (currentLanguage) {
            "hi" -> "🚫 धोखाधड़ी की रिपोर्ट की गई। आपका खाता सुरक्षित है।"
            "te" -> "🚫 మోసం నివేదించబడింది. మీ ఖాతా సురక్షితంగా ఉంది."
            else -> "🚫 Fraud reported. Your account is secure."
        }, Toast.LENGTH_LONG).show()
        
        loadFraudAlerts() // Refresh
    }
    
    private fun applyFilter(filterPosition: Int) {
        val filtered = when (filterPosition) {
            0 -> allAlerts // All
            1 -> allAlerts.filter { it.riskLevel == "CRITICAL" }
            2 -> allAlerts.filter { it.riskLevel == "HIGH" }
            3 -> allAlerts.filter { it.riskLevel == "MEDIUM" }
            4 -> allAlerts.filter { it.riskLevel == "LOW" }
            else -> allAlerts
        }
        
        displayAlerts(filtered)
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

