// 📁 data/local/AppDatabase.kt
package com.example.luontopeli.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.local.entity.WalkSession

/**
 * Room-tietokannan pääluokka (Singleton-malli).
 *
 * Hallinnoi SQLite-tietokantaa ja tarjoaa DAO-rajapinnat tietokantaoperaatioille.
 * Käyttää fallbackToDestructiveMigration()-strategiaa kehitysvaiheessa,
 * mikä tyhjentää tietokannan skeeman muuttuessa (tuotannossa käytettäisiin migraatiota).
 */
@Database(
    entities = [
        NatureSpot::class,   // Luontolöydöt (viikko 4)
        WalkSession::class   // Kävelysessiot (viikko 2)
    ],
    version = 3,             // Kasvatettu 1→2 koska lisättiin NatureSpot
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /** DAO luontolöytöjen tietokantaoperaatioille */
    abstract fun natureSpotDao(): NatureSpotDao
    /** DAO kävelysessioiden tietokantaoperaatioille */
    abstract fun walkSessionDao(): WalkSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /** Palauttaa tietokannan singleton-instanssin (thread-safe) */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luontopeli_database"
                )
                    .fallbackToDestructiveMigration()  // Kehitysvaiheessa OK
                    .build().also { INSTANCE = it }
            }
        }
    }
}