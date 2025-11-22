package com.lumeai.banking

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lumeai.banking.ui.AIExplainabilityHubActivity
import com.lumeai.banking.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch

/**
 * FirebaseListenerService - Background service that listens for new decisions
 * Runs continuously to provide instant notifications to customers
 */
class FirebaseListenerService : Service() {
    
    private val TAG = "FirebaseListenerService"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var listenerJob: Job? = null
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "lumeai_decisions"
        private const val NOTIFICATION_CHANNEL_NAME = "LumeAI Decisions"
        private const val ONGOING_NOTIFICATION_ID = 1
        
        // Store customer ID in SharedPreferences
        fun getCustomerId(context: Context): String {
            val prefs = context.getSharedPreferences("lumeai_prefs", Context.MODE_PRIVATE)
            var customerId = prefs.getString("customer_id", null)
            if (customerId == null) {
                customerId = FirebaseSyncManager.generateCustomerId()
                prefs.edit().putString("customer_id", customerId).apply()
            }
            return customerId
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 FirebaseListenerService created")
        createNotificationChannel()
        startForeground(ONGOING_NOTIFICATION_ID, createOngoingNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "📡 Starting Firebase listener...")
        
        val customerId = getCustomerId(this)
        Log.d(TAG, "👤 Customer ID: $customerId")
        
        // Start listening for decisions
        listenerJob?.cancel()
        listenerJob = scope.launch {
            try {
                FirebaseSyncManager.listenForCustomerDecisions(customerId)
                    .catch { e ->
                        Log.e(TAG, "❌ Error in Firebase listener", e)
                    }
                    .collect { decision ->
                        Log.d(TAG, "🔔 New decision received: ${decision.id}")
                        withContext(Dispatchers.Main) {
                            showDecisionNotification(decision.id, decision.outcome, decision.decisionType)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to start listener", e)
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        listenerJob?.cancel()
        scope.cancel()
        Log.d(TAG, "🛑 FirebaseListenerService destroyed")
    }
    
    /**
     * Create notification channel for Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for loan and banking decisions"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create ongoing notification (required for foreground service)
     */
    private fun createOngoingNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("✨ LumeAI Protection Active")
            .setContentText("Monitoring for new banking decisions")
            .setSmallIcon(android.R.drawable.star_on)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    /**
     * Show notification when new decision arrives
     */
    private fun showDecisionNotification(decisionId: String, outcome: String, decisionType: String) {
        // Navigate to AI Explainability Hub (unified explanation page)
        val intent = Intent(this, AIExplainabilityHubActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val (title, message, icon) = when (outcome) {
            "APPROVED" -> Triple(
                "✅ Loan Approved!",
                "Great news! Your loan application has been approved. Tap to see details.",
                android.R.drawable.ic_dialog_info
            )
            "DENIED" -> Triple(
                "Loan Decision Update",
                "Your loan application was not approved. Tap to see why and how to improve.",
                android.R.drawable.ic_dialog_alert
            )
            else -> Triple(
                "Loan Decision Update",
                "A decision has been made on your application. Tap to view.",
                android.R.drawable.ic_dialog_info
            )
        }
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(decisionId.hashCode(), notification)
        
        Log.d(TAG, "✅ Notification shown for decision: $decisionId")
    }
}

