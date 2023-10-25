package com.face.businesscard.ui.face_detector

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.ui.face_recognizer.Person
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceDetector(
    navigateBack:() -> Unit,
    navigateToRecognizedFacesScreen:(PersonDto)-> Unit
){
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

        // Rest of the compose code will be here
        BackHandler(
            enabled = true,
            onBack = navigateBack
        )
        if(permissionState.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ScanSurface(navigateBack,navigateToRecognizedFacesScreen)
            }
        }

}