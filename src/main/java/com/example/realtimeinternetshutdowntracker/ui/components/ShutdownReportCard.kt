package com.example.realtimeinternetshutdowntracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realtimeinternetshutdowntracker.data.ShutdownReport
import com.example.realtimeinternetshutdowntracker.data.ShutdownStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShutdownReportCard(
    report: ShutdownReport,
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when (report.status) {
                ShutdownStatus.SUSPECTED -> MaterialTheme.colorScheme.tertiaryContainer
                ShutdownStatus.CONFIRMED -> MaterialTheme.colorScheme.errorContainer
                ShutdownStatus.FALSE_ALARM -> MaterialTheme.colorScheme.surfaceVariant
                ShutdownStatus.RESOLVED -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${report.district}, ${report.state}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                StatusChip(status = report.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Network: ${report.networkType.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Time: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(report.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (report.status == ShutdownStatus.SUSPECTED) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm")
                    }
                    
                    OutlinedButton(
                        onClick = onDeny,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deny")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: ShutdownStatus) {
        val (text, color) = when (status) {
        ShutdownStatus.SUSPECTED -> "Suspected" to Color(0xFFFF9800) // Orange
        ShutdownStatus.CONFIRMED -> "Confirmed" to Color(0xFFF44336) // Red
        ShutdownStatus.FALSE_ALARM -> "False Alarm" to Color(0xFF9E9E9E) // Gray
        ShutdownStatus.RESOLVED -> "Resolved" to Color(0xFF4CAF50) // Green
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
