// 📁 viewmodel/StatsViewModel.kt
package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel tilastonäkymälle (StatsScreen).
 *
 * Lataa kaikki kävelykerrat ja luontolöytöjen lukumäärän Room-tietokannasta.
 * Tarjoaa datan StatsScreen-näkymälle yhteenvetotilastojen (askeleet, matka,
 * löydöt, lenkit) ja kävelyhistorian näyttämiseen.
 *
 * Molemmat Flow-virrat päivittyvät automaattisesti kun tietokanta muuttuu.
 */
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    /** Room-tietokantainstanssi */
    private val db = AppDatabase.getDatabase(application)

    /** Kaikki kävelykerrat aikajärjestyksessä (uusin ensin) */
    private val _allSessions = MutableStateFlow<List<WalkSession>>(emptyList())
    val allSessions: StateFlow<List<WalkSession>> = _allSessions.asStateFlow()

    /** Luontolöytöjen kokonaismäärä */
    private val _totalSpots = MutableStateFlow(0)
    val totalSpots: StateFlow<Int> = _totalSpots.asStateFlow()

    init {
        // Seurataan kävelykertojen muutoksia tietokannassa
        viewModelScope.launch {
            db.walkSessionDao().getAllSessions().collect { sessions ->
                _allSessions.value = sessions
            }
        }
        // Seurataan luontolöytöjen kokonaismäärän muutoksia
        viewModelScope.launch {
            db.natureSpotDao().getAllSpots().collect { spots ->
                _totalSpots.value = spots.size
            }
        }
    }
}