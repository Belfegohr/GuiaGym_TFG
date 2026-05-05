package com.guiagym.app.data.network.models

import com.google.gson.annotations.SerializedName

data class EjercicioResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("grupo_muscular") val grupoMuscular: String?,
    val tipo: String?,
    val dificultad: String?,
    @SerializedName("imagen_url") val imagenUrl: String?,
    @SerializedName("creado_por") val creadoPor: Int?,
)
