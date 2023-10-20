package com.face.businesscard.navigation

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.ui.face_recognizer.Person
import com.face.businesscard.ui.home.NavBarItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val navigator: Navigator,
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {

    val sharedFlow = navigator.sharedFlow
    val isNavbarVisible = MutableStateFlow(true)

    init {
        sharedFlow.onEach {sharedNavTarget  ->
            if(!NavBarItems.values().map { it.name }.contains(sharedNavTarget.name))
                isNavbarVisible.tryEmit(false)
            else
                isNavbarVisible.tryEmit(true)
        }.launchIn(viewModelScope)
    }

    fun navigateToDetail() {
        navigator.navigateTo(Navigator.NavTarget.History)
    }

    fun navigateToHome(){
        navigator.navigateTo(Navigator.NavTarget.Home)
    }
    fun navigateToHistory(){
        navigator.navigateTo(Navigator.NavTarget.History)
    }
    fun navigateToProfile(){
        navigator.navigateTo(Navigator.NavTarget.Profile)
    }
    fun navigateToRecognizedFacesScreen(id: String,faces:List<Person?>){
        setData(SharedData.Value(Pair(id,faces)))
        navigator.navigateTo(Navigator.NavTarget.RecognizedFacesScreen)
    }
    fun navigateToFaceRegistrationScreen(){
        navigator.navigateTo(Navigator.NavTarget.FaceRegistrationScreen)
    }
    fun navigateToFaceDetector(){
        navigator.navigateTo(Navigator.NavTarget.FaceDetectorScreen)
    }
    fun selectOnNavigationBar(navBarItem: NavBarItems){
        navigator.navigateTo(Navigator.NavTarget.valueOf(navBarItem.name))
    }

    fun setData(data: SharedData.Value<*>) {
        sharedDataRepository.setData(data)
    }

    fun getSharedData(): SharedData.Value<*>? {
        return sharedDataRepository.getShareData()
    }
}