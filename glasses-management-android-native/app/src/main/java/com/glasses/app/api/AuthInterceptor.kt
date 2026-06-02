package com.glasses.app.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    companion object {
        @Volatile
        var token: String? = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val currentToken = token
        val request = if (!currentToken.isNullOrEmpty()) {
            chain.request().newBuilder()
                .header("Authorization", currentToken)
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
