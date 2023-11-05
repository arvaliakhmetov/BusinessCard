package com.face.businessface.navigation

sealed class Routes(val route: String) {
    data object HomeRoute: Routes("home_route")
    data object FaceDetectorRoute: Routes("face_detector_route")
}
