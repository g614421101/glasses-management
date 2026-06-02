package com.glasses.native.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.glasses.native.di.dataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            context.dataStore.data.firstOrNull()?.get(TOKEN_KEY)
        }

        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .header("Authorization", token)
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
