// 📁 viewmodel/WalkViewModel.kt
package com.example.luontopeli.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.sensor.StepCounterManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel kävelyn hallintaan.
 * Hallinnoi kävelylenkin aloittamista, askelten laskentaa ja lopetusta.
 * Jaettu MapScreen:n kanssa.
 */
class WalkViewModel(application: Application) : AndroidViewModel(application) {

    /** Askelmittarin hallintapalvelu (STEP_DETECTOR-sensori) */
    private val stepManager = StepCounterManager(application)

    /** Room-tietokantainstanssi kävelykertojen tallentamiseen */
    private val db = AppDatabase.getDatabase(application)

    /** Nykyinen kävelykerta (null jos kävely ei ole käynnissä) */
    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    /** Onko kävely parhaillaan käynnissä */
    private val _isWalking = MutableStateFlow(false)

    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    /**
     * Aloittaa uuden kävelylenkin.
     * 1. Luo uuden WalkSession-olion
     * 2. Asettaa tilan aktiiviseksi
     * 3. Käynnistää askelmittarin
     */

    private var fakeStepJob: Job? = null

    fun startFakeSteps() {
        fakeStepJob = viewModelScope.launch {
            while (isActive) {
                delay(800) // askel joka 0.8s
                _currentSession.update { current ->
                    current?.copy(
                        stepCount = current.stepCount + 1,
                        distanceMeters = current.distanceMeters + StepCounterManager.STEP_LENGTH_METERS
                    )
                }
            }
        }
    }

    fun stopFakeSteps() {
        fakeStepJob?.cancel()
    }
    fun startWalk() {
        // Estä päällekkäiset käynnistykset
        if (_isWalking.value) return

        val session = WalkSession(startTime=System.currentTimeMillis())
        _currentSession.value = session
        _isWalking.value = true

        startFakeSteps()
    }

    /**
     * Lopettaa käynnissä olevan kävelylenkin.
     * 1. Pysäyttää askelmittarin
     * 2. Merkitsee kävelykerran päättyneeksi
     * 3. Tallentaa kävelykerran tietokantaan
     */
    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false

        // Päivitä session lopetustiedoilla
        _currentSession.update { it?.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )}

        // Tallenna valmistunut kävely tietokantaan
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                db.walkSessionDao().insert(session)
            }
        }
        stopFakeSteps()
    }

    /** Vapauttaa sensorien resurssit ViewModelin tuhoutuessa. */
    override fun onCleared() {
        super.onCleared()
        stepManager.stopAll()
    }
}

/**
 * Muotoilee matkan metreinä luettavaan muotoon.
 * Alle 1000 m näytetään metreinä, muuten kilometreinä.
 * @param meters Matka metreinä
 * @return Muotoiltu merkkijono
 */
fun formatDistance(meters: Float): String {
    return if (meters < 1000f) {
        "${meters.toInt()} m"
    } else {
        "${"%.1f".format(meters / 1000f)} km"
    }
}

/**
 * Muotoilee keston aloitus- ja lopetusajan perusteella.
 * @param startTime Aloitusaika millisekunteina
 * @param endTime Lopetusaika millisekunteina
 * @return Muotoiltu kesto
 */
fun formatDuration(startTime: Long, endTime: Long = System.currentTimeMillis()): String {
    if (endTime <= startTime) return "0s"
    val seconds = (endTime - startTime) / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}min"
        minutes > 0 -> "${minutes}min ${seconds % 60}s"
        else -> "${seconds}s"
    }
}