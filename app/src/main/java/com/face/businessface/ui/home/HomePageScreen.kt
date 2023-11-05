package com.face.businessface.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.face.businessface.api.dto.PersonDto
import com.face.businessface.database.entity.CardInfo
import com.face.businessface.navigation.NavigationViewModel
import com.face.businessface.navigation.Navigator
import com.face.businessface.navigation.Screen
import com.face.businessface.ui.details.DetailScreen
import com.face.businessface.ui.details.DetailsScreenViewModel
import com.face.businessface.ui.faceRegistration.FaceRegistration
import com.face.businessface.ui.face_detector.FaceDetector
import com.face.businessface.ui.profile.ProfileScreen
import com.face.businessface.ui.profile.ProfileViewModel
import com.face.businessface.ui.recognized_faces_screen.RecognizedFacesScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.tensorflow.lite.support.common.FileUtil


@Composable
fun HomePageScreen(
  viewModel: NavigationViewModel = hiltViewModel(),
  detailViewModel: DetailsScreenViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val navController = rememberNavController()
  val showNavBar by viewModel.isNavbarVisible.collectAsState()
  var currentTab by remember { mutableStateOf(Navigator.NavTarget.Home) }
  val recognitionModel = FileUtil.loadMappedFile(context, "mobile_face_net.tflite")


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
              BackHandler(enabled = true, onBack = viewModel::navigateToHome)
              DetailScreen(
                navigateToRecognizedFace = viewModel::navigateToRecognizedFacesScreen,
              )
          }
          composable(Screen.Profile.route) {
            BackHandler(enabled = true, onBack = viewModel::navigateToHome)
            val faces = listOf(CardInfo())
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val knownFaces by profileViewModel.knownFaces.collectAsState()
            ProfileScreen(
              faces = knownFaces,
              navigateToFaceRegistrationScreen = viewModel::navigateToFaceRegistrationScreen,
              deleteCard = profileViewModel::deletePerson,
            )
          }
          composable(route = Screen.FaceDetector.route) {
            FaceDetector(
              recognitionModel = recognitionModel,
              navigateBack = viewModel::navigateToHome,
              navigateToRecognizedFacesScreen = viewModel::navigateToRecognizedFacesScreen
            )
          }
          composable(Screen.RecognizedFaceScreen.route){
            val data= viewModel.getSharedData()?.data
            val person = if(data is PersonDto) data else null
            BackHandler(enabled = true, onBack = viewModel::navigateToHome)
            RecognizedFacesScreen(person, onDelete=viewModel::navigateToHome,viewModel::navigateToFaceDetector)
          }
        composable(Screen.FaceRegistrationScreen.route){
          BackHandler(enabled = true, onBack = viewModel::navigateToProfile)
          FaceRegistration(
            recognitionModel,
            viewModel::navigateToProfile
          )
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