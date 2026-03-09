package com.example.pawcketdoc.util

import com.example.pawcketdoc.dto.AppointmentMonthCount

object AppointmentUtil {
     fun fillMissingMonths(data: List<AppointmentMonthCount>): List<AppointmentMonthCount> {
        //convert list to hashmap
        val monthMap = data.associateBy { it.month }
        return (1..12).map { month ->
            val key = month.toString().padStart(2, '0') // "01", "02", etc.
            //if key exists use it, otherwise create a new entry
            monthMap[key] ?: AppointmentMonthCount(key, 0)
        }
    }
}