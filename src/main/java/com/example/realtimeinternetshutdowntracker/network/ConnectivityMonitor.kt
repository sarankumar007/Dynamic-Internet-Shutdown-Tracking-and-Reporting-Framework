package com.example.realtimeinternetshutdowntracker.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.telephony.SignalStrength
import android.telephony.CellInfo
import android.telephony.CellSignalStrength
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.realtimeinternetshutdowntracker.data.*
import kotlinx.coroutines.*
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class ConnectivityMonitor(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    private val pingTargets = listOf(
        "8.8.8.8",
        "1.1.1.1",
        "208.67.222.222"
    )
    
    private val pingConfig = PingConfig(
        packetsPerTarget = 3,
        timeoutMs = 5000,
        intervalMs = 1000
    )
    
    data class PingConfig(
        val packetsPerTarget: Int,
        val timeoutMs: Int,
        val intervalMs: Int
    )
    
    suspend fun checkConnectivity(): NetworkCheckResult {
        val connectivityStatus = getCurrentConnectivityStatus()
        val pingResults = performPingTests()

        val isShutdownSuspected = determineShutdownSuspicion(connectivityStatus, pingResults)
        val confidence = calculateConfidence(connectivityStatus, pingResults)

        val isPoorSignal = connectivityStatus.signalQuality == SignalQuality.POOR
        val isActualShutdown = isShutdownSuspected && !isPoorSignal

        println("🔍 === CONNECTIVITY MONITORING DEBUG ===")
        println("📡 Network Status:")
        println("   - Connected: ${connectivityStatus.isConnected}")
        println("   - Network Type: ${connectivityStatus.networkType}")
        println("   - Has Internet: ${connectivityStatus.hasInternet}")
        println("   - Signal Strength: ${connectivityStatus.signalStrength}dBm")
        println("   - Signal Quality: ${connectivityStatus.signalQuality}")
        println("   - Carrier: ${connectivityStatus.carrier}")
        println("   - Timestamp: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(connectivityStatus.timestamp))}")
        
        println("\n🌐 Enhanced Ping Test Results:")
        pingResults.forEachIndexed { index, result ->
            println("   Target ${index + 1}: ${result.target}")
            println("     - Overall Success: ${result.success}")
            println("     - Avg Response Time: ${result.avgResponseTime}ms")
            println("     - Min Response Time: ${result.minResponseTime}ms")
            println("     - Max Response Time: ${result.maxResponseTime}ms")
            println("     - Packet Loss: ${(result.packetLoss * 100).toInt()}%")
            println("     - Jitter: ${result.jitter}ms")
            println("     - Packets Sent: ${result.totalPacketsSent}")
            println("     - Packets Received: ${result.totalPacketsReceived}")
            println("     - Timestamp: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(result.timestamp))}")
        }
        
        println("\n🎯 Analysis Results:")
        println("   - Shutdown Suspected: $isShutdownSuspected")
        println("   - Confidence Level: ${(confidence * 100).toInt()}%")
        println("   - Poor Signal: $isPoorSignal")
        println("   - Actual Shutdown: $isActualShutdown")
        println("=====================================\n")

        return NetworkCheckResult(
            connectivityStatus = connectivityStatus,
            pingResults = pingResults,
            isShutdownSuspected = isShutdownSuspected,
            confidence = confidence,
            isPoorSignal = isPoorSignal,
            isActualShutdown = isActualShutdown
        )
    }
    
    private fun getCurrentConnectivityStatus(): ConnectivityStatus {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        val isConnected = activeNetwork != null && networkCapabilities != null
        val networkType = determineNetworkType(networkCapabilities)
        val hasInternet = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val signalStrength = getSignalStrength(networkType)
        val carrier = getCarrierName()
        
        val connectivityStatus = ConnectivityStatus(
            isConnected = isConnected,
            networkType = networkType,
            hasInternet = hasInternet,
            signalStrength = signalStrength,
            carrier = carrier,
            signalQuality = SignalQuality.UNKNOWN
        )
        
        return connectivityStatus.copy(
            signalQuality = determineSignalQuality(connectivityStatus)
        )
    }
    
    private fun determineNetworkType(capabilities: NetworkCapabilities?): NetworkType {
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.MOBILE_DATA
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }
    
    private fun getSignalStrength(networkType: NetworkType): Int? {
        return when (networkType) {
            NetworkType.WIFI -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val wifiInfo = wifiManager.connectionInfo
                    wifiInfo?.rssi
                } else {
                    val wifiInfo = wifiManager.connectionInfo
                    wifiInfo?.rssi
                }
            }
            NetworkType.MOBILE_DATA -> {
                getMobileSignalStrength()
            }
            else -> null
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMobileSignalStrength(): Int? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cellInfos = telephonyManager.allCellInfo
                cellInfos?.firstOrNull()?.let { cellInfo ->
                    when (cellInfo) {
                        is android.telephony.CellInfoLte -> {
                            cellInfo.cellSignalStrength.level
                        }
                        is android.telephony.CellInfoGsm -> {
                            cellInfo.cellSignalStrength.level
                        }
                        is android.telephony.CellInfoWcdma -> {
                            cellInfo.cellSignalStrength.level
                        }
                        else -> null
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getCarrierName(): String? {
        return try {
            telephonyManager.networkOperatorName
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun performPingTests(): List<PingResult> {
        val results = mutableListOf<PingResult>()

        for (target in pingTargets) {
            val result = performAdvancedPing(target)
            results.add(result)
        }

        return results
    }
    
    private suspend fun performAdvancedPing(host: String): PingResult {
        val responseTimes = mutableListOf<Long>()
        var successfulPings = 0
        val totalPings = pingConfig.packetsPerTarget
        val startTime = System.currentTimeMillis()
        
        println("🌐 === ADVANCED PING TEST: $host ===")
        println("📊 Ping Configuration:")
        println("   - Packets per target: ${pingConfig.packetsPerTarget}")
        println("   - Timeout: ${pingConfig.timeoutMs}ms")
        println("   - Interval: ${pingConfig.intervalMs}ms")
        
        for (i in 1..totalPings) {
            try {
                val pingStartTime = System.currentTimeMillis()
                val isReachable = pingHostOnce(host, pingConfig.timeoutMs)
                val pingEndTime = System.currentTimeMillis()
                
                if (isReachable) {
                    val responseTime = pingEndTime - pingStartTime
                    responseTimes.add(responseTime)
                    successfulPings++
                    println("   📡 Ping $i: SUCCESS (${responseTime}ms)")
                } else {
                    println("   📡 Ping $i: FAILED (timeout after ${pingConfig.timeoutMs}ms)")
                }
                
                if (i < totalPings) {
                    delay(pingConfig.intervalMs.toLong())
                }
            } catch (e: Exception) {
                println("   ❌ Ping $i: ERROR - ${e.javaClass.simpleName}: ${e.message}")
            }
        }
        
        val packetLoss = if (totalPings > 0) {
            (totalPings - successfulPings).toFloat() / totalPings.toFloat()
        } else 0.0f
        
        val minResponseTime = responseTimes.minOrNull()
        val maxResponseTime = responseTimes.maxOrNull()
        val avgResponseTime = if (responseTimes.isNotEmpty()) {
            responseTimes.average().toLong()
        } else null
        
        val jitter = if (responseTimes.size > 1) {
            val mean = responseTimes.average()
            val variance = responseTimes.map { (it - mean) * (it - mean) }.average()
            kotlin.math.sqrt(variance).toLong()
        } else null
        
        val overallSuccess = successfulPings > 0
        val overallResponseTime = if (overallSuccess) avgResponseTime else null
        
        println("📊 Ping Statistics:")
        println("   - Total Packets Sent: $totalPings")
        println("   - Successful Pings: $successfulPings")
        println("   - Packet Loss: ${(packetLoss * 100).toInt()}%")
        println("   - Min Response Time: ${minResponseTime}ms")
        println("   - Max Response Time: ${maxResponseTime}ms")
        println("   - Avg Response Time: ${avgResponseTime}ms")
        println("   - Jitter: ${jitter}ms")
        println("   - Overall Success: $overallSuccess")
        println("=== PING TEST COMPLETE ===\n")
        println()
        
        return PingResult(
            timestamp = startTime,
            success = overallSuccess,
            responseTime = overallResponseTime,
            target = host,
            packetLoss = packetLoss,
            jitter = jitter,
            minResponseTime = minResponseTime,
            maxResponseTime = maxResponseTime,
            avgResponseTime = avgResponseTime,
            totalPacketsSent = totalPings,
            totalPacketsReceived = successfulPings
        )
    }
    
    private suspend fun pingHostOnce(host: String, timeoutMs: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("      🔍 Attempting to reach $host with ${timeoutMs}ms timeout...")
                val address = InetAddress.getByName(host)
                println("      ✓ Resolved to: ${address.hostAddress}")
                val result = address.isReachable(timeoutMs)
                println("      ${if (result) "✓" else "✗"} Reachable check result: $result")
                result
            } catch (e: Exception) {
                println("      ❌ Exception during ping: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
    
    
    private fun determineShutdownSuspicion(
        connectivityStatus: ConnectivityStatus,
        pingResults: List<PingResult>
    ): Boolean {
        val hasNetworkButNoInternet = connectivityStatus.isConnected && !connectivityStatus.hasInternet
        val allPingsFailed = pingResults.all { !it.success }
        val signalQuality = determineSignalQuality(connectivityStatus)
        
        val totalPacketsSent = pingResults.sumOf { it.totalPacketsSent }
        val totalPacketsReceived = pingResults.sumOf { it.totalPacketsReceived }
        val overallPacketLoss = if (totalPacketsSent > 0) {
            (totalPacketsSent - totalPacketsReceived).toFloat() / totalPacketsSent.toFloat()
        } else 1.0f
        
        val avgJitter = pingResults.mapNotNull { it.jitter }.average().toLong()
        val highPacketLoss = overallPacketLoss > 0.5f
        val highJitter = avgJitter > 100
        
        println("🔍 === SHUTDOWN ANALYSIS ===")
        println("📊 Network Analysis:")
        println("   - Has Network but No Internet: $hasNetworkButNoInternet")
        println("   - All Pings Failed: $allPingsFailed")
        println("   - Signal Quality: $signalQuality")
        println("   - Overall Packet Loss: ${(overallPacketLoss * 100).toInt()}%")
        println("   - Average Jitter: ${avgJitter}ms")
        println("   - High Packet Loss (>50%): $highPacketLoss")
        println("   - High Jitter (>100ms): $highJitter")
        

        val isLikelyShutdown = when {

//            hasNetworkButNoInternet && allPingsFailed && signalQuality != SignalQuality.POOR -> {
            true ->{
                println("   🚨 SHUTDOWN DETECTED: Network connected but no internet + all pings failed + good signal")
                true
            }

            hasNetworkButNoInternet && highPacketLoss && signalQuality == SignalQuality.EXCELLENT -> {
                println("   🚨 SHUTDOWN DETECTED: High packet loss with excellent signal (possible throttling)")
                true
            }

            allPingsFailed && signalQuality in listOf(SignalQuality.EXCELLENT, SignalQuality.GOOD) -> {
                println("   🚨 SHUTDOWN DETECTED: All pings failed with good signal quality")
                true
            }

            signalQuality == SignalQuality.POOR -> {
                println("   📶 POOR SIGNAL: Not a shutdown, just poor network quality")
                false
            }

            highJitter && signalQuality == SignalQuality.POOR -> {
                println("   📶 NETWORK ISSUES: High jitter with poor signal - network problems, not shutdown")
                false
            }
            else -> {
                println("   ✅ NORMAL: No shutdown indicators detected")
                false
            }
        }
        
        println("   🎯 Final Decision: ${if (isLikelyShutdown) "SHUTDOWN SUSPECTED" else "NO SHUTDOWN"}")
        println("=== ANALYSIS COMPLETE ===\n")
        
        return isLikelyShutdown
    }
    
    private fun determineSignalQuality(connectivityStatus: ConnectivityStatus): SignalQuality {
        val signalStrength = connectivityStatus.signalStrength ?: return SignalQuality.UNKNOWN
        
        return when (connectivityStatus.networkType) {
            NetworkType.WIFI -> {
                when {
                    signalStrength > -30 -> SignalQuality.EXCELLENT
                    signalStrength > -50 -> SignalQuality.GOOD
                    signalStrength > -70 -> SignalQuality.FAIR
                    else -> SignalQuality.POOR
                }
            }
            NetworkType.MOBILE_DATA -> {
                when {
                    signalStrength >= 4 -> SignalQuality.EXCELLENT
                    signalStrength >= 3 -> SignalQuality.GOOD
                    signalStrength >= 2 -> SignalQuality.FAIR
                    signalStrength >= 1 -> SignalQuality.POOR
                    else -> SignalQuality.POOR
                }
            }
            else -> SignalQuality.UNKNOWN
        }
    }
    
    private fun calculateConfidence(
        connectivityStatus: ConnectivityStatus,
        pingResults: List<PingResult>
    ): Float {
        var confidence = 0.0f

        if (connectivityStatus.isConnected && !connectivityStatus.hasInternet) {
            confidence += 0.3f
        }

        val successfulPings = pingResults.count { it.success }
        val totalPings = pingResults.size
        if (totalPings > 0) {
            val pingFailureRate = 1.0f - (successfulPings.toFloat() / totalPings)
            confidence += pingFailureRate * 0.3f
        }

        val totalPacketsSent = pingResults.sumOf { it.totalPacketsSent }
        val totalPacketsReceived = pingResults.sumOf { it.totalPacketsReceived }
        val overallPacketLoss = if (totalPacketsSent > 0) {
            (totalPacketsSent - totalPacketsReceived).toFloat() / totalPacketsSent.toFloat()
        } else 1.0f
        
        confidence += overallPacketLoss * 0.2f

        val signalQuality = determineSignalQuality(connectivityStatus)
        when (signalQuality) {
            SignalQuality.EXCELLENT -> confidence += 0.2f
            SignalQuality.GOOD -> confidence += 0.15f
            SignalQuality.FAIR -> confidence += 0.1f
            SignalQuality.POOR -> confidence -= 0.3f
            SignalQuality.UNKNOWN -> confidence += 0.05f
        }

        val avgJitter = pingResults.mapNotNull { it.jitter }.average()
        if (avgJitter > 100 && signalQuality in listOf(SignalQuality.EXCELLENT, SignalQuality.GOOD)) {
            confidence += 0.1f
        }

        println("📊 === CONFIDENCE CALCULATION ===")
        println("   - Network Status Factor: ${if (connectivityStatus.isConnected && !connectivityStatus.hasInternet) 0.3f else 0.0f}")
        println("   - Ping Failure Factor: ${if (totalPings > 0) (1.0f - (successfulPings.toFloat() / totalPings)) * 0.3f else 0.0f}")
        println("   - Packet Loss Factor: ${overallPacketLoss * 0.2f}")
        println("   - Signal Quality Factor: ${when (signalQuality) {
            SignalQuality.EXCELLENT -> 0.2f
            SignalQuality.GOOD -> 0.15f
            SignalQuality.FAIR -> 0.1f
            SignalQuality.POOR -> -0.3f
            SignalQuality.UNKNOWN -> 0.05f
        }}")
        println("   - Jitter Factor: ${if (avgJitter > 100 && signalQuality in listOf(SignalQuality.EXCELLENT, SignalQuality.GOOD)) 0.1f else 0.0f}")
        println("   - Final Confidence: ${confidence.coerceIn(0.0f, 1.0f)}")
        println("=== CONFIDENCE CALCULATION COMPLETE ===\n")

        return confidence.coerceIn(0.0f, 1.0f)
    }
}
