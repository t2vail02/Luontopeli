// 📁 app/src/main/java/com/example/luontopeli/ui/theme/Theme.kt
package com.example.luontopeli.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * Vaalea väriskeema (Light Color Scheme).
 * Käyttää tummia vihreitä sävyjä (Green40, GreenGrey40, LightGreen40)
 * vaalealla taustalla paremman kontrastin saavuttamiseksi.
 */
private val LightColorScheme = lightColorScheme(
    primary = Green40,        // Pääväri: tummanvihreä (#2E7D32)
    secondary = GreenGrey40,  // Toissijainen: harmaanvihreä
    tertiary = LightGreen40,  // Kolmannen tason: keskivihreä
)

/**
 * Tumma väriskeema (Dark Color Scheme).
 * Käyttää vaaleampia vihreitä sävyjä (Green80, GreenGrey80, LightGreen80)
 * tummalla taustalla näkyvyyden varmistamiseksi.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Green80,        // Pääväri: vaaleanvihreä (#A5D6A7)
    secondary = GreenGrey80,  // Toissijainen: vihertävänharmaa
    tertiary = LightGreen80,  // Kolmannen tason: haaleankeltavihreä
)

/**
 * Luontopelin Material3-teema.
 *
 * Valitsee automaattisesti vaalean tai tumman väriskeeman järjestelmän
 * asetuksen perusteella (isSystemInDarkTheme). Sisältää mukautetun
 * typografian ja vihrean luonto-väripaletin.
 *
 * @param darkTheme Käytetäänkö tummaa teemaa (oletuksena järjestelmäasetus)
 * @param content Teeman sisältö (Composable-funktiot)
 */
@Composable
fun LuontopeliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Valitaan väriskeema järjestelmäasetuksen perusteella
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,  // Mukautettu typografia (Type.kt)
        content = content
    )
}