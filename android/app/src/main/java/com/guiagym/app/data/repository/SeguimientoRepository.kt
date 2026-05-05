package com.guiagym.app.data.repository

import com.google.gson.Gson
import com.guiagym.app.data.network.ApiService
import com.guiagym.app.data.network.models.ApiError
import com.guiagym.app.data.network.models.SeguimientoPesoCreateRequest
import com.guiagym.app.data.network.models.SeguimientoPesoResponse
import okhttp3.ResponseBody

class SeguimientoRepository(private val apiService: ApiService) {

    suspend fun getSeguimiento(): Result<List<SeguimientoPesoResponse>> = runCatching {
        val response = apiService.getSeguimiento()
        if (response.isSuccessful) response.body()!!
        else throw Exception(parseError(response.errorBody()))
    }

    suspend fun registrarPeso(peso: Double, notas: String?): Result<Unit> = runCatching {
        val response = apiService.registrarPeso(SeguimientoPesoCreateRequest(peso = peso, notas = notas))
        if (!response.isSuccessful) throw Exception(parseError(response.errorBody()))
    }

    private fun parseError(body: ResponseBody?): String = try {
        Gson().fromJson(body?.string(), ApiError::class.java).detail
    } catch (e: Exception) { "Error desconocido" }
}
