package com.guiagym.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.guiagym.app.navigation.NavGraph
import com.guiagym.app.navigation.Screen
import com.guiagym.app.ui.theme.GuiaGymTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GuiaGymApp

        setContent {
            GuiaGymTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(app.tokenDataStore)
                )
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()

                Surface(modifier = Modifier.fillMaxSize()) {
                    when (isLoggedIn) {
                        // Todavía leyendo DataStore → spinner breve
                        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        // Token encontrado o no → NavGraph con destino inicial correcto
                        else -> {
                            val navController = rememberNavController()
                            NavGraph(
                                navController    = navController,
                                startDestination = if (isLoggedIn == true) Screen.Home.route else Screen.Login.route,
                            )
                        }
                    }
                }
            }
        }
    }
}
