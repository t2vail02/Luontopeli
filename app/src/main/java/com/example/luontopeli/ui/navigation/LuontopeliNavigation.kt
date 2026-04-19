// 📁 ui/navigation/LuontopeliNavigation.kt
package com.example.luontopeli.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.luontopeli.ui.map.MapScreen
import com.example.luontopeli.camera.CameraScreen
import com.example.luontopeli.ui.discover.DiscoverScreen
import com.example.luontopeli.ui.profile.ProfileScreen
import com.example.luontopeli.ui.stats.StatsScreen

/**
 * Sovelluksen navigointikomponentti (NavHost).
 *
 * Määrittelee kaikki navigointireitit ja niihin liittyvät Compose-näkymät.
 * Käyttää Navigation Compose -kirjastoa näkymien väliseen navigointiin.
 * Aloituskohde on karttanäkymä (Screen.Map).
 *
 * @param navController NavHostController navigoinnin hallintaan
 * @param modifier Modifier Scaffoldin innerPaddingin välittämiseen
 */
@Composable
fun LuontopeliNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,  // Kartta on aloitusnäkymä
        modifier = modifier
    ) {
        // Karttanäkymä – GPS-seuranta, OpenStreetMap, reittien piirto
        composable(Screen.Map.route) {
            MapScreen()
        }
        // Kameranäkymä – CameraX-esikatselu, kuvan otto, ML Kit -tunnistus
        composable(Screen.Camera.route) {
            CameraScreen()
        }
        // Löytönäkymä – kaikki tallennetut luontolöydöt listana
        composable(Screen.Discover.route) {
            DiscoverScreen()
        }
        // Tilastonäkymä – yhteenvetotilastot ja kävelyhistoria
        composable(Screen.Stats.route) {
            StatsScreen()
        }
        composable(Screen.Profile.route){
            ProfileScreen()
        }
    }
}