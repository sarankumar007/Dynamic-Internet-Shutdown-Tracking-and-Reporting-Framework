package com.example.realtimeinternetshutdowntracker.data

import com.google.gson.annotations.SerializedName


data class PingReportResponse(
    val id: String,
    @SerializedName("probe_time")
    val probeTime: String,
    @SerializedName("confirmed_shutdown")
    val confirmedShutdown: Boolean,
    val status: String?,
    @SerializedName("ping_results")
    val pingResults: List<ApiPingResult>
)


data class ApiPingResult(
    val timestamp: Long,
    val success: Boolean,
    val target: String?,
    @SerializedName("response_time")
    val responseTime: Double? = null,
    @SerializedName("packet_loss")
    val packetLoss: Double? = null,
    val jitter: Double? = null,
    @SerializedName("min_response_time")
    val minResponseTime: Double? = null,
    @SerializedName("max_response_time")
    val maxResponseTime: Double? = null,
    @SerializedName("avg_response_time")
    val avgResponseTime: Double? = null,
    @SerializedName("total_packets_sent")
    val totalPacketsSent: Int? = null,
    @SerializedName("total_packets_received")
    val totalPacketsReceived: Int? = null
)

