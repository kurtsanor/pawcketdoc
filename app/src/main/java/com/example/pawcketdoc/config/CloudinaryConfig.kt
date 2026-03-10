package com.example.pawcketdoc.config

import com.cloudinary.Cloudinary
import com.example.pawcketdoc.BuildConfig

object CloudinaryConfig {
    val instance: Cloudinary by lazy {
         Cloudinary(mapOf(
            "cloud_name" to "dorumqag0",
            "api_key" to "373386584149747",
            "api_secret" to "pJDJW58tDphaIIsKA1oyV96m4po"
        ))
    }
}