package com.face.businessface.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object FaceDetector: Screen("face_detector")
    data object DashWithNavTabs: Screen("dash_with_tabs")
    data object FoundedProfile: Screen("founded_profile")
    data object SelectPeople: Screen("select_people")
    data object Auth: Screen("auth")
    data object Login: Screen("login")
    data object RecognizedFaceScreen: Screen("RecognizedFacesScreen")
    data object FaceRegistrationScreen: Screen("FaceRegistrationScreen")
}
