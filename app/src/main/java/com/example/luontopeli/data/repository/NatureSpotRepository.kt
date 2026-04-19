// 📁 data/repository/NatureSpotRepository.kt
package com.example.luontopeli.data.repository

import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.remote.firebase.StorageManager
import kotlinx.coroutines.flow.Flow

/**
 * Repository-luokka luontolöytöjen hallintaan (Repository-suunnittelumalli).
 *
 * Toimii välittäjänä tietolähteiden (Room-tietokanta) ja ViewModelien välillä.
 * Offline-tilassa kaikki data tallennetaan ja haetaan paikallisesta Room-tietokannasta.
 * Firebase-managerit ovat no-op -toteutuksia, jotka eivät tee verkko-operaatioita.
 */
// 📁 data/repository/NatureSpotRepository.kt

class NatureSpotRepository(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager
) {
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()

    // Tallenna löytö: ensin Room, sitten Firebase
    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(userId = authManager.currentUserId)

        // 1. Tallenna paikallisesti HETI (toimii offline-tilassakin)
        dao.insert(spotWithUser.copy(synced = false))

        // 2. Yritä synkronoida Firebaseen
        syncSpotToFirebase(spotWithUser)
    }

    // Synkronoi yksittäinen kohde Firebaseen
    private suspend fun syncSpotToFirebase(spot: NatureSpot) {
        try {
            // 2a. Lataa kuva Storageen (jos paikallinen kuva olemassa)
            val firebaseImageUrl = spot.imageLocalPath?.let { localPath ->
                storageManager.uploadImage(localPath, spot.id).getOrNull()
            }

            // 2b. Tallenna metadata Firestoreen
            val spotWithUrl = spot.copy(imageFirebaseUrl = firebaseImageUrl)
            firestoreManager.saveSpot(spotWithUrl).getOrThrow()

            // 2c. Merkitse Room:ssa synkronoiduksi
            dao.markSynced(spot.id, firebaseImageUrl ?: "")
        } catch (e: Exception) {
            // Synkronointi epäonnistui – yritetään uudelleen myöhemmin
            // synced = false pysyy Room:ssa
        }
    }

    // Synkronoi kaikki odottavat kohteet (kutsutaan yhteyden palautuessa)
    suspend fun syncPendingSpots() {
        val unsyncedSpots = dao.getUnsyncedSpots()
        unsyncedSpots.forEach { spot ->
            syncSpotToFirebase(spot)
        }
    }
}