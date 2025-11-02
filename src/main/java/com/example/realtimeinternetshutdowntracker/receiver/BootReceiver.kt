package com.example.realtimeinternetshutdowntracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.realtimeinternetshutdowntracker.service.ShutdownMonitoringService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, ShutdownMonitoringService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
