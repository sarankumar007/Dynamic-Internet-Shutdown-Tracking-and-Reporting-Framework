package com.example.realtimeinternetshutdowntracker.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.realtimeinternetshutdowntracker.data.ShutdownReport
import com.example.realtimeinternetshutdowntracker.data.ShutdownStatus
import com.example.realtimeinternetshutdowntracker.ui.components.ShutdownReportCard
import com.example.realtimeinternetshutdowntracker.ui.components.PingReportCard
import com.example.realtimeinternetshutdowntracker.ui.components.ConnectivityStatusCard
import com.example.realtimeinternetshutdowntracker.ui.components.PermissionRequestDialog
import com.example.realtimeinternetshutdowntracker.ui.components.PermissionStatusCard
import com.example.realtimeinternetshutdowntracker.utils.PermissionHandler
import com.example.realtimeinternetshutdowntracker.viewmodel.ShutdownViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShutdownViewModel = viewModel(),
    onNavigateToDetails: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val hasLocationPermission = PermissionHandler.hasLocationPermission(context)
    val hasNetworkPermission = PermissionHandler.hasNetworkPermissions(context)
    val hasPhonePermission = PermissionHandler.hasPhoneStatePermission(context)
    val hasNotificationPermission = PermissionHandler.hasNotificationPermission(context)
    
    val showPermissionDialog = remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        showPermissionDialog.value = false
    }
    
    if (showPermissionDialog.value) {
        PermissionRequestDialog(
            onRequestPermissions = {
                permissionLauncher.launch(PermissionHandler.requiredPermissions)
            },
            onDismiss = { showPermissionDialog.value = false }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Internet Shutdown Tracker") },
                actions = {
                    if (!hasLocationPermission || !hasNetworkPermission || !hasPhonePermission) {
                        TextButton(
                            onClick = { showPermissionDialog.value = true }
                        ) {
                            Text("Permissions")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            PermissionStatusCard(
                hasLocationPermission = hasLocationPermission,
                hasNetworkPermission = hasNetworkPermission,
                hasPhonePermission = hasPhonePermission,
                hasNotificationPermission = hasNotificationPermission
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ConnectivityStatusCard(
                isConnected = uiState.isConnected,
                networkType = uiState.networkType,
                lastCheckTime = uiState.lastCheckTime,
                hasInternet = uiState.hasInternet,
                signalStrength = uiState.signalStrength,
                carrier = uiState.carrier,
                isShutdownSuspected = uiState.isShutdownSuspected,
                confidence = uiState.confidence,
                isPoorSignal = uiState.isPoorSignal,
                isActualShutdown = uiState.isActualShutdown
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shutdown Reports",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { viewModel.refreshData() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.pingReports.isEmpty() && uiState.shutdownReports.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No reports found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "The app will automatically detect and report internet shutdowns",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.pingReports) { report ->
                            PingReportCard(
                                report = report,
                                onClick = { onNavigateToDetails(report.id) }
                            )
                        }
                        
                        items(uiState.shutdownReports) { report ->
                            ShutdownReportCard(
                                report = report,
                                onConfirm = { viewModel.confirmShutdown(report.id, true) },
                                onDeny = { viewModel.confirmShutdown(report.id, false) },
                                onClick = { onNavigateToDetails(report.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
