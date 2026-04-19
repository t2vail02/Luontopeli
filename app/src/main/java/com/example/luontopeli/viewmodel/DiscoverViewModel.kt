// 📁 viewmodel/DiscoverViewModel.kt
package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel löytönäkymälle (DiscoverScreen).
 *
 * Lataa kaikki tallennetut luontolöydöt Room-tietokannasta ja tarjoaa ne
 * DiscoverScreen-näkymälle reaktiivisena StateFlow-virtana.
 * Lista päivittyy automaattisesti kun tietokantaan lisätään tai poistetaan löytöjä.
 */
class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    /** Room-tietokantainstanssi */
    private val db = AppDatabase.getDatabase(application)

    /** Kaikki luontolöydöt aikajärjestyksessä (uusin ensin) */
    private val _allSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val allSpots: StateFlow<List<NatureSpot>> = _allSpots.asStateFlow()

    init {
        // Seurataan tietokannan muutoksia – lista päivittyy automaattisesti
        viewModelScope.launch {
            db.natureSpotDao().getAllSpots().collect { spots ->
                _allSpots.value = spots
            }
        }
    }
}