package com.face.businesscard.ui.details

import androidx.lifecycle.viewModelScope
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.ui.BaseViewModel
import com.face.businesscard.ui.face_recognizer.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor(
    cardInfoRepository: CardInfoRepository
):BaseViewModel() {
    val personsList = MutableStateFlow<List<PersonDto>>(emptyList())



    init {
        viewModelScope.launch{
            personsList.value = cardInfoRepository.getKnownFaces().first().map {
                PersonDto(
                    id = it.id!!.toInt(),
                    name = it.name,
                    surname = it.surname,
                    second_name = it.secondName,
                    company = it.company,
                    jobtitile = it.jobtitle,
                    activities = it.activities,
                    conts = it.links.map { it.name to it.link }.toMap(),
                    description = it.description,
                    dist = 0f
                )
            }
        }
    }


}