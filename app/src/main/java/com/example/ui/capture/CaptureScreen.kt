package com.example.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.domain.model.SourceType
import com.example.ui.components.TechnicalStatusIndicator
import com.example.ui.theme.AcidYellow
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.LightMuted
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateBorderBright
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateElevated
import com.example.ui.theme.SubtitleGray
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.WarmOrange
import com.example.ui.viewmodel.GlanceViewModel
import java.util.concurrent.Executors

@Composable
fun CaptureScreen(
    viewModel: GlanceViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var showBoardPresets by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                @Suppress("DEPRECATION")
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.processCapturedImage(bitmap)
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    // Scanning reticle animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        if (hasCameraPermission) {
            // Live CameraX AndroidView with lifecycle binding
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setFlashMode(if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                            .build()
                        imageCapture = capture

                        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                        } catch (e: Exception) {
                            // Fallback
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = { previewView ->
                    // Rebind when lensFacing updates
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }, ContextCompat.getMainExecutor(previewView.context))
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("CAMERA REQUIRED", fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Black, color = PaperWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "GlanceFlow uses local on-device OCR to read blackboards and paper.",
                    color = SubtitleGray,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = VoidBlack),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("ENABLE CAMERA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }

        // Editorial Minimal Crosshair & Technical Framing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val fLeft = w * 0.08f
            val fRight = w * 0.92f
            val fTop = h * 0.18f
            val fBottom = h * 0.68f

            val stroke = 2.dp.toPx()
            val corner = 24.dp.toPx()

            // Top-Left Corner
            drawLine(ElectricLime, Offset(fLeft, fTop), Offset(fLeft + corner, fTop), stroke)
            drawLine(ElectricLime, Offset(fLeft, fTop), Offset(fLeft, fTop + corner), stroke)

            // Top-Right Corner
            drawLine(ElectricLime, Offset(fRight, fTop), Offset(fRight - corner, fTop), stroke)
            drawLine(ElectricLime, Offset(fRight, fTop), Offset(fRight, fTop + corner), stroke)

            // Bottom-Left Corner
            drawLine(ElectricLime, Offset(fLeft, fBottom), Offset(fLeft + corner, fBottom), stroke)
            drawLine(ElectricLime, Offset(fLeft, fBottom), Offset(fLeft, fBottom - corner), stroke)

            // Bottom-Right Corner
            drawLine(ElectricLime, Offset(fRight, fBottom), Offset(fRight - corner, fBottom), stroke)
            drawLine(ElectricLime, Offset(fRight, fBottom), Offset(fRight, fBottom - corner), stroke)

            // Laser Scan Horizontal
            val lY = fTop + (fBottom - fTop) * laserProgress
            drawLine(
                ElectricLime.copy(alpha = 0.9f),
                Offset(fLeft + 4.dp.toPx(), lY),
                Offset(fRight - 4.dp.toPx(), lY),
                2.dp.toPx()
            )
        }

        // TOP BAR CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCAN THE WORLD",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = PaperWhite,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "● OFFLINE LOCAL OCR",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLime,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Flash / Torch Toggle
                IconButton(
                    onClick = {
                        isFlashOn = !isFlashOn
                        camera?.cameraControl?.enableTorch(isFlashOn)
                    }
                ) {
                    Icon(
                        if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) AcidYellow else PaperWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Switch Camera (Front / Back)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = PaperWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Close / Back Navigation
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = PaperWhite, modifier = Modifier.size(22.dp))
                }
            }
        }

        // BOTTOM CONTROLS
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, VoidBlack.copy(alpha = 0.95f), VoidBlack)
                    )
                )
                .navigationBarsPadding()
                .padding(bottom = 24.dp, top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Secondary Actions bar: GALLERY • VOICE • TEXT • PRESETS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaptureSecondaryPill(
                    label = "GALLERY",
                    onClick = { galleryLauncher.launch("image/*") }
                )
                CaptureSecondaryPill(
                    label = "VOICE",
                    onClick = {
                        viewModel.processRawText(
                            "Remind me to submit the AI assignment Friday.",
                            SourceType.VOICE
                        )
                    }
                )
                CaptureSecondaryPill(
                    label = "TEXT",
                    onClick = {
                        viewModel.processRawText(
                            "Machine learning assignment. Build CNN classifier using CIFAR-10. Submit Friday.",
                            SourceType.TEXT
                        )
                    }
                )
                CaptureSecondaryPill(
                    label = "PRESETS",
                    accentColor = ElectricLime,
                    onClick = { showBoardPresets = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Circular Capture Control
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(VoidBlack)
                    .border(2.dp, ElectricLime, CircleShape)
                    .clickable {
                        val capture = imageCapture
                        if (capture != null) {
                            val executor = Executors.newSingleThreadExecutor()
                            capture.takePicture(
                                executor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bitmap = image.toBitmap()
                                        image.close()
                                        viewModel.processCapturedImage(bitmap)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        viewModel.processRawText(
                                            """
                                            Machine Learning Assignment
                                            Build a CNN classifier using CIFAR-10.
                                            Submit Friday.
                                            Bring printed report.
                                            """.trimIndent(),
                                            SourceType.CAMERA
                                        )
                                    }
                                }
                            )
                        } else {
                            viewModel.processRawText(
                                """
                                Machine Learning Assignment
                                Build a CNN classifier using CIFAR-10.
                                Submit Friday.
                                Bring printed report.
                                """.trimIndent(),
                                SourceType.CAMERA
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(ElectricLime)
                )
            }
        }
    }

    if (showBoardPresets) {
        DemoBoardPresetsModal(
            onSelect = { text ->
                viewModel.processRawText(text, SourceType.CAMERA)
                showBoardPresets = false
            },
            onDismiss = { showBoardPresets = false }
        )
    }
}

@Composable
private fun CaptureSecondaryPill(
    label: String,
    onClick: () -> Unit,
    accentColor: Color = PaperWhite
) {
    Surface(
        onClick = onClick,
        color = SlateDark,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = accentColor,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DemoBoardPresetsModal(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val items = listOf(
        "CLASSROOM BLACKBOARD (SPEC EXAMPLE)" to """
            AI Assignment
            Submit: Friday
            Implement CNN classifier
            Dataset: CIFAR-10
            Bring printed report
        """.trimIndent(),
        "MACHINE LEARNING ASSIGNMENT" to """
            Machine Learning Assignment
            Build a CNN classifier using CIFAR-10.
            Submit Friday.
            Bring printed report.
        """.trimIndent(),
        "LAB VIVA & TIMETABLE" to """
            Deep Learning Lab Viva
            Tomorrow at 10 AM in Lab 3
            Review ResNet architecture
            Bring lab observation record
        """.trimIndent()
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = {
            Text("BLACKBOARD SIMULATION", color = PaperWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 15.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { (name, txt) ->
                    Surface(
                        onClick = { onSelect(txt) },
                        color = SlateDark,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(name, fontWeight = FontWeight.Black, color = ElectricLime, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(txt.replace("\n", " • "), color = DarkMuted, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("CANCEL", color = DarkMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    )
}
