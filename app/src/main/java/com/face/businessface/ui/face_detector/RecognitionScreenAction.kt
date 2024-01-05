package com.face.businessface.ui.face_detector

import FaceRecognitionProcessor
import android.graphics.Bitmap
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.mvi.Action
import com.google.mlkit.vision.face.Face
import java.nio.MappedByteBuffer

sealed class RecognitionScreenAction {

    data object Init: RecognitionScreenAction()

    data class OnAnalyze(val face: Face): RecognitionScreenAction()

    data class OnSaveScreen(val bitmap: Bitmap?): RecognitionScreenAction()

    data class OnSetDetectedFacesRects(val bitmap: Bitmap,val faces: List<Face>): RecognitionScreenAction()

    data class OnSetNeyroModel(val model: FaceRecognitionProcessor): RecognitionScreenAction()

    data class OnAnalyzeResult(val id: Long?) : RecognitionScreenAction()

    data class OnCameraFlip(val flip: Boolean) : RecognitionScreenAction()

    data object OnClose: RecognitionScreenAction()
}
