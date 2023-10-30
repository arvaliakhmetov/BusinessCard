package com.face.businesscard.ui.face_detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.ScaleGestureDetector
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.impl.CameraConfig
import androidx.camera.core.impl.ExtendedCameraConfigProviderStore
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun CameraView(
    context: Context,
    analyzer: ImageAnalysis.Analyzer?,
    lensFacing: Int,
    lifecycleOwner: LifecycleOwner,
    cameraExit: Boolean? = false,
    imageCapture: ImageWithCropCapture?,
    showScreenSHot: (Bitmap?) -> Unit,
    scale: PreviewView.ScaleType? = PreviewView.ScaleType.FILL_CENTER
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageAnalysis: ImageAnalysis? = remember { ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
            analyzer?.let {
                setAnalyzer(cameraExecutor, analyzer)
            }

        }
    }

    val defaultImageCapture = remember { imageCapture?.imageCapture?: ImageCapture.Builder().build()
    }
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = ContextCompat.getMainExecutor(context)
    val cameraProvider = cameraProviderFuture.get()
    val previewView by remember {
        mutableStateOf(
            PreviewView(context).apply {
                scaleType = scale!!
            }
        )
    }
    LaunchedEffect(previewView.viewPort){
        preview?.let {
            Log.d("VIEWPORT_RATIO",preview?.resolutionInfo.toString())
        }
    }


    LaunchedEffect(lensFacing,cameraExit) {
        if(cameraExit == true) {
            cameraProvider.unbindAll()
            showScreenSHot(previewView.bitmap)
            previewView.alpha = 0f
        }else {
            Log.d("lens", lensFacing.toString())
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis,
                defaultImageCapture
            )
            val scaleGestureDetector = ScaleGestureDetector(context,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val scale =
                            camera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                        camera.cameraControl.setZoomRatio(scale)
                        return true
                    }
                })
            previewView.setOnTouchListener { view, event ->
                view.performClick()
                scaleGestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }

            preview = Preview.Builder()
                .build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        }
    }


    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            cameraProviderFuture.addListener({
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis,
                    preview,
                    defaultImageCapture
                )
                val scaleGestureDetector = ScaleGestureDetector(context,
                    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                            val scale = camera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                            camera.cameraControl.setZoomRatio(scale)
                            return true
                        }
                    })
                previewView.setOnTouchListener { view, event ->
                    view.performClick()
                    scaleGestureDetector.onTouchEvent(event)
                    return@setOnTouchListener true
                }
            }, executor)
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            previewView
        },
    )
}