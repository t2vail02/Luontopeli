// 📁 app/src/main/java/com/example/luontopeli/ui/theme/Color.kt
package com.example.luontopeli.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Sovelluksen väripaletti (Material3 Color Scheme).
 *
 * Luontopeli käyttää vihreää luonto-teemaa.
 * "80"-loppuiset värit ovat vaaleampia (dark theme) ja
 * "40"-loppuiset tummempia (light theme).
 */

// Tumma teema (Dark Theme) – vaaleammat sävyt tummalla taustalla
val Green80 = Color(0xFFA5D6A7)      // Vaaleanvihreä (primary dark)
val GreenGrey80 = Color(0xFFB0BEC5)  // Vihertävänharmaa (secondary dark)
val LightGreen80 = Color(0xFFC5E1A5) // Haaleankeltavihreä (tertiary dark)

// Vaalea teema (Light Theme) – tummemmat sävyt vaalealla taustalla
val Green40 = Color(0xFF2E7D32)      // Tummanvihreä (primary light) – pääväri
val GreenGrey40 = Color(0xFF546E7A)  // Harmaanvihreä (secondary light)
val LightGreen40 = Color(0xFF558B2F) // Keskivihreä (tertiary light)