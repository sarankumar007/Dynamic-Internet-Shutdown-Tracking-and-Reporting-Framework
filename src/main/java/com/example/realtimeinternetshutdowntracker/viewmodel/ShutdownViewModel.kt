package com.example.realtimeinternetshutdowntracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimeinternetshutdowntracker.data.*
import com.example.realtimeinternetshutdowntracker.location.LocationService
import com.example.realtimeinternetshutdowntracker.network.ConnectivityMonitor
import com.example.realtimeinternetshutdowntracker.repository.ShutdownRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ShutdownViewModel(
    private val repository: ShutdownRepository,
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ShutdownUiState())
    val uiState: StateFlow<ShutdownUiState> = _uiState.asStateFlow()
    
    private val connectivityMonitor = ConnectivityMonitor(context)
    private val locationService = LocationService(context)
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()
    
    init {
        loadShutdownReports()
        startConnectivityMonitoring()
    }
    
    private fun loadShutdownReports() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = repository.getPingReports()
                result.fold(
                    onSuccess = { pingReports ->
                        _uiState.value = _uiState.value.copy(
                            pingReports = pingReports,
                            isLoading = false
                        )
                        println("✅ Loaded ${pingReports.size} ping reports")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load reports: ${error.message}"
                        )
                        println("❌ Failed to load ping reports: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading reports: ${e.message}"
                )
            }
        }
    }
    
            private fun startConnectivityMonitoring() {
                println("🔄 Starting UI connectivity monitoring...")
                viewModelScope.launch {
                    while (true) {
                        try {
                            println("📱 UI Monitoring cycle - checking connectivity...")
                            val result = connectivityMonitor.checkConnectivity()
                            val connectivityStatus = result.connectivityStatus

                            _uiState.value = _uiState.value.copy(
                                isConnected = connectivityStatus.isConnected,
                                networkType = connectivityStatus.networkType,
                                hasInternet = connectivityStatus.hasInternet,
                                signalStrength = connectivityStatus.signalStrength,
                                carrier = connectivityStatus.carrier,
                                lastCheckTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                                isShutdownSuspected = result.isShutdownSuspected,
                                confidence = result.confidence,
                                isPoorSignal = result.isPoorSignal,
                                isActualShutdown = result.isActualShutdown
                            )

                            println("📊 UI State Updated:")
                            println("   - Connected: ${connectivityStatus.isConnected}")
                            println("   - Has Internet: ${connectivityStatus.hasInternet}")
                            println("   - Shutdown Suspected: ${result.isShutdownSuspected}")
                            println("   - Confidence: ${(result.confidence * 100).toInt()}%")

                            if (result.isShutdownSuspected && result.confidence > 0.7f) {
                                println("🚨 UI detected shutdown - handling...")
                                handleSuspectedShutdown(result)
                            }

                        } catch (e: Exception) {
                            println("❌ UI monitoring error: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                lastCheckTime = "Error: ${e.message}"
                            )
                        }

                        kotlinx.coroutines.delay(30000)
                    }
                }
            }
    
    private suspend fun handleSuspectedShutdown(result: NetworkCheckResult) {
        val report = createShutdownReport(result)
        
        println("\n📋 === VIEWMODEL: COMPLETE JSON PAYLOAD (for FastAPI testing) ===")
        val jsonPayload = gson.toJson(report)
        println(jsonPayload)
        println("=== END OF JSON PAYLOAD ===\n")
        
        val apiResult = repository.sendReportToBackend(report)
        
        _uiState.value = _uiState.value.copy(
            isShutdownSuspected = true,
            lastShutdownReport = report,
            error = if (apiResult.isFailure) "Failed to send report: ${apiResult.exceptionOrNull()?.message}" else null
        )
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
                androidVersion = android.os.Build.VERSION.RELEASE,
                deviceModel = android.os.Build.MODEL,
                carrier = result.connectivityStatus.carrier,
                signalStrength = result.connectivityStatus.signalStrength
            ),
            signalQuality = result.connectivityStatus.signalQuality
        )
    }
    
    fun confirmShutdown(reportId: String, confirmed: Boolean) {
        viewModelScope.launch {
            val status = if (confirmed) "confirmed" else "false_alarm"
            val result = repository.updateReportStatus(reportId, status)
            
            result.fold(
                onSuccess = {
                    loadShutdownReports()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update report: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun refreshData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadShutdownReports()
    }
}

data class ShutdownUiState(
    val isLoading: Boolean = true,
    val shutdownReports: List<ShutdownReport> = emptyList(),
    val pingReports: List<PingReportResponse> = emptyList(),
    val isConnected: Boolean = false,
    val networkType: NetworkType = NetworkType.UNKNOWN,
    val hasInternet: Boolean = false,
    val signalStrength: Int? = null,
    val carrier: String? = null,
    val lastCheckTime: String = "",
    val isShutdownSuspected: Boolean = false,
    val confidence: Float = 0.0f,
    val lastShutdownReport: ShutdownReport? = null,
    val error: String? = null,
    val isPoorSignal: Boolean = false,
    val isActualShutdown: Boolean = false
)
