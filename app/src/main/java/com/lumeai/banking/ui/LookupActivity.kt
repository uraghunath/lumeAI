package com.lumeai.banking.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.lumeai.banking.MockBankAPI
import com.lumeai.banking.models.BankDecision
import com.lumeai.banking.utils.AppTheme

/**
 * LookupActivity - Modern UI for transaction lookup
 */
class LookupActivity : AppCompatActivity() {
    
    private lateinit var bankSpinner: Spinner
    private lateinit var transactionTypeSpinner: Spinner
    private lateinit var idInput: EditText
    private lateinit var lookupButton: Button
    private lateinit var resultContainer: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.statusBarColor = 0xFF1976D2.toInt()
        
        // Hide action bar to avoid duplicate header
        supportActionBar?.hide()
        
        setContentView(createUI())
        setupSpinners()
    }
    
    private fun createUI(): ScrollView {
        val scrollView = ScrollView(this)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F7FA.toInt())
        }
        
        // Header
        rootLayout.addView(createHeader())
        
        // Content
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
        }
        
        // Info card
        contentLayout.addView(createInfoCard())
        addSpace(contentLayout, 20)
        
        // Input form card
        contentLayout.addView(createInputCard())
        addSpace(contentLayout, 20)
        
        // Result container
        resultContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        contentLayout.addView(resultContainer)
        
        rootLayout.addView(contentLayout)
        scrollView.addView(rootLayout)
        return scrollView
    }
    
    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(AppTheme.Background.Secondary)
            setPadding(dpToPx(16), dpToPx(40), dpToPx(20), dpToPx(24))
            gravity = Gravity.CENTER_VERTICAL
            
            addView(TextView(this@LookupActivity).apply {
                text = "←"
                textSize = 32f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 0, dpToPx(16), 0)
                setOnClickListener { finish() }
            })
            
            addView(LinearLayout(this@LookupActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                
                addView(TextView(this@LookupActivity).apply {
                    text = "🔍 Transaction Lookup"
                    textSize = 22f
                    setTextColor(0xFFFFFFFF.toInt())
                    setTypeface(null, android.graphics.Typeface.BOLD)
                })
                
                addView(TextView(this@LookupActivity).apply {
                    text = "Find decisions from any bank"
                    textSize = 13f
                    setTextColor(0xFFBBDEFB.toInt())
                    setPadding(0, dpToPx(4), 0, 0)
                })
            })
        }
    }
    
    private fun createInfoCard(): LinearLayout {
        return createModernCard(0xFFE3F2FD.toInt()) {
            addView(TextView(this@LookupActivity).apply {
                text = "ℹ️ How It Works"
                textSize = 16f
                setTextColor(0xFF1976D2.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(12))
            })
            
            listOf(
                "Enter your transaction or loan ID from any bank",
                "Select your bank and transaction type",
                "Get instant AI decision explanations",
                "See bias analysis and improvement steps"
            ).forEach { point ->
                addView(TextView(this@LookupActivity).apply {
                    text = "• $point"
                    textSize = 13f
                    setTextColor(0xFF424242.toInt())
                    setPadding(dpToPx(8), dpToPx(4), 0, dpToPx(4))
                    setLineSpacing(0f, 1.3f)
                })
            }
        }
    }
    
    private fun createInputCard(): LinearLayout {
        return createModernCard(0xFFFFFFFF.toInt()) {
            // Bank selection
            addView(TextView(this@LookupActivity).apply {
                text = "Select Bank"
                textSize = 14f
                setTextColor(0xFF424242.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(8))
            })
            
            bankSpinner = Spinner(this@LookupActivity).apply {
                setBackgroundColor(0xFFF5F7FA.toInt())
                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            }
            addView(bankSpinner)
            addSpace(this, 16)
            
            // Transaction type
            addView(TextView(this@LookupActivity).apply {
                text = "Transaction Type"
                textSize = 14f
                setTextColor(0xFF424242.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(8))
            })
            
            transactionTypeSpinner = Spinner(this@LookupActivity).apply {
                setBackgroundColor(0xFFF5F7FA.toInt())
                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            }
            addView(transactionTypeSpinner)
            addSpace(this, 16)
            
            // ID input
            addView(TextView(this@LookupActivity).apply {
                text = "Transaction/Loan ID"
                textSize = 14f
                setTextColor(0xFF424242.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(8))
            })
            
            idInput = EditText(this@LookupActivity).apply {
                hint = "e.g., TXN123456 or LOAN2024001"
                setHintTextColor(0xFF9E9E9E.toInt())
                setBackgroundColor(0xFFF5F7FA.toInt())
                setPadding(dpToPx(12), dpToPx(14), dpToPx(12), dpToPx(14))
                textSize = 15f
            }
            addView(idInput)
            addSpace(this, 20)
            
            // Lookup button
            lookupButton = Button(this@LookupActivity).apply {
                text = "🔍 Fetch Decision"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFF1976D2.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dpToPx(16), 0, dpToPx(16))
                elevation = dpToPx(4).toFloat()
                setOnClickListener { performLookup() }
            }
            addView(lookupButton)
        }
    }
    
    private fun setupSpinners() {
        bankSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("HDFC Bank", "State Bank of India", "ICICI Bank", "Axis Bank", "Kotak Mahindra")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        transactionTypeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Loan Application", "Transaction Block", "Credit Limit Change", "Account Action")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
    
    private fun performLookup() {
        val transactionId = idInput.text.toString().trim()
        if (transactionId.isEmpty()) {
            Toast.makeText(this, "Please enter a transaction ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        lookupButton.isEnabled = false
        lookupButton.text = "⏳ Fetching..."
        
        resultContainer.postDelayed({
            val bank = bankSpinner.selectedItem.toString()
            val type = transactionTypeSpinner.selectedItem.toString()
            val decision = fetchDecision(type)
            showResult(decision, bank, transactionId)
            lookupButton.isEnabled = true
            lookupButton.text = "🔍 Fetch Decision"
        }, 800)
    }
    
    private fun fetchDecision(type: String): BankDecision {
        return when (type) {
            "Transaction Block" -> MockBankAPI.simulateTransactionBlock()
            "Credit Limit Change" -> MockBankAPI.simulateCreditLimitReduction()
            else -> if (Math.random() > 0.5) MockBankAPI.simulateLoanDenial() 
                    else MockBankAPI.simulateLoanApproval()
        }
    }
    
    private fun showResult(decision: BankDecision, bank: String, id: String) {
        resultContainer.removeAllViews()
        resultContainer.visibility = View.VISIBLE
        
        resultContainer.addView(createModernCard(0xFFE8F5E9.toInt()) {
            addView(TextView(this@LookupActivity).apply {
                text = "✓ Decision Found"
                textSize = 18f
                setTextColor(0xFF2E7D32.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dpToPx(12))
            })
            
            addView(TextView(this@LookupActivity).apply {
                text = "Bank: $bank\nTransaction ID: $id"
                textSize = 14f
                setTextColor(0xFF424242.toInt())
                setLineSpacing(0f, 1.4f)
                setPadding(0, 0, 0, dpToPx(16))
            })
            
            addView(Button(this@LookupActivity).apply {
                text = "💡 View Full Explanation"
                textSize = 15f
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xFF2E7D32.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dpToPx(14), 0, dpToPx(14))
                setOnClickListener {
                    // Navigate to AI Explainability Hub (unified explanation page)
                    val intent = Intent(this@LookupActivity, AIExplainabilityHubActivity::class.java)
                    startActivity(intent)
                }
            })
        })
    }
    
    private fun createModernCard(bgColor: Int, content: LinearLayout.() -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
            setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
            elevation = dpToPx(3).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            content()
        }
    }
    
    private fun addSpace(parent: LinearLayout, dp: Int) {
        parent.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(dp)
            )
        })
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

