package com.face.businessface.ui.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val cardInfoRepository: CardInfoRepository,
): BaseViewModel(){



    val knownFaces = MutableStateFlow<List<CardInfo>>(emptyList())

    init {
        viewModelScope.launch {

            Log.d("Face_known",knownFaces.value.toString())
        }
    }



    fun deletePerson(id:Long){
        viewModelScope.launch {
            cardInfoRepository.deleteCard(id)
        }
    }
}