package com.face.businessface.ui.faceRegistration

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.nio.MappedByteBuffer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceRegistration(
    recognitionModel: MappedByteBuffer,
    navigateToMain:() -> Unit
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
    if(permissionState.allPermissionsGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Green)
        ) {
            CardCreation(recognitionModel,navigateToMain)
        }
    }
}