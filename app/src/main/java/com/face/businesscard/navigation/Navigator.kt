package com.face.businesscard.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.List
import androidx.compose.material.icons.twotone.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class Navigator {
    private val mShareFlow = MutableSharedFlow<NavTarget>(extraBufferCapacity = 1)
    val sharedFlow = mShareFlow.asSharedFlow()

    init {
        navigateTo(NavTarget.Home)
    }
    fun navigateTo(navTarget: NavTarget) {
      mShareFlow.tryEmit(navTarget)
    }

    enum class NavTarget(val label: String) {
        Home("home"),
        History("history"),
        Profile("profile"),
        FaceDetectorScreen("face_detector"),
        RecognizedFacesScreen("RecognizedFacesScreen"),
        FaceRegistrationScreen("FaceRegistrationScreen")
    }
  }