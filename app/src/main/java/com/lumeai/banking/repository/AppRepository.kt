package com.lumeai.banking.repository

import android.content.Context
import androidx.room.withTransaction
import com.lumeai.banking.GenerativeAIService
import com.lumeai.banking.data.AppDatabase
import com.lumeai.banking.data.ImprovementStepEntity
import com.lumeai.banking.data.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.lumeai.banking.remote.BureauMockApi
import com.lumeai.banking.notifications.Notifier

class AppRepository private constructor(context: Context) {
    
    private val db = AppDatabase.get(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Profile streams
    val profile: Flow<UserProfileEntity> =
        db.userProfileDao().observe().map { it ?: UserProfileEntity() }
            .distinctUntilChanged()
    
    val creditScore: Flow<Int> = profile.map { it.creditScore }.distinctUntilChanged()
    val monthlyIncome: Flow<Double> = profile.map { it.monthlyIncome }.distinctUntilChanged()
    val monthlyDebt: Flow<Double> = profile.map { it.monthlyDebt }.distinctUntilChanged()
    
    val dti: Flow<Double> = combine(monthlyDebt, monthlyIncome) { debt, income ->
        if (income <= 0.0) 0.0 else (debt / income).coerceIn(0.0, 1.0)
    }.distinctUntilChanged()
    
    val eligibility: Flow<Int> = combine(creditScore, dti) { cs, d ->
        GenerativeAIService.estimateEligibility(cs, d)
    }.distinctUntilChanged()
    
    val nudges: Flow<List<String>> = combine(creditScore, dti) { cs, d ->
        GenerativeAIService.composeImprovementNudges(cs, d)
    }
    
    // Steps
    val steps: Flow<List<ImprovementStepEntity>> = db.stepsDao().observeAll()
        .map { if (it.isEmpty()) defaultSteps() else it }
        .distinctUntilChanged()
    
    // Next best action derived from pending steps and weights; fallback to generative text.
    val nextBestAction: Flow<String> = combine(steps, creditScore, dti) { s, cs, ratio ->
        val pending = s.filter { it.status != "Done" }.sortedByDescending { it.weight }
        val top = pending.firstOrNull()
        when (top?.id) {
            "lower_utilization" -> "Lower credit utilization below 50% to improve DTI."
            "on_time_payments" -> "Enable autopay to build 6 months of on‑time payments."
            "credit_report" -> "Pull your credit report and dispute any errors."
            "consolidate_debt" -> "Consolidate high‑interest debt to reduce monthly payments."
            "update_income" -> "Upload latest income proofs to boost eligibility."
            else -> GenerativeAIService.composeDashboardSummary(cs, ratio, GenerativeAIService.estimateEligibility(cs, ratio))
        }
    }.distinctUntilChanged()
    
    val progressPct: Flow<Int> = steps.map { list ->
        val total = list.sumOf { it.weight }
        val done = list.filter { it.status == "Done" }.sumOf { it.weight }
        if (total <= 0.0) 0 else ((done / total) * 100).roundToInt()
    }
    
    suspend fun updateProfile(
        creditScore: Int? = null,
        monthlyIncome: Double? = null,
        monthlyDebt: Double? = null
    ) {
        val current = profile.first()
        val next = current.copy(
            creditScore = creditScore ?: current.creditScore,
            monthlyIncome = monthlyIncome ?: current.monthlyIncome,
            monthlyDebt = monthlyDebt ?: current.monthlyDebt,
            updatedAt = System.currentTimeMillis()
        )
        db.userProfileDao().upsert(next)
        // Regenerate steps based on new state
        scope.launch { regenerateSteps() }
    }
    
    suspend fun markStepDone(id: String, done: Boolean) {
        val current = steps.first()
        val updated = current.map {
            if (it.id == id) it.copy(status = if (done) "Done" else "Pending") else it
        }
        db.withTransaction {
            db.stepsDao().clear()
            db.stepsDao().upsertAll(updated)
        }
    }
    
    suspend fun regenerateSteps() {
        val cs = creditScore.first()
        val ratio = dti.first()
        val newSteps = mutableListOf<ImprovementStepEntity>()
        newSteps += ImprovementStepEntity(
            id = "credit_report",
            title = "Get free credit report",
            subtitle = "Check for errors and disputes",
            weight = 0.15,
            status = "Pending"
        )
        if (cs < 720) {
            newSteps += ImprovementStepEntity(
                id = "on_time_payments",
                title = "On-time payments",
                subtitle = "Autopay for next 6 months",
                weight = 0.25,
                status = "Pending"
            )
        }
        if (ratio > 0.4) {
            newSteps += ImprovementStepEntity(
                id = "lower_utilization",
                title = "Lower card utilization",
                subtitle = "Pay down balances < 50%",
                weight = 0.35,
                status = "Pending"
            )
            newSteps += ImprovementStepEntity(
                id = "consolidate_debt",
                title = "Consolidate high-interest debt",
                subtitle = "Reduce monthly payments",
                weight = 0.15,
                status = "Pending"
            )
        } else {
            newSteps += ImprovementStepEntity(
                id = "maintain_habits",
                title = "Maintain good habits",
                subtitle = "Review your credit report quarterly",
                weight = 0.20,
                status = "Pending"
            )
        }
        db.withTransaction {
            db.stepsDao().clear()
            db.stepsDao().upsertAll(newSteps)
        }
    }
    
    // Complete prerequisites for a target step (e.g., reapply requires others done)
    suspend fun completePrerequisites(targetId: String) {
        val current = steps.first()
        val required = when (targetId) {
            "reapply" -> listOf("credit_report", "lower_utilization", "on_time_payments", "update_income")
            else -> emptyList()
        }
        if (required.isEmpty()) return
        val updated = current.map {
            if (it.id in required) it.copy(status = "Done") else it
        }
        db.withTransaction {
            db.stepsDao().clear()
            db.stepsDao().upsertAll(updated)
        }
    }
    
    // Simulate bureau pull; return whether eligibility improved to trigger notifications.
    suspend fun refreshFromBureau(context: Context) {
        val beforeElig = eligibility.first()
        val before = profile.first()
        val snap = BureauMockApi.fetchSnapshot(before)
        db.userProfileDao().upsert(
            before.copy(
                creditScore = snap.creditScore,
                monthlyIncome = snap.monthlyIncome,
                monthlyDebt = snap.monthlyDebt,
                updatedAt = System.currentTimeMillis()
            )
        )
        regenerateSteps()
        val afterElig = eligibility.first()
        if (afterElig > beforeElig) {
            Notifier.eligibilityImproved(context, beforeElig, afterElig)
            val nba = nextBestAction.first()
            Notifier.nextBestAction(context, nba)
        }
    }
    
    private fun defaultSteps(): List<ImprovementStepEntity> = listOf(
        ImprovementStepEntity("credit_report","Get free credit report","Check for errors and disputes",0.15,"Pending"),
        ImprovementStepEntity("lower_utilization","Lower card utilization","Pay down balances < 50%",0.35,"Pending"),
        ImprovementStepEntity("update_income","Update income proof","Upload latest salary slips/ITR",0.20,"Pending"),
        ImprovementStepEntity("on_time_payments","On-time payments","Autopay for next 6 months",0.25,"Pending"),
        ImprovementStepEntity("reapply","Reapply for loan","After completing above steps",0.05,"Pending")
    )
    
    companion object {
        @Volatile private var INSTANCE: AppRepository? = null
        fun get(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                val i = AppRepository(context.applicationContext)
                INSTANCE = i
                i
            }
        }
    }
}


