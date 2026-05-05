package com.guiagym.app.data.repository

import com.google.gson.Gson
import com.guiagym.app.data.network.ApiService
import com.guiagym.app.data.network.models.ApiError
import com.guiagym.app.data.network.models.RutinaCreateRequest
import com.guiagym.app.data.network.models.RutinaDetailResponse
import com.guiagym.app.data.network.models.RutinaEjercicioCreateRequest
import com.guiagym.app.data.network.models.RutinaListResponse
import okhttp3.ResponseBody

class RutinaRepository(private val apiService: ApiService) {

    suspend fun getRutinas(): Result<List<RutinaListResponse>> = runCatching {
        val r = apiService.getRutinas()
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    suspend fun getRutinaDetail(id: Int): Result<RutinaDetailResponse> = runCatching {
        val r = apiService.getRutinaDetail(id)
        if (r.isSuccessful) r.body()!! else throw Exception(parseError(r.errorBody()))
    }

    suspend fun createRutina(nombre: String, descripcion: String?, publica: Boolean): Result<Unit> = runCatching {
        val r = apiService.createRutina(RutinaCreateRequest(nombre, descripcion, publica))
        if (!r.isSuccessful) throw Exception(parseError(r.errorBody()))
    }

    suspend fun addEjercicio(rutinaId: Int, req: RutinaEjercicioCreateRequest): Result<Unit> = runCatching {
        val r = apiService.addEjercicioToRutina(rutinaId, req)
        if (!r.isSuccessful) throw Exception(parseError(r.errorBody()))
    }

    suspend fun removeEjercicio(rutinaId: Int, reId: Int): Result<Unit> = runCatching {
        val r = apiService.removeEjercicioFromRutina(rutinaId, reId)
        if (!r.isSuccessful) throw Exception(parseError(r.errorBody()))
    }

    private fun parseError(body: ResponseBody?): String = try {
        Gson().fromJson(body?.string(), ApiError::class.java).detail
    } catch (e: Exception) { "Error desconocido" }
}
