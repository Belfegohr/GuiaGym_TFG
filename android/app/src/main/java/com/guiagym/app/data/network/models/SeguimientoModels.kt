package com.guiagym.app.data.network.models

import com.google.gson.annotations.SerializedName

data class SeguimientoPesoResponse(
    val id: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    val peso: Double,
    val fecha: String,
    val notas: String?,
)

data class SeguimientoPesoCreateRequest(
    val peso: Double,
    val fecha: String? = null,
    val notas: String? = null,
)
