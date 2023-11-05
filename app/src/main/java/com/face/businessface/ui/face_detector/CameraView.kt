package com.face.businessface.ui.face_detector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import kotlin.math.roundToInt

@SuppressLint("RestrictedApi")
@Composable
fun CameraView(
    context: Context,
    analyzer: ImageAnalysis.Analyzer?,
    lensFacing: Int,
    lifecycleOwner: LifecycleOwner,
    cameraExit: Boolean? = false,
    showScreenSHot: (Bitmap?) -> Unit,
    scale: PreviewView.ScaleType? = PreviewView.ScaleType.FILL_CENTER,
    viewPortCorrection: (Int) -> Unit
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
    val screenWidth by remember { mutableIntStateOf(context.resources.displayMetrics.widthPixels) }
    val width  = context.resources.displayMetrics.heightPixels
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
            Log.d("VIEWPORT_RATIO_HEIGHT",width.toString())
            Log.d("VIEWPORT_RATIO",screenWidth.toString())

            it.resolutionInfo?.cropRect?.width()?.let { it1 ->
                val dif = width-it1
                val ratio = it1.toFloat()/width.toFloat()
                viewPortCorrection.invoke((dif*ratio).roundToInt())
            }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
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
                        preview
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
                }, executor)
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                previewView
            },
        )
    }
}