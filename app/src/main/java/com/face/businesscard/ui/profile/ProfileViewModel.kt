package com.face.businesscard.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.database.entity.CardInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val cardInfoRepository: CardInfoRepository
): ViewModel(){
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

    fun deletePerson(id:Long){
        viewModelScope.launch {
            cardInfoRepository.deleteCard(id)
        }
    }
}