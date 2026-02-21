package com.frootsnoops.brickognize.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.frootsnoops.brickognize.ui.bins.BinsScreen
import com.frootsnoops.brickognize.ui.history.HistoryScreen
import com.frootsnoops.brickognize.ui.home.HomeScreen
import com.frootsnoops.brickognize.ui.results.ResultsScreen
import com.frootsnoops.brickognize.ui.scan.ScanScreen
import com.frootsnoops.brickognize.ui.scan.ScanViewModel
import com.frootsnoops.brickognize.ui.scan.ScanUiState

@Composable
fun BrickognizeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToScan = {
                    navController.navigate(Screen.Scan.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToBins = {
                    navController.navigate(Screen.Bins.route)
                }
            )
        }
        
        composable(Screen.Scan.route) {
            val scanViewModel: ScanViewModel = hiltViewModel()
            
            ScanScreen(
                onNavigateBack = {
                    scanViewModel.resetState()
                    navController.popBackStack()
                },
                onNavigateToResults = {
                    navController.navigate(Screen.Results.route)
                },
                viewModel = scanViewModel
            )
        }
        
        composable(Screen.Results.route) {
            val scanBackStackEntry = navController.findBackStackEntryOrNull(Screen.Scan.route)
            val scanViewModel = scanBackStackEntry?.let { entry ->
                hiltViewModel<ScanViewModel>(entry)
            }
            val scanUiState = if (scanViewModel != null) {
                scanViewModel.uiState.collectAsState().value
            } else {
                null
            }

            ResultsScreen(
                onNavigateBack = {
                    scanViewModel?.resetState()
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToScan = {
                    scanViewModel?.resetState()
                    scanViewModel?.setAutoLaunchCamera(true)
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(Screen.Scan.route)
                    }
                },
                recognitionResult = (scanUiState as? ScanUiState.Success)?.result
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Bins.route) {
            BinsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun NavHostController.findBackStackEntryOrNull(route: String): NavBackStackEntry? {
    return try {
        getBackStackEntry(route)
    } catch (_: IllegalArgumentException) {
        null
    }
}
