package com.example.realtimeinternetshutdowntracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.realtimeinternetshutdowntracker.repository.ShutdownRepository
import com.example.realtimeinternetshutdowntracker.service.ShutdownMonitoringService
import com.example.realtimeinternetshutdowntracker.ui.screens.HomeScreen
import com.example.realtimeinternetshutdowntracker.ui.theme.RealTimeInternetShutdownTrackerTheme
import com.example.realtimeinternetshutdowntracker.viewmodel.ShutdownViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start the monitoring service
        startMonitoringService()
        
        setContent {
            RealTimeInternetShutdownTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        viewModel = ShutdownViewModel(ShutdownRepository(this), this)
                    )
                }
            }
        }
    }
    
    private fun startMonitoringService() {
        val serviceIntent = Intent(this, ShutdownMonitoringService::class.java)
        startForegroundService(serviceIntent)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RealTimeInternetShutdownTrackerTheme {
        HomeScreen(
            viewModel = ShutdownViewModel(ShutdownRepository(LocalContext.current), LocalContext.current)
        )
    }
}