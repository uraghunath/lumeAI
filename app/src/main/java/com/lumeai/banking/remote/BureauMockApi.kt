package com.lumeai.banking.remote

import com.lumeai.banking.data.UserProfileEntity
import kotlin.random.Random

/**
 * BureauMockApi - simulates a credit bureau snapshot pull.
 * Uses prior values to produce realistic drifts and occasional improvements.
 */
object BureauMockApi {
	
	data class Snapshot(
		val creditScore: Int,
		val monthlyIncome: Double,
		val monthlyDebt: Double
	)
	
	fun fetchSnapshot(previous: UserProfileEntity): Snapshot {
		// Simulate score drifting toward 720, with bounded noise
		val target = 720
		val delta = ((target - previous.creditScore) * 0.15).toInt()
		val noise = Random.nextInt(-12, 20)
		val newScore = (previous.creditScore + delta + noise).coerceIn(300, 900)
		
		// Income small seasonal bump
		val incomeNoise = Random.nextDouble(-0.01, 0.02)
		val newIncome = (previous.monthlyIncome * (1 + incomeNoise)).coerceAtLeast(10000.0)
		
		// Debt reacts to nudges (slowly reduces if DTI high)
		val highDti = previous.monthlyDebt / previous.monthlyIncome > 0.4
		val debtNoise = if (highDti) Random.nextDouble(-0.05, 0.00) else Random.nextDouble(-0.02, 0.02)
		val newDebt = (previous.monthlyDebt * (1 + debtNoise)).coerceAtLeast(0.0)
		
		return Snapshot(
			creditScore = newScore,
			monthlyIncome = "%.2f".format(newIncome).toDouble(),
			monthlyDebt = "%.2f".format(newDebt).toDouble()
		)
	}
}


