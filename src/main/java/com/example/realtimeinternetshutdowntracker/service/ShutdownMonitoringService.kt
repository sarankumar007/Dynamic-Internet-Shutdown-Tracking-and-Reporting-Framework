package com.example.realtimeinternetshutdowntracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.realtimeinternetshutdowntracker.MainActivity
import com.example.realtimeinternetshutdowntracker.R
import com.example.realtimeinternetshutdowntracker.data.*
import com.example.realtimeinternetshutdowntracker.location.LocationService
import com.example.realtimeinternetshutdowntracker.network.ConnectivityMonitor
import com.example.realtimeinternetshutdowntracker.repository.ShutdownRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class ShutdownMonitoringService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var connectivityMonitor: ConnectivityMonitor
    private lateinit var repository: ShutdownRepository
    private lateinit var locationService: LocationService
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "shutdown_monitoring_channel"
        private const val MONITORING_INTERVAL_MINUTES = 30L
        private const val PING_INTERVAL_MINUTES = 3L
    }
    
    override fun onCreate() {
        super.onCreate()
        connectivityMonitor = ConnectivityMonitor(this)
        repository = ShutdownRepository(this)
        locationService = LocationService(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Monitoring internet connectivity..."))
        startPeriodicMonitoring()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Internet Shutdown Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors internet connectivity for shutdown detection"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Internet Shutdown Tracker")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun startPeriodicMonitoring() {
        println("🚀 Starting Internet Shutdown Monitoring Service...")
        println("⏰ Monitoring interval: $MONITORING_INTERVAL_MINUTES minutes")
        
        serviceScope.launch {
            while (isActive) {
                try {
                    println("\n🔄 === MONITORING CYCLE START ===")
                    println("⏰ Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                    
                    val result = connectivityMonitor.checkConnectivity()
                    
                    if (result.isShutdownSuspected && result.confidence > 0.7f) {
                        println("🚨 SHUTDOWN DETECTED! Handling suspected shutdown...")
                        handleSuspectedShutdown(result)
                    } else {
                        println("✅ No shutdown detected. Continuing monitoring...")
                    }
                    
                    updateNotification("Last check: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}")
                    
                    println("⏳ Waiting $MONITORING_INTERVAL_MINUTES minutes before next check...")
                    println("=== MONITORING CYCLE END ===\n")
                    
                } catch (e: Exception) {
                    println("❌ Monitoring error: ${e.message}")
                    println("🔄 Retrying in 1 minute...")
                }
                
                delay(TimeUnit.MINUTES.toMillis(MONITORING_INTERVAL_MINUTES))
            }
        }
    }
    
    private suspend fun handleSuspectedShutdown(result: NetworkCheckResult) {
        println("🚨 === HANDLING SUSPECTED SHUTDOWN ===")
        println("📊 Shutdown Details:")
        println("   - Confidence: ${(result.confidence * 100).toInt()}%")
        println("   - Poor Signal: ${result.isPoorSignal}")
        println("   - Actual Shutdown: ${result.isActualShutdown}")
        println("   - Network Type: ${result.connectivityStatus.networkType}")
        println("   - Signal Strength: ${result.connectivityStatus.signalStrength}dBm")
        println("   - Carrier: ${result.connectivityStatus.carrier}")
        
        val report = createShutdownReport(result)
        println("📝 Created shutdown report with ID: ${report.id}")
        println("   - District: ${report.district}")
        println("   - State: ${report.state}")
        println("   - Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(report.timestamp))}")
        println("   - Ping Results Count: ${report.pingResults.size}")
        
        println("\n📋 === SERVICE: COMPLETE JSON PAYLOAD (for FastAPI testing) ===")
        val jsonPayload = gson.toJson(report)
        println(jsonPayload)
        println("=== END OF JSON PAYLOAD ===\n")
        
        println("🌐 Sending report to backend API...")
        val apiResult = repository.sendReportToBackend(report)
        
        if (apiResult.isSuccess) {
            println("✅ Report sent successfully to backend!")
            showShutdownNotification()
        } else {
            println("❌ Failed to send report to backend: ${apiResult.exceptionOrNull()?.message}")
            updateNotification("Failed to send shutdown report: ${apiResult.exceptionOrNull()?.message}")
        }
        println("=== SHUTDOWN HANDLING COMPLETE ===\n")
    }
    
    private suspend fun createShutdownReport(result: NetworkCheckResult): ShutdownReport {
        val locationResult = locationService.getCurrentLocation()
        val district: String
        val state: String
        val latitude: Double
        val longitude: Double
        
        if (locationResult != null) {
            println("📍 Location obtained: ${locationResult.district}, ${locationResult.state}")
            println("   Coordinates: (${locationResult.latitude}, ${locationResult.longitude})")
            district = locationResult.district
            state = locationResult.state
            latitude = locationResult.latitude
            longitude = locationResult.longitude
        } else {
            println("⚠️ Could not obtain location: permissions not granted or location unavailable")
            district = "Unknown"
            state = "Unknown"
            latitude = 0.0
            longitude = 0.0
        }
        
        return ShutdownReport(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            district = district,
            state = state,
            latitude = latitude,
            longitude = longitude,
            networkType = result.connectivityStatus.networkType,
            pingResults = result.pingResults,
            deviceInfo = DeviceInfo(
                androidVersion = Build.VERSION.RELEASE,
                deviceModel = Build.MODEL,
                carrier = result.connectivityStatus.carrier,
                signalStrength = result.connectivityStatus.signalStrength
            ),
            signalQuality = result.connectivityStatus.signalQuality
        )
    }
    
    private fun showShutdownNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Internet Shutdown Detected")
            .setContentText("Tap to confirm or deny this shutdown report")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(2, notification)
    }
    
    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
