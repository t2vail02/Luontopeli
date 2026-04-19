// 📁 ml/PlantClassifier.kt
package com.example.luontopeli.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit -pohjainen kasvin tunnistaja (on-device Image Labeling).
 * Käyttää Google ML Kit:n paikallista kuvatunnistusta.
 * ML Kit -malli toimii täysin laitteella (on-device).
 */
class PlantClassifier {

    /** ML Kit Image Labeler -instanssi. Konfiguroitu 50% minimivarmuuskynnyksellä. */
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    /**
     * Luontoon liittyvät avainsanat, joilla suodatetaan ML Kit:n tuloksia.
     * Jos tunnistettu merkintä sisältää jonkin näistä sanoista...
     */
    private val natureKeywords = setOf(
        "plant", "flower", "tree", "shrub", "leaf", "fern", "moss",
        "mushroom", "fungus", "grass", "herb", "bush", "berry",
        "pine", "birch", "spruce", "algae", "lichen", "bark",
        "nature", "forest", "woodland", "botanical", "flora",
        //Eläimet
        "animal", "dog", "cat", "fox", "wolf", "bear", "deer",
        "rabbit", "horse", "cow", "bird", "fish",
        //Sienet
        "mushroom", "fungus",
        //Hyönteiset
        "insect", "bee", "butterfly", "ant","spider"
    )

    /**
     * Analysoi kuvan ja tunnistaa siitä luontokohteet.
     * Prosessi:
     * 1. Luo InputImage kuvan URI:sta
     * 2. Ajaa ML Kit Image Labeling -mallin
     * 3. Suodattaa tuloksista luontoon liittyvät merkinnät
     * 4. Palauttaa parhaan osuman
     */
    suspend fun classify(imageUri: Uri, context: Context): ClassificationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Luodaan ML Kit -yhteensopiva kuva URI:sta
                val inputImage = InputImage.fromFilePath(context, imageUri)

                // Ajetaan tunnistus asynkronisesti
                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        // Suodatetaan vain luontoon liittyvät merkinnät avainsanojen perusteella
                        val natureLabels = labels.filter { label ->
                            natureKeywords.any { keyword ->
                                label.text.contains(keyword, ignoreCase = true)
                            }
                        }

                        val result = if (natureLabels.isNotEmpty()) {
                            // Valitaan paras osuma varmuusasteen perusteella
                            val best = natureLabels.maxByOrNull { it.confidence }!!
                            ClassificationResult.Success(
                                label = best.text,
                                confidence = best.confidence,
                                allLabels = labels.take(5)  // Top 5 kaikista tunnistuksista
                            )
                        } else {
                            // Kuva tunnistettiin mutta ei löytynyt luontokohteita
                            ClassificationResult.NotNature(
                                allLabels = labels.take(3)
                            )
                        }

                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    /** Vapauttaa ML Kit -resurssit. Kutsutaan kun CameraViewModel tuhotaan. */
    fun close() {
        labeler.close()
    }
}

/**
 * Sealed class kuvatunnistuksen tulokselle.
 * Kolme mahdollista tilaa: Success, NotNature, Error.
 */
sealed class ClassificationResult {
    /**
     * Onnistunut tunnistus – kuva sisältää luontokohteen.
     * @property label Tunnistettu luontokohde
     * @property confidence Varmuusaste 0.0 – 1.0
     * @property allLabels Kaikki tunnistetut merkinnät
     */
    data class Success(
        val label: String,
        val confidence: Float,
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    /**
     * Kuva ei sisällä luontokohteita.
     * @property allLabels Kaikki tunnistetut merkinnät
     */
    data class NotNature(
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    /**
     * Tunnistus epäonnistui.
     * @property message Virheilmoitus
     */
    data class Error(val message: String) : ClassificationResult()
}