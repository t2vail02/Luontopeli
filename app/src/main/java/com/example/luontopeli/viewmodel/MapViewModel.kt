// 📁 viewmodel/MapViewModel.kt
package com.example.luontopeli.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.location.LocationManager
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel karttanäkymälle (MapScreen).
 * Hallinnoi sijaintiseurantaa, reittipisteita ja luontolöytöjen näyttämistä kartalla.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    /** GPS-sijainnin seurantapalvelu */
    private val locationManager = LocationManager(application)
    /** Room-tietokantainstanssi luontolöytöjen hakemiseen */
    private val db = AppDatabase.getDatabase(application)

    /** Kävelyn aikana kerätyt reittipisteet (GeoPoint-lista) kartalle piirtämistä varten */
    val routePoints: StateFlow<List<GeoPoint>> = locationManager.routePoints
    /** Nykyinen GPS-sijainti */
    val currentLocation: StateFlow<Location?> = locationManager.currentLocation

    /** Kartalla näytettävät luontolöydöt joilla on validi sijainti */
    private val _natureSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val natureSpots: StateFlow<List<NatureSpot>> = _natureSpots.asStateFlow()

    init {
        // Lataa luontolöydöt tietokannasta heti ViewModelin luomisen yhteydessä
        loadNatureSpots()
    }

    /** Käynnistää GPS-sijainnin seurannan. Kutsutaan MapScreenista kun sijaintiluvat on myönnetty. */
    fun startTracking() = locationManager.startTracking()
    /** Pysäyttää GPS-sijainnin seurannan. */
    fun stopTracking() = locationManager.stopTracking()
    /** Tyhjentää kaikki kerätyt reittipisteet. */
    fun resetRoute() = locationManager.resetRoute()

    /**
     * Lataa luontolöydöt Room-tietokannasta reaktiivisesti.
     * Hakee vain löydöt joilla on validi GPS-sijainti.
     */
    private fun loadNatureSpots() {
        viewModelScope.launch {
            db.natureSpotDao().getSpotsWithLocation().collect { spots ->
                _natureSpots.value = spots
            }
        }
    }

    /** Vapauttaa LocationManager-resurssit ViewModelin tuhoutuessa. */
    override fun onCleared() {
        super.onCleared()
        locationManager.stopTracking()
    }
}

/**
 * Laajennusfunktio Long-aikaleiman muuntamiseen luettavaan päivämäärämuotoon.
 *
 * @return Muotoiltu päivämäärä (esim. "11.03.2026 14:30")
 */
fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}