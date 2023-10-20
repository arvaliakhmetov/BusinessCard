package com.face.businesscard.ui.face_recognizer

import FaceDirection
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.face.Face

abstract class VisionBaseProcessor<T> {
    abstract fun detectInImage(faces: List<Face>, bitmap: Bitmap,rotation: Float? = null,faceDirection: FaceDirection? = null):Boolean
}