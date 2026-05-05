package com.guiagym.app.data.repository

import com.google.gson.Gson
import com.guiagym.app.data.network.ApiService
import com.guiagym.app.data.network.models.ApiError
import com.guiagym.app.data.network.models.EjercicioResponse
import okhttp3.ResponseBody

class EjercicioRepository(private val apiService: ApiService) {

    suspend fun getEjercicios(): Result<List<EjercicioResponse>> = runCatching {
        val response = apiService.getEjercicios()
        if (response.isSuccessful) response.body()!!
        else throw Exception(parseError(response.errorBody()))
    }

    suspend fun getEjercicio(id: Int): Result<EjercicioResponse> = runCatching {
        val response = apiService.getEjercicio(id)
        if (response.isSuccessful) response.body()!!
        else throw Exception(parseError(response.errorBody()))
    }

    private fun parseError(body: ResponseBody?): String = try {
        Gson().fromJson(body?.string(), ApiError::class.java).detail
    } catch (e: Exception) { "Error desconocido" }
}
