package com.guiagym.app.data.network

import com.guiagym.app.data.network.models.EjercicioResponse
import com.guiagym.app.data.network.models.EntrenamientoCreateRequest
import com.guiagym.app.data.network.models.EntrenamientoDetailResponse
import com.guiagym.app.data.network.models.EntrenamientoFinalizarRequest
import com.guiagym.app.data.network.models.EntrenamientoListResponse
import com.guiagym.app.data.network.models.LoginRequest
import com.guiagym.app.data.network.models.RegisterRequest
import com.guiagym.app.data.network.models.RegistroEjercicioCreateRequest
import com.guiagym.app.data.network.models.RegistroEjercicioResponse
import com.guiagym.app.data.network.models.RutinaCreateRequest
import com.guiagym.app.data.network.models.RutinaDetailResponse
import com.guiagym.app.data.network.models.RutinaEjercicioCreateRequest
import com.guiagym.app.data.network.models.RutinaEjercicioResponse
import com.guiagym.app.data.network.models.RutinaListResponse
import com.guiagym.app.data.network.models.SeguimientoPesoCreateRequest
import com.guiagym.app.data.network.models.SeguimientoPesoResponse
import com.guiagym.app.data.network.models.TokenResponse
import com.guiagym.app.data.network.models.UsuarioResponse
import com.guiagym.app.data.network.models.UsuarioUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("auth/registro")
    suspend fun register(@Body request: RegisterRequest): Response<UsuarioResponse>

    // ── Usuario ───────────────────────────────────────────────────────────────
    @GET("usuarios/me")
    suspend fun getMe(): Response<UsuarioResponse>

    @PUT("usuarios/me")
    suspend fun updateMe(@Body request: UsuarioUpdateRequest): Response<UsuarioResponse>

    // ── Ejercicios ────────────────────────────────────────────────────────────
    @GET("ejercicios/")
    suspend fun getEjercicios(
        @Query("grupo_muscular") grupoMuscular: String? = null,
        @Query("tipo")           tipo: String? = null,
        @Query("dificultad")     dificultad: String? = null,
        @Query("limit")          limit: Int = 200,
    ): Response<List<EjercicioResponse>>

    @GET("ejercicios/{id}")
    suspend fun getEjercicio(@Path("id") id: Int): Response<EjercicioResponse>

    // ── Rutinas ───────────────────────────────────────────────────────────────
    @GET("rutinas/")
    suspend fun getRutinas(): Response<List<RutinaListResponse>>

    @GET("rutinas/{id}")
    suspend fun getRutinaDetail(@Path("id") id: Int): Response<RutinaDetailResponse>

    @POST("rutinas/")
    suspend fun createRutina(@Body request: RutinaCreateRequest): Response<RutinaListResponse>

    @POST("rutinas/{id}/ejercicios")
    suspend fun addEjercicioToRutina(
        @Path("id") rutinaId: Int,
        @Body request: RutinaEjercicioCreateRequest,
    ): Response<RutinaEjercicioResponse>

    @DELETE("rutinas/{id}/ejercicios/{re_id}")
    suspend fun removeEjercicioFromRutina(
        @Path("id") rutinaId: Int,
        @Path("re_id") reId: Int,
    ): Response<Unit>

    // ── Entrenamientos ────────────────────────────────────────────────────────
    @GET("entrenamientos/")
    suspend fun getEntrenamientos(): Response<List<EntrenamientoListResponse>>

    @GET("entrenamientos/{id}")
    suspend fun getEntrenamientoDetail(@Path("id") id: Int): Response<EntrenamientoDetailResponse>

    @POST("entrenamientos/")
    suspend fun iniciarEntrenamiento(@Body request: EntrenamientoCreateRequest): Response<EntrenamientoListResponse>

    @PATCH("entrenamientos/{id}/finalizar")
    suspend fun finalizarEntrenamiento(
        @Path("id") id: Int,
        @Body request: EntrenamientoFinalizarRequest,
    ): Response<EntrenamientoListResponse>

    @POST("entrenamientos/{id}/registros")
    suspend fun addRegistro(
        @Path("id") entrenamientoId: Int,
        @Body request: RegistroEjercicioCreateRequest,
    ): Response<RegistroEjercicioResponse>

    // ── Seguimiento ───────────────────────────────────────────────────────────
    @GET("seguimiento/")
    suspend fun getSeguimiento(): Response<List<SeguimientoPesoResponse>>

    @POST("seguimiento/")
    suspend fun registrarPeso(@Body request: SeguimientoPesoCreateRequest): Response<SeguimientoPesoResponse>
}
