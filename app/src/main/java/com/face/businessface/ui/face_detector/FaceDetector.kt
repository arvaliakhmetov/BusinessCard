package com.face.businessface.ui.face_detector

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.face.businessface.api.dto.PersonDto
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.nio.MappedByteBuffer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceDetector(
    recognitionModel: MappedByteBuffer,
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
                ScanSurface(recognitionModel,navigateBack,navigateToRecognizedFacesScreen)
            }
        }

}