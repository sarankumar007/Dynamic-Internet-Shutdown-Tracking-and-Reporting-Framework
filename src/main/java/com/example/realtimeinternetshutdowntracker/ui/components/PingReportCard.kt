package com.example.realtimeinternetshutdowntracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realtimeinternetshutdowntracker.data.PingReportResponse
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PingReportCard(
    report: PingReportResponse,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = when {
                            report.confirmedShutdown -> listOf(
                                Color(0xFFFF5252),
                                Color(0xFFFF1744)
                            )
                            report.status == "SUSPECTED" -> listOf(
                                Color(0xFFFF9800),
                                Color(0xFFFF6F00)
                            )
                            else -> listOf(
                                Color(0xFF42A5F5),
                                Color(0xFF2196F3)
                            )
                        }
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (report.confirmedShutdown) Icons.Default.Warning
                            else if (report.status == "SUSPECTED") Icons.Default.ReportProblem
                            else Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Report #${report.id.take(8)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    StatusBadge(
                        status = report.status,
                        confirmed = report.confirmedShutdown
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = formatProbeTime(report.probeTime),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (report.pingResults.isNotEmpty()) {
                    PingResultsSummary(pingResults = report.pingResults)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "No ping results available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String?,
    confirmed: Boolean
) {
    val pair = when {
        confirmed -> Pair("✓ Confirmed", Color(0xFFFF1744))
        status == "SUSPECTED" -> Pair("⚠ Suspected", Color(0xFFFF9800))
        else -> Pair(status ?: "Unknown", Color(0xFF9E9E9E))
    }
    val badgeText = pair.first
    val badgeColor = pair.second
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.25f),
        modifier = Modifier
            .background(
                color = badgeColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = badgeText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PingResultsSummary(pingResults: List<com.example.realtimeinternetshutdowntracker.data.ApiPingResult>) {
    val totalPings = pingResults.size
    val successfulPings = pingResults.count { it.success }
    val failedPings = totalPings - successfulPings
    val avgPacketLoss = pingResults.mapNotNull { it.packetLoss }.average()
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ping Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PingStatCard(
                    label = "Total",
                    value = totalPings.toString(),
                    icon = Icons.Default.NetworkCheck,
                    modifier = Modifier.weight(1f)
                )
                
                PingStatCard(
                    label = "Success",
                    value = successfulPings.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                PingStatCard(
                    label = "Failed",
                    value = failedPings.toString(),
                    icon = Icons.Default.Error,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (avgPacketLoss > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Avg Packet Loss: ${(avgPacketLoss * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (pingResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                pingResults.take(3).forEach { pingResult ->
                    PingResultItem(pingResult = pingResult)
                    if (pingResult != pingResults.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (pingResults.size > 3) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+ ${pingResults.size - 3} more targets",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PingStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = Color(0xFF2196F3),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PingResultItem(pingResult: com.example.realtimeinternetshutdowntracker.data.ApiPingResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (pingResult.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (pingResult.success) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = pingResult.target ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                if (pingResult.responseTime != null) {
                    Text(
                        text = "${pingResult.responseTime.toInt()}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        if (pingResult.packetLoss != null && pingResult.packetLoss > 0) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFF5252).copy(alpha = 0.3f)
            ) {
                Text(
                    text = "${(pingResult.packetLoss * 100).toInt()}% loss",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatProbeTime(probeTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val date = inputFormat.parse(probeTime)
        if (date != null) {
            val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
            outputFormat.format(date)
        } else {
            probeTime
        }
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(probeTime)
            if (date != null) {
                val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
                outputFormat.format(date)
            } else {
                probeTime
            }
        } catch (e2: Exception) {
            probeTime
        }
    }
}

