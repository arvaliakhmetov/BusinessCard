package com.face.businesscard.ui.faceRegistration

import FaceDirection
import FaceRecognitionProcessorForRegistration
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.api.ApiRepository
import com.face.businesscard.api.dto.CardCreateDto
import com.face.businesscard.api.dto.CardDataDto
import com.face.businesscard.api.dto.toJSONString
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.database.entity.CardInfo
import com.face.businesscard.database.entity.LinkEntity
import com.face.businesscard.ui.ApiResponse
import com.face.businesscard.ui.BaseViewModel
import com.face.businesscard.ui.CoroutinesErrorHandler
import com.face.businesscard.ui.face_detector.analyzer.FaceDetectionAccurateAnalyzer
import com.face.businesscard.ui.face_detector.analyzer.FaceDetectionAnalyzer
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import rotate
import java.nio.MappedByteBuffer
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CardCreationViewModel @Inject constructor(
    private val api:ApiRepository
): BaseViewModel() {

    private val coroutinesErrorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {

        }
    }
    var faceProcessor by mutableStateOf<FaceRecognitionProcessorForRegistration?>(null)
    val faces = mutableStateOf<List<Face>?>(null)
    val recognisedPositions = MutableStateFlow<MutableMap<String,FloatArray>>(mutableMapOf())
    val extraArray by mutableStateOf(mutableListOf<FloatArray>())
    val recognitionIterator = MutableStateFlow(mutableListOf(FaceDirection.FACE_CENTER))
    val sizeOfrecognizedPoses = MutableStateFlow(0)
    val showLoader = MutableStateFlow(false)
    val showCredsScreen = MutableStateFlow(false)
    val credsState = MutableStateFlow(CreditsState())
    val closeCamera = MutableStateFlow(false)
    val showContentFilter = MutableStateFlow(false)
    val itemsList= MutableStateFlow(spheresOfActivity.map { it to false}.toMutableStateMap())
    val currBitmap = MutableStateFlow<Bitmap?>(null)
    val faceNotRecognised = MutableStateFlow<FaceDirection?>(null)
    val userImage = MutableStateFlow<Bitmap?>(null)

    val createCardresponse = MutableStateFlow<ApiResponse<String>>(ApiResponse.Loading)


    fun updateSelection(pair: Pair<String,Boolean>) {
        if(itemsList.value.values.count { it == true } <= 2 || !pair.second){
            itemsList.value[pair.first] = pair.second
        }
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
            val newList = recognitionIterator.value
            viewModelScope.launch {
                if(recognitionIterator.value.size < 28) {
                    newList.add(FaceDirection.entries[recognitionIterator.value.size])
                    recognitionIterator.emit(newList)
                }
                if(recognisedPositions.value[faceDirection.name] == null){
                    recognisedPositions.value[faceDirection.name] = floatArray
                }
               if(recognisedPositions.value.keys.size == 28){
                   showChecksLoader()
               }
                sizeOfrecognizedPoses.emit(recognisedPositions.value.keys.size-1)
            }
        }
    }

    init {
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

    fun analyze(lensFacing: Int) =  FaceDetectionAccurateAnalyzer{ _faces, width, height, _image ->
        viewModelScope.launch {
            val currentDirection = recognitionIterator.value.last()
            val bitmap = _image.toBitmap().rotate(
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) 270F else 90F
            ).getOrNull()
            faces.value = _faces
            if (_faces.isNotEmpty()) {
                if(_faces.last().boundingBox.width().toDouble() in width/3.2..width/2.4){
                    faceNotRecognised.emit(null)
                    bitmap?.let {
                        val fa = faceProcessor?.detectInImage(
                            _faces,
                            it,
                            faceDirection = currentDirection
                        )
                    }
                }else{
                    if(_faces.last().boundingBox.width().toDouble() <= width/3.2) faceNotRecognised.emit(FaceDirection.FACE_FAR)
                    if(_faces.last().boundingBox.width().toDouble() >= width/2.4) faceNotRecognised.emit(FaceDirection.FACE_CLOSE)
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
    fun saveCard() = baseRequest(
        createCardresponse,
        coroutinesErrorHandler,
    ){
        val features = recognisedPositions.value.values
        features.addAll(extraArray)
        val creds = credsState.value
        val person = CardInfo(
            name = creds.name,
            surname = creds.surname,
            secondName = creds.secondName,
            company = creds.company,
            jobtitle = creds.jobtitle,
            description = creds.description,
            activities = itemsList.value.filter { it.value == true }.keys.toList(),
            links = creds.links.values.map { LinkEntity(it.name,it.link) }.toList(),
            arrayOfFeatures = features.toList()
        )
        Log.d("NEW_AR",features.size.toString())
        val card = CardCreateDto(
            name = person.name,
            image = userImage.value,
            second_name = person.secondName,
            description = person.description,
            company = creds.company,
            jobtitle = creds.jobtitle,
            surname = person.surname,
            data = CardDataDto(
                activities = person.activities,
                contacts = person.links.map { it.name to it.link }.toMap(),
                features = person.arrayOfFeatures
            ).toJSONString()
        )
        api.createPerson(card)
    }
}