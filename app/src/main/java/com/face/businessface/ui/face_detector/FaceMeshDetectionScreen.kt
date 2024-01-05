package com.face.businessface.ui.face_detector


import FaceRecognitionProcessor
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.value.Value
import com.face.businessface.R
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.entity.toPerson
import com.face.businessface.mvi.Action
import com.face.businessface.ui.face_detector.analyzer.FaceDetectionAnalyzer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.reflect.KFunction1


@androidx.annotation.OptIn(ExperimentalZeroShutterLag::class)
@Composable
fun RecognitionScreen(
    componentState: Value<FaceRecognitionScreenState>,
    onAction: (action: RecognitionScreenAction) -> Unit,
) {

    val state by componentState.subscribeAsState()
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    val scope = rememberCoroutineScope()
    val screenWidth by remember { mutableIntStateOf(context.resources.displayMetrics.widthPixels) }
    val screenHeight by remember { mutableIntStateOf(context.resources.displayMetrics.heightPixels) }

    val imageWidth = remember { mutableIntStateOf(0) }
    val viewPortCorrector = remember { mutableIntStateOf(0) }
    val imageHeight = remember { mutableIntStateOf(0) }
    var centerX by remember{ mutableFloatStateOf(0F) }
    var centerY by remember{ mutableFloatStateOf(0F) }
    var rawCenterX by remember{ mutableFloatStateOf(0F) }
    var rawCenterY by remember{ mutableFloatStateOf(0F) }
    var radius by remember { mutableFloatStateOf(0f) }
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
            state.neyro?.let {
                CameraView(
                    context = context,
                    lensFacing = lensFacing,
                    lifecycleOwner = lifecycleOwner,
                    analyzer = FaceDetectionAnalyzer { faces, width, height, imageProxy ->
                        imageWidth.intValue = width
                        imageHeight.intValue = height
                        onAction(RecognitionScreenAction.OnSetDetectedFacesRects(imageProxy.toBitmap(),faces))
                    },
                    cameraExit = !state.needCamera,
                    showScreenSHot = {
                        onAction(RecognitionScreenAction.OnSaveScreen(it))
                    }
                ) { viewPortWidthCorrector ->
                    viewPortCorrector.intValue = viewPortWidthCorrector
                }
            }

            state.screenShot?.let {
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
                        faces = if (!state.needCamera) state.faces.filter { it.trackingId == state.selectedFace?.trackingId } else state.faces,
                        imageHeight.intValue,
                        imageWidth.intValue,
                        screenWidth,
                        screenHeight,
                        viewPortCorrector.intValue,
                        lensFacing,
                        !state.needCamera,
                        state.isCompleteLoading
                    ) { face, _centerX, _centerY, _radius, _rawCenterX, _rawCenterY ->
                        scope.launch {
                            onAction(RecognitionScreenAction.OnAnalyze(face = face))
                            delay(600)
                            centerX = _centerX
                            centerY = _centerY
                            radius = _radius
                            rawCenterX = _rawCenterX
                            rawCenterY = _rawCenterY
                        }

                    }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {

            IconButton(onClick = { onAction(RecognitionScreenAction.OnClose) }) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            if(state.showSwitch){
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ){
                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            // Toggle between front and back lenses
                            lensFacing = if (!state.flip) {
                                onAction(RecognitionScreenAction.OnCameraFlip(flip = true))
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                onAction(RecognitionScreenAction.OnCameraFlip(flip = false))
                                CameraSelector.LENS_FACING_BACK
                            }
                        }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.switch_icon),
                            modifier = Modifier.size(24.dp),
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}






