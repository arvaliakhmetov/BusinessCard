package com.face.businessface.ui.face_detector.analyzer

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

@SuppressLint("UnsafeOptInUsageError")
class FaceDetectionAnalyzer(
    private val onFaceDetected: (faces: MutableList<Face>, width: Int, height: Int,imageProxy: ImageProxy) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(2F)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .enableTracking()
        .build()

    private val faceDetector = FaceDetection.getClient(options)
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
                                onFaceDetected(faces, _image.width, _image.height, imageProxy)
                                imageProxy.close()
                            }
                        }
                    }
            }
    }

}