package com.example.pawcketdoc.util

import com.example.pawcketdoc.R
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

object SnackbarUtil {

    @SuppressLint("RestrictedApi")
    fun showError(view: View, title: String, message: String) {
        show(view, title, message, R.drawable.snackbar_bg, R.drawable.error)
    }

    @SuppressLint("RestrictedApi")
    fun showSuccess(view: View, title: String, message: String) {
        show(view, title, message, R.drawable.snackbar_bg, R.drawable.check)
    }

    @SuppressLint("RestrictedApi")
    private fun show(view: View, title: String, message: String, bgRes: Int, iconRes: Int) {
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)

        val customView = LayoutInflater.from(view.context)
            .inflate(R.layout.snackbar_error, null)

        customView.findViewById<TextView>(R.id.snackTitle).text = title
        customView.findViewById<TextView>(R.id.snackMessage).text = message
        customView.background = ContextCompat.getDrawable(view.context, bgRes)
        customView.findViewById<ImageView>(R.id.snackIcon)
            .setImageDrawable(ContextCompat.getDrawable(view.context, iconRes))

        val snackLayout = snackbar.view as Snackbar.SnackbarLayout
        snackLayout.setPadding(0, 0, 0, 0)
        snackLayout.setBackgroundColor(Color.TRANSPARENT)
        snackLayout.addView(customView, 0)

        snackbar.show()
    }
}