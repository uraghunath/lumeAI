package com.lumeai.banking

import android.util.Log
import com.google.firebase.database.*
import com.lumeai.banking.models.FirebaseDecision
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseSyncManager - Real-time sync between Bank Portal and Customer App
 * 
 * Architecture:
 * Bank Portal → Firebase → Customer App (instant notification)
 */
object FirebaseSyncManager {
    
    private const val TAG = "FirebaseSyncManager"
    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    
    // Firebase paths
    private const val DECISIONS_PATH = "decisions"
    private const val PENDING_PATH = "pending_decisions"
    
    /**
     * Bank Portal: Submit new decision to Firebase
     * This triggers real-time notification to customer app
     */
    suspend fun submitDecision(decision: FirebaseDecision): Result<String> {
        return try {
            val decisionRef = database.child(DECISIONS_PATH).child(decision.id)
            decisionRef.setValue(decision).await()
            
            // Also add to customer's pending list
            val pendingRef = database.child(PENDING_PATH).child(decision.customerId).child(decision.id)
            pendingRef.setValue(mapOf(
                "decisionId" to decision.id,
                "timestamp" to decision.timestamp,
                "outcome" to decision.outcome,
                "notified" to false
            )).await()
            
            Log.d(TAG, "✅ Decision ${decision.id} submitted successfully")
            Result.success(decision.id)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to submit decision", e)
            Result.failure(e)
        }
    }
    
    /**
     * Customer App: Listen for new decisions in real-time
     * Returns Flow that emits whenever a new decision arrives
     */
    fun listenForCustomerDecisions(customerId: String): Flow<FirebaseDecision> = callbackFlow {
        val pendingRef = database.child(PENDING_PATH).child(customerId)
        
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val decisionId = snapshot.child("decisionId").getValue(String::class.java) ?: return
                val notified = snapshot.child("notified").getValue(Boolean::class.java) ?: false
                
                if (!notified) {
                    // Fetch full decision
                    database.child(DECISIONS_PATH).child(decisionId)
                        .get()
                        .addOnSuccessListener { decisionSnapshot ->
                            val decision = decisionSnapshot.getValue(FirebaseDecision::class.java)
                            if (decision != null) {
                                trySend(decision)
                                
                                // Mark as notified
                                snapshot.ref.child("notified").setValue(true)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "❌ Failed to fetch decision $decisionId", e)
                        }
                }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle decision updates
                onChildAdded(snapshot, previousChildName)
            }
            
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Firebase listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        
        pendingRef.addChildEventListener(listener)
        
        awaitClose {
            pendingRef.removeEventListener(listener)
        }
    }
    
    /**
     * Customer App: Get decision by ID
     */
    suspend fun getDecision(decisionId: String): FirebaseDecision? {
        return try {
            val snapshot = database.child(DECISIONS_PATH).child(decisionId).get().await()
            snapshot.getValue(FirebaseDecision::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get decision $decisionId", e)
            null
        }
    }
    
    /**
     * Customer App: Request manual review (appeal)
     */
    suspend fun requestAppeal(decisionId: String): Boolean {
        return try {
            val updates = mapOf(
                "appealRequested" to true,
                "appealTimestamp" to System.currentTimeMillis()
            )
            database.child(DECISIONS_PATH).child(decisionId).updateChildren(updates).await()
            Log.d(TAG, "✅ Appeal requested for decision $decisionId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to request appeal", e)
            false
        }
    }
    
    /**
     * Customer App: Get all decisions for a customer
     * Runs bias detection on decisions that don't have bias analysis yet
     */
    suspend fun getCustomerDecisions(customerId: String): List<FirebaseDecision> {
        return try {
            Log.d(TAG, "🔍 Querying Firebase for customer: $customerId")
            
            val snapshot = database.child(DECISIONS_PATH)
                .orderByChild("customerId")
                .equalTo(customerId)
                .get()
                .await()
            
            Log.d(TAG, "📦 Firebase returned ${snapshot.childrenCount} children")
            
            val decisions = snapshot.children.mapNotNull { 
                val decision = it.getValue(FirebaseDecision::class.java)
                if (decision != null) {
                    Log.d(TAG, "✅ Parsed decision: ${decision.id} - ${decision.bankName} - ${decision.outcome}")
                    
                    // Run bias detection if not already done
                    if (!decision.biasDetected && decision.biasMessage.isEmpty()) {
                        try {
                            // CRITICAL CHECK: All factors passed but decision was DENIED = BIAS!
                            val isDenied = decision.outcome.equals("DENIED", ignoreCase = true)
                            val allFactorsPassed = listOf(
                                decision.creditScorePassed,
                                decision.incomePassed,
                                decision.debtRatioPassed,
                                decision.employmentPassed,
                                decision.digitalFootprintPassed
                            ).all { it }
                            
                            if (isDenied && allFactorsPassed) {
                                Log.w(TAG, "🚨 CRITICAL BIAS: All factors passed but loan was DENIED for ${decision.id}")
                                
                                val updatedDecision = decision.copy(
                                    biasDetected = true,
                                    biasSeverity = "HIGH",
                                    biasMessage = "CRITICAL: All eligibility criteria were met, but the loan was still denied. This suggests potential human bias or discriminatory decision-making that requires immediate review.",
                                    biasAffectedGroups = "Potentially all qualified applicants in this demographic"
                                )
                                
                                // Save critical bias finding to Firebase
                                updateDecisionWithExplanation(
                                    decision.id,
                                    decision.summaryEnglish,
                                    decision.summaryHindi,
                                    true,
                                    "HIGH",
                                    updatedDecision.biasMessage,
                                    updatedDecision.biasAffectedGroups
                                )
                                
                                return@mapNotNull updatedDecision
                            }
                            
                            // Regular bias detection for other cases
                            val bankDecision = decision.toBankDecision()
                            val biasWarning = BiasDetector.analyze(bankDecision)
                            
                            if (biasWarning != null) {
                                Log.d(TAG, "⚠️ Bias detected for decision ${decision.id}: ${biasWarning.severity}")
                                
                                // Update decision with bias analysis
                                val updatedDecision = decision.copy(
                                    biasDetected = true,
                                    biasSeverity = biasWarning.severity,
                                    biasMessage = biasWarning.message,
                                    biasAffectedGroups = biasWarning.affectedGroups.joinToString(", ")
                                )
                                
                                // Save bias analysis back to Firebase (async, don't wait)
                                updateDecisionWithExplanation(
                                    decision.id,
                                    decision.summaryEnglish,
                                    decision.summaryHindi,
                                    true,
                                    biasWarning.severity,
                                    biasWarning.message,
                                    biasWarning.affectedGroups.joinToString(", ")
                                )
                                
                                return@mapNotNull updatedDecision
                            } else {
                                Log.d(TAG, "✓ No bias detected for decision ${decision.id}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Failed to run bias detection for ${decision.id}: ${e.message}", e)
                        }
                    }
                } else {
                    Log.w(TAG, "⚠️ Failed to parse decision from key: ${it.key}")
                }
                decision
            }
            
            Log.d(TAG, "✅ Returning ${decisions.size} decisions for customer $customerId")
            decisions
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get customer decisions for $customerId: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Bank Portal: Get all decisions (for dashboard)
     */
    suspend fun getAllDecisions(limit: Int = 50): List<FirebaseDecision> {
        return try {
            val snapshot = database.child(DECISIONS_PATH)
                .orderByChild("timestamp")
                .limitToLast(limit)
                .get()
                .await()
            
            snapshot.children.mapNotNull { it.getValue(FirebaseDecision::class.java) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get all decisions", e)
            emptyList()
        }
    }
    
    /**
     * Bank Portal: Update decision with explanation and bias analysis
     */
    suspend fun updateDecisionWithExplanation(
        decisionId: String,
        summaryEnglish: String,
        summaryHindi: String,
        biasDetected: Boolean,
        biasSeverity: String,
        biasMessage: String,
        biasAffectedGroups: String
    ): Boolean {
        return try {
            val updates = mapOf(
                "status" to "completed",
                "summaryEnglish" to summaryEnglish,
                "summaryHindi" to summaryHindi,
                "biasDetected" to biasDetected,
                "biasSeverity" to biasSeverity,
                "biasMessage" to biasMessage,
                "biasAffectedGroups" to biasAffectedGroups
            )
            database.child(DECISIONS_PATH).child(decisionId).updateChildren(updates).await()
            Log.d(TAG, "✅ Decision $decisionId updated with explanation")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to update decision", e)
            false
        }
    }
    
    /**
     * Generate unique decision ID
     */
    fun generateDecisionId(): String {
        return "DEC_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Generate unique customer ID
     * Format: LUM_XXXXXXXXXX (matches real bank app customer ID formats)
     * Example: LUM_1234567890AB (10 digits + 2 letters)
     * 
     * Real bank apps use formats like:
     * - HDFC: HDFC12345678 (mostly numeric with prefix)
     * - ICICI: ICICI123456 (mostly numeric with prefix)
     * - SBI: SBI12345678 (mostly numeric with prefix)
     * 
     * LumeAI format: LUM_ + 10 digits + 2 uppercase letters
     */
    fun generateCustomerId(): String {
        // Generate 10 random digits
        val digits = (1..10).map { (0..9).random() }.joinToString("")
        // Generate 2 random uppercase letters
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val letterPart = (1..2).map { letters.random() }.joinToString("")
        return "LUM_$digits$letterPart"
    }
    
    /**
     * Save user consent preferences to Firebase
     */
    suspend fun saveUserConsents(
        customerId: String,
        aiAnalysis: Boolean,
        biasDetection: Boolean,
        dataSharing: Boolean,
        dataStorage: Boolean
    ): Boolean {
        return try {
            val consents = mapOf(
                "aiAnalysis" to aiAnalysis,
                "biasDetection" to biasDetection,
                "dataSharing" to dataSharing,
                "dataStorage" to dataStorage,
                "lastUpdated" to System.currentTimeMillis()
            )
            database.child("user_consents").child(customerId).setValue(consents).await()
            Log.d(TAG, "✅ Consents saved for customer $customerId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save consents", e)
            false
        }
    }
    
    /**
     * Get user consent preferences from Firebase
     */
    suspend fun getUserConsents(customerId: String): Map<String, Any>? {
        return try {
            val snapshot = database.child("user_consents").child(customerId).get().await()
            snapshot.value as? Map<String, Any>
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get consents", e)
            null
        }
    }
    
    /**
     * Submit appeal request for a bias-detected decision
     */
    suspend fun submitAppeal(decisionId: String, customerId: String): Boolean {
        return try {
            val appealData = mapOf(
                "decisionId" to decisionId,
                "customerId" to customerId,
                "timestamp" to System.currentTimeMillis(),
                "status" to "PENDING_REVIEW",
                "reason" to "BIAS_DETECTED"
            )
            database.child("appeals").child(decisionId).setValue(appealData).await()
            
            // Also update the decision to mark it as appealed
            database.child("decisions").child(decisionId).child("appealRequested").setValue(true).await()
            database.child("decisions").child(decisionId).child("appealTimestamp").setValue(System.currentTimeMillis()).await()
            
            Log.d(TAG, "✅ Appeal submitted for decision $decisionId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to submit appeal", e)
            false
        }
    }
    
    /**
     * Save user profile (name, email, mobile) to Firebase
     * This allows bank portal to fetch customer name when ID is entered
     */
    suspend fun saveUserProfile(
        customerId: String,
        name: String,
        email: String = "",
        mobile: String = ""
    ): Boolean {
        return try {
            val profile = mapOf(
                "name" to name,
                "email" to email,
                "mobile" to mobile,
                "lastUpdated" to System.currentTimeMillis()
            )
            database.child("user_profiles").child(customerId).setValue(profile).await()
            Log.d(TAG, "✅ User profile saved for customer $customerId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save user profile", e)
            false
        }
    }
    
    /**
     * Get user profile from Firebase
     */
    suspend fun getUserProfile(customerId: String): Map<String, Any>? {
        return try {
            val snapshot = database.child("user_profiles").child(customerId).get().await()
            snapshot.value as? Map<String, Any>
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get user profile", e)
            null
        }
    }
}

