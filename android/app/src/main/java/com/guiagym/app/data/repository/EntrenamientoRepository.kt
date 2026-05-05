package com.guiagym.app.data.repository

import com.google.gson.Gson
import com.guiagym.app.data.network.ApiService
import com.guiagym.app.data.network.models.ApiError
import com.guiagym.app.data.network.models.EntrenamientoCreateRequest
import com.guiagym.app.data.network.models.EntrenamientoDetailResponse
import com.guiagym.app.data.network.models.EntrenamientoFinalizarRequest
import com.guiagym.app.data.network.models.EntrenamientoListResponse
import com.guiagym.app.data.network.models.RegistroEjercicioCreateRequest
import com.guiagym.app.data.network.models.RegistroEjercicioResponse
import okhttp3.ResponseBody

class EntrenamientoRepository(private val apiService: ApiService) {

    suspend fun getEntrenamientos(): Result<List<EntrenamientoListResponse>> = runCatching {
        val r = apiService.getEntrenamientos()
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    suspend fun getDetail(id: Int): Result<EntrenamientoDetailResponse> = runCatching {
        val r = apiService.getEntrenamientoDetail(id)
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    suspend fun iniciarEntrenamiento(rutinaId: Int? = null): Result<EntrenamientoListResponse> = runCatching {
        val r = apiService.iniciarEntrenamiento(EntrenamientoCreateRequest(rutinaId))
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    suspend fun finalizarEntrenamiento(id: Int): Result<Unit> = runCatching {
        val r = apiService.finalizarEntrenamiento(id, EntrenamientoFinalizarRequest())
        if (!r.isSuccessful) throw Exception(parseError(r.errorBody()))
    }

    suspend fun addRegistro(entrenamientoId: Int, req: RegistroEjercicioCreateRequest): Result<RegistroEjercicioResponse> = runCatching {
        val r = apiService.addRegistro(entrenamientoId, req)
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    private fun parseError(body: ResponseBody?): String = try {
        Gson().fromJson(body?.string(), ApiError::class.java).detail
    } catch (e: Exception) { "Error desconocido" }
}
