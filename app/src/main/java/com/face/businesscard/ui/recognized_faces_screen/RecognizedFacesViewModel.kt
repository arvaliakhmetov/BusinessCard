package com.face.businesscard.ui.recognized_faces_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.database.entity.CardInfo
import com.face.businesscard.ui.face_recognizer.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class RecognizedFacesViewModel @Inject constructor(
    private val cardInfoRepository: CardInfoRepository
): ViewModel() {


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

    fun findFace(person: Person?){
        viewModelScope.launch {
            person?.let { _person ->
                findNearestKnownFace(_person.faceVector)?.let { result ->
                    if (result.second <= 0.9) {
                        foundedFace.emit(knownFaces.value.find { it.id.toString() == result.first })
                    }else{
                        foundedFace.emit(CardInfo(id = _person.id.toLong(),surname = "Пользователь не найден"))
                    }
                }
                currentFaceV.emit(_person)
            }
        }

    }

    fun resetFace(){
        foundedFace.value = null
    }

    fun findNearestKnownFace(vector: FloatArray): Pair<String, Float>? {
        val list = mutableListOf<Person?>()
        knownFaces.value.forEach {cardInfo ->
            cardInfo.arrayOfFeatures.forEach { floatArray ->
                list.add(Person(cardInfo.id.toString(),floatArray,null))
            }
        }
        return list.toList()
            .filter { person ->
                person?.faceVector != null && vector.size == person.faceVector.size
            }
            .minByOrNull { person ->
                vector.zip(person!!.faceVector)
                    .map { (a, b) -> (a - b) * (a - b) }
                    .sum()
            }
            ?.let { person ->
                val squaredDistance = vector.zip(person.faceVector)
                    .map { (a, b) -> (a - b) * (a - b) }
                    .sum()
                Pair(person.id, sqrt(squaredDistance.toDouble()).toFloat())
            }
    }
}