package com.example.pawcketdoc.util

import com.example.pawcketdoc.dto.GrowthProgress

object GrowthUtil {
    fun fillMissingMonths(data: List<GrowthProgress>):  List<GrowthProgress> {
        //convert list to hashmap
        val monthMap = data.associateBy { it.month }
        return (1..12).map { month ->
            val key = month.toString().padStart(2, '0') // "01", "02", etc.
            //if key exists use it, otherwise create a new entry
            monthMap[key] ?: GrowthProgress(key, 0f)
        }
    }
}