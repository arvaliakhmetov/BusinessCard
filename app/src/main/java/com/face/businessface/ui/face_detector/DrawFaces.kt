package com.face.businessface.ui.face_detector

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun DrawFaces(
    faces: List<Face>,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    viewPortCor: Int,
    lens: Int,
    stopCamera: Boolean,
    loadingCompete: Boolean = false,
    onFaceClick:(Face, Float, Float, Float, Float, Float) -> Unit,
) {
    val density = LocalContext.current.resources.displayMetrics.density
    val alpha: Float by animateFloatAsState(if (stopCamera && faces.size == 1) 1f else 0.5f, label = "bg_alpha", animationSpec = tween(800))
    val radiusExt: Float by animateFloatAsState(if (stopCamera && faces.size == 1) 10f else 0f, label = "bg_radExt", animationSpec = tween(500))
    var showLoader by remember { mutableStateOf(false) }
    var hasClickableFace by remember{ mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(stopCamera){
        if (stopCamera){
            delay(800)
            showLoader = true
        }
    }
    LaunchedEffect(hasClickableFace){
        if(hasClickableFace){
            Toast.makeText(context,"Тапните по выделенному лицу", Toast.LENGTH_SHORT).show()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
        ) {
            val scaleFactor = min(
                (screenWidth + viewPortCor).toFloat() / imageWidth,
                screenHeight.toFloat() / imageHeight
            )
            val offsetX = (screenWidth - (imageWidth * scaleFactor)) / 2
            val offsetY = (screenHeight - (imageHeight * scaleFactor)) / 2
            val outsidePath = Path()
            faces.forEach { face ->
                val boundingBox = face.boundingBox.toComposeRect()
                val centerY = boundingBox.center.y * scaleFactor + offsetY
                when (lens) {
                    CameraSelector.LENS_FACING_FRONT -> {
                        val centerX =
                            screenWidth + viewPortCor - boundingBox.center.x * scaleFactor + offsetX
                        val radius = ((boundingBox.width / 2.0) * scaleFactor).toFloat()
                        outsidePath.addOval(
                            Rect(
                                Offset(
                                    (centerX - radius - radiusExt),
                                    (centerY - radius - radiusExt)
                                ),
                                Size(
                                    (2 * (radius + radiusExt)),
                                    (2 * (radius + radiusExt))
                                )
                            )
                        )
                    }
                    else -> {
                        val centerX = boundingBox.center.x * scaleFactor + offsetX
                        val radius = ((boundingBox.width / 2.0) * scaleFactor).toFloat()
                        try {
                            val rect = Rect(
                                Offset(
                                    (centerX - radius - radiusExt),
                                    (centerY - radius - radiusExt)
                                ),
                                Size(
                                    (2 * (radius + radiusExt)),
                                    (2 * (radius + radiusExt))
                                )
                            )
                            outsidePath.addOval(rect)
                        } catch (_: Throwable){}

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
                if(face.headEulerAngleZ in -30f..30f){
                    val scaleFactor = min(
                        (screenWidth + viewPortCor).toFloat() / imageWidth,
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
                                screenWidth + viewPortCor - boundingBox.center.x * scaleFactor + offsetX
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
                                val x = (centerX - placeable.width / 2).toInt()
                                val y = (centerY - placeable.height / 2).toInt()
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(x, y)
                                }
                            }
                    ) {
                        if (
                            radius + centerX < screenWidth - 30 &&
                            centerX - radius > 30 &&
                            radius + centerY < screenHeight - 10 &&
                            centerY-radius > 10 &&
                            face.headEulerAngleX in -20f..20f &&
                            abs(face.headEulerAngleY) < 25f &&
                            face.boundingBox.width() > 80 &&
                            radius * 2< screenWidth-40 &&
                            face.headEulerAngleZ in -19f..19f
                        ) {
                            hasClickableFace = true
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
                        }else{
                            hasClickableFace = false
                        }
                        AnimatedVisibility(
                            visible = loadingCompete,
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
                    if (showLoader) {
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
                            AnimatedVisibility(
                                visible = !loadingCompete,
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
}