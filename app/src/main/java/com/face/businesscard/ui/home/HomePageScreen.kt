package com.face.businesscard.ui.home

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.face.businesscard.database.entity.CardInfo
import com.face.businesscard.navigation.NavigationViewModel
import com.face.businesscard.navigation.Navigator
import com.face.businesscard.navigation.Routes
import com.face.businesscard.navigation.Screen
import com.face.businesscard.navigation.SharedData.Value
import com.face.businesscard.ui.details.DetailScreen
import com.face.businesscard.ui.faceRegistration.FaceRegistration
import com.face.businesscard.ui.face_detector.FaceDetector
import com.face.businesscard.ui.face_recognizer.Person
import com.face.businesscard.ui.profile.ProfileScreen
import com.face.businesscard.ui.profile.ProfileViewModel
import com.face.businesscard.ui.recognized_faces_screen.RecognizedFacesScreen
import com.face.businesscard.ui.theme.BusinessCardTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@Composable
fun HomePageScreen(
  viewModel: NavigationViewModel = hiltViewModel()
) {
  val navController = rememberNavController()
  val showNavBar by viewModel.isNavbarVisible.collectAsState()
  var currentTab by remember { mutableStateOf(Navigator.NavTarget.Home) }


  LaunchedEffect("navigation") {
    viewModel.sharedFlow.onEach { navTarget ->
      if (navTarget != currentTab) navController.navigate(navTarget.label)
      currentTab = navTarget
    }.launchIn(this)
  }
  Column (modifier = Modifier.fillMaxSize()){
    Box(modifier = Modifier.weight(1f)) {
      NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
      ) {

          composable(Screen.Home.route) {
            viewModel.getSharedData()
            BackHandler(enabled = true, onBack = {})
            HomeScreen(
              navigateToFaceDetector = viewModel::navigateToFaceDetector,
            )
          }
          composable(Screen.History.route) {
            val data = viewModel.getSharedData()?.data
            if (data is Pair<*, *>) {
              val second = data.second
              if (second is List<*>) {
                val personList = second.filterIsInstance<Person?>()
                BackHandler(enabled = true, onBack = {})
                DetailScreen(
                  personList,
                  navigateToRecognizedFace = viewModel::navigateToRecognizedFacesScreen
                )
              }
              else{
                DetailScreen(
                  emptyList(),
                  navigateToRecognizedFace = viewModel::navigateToRecognizedFacesScreen
                )
              }
            }else{
              DetailScreen(
                emptyList(),
                navigateToRecognizedFace = viewModel::navigateToRecognizedFacesScreen
              )
            }

          }
          composable(Screen.Profile.route) {
            BackHandler(enabled = true, onBack = {})
            val faces = listOf(CardInfo())
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val knownFaces by profileViewModel.knownFaces.collectAsState()
            ProfileScreen(
              faces = knownFaces,
              navigateToFaceRegistrationScreen = viewModel::navigateToFaceRegistrationScreen,
              navigateToRecognizedface = viewModel::navigateToRecognizedFacesScreen,
              deleteCard = profileViewModel::deletePerson
            )
          }
          composable(route = Screen.FaceDetector.route) {
            FaceDetector(
              navigateBack = viewModel::navigateToHome,
              navigateToRecognizedFacesScreen = viewModel::navigateToRecognizedFacesScreen
            )
          }
          composable(Screen.RecognizedFaceScreen.route){
            val data= viewModel.getSharedData()!!.data as Pair<*, *>
            BackHandler(enabled = true, onBack = viewModel::navigateToHome)
            RecognizedFacesScreen(selectedFace = data.first as String, faces = data.second as List<Person?>,viewModel::navigateToHome)
          }
        composable(Screen.FaceRegistrationScreen.route){
          //val data= viewModel.getSharedData()!!.data as Pair<*, *>
          BackHandler(enabled = true, onBack = viewModel::navigateToProfile)
          FaceRegistration(viewModel::navigateToProfile)
        }

      }
    }
    AnimatedVisibility(visible = showNavBar) {
      NavigationBar(
        modifier = Modifier.fillMaxWidth()
      ) {
        NavBarItems.values().forEach { item ->
          NavigationBarItem(
            selected = currentTab.name == item.name,
            onClick = {
              viewModel.selectOnNavigationBar(item)
            },
            icon = {
              Icon(imageVector = item.icon, contentDescription = item.name)
            })
        }
      }
    }
  }
}