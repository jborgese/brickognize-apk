package com.frootsnoops.brickognize.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
        
        composable(Screen.Results.route) { _ ->
            // Get the ScanViewModel from the Scan screen's backstack entry
            val scanBackStackEntry = navController.getBackStackEntry(Screen.Scan.route)
            val scanViewModel: ScanViewModel = hiltViewModel(scanBackStackEntry)
            
            // Get the recognition result from ScanViewModel
            val scanUiState by scanViewModel.uiState.collectAsState()
            
            ResultsScreen(
                onNavigateBack = {
                    // Reset the scan state before going back
                    scanViewModel.resetState()
                    navController.popBackStack()
                },
                onNavigateHome = {
                    // Reset scan state and navigate to home
                    scanViewModel.resetState()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToScan = {
                    // Reset before returning so we do not auto-redirect back to Results
                    // Set autoLaunchCamera flag to reopen camera directly with current recognition type
                    scanViewModel.resetState()
                    scanViewModel.setAutoLaunchCamera(true)
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
