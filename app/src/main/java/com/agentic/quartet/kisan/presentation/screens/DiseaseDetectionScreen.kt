package com.agentic.quartet.kisan.presentation.screens

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiseaseDetectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var diagnosisResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val detectedDisease = stringResource(R.string.detected_leaf_spot_disease)
    val detectedDiseaseSuggestions = stringResource(R.string.suggestions)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            diagnosisResult = null
            isLoading = true
            scope.launch {
                delay(2000)
                diagnosisResult =
                    "🦠 $detectedDisease\n💊 $detectedDiseaseSuggestions"
                isLoading = false
            }
        }
    }

    val healthyCrop = stringResource(R.string.healthy_crop)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedImageUri = null
            diagnosisResult = null
            isLoading = true
            scope.launch {
                delay(2000)
                diagnosisResult = "🌿 $healthyCrop}"
                isLoading = false
            }
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.crop_disease_detection),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.gallery), color = Color.White,  fontSize = 18.sp,)
                }

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.use_camera), color = Color.White,  fontSize = 18.sp,)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            selectedImageUri?.let { uri ->
                val bitmap = remember(uri) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(2.dp, Color(0xFF81C784), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.analyzing_image), color = Color.Gray)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AnimatedVisibility(
                visible = diagnosisResult != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                diagnosisResult?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🩺 ${stringResource(R.string.diagnosis_result)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF33691E)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.tip_for_disease),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }
    }
}