package com.example.datetimenotificationapp_bm
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        sendNotification(
            context,
            intent.getStringExtra("title") !!,
            intent.getStringExtra("text") !!
        )

    }
}