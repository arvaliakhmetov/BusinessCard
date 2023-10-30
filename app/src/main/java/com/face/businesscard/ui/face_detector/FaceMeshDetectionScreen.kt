package com.face.businesscard.ui.face_detector

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toRect
import androidx.hilt.navigation.compose.hiltViewModel
import com.face.businesscard.R
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.ui.ApiResponse
import com.face.businesscard.ui.face_detector.analyzer.FaceDetectionAnalyzer
import com.face.businesscard.ui.face_recognizer.Person
import com.google.mlkit.vision.face.Face
import flip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.min
import kotlin.math.roundToInt


@androidx.annotation.OptIn(ExperimentalZeroShutterLag::class) @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScanSurface(
    navigateBack:() -> Unit,
    navigateToRecognizedFacesScreen:(person: PersonDto) -> Unit,
    viewModel: FaceRecognitionViewModel = hiltViewModel(),
) {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentSheetValue = remember {
        SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
    }

    val findResponse by viewModel.recognisedFaceResponse.collectAsState()
    var stopCamera by remember { mutableStateOf(false) }
    val recognitionModel = FileUtil.loadMappedFile(context, "mobile_face_net.tflite")
    val scope = rememberCoroutineScope()
    val imageCaptureUseCase = viewModel.imageCapture

    val screenWidth by remember { mutableStateOf(context.resources.displayMetrics.widthPixels) }
    val screenHeight by remember { mutableStateOf(context.resources.displayMetrics.heightPixels) }
    val facesList = remember { mutableStateListOf<Face>() }
    var selectedFace by remember { mutableStateOf<Face?>(null) }
    val imageWidth = remember { mutableStateOf(0) }
    val capturedBitmap by viewModel.capturedImage.collectAsState()
    val imageHeight = remember { mutableStateOf(0) }
    var centerX by remember{ mutableStateOf(0F) }
    var centerY by remember{ mutableStateOf(0F) }
    var rawCenterX by remember{ mutableStateOf(0F) }
    var rawCenterY by remember{ mutableStateOf(0F) }
    var radius by remember {mutableStateOf(0f)}
    var loadingCompete by remember { mutableStateOf(false) }
    val offsetX: Float by animateFloatAsState(
        if (centerX != 0F)
            if(lensFacing == CameraSelector.LENS_FACING_FRONT)
                centerX- screenWidth/2
            else
                -centerX+ screenWidth/2
        else 0F,
        label = "bg_alpha",
        animationSpec = tween(800)
    )
    val offsetY: Float by animateFloatAsState(
        if (centerY != 0F)
            centerY
        else 0F,
        label = "bg_alpha",
        animationSpec = tween(800)
    )


    LaunchedEffect(capturedBitmap){
        capturedBitmap?.let {
            stopCamera = true
            delay(1000)
            try {
                val image = cropToBBox(it,Rect(
                    Offset(
                        (rawCenterX - imageWidth.value/2),
                        (rawCenterY - imageHeight.value/2)
                    ),
                    Size(
                        (imageWidth.value).toFloat(),
                        (imageHeight.value).toFloat()
                    )
                ).toAndroidRectF().toRect(),0f)
                image?.let {
                    it.flip(horizontal = false).getOrNull()?.let {bitmap ->
                        viewModel.analyze(selectedFace!!, lensFacing == CameraSelector.LENS_FACING_FRONT)
                    }
                }
            } catch (_:Throwable){}
        }
    }

    LaunchedEffect(findResponse){
        if(findResponse is ApiResponse.Success){
            delay(1000)
            loadingCompete = true
            delay(1000)
            navigateToRecognizedFacesScreen((findResponse as ApiResponse.Success<PersonDto>).data)
        }
    }

    SideEffect {
        if (viewModel.faceProcessor == null) viewModel.setRecognitionModel(recognitionModel)
    }


        Box(
            modifier = Modifier
        ) {
            Box(modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val x = offsetX
                    val y = offsetY
                    layout((placeable.width), (placeable.height)) {
                        placeable.placeRelative(x.roundToInt(), y.roundToInt())
                    }
                }
            ) {

                    CameraView(
                        context = context,
                        lensFacing = lensFacing,
                        lifecycleOwner = lifecycleOwner,
                        analyzer = FaceDetectionAnalyzer { faces, width, height, imageProxy ->
                            facesList.clear()
                            facesList.addAll(faces)
                            imageWidth.value = width
                            imageHeight.value = height
                            viewModel.setDetectedFacesRects(
                                faces = faces,
                                imageWidth = width,
                                imageHeight = height,
                                screenWidth = screenWidth,
                                screenHeight = screenHeight,
                                _imageProxy = imageProxy.toBitmap()
                            )
                        },
                        imageCapture = imageCaptureUseCase,
                        cameraExit = stopCamera,
                        showScreenSHot = {
                            viewModel.saveScreen(it)
                        }
                    )

                capturedBitmap?.let {
                    Image(
                        modifier = Modifier.fillMaxHeight(),
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        alignment = Alignment.Center
                    )
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    DrawFaces(
                        faces = if (stopCamera) facesList.filter { it.trackingId == selectedFace?.trackingId } else facesList,
                        imageHeight.value,
                        imageWidth.value,
                        screenWidth,
                        screenHeight,
                        lensFacing,
                        stopCamera,
                        loadingCompete
                    ) { face, _centerX, _centerY, _radius,_rawCenterX,_rawCenterY ->
                        scope.launch {
                            selectedFace = face
                            stopCamera = true
                            delay(800)
                            centerX = _centerX
                            centerY = _centerY
                            radius = _radius
                            rawCenterX = _rawCenterX
                            rawCenterY = _rawCenterY
                        }

                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = !viewModel.needAnalyzer.collectAsState().value,
                        enter = slideInVertically { -it }.plus(fadeIn()),
                        exit = slideOutVertically { -it }.plus(fadeOut())
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 24.dp)
                                .background(Color.White, RoundedCornerShape(20.dp)),
                            text = "  Поиск остановлен  ",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily(Font(R.font.inter)),
                                fontWeight = FontWeight(600),
                                color = Color(0xFF000000),
                            )
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.padding(20.dp),
                            onClick = {
                                // Toggle between front and back lenses
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.switch_icon),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = navigateBack) {
                                Icon(
                                    modifier = Modifier.size(64.dp),
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }

                }
            }

        }
    }

@Composable
fun DrawFaces(
    faces: List<Face>,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    lens: Int,
    stopCamera: Boolean,
    loadingCompete: Boolean = false,
    onFaceClick:(Face, Float, Float, Float,Float,Float) -> Unit,
) {
    val density = LocalContext.current.resources.displayMetrics.density
    val alpha: Float by animateFloatAsState(if (stopCamera && faces.size == 1) 1f else 0.5f, label = "bg_alpha", animationSpec = tween(800))
    val radiusExt: Float by animateFloatAsState(if (stopCamera && faces.size == 1) 10f else 0f, label = "bg_radExt", animationSpec = tween(500))
    var showLoader by remember { mutableStateOf(false) }

    LaunchedEffect(stopCamera){
        if (stopCamera){
            delay(800)
            showLoader = true
        }
    }


        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(IntrinsicSize.Max)
            ) {
                val scaleFactor = min(
                    (screenWidth + 480).toFloat() / imageWidth,
                    screenHeight.toFloat() / imageHeight
                )
                val offsetX = (screenWidth - (imageWidth * scaleFactor)) / 2
                val offsetY = (screenHeight - (imageHeight * scaleFactor)) / 2
                val outsidePath = Path()
                faces.forEach { face ->
                    val boundingBox = face.boundingBox.toComposeRect()
                    val centerX: Float
                    val centerY = boundingBox.center.y * scaleFactor + offsetY
                    val radius: Float

                    when (lens) {
                        CameraSelector.LENS_FACING_FRONT -> {
                            centerX =
                                screenWidth + 480 - boundingBox.center.x * scaleFactor + offsetX
                            radius = ((boundingBox.width / 2.0) * scaleFactor).toFloat()

                            // ClipPath setup for the front-facing camera
                            outsidePath.addOval(
                                Rect(
                                    Offset(
                                        (centerX - radius - radiusExt).toFloat(),
                                        (centerY - radius - radiusExt).toFloat()
                                    ),
                                    Size(
                                        (2 * (radius + radiusExt)).toFloat(),
                                        (2 * (radius + radiusExt)).toFloat()
                                    )
                                )
                            )
                        }

                        else -> {
                            centerX = boundingBox.center.x * scaleFactor + offsetX
                            radius = ((boundingBox.width / 2.0) * scaleFactor).toFloat()

                            // ClipPath setup for the rear-facing camera
                            outsidePath.addOval(
                                Rect(
                                    Offset(
                                        (centerX - radius - radiusExt).toFloat(),
                                        (centerY - radius - radiusExt).toFloat()
                                    ),
                                    Size(
                                        (2 * (radius + radiusExt)).toFloat(),
                                        (2 * (radius + radiusExt)).toFloat()
                                    )
                                )
                            )
                        }
                    }
                }

                // Common code for both conditions
                clipPath(outsidePath, clipOp = ClipOp.Difference) {
                    drawRect(
                        color = Color.Black,
                        alpha = alpha,
                        topLeft = Offset(0f, 0f),
                        size = Size(screenWidth.toFloat(), screenHeight.toFloat())
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                faces.forEach { face ->
                    val scaleFactor = min(
                        (screenWidth + 480).toFloat() / imageWidth,
                        screenHeight.toFloat() / imageHeight
                    )
                    val offsetX = (screenWidth - (imageWidth * scaleFactor)) / 2
                    val offsetY = (screenHeight - (imageHeight * scaleFactor)) / 2
                    val boundingBox = face.boundingBox.toComposeRect()
                    val centerX: Float
                    val centerY = boundingBox.center.y * scaleFactor + offsetY
                    val radius: Float
                    when (lens) {
                        CameraSelector.LENS_FACING_FRONT -> {
                            centerX =
                                screenWidth + 480 - boundingBox.center.x * scaleFactor + offsetX
                            radius = ((boundingBox.width / 2.0) * scaleFactor).toFloat()
                        }

                        else -> {
                            centerX = boundingBox.center.x * scaleFactor + offsetX
                            radius = ((boundingBox.width / 2.0) * scaleFactor).toFloat()
                        }
                    }
                    Box(
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                val x = centerX - placeable.width / 2
                                val y = centerY - placeable.height / 2
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(x.roundToInt(), y.roundToInt())
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .size(((radius + radiusExt) * 2 / density).dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Yellow, CircleShape)
                                .clickable {
                                    onFaceClick.invoke(
                                        face,
                                        boundingBox.center.x * scaleFactor + offsetX,
                                        screenHeight / 2 - boundingBox.center.y * scaleFactor + offsetY,
                                        radius,
                                        centerX,
                                        centerY
                                    )
                                }
                        ) {

                        }
                        AnimatedVisibility(visible = loadingCompete,
                            enter = fadeIn(tween(500))
                            ) {
                            Row(
                                modifier = Modifier
                                    .size(((radius + radiusExt) * 2 / density).dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Green, CircleShape)
                                    .clickable {
                                        onFaceClick.invoke(
                                            face,
                                            boundingBox.center.x * scaleFactor + offsetX,
                                            screenHeight / 2 - boundingBox.center.y * scaleFactor + offsetY,
                                            radius,
                                            centerX,
                                            centerY
                                        )
                                    }
                            ) {

                            }
                        }

                    }
                    if(showLoader) {
                    Box(
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                val x = centerX - placeable.width / 2
                                val y = centerY - placeable.height / 2
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(x.roundToInt(), y.roundToInt())
                                }
                            }
                    ) {
                        AnimatedVisibility(visible = !loadingCompete,
                            exit = fadeOut(tween(200))
                        ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(((radius + radiusExt) * 2 / density).dp + 40.dp),
                                    strokeWidth = 5.dp,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }


}





