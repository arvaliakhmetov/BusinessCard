package com.face.businesscard.ui.face_detector

import FaceRecognitionProcessor
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LensFacing
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.graphics.toComposeRect
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.businesscard.api.ApiRepository
import com.face.businesscard.api.dto.FeatureDto
import com.face.businesscard.api.dto.PersonDto
import com.face.businesscard.database.dao.CardInfoRepository
import com.face.businesscard.ui.ApiResponse
import com.face.businesscard.ui.BaseViewModel
import com.face.businesscard.ui.CoroutinesErrorHandler
import com.face.businesscard.ui.face_detector.utils.MatchedFaceInfo
import com.face.businesscard.ui.face_recognizer.Person
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import flip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import rotate
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class FaceRecognitionViewModel @Inject constructor(
    private val cardInfoRepository: CardInfoRepository,
    private val apiRepository: ApiRepository
): BaseViewModel() {

    private val coroutinesErrorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {
            Log.d("ResponseError",message)
        }
    }

    val recognisedFaceResponse = MutableStateFlow<ApiResponse<PersonDto>>(ApiResponse.Idling)

    var needAnalyzer = MutableStateFlow(true)
    var faceRects = MutableStateFlow(listOf<Rect>())
    val coroutineDispatcher = CoroutineScope(Dispatchers.Default)
    var working by mutableStateOf(false)
    val capturedImage = MutableStateFlow<Bitmap?>(null)
    var lensFacing = CameraSelector.LENS_FACING_BACK
    var imageProxy = mutableStateOf<Bitmap?>(null)
        private set

    var bitmap = mutableStateOf<Bitmap?>(null)
        private set

    val showCropError = mutableStateOf(false)

    private val callBack = object : FaceRecognitionProcessor.FaceRecognitionCallback{
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

    private val captureCallback = object : ImageCapture.OnImageCapturedCallback() {
        @OptIn(ExperimentalGetImage::class) override fun onCaptureSuccess(image: ImageProxy) {
            image.image?.let {
                    Log.d("GET_MATCH_IMAGE_SIZE", image.imageInfo.toString())
                    val _image = InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees,)
                    capturedImage.value = _image.bitmapInternal?.flip(horizontal = lensFacing == CameraSelector.LENS_FACING_FRONT)?.getOrNull()
                    image.close()
            }
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
        }
    }

    var faceProcessor by mutableStateOf<FaceRecognitionProcessor?>(null)
    var imageCapture by mutableStateOf<ImageWithCropCapture?>(null)
    val faceSLIST = MutableStateFlow<List<Person?>>(emptyList())
    val tapMatchedFace = MutableStateFlow(MatchedFaceInfo())

    init {
        viewModelScope.launch {
            recognisedFaceResponse.collect{
                if(it is ApiResponse.Success){
                    Log.d("Response",it.data.toString())
                }
                if(it is ApiResponse.Failure){
                    Log.d("Response",it.errorMessage.toString())
                }
            }
        }
    }

    fun saveScreen(image: Bitmap?){
        image?.let {
            capturedImage.value = image
        }
    }


    @OptIn(ExperimentalGetImage::class)
    fun analyze(face: Face,flip: Boolean){
        coroutineDispatcher.launch {
                faceProcessor?.let { processor ->
                    imageProxy.value?.let {image ->
                        val faceBox = cropToBBox(image,face.boundingBox,if(flip) 270f else 90f )?.flip(horizontal = flip)
                        if (faceBox?.isSuccess == true){
                            val feature = processor.detectInImage(listOf(face),faceBox.getOrNull()!!)
                            feature?.let {
                                Log.d("ARRAY_I", it.toList().toString())
                                findNearestFace(feature)
                            }
                        }else{
                            showCropError.value = true
                        }
                    }
                }
            }
        }

    fun findNearestFace(feature: FloatArray) = baseRequest(
        recognisedFaceResponse,
        coroutinesErrorHandler
    ){
        apiRepository.getPerson(FeatureDto(feature = feature))
    }
    fun setDetectedFacesRects(
        faces: List<Face>,
        imageWidth: Int,
        imageHeight: Int,
        screenWidth: Int,
        screenHeight:Int,
        _imageProxy: Bitmap
    ){
        viewModelScope.launch {
            faceRects.emit(faces.map { it.boundingBox.toComposeRect() })
            imageProxy.value = _imageProxy
        }

    }
    fun setRecognitionModel(recognitionModel: MappedByteBuffer) {
        faceProcessor = FaceRecognitionProcessor(Interpreter(recognitionModel),callBack)
    }
    @OptIn(ExperimentalZeroShutterLag::class)
    fun setImageCapture(){
        imageCapture = ImageWithCropCapture(captureCallback)
    }
    fun needAnalyzer(needAnalyze: Boolean){
        viewModelScope.launch { needAnalyzer.emit(needAnalyze) }
    }
}


fun cropToBBox(_image: Bitmap, boundingBox: android.graphics.Rect, rotation: Float): Bitmap? {
    var image = _image
    val shift = 0
    Log.d("CropToBBox", "BoundingBox: $boundingBox")
    Log.d("CropToBBox", "Image Dimensions: ${image.width} x ${image.height}")
    Log.d("CropToBBox", "Rotation: $rotation")
    if (rotation != 0f) {
        val matrix = Matrix()
        matrix.postRotate(rotation)
        image = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
    }
    return if (
        boundingBox.top >= 0 &&
        boundingBox.bottom <= image.height &&  // Corrected from image.width
        boundingBox.left >= 0 &&  // Corrected from boundingBox.top
        boundingBox.left + boundingBox.width() <= image.width
    ) {
        Bitmap.createBitmap(
            image,
            boundingBox.left,
            boundingBox.top + shift,
            boundingBox.width(),
            boundingBox.height()
        )
    } else null
}





