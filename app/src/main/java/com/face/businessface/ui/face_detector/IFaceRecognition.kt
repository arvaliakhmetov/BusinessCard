package com.face.businessface.ui.face_detector

import FaceRecognitionProcessor
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.mvi.Action
import com.face.businessface.mvi.State
import com.google.mlkit.vision.face.Face

data class FaceRecognitionScreenState(
    val faceCatched: Bitmap? = null,
    val faces: List<Face> = emptyList(),
    val screenShot: Bitmap? = null,
    var lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    var imageProxy: Bitmap? = null,
    val showCropError:Boolean = false,
    val neyro:FaceRecognitionProcessor? = null,
    val needAnalyzer: Boolean = true,
    val needCamera: Boolean = true,
    val showSwitch: Boolean = true,
    val flip: Boolean = false,
    val selectedFace: Face? = null,
    val isCompleteLoading: Boolean = false,
    val faceFound: Long? = null
): State
