// 📁 camera/CameraScreen.kt
package com.example.luontopeli.camera


import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.viewmodel.CameraViewModel
import java.io.File

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val realtimeLabels by viewModel.realtimeLabels.collectAsState()
    val classificationResult by viewModel.classificationResult.collectAsState()
    val comment = viewModel.comment.collectAsState()


    // ImageCapture use case – tallennetaan muuttujaan jotta nappia painaessa voidaan käyttää
    val imageCapture = remember { ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
    }

    // Lupatarkistus
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val capturedImagePath by viewModel.capturedImagePath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()


    if (!hasCameraPermission) {
        // Lupanäkymä
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CameraAlt, contentDescription = null,
                    modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("Kameran lupa tarvitaan", modifier = Modifier.padding(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Myönnä lupa")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {


        if (capturedImagePath == null) {

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture,
                                viewModel.imageAnalyzer
                            )
                        } catch (e: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // 🔥 OVERLAY (EI PEITÄ KOKO NÄKYMÄÄ)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .background(Color(0x66000000)), // läpinäkyvä tumma tausta
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                realtimeLabels.forEach {
                    Text(
                        text = it,
                        color = Color.White
                    )
                }
            }

            // Kamera nappi
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(
                    onClick = { viewModel.takePhoto(context, imageCapture) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.Camera, "Ota kuva", tint = Color.White)
                    }
                }
            }
        } else {
            CapturedImageView(
                imagePath = capturedImagePath!!,
                onRetake = { viewModel.clearCapturedImage() },
                result = classificationResult,
                onSave = { viewModel.saveCurrentSpot() },
                comment = comment.value,
                onCommentChange = viewModel::updateComment
            )
        }
    }
}

@Composable
fun CapturedImageView(
    imagePath: String,
    result: ClassificationResult?,
    onRetake: () -> Unit,
    onSave: () -> Unit,
    comment: String,
    onCommentChange:(String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Otettu kuva
        AsyncImage(
            model = File(imagePath),
            contentDescription = "Otettu kuva",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
        )
        result?.let{
            ClassificationResultCard(it)
        }
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("Kommentti löydöstä") },
            placeholder = { Text("Esim. löytyi polun varresta") }
        )
        // Toimintopainikkeet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onRetake) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Ota uudelleen")
            }
            Button(onClick = onSave) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Tallenna löytö")
            }
        }
    }
}

@Composable
fun ClassificationResultCard(result: ClassificationResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is ClassificationResult.Success ->
                    if (result.confidence > 0.8f)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (result) {
                is ClassificationResult.Success -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tunnistettu:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.width(8.dp))
                        // Varmuustaso-badge
                        Badge(
                            containerColor = when {
                                result.confidence > 0.8f -> Color(0xFF2E7D32)
                                result.confidence > 0.6f -> Color(0xFFF57C00)
                                else -> Color(0xFFD32F2F)
                            }
                        ) {
                            Text("${"%.0f".format(result.confidence * 100)}%")
                        }
                    }

                    Text(
                        text = result.label,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Varmuuspalkki
                    LinearProgressIndicator(
                        progress = result.confidence,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = when {
                            result.confidence > 0.8f -> Color(0xFF2E7D32)
                            result.confidence > 0.6f -> Color(0xFFF57C00)
                            else -> Color(0xFFD32F2F)
                        }
                    )
                }

                is ClassificationResult.NotNature -> {
                    Text("Ei luontokohde", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Kuvassa tunnistettiin: ${result.allLabels.joinToString { it.text }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                is ClassificationResult.Error -> {
                    Text("Tunnistus epäonnistui: ${result.message}",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}