package com.guiagym.app.data.network

import com.guiagym.app.data.local.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 10.0.2.2 es el alias de localhost del emulador Android
private const val BASE_URL = "http://10.0.2.2:8000/"

fun buildRetrofit(tokenDataStore: TokenDataStore): ApiService {
    val authInterceptor = Interceptor { chain ->
        val token = runBlocking { tokenDataStore.token.first() }
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
