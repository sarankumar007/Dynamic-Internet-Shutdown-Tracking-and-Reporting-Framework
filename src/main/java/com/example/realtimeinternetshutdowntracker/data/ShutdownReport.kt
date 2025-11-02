package com.example.realtimeinternetshutdowntracker.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ShutdownReport(
    val id: String,
    val timestamp: Long,
    val district: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val networkType: NetworkType,
    val isConfirmed: Boolean = false,
    val userConfirmed: Boolean? = null,
    val pingResults: List<PingResult>,
    val deviceInfo: DeviceInfo,
    val status: ShutdownStatus = ShutdownStatus.SUSPECTED,
    val signalQuality: SignalQuality = SignalQuality.UNKNOWN
)

enum class NetworkType {
    WIFI, MOBILE_DATA, ETHERNET, UNKNOWN
}

enum class ShutdownStatus {
    SUSPECTED, CONFIRMED, FALSE_ALARM, RESOLVED
}

enum class SignalQuality {
    EXCELLENT, GOOD, FAIR, POOR, UNKNOWN
}

data class PingResult(
    val timestamp: Long,
    val success: Boolean,
    @SerializedName("response_time")
    val responseTime: Long?,
    val target: String,
    @SerializedName("packet_loss")
    val packetLoss: Float = 0.0f,
    val jitter: Long? = null,
    @SerializedName("min_response_time")
    val minResponseTime: Long? = null,
    @SerializedName("max_response_time")
    val maxResponseTime: Long? = null,
    @SerializedName("avg_response_time")
    val avgResponseTime: Long? = null,
    @SerializedName("total_packets_sent")
    val totalPacketsSent: Int = 1,
    @SerializedName("total_packets_received")
    val totalPacketsReceived: Int = 0
)

data class DeviceInfo(
    val androidVersion: String,
    val deviceModel: String,
    val carrier: String?,
    val signalStrength: Int?,
    val batteryLevel: Int? = null
)

data class ConnectivityStatus(
    val isConnected: Boolean,
    val networkType: NetworkType,
    val hasInternet: Boolean,
    val signalStrength: Int?,
    val carrier: String?,
    val signalQuality: SignalQuality,
    val timestamp: Long = System.currentTimeMillis()
)

data class NetworkCheckResult(
    val connectivityStatus: ConnectivityStatus,
    val pingResults: List<PingResult>,
    val isShutdownSuspected: Boolean,
    val confidence: Float,
    val isPoorSignal: Boolean = false,
    val isActualShutdown: Boolean = false
)