package com.example.pawcketdoc.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SwipeDeleteHelper {
    private val viewBinderHelper = ViewBinderHelper().apply {
        setOpenOnlyOne(true)
    }

    fun bind(swipeLayout: SwipeRevealLayout, uniqueId: String) {
        viewBinderHelper.bind(swipeLayout, uniqueId)
    }

    fun close(uniqueId: String) {
        viewBinderHelper.closeLayout(uniqueId)
    }

    companion object {
        fun confirmDelete(
            fragment: Fragment,
            title: String = "Delete Record",
            message: String,
            onConfirm: suspend () -> Unit
        ) {
            MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { dialog, _ ->
                    fragment.viewLifecycleOwner.lifecycleScope.launch {
                        onConfirm()
                        dialog.dismiss()
                    }
                }
                .show()
        }
    }
}
