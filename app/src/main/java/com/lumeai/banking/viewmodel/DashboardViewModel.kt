package com.lumeai.banking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lumeai.banking.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository.get(app)
    val creditScore: Flow<Int> = repo.creditScore
    val dti: Flow<Double> = repo.dti
    val eligibility: Flow<Int> = repo.eligibility
    val nudges: Flow<List<String>> = repo.nudges

    fun seedIfNeeded() {
        viewModelScope.launch {
            // Touch the profile to ensure first row exists (AppRepository emits default)
            val cs = repo.creditScore
        }
    }
}


