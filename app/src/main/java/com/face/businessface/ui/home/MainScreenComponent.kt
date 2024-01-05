package com.face.businessface.ui.home

import com.arkivanov.decompose.ComponentContext
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.navigation.RootComponent

class MainScreenComponent(
    componentContext: ComponentContext,
    private val onNavigateToFaceRecognitionScreen:()-> Unit
): ComponentContext by componentContext {
    fun navigate() = onNavigateToFaceRecognitionScreen

}
