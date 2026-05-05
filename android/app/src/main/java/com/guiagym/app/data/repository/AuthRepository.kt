package com.guiagym.app.data.repository

import com.google.gson.Gson
import com.guiagym.app.data.local.TokenDataStore
import com.guiagym.app.data.network.ApiService
import com.guiagym.app.data.network.models.ApiError
import com.guiagym.app.data.network.models.LoginRequest
import com.guiagym.app.data.network.models.RegisterRequest
import com.guiagym.app.data.network.models.UsuarioResponse
import com.guiagym.app.data.network.models.UsuarioUpdateRequest
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody

class AuthRepository(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore,
) {
    val tokenFlow: Flow<String?> = tokenDataStore.token

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val r = apiService.login(LoginRequest(email, password))
        if (r.isSuccessful) tokenDataStore.saveToken(r.body()!!.accessToken)
        else throw Exception(parseError(r.errorBody()))
    }

    suspend fun register(nombre: String, email: String, password: String): Result<Unit> = runCatching {
        val r = apiService.register(RegisterRequest(nombre, email, password))
        if (!r.isSuccessful) throw Exception(parseError(r.errorBody()))
    }

    suspend fun getMe(): Result<UsuarioResponse> = runCatching {
        val r = apiService.getMe()
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    suspend fun updateMe(nombre: String?, pesoInicial: Double?, altura: Double?): Result<UsuarioResponse> = runCatching {
        val r = apiService.updateMe(UsuarioUpdateRequest(nombre, pesoInicial, altura))
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    suspend fun logout() = tokenDataStore.clearToken()

    private fun parseError(body: ResponseBody?): String = try {
        Gson().fromJson(body?.string(), ApiError::class.java).detail
    } catch (e: Exception) { "Error desconocido" }
}
