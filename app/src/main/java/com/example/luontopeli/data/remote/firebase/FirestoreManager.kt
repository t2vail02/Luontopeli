// 📁 data/remote/firebase/FirestoreManager.kt
package com.example.luontopeli.data.remote.firebase

import com.example.luontopeli.data.local.entity.NatureSpot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore-hallinta luontolöytöjen pilvisynkronointiin.
 *
 * Tallentaa NatureSpot-oliot "nature_spots"-kokoelmaan.
 * Jokainen löytö on oma dokumenttinsa, jonka ID on NatureSpot.id.
 */
class FirestoreManager {

    /** Firestore-instanssi */
    private val db = FirebaseFirestore.getInstance()

    /** Kokoelman nimi Firestoressa */
    private val spotsCollection = db.collection("nature_spots")

    /**
     * Tallentaa luontolöydön Firestoreen.
     * Käyttää set()-metodia, joka luo dokumentin tai korvaa sen.
     */
    suspend fun saveSpot(spot: NatureSpot): Result<Unit> {
        return try {
            // Muunnetaan NatureSpot Map-muotoon Firestorea varten
            val data = mapOf(
                "id" to spot.id,
                "name" to spot.name,
                "latitude" to spot.latitude,
                "longitude" to spot.longitude,
                "imageLocalPath" to spot.imageLocalPath,
                "imageFirebaseUrl" to spot.imageFirebaseUrl,
                "plantLabel" to spot.plantLabel,
                "confidence" to spot.confidence,
                "userId" to spot.userId,
                "timestamp" to spot.timestamp,
                "comment" to spot.comment,
                "synced" to true
            )
            spotsCollection.document(spot.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hakee käyttäjän löydöt Firestoresta reaaliaikaisena virtana.
     * callbackFlow muuntaa Firestoren snapshot-kuuntelijan Flow:ksi.
     */
    fun getUserSpots(userId: String): Flow<List<NatureSpot>> = callbackFlow {
        val listener = spotsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val spots = snapshot?.documents?.mapNotNull { doc ->
                    NatureSpot(
                        id = doc.getString("id") ?: "",
                        name = doc.getString("name") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        imageFirebaseUrl = doc.getString("imageFirebaseUrl"),
                        plantLabel = doc.getString("plantLabel"),
                        confidence = doc.getDouble("confidence")?.toFloat(),
                        userId = doc.getString("userId"),
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        synced = true
                    )
                } ?: emptyList()
                trySend(spots)
            }
        // Poistetaan kuuntelija kun Flow peruutetaan
        awaitClose { listener.remove() }
    }
}