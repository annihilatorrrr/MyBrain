package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mhss.app.mybrain.presentation.app_lock.AppLockManager
import com.mhss.app.mybrain.presentation.main.DashboardScreen
import com.mhss.app.mybrain.presentation.main.SettingsScreen
import com.mhss.app.mybrain.presentation.main.SpacesScreen
import com.mhss.app.mybrain.presentation.main.SpacesViewModel
import com.mhss.app.ui.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainNavController: NavHostController,
    startUpScreen: Screen,
    appLockManager: AppLockManager
) {
    NavHost(modifier = modifier, navController = navController, startDestination = startUpScreen){

        composable<Screen.DashboardScreen>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            DashboardScreen(mainNavController)
        }
        composable<Screen.SpacesScreen>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            val viewModel: SpacesViewModel = koinViewModel()
            val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
            SpacesScreen(mainNavController, pairedDevices)
        }
        composable<Screen.SettingsScreen>(
            enterTransition = { fadeIn(tween(0)) },
            exitTransition = { fadeOut(tween(0)) },
        ) {
            SettingsScreen(mainNavController, appLockManager)
        }
    }
}