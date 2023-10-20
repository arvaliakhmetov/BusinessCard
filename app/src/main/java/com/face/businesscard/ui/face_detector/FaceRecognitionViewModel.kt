package com.face.businesscard.ui.face_detector

import FaceRecognitionProcessor
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.ui.face_recognizer.Person
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import flip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import rotate
import java.nio.MappedByteBuffer
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class FaceRecognitionViewModel @Inject constructor(

): ViewModel() {

    var needAnalyzer = MutableStateFlow(true)
    val coroutineDispatcher = CoroutineScope(Dispatchers.Default)
    var working by mutableStateOf(false)
    var imageProxy = mutableStateOf<ImageProxy?>(null)
        private set

    var bitmap = mutableStateOf<Bitmap?>(null)
        private set

    val callBack = object : FaceRecognitionProcessor.FaceRecognitionCallback{
        override fun onFaceRecognised(face: Face?, probability: Float, name: String?) {
            faceProcessor?.let {

                Log.d("узнал ЛИЦО",name.toString())
            }

        }

        override fun onFaceDetected(face: Face?, faceBitmap: Bitmap?, vector: FloatArray?) {
            faceProcessor?.let {
                faceSLIST.value = it.recognisedFaceList.toList()
                Log.d("распознал",face?.trackingId.toString())
            }
        }
    }

   var faceProcessor by mutableStateOf<FaceRecognitionProcessor?>(null)

    val faceSLIST = MutableStateFlow<List<Person?>>(emptyList())





    fun analyze(lensFacing: Int) =  FaceDetectionAnalyzer { faces, width, height, _image ->
        if(needAnalyzer.value) {
            var rotation= 0F
            val bitmap = _image.toBitmap().rotate(
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    _image.imageInfo.rotationDegrees.toFloat()

                } else {
                    rotation = _image.imageInfo.rotationDegrees.toFloat()
                    rotation
                }
            ).onSuccess {
                if (faces.isNotEmpty()) {
                    coroutineDispatcher.launch {
                        if (!working) {
                            faceProcessor?.let { processor ->
                                working = true
                                working = processor.detectInImage(faces, it, 0F)
                                faceSLIST.emit(faceProcessor!!.recognisedFaceList.toList())
                            }
                        }
                    }
                }

            }
        }
    }
    fun setRecognitionModel(recognitionModel: MappedByteBuffer) {
        faceProcessor = FaceRecognitionProcessor(Interpreter(recognitionModel),callBack)
    }
    fun needAnalyzer(needAnalyze: Boolean){
        viewModelScope.launch { needAnalyzer.emit(needAnalyze) }
    }
}