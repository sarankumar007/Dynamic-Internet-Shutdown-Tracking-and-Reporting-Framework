package com.example.realtimeinternetshutdowntracker.network

import com.example.realtimeinternetshutdowntracker.data.PingReportResponse
import com.example.realtimeinternetshutdowntracker.data.ShutdownReport
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    
    @POST("ping_report")
    suspend fun submitShutdownReport(@Body report: ShutdownReport): Response<ShutdownReport>
    
    @GET("ping_report")
    suspend fun getShutdownReports(): Response<List<ShutdownReport>>
    
    @GET("ping")
    suspend fun getPingReports(): Response<List<PingReportResponse>>
    
    @PUT("ping_report/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") reportId: String, 
        @Query("status") status: String
    ): Response<Unit>
    
    companion object {
        private const val BASE_URL = "https://sflc-hackathon-2.onrender.com/"
        
        fun create(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            return retrofit.create(ApiService::class.java)
        }
    }
}
