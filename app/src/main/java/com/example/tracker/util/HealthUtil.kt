package com.example.tracker.util

import com.example.tracker.dto.HealthAnalytics
import com.example.tracker.dto.PetMedicationCount

object HealthUtil {
    fun calculateHealthAnalytics(medicationData: List<PetMedicationCount>): HealthAnalytics {
        var healthyCount = 0
        var minorCount = 0
        var seriousCount = 0
        var criticalCount = 0

        for (petData in medicationData) {
            when  {
                petData.medicationCount == 1 -> minorCount++
                petData.medicationCount == 2 -> seriousCount++
                petData.medicationCount >= 3 -> criticalCount++
                else -> healthyCount++
            }
        }
        return HealthAnalytics(
            healthyCount = healthyCount,
            minorIssueCount = minorCount,
            seriousIssueCount = seriousCount,
            criticalIssueCount = criticalCount
        )
    }

    fun calculateHealthyPercentage(healthAnalytics: HealthAnalytics): Float {
        val total = healthAnalytics.healthyCount + healthAnalytics.minorIssueCount +
                healthAnalytics.seriousIssueCount + healthAnalytics.seriousIssueCount
        val healthyPercentage = (healthAnalytics.healthyCount / total.toFloat()) * 100
        return healthyPercentage
    }

}