package com.face.businessface.ui.face_recognizer

import FaceDirection
import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face

abstract class VisionBaseProcessor<T> {
    abstract fun detectInImage(faces: List<Face>, bitmap: Bitmap,rotation: Float? = null,faceDirection: FaceDirection? = null):FloatArray?
}