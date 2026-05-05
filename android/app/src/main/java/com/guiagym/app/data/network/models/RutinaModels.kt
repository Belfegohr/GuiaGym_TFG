package com.guiagym.app.data.network.models

import com.google.gson.annotations.SerializedName

data class RutinaListResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("usuario_id")     val usuarioId: Int,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    val activo: Boolean,
    val publica: Boolean,
)

data class RutinaDetailResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("usuario_id")     val usuarioId: Int,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    val activo: Boolean,
    val publica: Boolean,
    val ejercicios: List<RutinaEjercicioResponse>,
)

data class RutinaEjercicioResponse(
    val id: Int,
    @SerializedName("ejercicio_id")       val ejercicioId: Int,
    val series: Int,
    val repeticiones: Int,
    @SerializedName("peso_sugerido")      val pesoSugerido: Double?,
    @SerializedName("descanso_segundos")  val descansoSegundos: Int,
    val orden: Int,
    @SerializedName("dia_semana")         val diaSemana: String?,
    val ejercicio: EjercicioResponse,
)

data class RutinaCreateRequest(
    val nombre: String,
    val descripcion: String? = null,
    val publica: Boolean = false,
)

data class RutinaEjercicioCreateRequest(
    @SerializedName("ejercicio_id")      val ejercicioId: Int,
    val series: Int,
    val repeticiones: Int,
    @SerializedName("peso_sugerido")     val pesoSugerido: Double? = null,
    @SerializedName("descanso_segundos") val descansoSegundos: Int,
    val orden: Int = 0,
    @SerializedName("dia_semana")        val diaSemana: String? = null,
)
