package com.streamnator.app.util

import android.view.View

object FocusHelper {
    fun requestFocusDelayed(view: View, delayMs: Long = 100) {
        view.postDelayed({
            view.requestFocus()
        }, delayMs)
    }
}
