package com.lumeai.banking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lumeai.banking.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import com.lumeai.banking.data.ImprovementStepEntity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProgressViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository.get(app)
    val steps: Flow<List<ImprovementStepEntity>> = repo.steps
    val progressPct: Flow<Int> = repo.progressPct
    val nudges: Flow<List<String>> = repo.nudges
    val nextBestAction: Flow<String> = repo.nextBestAction
    
    fun setStepDone(id: String, done: Boolean) {
        viewModelScope.launch { repo.markStepDone(id, done) }
    }
    
    fun regenerate() {
        viewModelScope.launch { repo.regenerateSteps() }
    }
    
    fun completePrerequisitesForReapply() {
        viewModelScope.launch { repo.completePrerequisites("reapply") }
    }
    
    fun refreshBureau() {
        viewModelScope.launch { repo.refreshFromBureau(getApplication()) }
    }
    
    fun randomizeCreditScore(min: Int = 600, max: Int = 800) {
        val score = Random.nextInt(min, max + 1)
        viewModelScope.launch { repo.updateProfile(creditScore = score) }
    }
}


