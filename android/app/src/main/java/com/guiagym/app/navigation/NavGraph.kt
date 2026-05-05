package com.guiagym.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.guiagym.app.GuiaGymApp
import com.guiagym.app.ui.auth.LoginScreen
import com.guiagym.app.ui.auth.LoginViewModel
import com.guiagym.app.ui.auth.RegisterScreen
import com.guiagym.app.ui.auth.RegisterViewModel
import com.guiagym.app.ui.ejercicios.EjerciciosScreen
import com.guiagym.app.ui.ejercicios.EjerciciosViewModel
import com.guiagym.app.ui.estadisticas.EstadisticasScreen
import com.guiagym.app.ui.estadisticas.EstadisticasViewModel
import com.guiagym.app.ui.entrenamientos.EntrenamientoEnCursoScreen
import com.guiagym.app.ui.entrenamientos.EntrenamientoEnCursoViewModel
import com.guiagym.app.ui.entrenamientos.EntrenamientosScreen
import com.guiagym.app.ui.entrenamientos.EntrenamientosViewModel
import com.guiagym.app.ui.home.HomeScreen
import com.guiagym.app.ui.perfil.PerfilScreen
import com.guiagym.app.ui.perfil.PerfilViewModel
import com.guiagym.app.ui.rutinas.MisRutinasScreen
import com.guiagym.app.ui.rutinas.MisRutinasViewModel
import com.guiagym.app.ui.rutinas.RutinaDetailScreen
import com.guiagym.app.ui.rutinas.RutinaDetailViewModel
import com.guiagym.app.ui.seguimiento.SeguimientoPesoScreen
import com.guiagym.app.ui.seguimiento.SeguimientoPesoViewModel

sealed class Screen(val route: String) {
    data object Login              : Screen("login")
    data object Register           : Screen("register")
    data object Home               : Screen("home")
    data object MisRutinas         : Screen("mis_rutinas")
    data object RutinaDetail       : Screen("rutina_detail/{rutinaId}") {
        fun createRoute(id: Int) = "rutina_detail/$id"
    }
    data object EjerciciosPicker   : Screen("ejercicios_picker/{rutinaId}") {
        fun createRoute(id: Int) = "ejercicios_picker/$id"
    }
    data object Ejercicios         : Screen("ejercicios")
    data object Entrenamientos     : Screen("entrenamientos")
    data object EntrenamientoEnCurso : Screen("entrenamiento_en_curso/{entrenamientoId}") {
        fun createRoute(id: Int) = "entrenamiento_en_curso/$id"
    }
    data object SeguimientoPeso    : Screen("seguimiento_peso")
    data object Estadisticas       : Screen("estadisticas")
    data object Perfil             : Screen("perfil")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    val app = LocalContext.current.applicationContext as GuiaGymApp

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(app.authRepository))
            LoginScreen(
                viewModel            = vm,
                onLoginSuccess       = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
            )
        }

        composable(Screen.Register.route) {
            val vm: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory(app.authRepository))
            RegisterScreen(
                viewModel         = vm,
                onRegisterSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogout               = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                onNavigateRutinas      = { navController.navigate(Screen.MisRutinas.route) },
                onNavigateEntrenam     = { navController.navigate(Screen.Entrenamientos.route) },
                onNavigateSeguim       = { navController.navigate(Screen.SeguimientoPeso.route) },
                onNavigateEjercicios   = { navController.navigate(Screen.Ejercicios.route) },
                onNavigateEstadisticas = { navController.navigate(Screen.Estadisticas.route) },
                onNavigatePerfil       = { navController.navigate(Screen.Perfil.route) },
            )
        }

        composable(Screen.MisRutinas.route) {
            val vm: MisRutinasViewModel = viewModel(factory = MisRutinasViewModel.Factory(app.rutinaRepository))
            MisRutinasScreen(
                viewModel          = vm,
                onBack             = { navController.popBackStack() },
                onNavigateToRutina = { id -> navController.navigate(Screen.RutinaDetail.createRoute(id)) },
            )
        }

        composable(
            Screen.RutinaDetail.route,
            arguments = listOf(navArgument("rutinaId") { type = NavType.IntType }),
        ) { backEntry ->
            val rutinaId = backEntry.arguments!!.getInt("rutinaId")
            val vm: RutinaDetailViewModel = viewModel(
                factory = RutinaDetailViewModel.Factory(rutinaId, app.rutinaRepository, app.entrenamientoRepository),
            )
            val state by vm.state.collectAsStateWithLifecycle()

            LaunchedEffect(state.entrenamientoIniciadoId) {
                state.entrenamientoIniciadoId?.let { id ->
                    vm.clearEntrenamientoIniciadoId()
                    navController.navigate(Screen.EntrenamientoEnCurso.createRoute(id))
                }
            }

            RutinaDetailScreen(
                viewModel              = vm,
                onBack                 = { navController.popBackStack() },
                onPickEjercicio        = { navController.navigate(Screen.EjerciciosPicker.createRoute(rutinaId)) },
                onIniciarEntrenamiento = { _ -> vm.iniciarEntrenamiento() },
            )
        }

        composable(
            Screen.EjerciciosPicker.route,
            arguments = listOf(navArgument("rutinaId") { type = NavType.IntType }),
        ) { backEntry ->
            val rutinaId = backEntry.arguments!!.getInt("rutinaId")
            val ejVm: EjerciciosViewModel = viewModel(
                factory = EjerciciosViewModel.Factory(app.ejercicioRepository),
            )
            // Retrieve the existing RutinaDetailViewModel from the back stack to share state
            val rutinaEntry = remember(backEntry) {
                navController.getBackStackEntry(Screen.RutinaDetail.createRoute(rutinaId))
            }
            val rutinaVm: RutinaDetailViewModel = viewModel(
                viewModelStoreOwner = rutinaEntry,
                factory             = RutinaDetailViewModel.Factory(rutinaId, app.rutinaRepository, app.entrenamientoRepository),
            )
            EjerciciosScreen(
                viewModel  = ejVm,
                pickerMode = true,
                onBack     = { navController.popBackStack() },
                onPick     = { ejercicioId ->
                    rutinaVm.setPickedEjercicio(ejercicioId)
                    navController.popBackStack()
                },
            )
        }

        composable(Screen.Ejercicios.route) {
            val vm: EjerciciosViewModel = viewModel(factory = EjerciciosViewModel.Factory(app.ejercicioRepository))
            EjerciciosScreen(
                viewModel  = vm,
                pickerMode = false,
                onBack     = { navController.popBackStack() },
                onPick     = null,
            )
        }

        composable(Screen.Entrenamientos.route) {
            val vm: EntrenamientosViewModel = viewModel(factory = EntrenamientosViewModel.Factory(app.entrenamientoRepository))
            EntrenamientosScreen(
                viewModel                 = vm,
                onBack                    = { navController.popBackStack() },
                onNavigateToEntrenamiento = { id -> navController.navigate(Screen.EntrenamientoEnCurso.createRoute(id)) },
            )
        }

        composable(
            Screen.EntrenamientoEnCurso.route,
            arguments = listOf(navArgument("entrenamientoId") { type = NavType.IntType }),
        ) { backEntry ->
            val entrenamientoId = backEntry.arguments!!.getInt("entrenamientoId")
            val vm: EntrenamientoEnCursoViewModel = viewModel(
                factory = EntrenamientoEnCursoViewModel.Factory(
                    entrenamientoId   = entrenamientoId,
                    entrenamientoRepo = app.entrenamientoRepository,
                    rutinaRepo        = app.rutinaRepository,
                ),
            )
            EntrenamientoEnCursoScreen(
                viewModel = vm,
                onBack    = { navController.popBackStack() },
            )
        }

        composable(Screen.SeguimientoPeso.route) {
            val vm: SeguimientoPesoViewModel = viewModel(factory = SeguimientoPesoViewModel.Factory(app.seguimientoRepository))
            SeguimientoPesoScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Screen.Estadisticas.route) {
            val vm: EstadisticasViewModel = viewModel(
                factory = EstadisticasViewModel.Factory(app.ejercicioRepository, app.entrenamientoRepository),
            )
            EstadisticasScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Screen.Perfil.route) {
            val vm: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory(app.authRepository))
            PerfilScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
