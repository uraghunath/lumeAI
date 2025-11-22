package com.lumeai.banking.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lumeai.banking.repository.AppRepository

class BureauSyncWorker(
	context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {
	
	override suspend fun doWork(): Result {
		return try {
			AppRepository.get(applicationContext).refreshFromBureau(applicationContext)
			Result.success()
		} catch (t: Throwable) {
			Result.retry()
		}
	}
}


