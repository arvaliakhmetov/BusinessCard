package com.face.businessface.ui.recognized_faces_screen

import androidx.lifecycle.viewModelScope
import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.mvi.Action
import com.face.businessface.mvi.State
import com.face.businessface.navigation.Navigator
import com.face.businessface.navigation.componentCoroutineScope
import com.face.businessface.ui.face_detector.FaceRecognitionScreenState
import com.face.businessface.ui.face_detector.RecognitionScreenAction
import com.face.businessface.ui.mvicore.BaseViewModel
import com.google.android.gms.tasks.CancellationToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


class RecognizedFaceComponent (
    val personId: Long,
    componentContext: ComponentContext,
    private val cardInfoRepository: CardInfoRepository,
    private val onClose:()-> Unit
): ComponentContext by componentContext {
    private var _state = MutableValue(RecognizedFaceScreenState())
    val state: Value<RecognizedFaceScreenState> = _state
    var _action = MutableValue<RecognizedFaceScreenAction>(RecognizedFaceScreenAction.Init)

    val subscribe: Cancellation


    init {
        subscribe = _action.observe { action ->
            val curState = _state.value
            _state.value = reduce(action,curState)
            componentContext.componentCoroutineScope().launch {
                dispatchAction(action,curState)
            }
        }
    }


    fun onAction(action: RecognizedFaceScreenAction){
        _action.value = action
    }
    private fun reduce(action: RecognizedFaceScreenAction, state: RecognizedFaceScreenState): RecognizedFaceScreenState {
        return when(action){
            is RecognizedFaceScreenAction.OnDelete -> state
            is RecognizedFaceScreenAction.OnFavoriteClick -> state.copy(person = state.person!!.copy(favorite = !state.person.favorite))
            is RecognizedFaceScreenAction.OnClose -> state
            RecognizedFaceScreenAction.Init -> state
            else -> state
        }
    }

    suspend fun dispatchAction(action: RecognizedFaceScreenAction, state: RecognizedFaceScreenState) {
        when(action){
            is RecognizedFaceScreenAction.OnDelete -> cardInfoRepository.deleteCard(state.person!!.id)
            is RecognizedFaceScreenAction.OnFavoriteClick -> cardInfoRepository.setFavoriteCard(state.person!!.id,!state.person.favorite)
            is RecognizedFaceScreenAction.OnClose -> {
                subscribe.cancel()
                onClose.invoke()
            }
            RecognizedFaceScreenAction.Init -> _state.value = _state.value.copy(person = cardInfoRepository.getPersonById(personId))
        }
    }
}