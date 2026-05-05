package com.guiagym.app.data.network.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val nombre: String, val email: String, val password: String)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type")   val tokenType: String,
)

data class UsuarioResponse(
    val id: Int,
    val nombre: String,
    val email: String,
    @SerializedName("peso_inicial") val pesoInicial: Double?,
    val altura: Double?,
    val activo: Boolean,
)

data class UsuarioUpdateRequest(
    val nombre: String?,
    @SerializedName("peso_inicial") val pesoInicial: Double?,
    val altura: Double?,
)

data class ApiError(val detail: String)
