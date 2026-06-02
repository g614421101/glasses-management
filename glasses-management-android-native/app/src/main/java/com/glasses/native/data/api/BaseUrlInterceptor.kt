package com.glasses.native.data.api

import com.glasses.native.discovery.ConnectionManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor(private val connectionManager: ConnectionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val baseUrl = connectionManager.baseUrl
        if (baseUrl != null) {
            val newBase = "$baseUrl/".toHttpUrlOrNull()
            if (newBase != null) {
                val original = chain.request()
                val newUrl = original.url.newBuilder()
                    .scheme(newBase.scheme)
                    .host(newBase.host)
                    .port(newBase.port)
                    .build()
                val newRequest = original.newBuilder().url(newUrl).build()
                return chain.proceed(newRequest)
            }
        }
        return chain.proceed(chain.request())
    }
}
