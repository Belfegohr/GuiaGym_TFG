package com.guiagym.app

import android.app.Application
import com.guiagym.app.data.local.TokenDataStore
import com.guiagym.app.data.network.buildRetrofit
import com.guiagym.app.data.repository.AuthRepository
import com.guiagym.app.data.repository.EjercicioRepository
import com.guiagym.app.data.repository.EntrenamientoRepository
import com.guiagym.app.data.repository.RutinaRepository
import com.guiagym.app.data.repository.SeguimientoRepository

class GuiaGymApp : Application() {

    val tokenDataStore by lazy { TokenDataStore(this) }
    private val apiService by lazy { buildRetrofit(tokenDataStore) }

    val authRepository          by lazy { AuthRepository(apiService, tokenDataStore) }
    val ejercicioRepository     by lazy { EjercicioRepository(apiService) }
    val rutinaRepository        by lazy { RutinaRepository(apiService) }
    val entrenamientoRepository by lazy { EntrenamientoRepository(apiService) }
    val seguimientoRepository   by lazy { SeguimientoRepository(apiService) }
}
