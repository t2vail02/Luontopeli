// 📁 data/local/dao/NatureSpotDao.kt
package com.example.luontopeli.data.local.dao

import androidx.room.*
import com.example.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) luontolöytöjen tietokantaoperaatioille.
 *
 * Tarjoaa CRUD-operaatiot NatureSpot-entiteetille Room-tietokannan kautta.
 * Flow-paluuarvot mahdollistavat reaktiivisen UI-päivityksen Composessa.
 */
@Dao
interface NatureSpotDao {

    /** Lisää uusi löytö tai korvaa olemassa oleva samalla ID:llä (UPSERT-toiminto) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spot: NatureSpot): Long

    /** Palauttaa kaikki löydöt aikajärjestyksessä (uusin ensin) reaktiivisena Flow-virtana */
    @Query("SELECT * FROM nature_spots ORDER BY timestamp DESC")
    fun getAllSpots(): Flow<List<NatureSpot>>

    /** Palauttaa löydöt joilla on validi GPS-sijainti (kartalla näytettävät) */
    @Query("SELECT * FROM nature_spots WHERE latitude != 0.0")
    fun getSpotsWithLocation(): Flow<List<NatureSpot>>

    /** Hakee synkronoimattomat löydöt Firebase-lähetystä varten */
    @Query("SELECT * FROM nature_spots WHERE synced = 0")
    suspend fun getUnsyncedSpots(): List<NatureSpot>

    /** Merkitsee löydön synkronoiduksi ja tallentaa Firebase Storage -URL:n */
    @Query("UPDATE nature_spots SET synced = 1, imageFirebaseUrl = :url WHERE id = :id")
    suspend fun markSynced(id: String, url: String)

    /** Poistaa löydön tietokannasta */
    @Delete
    suspend fun delete(spot: NatureSpot)
}