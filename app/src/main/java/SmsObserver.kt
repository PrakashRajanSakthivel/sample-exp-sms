package com.example.expensesmstracker

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log

class SmsObserver(
    private val context: Context,
    private val onSmsReceived: () -> Unit, // Callback to trigger when a new SMS is received
    handler: Handler
) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d("SmsObserver", "SMS inbox changed. URI: $uri")
        onSmsReceived() // Trigger the callback to read the new SMS
    }
}