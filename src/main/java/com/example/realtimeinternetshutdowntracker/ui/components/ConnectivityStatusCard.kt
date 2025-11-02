package com.example.realtimeinternetshutdowntracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realtimeinternetshutdowntracker.data.NetworkType

@Composable
fun ConnectivityStatusCard(
    isConnected: Boolean,
    networkType: NetworkType,
    lastCheckTime: String,
    hasInternet: Boolean = false,
    signalStrength: Int? = null,
    carrier: String? = null,
    isShutdownSuspected: Boolean = false,
    confidence: Float = 0.0f,
    isPoorSignal: Boolean = false,
    isActualShutdown: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isActualShutdown -> MaterialTheme.colorScheme.errorContainer
                isPoorSignal -> MaterialTheme.colorScheme.tertiaryContainer
                isConnected && hasInternet -> MaterialTheme.colorScheme.primaryContainer
                isConnected && !hasInternet -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        isActualShutdown -> Icons.Default.Warning
                        isPoorSignal -> Icons.Default.WifiOff
                        isConnected && hasInternet -> Icons.Default.Wifi
                        isConnected && !hasInternet -> Icons.Default.WifiOff
                        else -> Icons.Default.WifiOff
                    },
                    contentDescription = null,
                    tint = when {
                        isActualShutdown -> Color(0xFFFF9800) // Orange - Actual shutdown
                        isPoorSignal -> Color(0xFF9C27B0) // Purple - Poor signal
                        isConnected && hasInternet -> Color.Green
                        isConnected && !hasInternet -> Color(0xFFFF9800) // Orange
                        else -> Color.Red
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = when {
                            isActualShutdown -> "Internet Shutdown Detected!"
                            isPoorSignal -> "Poor Signal - No Internet"
                            isConnected && hasInternet -> "Connected"
                            isConnected && !hasInternet -> "Connected (No Internet)"
                            else -> "Disconnected"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isActualShutdown -> Color(0xFFFF9800)
                            isPoorSignal -> Color(0xFF9C27B0)
                            isConnected && hasInternet -> Color.Green
                            isConnected && !hasInternet -> Color(0xFFFF9800)
                            else -> Color.Red
                        }
                    )
                    
                    Text(
                        text = "Network: ${networkType.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (carrier != null) {
                Text(
                    text = "Carrier: $carrier",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (signalStrength != null) {
                Text(
                    text = "Signal: ${signalStrength}dBm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isActualShutdown) {
                Text(
                    text = "Shutdown Confidence: ${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isPoorSignal) {
                Text(
                    text = "Poor Signal Quality - Not a shutdown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Last check: $lastCheckTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
