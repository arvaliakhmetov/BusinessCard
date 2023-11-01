package com.face.businesscard.ui.recognized_faces_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.api.ApiRepository
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.database.entity.CardInfo
import com.face.businesscard.database.entity.LinkEntity
import com.face.businesscard.ui.ApiResponse
import com.face.businesscard.ui.BaseViewModel
import com.face.businesscard.ui.CoroutinesErrorHandler
import com.face.businesscard.ui.face_recognizer.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class RecognizedFacesViewModel @Inject constructor(
    private val cardInfoRepository: CardInfoRepository,
    private val api: ApiRepository
): BaseViewModel() {


    private val coroutinesErrorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {
            Log.d("ResponseError",message)
        }
    }

    val imageResponse = MutableStateFlow<ApiResponse<ResponseBody>>(ApiResponse.Idling)

    val foundedFace = MutableStateFlow<CardInfo?>(null)
    val currentFaceV = MutableStateFlow<Person?>(null)
    val knownFaces = MutableStateFlow<List<CardInfo>>(emptyList())

    init {
        viewModelScope.launch {
            val faces = cardInfoRepository.getKnownFaces().first()
            /*cardInfoRepository.insertCard(faces.find { it.name == "Артур" }!!.copy(
                secondName = "Раушанович"
            ))*/
            knownFaces.value = faces

            Log.d("Face_known",knownFaces.value.toString())
        }
    }

    fun getImage(id: String) = baseRequest(
        imageResponse,
        coroutinesErrorHandler
    ){
        api.getImage(id)
    }

    fun savePerson(creds: PersonDto){
        viewModelScope.launch {
            cardInfoRepository.insertCard(
               CardInfo(
                    id = creds.id.toLong(),
                    name = creds.name,
                    surname = creds.surname,
                    secondName = creds.second_name,
                    company = creds.company,
                    jobtitle = creds.jobtitile,
                    description = creds.description,
                    activities = creds.activities,
                    links = creds.conts.map { LinkEntity(it.key,it.value) }.toList(),
                    arrayOfFeatures = listOf(FloatArray(1))
                )
            )

        }
    }
}