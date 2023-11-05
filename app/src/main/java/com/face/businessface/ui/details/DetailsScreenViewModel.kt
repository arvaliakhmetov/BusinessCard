package com.face.businessface.ui.details

import androidx.lifecycle.viewModelScope
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor(
    val cardInfoRepository: CardInfoRepository
):BaseViewModel() {
    val personsList = MutableStateFlow<List<CardInfo>>(emptyList())



    fun getData(){
        viewModelScope.launch{
            cardInfoRepository.getKnownFaces().collect{
                personsList.value = it
            }
        }
    }


}