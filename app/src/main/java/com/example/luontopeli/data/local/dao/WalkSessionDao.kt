// 📁 data/local/dao/WalkSessionDao.kt
package com.example.luontopeli.data.local.dao

import androidx.room.*
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO (Data Access Object) kävelykertojen tietokantaoperaatioille.
 *
 * Tarjoaa CRUD-operaatiot WalkSession-entiteetille. Käytetään kävelylenkkien
 * tallentamiseen, hakemiseen ja päivittämiseen paikallisessa Room-tietokannassa.
 */
@Dao
interface WalkSessionDao {

    /**
     * Lisää uuden kävelykerran tietokantaan.
     * Jos sama ID on jo olemassa, korvaa vanhan rivin.
     * @param session Tallennettava WalkSession-entiteetti
     * @return Lisätyn rivin ID (Long)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WalkSession): Long

    /**
     * Hakee kaikki kävelykerrat aloitusajan mukaan laskevassa järjestyksessä (uusin ensin).
     * @return Flow-virta, joka päivittyy automaattisesti kun tietokanta muuttuu
     */
    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    /**
     * Hakee parhaillaan aktiivisen kävelykerran (isActive = 1).
     * @return Aktiivinen kävelykerta tai null jos kävely ei ole käynnissä
     */
    @Query("SELECT * FROM walk_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): WalkSession?

    /**
     * Päivittää olemassa olevan kävelykerran tiedot (esim. askeleet, matka, lopetusaika).
     * @param session Päivitettävä WalkSession-entiteetti
     */
    @Update
    suspend fun update(session: WalkSession)

    /**
     * Poistaa kävelykerran tietokannasta.
     * @param session Poistettava WalkSession-entiteetti
     */
    @Delete
    suspend fun delete(session: WalkSession)
}