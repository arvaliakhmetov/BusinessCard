package com.face.businessface.ui.face_detector

import FaceRecognitionProcessor
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.mvi.State
import com.face.businessface.navigation.componentCoroutineScope
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FaceRecognitionComponent (
    componentContext: ComponentContext,
    private val cardInfoRepository: CardInfoRepository,
    private val model: FaceRecognitionProcessor,
    private val onNavigateToRecognizedFace: (id: Long) -> Unit,
    private val onClose: () -> Unit
): ComponentContext by componentContext {

    private var _state= MutableValue(FaceRecognitionScreenState(neyro = model))
    val state: Value<FaceRecognitionScreenState> = _state
    private var _action = MutableValue<RecognitionScreenAction>(RecognitionScreenAction.Init)


    init {
        _action.observe { action ->
            val curState = _state.value
            _state.value = reduce(action,curState)
            componentContext.componentCoroutineScope().launch {
                dispatchAction(action,curState)
            }
        }
    }

    fun onAction(action: RecognitionScreenAction){
        _action.value = action
    }

    private fun reduce(action: RecognitionScreenAction, _state: FaceRecognitionScreenState): FaceRecognitionScreenState{
        return when (action){
            is RecognitionScreenAction.OnSetNeyroModel -> _state.copy(neyro = model)
            is RecognitionScreenAction.OnSetDetectedFacesRects -> _state.copy(imageProxy = action.bitmap, faces = action.faces)
            is RecognitionScreenAction.OnCameraFlip -> _state.copy(flip = action.flip)
            is RecognitionScreenAction.OnSaveScreen -> _state.copy(
                screenShot = action.bitmap,
            )
            is RecognitionScreenAction.OnAnalyze -> _state.copy(
                selectedFace = action.face,
                needCamera = false,
                needAnalyzer = false,
                showSwitch = false
            )
            is RecognitionScreenAction.OnAnalyzeResult -> _state.copy(
                isCompleteLoading = true,
                faceFound = action.id
            )

            RecognitionScreenAction.Init -> _state
            RecognitionScreenAction.OnClose -> _state
        }

    }

    private suspend fun dispatchAction(action: RecognitionScreenAction, state: FaceRecognitionScreenState){
        when (action){
            is RecognitionScreenAction.OnSetNeyroModel -> Unit
            is RecognitionScreenAction.OnSetDetectedFacesRects -> Unit
            is RecognitionScreenAction.OnSaveScreen -> Unit
            is RecognitionScreenAction.OnAnalyze -> onAction(analyze(action.face,state.flip,state.neyro!!,state.imageProxy!!))
            is RecognitionScreenAction.OnAnalyzeResult -> {
                delay(500)
                _state.value = FaceRecognitionScreenState(neyro = model)
                _action.value = RecognitionScreenAction.Init
                onNavigateToRecognizedFace(action.id!!)
            }

            RecognitionScreenAction.Init -> Unit
            is RecognitionScreenAction.OnCameraFlip -> Unit
            RecognitionScreenAction.OnClose -> {
                _state.value = FaceRecognitionScreenState(neyro = model)
                _action.value = RecognitionScreenAction.Init
                onClose()
            }
        }
    }
    @OptIn(ExperimentalGetImage::class)
    suspend fun analyze(
        face: Face,
        flip: Boolean,
        faceProcessor: FaceRecognitionProcessor,
        imageProxy: Bitmap,
    ): RecognitionScreenAction {
        val nearestFace = cardInfoRepository.getNearestFace(
            face,
            flip,
            faceProcessor,
            imageProxy
        )
        delay(1000)
        return RecognitionScreenAction.OnAnalyzeResult(id = nearestFace)

    }
}





