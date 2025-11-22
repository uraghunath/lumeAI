package com.lumeai.banking.utils

import android.content.Context
import com.lumeai.banking.models.FirebaseDecision

/**
 * LanguageHelper - Centralized language management
 * Ensures all activities consistently use the correct language for both static and dynamic content
 */
object LanguageHelper {
    
    private const val PREFS_NAME = "LumeAILanguage"
    private const val KEY_LANGUAGE = "language"
    
    /**
     * Get current language preference
     * @return Language code: "en", "hi", or "te"
     */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }
    
    /**
     * Save language preference
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Get AI-generated summary in the current language
     * Automatically falls back to English if translation not available
     */
    fun getSummary(decision: FirebaseDecision, languageCode: String): String {
        return when (languageCode) {
            "hi" -> decision.summaryHindi.ifEmpty { decision.summaryEnglish }
            "te" -> decision.summaryTelugu.ifEmpty { decision.summaryEnglish } // ✅ FIXED: Use Telugu summary
            else -> decision.summaryEnglish
        }
    }
    
    /**
     * Get bias message in the current language
     * Note: biasMessage is currently only in English, this is a placeholder for future multilingual support
     */
    fun getBiasMessage(decision: FirebaseDecision, languageCode: String): String {
        // Currently bias messages are only in English
        // In future, we can add biasMessageHindi, biasMessageTelugu fields
        return decision.biasMessage
    }
    
    /**
     * Get language display name
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "hi" -> "हिंदी (Hindi)"
            "te" -> "తెలుగు (Telugu)"
            else -> "English"
        }
    }
    
    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            "English" to "en",
            "हिंदी" to "hi",
            "తెలుగు" to "te"
        )
    }
}

