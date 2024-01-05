package com.face.businessface.ui.profile

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.arkivanov.decompose.ComponentContext
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.ui.mvicore.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class ProfileScreenComponent (
    componentContext: ComponentContext,
    private val onNavigateToCardCreation:()-> Unit
): ComponentContext by componentContext {



    fun navigateToFaceCreation() = onNavigateToCardCreation.invoke()
}