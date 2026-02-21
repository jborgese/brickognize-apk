package com.frootsnoops.brickognize.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Scan : Screen("scan")
    data object Results : Screen("results")
    data object History : Screen("history")
    data object Bins : Screen("bins")
    data object BinDetails : Screen("bins/{binId}") {
        fun createRoute(binId: Long) = "bins/$binId"
    }
}
