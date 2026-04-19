// 📁 LuontopeliApplication.kt
package com.example.luontopeli

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Sovelluksen Application-luokka.
 *
 * @HiltAndroidApp-annotaatio tekee tästä Hilt-riippuvuusinjektion juurikomponentin.
 * Hilt generoi automaattisesti DI-komponentit ja injektoi riippuvuudet
 * kaikkiin @AndroidEntryPoint-annotoiduilla merkittyihin luokkiin (Activity, Fragment jne.).
 *
 * Tämä luokka on rekisteröity AndroidManifest.xml:ssä android:name-attribuutilla.
 */
@HiltAndroidApp
class LuontopeliApplication : Application()