package com.face.businesscard.ui.face_recognizer

import androidx.compose.ui.graphics.ImageBitmap

data class Person(var id: String, var faceVector: FloatArray, var faceBitmap: ImageBitmap?)
