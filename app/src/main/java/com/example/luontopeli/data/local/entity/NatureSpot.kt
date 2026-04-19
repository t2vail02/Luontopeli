// 📁 data/local/entity/NatureSpot.kt
package com.example.luontopeli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room-entiteetti yksittäiselle luontolöydölle.
 *
 * Jokainen kuvattu kasvi/luontokohde tallennetaan omana rivinä "nature_spots"-tauluun.
 * Sisältää GPS-sijainnin, kuvapolun, ML Kit -tunnistustuloksen ja synkronointitilan.
 *
 * UUID pääavaimena mahdollistaa offline-luonnin ilman ID-konflikteja
 * ja yhteensopivuuden Firestore-dokumentti-ID:n kanssa.
 */
@Entity(tableName = "nature_spots")
data class NatureSpot(
    /** Globaalisti uniikki tunniste (UUID), toimii myös Firestore-dokumentin ID:nä */
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    /** Löydön nimi – ML Kit -tunnistustulos tai käyttäjän antama */
    val name: String,
    /** GPS-leveysaste (WGS84) */
    val latitude: Double,
    /** GPS-pituusaste (WGS84) */
    val longitude: Double,

    val comment: String? = null,

    /** Kuvan paikallinen tiedostopolku (sovelluksen sisäinen tallennustila) */
    val imageLocalPath: String? = null,
    /** Firebase Storage -URL (täytetään pilvisynkronoinnissa) */
    val imageFirebaseUrl: String? = null,

    /** ML Kit -tunnistama kasvilaji (esim. "Rosa canina") */
    val plantLabel: String? = null,
    /** ML Kit -tunnistuksen luottamusarvo (0.0–1.0) */
    val confidence: Float? = null,

    /** Firebase Auth -käyttäjätunniste (UID) */
    val userId: String? = null,
    /** Löydön aikaleima millisekunteina (epoch) */
    val timestamp: Long = System.currentTimeMillis(),

    /** Synkronointitila: false = vain paikallinen, true = synkronoitu Firestoreen */
    val synced: Boolean = false
)