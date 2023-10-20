package com.face.businesscard.ui.faceRegistration

import FaceDirection
import FaceRecognitionProcessorForRegistration
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.database.entity.CardInfo
import com.face.businesscard.database.entity.LinkEntity
import com.face.businesscard.ui.face_detector.FaceDetectionAnalyzer
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import rotate
import java.nio.MappedByteBuffer
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CardCreationViewModel @Inject constructor(
    private val cardInfoRepository: CardInfoRepository
): ViewModel() {
    var faceProcessor by mutableStateOf<FaceRecognitionProcessorForRegistration?>(null)
    val currentDirection = MutableStateFlow(FaceDirection.FACE_CENTER)
    val registeredBottom = MutableStateFlow(false)
    val registeredRight = MutableStateFlow(false)
    val registeredLeft = MutableStateFlow(false)
    val registeredTop = MutableStateFlow(false)
    val registeredCenter = MutableStateFlow(false)
    val showLoader = MutableStateFlow(false)
    val showCredsScreen = MutableStateFlow(false)
    val credsState = MutableStateFlow(CreditsState())
    val closeCamera = MutableStateFlow(false)
    val showContentFilter = MutableStateFlow(false)
    val itemsList= MutableStateFlow(spheresOfActivity.map { it to false}.toMutableStateMap())
    val faceNotRecognised = MutableStateFlow(false)


    fun updateSelection(pair: Pair<String,Boolean>) {
        if(itemsList.value.values.count { it == true } <= 2 || !pair.second){
            itemsList.value[pair.first] = pair.second
        }
    }

    val combineFaceChecks = combine(registeredBottom,registeredRight,registeredLeft,registeredTop,registeredCenter){array ->
        array.all { it == true }
    }

    val callBack = object : FaceRecognitionProcessorForRegistration.FaceRecognitionCallback{
        override fun onFaceRecognised(face: Face?, probability: Float, name: String?) {
            faceProcessor?.let {
                Log.d("узнал ЛИЦО",face?.trackingId.toString())
            }

        }

        override fun onFaceDetected(
            floatArray: FloatArray,
            faceDirection: FaceDirection
        ) {
            faceProcessor?.let {
                val string = faceProcessor!!.recognisedFaceDirectionsMap.map { "${it.key}: ${it.value}" }.joinToString(", ")
                Log.d("DETECTED",string)
                when (faceDirection){
                    FaceDirection.FACE_TOP -> viewModelScope.launch {
                        registeredTop.emit(true)
                        currentDirection.emit(FaceDirection.FACE_BOTTOM)
                    }
                    FaceDirection.FACE_CENTER ->viewModelScope.launch {
                        registeredCenter.emit(true)
                        faceNotRecognised.emit(false)
                        currentDirection.emit(FaceDirection.FACE_TOP)
                    }
                    FaceDirection.FACE_LEFT ->viewModelScope.launch {
                        registeredLeft.emit(true)
                        currentDirection.emit(FaceDirection.FACE_RIGHT)
                    }
                    FaceDirection.FACE_RIGHT ->viewModelScope.launch {
                        registeredRight.emit(true)
                        currentDirection.emit(FaceDirection.FACE_RIGHT)
                    }
                    FaceDirection.FACE_BOTTOM ->viewModelScope.launch {
                        registeredBottom.emit(true)
                        currentDirection.emit(FaceDirection.FACE_LEFT)
                    }
                    FaceDirection.FACE_NOT_RECOGNISED ->viewModelScope.launch {
                        registeredBottom.emit(false)
                        registeredRight.emit(false)
                        registeredLeft.emit(false)
                        registeredCenter.emit(false)
                        registeredTop.emit(false)
                        faceNotRecognised.emit(true)
                        currentDirection.emit(FaceDirection.FACE_CENTER)
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            combineFaceChecks.collect{
                if(it){
                    showChecksLoader()
                }
            }
        }
        viewModelScope.launch {
            credsState.collect{credits ->
                if( credits.name.isNotEmpty() &&
                    credits.description.isNotEmpty() &&
                    credits.surname.isNotEmpty()){
                    showContentFilter.emit(true)
                }
            }
        }
    }

    fun analyze(lensFacing: Int) =  FaceDetectionAnalyzer { faces, width, height, _image ->
        viewModelScope.launch {
            val bitmap = _image.toBitmap().rotate(
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) 270F else 90F
            ).getOrNull()
            if (faces.isNotEmpty()) {
                bitmap?.let {
                    faceProcessor?.detectInImage(
                        faces,
                        it,
                        faceDirection = FaceDirection.FACE_NOT_RECOGNISED
                    )

                }
                bitmap?.let {
                    faceProcessor?.detectInImage(
                        faces,
                        it,
                        faceDirection = currentDirection.value
                    )

                }
            }
        }
    }
    fun setRecognitionModel(recognitionModel: MappedByteBuffer) {
        faceProcessor = FaceRecognitionProcessorForRegistration(Interpreter(recognitionModel),callBack)
    }

    fun showChecksLoader(){
        viewModelScope.launch {
            closeCamera.emit(true)
            showLoader.emit(true)
            delay(2000)
            showLoader.emit(false)
            showCredsScreen.emit(true)
        }
    }

    fun createLink(){
        viewModelScope.launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap[creds.links.size+1] = Link()
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }
    fun deleteLink(index: Int){
        viewModelScope.launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap.remove(index)
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }

    fun addNameToLink(position: Int,name: String){
        viewModelScope.launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap[position] = newMap[position]!!.copy(name = name)
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }

    fun addLinkToLink(position: Int,link: String) {
        viewModelScope.launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap[position] = newMap[position]!!.copy(link = link)
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }
    fun setCreds(creds: CreditsState){
        viewModelScope.launch{
            credsState.emit(creds)
        }
    }

    fun showContentFilter(){
        viewModelScope.launch {
            showCredsScreen.emit(false)
        }
    }
    fun saveCard(){
        viewModelScope.launch {
            val id = Instant.now().toEpochMilli()
            val creds = credsState.value
            cardInfoRepository.insertCard(CardInfo(
                id = id,
                name = creds.name,
                surname = creds.surname,
                secondName = creds.secondName,
                description = creds.description,
                activities = itemsList.value.filter { it.value == true }.keys.toList(),
                links = creds.links.values.map { LinkEntity(it.name,it.link) }.toList(),
                arrayOfFeatures = faceProcessor!!.recognisedFaceDirectionsMap.values.toList()
            ))
        }
    }
}