package com.example.aranimallayout.network

import com.example.aranimallayout.network.models.DetectionResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UltralyticsApi {
    @Multipart
    @POST("/")
    fun detectObjects(
        @Header("x-api-key") apiKey: String,
        @Part model: MultipartBody.Part,  // Removed "model"
        @Part imgsz: MultipartBody.Part,  // Removed "imgsz"
        @Part conf: MultipartBody.Part,   // Removed "conf"
        @Part iou: MultipartBody.Part,    // Removed "iou"
        @Part file: MultipartBody.Part   // Removed "file"
    ): Call<DetectionResponse> // Adjust based on your API response
}