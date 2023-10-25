package com.face.businesscard.ui.face_detector.analyzer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageUtils
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import crop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@SuppressLint("UnsafeOptInUsageError")
class FaceDetectionAccurateAnalyzer(
    private val onFaceDetected: (faces: MutableList<Face>, width: Int, height: Int,imageProxy: Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(2F)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    val faceDetector = FaceDetection.getClient(options)
    var frameCounter = 0

    override fun analyze(imageProxy: ImageProxy) {
            imageProxy.image?.let {_image ->
                val image = InputImage.fromMediaImage(_image, imageProxy.imageInfo.rotationDegrees,)
                faceDetector.process(image)
                    .addOnFailureListener {
                        imageProxy.close()
                    }.addOnSuccessListener { faces ->

                    }.addOnCompleteListener {
                        if (it.isSuccessful){
                            it.addOnSuccessListener {faces ->
                                onFaceDetected(faces, _image.width, _image.height, imageProxy.toBitmap())
                                imageProxy.close()
                            }
                        }
                    }
            }
    }

    fun analyzeFrame(image: Bitmap): Pair<MutableList<Face>,Bitmap>{
        val image1 = InputImage.fromBitmap(image,0)
        val task = faceDetector.process(image1)
            if (task.isSuccessful){
                return Pair(task.result,image)
            }else{
                return Pair(mutableListOf(),image)
            }
    }

}