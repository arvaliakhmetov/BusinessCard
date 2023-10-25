package com.face.businesscard.ui.face_detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.util.Size
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.impl.UseCaseConfigFactory
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@ExperimentalZeroShutterLag
class ImageWithCropCapture(
    private val callback: OnImageCapturedCallback
){
    val resolutionSelector = ResolutionSelector.Builder()
        .setResolutionStrategy(ResolutionStrategy(Size( 1024,768),ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER))
        .build()
    val executor = Executors.newSingleThreadExecutor()
    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(CAPTURE_MODE_ZERO_SHUTTER_LAG)
        .setFlashMode(FLASH_MODE_OFF)
        .setResolutionSelector(resolutionSelector)
        .build()
    fun captureImage() {
            imageCapture.takePicture(
                executor,
                callback
            )
    }
}