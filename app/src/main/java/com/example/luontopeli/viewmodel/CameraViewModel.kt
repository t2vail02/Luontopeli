// 📁 viewmodel/CameraViewModel.kt
package com.example.luontopeli.viewmodel


import android.content.Context
import android.net.Uri
import android.app.Application
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.remote.firebase.StorageManager
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.location.LocationManager
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.ml.PlantClassifier
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * ViewModel kameranäkymälle (CameraScreen).
 *
 * Hallinnoi kuvien ottamista (CameraX), kasvin tunnistamista (ML Kit)
 * ja luontolöytöjen tallentamista Room-tietokantaan.
 *
 * Toimintaketju: Kuvan otto -> ML Kit -tunnistus -> Tulosten näyttö -> Tallennus tietokantaan
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {

    /** Room-tietokantainstanssi */
    private val db = AppDatabase.getDatabase(application)

    private val locationManager = LocationManager(application)
    private val analysisExecutor = Executors.newSingleThreadExecutor()


    init {
        locationManager.startTracking()
    }
    private val realtimeLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    private val _realtimeLabels = MutableStateFlow<List<String>>(emptyList())
    val realtimeLabels = _realtimeLabels.asStateFlow()
    /**
     * NatureSpotRepository hallinnoi luontolöytöjen tallennusta.
     * Firebase-managerit ovat offline-tilassa no-op -toteutuksia.
     */
    private val repository = NatureSpotRepository(
        dao = db.natureSpotDao(),
        firestoreManager = FirestoreManager(),
        storageManager = StorageManager(),
        authManager = AuthManager()
    )

    @androidx.camera.core.ExperimentalGetImage
    val imageAnalyzer: ImageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(640, 480))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { analysis ->

            analysis.setAnalyzer(analysisExecutor) { imageProxy ->

                val mediaImage = imageProxy.image
                if (mediaImage != null) {

                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    realtimeLabeler.process(inputImage)
                        .addOnSuccessListener { labels ->

                            _realtimeLabels.value = labels.take(3).map {
                                "${it.text} (${(it.confidence * 100).toInt()}%)"
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }
        }

    /** ML Kit -pohjainen kasvin tunnistaja (toimii laitteella ilman internetiä) */
    private val classifier = PlantClassifier()

    /** Otetun kuvan paikallinen tiedostopolku (null = ei kuvaa) */
    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment = _comment.asStateFlow()


    /** Latausilmaisin (true = kuva otetaan tai tunnistus käynnissä) */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** ML Kit -tunnistuksen tulos (null = tunnistusta ei ole suoritettu) */
    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()


    /** Nykyinen GPS-sijainti (asetetaan MapViewModelista) löydön sijaintitiedoiksi */
    val currentLatitude: Double get() = locationManager.currentLocation.value?.latitude ?: 0.0
    val currentLongitude: Double get() = locationManager.currentLocation.value?.longitude ?: 0.0

    /**
     * Ottaa kuvan CameraX:n ImageCapture-objektilla ja tunnistaa kasvin.
     *
     * Prosessi:
     * 1. Luo aikaleimalla nimetyn kuvatiedoston laitteen sisäiseen tallennustilaan
     * 2. Ottaa kuvan CameraX:lla ja tallentaa sen tiedostoon
     * 3. Onnistuneen kuvan jälkeen käynnistää ML Kit -tunnistuksen taustasäikeessä
     * 4. Päivittää UI-tilan tunnistuksen tuloksella
     */

    fun updateComment(text: String){
        _comment.value = text
    }
    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val outputDir = File(context.filesDir, "nature_photos").also { it.mkdirs() }
        val outputFile = File(outputDir, "IMG_${timestamp}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                /** Kuva tallennettu onnistuneesti – käynnistä ML Kit -tunnistus */
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _capturedImagePath.value = outputFile.absolutePath

                    // Tunnista kasvi kuvasta ML Kit:n avulla
                    viewModelScope.launch {
                        try {
                            val uri = Uri.fromFile(outputFile)
                            val result = classifier.classify(uri, context)
                            _classificationResult.value = result
                        } catch (e: Exception) {
                            _classificationResult.value =
                                ClassificationResult.Error(e.message ?: "Tuntematon virhe")
                        }
                        _isLoading.value = false
                    }
                }

                /** Kuvan otto epäonnistui (esim. kameravirhe) */
                override fun onError(exception: ImageCaptureException) {
                    _isLoading.value = false
                }
            }
        )
    }

    /**
     * Tyhjentää otetun kuvan ja tunnistustuloksen.
     * Palauttaa UI:n kameran esikatselunäkymään.
     */
    fun clearCapturedImage() {
        _capturedImagePath.value = null
        _classificationResult.value = null
    }

    /**
     * Tallentaa nykyisen luontolöydön Room-tietokantaan.
     * Luo NatureSpot-entiteetin ML Kit -tunnistustuloksen perusteella.
     */
    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        viewModelScope.launch {
            val result = _classificationResult.value

            // Luodaan NatureSpot tunnistustuloksen perusteella
            val spot = NatureSpot(
                name = when (result) {
                    is ClassificationResult.Success -> result.label
                    else -> "Luontolöytö"
                },
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence,
                comment = _comment.value.ifBlank { null }
            )
            repository.insertSpot(spot)
            _comment.value = ""
            clearCapturedImage()
        }
    }

    /** Vapauttaa ML Kit -resurssit ViewModelin tuhoutuessa. */
    override fun onCleared() {
        super.onCleared()
        locationManager.stopTracking()
        classifier.close()
        realtimeLabeler.close()
        analysisExecutor.shutdown()

    }
}