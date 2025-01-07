package com.example.expensesmstracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    // Callback to trigger when a new SMS is received
    var onSmsReceived: ((String) -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SmsReceiver", "onReceive called with action: ${intent.action}")
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>?
                if (pdus != null) {
                    for (pdu in pdus) {
                        val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                        val messageBody = smsMessage.messageBody
                        Log.d("SmsReceiver", "New SMS received: $messageBody")
                        onSmsReceived?.invoke(messageBody) // Trigger the callback with the new SMS
                    }
                }
            }
        }
    }
}