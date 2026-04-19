// 📁 app/src/main/java/com/example/luontopeli/ui/theme/Type.kt
package com.example.luontopeli.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Sovelluksen typografia (Material3 Type Scale).
 *
 * Määrittelee kolme tekstityyliä:
 * - headlineLarge: Suuret otsikot (32sp, Bold) – sivujen pääotsikot
 * - titleMedium: Keskikokoiset otsikot (16sp, SemiBold) – kortit, listaelementit
 * - bodyMedium: Leipäteksti (14sp, Normal) – kuvaukset, selitteet
 *
 * Käyttää järjestelmän oletusfonttiperhettä (FontFamily.Default).
 */
val Typography = Typography(
    // Suuret otsikot – käytetään esim. StatsScreen:n "Tilastot" -otsikossa
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    // Keskikokoiset otsikot – käytetään kortteissa ja listoissa
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Leipäteksti – käytetään kuvauksissa ja pienissä teksteissä
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)