package com.example.pawcketdoc.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toShortMonthFormat(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
        return date.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toShortMonthFormat(datetime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM, YYYY • hh:mm a", Locale.ENGLISH)
        return datetime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toShorterMonthFormat(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH)
        return date.format(formatter).uppercase()
    }
}