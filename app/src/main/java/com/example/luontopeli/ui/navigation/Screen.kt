// 📁 ui/navigation/Screen.kt
package com.example.luontopeli.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class sovelluksen näkymien (screen) määrittelyyn.
 *
 * Jokainen näkymä sisältää:
 * - route: Navigoinnin reittitunniste (esim. "map", "camera")
 * - label: Käyttäjälle näytettävä teksti alanäkymäpalkissa
 * - icon: Material Design -ikoni alanäkymäpalkissa
 *
 * Sealed class varmistaa, että kaikki mahdolliset näkymät on määritelty
 * käännösaikaisesti, mikä estää virheelliset reitit.
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    /** Karttanäkymä – OpenStreetMap-kartta, GPS-reitinseuranta ja kävelytilastot */
    object Map : Screen("map", "Kartta", Icons.Filled.Map)

    /** Kameranäkymä – CameraX-esikatselu, kuvan otto ja ML Kit -tunnistus */
    object Camera : Screen("camera", "Kamera", Icons.Filled.CameraAlt)

    /** Löytönäkymä – lista kaikista tallennetuista luontolöydöistä */
    object Discover : Screen("discover", "Löydöt", Icons.Filled.Explore)

    /** Tilastonäkymä – yhteenvetotilastot ja kävelyhistoria */
    object Stats : Screen("stats", "Tilastot", Icons.Filled.BarChart)

    object Profile : Screen(route = "profile", label = "Profiili", icon = Icons.Filled.Person)

    companion object {
        /** Lista kaikista näkymistä alanäkymäpalkkia varten */
        val bottomNavScreens = listOf(Map, Camera, Discover, Stats, Profile)
    }
}