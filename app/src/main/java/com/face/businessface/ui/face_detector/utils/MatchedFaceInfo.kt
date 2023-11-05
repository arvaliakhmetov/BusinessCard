package com.face.businessface.ui.face_detector.utils

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect

data class MatchedFaceInfo(
    val croppedImage: Bitmap? = null,
    val rect: Rect? = null,
)