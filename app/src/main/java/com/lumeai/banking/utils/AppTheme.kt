package com.lumeai.banking.utils

import android.graphics.Color
import android.os.Build
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.google.android.material.internal.ViewUtils.dpToPx
import com.lumeai.banking.R

/**
 * LumeAI App Theme - Consistent color system across all screens
 * Inspired by modern fintech apps: Revolut, Stripe, Wise, Chase
 */
object AppTheme {
    
    // PRIMARY COLORS - DARKER Blue for visibility
    object Primary {
        val Blue = Color.parseColor("#008577")             // Medium blue for text/icons#008577
        val LightBlue = Color.parseColor("#E3F2FD")        // Very light blue for backgrounds#00574B
        val HeaderBlue =
            Color.parseColor("#42A5F5")       // Darker blue for headers (white text visible!)#D81B60
        val Primary = "#1EB980".toColorInt()
        val PrimaryDark = "#045D56".toColorInt()
        val Accent = "#D81B60".toColorInt()
        val PrimaryLight = "#37EFBA".toColorInt()
    }
    
    // Keep for backward compatibility
    object Secondary {
        val Blue = Color.parseColor("#64B5F6")             // Same as primary
    }
    
    // DARKER blue gradient for headers (white text clearly visible)
    object Gradients {
        val PrimaryHeader = intArrayOf(
            Color.parseColor("#42A5F5"),  // Darker blue
            Color.parseColor("#1E88E5")   // Deep blue
        )
        val CardAccent = intArrayOf(Primary.HeaderBlue, Primary.Blue)
        val Subtle = intArrayOf(Primary.LightBlue, Primary.HeaderBlue)
    }
    
    // Convenience properties
    val DeepBlue = Primary.Blue
    val BrightBlue = Primary.Blue
    val LightBlue = Primary.LightBlue
    
    // SEMANTIC COLORS - Status and feedback
    object Status {
        val Success = Color.parseColor("#10B981")          // Green
        val Warning = Color.parseColor("#F59E0B")          // Amber
        val Error = Color.parseColor("#EF4444")            // Red
        val Info = Color.parseColor("#3B82F6")             // Blue
    }
    
    object Cards {
        val Surface = /*"#373740"*/"#FFFFFF".toColorInt()
        val SurfaceLight = "#f6f7f9".toColorInt()
        val OnSurface = "#26282F".toColorInt()
    }

    // BACKGROUND COLORS
    object Background {
        val Primary = Color.parseColor(/*"#33333D"*/"#F3F5F8")          // Light gray-blue
        val Secondary = Color.parseColor("#29529E")        // Very light blue
        val Card = Color.WHITE                             // Pure white
        val Subtle = Color.parseColor("#F1F5F9")           // Subtle gray
    }
    
    // TEXT COLORS
    object Text {
        val Primary = "#FFFFFF".toColorInt()          // Almost black
        val Secondary = "#a4b5d5".toColorInt()        // Medium gray
        val OnCard = "#113D6B".toColorInt()
        val OnCardSecondary = "#27519e".toColorInt()
        val Tertiary = "#e0e3e8".toColorInt()         // Light gray
        val OnPrimary = Color.WHITE                        // White on colored bg
        val Link = Color.parseColor("#2563EB")             // Link blue
    }
    
    // STATUS CARD BACKGROUNDS
    object StatusBg {
        val Success = Color.parseColor("#F0FDF4")          // Light green
        val Warning = Color.parseColor("#FFFBEB")          // Light amber
        val Error = Color.parseColor("#FEF2F2")            // Light red
        val Info = Color.parseColor("#EFF6FF")             // Light blue
    }
    
    // ACCENT COLORS - Special highlights
    object Accent {
        val Purple = Color.parseColor("#7C3AED")           // AI intelligence
        val Pink = Color.parseColor("#EC4899")             // Highlights
        val Teal = Color.parseColor("#14B8A6")             // Alternative accent
    }
    
    // OPACITY HELPERS
    fun withOpacity(color: Int, opacity: Float): Int {
        val alpha = (opacity * 255).toInt().coerceIn(0, 255)
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }

    val CornerRadius = 24
}

