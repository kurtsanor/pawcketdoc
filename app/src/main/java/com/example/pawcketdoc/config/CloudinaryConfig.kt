package com.example.pawcketdoc.config

import com.cloudinary.Cloudinary
import com.example.pawcketdoc.BuildConfig

object CloudinaryConfig {
    val instance: Cloudinary by lazy {
         Cloudinary(mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        ))
    }
}