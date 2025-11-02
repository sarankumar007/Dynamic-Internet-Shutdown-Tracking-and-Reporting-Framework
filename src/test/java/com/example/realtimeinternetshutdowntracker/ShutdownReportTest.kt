package com.example.realtimeinternetshutdowntracker

import com.example.realtimeinternetshutdowntracker.data.*
import org.junit.Test
import org.junit.Assert.*

class ShutdownReportTest {
    
    @Test
    fun testShutdownReportCreation() {
        val pingResults = listOf(
            PingResult(
                timestamp = System.currentTimeMillis(),
                success = false,
                responseTime = null,
                target = "8.8.8.8"
            )
        )
        
        val deviceInfo = DeviceInfo(
            androidVersion = "14",
            deviceModel = "Test Device",
            carrier = "Test Carrier",
            signalStrength = -70
        )
        
        val report = ShutdownReport(
            id = "test-id",
            timestamp = System.currentTimeMillis(),
            district = "Test District",
            state = "Test State",
            latitude = 0.0,
            longitude = 0.0,
            networkType = NetworkType.WIFI,
            isConfirmed = false,
            userConfirmed = null,
            pingResults = pingResults,
            deviceInfo = deviceInfo,
            status = ShutdownStatus.SUSPECTED
        )
        
        assertEquals("test-id", report.id)
        assertEquals("Test District", report.district)
        assertEquals("Test State", report.state)
        assertEquals(NetworkType.WIFI, report.networkType)
        assertEquals(ShutdownStatus.SUSPECTED, report.status)
        assertFalse(report.isConfirmed)
        assertNull(report.userConfirmed)
    }
    
    @Test
    fun testNetworkTypeEnum() {
        assertEquals(NetworkType.WIFI, NetworkType.valueOf("WIFI"))
        assertEquals(NetworkType.MOBILE_DATA, NetworkType.valueOf("MOBILE_DATA"))
        assertEquals(NetworkType.ETHERNET, NetworkType.valueOf("ETHERNET"))
        assertEquals(NetworkType.UNKNOWN, NetworkType.valueOf("UNKNOWN"))
    }
    
    @Test
    fun testShutdownStatusEnum() {
        assertEquals(ShutdownStatus.SUSPECTED, ShutdownStatus.valueOf("SUSPECTED"))
        assertEquals(ShutdownStatus.CONFIRMED, ShutdownStatus.valueOf("CONFIRMED"))
        assertEquals(ShutdownStatus.FALSE_ALARM, ShutdownStatus.valueOf("FALSE_ALARM"))
        assertEquals(ShutdownStatus.RESOLVED, ShutdownStatus.valueOf("RESOLVED"))
    }
}
