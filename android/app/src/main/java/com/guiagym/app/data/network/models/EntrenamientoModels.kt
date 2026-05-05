package com.guiagym.app.data.network.models

import com.google.gson.annotations.SerializedName

data class EntrenamientoListResponse(
    val id: Int,
    @SerializedName("usuario_id")   val usuarioId: Int,
    @SerializedName("rutina_id")    val rutinaId: Int?,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin")    val fechaFin: String?,
    val notas: String?,
)

data class EntrenamientoDetailResponse(
    val id: Int,
    @SerializedName("usuario_id")   val usuarioId: Int,
    @SerializedName("rutina_id")    val rutinaId: Int?,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin")    val fechaFin: String?,
    val notas: String?,
    val registros: List<RegistroEjercicioResponse>,
)

data class RegistroEjercicioResponse(
    val id: Int,
    @SerializedName("ejercicio_id")   val ejercicioId: Int,
    @SerializedName("serie_numero")   val serieNumero: Int,
    val repeticiones: Int?,
    val peso: Double?,
    @SerializedName("tiempo_segundos") val tiempoSegundos: Int?,
    val ejercicio: EjercicioResponse,
)

data class EntrenamientoCreateRequest(
    @SerializedName("rutina_id") val rutinaId: Int? = null,
    val notas: String? = null,
)

data class EntrenamientoFinalizarRequest(
    val notas: String? = null,
)

data class RegistroEjercicioCreateRequest(
    @SerializedName("ejercicio_id")  val ejercicioId: Int,
    @SerializedName("serie_numero")  val serieNumero: Int,
    val repeticiones: Int?,
    val peso: Double?,
)
