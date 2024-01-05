package com.face.businessface.ui.details

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.navigation.componentCoroutineScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class HistoryScreenComponent (
    componentContext: ComponentContext,
    private val cardInfoRepository: CardInfoRepository,
    private val onNavigateToRecognizedFaceScreen:(id:Long)-> Unit
): ComponentContext by componentContext {
    val personsList = MutableValue<List<CardInfo>>(emptyList())

    init {
        getData()
    }

    fun navigateToPerson(id: Long) = onNavigateToRecognizedFaceScreen.invoke(id)

    fun getData(){
        componentCoroutineScope().launch{
            personsList.value = cardInfoRepository.getKnownFaces().first()
        }
    }


}