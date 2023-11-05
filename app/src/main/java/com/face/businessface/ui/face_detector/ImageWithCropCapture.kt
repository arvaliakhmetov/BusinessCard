package com.face.businessface.ui.face_detector

import android.util.Size
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
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