package com.example.realtimeinternetshutdowntracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.realtimeinternetshutdowntracker.utils.PermissionHandler

@Composable
fun PermissionRequestDialog(
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permissions Required",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("This app needs the following permissions to monitor internet connectivity:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Location access - to aggregate reports by district")
                Text("• Network state - to monitor WiFi and mobile data")
                Text("• Phone state - to detect mobile signal strength")
                Text("• Notifications - to alert you about suspected shutdowns")
            }
        },
        confirmButton = {
            TextButton(onClick = onRequestPermissions) {
                Text("Grant Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
fun PermissionStatusCard(
    hasLocationPermission: Boolean,
    hasNetworkPermission: Boolean,
    hasPhonePermission: Boolean,
    hasNotificationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasLocationPermission && hasNetworkPermission && hasPhonePermission) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Permission Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PermissionItem(
                name = "Location",
                granted = hasLocationPermission,
                description = "Required for district-level aggregation"
            )
            
            PermissionItem(
                name = "Network State",
                granted = hasNetworkPermission,
                description = "Required for WiFi/mobile monitoring"
            )
            
            PermissionItem(
                name = "Phone State",
                granted = hasPhonePermission,
                description = "Required for mobile signal strength"
            )
            
            PermissionItem(
                name = "Notifications",
                granted = hasNotificationPermission,
                description = "Required for shutdown alerts"
            )
        }
    }
}

@Composable
private fun PermissionItem(
    name: String,
    granted: Boolean,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $name: ${if (granted) "✓ Granted" else "✗ Denied"}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (granted) 
                MaterialTheme.colorScheme.onPrimaryContainer 
            else 
                MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
