package com.vlog.my.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 日志拦截器
 * 用于记录所有的 API 请求和响应
 */
class LoggingInterceptor : Interceptor {
    private val TAG = "API_REQUEST_DEBUG"
    private val UTF8 = StandardCharsets.UTF_8

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 记录请求信息
        val requestBody = request.body
        val hasRequestBody = requestBody != null

        val requestMessage = StringBuilder()
        requestMessage.append("--> ${request.method} ${request.url}")

        if (hasRequestBody) {
            requestBody?.contentType()?.let {
                requestMessage.append(" (${it})")
            }
        }

        requestMessage.append("\n")

        if (hasRequestBody) {
            val buffer = Buffer()
            requestBody?.writeTo(buffer)

            val charset = requestBody?.contentType()?.charset(UTF8) ?: UTF8
            val bodyString = buffer.readString(charset)

            requestMessage.append("Body: $bodyString\n")
        }

        requestMessage.append("--> END ${request.method}")
        Log.d(TAG, requestMessage.toString())

        // 执行请求
        val startTime = System.currentTimeMillis()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "<-- HTTP FAILED: ${e.message}")
            throw e
        }
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // 记录响应信息
        val responseBody = response.body
        val contentLength = responseBody?.contentLength() ?: 0

        val responseMessage = StringBuilder()
        responseMessage.append("<-- ${response.code} ${response.message} ${response.request.url} (${duration}ms)")
        responseMessage.append("\n")

        // 记录响应头
        response.headers.forEach { (name, value) ->
            responseMessage.append("$name: $value\n")
        }

        // 记录响应体
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer = source.buffer

            val charset = responseBody.contentType()?.charset(UTF8) ?: UTF8

            if (contentLength != 0L) {
                val bodyString = buffer.clone().readString(charset)
                responseMessage.append("Body: $bodyString\n")
            }
        }

        responseMessage.append("<-- END HTTP")
        Log.d(TAG, responseMessage.toString())

        return response
    }
}
