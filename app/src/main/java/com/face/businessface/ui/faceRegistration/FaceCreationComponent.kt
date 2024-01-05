package com.face.businessface.ui.faceRegistration

import FaceDirection
import FaceRecognitionProcessorForRegistration
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.viewModelScope
import com.arkivanov.decompose.ComponentContext
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.database.entity.LinkEntity
import com.face.businessface.navigation.componentCoroutineScope
import com.face.businessface.ui.ApiResponse
import com.face.businessface.ui.mvicore.CoroutinesErrorHandler
import com.face.businessface.ui.face_detector.analyzer.FaceDetectionAccurateAnalyzer
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.tensorflow.lite.Interpreter
import rotate
import java.nio.MappedByteBuffer
import javax.inject.Inject

class FaceCreationComponent (
    private val model: MappedByteBuffer,
    componentContext: ComponentContext,
    private val cardInfoRepository: CardInfoRepository,
    private val onSaveCard:(id:Long)-> Unit
): ComponentContext by componentContext {


    var faceProcessor by mutableStateOf<FaceRecognitionProcessorForRegistration?>(null)
    val faces = mutableStateOf<List<Face>?>(null)
    val recognisedPositions = MutableStateFlow<MutableMap<String,FloatArray>>(mutableMapOf())
    val extraArray by mutableStateOf(mutableListOf<FloatArray>())
    val smile = MutableStateFlow(false)
    val recognitionIterator = MutableStateFlow(mutableListOf(FaceDirection.FACE_CENTER))
    val sizeOfrecognizedPoses = MutableStateFlow(0)
    val showLoader = MutableStateFlow(false)
    val showCredsScreen = MutableStateFlow(false)
    val credsState = MutableStateFlow(CreditsState())
    val closeCamera = MutableStateFlow(false)
    var faceFlagExtra by mutableStateOf(false)
    val showContentFilter = MutableStateFlow(false)
    val itemsList= MutableStateFlow(spheresOfActivity.map { it to false}.toMutableStateMap())
    val faceNotRecognised = MutableStateFlow<FaceDirection?>(null)
    val userImage = MutableStateFlow<Bitmap?>(null)
    val createCardresponse = MutableStateFlow<ApiResponse<ResponseBody>>(ApiResponse.Loading)


    fun navigateOnSave() = onSaveCard
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
            componentCoroutineScope().launch {
                if(recognitionIterator.value.size < 53) {
                    newList.add(FaceDirection.entries[recognitionIterator.value.size])
                    recognitionIterator.emit(newList)
                }
                if(recognisedPositions.value[faceDirection.name] == null){
                    recognisedPositions.value[faceDirection.name] = floatArray
                }
               if(recognisedPositions.value.keys.size == 53){
                   showChecksLoader()
               }
                sizeOfrecognizedPoses.emit(recognisedPositions.value.keys.size-1)
            }
        }
    }

    init {
        componentCoroutineScope().launch {
            credsState.collect{credits ->
                if( credits.name.isNotEmpty() &&
                    credits.description.isNotEmpty() &&
                    credits.surname.isNotEmpty()
                    ){
                    showContentFilter.emit(true)
                    if(credits.links.isNotEmpty()){
                        showContentFilter.emit(credits.links.values.all { it.link.isNotEmpty() && it.name.isNotEmpty() })
                    }
                } else{
                    showContentFilter.emit(false)
                }
            }
        }
    }

    fun analyze(lensFacing: Int) =  FaceDetectionAccurateAnalyzer{ _faces, width, height, _image ->
        componentCoroutineScope().launch {
            val currentDirection = recognitionIterator.value.last()
            if(currentDirection.name == FaceDirection.ANGLE_SMILE.name){
                smile.emit(true)
            }
            val bitmap = _image.toBitmap().rotate(
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) 270F else 90F
            ).getOrNull()
            faces.value = _faces
            if (_faces.isNotEmpty()) {
                    faceNotRecognised.emit(null)
                    if(currentDirection.name.contains(FaceDirection.FACE_EXTRA.name)) {
                        faceFlagExtra = !faceFlagExtra
                        if (faceFlagExtra) {
                            bitmap?.let {
                                faceProcessor?.detectInImage(
                                    _faces,
                                    it,
                                    faceDirection = currentDirection
                                )
                            }
                        }
                    }else {
                        bitmap?.let {
                            faceProcessor?.detectInImage(
                                _faces,
                                it,
                                faceDirection = currentDirection
                            )
                        }
                    }
                }
        }
    }
    fun setRecognitionModel() {
        faceProcessor = FaceRecognitionProcessorForRegistration(Interpreter(model),callBack)
    }

    fun showChecksLoader(){
        componentCoroutineScope().launch {
            closeCamera.emit(true)
            showLoader.emit(true)
            delay(2000)
            showLoader.emit(false)
            showCredsScreen.emit(true)
        }
    }

    fun createLink(){
        componentCoroutineScope().launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap[creds.links.size+1] = Link()
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }
    fun deleteLink(index: Int){
        componentCoroutineScope().launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap.remove(index)
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }

    fun addNameToLink(position: Int,name: String){
        componentCoroutineScope().launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap[position] = newMap[position]!!.copy(name = name)
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }

    fun addLinkToLink(position: Int,link: String) {
        componentCoroutineScope().launch {
            val creds = credsState.first()
            val newMap = creds.links.toMutableMap()
            newMap[position] = newMap[position]!!.copy(link = link)
            credsState.emit(
                creds.copy(links = newMap.toMap())
            )
        }
    }
    fun setCreds(creds: CreditsState){
        componentCoroutineScope().launch{
            credsState.emit(creds)
        }
    }

    fun showContentFilter(){
        componentCoroutineScope().launch {
            showCredsScreen.emit(false)
        }
    }
    fun saveCard(){
        componentCoroutineScope().launch {
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
                links = creds.links.values.map { LinkEntity(it.name, it.link) }.toList(),
                arrayOfFeatures = features.toList()
            )
            cardInfoRepository.insertCard(person)
            navigateOnSave()
        }
    }
}