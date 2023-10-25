package com.face.businesscard.ui.face_detector.utils

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect

data class MatchedFaceInfo(
    val croppedImage: Bitmap? = null,
    val rect: Rect? = null,
)