package com.lumeai.banking.utils

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lumeai.banking.R

/**
 * UI Helper - Common UI components for consistent design across all activities
 */
object UIHelper {
    
    /**
     * Create consistent header with gradient background, back button, and title
     */
    fun createStandardHeader(
        activity: Activity,
        title: String,
        showBackButton: Boolean = true
    ): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                activity.dpToPx(64)
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(activity.dpToPx(16), activity.dpToPx(8), activity.dpToPx(16), activity.dpToPx(8))
            
            // Gradient background
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                AppTheme.Gradients.PrimaryHeader
            )
            
            // Back button
            if (showBackButton) {
                addView(TextView(activity).apply {
                    text = "←"
                    textSize = 28f
                    setTextColor(Color.WHITE)
                    setPadding(activity.dpToPx(4), activity.dpToPx(4), activity.dpToPx(4), activity.dpToPx(4))
                    layoutParams = LinearLayout.LayoutParams(activity.dpToPx(48), activity.dpToPx(48)).apply {
                        rightMargin = activity.dpToPx(12)
                    }
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    // Add ripple effect
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    setOnClickListener { activity.finish() }
                })
            }
            
            // Title
            addView(TextView(activity).apply {
                text = title
                textSize = 20f
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
        }
    }
    
    /**
     * Create standard white card with rounded corners
     */
    fun createCard(activity: Activity): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(activity.dpToPx(20), activity.dpToPx(12), activity.dpToPx(20), activity.dpToPx(12))
            }
            setPadding(activity.dpToPx(20), activity.dpToPx(20), activity.dpToPx(20), activity.dpToPx(20))
            
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = activity.dpToPx(AppTheme.CornerRadius).toFloat()
                setStroke(activity.dpToPx(1), AppTheme.withOpacity(AppTheme.Text.OnCard, 0.1f))
            }
            
            elevation = activity.dpToPx(2).toFloat()
        }
    }
    
    /**
     * Create section header text
     */
    fun createSectionHeader(activity: Activity, text: String): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 18f
            setTextColor(AppTheme.Text.OnCard)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, activity.dpToPx(12))
        }
    }
    
    /**
     * Create body text with consistent styling
     */
    fun createBodyText(activity: Activity, text: String): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 14f
            setTextColor(AppTheme.Text.OnCardSecondary)
            lineHeight = (textSize * 1.5f).toInt()
        }
    }
    
    /**
     * Create gradient button
     */
    fun createGradientButton(
        activity: Activity,
        text: String,
        gradientColors: IntArray = AppTheme.Gradients.PrimaryHeader,
        onClick: () -> Unit
    ): TextView {
        return TextView(activity).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(activity.dpToPx(24), activity.dpToPx(16), activity.dpToPx(24), activity.dpToPx(16))
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = activity.dpToPx(16)
            }
            
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors).apply {
                cornerRadius = activity.dpToPx(12).toFloat()
            }
            
            elevation = activity.dpToPx(4).toFloat()
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }
    
    /**
     * Extension function to convert dp to pixels
     */
    private fun Activity.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}

