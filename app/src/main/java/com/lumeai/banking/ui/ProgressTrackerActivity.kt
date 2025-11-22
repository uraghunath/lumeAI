package com.lumeai.banking.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lumeai.banking.viewmodel.ProgressViewModel
import androidx.activity.viewModels
import com.lumeai.banking.data.ImprovementStepEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lumeai.banking.work.BureauSyncWorker
import java.util.concurrent.TimeUnit

class ProgressTrackerActivity : AppCompatActivity() {
    
    private val vm: ProgressViewModel by viewModels()
    private lateinit var progressValue: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var stepsContainer: LinearLayout
    private lateinit var nextBestContainer: LinearLayout
    private lateinit var nudgesContainer: LinearLayout
    private lateinit var refreshButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Credit Improvement Roadmap"
        setContentView(buildUi())
        bindFlows()
        scheduleSync()
    }
    
    private fun buildUi(): ScrollView {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        
        // Header
        root.addView(TextView(this).apply {
            text = "Your Roadmap"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        addSpace(root, 8)
        
        // Next Best Action
        root.addView(card().apply {
            addView(TextView(this@ProgressTrackerActivity).apply {
                text = "Next Best Action"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dp(6))
            })
            nextBestContainer = LinearLayout(this@ProgressTrackerActivity).apply {
                orientation = LinearLayout.VERTICAL
            }
            addView(nextBestContainer)
            val prereqBtn = Button(this@ProgressTrackerActivity).apply {
                text = "Complete prerequisites for Reapply"
                setOnClickListener { vm.completePrerequisitesForReapply() }
            }
            addView(prereqBtn)
        })
        
        addSpace(root, 8)
        
        // Progress summary
        root.addView(card().apply {
            addView(TextView(this@ProgressTrackerActivity).apply {
                text = "Estimated Progress"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, dp(8))
            })
            progressBar = ProgressBar(this@ProgressTrackerActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                this.progress = 0
            }
            progressValue = TextView(this@ProgressTrackerActivity).apply {
                text = "0% to target"
                textSize = 14f
                setPadding(0, 0, 0, dp(6))
            }
            addView(progressValue)
            addView(progressBar)
            
            refreshButton = Button(this@ProgressTrackerActivity).apply {
                text = "Refresh Recommendations"
                setOnClickListener { vm.regenerate() }
            }
            addView(refreshButton)
            
            // Actions row: Generate Bureau + Random Score
            val actions = LinearLayout(this@ProgressTrackerActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(8), 0, 0)
            }
            val generateBureau = Button(this@ProgressTrackerActivity).apply {
                text = "Generate Bureau"
                setOnClickListener { 
                    vm.refreshBureau()
                    Toast.makeText(this@ProgressTrackerActivity, "Fetching bureau snapshot…", Toast.LENGTH_SHORT).show()
                }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val randomScore = Button(this@ProgressTrackerActivity).apply {
                text = "Random Score 600–800"
                setOnClickListener { 
                    vm.randomizeCreditScore(600, 800)
                    Toast.makeText(this@ProgressTrackerActivity, "Credit score randomized", Toast.LENGTH_SHORT).show()
                }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            actions.addView(generateBureau)
            actions.addView(randomScore)
            addView(actions)
        })
        
        addSpace(root, 14)
        
        // Steps
        root.addView(TextView(this).apply {
            text = "Key Steps"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        addSpace(root, 6)
        
        stepsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(stepsContainer)
        
        // Nudges (generative, broader than next best)
        root.addView(TextView(this).apply {
            text = "Personalized Nudges"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(10), 0, dp(4))
        })
        nudgesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(nudgesContainer)
        
        scroll.addView(root)
        return scroll
    }
    
    private fun scheduleSync() {
        val work = PeriodicWorkRequestBuilder<BureauSyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bureau_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }
    
    private fun stepItem(index: Int, step: ImprovementStepEntity): LinearLayout {
        return card().apply {
            val row = LinearLayout(this@ProgressTrackerActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            row.addView(TextView(this@ProgressTrackerActivity).apply {
                text = "$index. ${step.title}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            val cb = android.widget.CheckBox(this@ProgressTrackerActivity).apply {
                isChecked = step.status == "Done"
                setOnCheckedChangeListener { _, checked ->
                    vm.setStepDone(step.id, checked)
                }
            }
            row.addView(cb)
            addView(row)
            addView(TextView(this@ProgressTrackerActivity).apply {
                text = step.subtitle
                textSize = 13f
                setTextColor(0xFF616161.toInt())
                setPadding(0, dp(4), 0, 0)
            })
            addView(TextView(this@ProgressTrackerActivity).apply {
                text = if (step.status == "Done") "Status: Completed" else "Status: Pending"
                textSize = 12f
                setPadding(0, dp(8), 0, 0)
            })
        }
    }
    private fun bindFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.steps.collectLatest { list ->
                    stepsContainer.removeAllViews()
                    list.forEachIndexed { idx, s ->
                        stepsContainer.addView(stepItem(idx + 1, s))
                        addSpace(stepsContainer, 8)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.progressPct.collectLatest { pct ->
                    progressBar.progress = pct
                    progressValue.text = "$pct% to target"
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.nudges.collectLatest { nudges ->
                    nudgesContainer.removeAllViews()
                    if (nudges.isEmpty()) {
                        nudgesContainer.addView(TextView(this@ProgressTrackerActivity).apply {
                            text = "You're on track. Keep it up!"
                            textSize = 14f
                        })
                    } else {
                        nudges.forEach { n -> nudgesContainer.addView(bullet(n)) }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.nextBestAction.collectLatest { action ->
                    nextBestContainer.removeAllViews()
                    nextBestContainer.addView(bullet(action))
                }
            }
        }
    }
    
    private fun bullet(text: String): TextView {
        return TextView(this).apply {
            this.text = "• $text"
            textSize = 14f
            setLineSpacing(0f, 1.2f)
            setPadding(0, dp(2), 0, dp(2))
        }
    }
    
    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dp(16), dp(16), dp(16), dp(16))
            elevation = dp(2).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
    
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}


