// 📁 location/LocationManager.kt
package com.example.luontopeli.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import org.osmdroid.util.GeoPoint               // GeoPoint on osmdroid-kirjaston paikkatyyppi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GPS-sijainnin seurantaan käytettävä hallintapalvelu.
 * Käyttää Androidin natiivi LocationManager-rajapintaa.
 *
 * @param context Android-konteksti
 */
class LocationManager(context: Context) {

    /** Android-järjestelmän LocationManager sijaintipalvelujen käyttöön */
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    /** Nykyinen GPS-sijainti (null jos sijaintia ei ole vielä saatu) */
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    /** Lista kävelyn aikana kerätyistä reittipisteistä (GeoPoint) kartan reittiviivaaa varten */
    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    /**
     * Sijaintipäivitysten kuuntelija.
     * Jokainen sijaintipäivitys:
     * 1. Päivittää nykyisen sijainnin
     * 2. Lisää uuden GeoPoint-reittipisteen listaan
     */
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation.value = location
            val newPoint = GeoPoint(location.latitude, location.longitude)
            _routePoints.value = _routePoints.value + newPoint
        }

        // Tarvitaan vanhemmille Android-versioille
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    /**
     * Käynnistää GPS-sijainnin seurannan.
     * Valitsee paikannuspalvelun prioriteettijärjestyksessä:
     * 1. GPS_PROVIDER
     * 2. NETWORK_PROVIDER
     * Päivitysväli: vähintään 5 sekuntia tai 10 metrin siirtymä.
     */
    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            // Kokeile GPS ensin, sitten verkko
            val provider = when {
                locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ->
                    android.location.LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) ->
                    android.location.LocationManager.NETWORK_PROVIDER
                else -> return  // Ei saatavilla olevaa paikannusta
            }

            locationManager.requestLocationUpdates(
                provider,
                5000L,   // Minimiväli: 5 sekuntia
                10f,     // Minimietäisyys: 10 metriä
                locationListener
            )
        } catch (_: SecurityException) {
            // Sijaintilupaa ei ole vielä myönnetty – seuranta käynnistyy myöhemmin
        }
    }

    /** Pysäyttää GPS-sijainnin seurannan. Poistaa LocationListener-kuuntelijan. */
    fun stopTracking() {
        locationManager.removeUpdates(locationListener)
    }

    /** Tyhjentää kaikki kerätyt reittipisteet. Kutsutaan kun uusi kävely aloitetaan. */
    fun resetRoute() {
        _routePoints.value = emptyList()
    }

    /**
     * Laskee reitin kokonaispituuden metreinä kaikkien reittipisteiden välillä.
     * Käyttää Location.distanceBetween()-metodia.
     *
     * @return Reitin kokonaismatka metreinä
     */
    fun calculateTotalDistance(): Float {
        val points = _routePoints.value
        if (points.size < 2) return 0f

        var total = 0f
        for (i in 0 until points.size - 1) {
            val results = FloatArray(1)
            Location.distanceBetween(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude,
                results
            )
            total += results[0]
        }
        return total
    }
}