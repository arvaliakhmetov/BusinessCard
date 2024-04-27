package com.face.businessface.ui.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.face.businessface.navigation.RootComponent
import com.face.businessface.ui.details.HistoryScreen
import com.face.businessface.ui.faceRegistration.FaceCreationScreen
import com.face.businessface.ui.face_detector.RecognitionScreen
import com.face.businessface.ui.profile.ProfileScreen
import com.face.businessface.ui.recognized_faces_screen.RecognizedFacesScreen


@Composable
fun HomePageScreen(
  root: RootComponent,
) {
  val childStack by root.childStack.subscribeAsState()
  Children(
    modifier = Modifier.fillMaxSize(),
    animation = stackAnimation(fade(),true),
    stack = childStack
  ) { child ->
    when(val instance = child.instance) {
      is RootComponent.Child.MainScreen -> MainScreen(instance.component)
      is RootComponent.Child.FaceRecognitionScreen -> RecognitionScreen(instance.component.state,instance.component::onAction)
      is RootComponent.Child.HistoryScreen -> HistoryScreen(instance.component)
      is RootComponent.Child.ProfileScreen -> ProfileScreen(instance.component)
      is RootComponent.Child.FaceCreationScreen -> FaceCreationScreen(
        component =instance.component
      )
      is RootComponent.Child.RecognizedFaceScreen -> RecognizedFacesScreen(
        component = instance.component,
        onAction = instance.component::onAction
      )

      else -> {}
    }
    root.showNavBar
  }
    if(childStack.active.instance is RootComponent.Child.MainScreen ||
      childStack.active.instance is RootComponent.Child.HistoryScreen ||
      childStack.active.instance is RootComponent.Child.ProfileScreen){
      BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
      ) {
        NavigationBar(
          modifier = Modifier.fillMaxWidth()
        ) {
          NavBarItem.entries.forEach { item ->
            NavigationBarItem(
              selected = false,
              onClick = {
                root.selectNavBar(item)
              },
              icon = {
                Icon(imageVector = item.icon, contentDescription = item.name)
              })
          }
        }
      }
    }
  }
