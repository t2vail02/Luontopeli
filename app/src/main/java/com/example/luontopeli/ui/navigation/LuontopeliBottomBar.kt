// 📁 ui/navigation/LuontopeliBottomBar.kt
package com.example.luontopeli.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Sovelluksen alanäkymäpalkki (Bottom Navigation Bar).
 *
 * Näyttää 4 navigointivälilehteä: Kartta, Kamera, Löydöt ja Tilastot.
 * Korostaa nykyisen sivun välilehden Material3-tyylillä.
 *
 * Navigointi on konfiguroitu seuraavasti:
 * - popUpTo(startDestination): Estää back stack -pinon kasvamisen
 * - saveState: Tallentaa jokaisen välilehden tilan navigoitaessa pois
 * - launchSingleTop: Estää saman näkymän avaamisen uudelleen päällekkäin
 * - restoreState: Palauttaa tallennetun tilan palatessa välilehdelle
 *
 * @param navController NavController navigointitapahtumien käsittelyyn
 */
@Composable
fun LuontopeliBottomBar(navController: NavController) {
    // Seurataan nykyistä navigointireittiä back stackista
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        // Luodaan NavigationBarItem jokaiselle näkymälle (Map, Camera, Discover, Stats)
        Screen.bottomNavScreens.forEach { screen ->
            NavigationBarItem(
                // Korostetaan nykyisen sivun välilehti
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        // Palataan aina aloitusnäkymään asti back stackissa
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Estetään saman näkymän avaaminen uudelleen
                        launchSingleTop = true
                        // Palautetaan tallennettu tila palatessa
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label
                    )
                },
                label = { Text(screen.label) }
            )
        }
    }
}