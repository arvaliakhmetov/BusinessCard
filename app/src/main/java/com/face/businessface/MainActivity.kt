package com.face.businessface

import FaceRecognitionProcessor
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.defaultComponentContext
import com.face.businessface.database.dao.CardInfoRepository
import com.face.businessface.navigation.RootComponent
import com.face.businessface.ui.home.HomePageScreen
import com.face.businessface.ui.theme.BusinessFaceTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var cardInfoRepository: CardInfoRepository



    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val recognitionModel = FaceRecognitionProcessor(
            faceNetModelInterpreter = Interpreter(FileUtil.loadMappedFile(this@MainActivity, "mobile_face_net.tflite"))
        )
        val faceNetModel = FileUtil.loadMappedFile(this@MainActivity, "mobile_face_net.tflite")
        val root =
            RootComponent(
                componentContext = defaultComponentContext(),
                cardInfoRepository,
                recognitionModel,
                faceNetModel,
            )
        setContent {
            BusinessFaceTheme {
                val permissionState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.CAMERA
                    )
                )

                LaunchedEffect(Unit) {
                    permissionState.launchMultiplePermissionRequest()
                }
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomePageScreen(root)
                }
            }
        }
    }
}


