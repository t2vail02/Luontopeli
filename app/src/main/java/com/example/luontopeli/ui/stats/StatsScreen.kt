// 📁 ui/stats/StatsScreen.kt
package com.example.luontopeli.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luontopeli.viewmodel.StatsViewModel
import com.example.luontopeli.viewmodel.formatDistance
import com.example.luontopeli.viewmodel.formatDuration
import com.example.luontopeli.viewmodel.toFormattedDate

/**
 * Tilastonakyma – kokonaistilastot ja kävelyhistoria.
 *
 * Näyttää käyttäjän kokonaistilastot 4 yhteenvetokortissa:
 * - Askeleet yhteensä (kaikista kävelykerroista)
 * - Matka yhteensä (kilometreinä/metreinä)
 * - Löytöjen kokonaismäärä
 * - Kävelylenkkien lukumäärä
 *
 * Alapuolella kävelyhistoria: jokainen lenkki omana korttinaan.
 */
@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val sessions by viewModel.allSessions.collectAsState()
    val totalSpots by viewModel.totalSpots.collectAsState()

    // Lasketaan kokonaistilastot kaikista kävelykerroista
    val totalSteps = sessions.sumOf { it.stepCount }
    val totalDistance = sessions.sumOf { it.distanceMeters.toDouble() }.toFloat()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sivun otsikko
        item {
            Text(
                "Tilastot",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Yhteenveto-kortit rivi 1: Askeleet + Matka
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryCard(
                    value = "$totalSteps",
                    label = "Askelta yhteensä",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryCard(
                    value = formatDistance(totalDistance),
                    label = "Matka yhteensä",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Yhteenveto-kortit rivi 2: Löydöt + Kävelylenkit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatSummaryCard(
                    value = "$totalSpots",
                    label = "Löytöjä",
                    modifier = Modifier.weight(1f)
                )
                StatSummaryCard(
                    value = "${sessions.size}",
                    label = "Kävelylenkkejä",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Kävelyhistoria-osio
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    "Kävelyhistoria",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            // Jokainen kävelykerta omana korttinaan
            items(sessions) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kävelyikoni
                        Icon(
                            Icons.Default.DirectionsWalk, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            // Askeleet ja matka
                            Text(
                                "${session.stepCount} askelta • ${formatDistance(session.distanceMeters)}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            // Aloitusaika
                            Text(
                                session.startTime.toFormattedDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            // Kesto (näytetään vain jos kävely on päättynyt)
                            session.endTime?.let { end ->
                                Text(
                                    "Kesto: ${formatDuration(session.startTime, end)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Tyhjä tila – ei kävelylenkkejä vielä
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BarChart, null,
                            modifier = Modifier.size(48.dp), tint = Color.Gray
                        )
                        Text(
                            "Ei kävelylenkkejä vielä",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Yhteenvetotilastokortti.
 *
 * Näyttää yhden tilastoarvon otsikolla (2x2 ruudukko StatsScreenissä).
 */
@Composable
fun StatSummaryCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tilastoarvo suurella fontilla
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // Seliteteksti pienellä fontilla
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}