package com.islamic.bangla.presentation.screen

import android.Manifest
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.viewmodel.QiblaViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    onBack: () -> Unit,
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var permissionChecked by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionChecked = true
        viewModel.refreshLocation()
    }

    DisposableEffect(Unit) {
        viewModel.start()
        onDispose { viewModel.stop() }
    }

    val hasPermission = remember(permissionChecked) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Angle of the Kaaba relative to the phone's current heading.
    val relativeAngle = ((uiState.qiblaBearing - uiState.azimuth + 540) % 360) - 180
    val aligned = abs(relativeAngle) < 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("কিবলা") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { padding ->
        if (!uiState.sensorAvailable) {
            Box(modifier = Modifier.padding(padding)) {
                EmptyState(
                    title = "কম্পাস সেন্সর নেই",
                    message = "আপনার ডিভাইসে ম্যাগনেটোমিটার সেন্সর না থাকায় কিবলা কম্পাস চালানো যাচ্ছে না"
                )
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "সঠিক কিবলার দিকের জন্য অবস্থানের অনুমতি দিন (এখন ঢাকার দিক দেখানো হচ্ছে)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Text("অনুমতি দিন")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            CompassDial(
                azimuth = uiState.azimuth,
                qiblaBearing = uiState.qiblaBearing,
                aligned = aligned
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (aligned) "✓ আপনি কিবলামুখী" else "কাবা ঘরের দিকে ঘুরুন",
                style = MaterialTheme.typography.titleLarge,
                color = if (aligned) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "কিবলা: ${uiState.qiblaBearing.toInt()}° • ${uiState.locationLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uiState.accuracyLow) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "কম্পাস ক্যালিব্রেট করতে ফোনটি 8 আকৃতিতে কয়েকবার ঘোরান",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CompassDial(azimuth: Float, qiblaBearing: Double, aligned: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val error = MaterialTheme.colorScheme.error

    val textPaint = remember(onSurface) {
        Paint().apply {
            color = onSurface.toArgb()
            textSize = 42f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    Canvas(modifier = Modifier.size(300.dp)) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Outer dial (rotates opposite to heading so N points to real north).
        rotate(-azimuth, center) {
            drawCircle(color = surfaceVariant, radius = radius, center = center)
            drawCircle(
                color = onSurface.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = 3f)
            )
            // Ticks every 15 degrees.
            for (deg in 0 until 360 step 15) {
                val rad = Math.toRadians(deg.toDouble())
                val long = deg % 90 == 0
                val start = center + Offset(
                    cos(rad).toFloat() * radius * if (long) 0.82f else 0.90f,
                    sin(rad).toFloat() * radius * if (long) 0.82f else 0.90f
                )
                val end = center + Offset(
                    cos(rad).toFloat() * radius * 0.97f,
                    sin(rad).toFloat() * radius * 0.97f
                )
                drawLine(
                    color = if (long) onSurface else onSurface.copy(alpha = 0.4f),
                    start = start,
                    end = end,
                    strokeWidth = if (long) 6f else 3f
                )
            }
            // Cardinal letters.
            val letters = listOf("N" to 270.0, "E" to 0.0, "S" to 90.0, "W" to 180.0)
            letters.forEach { (letter, deg) ->
                val rad = Math.toRadians(deg)
                drawContext.canvas.nativeCanvas.drawText(
                    letter,
                    center.x + cos(rad).toFloat() * radius * 0.68f,
                    center.y + sin(rad).toFloat() * radius * 0.68f + 15f,
                    textPaint
                )
            }
            // Kaaba marker at the qibla bearing (black cube with gold band).
            rotate(qiblaBearing.toFloat(), center) {
                val cubeSize = radius * 0.16f
                val top = center.y - radius * 0.55f
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(center.x - cubeSize / 2, top - cubeSize / 2),
                    size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize)
                )
                drawRect(
                    color = Color(0xFFC9A227),
                    topLeft = Offset(center.x - cubeSize / 2, top - cubeSize / 2),
                    size = androidx.compose.ui.geometry.Size(cubeSize, cubeSize * 0.25f)
                )
            }
        }

        // Fixed needle pointing up (phone's heading).
        val needleColor = if (aligned) primary else error
        val needlePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, center.y - radius * 0.42f)
            lineTo(center.x - 22f, center.y - radius * 0.10f)
            lineTo(center.x + 22f, center.y - radius * 0.10f)
            close()
        }
        drawPath(path = needlePath, color = needleColor)
        drawCircle(color = needleColor, radius = 12f, center = center)
    }
}
