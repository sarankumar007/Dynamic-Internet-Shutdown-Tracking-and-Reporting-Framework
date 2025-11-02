package com.example.realtimeinternetshutdowntracker.repository

import android.content.Context
import com.example.realtimeinternetshutdowntracker.data.PingReportResponse
import com.example.realtimeinternetshutdowntracker.data.ShutdownReport
import com.example.realtimeinternetshutdowntracker.network.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShutdownRepository(private val context: Context) {
    
    private val apiService = ApiService.create()
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()
    
    suspend fun sendReportToBackend(report: ShutdownReport): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                println("🌐 === API CALL: Sending Shutdown Report ===")
                println("📤 Report Details:")
                println("   - Report ID: ${report.id}")
                println("   - Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(report.timestamp))}")
                println("   - District: ${report.district}")
                println("   - State: ${report.state}")
                println("   - Network Type: ${report.networkType}")
                println("   - Status: ${report.status}")
                println("   - Signal Quality: ${report.signalQuality}")
                println("   - Is Confirmed: ${report.isConfirmed}")
                println("   - User Confirmed: ${report.userConfirmed}")
                println("   - Latitude: ${report.latitude}")
                println("   - Longitude: ${report.longitude}")
                println("   - Ping Results Count: ${report.pingResults.size}")
                report.pingResults.forEachIndexed { index, pingResult ->
                    println("   Ping ${index + 1}:")
                    println("     - Target: ${pingResult.target}")
                    println("     - Success: ${pingResult.success}")
                    println("     - Response Time: ${pingResult.responseTime}ms")
                    println("     - Packet Loss: ${(pingResult.packetLoss * 100).toInt()}%")
                    println("     - Jitter: ${pingResult.jitter}ms")
                    println("     - Min/Max/Avg Response Time: ${pingResult.minResponseTime}ms / ${pingResult.maxResponseTime}ms / ${pingResult.avgResponseTime}ms")
                    println("     - Packets Sent/Received: ${pingResult.totalPacketsSent} / ${pingResult.totalPacketsReceived}")
                }
                println("   - Device Model: ${report.deviceInfo.deviceModel}")
                println("   - Android Version: ${report.deviceInfo.androidVersion}")
                println("   - Carrier: ${report.deviceInfo.carrier}")
                println("   - Signal Strength: ${report.deviceInfo.signalStrength}")
                println("   - Battery Level: ${report.deviceInfo.batteryLevel}")
                
                println("\n📋 === COMPLETE JSON PAYLOAD (for FastAPI testing) ===")
                val jsonPayload = gson.toJson(report)
                println(jsonPayload)
                println("=== END OF JSON PAYLOAD ===\n")
                
                val response = apiService.submitShutdownReport(report)
                
                if (response.isSuccessful) {
                    println("✅ API Response: SUCCESS")
                    println("   - Response Code: ${response.code()}")
                    Result.success(Unit)
                } else {
                    println("❌ API Response: FAILED")
                    println("   - Response Code: ${response}")
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("❌ API Exception: ${e.message}")
                println("   - Exception Type: ${e.javaClass.simpleName}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun getShutdownReports(): Result<List<ShutdownReport>> {
        return withContext(Dispatchers.IO) {
            try {
                println("🌐 === API CALL: Getting Shutdown Reports ===")
                val response = apiService.getShutdownReports()
                if (response.isSuccessful) {
                    val reports = response.body() ?: emptyList()
                    println("✅ API Response: SUCCESS")
                    println("   - Response Code: ${response.code()}")
                    println("   - Reports Count: ${reports.size}")
                    Result.success(reports)
                } else {
                    println("❌ API Response: FAILED")
                    println("   - Response Code: ${response.code()}")
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("❌ API Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun getPingReports(): Result<List<PingReportResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                println("🌐 === API CALL: Getting Ping Reports from /ping ===")
                val response = apiService.getPingReports()
                if (response.isSuccessful) {
                    val reports = response.body() ?: emptyList()
                    println("✅ API Response: SUCCESS")
                    println("   - Response Code: ${response.code()}")
                    println("   - Reports Count: ${reports.size}")
                    Result.success(reports)
                } else {
                    println("❌ API Response: FAILED")
                    println("   - Response Code: ${response.code()}")
                    println("   - Error Body: ${response.errorBody()?.string()}")
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("❌ API Exception: ${e.message}")
                println("   - Exception Type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateReportStatus(reportId: String, status: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                println("🌐 === API CALL: Updating Report Status ===")
                println("📤 Update Details:")
                println("   - Report ID: $reportId")
                println("   - New Status: $status")
                
                val response = apiService.updateReportStatus(reportId, status)
                if (response.isSuccessful) {
                    println("✅ API Response: SUCCESS")
                    println("   - Response Code: ${response.code()}")
                    Result.success(Unit)
                } else {
                    println("❌ API Response: FAILED")
                    println("   - Response Code: ${response.code()}")
                    Result.failure(Exception("API Error: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("❌ API Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }
}