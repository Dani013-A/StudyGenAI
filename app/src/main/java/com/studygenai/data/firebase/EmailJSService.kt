package com.studygenai.data.firebase

import android.util.Log
import com.studygenai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailJSService @Inject constructor() {

    private val client = OkHttpClient()

    suspend fun sendOtp(
        toEmail: String,
        toName: String,
        otpCode: String
    ): Result<Unit> = withContext(Dispatchers.IO) {  // ← runs on background thread
        try {
            val json = JSONObject().apply {
                put("service_id",  BuildConfig.EMAILJS_SERVICE_ID)
                put("template_id", BuildConfig.EMAILJS_TEMPLATE_ID)
                put("user_id",     BuildConfig.EMAILJS_PUBLIC_KEY)
                put("accessToken", BuildConfig.EMAILJS_PUBLIC_KEY)
                put("template_params", JSONObject().apply {
                    put("to_email", toEmail)
                    put("to_name",  toName)
                    put("otp_code", otpCode)
                })
            }

            Log.d("EMAILJS_DEBUG", "Service ID: ${BuildConfig.EMAILJS_SERVICE_ID}")
            Log.d("EMAILJS_DEBUG", "Request: ${json.toString(2)}")

            val body = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("origin", "http://localhost")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            Log.d("EMAILJS_DEBUG", "Response code: ${response.code}")
            Log.d("EMAILJS_DEBUG", "Response body: $responseBody")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("EmailJS ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Log.e("EMAILJS_DEBUG", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}